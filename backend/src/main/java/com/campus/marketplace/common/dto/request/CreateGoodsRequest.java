package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建物品请求 DTO
 * 
 * 使用 Java 21 Record 类型简化代码
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public record CreateGoodsRequest(
        
        @NotBlank(message = "物品标题不能为空")
        @Size(min = 5, max = 100, message = "物品标题长度必须在 5-100 个字符之间")
        String title,
        
        @NotBlank(message = "物品描述不能为空")
        @Size(min = 10, max = 2000, message = "物品描述长度必须在 10-2000 个字符之间")
        String description,
        
        @NotNull(message = "物品价格不能为空")
        @DecimalMin(value = "0.01", message = "物品价格必须大于 0")
        @Digits(integer = 8, fraction = 2, message = "价格格式不正确")
        BigDecimal price,
        
        @NotNull(message = "物品分类不能为空")
        Long categoryId,
        
        @NotNull(message = "物品图片不能为空")
        @Size(min = 1, max = 9, message = "物品图片数量必须在 1-9 张之间")
        List<String> images
) {
}
