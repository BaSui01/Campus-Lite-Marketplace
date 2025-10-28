package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** USER/POST/GOODS **/
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "target_id", nullable = false)
    private Long targetId;
}
