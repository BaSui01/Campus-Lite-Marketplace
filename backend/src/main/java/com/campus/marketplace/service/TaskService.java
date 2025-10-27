package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.ScheduledTask;

import java.util.List;

public interface TaskService {
    void register(String name, String description, com.campus.marketplace.service.task.TaskRunner runner);
    List<ScheduledTask> list();
    void pause(String name);
    void resume(String name);
    Long trigger(String name, String params);
}
