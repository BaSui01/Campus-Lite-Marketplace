package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.service.FileService;
import com.campus.marketplace.common.annotation.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器 - 支持图片上传和缩略图生成
 *
 * @author BaSui 😎
 * @since 2025-10-27
 */
@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传、删除等操作")
public class FileController {

    private final FileService fileService;

        @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "上传图片", description = "上传单个图片文件，返回访问URL")
    @RateLimit(key = "file:upload", maxRequests = 30, timeWindow = 60)
    public ApiResponse<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("收到文件上传请求: {}", file.getOriginalFilename());

        String fileUrl = fileService.uploadFile(file);

        Map<String, String> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("filename", file.getOriginalFilename());

        return ApiResponse.success(result);
    }

        @PostMapping(value = "/upload-with-thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "上传图片（带缩略图）", description = "上传图片并自动生成缩略图")
    @RateLimit(key = "file:upload-thumb", maxRequests = 20, timeWindow = 60)
    public ApiResponse<Map<String, String>> uploadFileWithThumbnail(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("收到文件上传请求（带缩略图）: {}", file.getOriginalFilename());

        String fileUrl = fileService.uploadFileWithThumbnail(file);

        // 生成缩略图URL
        String thumbnailUrl = fileUrl.replace(
                fileUrl.substring(fileUrl.lastIndexOf(".")),
                "_thumb" + fileUrl.substring(fileUrl.lastIndexOf("."))
        );

        Map<String, String> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("thumbnail", thumbnailUrl);
        result.put("filename", file.getOriginalFilename());

        return ApiResponse.success(result);
    }

        @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除文件", description = "删除指定的文件（包括缩略图）")
    public ApiResponse<Boolean> deleteFile(@Parameter(description = "文件URL", example = "https://cdn.campus.com/uploads/xxx.png") @RequestParam("url") String fileUrl) {
        log.info("收到文件删除请求: {}", fileUrl);

        boolean result = fileService.deleteFile(fileUrl);

        return result ? ApiResponse.success(true) : ApiResponse.error(500, "文件删除失败");
    }
}
