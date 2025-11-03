package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.LogisticsDTO;
import com.campus.marketplace.common.dto.LogisticsStatisticsDTO;
import com.campus.marketplace.common.entity.Logistics;
import com.campus.marketplace.common.entity.LogisticsTrackRecord;
import com.campus.marketplace.common.enums.LogisticsCompany;
import com.campus.marketplace.common.enums.LogisticsStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.logistics.LogisticsProvider;
import com.campus.marketplace.logistics.LogisticsProviderFactory;
import com.campus.marketplace.repository.LogisticsRepository;
import com.campus.marketplace.service.impl.LogisticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 物流服务测试类
 * <p>
 * 测试物流服务的核心功能。
 * </p>
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("物流服务测试")
class LogisticsServiceTest {

    @Mock
    private LogisticsRepository logisticsRepository;

    @Mock
    private LogisticsProviderFactory providerFactory;

    @Mock
    private LogisticsProvider logisticsProvider;

    @InjectMocks
    private LogisticsServiceImpl logisticsService;

    private Logistics testLogistics;
    private List<LogisticsTrackRecord> testTrackRecords;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testTrackRecords = new ArrayList<>();
        testTrackRecords.add(new LogisticsTrackRecord(
                LocalDateTime.now().minusDays(2),
                "广东省深圳市",
                "【深圳市】已揽件",
                "张三"
        ));
        testTrackRecords.add(new LogisticsTrackRecord(
                LocalDateTime.now().minusDays(1),
                "湖北省武汉市",
                "【武汉市】运输中",
                "李四"
        ));

        testLogistics = Logistics.builder()
                
                .orderId(100L)
                .trackingNumber("SF1234567890")
                .logisticsCompany(LogisticsCompany.SHUNFENG)
                .status(LogisticsStatus.IN_TRANSIT)
                .currentLocation("湖北省武汉市")
                .estimatedDeliveryTime(LocalDateTime.now().plusDays(1))
                .isOvertime(false)
                .trackRecords(testTrackRecords)
                .syncCount(1)
                .lastSyncTime(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("创建物流信息 - 成功")
    void testCreateLogistics_Success() {
        // Given
        Long orderId = 100L;
        String trackingNumber = "SF1234567890";
        LogisticsCompany company = LogisticsCompany.SHUNFENG;

        when(logisticsRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(providerFactory.getProvider(company)).thenReturn(logisticsProvider);
        when(logisticsProvider.isValidTrackingNumber(trackingNumber)).thenReturn(true);
        when(logisticsRepository.save(any(Logistics.class))).thenReturn(testLogistics);
        when(logisticsProvider.queryTrackRecords(trackingNumber)).thenReturn(testTrackRecords);
        when(logisticsProvider.queryStatus(trackingNumber)).thenReturn(LogisticsStatus.IN_TRANSIT);

        // When
        LogisticsDTO result = logisticsService.createLogistics(orderId, trackingNumber, company);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getTrackingNumber()).isEqualTo(trackingNumber);
        assertThat(result.getLogisticsCompany()).isEqualTo(company);

        verify(logisticsRepository).findByOrderId(orderId);
        verify(logisticsRepository, atLeastOnce()).save(any(Logistics.class));
    }

    @Test
    @DisplayName("创建物流信息 - 参数为空")
    void testCreateLogistics_NullParameters() {
        // When & Then
        assertThatThrownBy(() -> logisticsService.createLogistics(null, "SF123", LogisticsCompany.SHUNFENG))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("参数不能为空");

        assertThatThrownBy(() -> logisticsService.createLogistics(100L, null, LogisticsCompany.SHUNFENG))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("参数不能为空");

        assertThatThrownBy(() -> logisticsService.createLogistics(100L, "SF123", null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("参数不能为空");
    }

    @Test
    @DisplayName("创建物流信息 - 订单已有物流信息")
    void testCreateLogistics_DuplicateOrder() {
        // Given
        Long orderId = 100L;
        when(logisticsRepository.findByOrderId(orderId)).thenReturn(Optional.of(testLogistics));

        // When & Then
        assertThatThrownBy(() -> logisticsService.createLogistics(orderId, "SF123", LogisticsCompany.SHUNFENG))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("订单已有物流信息");

        verify(logisticsRepository).findByOrderId(orderId);
        verify(logisticsRepository, never()).save(any(Logistics.class));
    }

    @Test
    @DisplayName("创建物流信息 - 快递单号格式不正确")
    void testCreateLogistics_InvalidTrackingNumber() {
        // Given
        Long orderId = 100L;
        String trackingNumber = "INVALID";
        LogisticsCompany company = LogisticsCompany.SHUNFENG;

        when(logisticsRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(providerFactory.getProvider(company)).thenReturn(logisticsProvider);
        when(logisticsProvider.isValidTrackingNumber(trackingNumber)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> logisticsService.createLogistics(orderId, trackingNumber, company))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("快递单号格式不正确");

        verify(logisticsRepository, never()).save(any(Logistics.class));
    }

    @Test
    @DisplayName("根据订单ID查询物流信息 - 成功")
    void testGetLogisticsByOrderId_Success() {
        // Given
        Long orderId = 100L;
        when(logisticsRepository.findByOrderId(orderId)).thenReturn(Optional.of(testLogistics));

        // When
        LogisticsDTO result = logisticsService.getLogisticsByOrderId(orderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getTrackingNumber()).isEqualTo("SF1234567890");
        assertThat(result.getLogisticsCompany()).isEqualTo(LogisticsCompany.SHUNFENG);
        assertThat(result.getStatus()).isEqualTo(LogisticsStatus.IN_TRANSIT);

        verify(logisticsRepository).findByOrderId(orderId);
    }

    @Test
    @DisplayName("根据订单ID查询物流信息 - 物流信息不存在")
    void testGetLogisticsByOrderId_NotFound() {
        // Given
        Long orderId = 999L;
        when(logisticsRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> logisticsService.getLogisticsByOrderId(orderId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("物流信息不存在");

        verify(logisticsRepository).findByOrderId(orderId);
    }

    @Test
    @DisplayName("根据快递单号查询物流信息 - 成功")
    void testGetLogisticsByTrackingNumber_Success() {
        // Given
        String trackingNumber = "SF1234567890";
        when(logisticsRepository.findByTrackingNumber(trackingNumber)).thenReturn(Optional.of(testLogistics));

        // When
        LogisticsDTO result = logisticsService.getLogisticsByTrackingNumber(trackingNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTrackingNumber()).isEqualTo(trackingNumber);
        assertThat(result.getLogisticsCompany()).isEqualTo(LogisticsCompany.SHUNFENG);

        verify(logisticsRepository).findByTrackingNumber(trackingNumber);
    }

    @Test
    @DisplayName("同步物流信息 - 成功")
    void testSyncLogistics_Success() {
        // Given
        Long orderId = 100L;
        when(logisticsRepository.findByOrderId(orderId)).thenReturn(Optional.of(testLogistics));
        when(providerFactory.getProvider(any())).thenReturn(logisticsProvider);
        when(logisticsProvider.queryTrackRecords(any())).thenReturn(testTrackRecords);
        when(logisticsProvider.queryStatus(any())).thenReturn(LogisticsStatus.DELIVERING);
        when(logisticsRepository.save(any(Logistics.class))).thenReturn(testLogistics);

        // When
        LogisticsDTO result = logisticsService.syncLogistics(orderId);

        // Then
        assertThat(result).isNotNull();
        verify(logisticsRepository).findByOrderId(orderId);
        verify(logisticsRepository).save(any(Logistics.class));
        verify(logisticsProvider).queryTrackRecords(any());
        verify(logisticsProvider).queryStatus(any());
    }

    @Test
    @DisplayName("批量同步物流信息 - 成功")
    void testBatchSyncLogistics_Success() {
        // Given
        List<Logistics> pendingLogistics = List.of(testLogistics);
        when(logisticsRepository.findPendingLogistics(any(), any())).thenReturn(pendingLogistics);
        when(providerFactory.getProvider(any())).thenReturn(logisticsProvider);
        when(logisticsProvider.queryTrackRecords(any())).thenReturn(testTrackRecords);
        when(logisticsProvider.queryStatus(any())).thenReturn(LogisticsStatus.DELIVERING);
        when(logisticsRepository.save(any(Logistics.class))).thenReturn(testLogistics);

        // When
        int result = logisticsService.batchSyncLogistics();

        // Then
        assertThat(result).isEqualTo(1);
        verify(logisticsRepository).findPendingLogistics(any(), any());
        verify(logisticsRepository).save(any(Logistics.class));
    }

    @Test
    @DisplayName("标记超时物流 - 成功")
    void testMarkOvertimeLogistics_Success() {
        // Given
        Logistics overtimeLogistics = Logistics.builder()
                
                .orderId(100L)
                .trackingNumber("SF1234567890")
                .logisticsCompany(LogisticsCompany.SHUNFENG)
                .status(LogisticsStatus.IN_TRANSIT)
                .estimatedDeliveryTime(LocalDateTime.now().minusDays(1)) // 已超时
                .isOvertime(false)
                .build();

        when(logisticsRepository.findOvertimeLogistics(any())).thenReturn(List.of(overtimeLogistics));
        when(logisticsRepository.save(any(Logistics.class))).thenReturn(overtimeLogistics);

        // When
        int result = logisticsService.markOvertimeLogistics();

        // Then
        assertThat(result).isEqualTo(1);
        verify(logisticsRepository).findOvertimeLogistics(any());
        verify(logisticsRepository).save(any(Logistics.class));
    }

    @Test
    @DisplayName("获取物流统计数据 - 成功")
    void testGetLogisticsStatistics_Success() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        when(logisticsRepository.countByCompanyAndDateRange(any(), any(), any())).thenReturn(10L);
        when(logisticsRepository.countOvertimeByCompanyAndDateRange(any(), any(), any())).thenReturn(2L);

        // When
        LogisticsStatisticsDTO result = logisticsService.getLogisticsStatistics(startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalOrders()).isGreaterThan(0);
        assertThat(result.getOvertimeOrders()).isGreaterThanOrEqualTo(0);
        assertThat(result.getAverageDeliveryTime()).isNotNull();
        assertThat(result.getOvertimeRate()).isNotNull();
        assertThat(result.getUserRating()).isNotNull();
    }
}
