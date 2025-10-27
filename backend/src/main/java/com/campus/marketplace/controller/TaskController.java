package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.ScheduledTask;
import com.campus.marketplace.service.TaskService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ApiResponse<List<ScheduledTask>> list() {
        return ApiResponse.success(taskService.list());
    }

    @PostMapping("/{name}/trigger")
    public ApiResponse<Long> trigger(@PathVariable("name") @NotBlank String name,
                                     @RequestParam(value = "params", required = false) String params) {
        return ApiResponse.success(taskService.trigger(name, params));
    }

    @PostMapping("/{name}/pause")
    public ApiResponse<Void> pause(@PathVariable("name") String name) {
        taskService.pause(name);
        return ApiResponse.success();
    }

    @PostMapping("/{name}/resume")
    public ApiResponse<Void> resume(@PathVariable("name") String name) {
        taskService.resume(name);
        return ApiResponse.success();
    }
}
