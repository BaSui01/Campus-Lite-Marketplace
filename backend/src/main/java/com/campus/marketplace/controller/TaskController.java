package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.ScheduledTask;
import com.campus.marketplace.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "任务调度", description = "定时任务列表、触发、暂停与恢复")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @Operation(summary = "任务列表", description = "查询所有可用的定时任务")
    public ApiResponse<List<ScheduledTask>> list() {
        return ApiResponse.success(taskService.list());
    }

    @PostMapping("/{name}/trigger")
    @Operation(summary = "触发执行", description = "立即触发指定任务，可传入参数")
    public ApiResponse<Long> trigger(@Parameter(description = "任务名称", example = "recompute-hot") @PathVariable("name") @NotBlank String name,
                                     @Parameter(description = "任务参数（可选）", example = "campusId=1") @RequestParam(value = "params", required = false) String params) {
        return ApiResponse.success(taskService.trigger(name, params));
    }

    @PostMapping("/{name}/pause")
    @Operation(summary = "暂停任务")
    public ApiResponse<Void> pause(@Parameter(description = "任务名称", example = "recompute-hot") @PathVariable("name") String name) {
        taskService.pause(name);
        return ApiResponse.success();
    }

    @PostMapping("/{name}/resume")
    @Operation(summary = "恢复任务")
    public ApiResponse<Void> resume(@Parameter(description = "任务名称", example = "recompute-hot") @PathVariable("name") String name) {
        taskService.resume(name);
        return ApiResponse.success();
    }
}
