package com.campus.marketplace.controller.perf;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.service.perf.PerfSeedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PerfSeedController 测试")
class PerfSeedControllerTest {

    @Mock
    private PerfSeedService perfSeedService;

    private PerfSeedController controller;

    @BeforeEach
    void setUp() {
        controller = new PerfSeedController(perfSeedService);
    }

    @Test
    @DisplayName("seedTimeoutOrders 会清理旧数据并返回统计信息")
    void seedTimeoutOrders_shouldReturnSummary() {
        when(perfSeedService.clearTimeoutFixtures()).thenReturn(5);
        when(perfSeedService.seedTimeoutOrders(300)).thenReturn(280);

        ApiResponse<Map<String, Object>> response = controller.seedTimeoutOrders(300);

        assertThat(response.getCode()).isEqualTo(ErrorCode.SUCCESS.getCode());
        assertThat(response.getData()).containsEntry("requested", 300)
                .containsEntry("cleared", 5)
                .containsEntry("created", 280);
        verify(perfSeedService).clearTimeoutFixtures();
        verify(perfSeedService).seedTimeoutOrders(300);
    }

    @Test
    @DisplayName("clearTimeoutFixtures 会返回清理数量")
    void clearTimeoutFixtures_shouldReturnClearedCount() {
        when(perfSeedService.clearTimeoutFixtures()).thenReturn(12);

        ApiResponse<Map<String, Integer>> response = controller.clearTimeoutFixtures();

        assertThat(response.getCode()).isEqualTo(ErrorCode.SUCCESS.getCode());
        assertThat(response.getData()).containsEntry("cleared", 12);
        verify(perfSeedService).clearTimeoutFixtures();
    }
}
