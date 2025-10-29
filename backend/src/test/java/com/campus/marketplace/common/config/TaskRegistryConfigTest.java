package com.campus.marketplace.common.config;

import com.campus.marketplace.common.config.TaskRegistryConfig;
import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.common.enums.CampusStatus;
import com.campus.marketplace.repository.CampusRepository;
import com.campus.marketplace.service.OrderService;
import com.campus.marketplace.service.RecommendService;
import com.campus.marketplace.service.TaskService;
import com.campus.marketplace.service.task.TaskRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskRegistryConfig 测试")
class TaskRegistryConfigTest {

    @Mock
    private TaskService taskService;
    @Mock
    private RecommendService recommendService;
    @Mock
    private OrderService orderService;
    @Mock
    private CampusRepository campusRepository;

    private TaskRegistryConfig config;

    @BeforeEach
    void setUp() {
        config = new TaskRegistryConfig(taskService, recommendService, orderService, campusRepository);
    }

    @Test
    @DisplayName("启动时注册内置任务并可执行")
    void registerBuiltInTasks_shouldRegisterRunners() throws Exception {
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<TaskRunner> runnerCaptor = ArgumentCaptor.forClass(TaskRunner.class);

        Campus campus1 = Campus.builder().status(CampusStatus.ACTIVE).build();
        campus1.setId(1L);
        Campus campus2 = Campus.builder().status(CampusStatus.INACTIVE).build();
        campus2.setId(2L);
        when(campusRepository.findAll()).thenReturn(List.of(campus1, campus2));

        config.registerBuiltInTasks();

        verify(taskService, times(2)).register(nameCaptor.capture(), anyString(), runnerCaptor.capture());

        List<String> names = nameCaptor.getAllValues();
        List<TaskRunner> runners = runnerCaptor.getAllValues();
        assertThat(names).contains("recommend.hot.refresh", "order.timeout.close");

        // 执行第一个任务，校验推荐服务调用
        TaskRunner recommendRunner = runners.get(names.indexOf("recommend.hot.refresh"));
        recommendRunner.run(null);
        verify(recommendService).refreshHotRanking(null, 50);
        verify(recommendService).refreshHotRanking(1L, 50);
        verify(recommendService, never()).refreshHotRanking(eq(2L), anyInt());

        // 执行第二个任务，校验订单服务调用
        TaskRunner orderRunner = runners.get(names.indexOf("order.timeout.close"));
        orderRunner.run(null);
        verify(orderService).cancelTimeoutOrders();
    }
}
