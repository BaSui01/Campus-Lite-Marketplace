package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价详情响应DTO（聚合）
 *
 * Spec #7：评价完整信息，包含媒体、回复、点赞、标签、情感等
 *
 * @author BaSui 😎 - 一次请求，获取评价全貌！
 * @since 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailDTO {

    // ==================== 基础评价信息 ====================

    /**
     * 评价ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 买家ID
     */
    private Long buyerId;

    /**
     * 买家用户名
     */
    private String buyerUsername;

    /**
     * 买家头像
     */
    private String buyerAvatar;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 综合评分（1-5星）
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价状态
     */
    private ReviewStatus status;

    /**
     * 是否匿名
     */
    private Boolean isAnonymous;

    // ==================== 三维评分 ====================

    /**
     * 物品质量评分（1-5星）
     */
    private Integer qualityScore;

    /**
     * 服务态度评分（1-5星）
     */
    private Integer serviceScore;

    /**
     * 物流速度评分（1-5星）
     */
    private Integer deliveryScore;

    // ==================== 追评功能 ====================

    /**
     * 是否有追评
     */
    private Boolean hasAppendReview;

    /**
     * 追评内容
     */
    private String appendContent;

    /**
     * 追评时间
     */
    private LocalDateTime appendedAt;

    // ==================== NLP分析结果 ====================

    /**
     * 情感倾向（POSITIVE/NEUTRAL/NEGATIVE）
     */
    private String sentimentTendency;

    /**
     * 情感置信度（0.0-1.0）
     */
    private Double sentimentConfidence;

    /**
     * 提取的标签列表
     */
    private List<String> tags;

    // ==================== 互动统计 ====================

    /**
     * 点赞数量
     */
    private Integer likeCount;

    /**
     * 回复数量
     */
    private Integer replyCount;

    /**
     * 当前用户是否点赞
     */
    private Boolean hasLiked;

    // ==================== 关联数据 ====================

    /**
     * 媒体列表（图片/视频）
     */
    private List<ReviewMediaDTO> mediaList;

    /**
     * 回复列表
     */
    private List<ReviewReplyDTO> replyList;

    // ==================== 时间信息 ====================

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 平均评分（三维评分的平均值）
     */
    public Double getAverageScore() {
        if (qualityScore == null || serviceScore == null || deliveryScore == null) {
            return rating != null ? rating.doubleValue() : 0.0;
        }
        return (qualityScore + serviceScore + deliveryScore) / 3.0;
    }

    /**
     * 是否有媒体
     */
    public Boolean getHasMedia() {
        return mediaList != null && !mediaList.isEmpty();
    }

    /**
     * 是否有回复
     */
    public Boolean getHasReply() {
        return replyList != null && !replyList.isEmpty();
    }
}
