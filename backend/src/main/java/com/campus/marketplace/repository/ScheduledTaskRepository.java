package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.ScheduledTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {
    Optional<ScheduledTask> findByName(String name);
}
