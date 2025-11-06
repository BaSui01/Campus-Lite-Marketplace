package com.campus.marketplace.controller.perf;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.service.perf.PerfSeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 性能环境数据操作控制器，仅在 perf Profile 下启用。
 */
@Slf4j
@Profile("perf")
@RestController
@RequiredArgsConstructor
@RequestMapping("/perf/orders/timeout")
@Tag(name = "性能环境数据", description = "压测专用数据初始化与清理")
public class PerfSeedController {

    private final PerfSeedService perfSeedService;

    @PostMapping("/seed")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "批量生成超时订单", description = "构造满足 cancelTimeoutOrders 的真实业务数据")
    public ApiResponse<Map<String, Object>> seedTimeoutOrders(@RequestParam(name = "count", defaultValue = "200") int count) {
        int cleaned = perfSeedService.clearTimeoutFixtures();
        int created = perfSeedService.seedTimeoutOrders(count);
        log.info("性能环境数据重置完成: cleared={}, created={}", cleaned, created);
        return ApiResponse.success(Map.of(
                "requested", count,
                "cleared", cleaned,
                "created", created
        ));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "清理压测数据", description = "删除 PERF- 前缀订单及性能压测商品")
    public ApiResponse<Map<String, Integer>> clearTimeoutFixtures() {
        int cleared = perfSeedService.clearTimeoutFixtures();
        return ApiResponse.success(Map.of("cleared", cleared));
    }
}
