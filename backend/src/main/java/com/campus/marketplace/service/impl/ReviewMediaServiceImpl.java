package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.ReviewMedia;
import com.campus.marketplace.common.enums.MediaType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.ReviewMediaRepository;
import com.campus.marketplace.service.ReviewMediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 评价媒体服务实现
 *
 * Spec #7：图文视频管理，支持晒单功能
 *
 * @author BaSui 😎 - 文件上传、格式验证、数量限制，一个都不能少！
 * @since 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewMediaServiceImpl implements ReviewMediaService {

    private final ReviewMediaRepository reviewMediaRepository;

    /**
     * 文件上传基础路径（生产环境应使用OSS）
     */
    private static final String UPLOAD_DIR = "uploads/reviews/";

    /**
     * 图片最大数量限制
     */
    private static final int MAX_IMAGE_COUNT = 10;

    /**
     * 视频最大数量限制
     */
    private static final int MAX_VIDEO_COUNT = 1;

    /**
     * 图片文件最大大小（5MB）
     */
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    /**
     * 视频文件最大大小（100MB）
     */
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024;

    /**
     * 支持的图片格式
     */
    private static final List<String> ALLOWED_IMAGE_FORMATS = List.of("jpg", "jpeg", "png", "gif", "webp");

    /**
     * 支持的视频格式
     */
    private static final List<String> ALLOWED_VIDEO_FORMATS = List.of("mp4", "avi", "mov", "flv", "wmv");

    @Override
    @Transactional
    public ReviewMedia uploadMedia(Long reviewId, MultipartFile file, MediaType mediaType, Integer sortOrder) {
        // 验证文件
        validateFile(file, mediaType);

        // 验证数量限制
        if (!validateMediaLimit(reviewId, mediaType, 1)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,
                    String.format("%s数量已达上限", mediaType == MediaType.IMAGE ? "图片" : "视频"));
        }

        // 保存文件
        String fileUrl = saveFile(file, reviewId);

        // 创建媒体记录
        ReviewMedia media = ReviewMedia.builder()
                .reviewId(reviewId)
                .mediaType(mediaType)
                .mediaUrl(fileUrl)
                .fileSize(file.getSize())
                .sortOrder(sortOrder != null ? sortOrder : 1)
                .originalFilename(file.getOriginalFilename())
                .build();

        ReviewMedia savedMedia = reviewMediaRepository.save(media);
        log.info("评价{}上传媒体成功：类型={}，大小={}字节", reviewId, mediaType, file.getSize());

        return savedMedia;
    }

    @Override
    @Transactional
    public List<ReviewMedia> uploadMediaBatch(Long reviewId, List<MultipartFile> files, MediaType mediaType) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        // 验证数量限制
        if (!validateMediaLimit(reviewId, mediaType, files.size())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,
                    String.format("%s数量超过限制（最多%d个）",
                            mediaType == MediaType.IMAGE ? "图片" : "视频",
                            mediaType == MediaType.IMAGE ? MAX_IMAGE_COUNT : MAX_VIDEO_COUNT));
        }

        List<ReviewMedia> mediaList = new ArrayList<>();
        int currentCount = (int) countReviewMediaByType(reviewId, mediaType);

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            ReviewMedia media = uploadMedia(reviewId, file, mediaType, currentCount + i + 1);
            mediaList.add(media);
        }

        log.info("评价{}批量上传{}个{}成功", reviewId, files.size(), mediaType == MediaType.IMAGE ? "图片" : "视频");
        return mediaList;
    }

    @Override
    public List<ReviewMedia> getReviewMedia(Long reviewId) {
        return reviewMediaRepository.findByReviewIdOrderBySortOrderAsc(reviewId);
    }

    @Override
    public List<ReviewMedia> getReviewMediaByType(Long reviewId, MediaType mediaType) {
        return reviewMediaRepository.findByReviewIdAndMediaType(reviewId, mediaType);
    }

    @Override
    @Transactional
    public void deleteMedia(Long mediaId) {
        ReviewMedia media = reviewMediaRepository.findById(mediaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "媒体不存在"));

        // 删除文件（生产环境应删除OSS文件）
        deleteFile(media.getMediaUrl());

        reviewMediaRepository.delete(media);
        log.info("删除媒体成功：ID={}", mediaId);
    }

    @Override
    @Transactional
    public void deleteAllMediaByReviewId(Long reviewId) {
        List<ReviewMedia> mediaList = getReviewMedia(reviewId);

        // 删除所有文件
        mediaList.forEach(media -> deleteFile(media.getMediaUrl()));

        reviewMediaRepository.deleteByReviewId(reviewId);
        log.info("删除评价{}的所有媒体成功", reviewId);
    }

    @Override
    public long countReviewMedia(Long reviewId) {
        return reviewMediaRepository.countByReviewId(reviewId);
    }

    @Override
    public long countReviewMediaByType(Long reviewId, MediaType mediaType) {
        return reviewMediaRepository.countByReviewIdAndMediaType(reviewId, mediaType);
    }

    @Override
    public boolean validateMediaLimit(Long reviewId, MediaType mediaType, int additionalCount) {
        long currentCount = countReviewMediaByType(reviewId, mediaType);
        int maxCount = mediaType == MediaType.IMAGE ? MAX_IMAGE_COUNT : MAX_VIDEO_COUNT;

        return (currentCount + additionalCount) <= maxCount;
    }

    /**
     * 验证文件格式和大小
     */
    private void validateFile(MultipartFile file, MediaType mediaType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件不能为空");
        }

        // 验证文件大小
        long maxSize = mediaType == MediaType.IMAGE ? MAX_IMAGE_SIZE : MAX_VIDEO_SIZE;
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,
                    String.format("文件大小超过限制（最大%dMB）", maxSize / 1024 / 1024));
        }

        // 验证文件格式
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件格式不正确");
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedFormats = mediaType == MediaType.IMAGE ? ALLOWED_IMAGE_FORMATS : ALLOWED_VIDEO_FORMATS;

        if (!allowedFormats.contains(extension)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,
                    String.format("不支持的文件格式（仅支持：%s）", String.join(", ", allowedFormats)));
        }
    }

    /**
     * 保存文件到本地/OSS
     */
    private String saveFile(MultipartFile file, Long reviewId) {
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            String relativePath = reviewId + "/" + filename;

            // 创建目录
            Path uploadPath = Paths.get(UPLOAD_DIR);
            Path reviewPath = uploadPath.resolve(reviewId.toString());
            if (!Files.exists(reviewPath)) {
                Files.createDirectories(reviewPath);
            }

            // 保存文件
            Path filePath = reviewPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.debug("文件保存成功：{}", filePath);
            return UPLOAD_DIR + relativePath;

        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        }
    }

    /**
     * 删除文件
     */
    private void deleteFile(String fileUrl) {
        try {
            Path filePath = Paths.get(fileUrl);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.debug("文件删除成功：{}", filePath);
            }
        } catch (IOException e) {
            log.error("文件删除失败：{}", fileUrl, e);
            // 不抛出异常，允许继续执行
        }
    }
}
