package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.BatchItemStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("批量任务项实体测试")
class BatchTaskItemTest {

    @Test
    @DisplayName("应该正确增加重试次数")
    void shouldIncrementRetry() {
        // Arrange
        BatchTaskItem item = BatchTaskItem.builder()
                .batchTaskId(1L)
                .targetId(100L)
                .retryCount(0)
                .build();
        
        // Act
        item.incrementRetry();
        item.incrementRetry();
        
        // Assert
        assertThat(item.getRetryCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("应该正确判断是否可以重试")
    void shouldCheckCanRetry() {
        // Arrange
        BatchTaskItem failedItem = BatchTaskItem.builder()
                .status(BatchItemStatus.FAILED)
                .retryCount(2)
                .build();
        
        BatchTaskItem maxRetryItem = BatchTaskItem.builder()
                .status(BatchItemStatus.FAILED)
                .retryCount(3)
                .build();
        
        BatchTaskItem successItem = BatchTaskItem.builder()
                .status(BatchItemStatus.SUCCESS)
                .retryCount(1)
                .build();
        
        // Act & Assert
        assertThat(failedItem.canRetry(3)).isTrue();
        assertThat(maxRetryItem.canRetry(3)).isFalse();
        assertThat(successItem.canRetry(3)).isFalse();
    }

    @Test
    @DisplayName("应该正确标记为失败")
    void shouldMarkFailed() {
        // Arrange
        BatchTaskItem item = BatchTaskItem.builder()
                .status(BatchItemStatus.PROCESSING)
                .build();
        
        // Act
        item.markFailed("数据验证失败");
        
        // Assert
        assertThat(item.getStatus()).isEqualTo(BatchItemStatus.FAILED);
        assertThat(item.getErrorMessage()).isEqualTo("数据验证失败");
    }

    @Test
    @DisplayName("应该正确标记为成功")
    void shouldMarkSuccess() {
        // Arrange
        BatchTaskItem item = BatchTaskItem.builder()
                .status(BatchItemStatus.PROCESSING)
                .errorMessage("旧错误信息")
                .build();
        
        // Act
        item.markSuccess();
        
        // Assert
        assertThat(item.getStatus()).isEqualTo(BatchItemStatus.SUCCESS);
        assertThat(item.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Builder应该正确设置默认值")
    void shouldSetDefaultValues() {
        // Act
        BatchTaskItem item = BatchTaskItem.builder()
                .batchTaskId(1L)
                .targetId(100L)
                .build();
        
        // Assert
        assertThat(item.getStatus()).isEqualTo(BatchItemStatus.PENDING);
        assertThat(item.getRetryCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("应该正确设置所有字段")
    void shouldSetAllFields() {
        // Act
        BatchTaskItem item = BatchTaskItem.builder()
                .batchTaskId(1L)
                .targetId(100L)
                .targetType("GOODS")
                .status(BatchItemStatus.SUCCESS)
                .inputData("{\"price\":99.99}")
                .outputData("{\"oldPrice\":88.88,\"newPrice\":99.99}")
                .errorMessage(null)
                .retryCount(1)
                .processingTime(250)
                .shardKey("shard-1")
                .build();
        
        // Assert
        assertThat(item.getBatchTaskId()).isEqualTo(1L);
        assertThat(item.getTargetId()).isEqualTo(100L);
        assertThat(item.getTargetType()).isEqualTo("GOODS");
        assertThat(item.getStatus()).isEqualTo(BatchItemStatus.SUCCESS);
        assertThat(item.getInputData()).isEqualTo("{\"price\":99.99}");
        assertThat(item.getOutputData()).isEqualTo("{\"oldPrice\":88.88,\"newPrice\":99.99}");
        assertThat(item.getErrorMessage()).isNull();
        assertThat(item.getRetryCount()).isEqualTo(1);
        assertThat(item.getProcessingTime()).isEqualTo(250);
        assertThat(item.getShardKey()).isEqualTo("shard-1");
    }
}
