package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import com.campus.marketplace.common.dto.response.MaterialUploadResponse;
import com.campus.marketplace.common.entity.AppealMaterial;
import com.campus.marketplace.service.AppealMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 申诉材料控制器
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Tag(name = "申诉材料")
@RestController
@RequestMapping("/api/appeals/materials")
@RequiredArgsConstructor
public class AppealMaterialController {

    private final AppealMaterialService appealMaterialService;

    @Operation(summary = "上传申诉材料")

    @PostMapping("/{appealId}/upload")
    public ApiResponse<MaterialUploadResponse> uploadMaterials(
            @PathVariable String appealId,
            @RequestParam("files") MultipartFile[] files
    ) {
        try {
            Long currentUserId = getCurrentUserId();
            String currentUserName = getCurrentUserName();
            
            MaterialUploadResponse response = appealMaterialService.uploadMaterials(
                appealId, files, currentUserId, currentUserName);
            
            if (Boolean.TRUE.equals(response.getSuccess())) {
                return ApiResponse.success(response);
            } else {
                return ApiResponse.error(400, "材料上传失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "上传过程中发生错误: " + e.getMessage());
        }
    }

    @Operation(summary = "删除申诉材料")

    @DeleteMapping("/{materialId}")
    public ApiResponse<Void> deleteMaterial(@PathVariable Long materialId) {
        try {
            Long currentUserId = getCurrentUserId();
            
            boolean success = appealMaterialService.deleteMaterial(materialId, currentUserId);
            
            if (success) {
                return ApiResponse.success();
            } else {
                return ApiResponse.error(400, "删除材料失败或无权限");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "删除过程中发生错误: " + e.getMessage());
        }
    }

    @Operation(summary = "获取申诉材料列表")
    @GetMapping("/{appealId}")
    public ApiResponse<java.util.List<AppealMaterial>> getAppealMaterials(@PathVariable String appealId) {
        try {
            java.util.List<AppealMaterial> materials = appealMaterialService.getAppealMaterials(appealId);
            return ApiResponse.success(materials);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取材料列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "分页获取申诉材料")
    @GetMapping("/{appealId}/page")
    public ApiResponse<Page<AppealMaterial>> getAppealMaterialsPage(
            @PathVariable String appealId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = Pageable.ofSize(size);
            Page<AppealMaterial> materialsPage = appealMaterialService.getAppealMaterials(appealId, pageable);
            return ApiResponse.success(materialsPage);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取材料列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "按类型获取材料")
    @GetMapping("/{appealId}/type/{fileType}")
    public ApiResponse<java.util.List<AppealMaterial>> getMaterialsByType(
            @PathVariable String appealId,
            @PathVariable String fileType) {
        try {
            java.util.List<AppealMaterial> materials = appealMaterialService.getMaterialsByType(appealId, fileType);
            return ApiResponse.success(materials);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取材料列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "生成缩略图")

    @PostMapping("/{materialId}/thumbnail")
    public ApiResponse<Boolean> generateThumbnail(@PathVariable Long materialId) {
        try {
            boolean success = appealMaterialService.generateThumbnail(materialId);
            if (success) {
                return ApiResponse.success(true);
            } else {
                return ApiResponse.error(400, "生成缩略图失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "生成缩略图失败: " + e.getMessage());
        }
    }

    @Operation(summary = "病毒扫描")

    @PostMapping("/{materialId}/scan")
    public ApiResponse<String> scanFileForVirus(@PathVariable Long materialId) {
        try {
            String result = appealMaterialService.scanFileForVirus(materialId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(500, "病毒扫描失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量病毒扫描")

    @PostMapping("/batch-scan")
    public ApiResponse<String> batchVirusScan() {
        try {
            String result = appealMaterialService.batchVirusScan();
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(500, "批量扫描失败: " + e.getMessage());
        }
    }

    @Operation(summary = "下载文件")

    @GetMapping("/{materialId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long materialId) {
        try {
            String downloadUrl = appealMaterialService.getDownloadUrl(materialId);
            if (downloadUrl == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 实际实现中应该根据URL生成文件下载响应
            // 这里简化处理
            return ResponseEntity.ok().header("Location", downloadUrl).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "检查文件是否存在")

    @GetMapping("/{materialId}/exists")
    public ApiResponse<Boolean> checkFileExists(@PathVariable Long materialId) {
        try {
            boolean exists = appealMaterialService.fileExists(materialId);
            return ApiResponse.success(exists);
        } catch (Exception e) {
            return ApiResponse.error(500, "检查文件存在性失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取材料统计信息")

    @GetMapping("/{appealId}/statistics")
    public ApiResponse<Object> getMaterialStatistics(
            @PathVariable String appealId) {
        try {
            Object stats = appealMaterialService.getMaterialStatistics(appealId);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取统计信息失败: " + e.getMessage());
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                return Long.parseLong(authentication.getName());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}
