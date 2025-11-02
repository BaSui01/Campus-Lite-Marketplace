package com.campus.marketplace.service.impl;

import com.campus.marketplace.service.FileSecurityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 文件安全检查服务实现类
 * 
 * 提供文件上传前的安全验证功能
 * 
 * @author BaSui 😎
 * @date 2025-11-03
 */
@Slf4j
@Service
public class FileSecurityServiceImpl implements FileSecurityService {

    // 允许的图片类型
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    // 允许的文档类型
    private static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
        "text/plain"
    );

    // 文件扩展名与MIME类型映射表
    private static final Map<String, String> EXTENSION_MIME_MAP = new HashMap<>();
    
    static {
        // 图片类型映射
        EXTENSION_MIME_MAP.put("jpg", "image/jpeg");
        EXTENSION_MIME_MAP.put("jpeg", "image/jpeg");
        EXTENSION_MIME_MAP.put("png", "image/png");
        EXTENSION_MIME_MAP.put("gif", "image/gif");
        EXTENSION_MIME_MAP.put("webp", "image/webp");
        EXTENSION_MIME_MAP.put("bmp", "image/bmp");
        
        // 文档类型映射
        EXTENSION_MIME_MAP.put("pdf", "application/pdf");
        EXTENSION_MIME_MAP.put("doc", "application/msword");
        EXTENSION_MIME_MAP.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        EXTENSION_MIME_MAP.put("xls", "application/vnd.ms-excel");
        EXTENSION_MIME_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        EXTENSION_MIME_MAP.put("txt", "text/plain");
    }

    // 危险的文件名字符
    private static final Set<String> DANGEROUS_PATTERNS = Set.of(
        "..", "/", "\\", ":", "*", "?", "\"", "<", ">", "|", "\0"
    );

    @Override
    public void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        
        if (contentType == null || contentType.trim().isEmpty()) {
            log.warn("文件类型为空: {}", file.getOriginalFilename());
            throw new IllegalArgumentException("无法识别的文件类型");
        }

        boolean isAllowed = ALLOWED_IMAGE_TYPES.contains(contentType) 
            || ALLOWED_DOCUMENT_TYPES.contains(contentType);

        if (!isAllowed) {
            log.warn("不支持的文件类型: {}, 文件名: {}", contentType, file.getOriginalFilename());
            throw new IllegalArgumentException("不支持的文件类型: " + contentType);
        }

        log.debug("文件类型验证通过: {}", contentType);
    }

    @Override
    public void validateFileSize(MultipartFile file, long maxSize) {
        long fileSize = file.getSize();
        
        if (fileSize > maxSize) {
            log.warn("文件大小超过限制: {} > {}, 文件名: {}", 
                fileSize, maxSize, file.getOriginalFilename());
            throw new IllegalArgumentException(
                String.format("文件大小超过限制: %.2fMB (最大允许: %.2fMB)", 
                    fileSize / 1024.0 / 1024.0, 
                    maxSize / 1024.0 / 1024.0)
            );
        }

        log.debug("文件大小验证通过: {} 字节", fileSize);
    }

    @Override
    public void validateFileName(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        
        if (fileName == null || fileName.trim().isEmpty()) {
            log.warn("文件名为空");
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 检查危险字符
        for (String pattern : DANGEROUS_PATTERNS) {
            if (fileName.contains(pattern)) {
                log.warn("文件名包含非法字符: {}, 文件名: {}", pattern, fileName);
                throw new IllegalArgumentException("文件名包含非法字符: " + pattern);
            }
        }

        // 检查文件名长度
        if (fileName.length() > 255) {
            log.warn("文件名过长: {} 字符", fileName.length());
            throw new IllegalArgumentException("文件名长度不能超过255个字符");
        }

        log.debug("文件名验证通过: {}", fileName);
    }

    @Override
    public void validateNotEmpty(MultipartFile file) {
        if (file.isEmpty() || file.getSize() == 0) {
            log.warn("文件为空: {}", file.getOriginalFilename());
            throw new IllegalArgumentException("文件不能为空");
        }

        log.debug("文件非空验证通过: {} 字节", file.getSize());
    }

    @Override
    public void validateExtensionMatchesMimeType(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();

        if (fileName == null || contentType == null) {
            log.warn("文件名或MIME类型为空");
            throw new IllegalArgumentException("无效的文件信息");
        }

        // 获取文件扩展名
        String extension = getFileExtension(fileName);
        if (extension.isEmpty()) {
            log.warn("文件没有扩展名: {}", fileName);
            throw new IllegalArgumentException("文件必须有扩展名");
        }

        // 查找扩展名对应的MIME类型
        String expectedMimeType = EXTENSION_MIME_MAP.get(extension.toLowerCase());
        if (expectedMimeType == null) {
            log.warn("未知的文件扩展名: {}", extension);
            throw new IllegalArgumentException("不支持的文件扩展名: " + extension);
        }

        // 验证MIME类型是否匹配
        if (!expectedMimeType.equalsIgnoreCase(contentType)) {
            log.warn("文件扩展名与MIME类型不匹配: 扩展名={}, 期望MIME={}, 实际MIME={}", 
                extension, expectedMimeType, contentType);
            throw new IllegalArgumentException(
                "文件扩展名与实际类型不匹配: " + extension + " vs " + contentType
            );
        }

        log.debug("扩展名与MIME类型验证通过: {} -> {}", extension, contentType);
    }

    @Override
    public void performSecurityCheck(MultipartFile file) {
        log.info("开始执行文件安全检查: {}", file.getOriginalFilename());

        // 1. 验证文件非空
        validateNotEmpty(file);

        // 2. 验证文件名安全
        validateFileName(file);

        // 3. 验证文件类型
        validateFileType(file);

        // 4. 验证扩展名与MIME类型匹配
        validateExtensionMatchesMimeType(file);

        log.info("文件安全检查全部通过: {}", file.getOriginalFilename());
    }

    @Override
    public String calculateFileHash(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = file.getBytes();
            byte[] hashBytes = digest.digest(fileBytes);
            
            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = String.format("%02x", b);
                hexString.append(hex);
            }
            
            String hash = hexString.toString();
            log.debug("计算文件哈希成功: {} -> {}", file.getOriginalFilename(), hash);
            return hash;
            
        } catch (Exception e) {
            log.error("计算文件哈希失败: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("计算文件哈希失败", e);
        }
    }

    @Override
    public String scanForVirus(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                return "ERROR";
            }

            // 模拟病毒扫描逻辑
            // 实际生产环境应该集成专业的病毒扫描服务（如ClamAV、VirusTotal等）
            String lowerFileName = fileName.toLowerCase();

            // 检测危险文件类型
            if (lowerFileName.endsWith(".exe") || 
                lowerFileName.endsWith(".bat") || 
                lowerFileName.endsWith(".cmd") ||
                lowerFileName.endsWith(".com") ||
                lowerFileName.endsWith(".scr") ||
                lowerFileName.endsWith(".vbs") ||
                lowerFileName.endsWith(".js") && file.getContentType() != null 
                    && !file.getContentType().startsWith("image/")) {
                log.warn("检测到可执行文件: {}", fileName);
                return "INFECTED";
            }

            // 检测恶意文件名关键词
            if (lowerFileName.contains("virus") || 
                lowerFileName.contains("malware") ||
                lowerFileName.contains("trojan") ||
                lowerFileName.contains("worm")) {
                log.warn("检测到疑似恶意文件名: {}", fileName);
                return "INFECTED";
            }

            // 检查文件内容（简化版）
            byte[] bytes = file.getBytes();
            if (bytes.length > 0) {
                // 检测常见恶意代码特征（这里只是示例）
                String content = new String(bytes, 0, Math.min(bytes.length, 1000));
                if (content.contains("<?php") && content.contains("eval") ||
                    content.contains("<script>") && content.contains("document.cookie")) {
                    log.warn("检测到疑似恶意代码: {}", fileName);
                    return "INFECTED";
                }
            }

            log.info("病毒扫描通过: {}", fileName);
            return "CLEAN";
            
        } catch (Exception e) {
            log.error("病毒扫描失败: {}", file.getOriginalFilename(), e);
            return "ERROR";
        }
    }

    /**
     * 获取文件扩展名
     * 
     * @param fileName 文件名
     * @return 扩展名（不包含点号），如果没有扩展名则返回空字符串
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        
        return "";
    }
}
