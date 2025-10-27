package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建回复请求 DTO
 * 
 * @param postId 帖子 ID（必填）
 * @param content 回复内容（必填，1-1000字符）
 * @param parentId 父回复 ID（可选，楼中楼）
 * @param toUserId 回复目标用户 ID（可选，楼中楼）
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public record CreateReplyRequest(
        
        @NotNull(message = "帖子 ID 不能为空")
        Long postId,
        
        @NotBlank(message = "回复内容不能为空")
        @Size(min = 1, max = 1000, message = "回复内容长度必须在 1-1000 字符之间")
        String content,
        
        Long parentId,
        
        Long toUserId
) {
}