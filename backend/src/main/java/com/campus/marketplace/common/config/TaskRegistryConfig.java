package com.campus.marketplace.common.config;

import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.common.enums.CampusStatus;
import com.campus.marketplace.repository.CampusRepository;
import com.campus.marketplace.service.OrderService;
import com.campus.marketplace.service.RecommendService;
import com.campus.marketplace.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TaskRegistryConfig {

    private final TaskService taskService;
    private final RecommendService recommendService;
    private final OrderService orderService;
    private final CampusRepository campusRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void registerBuiltInTasks() {
        taskService.register("recommend.hot.refresh", "刷新热门榜单", params -> {
            recommendService.refreshHotRanking(null, 50);
            List<Campus> campuses = campusRepository.findAll();
            for (Campus c : campuses) {
                if (c.getStatus() == CampusStatus.ACTIVE) {
                    recommendService.refreshHotRanking(c.getId(), 50);
                }
            }
        });

        taskService.register("order.timeout.close", "关闭超时未支付订单", params -> {
            int count = orderService.cancelTimeoutOrders();
            log.info("手动任务关闭超时订单完成: {}", count);
        });

        // 导出清理任务在 ExportService 中注册
    }
}
