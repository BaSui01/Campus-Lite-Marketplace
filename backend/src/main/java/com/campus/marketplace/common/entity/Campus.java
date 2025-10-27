package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.CampusStatus;
import jakarta.persistence.*;
import lombok.*;

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
public class Campus extends BaseEntity {

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CampusStatus status = CampusStatus.ACTIVE;
}
