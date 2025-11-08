package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.entity.ScheduledTask;
import com.campus.marketplace.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = TaskController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("TaskController MockMvc 测试")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    @DisplayName("查询定时任务列表成功")
    void listTasks_success() throws Exception {
        ScheduledTask task = ScheduledTask.builder()
                .name("recompute-hot")
                .status("ENABLED")
                .description("重算热门商品")
                .build();
        when(taskService.list()).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("recompute-hot"));

        verify(taskService).list();
    }

    @Test
    @DisplayName("触发定时任务成功返回执行ID")
    void triggerTask_success() throws Exception {
        when(taskService.trigger("recompute-hot", "campusId=1")).thenReturn(888L);

        mockMvc.perform(post("/api/tasks/{name}/trigger", "recompute-hot")
                        .param("params", "campusId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(888));

        verify(taskService).trigger("recompute-hot", "campusId=1");
    }

    @Test
    @DisplayName("暂停定时任务成功")
    void pauseTask_success() throws Exception {
        mockMvc.perform(post("/api/tasks/{name}/pause", "recompute-hot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(taskService).pause("recompute-hot");
    }

    @Test
    @DisplayName("恢复定时任务成功")
    void resumeTask_success() throws Exception {
        mockMvc.perform(post("/api/tasks/{name}/resume", "recompute-hot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(taskService).resume("recompute-hot");
    }
}
