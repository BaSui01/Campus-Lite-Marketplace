package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateBatchTaskRequest;
import com.campus.marketplace.common.dto.response.BatchTaskProgressResponse;
import com.campus.marketplace.common.dto.response.BatchTaskResponse;
import com.campus.marketplace.common.entity.BatchTask;
import com.campus.marketplace.common.enums.BatchTaskStatus;
import com.campus.marketplace.common.enums.BatchType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.repository.BatchTaskRepository;
import com.campus.marketplace.service.batch.BatchTaskOrchestrator;
import com.campus.marketplace.service.impl.BatchOperationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("批量操作服务测试")
class BatchOperationServiceTest {

    @Mock
    private BatchTaskRepository batchTaskRepository;

    @Mock
    private BatchTaskOrchestrator taskOrchestrator;

    @InjectMocks
    private BatchOperationServiceImpl batchOperationService;

    private Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        // 准备工作
    }

    @Test
    @DisplayName("应该能成功创建批量任务")
    void shouldCreateBatchTask() {
        // Arrange
        CreateBatchTaskRequest request = CreateBatchTaskRequest.builder()
                .batchType(BatchType.GOODS_BATCH)
                .requestData("{\"operation\":\"online\",\"ids\":[1,2,3]}")
                .estimatedDuration(300)
                .build();

        when(batchTaskRepository.save(any(BatchTask.class))).thenAnswer(invocation -> {
            BatchTask task = invocation.getArgument(0);
            task.setId(100L);
            return task;
        });

        // Act
        Long taskId = batchOperationService.createBatchTask(request, testUserId);

        // Assert
        assertThat(taskId).isEqualTo(100L);
        
        ArgumentCaptor<BatchTask> taskCaptor = ArgumentCaptor.forClass(BatchTask.class);
        verify(batchTaskRepository).save(taskCaptor.capture());
        
        BatchTask savedTask = taskCaptor.getValue();
        assertThat(savedTask.getBatchType()).isEqualTo(BatchType.GOODS_BATCH);
        assertThat(savedTask.getUserId()).isEqualTo(testUserId);
        assertThat(savedTask.getStatus()).isEqualTo(BatchTaskStatus.PENDING);
        assertThat(savedTask.getTaskCode()).startsWith("BATCH-");
    }

    @Test
    @DisplayName("应该能触发批量任务执行")
    void shouldExecuteBatchTask() {
        // Arrange
        Long taskId = 100L;
        BatchTask task = BatchTask.builder()
                .status(BatchTaskStatus.PENDING)
                .build();
        task.setId(taskId);

        when(batchTaskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act
        batchOperationService.executeBatchTask(taskId);

        // Assert
        verify(taskOrchestrator).orchestrateTask(taskId);
    }

    @Test
    @DisplayName("执行不存在的任务应抛出异常")
    void shouldThrowExceptionWhenExecuteNonExistentTask() {
        // Arrange
        when(batchTaskRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> batchOperationService.executeBatchTask(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("任务不存在");
    }

    @Test
    @DisplayName("应该能成功取消批量任务")
    void shouldCancelBatchTask() {
        // Arrange
        Long taskId = 100L;
        BatchTask task = BatchTask.builder()
                .userId(testUserId)
                .status(BatchTaskStatus.PENDING)
                .build();
        task.setId(taskId);

        when(batchTaskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(batchTaskRepository.save(any(BatchTask.class))).thenReturn(task);

        // Act
        boolean result = batchOperationService.cancelBatchTask(taskId, testUserId);

        // Assert
        assertThat(result).isTrue();
        verify(batchTaskRepository).save(argThat(t -> 
            t.getStatus() == BatchTaskStatus.CANCELLED
        ));
    }

    @Test
    @DisplayName("取消非本人任务应抛出异常")
    void shouldThrowExceptionWhenCancelOthersTask() {
        // Arrange
        Long taskId = 100L;
        Long otherUserId = 999L;
        BatchTask task = BatchTask.builder()
                .userId(otherUserId)
                .status(BatchTaskStatus.PENDING)
                .build();
        task.setId(taskId);

        when(batchTaskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act & Assert
        assertThatThrownBy(() -> batchOperationService.cancelBatchTask(taskId, testUserId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权操作");
    }

    @Test
    @DisplayName("应该能查询用户的批量任务列表")
    void shouldGetUserBatchTasks() {
        // Arrange
        BatchTask task1 = BatchTask.builder()
                .taskCode("TASK-001")
                .batchType(BatchType.GOODS_BATCH)
                .userId(testUserId)
                .status(BatchTaskStatus.SUCCESS)
                .totalCount(10)
                .successCount(10)
                .errorCount(0)
                .build();
        task1.setId(1L);

        Page<BatchTask> taskPage = new PageImpl<>(List.of(task1));
        Pageable pageable = PageRequest.of(0, 20);

        when(batchTaskRepository.findByUserId(testUserId, pageable)).thenReturn(taskPage);

        // Act
        Page<BatchTaskResponse> result = batchOperationService.getUserBatchTasks(testUserId, null, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        
        BatchTaskResponse response = result.getContent().get(0);
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTaskCode()).isEqualTo("TASK-001");
        assertThat(response.getBatchType()).isEqualTo(BatchType.GOODS_BATCH);
        assertThat(response.getStatus()).isEqualTo(BatchTaskStatus.SUCCESS);
    }

    @Test
    @DisplayName("应该能查询批量任务进度")
    void shouldGetTaskProgress() {
        // Arrange
        Long taskId = 100L;
        BatchTask task = BatchTask.builder()
                .taskCode("TASK-001")
                .status(BatchTaskStatus.PROCESSING)
                .totalCount(100)
                .successCount(60)
                .errorCount(10)
                .progressPercentage(70.0)
                .build();
        task.setId(taskId);

        when(batchTaskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act
        BatchTaskProgressResponse progress = batchOperationService.getTaskProgress(taskId);

        // Assert
        assertThat(progress).isNotNull();
        assertThat(progress.getTaskId()).isEqualTo(taskId);
        assertThat(progress.getTotalCount()).isEqualTo(100);
        assertThat(progress.getSuccessCount()).isEqualTo(60);
        assertThat(progress.getFailedCount()).isEqualTo(10);
        assertThat(progress.getProcessedCount()).isEqualTo(70);
        assertThat(progress.getProgressPercentage()).isEqualTo(70.0);
        assertThat(progress.getCompleted()).isFalse();
    }

    @Test
    @DisplayName("已完成任务的进度查询应标记为已完成")
    void shouldMarkCompletedTaskInProgress() {
        // Arrange
        Long taskId = 100L;
        BatchTask task = BatchTask.builder()
                .taskCode("TASK-001")
                .status(BatchTaskStatus.SUCCESS)
                .totalCount(100)
                .successCount(100)
                .errorCount(0)
                .progressPercentage(100.0)
                .build();
        task.setId(taskId);

        when(batchTaskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act
        BatchTaskProgressResponse progress = batchOperationService.getTaskProgress(taskId);

        // Assert
        assertThat(progress.getCompleted()).isTrue();
    }
}
