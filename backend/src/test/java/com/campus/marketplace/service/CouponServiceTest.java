package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.Coupon;
import com.campus.marketplace.common.entity.CouponUserRelation;
import com.campus.marketplace.common.enums.CouponStatus;
import com.campus.marketplace.common.enums.CouponType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.repository.CouponRepository;
import com.campus.marketplace.repository.CouponUserRelationRepository;
import com.campus.marketplace.service.impl.CouponServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("优惠券服务测试")
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUserRelationRepository relationRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    private Coupon testCoupon;

    @BeforeEach
    void setUp() {
        testCoupon = Coupon.builder()
                .code("SAVE10")
                .name("满100减10")
                .type(CouponType.FIXED)
                .discountAmount(new BigDecimal("10.00"))
                .minAmount(new BigDecimal("100.00"))
                .totalCount(100)
                .receivedCount(0)
                .usedCount(0)
                .limitPerUser(1)
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .build();
        testCoupon.setId(1L);
    }

    @Test
    @DisplayName("创建优惠券成功")
    void createCoupon_Success() {
        when(couponRepository.existsByCode("SAVE10")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenReturn(testCoupon);

        Coupon result = couponService.createCoupon(
                "SAVE10", "满100减10", CouponType.FIXED,
                new BigDecimal("10.00"), null, new BigDecimal("100.00"),
                100, 1, testCoupon.getStartTime(), testCoupon.getEndTime(),
                "测试优惠券"
        );

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("SAVE10");
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    @DisplayName("创建优惠券失败-代码重复")
    void createCoupon_DuplicateCode() {
        when(couponRepository.existsByCode("SAVE10")).thenReturn(true);

        assertThatThrownBy(() -> couponService.createCoupon(
                "SAVE10", "满100减10", CouponType.FIXED,
                new BigDecimal("10.00"), null, new BigDecimal("100.00"),
                100, 1, testCoupon.getStartTime(), testCoupon.getEndTime(),
                "测试优惠券"
        )).isInstanceOf(BusinessException.class)
          .hasMessageContaining("优惠券代码已存在");
    }

    @Test
    @DisplayName("领取优惠券成功")
    void receiveCoupon_Success() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(relationRepository.countByUserIdAndCouponId(1L, 1L)).thenReturn(0L);
        when(relationRepository.save(any(CouponUserRelation.class))).thenAnswer(invocation -> {
            CouponUserRelation relation = invocation.getArgument(0);
            relation.setId(100L);
            return relation;
        });

        CouponUserRelation result = couponService.receiveCoupon(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getCouponId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(CouponStatus.AVAILABLE);
        verify(couponRepository).save(any(Coupon.class)); // 增加领取数量
    }

    @Test
    @DisplayName("领取优惠券失败-优惠券不存在")
    void receiveCoupon_NotFound() {
        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.receiveCoupon(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("优惠券不存在");
    }

    @Test
    @DisplayName("领取优惠券失败-已达领取上限")
    void receiveCoupon_LimitExceeded() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(relationRepository.countByUserIdAndCouponId(1L, 1L)).thenReturn(1L);

        assertThatThrownBy(() -> couponService.receiveCoupon(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已达领取上限");
    }

    @Test
    @DisplayName("领取优惠券失败-库存不足")
    void receiveCoupon_NoStock() {
        testCoupon.setReceivedCount(100); // 已领完
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));

        assertThatThrownBy(() -> couponService.receiveCoupon(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("优惠券已领完");
    }

    @Test
    @DisplayName("使用优惠券成功")
    void useCoupon_Success() {
        CouponUserRelation relation = CouponUserRelation.builder()
                .userId(1L)
                .couponId(1L)
                .status(CouponStatus.AVAILABLE)
                .receiveTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusDays(30))
                .build();
        relation.setId(100L);

        when(relationRepository.findByIdAndUserIdAndStatus(100L, 1L, CouponStatus.AVAILABLE))
                .thenReturn(Optional.of(relation));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(relationRepository.save(any(CouponUserRelation.class))).thenReturn(relation);

        couponService.useCoupon(1L, 100L, 1000L);

        assertThat(relation.getStatus()).isEqualTo(CouponStatus.USED);
        assertThat(relation.getOrderId()).isEqualTo(1000L);
        assertThat(relation.getUseTime()).isNotNull();
        verify(couponRepository).save(any(Coupon.class)); // 增加使用数量
    }

    @Test
    @DisplayName("使用优惠券失败-不存在或不可用")
    void useCoupon_NotAvailable() {
        when(relationRepository.findByIdAndUserIdAndStatus(999L, 1L, CouponStatus.AVAILABLE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.useCoupon(1L, 999L, 1000L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("优惠券不存在或不可用");
    }
}
