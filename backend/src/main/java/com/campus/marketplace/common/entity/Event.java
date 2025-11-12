package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.EventStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * 校园活动实体
 * 
 * @author BaSui 😎
 * @date 2025-11-11
 */
@Entity
@Table(name = "t_event", indexes = {
    @Index(name = "idx_event_status", columnList = "status"),
    @Index(name = "idx_event_start_time", columnList = "start_time"),
    @Index(name = "idx_event_campus_id", columnList = "campus_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class Event extends BaseEntity {

    /**
     * 活动标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 活动描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 活动封面图片URL
     */
    @Column(name = "cover_image", length = 500)
    private String coverImage;

    /**
     * 活动开始时间
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /**
     * 活动地点
     */
    @Column(name = "location", length = 200)
    private String location;

    /**
     * 活动组织者ID
     */
    @Column(name = "organizer_id", nullable = false)
    private Long organizerId;

    /**
     * 校区ID
     */
    @Column(name = "campus_id")
    private Long campusId;

    /**
     * 报名上限（0表示不限制）
     */
    @Column(name = "max_participants")
    @Builder.Default
    private Integer maxParticipants = 0;

    /**
     * 当前报名人数
     */
    @Column(name = "current_participants")
    @Builder.Default
    private Integer currentParticipants = 0;

    /**
     * 活动状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EventStatus status = EventStatus.UPCOMING;

    /**
     * 浏览量
     */
    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;
}
