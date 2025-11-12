package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.ReviewMediaDTO;
import com.campus.marketplace.common.entity.ReviewMedia;
import com.campus.marketplace.common.enums.MediaType;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.service.ReviewMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 评价媒体管理Controller
 *
 * Spec #7：图文视频上传、查询、删除API
 *
 * @author BaSui 😎 - 晒单必备，图文视频一键上传！
 * @since 2025-11-03
 */
@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "评价媒体管理", description = "评价图文视频上传、查询、删除接口")
public class ReviewMediaController {

    private final ReviewMediaService reviewMediaService;

    @PostMapping(value = "/{reviewId}/media", consumes = "multipart/form-data")
    @Operation(summary = "上传评价媒体", description = "支持上传图片或视频，图片最多10张，视频最多1个")
    public ApiResponse<ReviewMediaDTO> uploadMedia(
            @Parameter(description = "评价ID", required = true)
            @PathVariable Long reviewId,

            @Parameter(description = "媒体文件", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "媒体类型（IMAGE/VIDEO）", required = true)
            @RequestParam MediaType mediaType,

            @Parameter(description = "排序顺序", required = false)
            @RequestParam(required = false) Integer sortOrder
    ) {
        log.info("上传评价媒体：reviewId={}, mediaType={}, size={}字节", 
                reviewId, mediaType, file.getSize());

        ReviewMedia media = reviewMediaService.uploadMedia(reviewId, file, mediaType, sortOrder);
        ReviewMediaDTO dto = convertToDTO(media);

        return ApiResponse.success(dto);
    }

    @PostMapping(value = "/{reviewId}/media/batch", consumes = "multipart/form-data")
    @Operation(summary = "批量上传评价媒体", description = "一次上传多个文件")
    public ApiResponse<List<ReviewMediaDTO>> uploadMediaBatch(
            @Parameter(description = "评价ID", required = true)
            @PathVariable Long reviewId,

            @Parameter(description = "媒体文件列表", required = true)
            @RequestParam("files") List<MultipartFile> files,

            @Parameter(description = "媒体类型（IMAGE/VIDEO）", required = true)
            @RequestParam MediaType mediaType
    ) {
        log.info("批量上传评价媒体：reviewId={}, mediaType={}, count={}", 
                reviewId, mediaType, files.size());

        List<ReviewMedia> mediaList = reviewMediaService.uploadMediaBatch(reviewId, files, mediaType);
        List<ReviewMediaDTO> dtoList = mediaList.stream()
                .map(this::convertToDTO)
                .toList();

        return ApiResponse.success(dtoList);
    }

    @GetMapping("/{reviewId}/media")
    @Operation(summary = "获取评价的所有媒体", description = "返回图片和视频列表，按sortOrder排序")
    public ApiResponse<List<ReviewMediaDTO>> getReviewMedia(
            @Parameter(description = "评价ID", required = true)
            @PathVariable Long reviewId
    ) {
        List<ReviewMedia> mediaList = reviewMediaService.getReviewMedia(reviewId);
        List<ReviewMediaDTO> dtoList = mediaList.stream()
                .map(this::convertToDTO)
                .toList();

        return ApiResponse.success(dtoList);
    }

    @GetMapping("/{reviewId}/media/{mediaType}")
    @Operation(summary = "获取评价的指定类型媒体", description = "只返回图片或视频")
    public ApiResponse<List<ReviewMediaDTO>> getReviewMediaByType(
            @Parameter(description = "评价ID", required = true)
            @PathVariable Long reviewId,

            @Parameter(description = "媒体类型（IMAGE/VIDEO）", required = true)
            @PathVariable MediaType mediaType
    ) {
        List<ReviewMedia> mediaList = reviewMediaService.getReviewMediaByType(reviewId, mediaType);
        List<ReviewMediaDTO> dtoList = mediaList.stream()
                .map(this::convertToDTO)
                .toList();

        return ApiResponse.success(dtoList);
    }

    @DeleteMapping("/media/{mediaId}")
    @Operation(summary = "删除评价媒体", description = "删除指定的图片或视频")
    public ApiResponse<Void> deleteMedia(
            @Parameter(description = "媒体ID", required = true)
            @PathVariable Long mediaId
    ) {
        log.info("删除评价媒体：mediaId={}", mediaId);
        reviewMediaService.deleteMedia(mediaId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{reviewId}/media")
    @Operation(summary = "删除评价的所有媒体", description = "批量删除评价的所有图片和视频")
    public ApiResponse<Void> deleteAllMediaByReviewId(
            @Parameter(description = "评价ID", required = true)
            @PathVariable Long reviewId
    ) {
        log.info("删除评价{}的所有媒体", reviewId);
        reviewMediaService.deleteAllMediaByReviewId(reviewId);
        return ApiResponse.success();
    }

    /**
     * 实体转DTO
     */
    private ReviewMediaDTO convertToDTO(ReviewMedia media) {
        return ReviewMediaDTO.builder()
                .id(media.getId())
                .reviewId(media.getReviewId())
                .mediaType(media.getMediaType())
                .mediaUrl(media.getMediaUrl())
                .thumbnailUrl(media.getThumbnailUrl())
                .fileSize(media.getFileSize())
                .sortOrder(media.getSortOrder())
                .originalFilename(media.getOriginalFilename())
                .width(media.getWidth())
                .height(media.getHeight())
                .duration(media.getDuration())
                .createdAt(media.getCreatedAt())
                .build();
    }
}
