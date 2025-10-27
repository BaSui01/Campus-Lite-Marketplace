package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.TaskExecution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskExecutionRepository extends JpaRepository<TaskExecution, Long> {
}
