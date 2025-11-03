package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.EvidenceType;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * 上传证据请求DTO
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadEvidenceRequest {

    @NotNull(message = "纠纷ID不能为空")
    private Long disputeId;

    @NotNull(message = "证据类型不能为空")
    private EvidenceType evidenceType;

    @NotBlank(message = "文件URL不能为空")
    @Size(max = 500, message = "文件URL长度不能超过500字符")
    private String fileUrl;

    @NotBlank(message = "文件名不能为空")
    @Size(max = 200, message = "文件名长度不能超过200字符")
    private String fileName;

    @NotNull(message = "文件大小不能为空")
    @Min(value = 1, message = "文件大小必须大于0")
    @Max(value = 52428800, message = "文件大小不能超过50MB")
    private Long fileSize;

    @Size(max = 500, message = "证据描述长度不能超过500字符")
    private String description;
}
