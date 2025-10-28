package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 合规白名单实体。
 */
@Entity
@Table(name = "t_compliance_whitelist", uniqueConstraints = {
        @UniqueConstraint(name = "uk_comp_white", columnNames = {"type", "target_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceWhitelist {

    /**
     * 主键 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 白名单类型（USER/POST/GOODS）
     */
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    /**
     * 白名单目标 ID
     */
    @Column(name = "target_id", nullable = false)
    private Long targetId;
}
