package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.service.FileSecurityService;
import com.campus.marketplace.service.FileService;
import jakarta.annotation.PostConstruct;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private final FileSecurityService fileSecurityService;

    @Value("${file.upload.dir:backend/uploads}")
    private String uploadDir;

    @Value("${file.upload.max-size:10485760}") // 默认 10MB
    private Long maxFileSize;

    @Value("${file.upload.allowed-types:image/jpeg,image/png,image/gif,image/webp,video/mp4,video/mpeg,video/quicktime,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet}")
    private String[] allowedTypes;

    // 🎯 构造函数注入 FileSecurityService
    public FileServiceImpl(FileSecurityService fileSecurityService) {
        this.fileSecurityService = fileSecurityService;
    }

    /**
     * 初始化方法：确保上传目录存在
     */
    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("✅ 创建上传目录成功: {}", uploadPath.toAbsolutePath());
            } else {
                log.info("✅ 上传目录已存在: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("❌ 创建上传目录失败: {}", uploadDir, e);
            throw new RuntimeException("无法创建上传目录: " + uploadDir, e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        // 🎯 第一步：执行完整的安全检查（集成 FileSecurityService）
        try {
            // 1. 执行基础安全检查（文件非空、文件名、类型、扩展名匹配）
            fileSecurityService.performSecurityCheck(file);

            // 2. 验证文件大小
            fileSecurityService.validateFileSize(file, maxFileSize);

            // 3. 验证文件魔数（防止伪造Content-Type）
            fileSecurityService.validateFileMagicNumber(file);

            log.info("文件安全检查全部通过: {}", file.getOriginalFilename());
        } catch (IllegalArgumentException e) {
            // 将安全检查异常转换为业务异常
            throw new BusinessException(ErrorCode.INVALID_PARAM, e.getMessage());
        }

        // 🎯 第二步：根据文件类型确定分类目录
        String categoryDir = determineFileCategory(file.getContentType());

        // 🎯 第三步：生成唯一文件名
        String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());

        // 🎯 第四步：按日期分类创建子目录（格式：category/yyyy/MM/dd）
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadPath = Paths.get(uploadDir, categoryDir, dateDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.debug("创建分类子目录: {}", uploadPath);
        }

        // 🎯 第五步：保存文件（使用重试机制防止文件名冲突）
        Path filePath = uploadPath.resolve(uniqueFileName);

        // 🛑 安全检查：如果文件已存在，重新生成文件名
        int retryCount = 0;
        while (Files.exists(filePath) && retryCount < 3) {
            log.warn("文件已存在，重新生成文件名: {}", uniqueFileName);
            uniqueFileName = generateUniqueFileName(file.getOriginalFilename());
            filePath = uploadPath.resolve(uniqueFileName);
            retryCount++;
        }

        if (Files.exists(filePath)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败：文件名冲突");
        }

        Files.copy(file.getInputStream(), filePath);

        log.info("文件上传成功: {}/{}/{}", categoryDir, dateDir, uniqueFileName);

        // 🎯 返回访问URL（包含分类和日期路径）
        return "/uploads/" + categoryDir + "/" + dateDir + "/" + uniqueFileName;
    }

    @Override
    public String uploadFileWithThumbnail(MultipartFile file) throws IOException {
        // 🎯 第一步：先上传原图
        String fileUrl = uploadFile(file);

        // 🎯 第二步：生成缩略图（仅支持图片）
        try {
            // 提取文件路径（去掉 /uploads/ 前缀）
            String relativePath = fileUrl.replace("/uploads/", "");
            Path originalFile = Paths.get(uploadDir, relativePath);

            // 生成缩略图文件名
            String fileName = originalFile.getFileName().toString();
            String extension = FilenameUtils.getExtension(fileName);
            String baseName = FilenameUtils.getBaseName(fileName);
            String thumbnailFileName = baseName + "_thumb." + extension;
            Path thumbnailPath = originalFile.getParent().resolve(thumbnailFileName);

            // 使用 Thumbnailator 生成缩略图 (最大 200x200)
            Thumbnails.of(originalFile.toFile())
                    .size(200, 200)
                    .keepAspectRatio(true)
                    .toFile(thumbnailPath.toFile());

            log.info("缩略图生成成功: {}", thumbnailPath);
        } catch (Exception e) {
            log.warn("缩略图生成失败（但原图上传成功）: {}", e.getMessage());
            // 缩略图生成失败不影响主流程，只记录警告
        }

        return fileUrl;
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            // 🎯 提取文件路径（去掉 /uploads/ 前缀）
            String relativePath = fileUrl.replace("/uploads/", "");

            // 🛑 安全检查：防止路径遍历攻击
            if (relativePath.contains("..") || relativePath.contains("//") || relativePath.startsWith("/")) {
                log.error("检测到路径遍历攻击：{}", fileUrl);
                throw new BusinessException(ErrorCode.INVALID_PARAM, "非法的文件路径");
            }

            // 🛑 路径规范化：解析并验证路径安全性
            Path uploadBasePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadBasePath.resolve(relativePath).normalize();

            // 🛑 边界检查：确保文件路径在上传目录内
            if (!filePath.startsWith(uploadBasePath)) {
                log.error("路径超出边界：fileUrl={}, uploadDir={}, resolvedPath={}",
                    fileUrl, uploadBasePath, filePath);
                throw new BusinessException(ErrorCode.INVALID_PARAM, "非法的文件路径");
            }

            // 🎯 删除文件
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("文件删除成功: {}", relativePath);

                // 🎯 如果有缩略图，也一起删除
                String fileName = filePath.getFileName().toString();
                String extension = FilenameUtils.getExtension(fileName);
                String baseName = FilenameUtils.getBaseName(fileName);
                String thumbnailFileName = baseName + "_thumb." + extension;
                Path thumbnailPath = filePath.getParent().resolve(thumbnailFileName);
                if (Files.exists(thumbnailPath)) {
                    Files.delete(thumbnailPath);
                    log.info("缩略图删除成功: {}", thumbnailPath);
                }

                return true;
            }
            return false;
        } catch (BusinessException e) {
            // 重新抛出业务异常
            throw e;
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
     * 根据文件MIME类型确定分类目录
     *
     * @param contentType 文件MIME类型
     * @return 分类目录名称
     */
    private String determineFileCategory(String contentType) {
        if (contentType == null) {
            return "others";
        }

        // 🎨 图片文件 → images/
        if (contentType.startsWith("image/")) {
            return "images";
        }

        // 🎬 视频文件 → videos/
        if (contentType.startsWith("video/")) {
            return "videos";
        }

        // 📄 文档文件 → documents/
        if (contentType.startsWith("application/pdf") ||
            contentType.startsWith("application/msword") ||
            contentType.startsWith("application/vnd.openxmlformats") ||
            contentType.startsWith("application/vnd.ms-excel") ||
            contentType.startsWith("text/plain")) {
            return "documents";
        }

        // 🗂️ 其他文件 → others/
        return "others";
    }
}
