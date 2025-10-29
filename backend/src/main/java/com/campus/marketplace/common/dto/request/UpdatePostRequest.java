package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 更新帖子请求 DTO
 *
 * @author BaSui
 * @date 2025-10-29
 */

public record UpdatePostRequest(
        @Size(min = 1, max = 100, message = "帖子标题长度必须在 1-100 字符之间")
        String title,

        @Size(min = 1, max = 5000, message = "帖子内容长度必须在 1-5000 字符之间")
        String content,

        @Size(max = 9, message = "图片数量不能超过 9 张")
        List<String> images
) {}
