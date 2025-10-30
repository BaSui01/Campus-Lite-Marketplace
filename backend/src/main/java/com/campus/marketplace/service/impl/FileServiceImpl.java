package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.service.FileService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

/**
 * 文件上传服务实现类 - 本地存储版本
 *
 * @author BaSui 😎
 * @since 2025-10-27
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.upload.max-size:10485760}") // 默认 10MB
    private Long maxFileSize;

    @Value("${file.upload.allowed-types:image/jpeg,image/png,image/gif,image/webp}")
    private String[] allowedTypes;

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        // 🎯 第一步：验证文件
        validateFile(file);

        // 🎯 第二步：生成唯一文件名
        String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());

        // 🎯 第三步：确保上传目录存在
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 🎯 第四步：保存文件
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("文件上传成功: {}", uniqueFileName);

        // 🎯 返回访问URL
        return "/uploads/" + uniqueFileName;
    }

    @Override
    public String uploadFileWithThumbnail(MultipartFile file) throws IOException {
        // 🎯 第一步：先上传原图
        String fileUrl = uploadFile(file);

        // 🎯 第二步：生成缩略图
        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path originalFile = Paths.get(uploadDir, fileName);
            
            // 生成缩略图文件名
            String extension = FilenameUtils.getExtension(fileName);
            String baseName = FilenameUtils.getBaseName(fileName);
            String thumbnailFileName = baseName + "_thumb." + extension;
            Path thumbnailPath = Paths.get(uploadDir, thumbnailFileName);

            // 使用 Thumbnailator 生成缩略图 (最大 200x200)
            Thumbnails.of(originalFile.toFile())
                    .size(200, 200)
                    .keepAspectRatio(true)
                    .toFile(thumbnailPath.toFile());

            log.info("缩略图生成成功: {}", thumbnailFileName);
        } catch (Exception e) {
            log.warn("缩略图生成失败（但原图上传成功）: {}", e.getMessage());
            // 缩略图生成失败不影响主流程，只记录警告
        }

        return fileUrl;
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            // 🎯 提取文件名
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir, fileName);

            // 🎯 删除文件
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("文件删除成功: {}", fileName);

                // 🎯 如果有缩略图，也一起删除
                String extension = FilenameUtils.getExtension(fileName);
                String baseName = FilenameUtils.getBaseName(fileName);
                String thumbnailFileName = baseName + "_thumb." + extension;
                Path thumbnailPath = Paths.get(uploadDir, thumbnailFileName);
                if (Files.exists(thumbnailPath)) {
                    Files.delete(thumbnailPath);
                    log.info("缩略图删除成功: {}", thumbnailFileName);
                }

                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String generateUniqueFileName(String originalFilename) {
        // 🎯 格式：时间戳_随机8位UUID.扩展名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String extension = FilenameUtils.getExtension(originalFilename);
        return timestamp + "_" + randomCode + "." + extension;
    }

    /**
     * 验证上传文件的合法性
     */
    private void validateFile(MultipartFile file) {
        // 🚫 验证：文件不能为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "文件不能为空");
        }

        // 🚫 验证：文件大小
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(
                    ErrorCode.INVALID_PARAM,
                    "文件大小超过限制（最大 " + (maxFileSize / 1024 / 1024) + "MB）"
            );
        }

        // 🚫 验证：文件类型
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(allowedTypes).contains(contentType)) {
            throw new BusinessException(
                    ErrorCode.INVALID_PARAM,
                    "不支持的文件类型，仅支持: " + String.join(", ", allowedTypes)
            );
        }
    }
}
