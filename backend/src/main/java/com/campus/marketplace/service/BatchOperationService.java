package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateBatchTaskRequest;
import com.campus.marketplace.common.dto.response.BatchTaskProgressResponse;
import com.campus.marketplace.common.dto.response.BatchTaskResponse;
import com.campus.marketplace.common.enums.BatchTaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 批量操作服务接口
 * 
 * @author BaSui
 * @date 2025-11-02
 */
public interface BatchOperationService {

    /**
     * 创建批量任务
     */
    Long createBatchTask(CreateBatchTaskRequest request, Long userId);

    /**
     * 执行批量任务
     */
    void executeBatchTask(Long taskId);

    /**
     * 取消批量任务
     */
    boolean cancelBatchTask(Long taskId, Long userId);

    /**
     * 查询用户的批量任务列表
     */
    Page<BatchTaskResponse> getUserBatchTasks(Long userId, BatchTaskStatus status, Pageable pageable);

    /**
     * 查询任务详情
     */
    BatchTaskResponse getBatchTaskDetail(Long taskId, Long userId);

    /**
     * 查询任务进度
     */
    BatchTaskProgressResponse getTaskProgress(Long taskId);
}
