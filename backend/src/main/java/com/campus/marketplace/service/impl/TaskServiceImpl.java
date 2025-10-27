package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.ScheduledTask;
import com.campus.marketplace.common.entity.TaskExecution;
import com.campus.marketplace.repository.ScheduledTaskRepository;
import com.campus.marketplace.repository.TaskExecutionRepository;
import com.campus.marketplace.service.TaskService;
import com.campus.marketplace.service.task.TaskRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final ScheduledTaskRepository taskRepo;
    private final TaskExecutionRepository execRepo;
    private final RedissonClient redissonClient;

    private final Map<String, TaskRunner> registry = new ConcurrentHashMap<>();

    @Override
    public void register(String name, String description, TaskRunner runner) {
        registry.put(name, runner);
        taskRepo.findByName(name).orElseGet(() ->
                taskRepo.save(ScheduledTask.builder()
                        .name(name)
                        .status("ENABLED")
                        .description(description)
                        .build()));
        log.info("任务已注册: {}", name);
    }

    @Override
    public List<ScheduledTask> list() {
        return taskRepo.findAll();
    }

    @Override
    public void pause(String name) {
        taskRepo.findByName(name).ifPresent(t -> { t.setStatus("PAUSED"); taskRepo.save(t); });
    }

    @Override
    public void resume(String name) {
        taskRepo.findByName(name).ifPresent(t -> { t.setStatus("ENABLED"); taskRepo.save(t); });
    }

    @Override
    public Long trigger(String name, String params) {
        ScheduledTask t = taskRepo.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Unknown task: " + name));
        if (!"ENABLED".equals(t.getStatus())) {
            throw new IllegalStateException("Task paused: " + name);
        }
        TaskExecution exec = execRepo.save(TaskExecution.builder()
                .taskName(name)
                .status("RUNNING")
                .params(params)
                .startedAt(Instant.now())
                .node(getHostname())
                .build());
        runAsync(exec.getId());
        return exec.getId();
    }

    @Async("virtualThreadExecutor")
    protected void runAsync(Long execId) {
        TaskExecution exec = execRepo.findById(execId).orElseThrow();
        String name = exec.getTaskName();
        TaskRunner runner = registry.get(name);
        if (runner == null) {
            finishAsFailed(exec, "Runner not found");
            return;
        }

        String lockKey = "task:lock:" + name;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(0, 30, TimeUnit.MINUTES);
            if (!locked) {
                finishAsFailed(exec, "Lock busy");
                return;
            }
            runner.run(exec.getParams());
            exec.setStatus("SUCCESS");
        } catch (Exception e) {
            exec.setStatus("FAILED");
            exec.setError(e.getMessage());
            log.error("任务执行失败: {}", name, e);
        } finally {
            exec.setEndedAt(Instant.now());
            execRepo.save(exec);
            if (locked) {
                try { lock.unlock(); } catch (Exception ignored) {}
            }
        }
    }

    private void finishAsFailed(TaskExecution exec, String reason) {
        exec.setStatus("FAILED");
        exec.setError(reason);
        exec.setEndedAt(Instant.now());
        execRepo.save(exec);
    }

    private String getHostname() {
        try { return java.net.InetAddress.getLocalHost().getHostName(); }
        catch (Exception e) { return "unknown"; }
    }
}
