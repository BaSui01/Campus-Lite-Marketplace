package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.TaskExecution;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Task Execution Repository
 *
 * @author BaSui
 * @date 2025-10-29
 */


public interface TaskExecutionRepository extends JpaRepository<TaskExecution, Long> {
}
