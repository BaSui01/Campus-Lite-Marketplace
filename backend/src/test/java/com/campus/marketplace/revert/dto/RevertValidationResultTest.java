package com.campus.marketplace.revert.dto;

import com.campus.marketplace.revert.dto.RevertValidationResult.ValidationLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RevertValidationResult DTO 测试
 *
 * 测试场景：
 * 1. Builder 模式构建
 * 2. 静态工厂方法（success/warning/failed）
 * 3. Getter/Setter 方法
 * 4. equals/hashCode/toString
 * 5. ValidationLevel 枚举
 *
 * @author BaSui 😎 - DTO的每个方法都要测到！
 * @date 2025-11-03
 */
@DisplayName("RevertValidationResult DTO 测试")
class RevertValidationResultTest {

    @Test
    @DisplayName("使用Builder构建验证通过结果")
    void builder_ShouldCreateValidResult() {
        // Arrange & Act
        RevertValidationResult result = RevertValidationResult.builder()
                .valid(true)
                .message("验证通过")
                .level(ValidationLevel.SUCCESS)
                .build();

        // Assert
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isEqualTo("验证通过");
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.SUCCESS);
    }

    @Test
    @DisplayName("使用Builder构建验证失败结果")
    void builder_ShouldCreateInvalidResult() {
        // Arrange & Act
        RevertValidationResult result = RevertValidationResult.builder()
                .valid(false)
                .message("验证失败：超过撤销期限")
                .level(ValidationLevel.ERROR)
                .build();

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).isEqualTo("验证失败：超过撤销期限");
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.ERROR);
    }

    @Test
    @DisplayName("Builder默认级别应该是SUCCESS")
    void builder_DefaultLevelShouldBeSuccess() {
        // Arrange & Act
        RevertValidationResult result = RevertValidationResult.builder()
                .valid(true)
                .message("测试")
                .build();

        // Assert
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.SUCCESS);
    }

    @Test
    @DisplayName("静态工厂方法 - success()无参数")
    void staticSuccess_NoArgs_ShouldReturnSuccessResult() {
        // Act
        RevertValidationResult result = RevertValidationResult.success();

        // Assert
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isEqualTo("验证通过");
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.SUCCESS);
    }

    @Test
    @DisplayName("静态工厂方法 - success(message)")
    void staticSuccess_WithMessage_ShouldReturnSuccessResult() {
        // Act
        RevertValidationResult result = RevertValidationResult.success(
                "商品撤销验证通过");

        // Assert
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isEqualTo("商品撤销验证通过");
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.SUCCESS);
    }

    @Test
    @DisplayName("静态工厂方法 - warning(message)")
    void staticWarning_ShouldReturnWarningResult() {
        // Act
        RevertValidationResult result = RevertValidationResult.warning(
                "该操作已完成，但仍可撤销，需严格审批");

        // Assert
        assertThat(result.isValid()).isTrue(); // 警告仍然算验证通过
        assertThat(result.getMessage()).isEqualTo("该操作已完成，但仍可撤销，需严格审批");
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.WARNING);
    }

    @Test
    @DisplayName("静态工厂方法 - failed(message)")
    void staticFailed_ShouldReturnFailedResult() {
        // Act
        RevertValidationResult result = RevertValidationResult.failed(
                "超过撤销期限");

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).isEqualTo("超过撤销期限");
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.ERROR);
    }

    @Test
    @DisplayName("Setter 方法应该正常工作")
    void setters_ShouldWorkCorrectly() {
        // Arrange
        RevertValidationResult result = new RevertValidationResult();

        // Act
        result.setValid(true);
        result.setMessage("测试消息");
        result.setLevel(ValidationLevel.WARNING);

        // Assert
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isEqualTo("测试消息");
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.WARNING);
    }

    @Test
    @DisplayName("equals 和 hashCode 应该正常工作")
    void equalsAndHashCode_ShouldWorkCorrectly() {
        // Arrange
        RevertValidationResult result1 = RevertValidationResult.builder()
                .valid(true)
                .message("成功")
                .level(ValidationLevel.SUCCESS)
                .build();

        RevertValidationResult result2 = RevertValidationResult.builder()
                .valid(true)
                .message("成功")
                .level(ValidationLevel.SUCCESS)
                .build();

        RevertValidationResult result3 = RevertValidationResult.builder()
                .valid(false)
                .message("失败")
                .level(ValidationLevel.ERROR)
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
        RevertValidationResult result = RevertValidationResult.builder()
                .valid(true)
                .message("测试验证")
                .level(ValidationLevel.WARNING)
                .build();

        // Act
        String resultString = result.toString();

        // Assert
        assertThat(resultString).contains("valid=true");
        assertThat(resultString).contains("message=测试验证");
        assertThat(resultString).contains("level=WARNING");
    }

    @Test
    @DisplayName("AllArgsConstructor 应该正常工作")
    void allArgsConstructor_ShouldWorkCorrectly() {
        // Act
        RevertValidationResult result = new RevertValidationResult(
                true, "验证成功", ValidationLevel.SUCCESS);

        // Assert
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isEqualTo("验证成功");
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.SUCCESS);
    }

    @Test
    @DisplayName("NoArgsConstructor 应该正常工作")
    void noArgsConstructor_ShouldWorkCorrectly() {
        // Act
        RevertValidationResult result = new RevertValidationResult();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).isNull();
        assertThat(result.getLevel()).isEqualTo(ValidationLevel.SUCCESS); // @Builder.Default 会设置默认值
    }

    @Test
    @DisplayName("ValidationLevel枚举应该有3个值")
    void validationLevel_ShouldHaveThreeValues() {
        // Arrange & Act
        ValidationLevel[] levels = ValidationLevel.values();

        // Assert
        assertThat(levels).hasSize(3);
        assertThat(levels).contains(
                ValidationLevel.SUCCESS,
                ValidationLevel.WARNING,
                ValidationLevel.ERROR
        );
    }

    @Test
    @DisplayName("ValidationLevel.valueOf应该正常工作")
    void validationLevel_ValueOf_ShouldWorkCorrectly() {
        // Act & Assert
        assertThat(ValidationLevel.valueOf("SUCCESS"))
                .isEqualTo(ValidationLevel.SUCCESS);
        assertThat(ValidationLevel.valueOf("WARNING"))
                .isEqualTo(ValidationLevel.WARNING);
        assertThat(ValidationLevel.valueOf("ERROR"))
                .isEqualTo(ValidationLevel.ERROR);
    }
}
