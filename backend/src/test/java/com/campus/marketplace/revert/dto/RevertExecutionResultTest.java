package com.campus.marketplace.revert.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RevertExecutionResult DTO 测试
 *
 * 测试场景：
 * 1. Builder 模式构建
 * 2. 静态工厂方法（success/failed）
 * 3. Getter/Setter 方法
 * 4. equals/hashCode/toString
 *
 * @author BaSui 😎 - DTO也要测试啊老铁！
 * @date 2025-11-03
 */
@DisplayName("RevertExecutionResult DTO 测试")
class RevertExecutionResultTest {

    @Test
    @DisplayName("使用Builder构建成功结果")
    void builder_ShouldCreateSuccessResult() {
        // Arrange & Act
        RevertExecutionResult result = RevertExecutionResult.builder()
                .success(true)
                .message("撤销成功")
                .entityId(100L)
                .entityType("GOODS")
                .executionTime(150L)
                .additionalData("extra data")
                .build();

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("撤销成功");
        assertThat(result.getEntityId()).isEqualTo(100L);
        assertThat(result.getEntityType()).isEqualTo("GOODS");
        assertThat(result.getExecutionTime()).isEqualTo(150L);
        assertThat(result.getAdditionalData()).isEqualTo("extra data");
    }

    @Test
    @DisplayName("使用Builder构建失败结果")
    void builder_ShouldCreateFailureResult() {
        // Arrange & Act
        RevertExecutionResult result = RevertExecutionResult.builder()
                .success(false)
                .message("撤销失败：权限不足")
                .entityId(200L)
                .entityType("ORDER")
                .build();

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("撤销失败：权限不足");
        assertThat(result.getEntityId()).isEqualTo(200L);
        assertThat(result.getEntityType()).isEqualTo("ORDER");
    }

    @Test
    @DisplayName("静态工厂方法 - success(message, entityId)")
    void staticSuccess_ShouldReturnSuccessResult() {
        // Act
        RevertExecutionResult result = RevertExecutionResult.success(
                "商品删除已撤销", 100L);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("商品删除已撤销");
        assertThat(result.getEntityId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("静态工厂方法 - failed(message)")
    void staticFailed_ShouldReturnFailureResult() {
        // Act
        RevertExecutionResult result = RevertExecutionResult.failed(
                "备份数据不存在");

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("备份数据不存在");
        assertThat(result.getEntityId()).isNull();
    }

    @Test
    @DisplayName("静态工厂方法 - failed(message, entityId)")
    void staticFailedWithEntityId_ShouldReturnFailureResult() {
        // Act
        RevertExecutionResult result = RevertExecutionResult.failed(
                "订单不存在", 200L);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("订单不存在");
        assertThat(result.getEntityId()).isEqualTo(200L);
    }

    @Test
    @DisplayName("Setter 方法应该正常工作")
    void setters_ShouldWorkCorrectly() {
        // Arrange
        RevertExecutionResult result = new RevertExecutionResult();

        // Act
        result.setSuccess(true);
        result.setMessage("测试消息");
        result.setEntityId(300L);
        result.setEntityType("USER");
        result.setExecutionTime(200L);
        result.setAdditionalData("test data");

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("测试消息");
        assertThat(result.getEntityId()).isEqualTo(300L);
        assertThat(result.getEntityType()).isEqualTo("USER");
        assertThat(result.getExecutionTime()).isEqualTo(200L);
        assertThat(result.getAdditionalData()).isEqualTo("test data");
    }

    @Test
    @DisplayName("equals 和 hashCode 应该正常工作")
    void equalsAndHashCode_ShouldWorkCorrectly() {
        // Arrange
        RevertExecutionResult result1 = RevertExecutionResult.builder()
                .success(true)
                .message("成功")
                .entityId(100L)
                .build();

        RevertExecutionResult result2 = RevertExecutionResult.builder()
                .success(true)
                .message("成功")
                .entityId(100L)
                .build();

        RevertExecutionResult result3 = RevertExecutionResult.builder()
                .success(false)
                .message("失败")
                .entityId(200L)
                .build();

        // Assert
        assertThat(result1).isEqualTo(result2);
        assertThat(result1).isNotEqualTo(result3);
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        assertThat(result1.hashCode()).isNotEqualTo(result3.hashCode());
    }

    @Test
    @DisplayName("toString 应该包含所有字段")
    void toString_ShouldContainAllFields() {
        // Arrange
        RevertExecutionResult result = RevertExecutionResult.builder()
                .success(true)
                .message("测试")
                .entityId(100L)
                .entityType("GOODS")
                .executionTime(150L)
                .build();

        // Act
        String resultString = result.toString();

        // Assert
        assertThat(resultString).contains("success=true");
        assertThat(resultString).contains("message=测试");
        assertThat(resultString).contains("entityId=100");
        assertThat(resultString).contains("entityType=GOODS");
        assertThat(resultString).contains("executionTime=150");
    }

    @Test
    @DisplayName("AllArgsConstructor 应该正常工作")
    void allArgsConstructor_ShouldWorkCorrectly() {
        // Act
        RevertExecutionResult result = new RevertExecutionResult(
                true, "成功消息", 100L, "GOODS", 150L, "extra");

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("成功消息");
        assertThat(result.getEntityId()).isEqualTo(100L);
        assertThat(result.getEntityType()).isEqualTo("GOODS");
        assertThat(result.getExecutionTime()).isEqualTo(150L);
        assertThat(result.getAdditionalData()).isEqualTo("extra");
    }

    @Test
    @DisplayName("NoArgsConstructor 应该正常工作")
    void noArgsConstructor_ShouldWorkCorrectly() {
        // Act
        RevertExecutionResult result = new RevertExecutionResult();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isNull();
        assertThat(result.getEntityId()).isNull();
    }
}
