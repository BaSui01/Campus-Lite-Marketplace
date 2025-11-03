package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.ReviewMedia;
import com.campus.marketplace.common.enums.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 评价媒体服务接口
 *
 * Spec #7：图文视频管理，支持晒单功能
 *
 * @author BaSui 😎 - 晒单必备，图文并茂更有说服力！
 * @since 2025-11-03
 */
public interface ReviewMediaService {

    /**
     * 上传评价媒体（图片/视频）
     *
     * @param reviewId 评价ID
     * @param file 文件
     * @param mediaType 媒体类型
     * @param sortOrder 排序顺序
     * @return 保存的媒体实体
     */
    ReviewMedia uploadMedia(Long reviewId, MultipartFile file, MediaType mediaType, Integer sortOrder);

    /**
     * 批量上传评价媒体
     *
     * @param reviewId 评价ID
     * @param files 文件列表
     * @param mediaType 媒体类型
     * @return 保存的媒体列表
     */
    List<ReviewMedia> uploadMediaBatch(Long reviewId, List<MultipartFile> files, MediaType mediaType);

    /**
     * 获取评价的所有媒体
     *
     * @param reviewId 评价ID
     * @return 媒体列表（按sortOrder排序）
     */
    List<ReviewMedia> getReviewMedia(Long reviewId);

    /**
     * 获取评价的指定类型媒体
     *
     * @param reviewId 评价ID
     * @param mediaType 媒体类型
     * @return 媒体列表
     */
    List<ReviewMedia> getReviewMediaByType(Long reviewId, MediaType mediaType);

    /**
     * 删除评价媒体
     *
     * @param mediaId 媒体ID
     */
    void deleteMedia(Long mediaId);

    /**
     * 删除评价的所有媒体
     *
     * @param reviewId 评价ID
     */
    void deleteAllMediaByReviewId(Long reviewId);

    /**
     * 统计评价的媒体数量
     *
     * @param reviewId 评价ID
     * @return 媒体数量
     */
    long countReviewMedia(Long reviewId);

    /**
     * 统计评价的指定类型媒体数量
     *
     * @param reviewId 评价ID
     * @param mediaType 媒体类型
     * @return 媒体数量
     */
    long countReviewMediaByType(Long reviewId, MediaType mediaType);

    /**
     * 验证媒体数量限制
     * 图片最多10张，视频最多1个
     *
     * @param reviewId 评价ID
     * @param mediaType 媒体类型
     * @param additionalCount 额外添加的数量
     * @return 是否符合限制
     */
    boolean validateMediaLimit(Long reviewId, MediaType mediaType, int additionalCount);
}
