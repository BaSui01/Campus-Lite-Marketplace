package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.BannerStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 轮播图实体
 * 
 * 用于首页轮播图管理
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Entity
@Table(name = "t_banner", indexes = {
    @Index(name = "idx_banner_status", columnList = "status"),
    @Index(name = "idx_banner_sort_order", columnList = "sort_order"),
    @Index(name = "idx_banner_start_time", columnList = "start_time"),
    @Index(name = "idx_banner_end_time", columnList = "end_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banner extends BaseEntity {

    /**
     * 轮播图标题
     */
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    /**
     * 轮播图描述
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 图片 URL
     */
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    /**
     * 跳转链接（可选）
     */
    @Column(name = "link_url", length = 500)
    private String linkUrl;

    /**
     * 排序顺序（数字越小越靠前）
     */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 状态（启用/禁用）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BannerStatus status = BannerStatus.ENABLED;

    /**
     * 开始时间（可选，用于定时上线）
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间（可选，用于定时下线）
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 点击次数
     */
    @Column(name = "click_count", nullable = false)
    @Builder.Default
    private Integer clickCount = 0;

    /**
     * 展示次数
     */
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    /**
     * 增加点击次数
     */
    public void incrementClickCount() {
        this.clickCount++;
    }

    /**
     * 增加展示次数
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 检查是否在有效期内
     */
    public boolean isInValidPeriod() {
        LocalDateTime now = LocalDateTime.now();
        
        // 如果没有设置时间限制，则始终有效
        if (startTime == null && endTime == null) {
            return true;
        }
        
        // 检查开始时间
        if (startTime != null && now.isBefore(startTime)) {
            return false;
        }
        
        // 检查结束时间
        if (endTime != null && now.isAfter(endTime)) {
            return false;
        }
        
        return true;
    }
}
