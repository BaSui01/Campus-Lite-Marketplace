package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 更新帖子请求 DTO
 *
 * 封装用户编辑帖子的请求数据
 *
 * @param title 帖子标题（可选，1-100字符）
 * @param content 帖子内容（可选，1-5000字符）
 * @param images 图片 URL 列表（可选，最多9张）
 * @param tagIds 标签 ID 列表（可选，最多10个）
 *
 * @author BaSui
 * @date 2025-10-29
 * @updated 2025-11-08 - 添加标签支持 😎
 */
public record UpdatePostRequest(
        @Size(min = 1, max = 100, message = "帖子标题长度必须在 1-100 字符之间")
        String title,

        @Size(min = 1, max = 5000, message = "帖子内容长度必须在 1-5000 字符之间")
        String content,

        @Size(max = 9, message = "图片数量不能超过 9 张")
        List<String> images,

        @Size(max = 10, message = "最多选择 10 个标签")
        List<Long> tagIds
) {}
