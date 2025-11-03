package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.DisputeDTO;
import com.campus.marketplace.common.dto.DisputeDetailDTO;
import com.campus.marketplace.common.dto.request.CreateDisputeRequest;
import com.campus.marketplace.common.entity.Dispute;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.enums.*;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.repository.DisputeRepository;
import com.campus.marketplace.service.impl.DisputeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 纠纷服务测试类
 *
 * 遵循TDD原则，测试先行！
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("纠纷服务测试")
class DisputeServiceTest {

    @Mock
    private DisputeRepository disputeRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private DisputeServiceImpl disputeService;

    private CreateDisputeRequest createRequest;
    private Order testOrder;
    private Dispute testDispute;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        createRequest = CreateDisputeRequest.builder()
                .orderId(123L)
                .disputeType(DisputeType.GOODS_MISMATCH)
                .description("商品与描述严重不符，要求全额退款处理，这是一段符合长度要求的描述内容。")
                .build();

        testOrder = new Order();
        testOrder.setOrderNo("ORD-20251103-000100");
        testOrder.setBuyerId(100L);
        testOrder.setSellerId(200L);
        testOrder.setStatus(OrderStatus.COMPLETED);

        testDispute = new Dispute();
        testDispute.setDisputeCode("DSP-20251103-000001");
        testDispute.setOrderId(123L);
        testDispute.setInitiatorId(100L);
        testDispute.setInitiatorRole(DisputeRole.BUYER);
        testDispute.setRespondentId(200L);
        testDispute.setDisputeType(DisputeType.GOODS_MISMATCH);
        testDispute.setDescription("商品与描述严重不符");
        testDispute.setStatus(DisputeStatus.SUBMITTED);
        testDispute.setNegotiationDeadline(LocalDateTime.now().plusDays(2));
        testDispute.setOrder(testOrder);
    }

    @Test
    @DisplayName("提交纠纷 - 订单已有纠纷时应该抛出异常")
    void submitDispute_ShouldThrowException_WhenDisputeAlreadyExists() {
        // Arrange
        when(disputeRepository.existsByOrderId(anyLong())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> disputeService.submitDispute(createRequest, 100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("该订单已存在纠纷");

        // 验证没有保存纠纷
        verify(disputeRepository, never()).save(any(Dispute.class));
    }

    @Test
    @DisplayName("查询用户纠纷列表 - 应该返回分页结果")
    void getUserDisputes_ShouldReturnPagedResults() {
        // Arrange
        Page<Dispute> disputePage = new PageImpl<>(List.of(testDispute));
        when(disputeRepository.findByUserIdWithStatus(anyLong(), any(), any(Pageable.class)))
                .thenReturn(disputePage);

        // Act
        Page<DisputeDTO> result = disputeService.getUserDisputes(
                100L,
                null,
                PageRequest.of(0, 10)
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDisputeCode()).isEqualTo("DSP-20251103-000001");
    }

    @Test
    @DisplayName("查询纠纷详情 - 应该返回完整的纠纷信息")
    void getDisputeDetail_ShouldReturnCompleteInformation() {
        // Arrange
        testDispute.setId(1L);
        when(disputeRepository.findById(anyLong())).thenReturn(Optional.of(testDispute));

        // Act
        DisputeDetailDTO detail = disputeService.getDisputeDetail(1L);

        // Assert
        assertThat(detail).isNotNull();
        assertThat(detail.getId()).isEqualTo(1L);
        assertThat(detail.getDisputeCode()).isEqualTo("DSP-20251103-000001");
        assertThat(detail.getStatus()).isEqualTo(DisputeStatus.SUBMITTED);
    }

    @Test
    @DisplayName("查询纠纷详情 - 纠纷不存在时应该抛出异常")
    void getDisputeDetail_ShouldThrowException_WhenDisputeNotFound() {
        // Arrange
        when(disputeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> disputeService.getDisputeDetail(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("纠纷不存在");
    }

    @Test
    @DisplayName("升级纠纷为仲裁状态 - 应该成功升级")
    void escalateToArbitration_ShouldEscalateSuccessfully() {
        // Arrange
        testDispute.setId(1L);
        testDispute.setStatus(DisputeStatus.NEGOTIATING);
        when(disputeRepository.findById(anyLong())).thenReturn(Optional.of(testDispute));
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        // Act
        boolean result = disputeService.escalateToArbitration(1L);

        // Assert
        assertThat(result).isTrue();
        assertThat(testDispute.getStatus()).isEqualTo(DisputeStatus.PENDING_ARBITRATION);
        assertThat(testDispute.getArbitrationDeadline()).isNotNull();

        // 验证保存了纠纷
        verify(disputeRepository, times(1)).save(testDispute);
    }

    @Test
    @DisplayName("关闭纠纷 - 应该成功关闭")
    void closeDispute_ShouldCloseSuccessfully() {
        // Arrange
        testDispute.setId(1L);
        when(disputeRepository.findById(anyLong())).thenReturn(Optional.of(testDispute));
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        // Act
        boolean result = disputeService.closeDispute(1L, "用户主动撤销");

        // Assert
        assertThat(result).isTrue();
        assertThat(testDispute.getStatus()).isEqualTo(DisputeStatus.CLOSED);
        assertThat(testDispute.getCloseReason()).isEqualTo("用户主动撤销");
        assertThat(testDispute.getClosedAt()).isNotNull();

        // 验证保存了纠纷
        verify(disputeRepository, times(1)).save(testDispute);
    }

    @Test
    @DisplayName("标记协商期到期纠纷 - 应该升级为待仲裁")
    void markExpiredNegotiations_ShouldEscalateToArbitration() {
        // Arrange
        Dispute expiredDispute1 = new Dispute();
        expiredDispute1.setId(1L);
        expiredDispute1.setDisputeCode("DSP-20251103-000001");
        expiredDispute1.setStatus(DisputeStatus.NEGOTIATING);
        expiredDispute1.setNegotiationDeadline(LocalDateTime.now().minusHours(1));
        expiredDispute1.setInitiatorId(100L);
        expiredDispute1.setRespondentId(200L);

        Dispute expiredDispute2 = new Dispute();
        expiredDispute2.setId(2L);
        expiredDispute2.setDisputeCode("DSP-20251103-000002");
        expiredDispute2.setStatus(DisputeStatus.NEGOTIATING);
        expiredDispute2.setNegotiationDeadline(LocalDateTime.now().minusHours(2));
        expiredDispute2.setInitiatorId(101L);
        expiredDispute2.setRespondentId(201L);

        when(disputeRepository.findExpiredNegotiations(any(), any()))
                .thenReturn(List.of(expiredDispute1, expiredDispute2));
        when(disputeRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        int count = disputeService.markExpiredNegotiations();

        // Assert
        assertThat(count).isEqualTo(2);
        assertThat(expiredDispute1.getStatus()).isEqualTo(DisputeStatus.PENDING_ARBITRATION);
        assertThat(expiredDispute2.getStatus()).isEqualTo(DisputeStatus.PENDING_ARBITRATION);

        // 验证保存了纠纷
        verify(disputeRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("标记仲裁期到期纠纷 - 应该自动关闭")
    void markExpiredArbitrations_ShouldCloseDispute() {
        // Arrange
        Dispute expiredDispute = new Dispute();
        expiredDispute.setId(1L);
        expiredDispute.setDisputeCode("DSP-20251103-000001");
        expiredDispute.setStatus(DisputeStatus.ARBITRATING);
        expiredDispute.setArbitrationDeadline(LocalDateTime.now().minusDays(1));
        expiredDispute.setInitiatorId(100L);
        expiredDispute.setArbitratorId(300L);

        when(disputeRepository.findExpiredArbitrations(any(), any()))
                .thenReturn(List.of(expiredDispute));
        when(disputeRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        int count = disputeService.markExpiredArbitrations();

        // Assert
        assertThat(count).isEqualTo(1);
        assertThat(expiredDispute.getStatus()).isEqualTo(DisputeStatus.CLOSED);
        assertThat(expiredDispute.getCloseReason()).contains("仲裁期到期");
        assertThat(expiredDispute.getClosedAt()).isNotNull();

        // 验证保存了纠纷
        verify(disputeRepository, times(1)).saveAll(anyList());
    }
}
