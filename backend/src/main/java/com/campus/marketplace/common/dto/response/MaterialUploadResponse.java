package com.campus.marketplace.common.dto.response;

import com.campus.marketplace.common.entity.AppealMaterial;
import lombok.*;

import java.util.List;

/**
 * 材料上传响应
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialUploadResponse {

    /**
     * 申诉ID
     */
    private String appealId;

    /**
     * 上传总数
     */
    private Integer totalCount;

    /**
     * 成功上传数量
     */
    private Integer successCount;

    /**
     * 失败上传数量
     */
    private Integer failureCount;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 成功上传的材料列表
     */
    private List<AppealMaterial> successMaterials;

    /**
     * 错误列表
     */
    private List<BatchError> errors;

    /**
     * 处理时间（毫秒）
     */
    private Long processingTimeMs;

    /**
     * 上传消息
     */
    private String message;
}
