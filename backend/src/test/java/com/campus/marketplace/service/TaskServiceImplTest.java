package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.ScheduledTask;
import com.campus.marketplace.common.entity.TaskExecution;
import com.campus.marketplace.common.lock.DistributedLockManager;
import com.campus.marketplace.repository.ScheduledTaskRepository;
import com.campus.marketplace.repository.TaskExecutionRepository;
import com.campus.marketplace.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskServiceImpl 单元测试")
class TaskServiceImplTest {

    @Mock
    ScheduledTaskRepository taskRepo;
    @Mock
    TaskExecutionRepository execRepo;
    @Mock
    DistributedLockManager lockManager;
    @Mock
    DistributedLockManager.LockHandle lockHandle;

    @InjectMocks
    TaskServiceImpl taskService;

    @Test
    @DisplayName("register: 不存在则持久化")
    void register_persist_when_absent() {
        when(taskRepo.findByName("t1")).thenReturn(Optional.empty());
        when(taskRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        taskService.register("t1", "desc", params -> {});
        verify(taskRepo).save(argThat(t -> t.getName().equals("t1") && t.getStatus().equals("ENABLED")));
    }

    @Test
    @DisplayName("pause/resume 更新状态")
    void pause_resume_updates_status() {
        ScheduledTask t = ScheduledTask.builder().name("t2").status("ENABLED").build();
        t.setId(1L);
        when(taskRepo.findByName("t2")).thenReturn(Optional.of(t));
        when(taskRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        taskService.pause("t2");
        assertThat(t.getStatus()).isEqualTo("PAUSED");
        taskService.resume("t2");
        assertThat(t.getStatus()).isEqualTo("ENABLED");
    }

    @Test
    @DisplayName("trigger: 成功执行并记录 SUCCESS")
    void trigger_success() {
        // 准备任务与执行记录存储
        ScheduledTask t = ScheduledTask.builder().name("t3").status("ENABLED").build();
        t.setId(1L);
        when(taskRepo.findByName("t3")).thenReturn(Optional.of(t));

        Map<Long, TaskExecution> store = new HashMap<>();
        when(execRepo.save(any())).thenAnswer(inv -> {
            TaskExecution e = inv.getArgument(0);
            if (e.getId() == null) e.setId(100L);
            store.put(e.getId(), e);
            return e;
        });
        when(execRepo.findById(100L)).thenAnswer(inv -> Optional.of(store.get(100L)));

        when(lockManager.tryLock(anyString(), anyLong(), anyLong(), any())).thenReturn(lockHandle);
        when(lockHandle.acquired()).thenReturn(true);

        AtomicInteger counter = new AtomicInteger();
        taskService.register("t3", "desc", params -> counter.incrementAndGet());

        Long execId = taskService.trigger("t3", "p");
        assertThat(execId).isEqualTo(100L);
        // 非代理环境下 @Async 不生效，runAsync 同步执行
        TaskExecution finished = store.get(100L);
        assertThat(finished.getStatus()).isEqualTo("SUCCESS");
        assertThat(counter.get()).isEqualTo(1);
        verify(lockHandle).close();
    }

    @Test
    @DisplayName("trigger: 任务暂停抛出异常")
    void trigger_paused_throws() {
        ScheduledTask t = ScheduledTask.builder().name("t4").status("PAUSED").build();
        t.setId(1L);
        when(taskRepo.findByName("t4")).thenReturn(Optional.of(t));
        assertThatThrownBy(() -> taskService.trigger("t4", null)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("runAsync: 锁繁忙则执行记录标记为 FAILED")
    void runAsync_lock_busy_marks_failed() {
        ScheduledTask t = ScheduledTask.builder().name("t5").status("ENABLED").build();
        t.setId(1L);
        when(taskRepo.findByName("t5")).thenReturn(Optional.of(t));

        Map<Long, TaskExecution> store = new HashMap<>();
        when(execRepo.save(any())).thenAnswer(inv -> {
            TaskExecution e = inv.getArgument(0);
            if (e.getId() == null) e.setId(200L);
            store.put(e.getId(), e);
            return e;
        });
        when(execRepo.findById(200L)).thenAnswer(inv -> Optional.of(store.get(200L)));

        when(lockManager.tryLock(anyString(), anyLong(), anyLong(), any())).thenReturn(lockHandle);
        when(lockHandle.acquired()).thenReturn(false);

        taskService.register("t5", "desc", params -> {});
        Long id = taskService.trigger("t5", "p");
        assertThat(id).isEqualTo(200L);
        TaskExecution finished = store.get(200L);
        assertThat(finished.getStatus()).isEqualTo("FAILED");
        assertThat(finished.getError()).contains("Lock busy");
    }
}
