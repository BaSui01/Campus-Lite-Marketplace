package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.CampusStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

/**
 * 校区实体，记录编码、名称与状态。
 *
 * @author BaSui
 * @date 2025-10-28
 */
@Entity
@Table(name = "t_campus", indexes = {
        @Index(name = "idx_campus_code", columnList = "code", unique = true),
        @Index(name = "idx_campus_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class Campus extends BaseEntity {

    /**
     * 校区编码（唯一）
     */
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    /**
     * 校区名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 校区状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CampusStatus status = CampusStatus.ACTIVE;
}
