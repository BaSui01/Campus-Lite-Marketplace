package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.entity.PaymentLog;
import com.campus.marketplace.common.entity.RefundRequest;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.common.enums.PaymentMethod;
import com.campus.marketplace.common.enums.RefundStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.PaymentLogRepository;
import com.campus.marketplace.repository.RefundRequestRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.RefundServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Refund Service Test
 *
 * @author BaSui
 * @date 2025-10-29
 */

@ExtendWith(MockitoExtension.class)
@DisplayName("退款服务测试")
class RefundServiceTest {

    @Mock
    private RefundRequestRepository refundRepository;
    @Mock
    private PaymentLogRepository paymentLogRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private RefundServiceImpl refundService;

    private MockedStatic<com.campus.marketplace.common.utils.SecurityUtil> secMock;

    @BeforeEach
    void setup() {
        secMock = mockStatic(com.campus.marketplace.common.utils.SecurityUtil.class);
        secMock.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUsername)
                .thenReturn("buyer");
    }

    @AfterEach
    void tearDown() {
        if (secMock != null) secMock.close();
    }

    @Test
    @DisplayName("买家申请退款成功")
    void apply_success() {
        User buyer = User.builder().username("buyer").build();
        buyer.setId(1L);
        Order order = Order.builder().orderNo("ORD1").buyerId(1L).status(OrderStatus.PAID)
                .paymentMethod(PaymentMethod.WECHAT.name())
                .actualAmount(new BigDecimal("10"))
                .build();
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findByOrderNo("ORD1")).thenReturn(Optional.of(order));
        when(refundRepository.existsActiveByOrderNo("ORD1")).thenReturn(false);
        when(refundRepository.save(any(RefundRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        String refundNo = refundService.applyRefund("ORD1", "不想要了", Map.of());
        assertThat(refundNo).startsWith("RFD");
    }

    @Test
    @DisplayName("申请退款-非买家应拒绝")
    void apply_shouldRejectWhenNotBuyer() {
        User buyer = User.builder().username("buyer").build();
        buyer.setId(1L);
        Order order = Order.builder().orderNo("ORD1").buyerId(2L).status(OrderStatus.PAID)
                .paymentMethod(PaymentMethod.WECHAT.name())
                .actualAmount(new BigDecimal("10"))
                .build();
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findByOrderNo("ORD1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> refundService.applyRefund("ORD1", "原因", Map.of()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", com.campus.marketplace.common.exception.ErrorCode.PERMISSION_DENIED.getCode());

        verify(refundRepository, never()).save(any());
    }

    @Test
    @DisplayName("审批退款成功应写入日志并通知")
    void approve_refund_success() {
        RefundRequest refund = RefundRequest.builder()
                .refundNo("RFD1")
                .orderNo("ORD1")
                .status(RefundStatus.APPLIED)
                .amount(new BigDecimal("10"))
                .channel(PaymentMethod.ALIPAY.name())
                .build();
        Order order = Order.builder()
                .orderNo("ORD1")
                .paymentMethod(PaymentMethod.ALIPAY.name())
                .buyerId(1L)
                .sellerId(2L)
                .build();
        when(refundRepository.findByRefundNo("RFD1")).thenReturn(Optional.of(refund));
        when(orderRepository.findByOrderNo("ORD1")).thenReturn(Optional.of(order));
        when(paymentService.refund(order, refund.getAmount(), PaymentMethod.ALIPAY)).thenReturn(true);
        when(refundRepository.save(any(RefundRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        refundService.approveAndRefund("RFD1");

        assertThat(refund.getStatus()).isEqualTo(RefundStatus.REFUNDED);
        verify(paymentLogRepository).save(argThat(log ->
                log.getType() == com.campus.marketplace.common.enums.PaymentLogType.REFUND && log.getSuccess()));
        verify(notificationService).sendNotification(eq(1L), any(com.campus.marketplace.common.enums.NotificationType.class),
                contains("退款成功"), anyString(), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("管理员审批发起退款-渠道失败")
    void approve_refund_failed() {
        RefundRequest refund = RefundRequest.builder()
                .refundNo("RFD1")
                .orderNo("ORD1")
                .status(RefundStatus.APPLIED)
                .amount(new BigDecimal("10"))
                .channel(PaymentMethod.ALIPAY.name())
                .build();
        Order order = Order.builder().orderNo("ORD1").paymentMethod(PaymentMethod.ALIPAY.name()).build();
        when(refundRepository.findByRefundNo("RFD1")).thenReturn(Optional.of(refund));
        when(orderRepository.findByOrderNo("ORD1")).thenReturn(Optional.of(order));
        when(refundRepository.save(any(RefundRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentService.refund(eq(order), any(), eq(PaymentMethod.ALIPAY))).thenReturn(false);

        assertThatThrownBy(() -> refundService.approveAndRefund("RFD1"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("审批退款-状态已退款应直接返回")
    void approve_refund_alreadyProcessed_shouldBeIdempotent() {
        RefundRequest refund = RefundRequest.builder()
                .refundNo("RFD1")
                .orderNo("ORD1")
                .status(RefundStatus.REFUNDED)
                .build();
        when(refundRepository.findByRefundNo("RFD1")).thenReturn(Optional.of(refund));

        refundService.approveAndRefund("RFD1");

        verify(paymentService, never()).refund(any(), any(), any());
    }

    @Test
    @DisplayName("拒绝退款应更新状态")
    void reject_shouldUpdateStatus() {
        RefundRequest refund = RefundRequest.builder()
                .refundNo("RFD1")
                .orderNo("ORD1")
                .status(RefundStatus.APPLIED)
                .build();
        when(refundRepository.findByRefundNo("RFD1")).thenReturn(Optional.of(refund));

        refundService.reject("RFD1", "商品完好");

        assertThat(refund.getStatus()).isEqualTo(RefundStatus.REJECTED);
        verify(refundRepository, times(1)).save(refund);
    }

    @Test
    @DisplayName("退款回调成功应标记已退款并通知")
    void handleCallback_success() {
        RefundRequest refund = RefundRequest.builder()
                .refundNo("RFD1")
                .orderNo("ORD1")
                .status(RefundStatus.PROCESSING)
                .build();
        Order order = Order.builder().orderNo("ORD1").buyerId(5L).build();
        when(refundRepository.findByOrderNo("ORD1")).thenReturn(Optional.of(refund));
        when(orderRepository.findByOrderNo("ORD1")).thenReturn(Optional.of(order));

        boolean success = refundService.handleRefundCallback("ORD1", PaymentMethod.ALIPAY.name(), true, Map.of("tradeNo", "TN1"));

        assertThat(success).isTrue();
        assertThat(refund.getStatus()).isEqualTo(RefundStatus.REFUNDED);
        verify(paymentLogRepository).save(argThat(PaymentLog::getSuccess));
        verify(notificationService).sendNotification(eq(5L), any(com.campus.marketplace.common.enums.NotificationType.class),
                contains("退款成功"), anyString(), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("退款回调失败应累计重试次数")
    void handleCallback_failure() {
        RefundRequest refund = RefundRequest.builder()
                .refundNo("RFD1")
                .orderNo("ORD1")
                .status(RefundStatus.PROCESSING)
                .retryCount(1)
                .build();
        Order order = Order.builder().orderNo("ORD1").buyerId(5L).build();
        when(refundRepository.findByOrderNo("ORD1")).thenReturn(Optional.of(refund));
        when(orderRepository.findByOrderNo("ORD1")).thenReturn(Optional.of(order));

        boolean success = refundService.handleRefundCallback("ORD1", PaymentMethod.ALIPAY.name(), false, Map.of("error", "oops"));

        assertThat(success).isFalse();
        assertThat(refund.getStatus()).isEqualTo(RefundStatus.FAILED);
        assertThat(refund.getRetryCount()).isEqualTo(2);
        assertThat(refund.getLastError()).isEqualTo("oops");
    }

    @Test
    @DisplayName("用户查询自己的退款列表 - 分页成功")
    void listMyRefunds_success() {
        User buyer = User.builder().username("buyer").build();
        buyer.setId(1L);
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));

        RefundRequest refund1 = RefundRequest.builder()
                .refundNo("RFD1")
                .orderNo("ORD1")
                .applicantId(1L)
                .status(RefundStatus.APPLIED)
                .amount(new BigDecimal("100"))
                .build();
        RefundRequest refund2 = RefundRequest.builder()
                .refundNo("RFD2")
                .orderNo("ORD2")
                .applicantId(1L)
                .status(RefundStatus.REFUNDED)
                .amount(new BigDecimal("200"))
                .build();

        org.springframework.data.domain.Page<RefundRequest> page = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(refund1, refund2),
                org.springframework.data.domain.PageRequest.of(0, 10),
                2
        );
        when(refundRepository.findByApplicantId(eq(1L), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        org.springframework.data.domain.Page<RefundRequest> result = refundService.listMyRefunds(0, 10, null);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(refundRepository).findByApplicantId(eq(1L), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("用户查询自己的退款列表 - 按状态筛选")
    void listMyRefunds_withStatusFilter() {
        User buyer = User.builder().username("buyer").build();
        buyer.setId(1L);
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));

        RefundRequest refund = RefundRequest.builder()
                .refundNo("RFD1")
                .orderNo("ORD1")
                .applicantId(1L)
                .status(RefundStatus.REFUNDED)
                .amount(new BigDecimal("100"))
                .build();

        org.springframework.data.domain.Page<RefundRequest> page = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(refund),
                org.springframework.data.domain.PageRequest.of(0, 10),
                1
        );
        when(refundRepository.findByApplicantIdAndStatus(eq(1L), eq(RefundStatus.REFUNDED), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        org.springframework.data.domain.Page<RefundRequest> result = refundService.listMyRefunds(0, 10, RefundStatus.REFUNDED);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(RefundStatus.REFUNDED);
        verify(refundRepository).findByApplicantIdAndStatus(eq(1L), eq(RefundStatus.REFUNDED), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("用户查询自己的退款详情 - 成功")
    void getMyRefund_success() {
        User buyer = User.builder().username("buyer").build();
        buyer.setId(1L);
        RefundRequest refund = RefundRequest.builder()
                .refundNo("RFD1")
                .orderNo("ORD1")
                .applicantId(1L)
                .status(RefundStatus.APPLIED)
                .amount(new BigDecimal("100"))
                .build();

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(refundRepository.findByRefundNo("RFD1")).thenReturn(Optional.of(refund));

        RefundRequest result = refundService.getMyRefund("RFD1");

        assertThat(result.getRefundNo()).isEqualTo("RFD1");
        assertThat(result.getApplicantId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("用户查询他人的退款详情 - 应拒绝")
    void getMyRefund_shouldRejectWhenNotOwner() {
        User buyer = User.builder().username("buyer").build();
        buyer.setId(1L);
        RefundRequest refund = RefundRequest.builder()
                .refundNo("RFD1")
                .orderNo("ORD1")
                .applicantId(2L) // 不是当前用户
                .status(RefundStatus.APPLIED)
                .amount(new BigDecimal("100"))
                .build();

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(refundRepository.findByRefundNo("RFD1")).thenReturn(Optional.of(refund));

        assertThatThrownBy(() -> refundService.getMyRefund("RFD1"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", com.campus.marketplace.common.exception.ErrorCode.PERMISSION_DENIED.getCode());
    }

    @Test
    @DisplayName("管理员查询所有退款列表 - 分页成功")
    void listAllRefunds_success() {
        RefundRequest refund1 = RefundRequest.builder()
                .refundNo("RFD1")
                .orderNo("ORD1")
                .applicantId(1L)
                .status(RefundStatus.APPLIED)
                .amount(new BigDecimal("100"))
                .build();

        org.springframework.data.domain.Page<RefundRequest> page = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(refund1),
                org.springframework.data.domain.PageRequest.of(0, 10),
                1
        );
        when(refundRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        org.springframework.data.domain.Page<RefundRequest> result = refundService.listAllRefunds(0, 10, null, null);

        assertThat(result.getContent()).hasSize(1);
        verify(refundRepository).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("管理员查询所有退款列表 - 按状态筛选")
    void listAllRefunds_withStatusFilter() {
        RefundRequest refund = RefundRequest.builder()
                .refundNo("RFD1")
                .orderNo("ORD1")
                .applicantId(1L)
                .status(RefundStatus.APPLIED)
                .amount(new BigDecimal("100"))
                .build();

        org.springframework.data.domain.Page<RefundRequest> page = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(refund),
                org.springframework.data.domain.PageRequest.of(0, 10),
                1
        );
        when(refundRepository.findByStatus(eq(RefundStatus.APPLIED), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        org.springframework.data.domain.Page<RefundRequest> result = refundService.listAllRefunds(0, 10, RefundStatus.APPLIED, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(RefundStatus.APPLIED);
        verify(refundRepository).findByStatus(eq(RefundStatus.APPLIED), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("管理员查询所有退款列表 - 按关键词搜索")
    void listAllRefunds_withKeyword() {
        RefundRequest refund = RefundRequest.builder()
                .refundNo("RFD1")
                .orderNo("ORD1")
                .applicantId(1L)
                .status(RefundStatus.APPLIED)
                .amount(new BigDecimal("100"))
                .build();

        org.springframework.data.domain.Page<RefundRequest> page = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(refund),
                org.springframework.data.domain.PageRequest.of(0, 10),
                1
        );
        when(refundRepository.findByRefundNoContainingOrOrderNoContaining(eq("RFD1"), eq("RFD1"), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        org.springframework.data.domain.Page<RefundRequest> result = refundService.listAllRefunds(0, 10, null, "RFD1");

        assertThat(result.getContent()).hasSize(1);
        verify(refundRepository).findByRefundNoContainingOrOrderNoContaining(eq("RFD1"), eq("RFD1"), any(org.springframework.data.domain.Pageable.class));
    }
}
