package com.campus.marketplace.common.dto.request;

import lombok.*;

/**
 * 材料上传请求
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialUploadRequest {

    private String appealId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String uploadedBy;
    private String uploadedByName;
    private String description;

    /**
     * 文件验证结果内部类
     */
    public static class uploadValidationResult {
        private boolean valid;
        private String errorCode;
        private String errorMessage;

        public uploadValidationResult(boolean valid, String errorCode, String errorMessage) {
            this.valid = valid;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * 文件上传结果
     */
    public static class uploadResult {
        private boolean success;
        private String materialId;
        private String errorMessage;

        public uploadResult(boolean success, String materialId, String errorMessage) {
            this.success = success;
            this.materialId = materialId;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMaterialId() {
            return materialId;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
