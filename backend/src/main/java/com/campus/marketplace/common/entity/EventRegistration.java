package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 活动报名记录实体
 * 
 * @author BaSui 😎
 * @date 2025-11-11
 */
@Entity
@Table(name = "t_event_registration", indexes = {
    @Index(name = "idx_registration_event_user", columnList = "event_id,user_id", unique = true),
    @Index(name = "idx_registration_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistration extends BaseEntity {

    /**
     * 活动ID
     */
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 报名备注
     */
    @Column(name = "note", length = 500)
    private String note;

    /**
     * 是否已签到
     */
    @Column(name = "checked_in")
    @Builder.Default
    private Boolean checkedIn = false;
}
