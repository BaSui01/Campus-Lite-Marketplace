package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.BatchTaskStatus;
import com.campus.marketplace.common.enums.BatchType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("批量任务实体测试")
class BatchTaskTest {

    @Test
    @DisplayName("应该正确计算执行时长")
    void shouldCalculateDuration() {
        // Arrange
        BatchTask task = BatchTask.builder()
                .taskCode("TASK-001")
                .batchType(BatchType.GOODS_BATCH)
                .userId(1L)
                .build();
        
        LocalDateTime startTime = LocalDateTime.of(2025, 11, 2, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 11, 2, 10, 5, 30);
        task.setStartTime(startTime);
        task.setEndTime(endTime);
        
        // Act
        Long duration = task.calculateDuration();
        
        // Assert
        assertThat(duration).isEqualTo(330L); // 5分30秒 = 330秒
    }

    @Test
    @DisplayName("开始和结束时间为空时应返回null")
    void shouldReturnNullWhenTimesAreNull() {
        // Arrange
        BatchTask task = BatchTask.builder().build();
        
        // Act & Assert
        assertThat(task.calculateDuration()).isNull();
        
        task.setStartTime(LocalDateTime.now());
        assertThat(task.calculateDuration()).isNull();
    }

    @Test
    @DisplayName("应该正确更新进度百分比")
    void shouldUpdateProgress() {
        // Arrange
        BatchTask task = BatchTask.builder()
                .totalCount(100)
                .successCount(60)
                .errorCount(20)
                .build();
        
        // Act
        task.updateProgress();
        
        // Assert
        assertThat(task.getProgressPercentage()).isEqualTo(80.0);
    }

    @Test
    @DisplayName("总数为0时进度应不变")
    void shouldNotUpdateProgressWhenTotalIsZero() {
        // Arrange
        BatchTask task = BatchTask.builder()
                .totalCount(0)
                .successCount(0)
                .errorCount(0)
                .progressPercentage(0.0)
                .build();
        
        // Act
        task.updateProgress();
        
        // Assert
        assertThat(task.getProgressPercentage()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("应该正确判断任务是否完成")
    void shouldCheckIfCompleted() {
        // Arrange
        BatchTask successTask = BatchTask.builder()
                .status(BatchTaskStatus.SUCCESS)
                .build();
        
        BatchTask partialTask = BatchTask.builder()
                .status(BatchTaskStatus.PARTIAL_SUCCESS)
                .build();
        
        BatchTask failedTask = BatchTask.builder()
                .status(BatchTaskStatus.FAILED)
                .build();
        
        BatchTask cancelledTask = BatchTask.builder()
                .status(BatchTaskStatus.CANCELLED)
                .build();
        
        BatchTask processingTask = BatchTask.builder()
                .status(BatchTaskStatus.PROCESSING)
                .build();
        
        BatchTask pendingTask = BatchTask.builder()
                .status(BatchTaskStatus.PENDING)
                .build();
        
        // Act & Assert
        assertThat(successTask.isCompleted()).isTrue();
        assertThat(partialTask.isCompleted()).isTrue();
        assertThat(failedTask.isCompleted()).isTrue();
        assertThat(cancelledTask.isCompleted()).isTrue();
        assertThat(processingTask.isCompleted()).isFalse();
        assertThat(pendingTask.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("Builder应该正确设置默认值")
    void shouldSetDefaultValues() {
        // Act
        BatchTask task = BatchTask.builder()
                .taskCode("TASK-001")
                .batchType(BatchType.GOODS_BATCH)
                .userId(1L)
                .build();
        
        // Assert
        assertThat(task.getStatus()).isEqualTo(BatchTaskStatus.PENDING);
        assertThat(task.getProgressPercentage()).isEqualTo(0.0);
        assertThat(task.getTotalCount()).isEqualTo(0);
        assertThat(task.getSuccessCount()).isEqualTo(0);
        assertThat(task.getErrorCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("应该正确设置所有字段")
    void shouldSetAllFields() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        // Act
        BatchTask task = BatchTask.builder()
                .taskCode("TASK-001")
                .batchType(BatchType.PRICE_BATCH)
                .userId(1L)
                .totalCount(100)
                .successCount(80)
                .errorCount(20)
                .status(BatchTaskStatus.PARTIAL_SUCCESS)
                .startTime(now)
                .endTime(now.plusMinutes(10))
                .estimatedDuration(600)
                .progressPercentage(100.0)
                .requestData("{\"operation\":\"update\"}")
                .resultData("{\"success\":80,\"failed\":20}")
                .errorSummary("20个商品更新失败")
                .build();
        
        // Assert
        assertThat(task.getTaskCode()).isEqualTo("TASK-001");
        assertThat(task.getBatchType()).isEqualTo(BatchType.PRICE_BATCH);
        assertThat(task.getUserId()).isEqualTo(1L);
        assertThat(task.getTotalCount()).isEqualTo(100);
        assertThat(task.getSuccessCount()).isEqualTo(80);
        assertThat(task.getErrorCount()).isEqualTo(20);
        assertThat(task.getStatus()).isEqualTo(BatchTaskStatus.PARTIAL_SUCCESS);
        assertThat(task.getStartTime()).isEqualTo(now);
        assertThat(task.getEndTime()).isEqualTo(now.plusMinutes(10));
        assertThat(task.getEstimatedDuration()).isEqualTo(600);
        assertThat(task.getProgressPercentage()).isEqualTo(100.0);
        assertThat(task.getRequestData()).isEqualTo("{\"operation\":\"update\"}");
        assertThat(task.getResultData()).isEqualTo("{\"success\":80,\"failed\":20}");
        assertThat(task.getErrorSummary()).isEqualTo("20个商品更新失败");
    }
}
