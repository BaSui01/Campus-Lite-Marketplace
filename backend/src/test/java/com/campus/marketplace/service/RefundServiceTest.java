package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.Order;
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
}
