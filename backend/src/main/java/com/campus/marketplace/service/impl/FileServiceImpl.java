package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.service.FileSecurityService;
import com.campus.marketplace.service.FileService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
import java.util.Base64;
import java.util.UUID;

/**
 * 文件上传服务实现类 - 本地存储版本
 *
 * @author BaSui 😎
 * @since 2025-10-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileSecurityService fileSecurityService;

    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${file.upload.max-size:10485760}") // 默认 10MB
    private Long maxFileSize;

    @Value("${file.upload.allowed-types:image/jpeg,image/png,image/gif,image/webp,video/mp4,video/mpeg,video/quicktime,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet}")
    private String[] allowedTypes;

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
        return uploadFile(file, "general");
    }

    @Override
    public String uploadFile(MultipartFile file, String category) throws IOException {
        // 🎯 第一步：执行完整的安全检查（集成 FileSecurityService）
        try {
            fileSecurityService.performSecurityCheck(file);
            fileSecurityService.validateFileSize(file, maxFileSize);
            fileSecurityService.validateFileMagicNumber(file);
            log.info("文件安全检查全部通过: {}", file.getOriginalFilename());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, e.getMessage());
        }

        // 🎯 第二步：根据业务场景确定分类目录
        String categoryDir = determineCategoryDir(category);

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
     * 根据业务场景确定分类目录
     *
     * @param category 业务场景（avatar/goods/post/message/general）
     * @return 分类目录名称
     */
    private String determineCategoryDir(String category) {
        if (category == null || category.isEmpty()) {
            return "general";
        }

        return switch (category.toLowerCase()) {
            case "avatar" -> "avatars";
            case "goods" -> "goods";
            case "post" -> "posts";
            case "message" -> "messages";
            default -> "general";
        };
    }

    /**
     * 上传头像并生成多尺寸缩略图
     * <p>
     * 自动生成以下尺寸的缩略图：
     * - 原图（自动压缩，质量 85%）
     * - 256x256（中等尺寸，用于个人中心）
     * - 128x128（小尺寸，用于评论列表）
     * - 64x64（超小尺寸，用于消息列表）
     * </p>
     *
     * @param file 上传的头像文件
     * @return 包含所有尺寸图片 URL 的 Map（original, large, medium, small）
     * @throws IOException 上传失败
     */
    @Override
    public java.util.Map<String, String> uploadAvatarWithMultipleSizes(MultipartFile file) throws IOException {
        // 🎯 第一步：执行完整的安全检查
        try {
            fileSecurityService.performSecurityCheck(file);
            fileSecurityService.validateFileSize(file, maxFileSize);
            fileSecurityService.validateFileMagicNumber(file);
            // ✅ 验证图片尺寸（最大 2048x2048）
            fileSecurityService.validateImageDimensions(file, 2048, 2048);
            log.info("文件安全检查全部通过: {}", file.getOriginalFilename());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, e.getMessage());
        }

        // 🎯 第二步：上传原图（自动压缩）
        String originalUrl = uploadFile(file, "avatar");

        // 🎯 第三步：生成多尺寸缩略图
        java.util.Map<String, String> result = new java.util.HashMap<>();
        result.put("original", originalUrl);

        try {
            // 提取文件路径
            String relativePath = originalUrl.replace("/uploads/", "");
            Path originalFile = Paths.get(uploadDir, relativePath);

            // 生成文件名前缀
            String fileName = originalFile.getFileName().toString();
            String extension = FilenameUtils.getExtension(fileName);
            String baseName = FilenameUtils.getBaseName(fileName);

            // 生成 256x256 缩略图（中等尺寸）
            String mediumFileName = baseName + "_256." + extension;
            Path mediumPath = originalFile.getParent().resolve(mediumFileName);
            Thumbnails.of(originalFile.toFile())
                    .size(256, 256)
                    .keepAspectRatio(true)
                    .outputQuality(0.85)
                    .toFile(mediumPath.toFile());
            result.put("medium", originalUrl.replace(fileName, mediumFileName));
            log.info("256x256 缩略图生成成功: {}", mediumPath);

            // 生成 128x128 缩略图（小尺寸）
            String smallFileName = baseName + "_128." + extension;
            Path smallPath = originalFile.getParent().resolve(smallFileName);
            Thumbnails.of(originalFile.toFile())
                    .size(128, 128)
                    .keepAspectRatio(true)
                    .outputQuality(0.85)
                    .toFile(smallPath.toFile());
            result.put("small", originalUrl.replace(fileName, smallFileName));
            log.info("128x128 缩略图生成成功: {}", smallPath);

            // 生成 64x64 缩略图（超小尺寸）
            String tinyFileName = baseName + "_64." + extension;
            Path tinyPath = originalFile.getParent().resolve(tinyFileName);
            Thumbnails.of(originalFile.toFile())
                    .size(64, 64)
                    .keepAspectRatio(true)
                    .outputQuality(0.85)
                    .toFile(tinyPath.toFile());
            result.put("tiny", originalUrl.replace(fileName, tinyFileName));
            log.info("64x64 缩略图生成成功: {}", tinyPath);

        } catch (Exception e) {
            log.warn("缩略图生成失败（但原图上传成功）: {}", e.getMessage());
            // 缩略图生成失败不影响主流程，只记录警告
        }

        return result;
    }

    /**
     * 上传 Base64 编码的图片
     * <p>
     * 支持场景：
     * - 图片裁剪后上传
     * - 剪贴板图片上传
     * - Canvas 绘图上传
     * </p>
     *
     * @param base64Data Base64 编码的图片数据（支持 data:image/png;base64,xxx 格式）
     * @param category 业务场景（avatar/goods/post/message/general）
     * @return 文件访问URL
     * @throws IOException 上传失败
     */
    @Override
    public String uploadBase64Image(String base64Data, String category) throws IOException {
        // 🎯 第一步：参数校验
        if (base64Data == null || base64Data.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "Base64 数据不能为空");
        }

        // 🎯 第二步：解析 Base64 数据（支持 data:image/png;base64,xxx 格式）
        String base64String;
        String mimeType = "image/png"; // 默认 PNG
        String extension = "png";

        if (base64Data.startsWith("data:")) {
            // 格式：data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...
            int commaIndex = base64Data.indexOf(",");
            if (commaIndex == -1) {
                throw new BusinessException(ErrorCode.INVALID_PARAM, "Base64 数据格式错误");
            }

            // 提取 MIME 类型
            String dataHeader = base64Data.substring(0, commaIndex);
            if (dataHeader.contains("image/jpeg") || dataHeader.contains("image/jpg")) {
                mimeType = "image/jpeg";
                extension = "jpg";
            } else if (dataHeader.contains("image/png")) {
                mimeType = "image/png";
                extension = "png";
            } else if (dataHeader.contains("image/gif")) {
                mimeType = "image/gif";
                extension = "gif";
            } else if (dataHeader.contains("image/webp")) {
                mimeType = "image/webp";
                extension = "webp";
            }

            // 提取 Base64 字符串
            base64String = base64Data.substring(commaIndex + 1);
        } else {
            // 纯 Base64 字符串
            base64String = base64Data;
        }

        // 🎯 第三步：解码 Base64 数据
        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(base64String);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "Base64 解码失败：" + e.getMessage());
        }

        // 🎯 第四步：文件大小校验
        if (imageBytes.length > maxFileSize) {
            throw new BusinessException(ErrorCode.INVALID_PARAM,
                    String.format("文件大小超过限制：%d bytes > %d bytes", imageBytes.length, maxFileSize));
        }

        // 🎯 第五步：根据业务场景确定分类目录
        String categoryDir = determineCategoryDir(category);

        // 🎯 第六步：生成唯一文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String uniqueFileName = timestamp + "_" + randomCode + "." + extension;

        // 🎯 第七步：按日期分类创建子目录（格式：category/yyyy/MM/dd）
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadPath = Paths.get(uploadDir, categoryDir, dateDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.debug("创建分类子目录: {}", uploadPath);
        }

        // 🎯 第八步：保存文件
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.write(filePath, imageBytes);
        log.info("Base64 图片上传成功: {}/{}/{} ({})", categoryDir, dateDir, uniqueFileName, mimeType);

        return "/uploads/" + categoryDir + "/" + dateDir + "/" + uniqueFileName;
    }
}
