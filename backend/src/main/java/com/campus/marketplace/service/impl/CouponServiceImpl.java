package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.CouponResponse;
import com.campus.marketplace.common.entity.Coupon;
import com.campus.marketplace.common.entity.CouponUserRelation;
import com.campus.marketplace.common.enums.CouponStatus;
import com.campus.marketplace.common.enums.CouponType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.CouponRepository;
import com.campus.marketplace.repository.CouponUserRelationRepository;
import com.campus.marketplace.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Coupon Service Impl
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponUserRelationRepository relationRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Coupon createCoupon(String code, String name, CouponType type,
                                BigDecimal discountAmount, BigDecimal discountRate, BigDecimal minAmount,
                                Integer totalCount, Integer limitPerUser,
                                LocalDateTime startTime, LocalDateTime endTime, String description) {
        log.info("创建优惠券: code={}, name={}, type={}", code, name, type);

        // 检查代码是否重复
        if (couponRepository.existsByCode(code)) {
            log.warn("优惠券代码已存在: code={}", code);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券代码已存在");
        }

        // 验证时间
        if (endTime.isBefore(startTime)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "结束时间不能早于开始时间");
        }

        Coupon coupon = Coupon.builder()
                .code(code)
                .name(name)
                .type(type)
                .discountAmount(discountAmount)
                .discountRate(discountRate)
                .minAmount(minAmount)
                .totalCount(totalCount)
                .receivedCount(0)
                .usedCount(0)
                .limitPerUser(limitPerUser)
                .startTime(startTime)
                .endTime(endTime)
                .description(description)
                .isActive(true)
                .build();

        couponRepository.save(coupon);
        log.info("优惠券创建成功: id={}, code={}", coupon.getId(), coupon.getCode());

        return coupon;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponUserRelation receiveCoupon(Long userId, Long couponId) {
        log.info("领取优惠券: userId={}, couponId={}", userId, couponId);

        // 查询优惠券
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在"));

        // 检查是否已过期
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券未开始");
        }
        if (now.isAfter(coupon.getEndTime())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券已过期");
        }

        // 检查是否激活
        if (!coupon.getIsActive()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券已停用");
        }

        // 检查库存
        if (coupon.getReceivedCount() >= coupon.getTotalCount()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券已领完");
        }

        // 检查领取限制
        if (coupon.getLimitPerUser() != null) {
            long receivedCount = relationRepository.countByUserIdAndCouponId(userId, couponId);
            if (receivedCount >= coupon.getLimitPerUser()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "已达领取上限");
            }
        }

        // 创建领取记录
        CouponUserRelation relation = CouponUserRelation.builder()
                .userId(userId)
                .couponId(couponId)
                .status(CouponStatus.AVAILABLE)
                .receiveTime(now)
                .expireTime(coupon.getEndTime())
                .build();

        relationRepository.save(relation);

        // 更新领取数量
        coupon.setReceivedCount(coupon.getReceivedCount() + 1);
        couponRepository.save(coupon);

        log.info("优惠券领取成功: userId={}, couponId={}, relationId={}", userId, couponId, relation.getId());

        return relation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useCoupon(Long userId, Long relationId, Long orderId) {
        log.info("使用优惠券: userId={}, relationId={}, orderId={}", userId, relationId, orderId);

        // 查询用户优惠券
        CouponUserRelation relation = relationRepository.findByIdAndUserIdAndStatus(
                relationId, userId, CouponStatus.AVAILABLE
        ).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在或不可用"));

        // 检查是否过期
        if (LocalDateTime.now().isAfter(relation.getExpireTime())) {
            relation.setStatus(CouponStatus.EXPIRED);
            relationRepository.save(relation);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券已过期");
        }

        // 标记为已使用
        relation.setStatus(CouponStatus.USED);
        relation.setUseTime(LocalDateTime.now());
        relation.setOrderId(orderId);
        relationRepository.save(relation);

        // 更新使用数量
        Coupon coupon = couponRepository.findById(relation.getCouponId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在"));
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);

        log.info("优惠券使用成功: userId={}, relationId={}, orderId={}", userId, relationId, orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundCoupon(Long relationId) {
        log.info("退还优惠券: relationId={}", relationId);

        CouponUserRelation relation = relationRepository.findById(relationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券记录不存在"));

        if (relation.getStatus() != CouponStatus.USED) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券状态不正确");
        }

        // 检查是否过期
        if (LocalDateTime.now().isAfter(relation.getExpireTime())) {
            relation.setStatus(CouponStatus.EXPIRED);
        } else {
            relation.setStatus(CouponStatus.AVAILABLE);
        }
        
        relation.setUseTime(null);
        relation.setOrderId(null);
        relationRepository.save(relation);

        // 更新使用数量
        Coupon coupon = couponRepository.findById(relation.getCouponId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在"));
        coupon.setUsedCount(coupon.getUsedCount() - 1);
        couponRepository.save(coupon);

        log.info("优惠券退还成功: relationId={}", relationId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponResponse> listAvailableCoupons(int page, int size) {
        log.info("查询可用优惠券列表: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Coupon> couponPage = couponRepository.findAll(pageable);

        return couponPage.map(CouponResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponUserRelation> listUserCoupons(Long userId, int page, int size) {
        log.info("查询用户优惠券列表: userId={}, page={}, size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "receiveTime"));
        return relationRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(Long couponId, BigDecimal originalAmount) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在"));

        // 检查最低消费
        if (coupon.getMinAmount() != null && originalAmount.compareTo(coupon.getMinAmount()) < 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, 
                    "订单金额未达到优惠券使用条件（最低" + coupon.getMinAmount() + "元）");
        }

        BigDecimal discount = BigDecimal.ZERO;

        switch (coupon.getType()) {
            case FIXED:
                // 满减券
                discount = coupon.getDiscountAmount();
                break;
            case PERCENT:
                // 折扣券
                discount = originalAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscountRate()))
                        .setScale(2, RoundingMode.HALF_UP);
                break;
            case FREE_SHIPPING:
                // 包邮券不计算折扣
                discount = BigDecimal.ZERO;
                break;
        }

        log.info("计算优惠金额: couponId={}, originalAmount={}, discount={}", 
                couponId, originalAmount, discount);

        return discount;
    }
}
