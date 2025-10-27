package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_feature_flag", indexes = {
        @Index(name = "uk_feature_key", columnList = "feature_key", unique = true)
})
public class FeatureFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "feature_key", nullable = false, unique = true, length = 128)
    private String key;

    @Column(nullable = false)
    private boolean enabled;

    @Column(columnDefinition = "TEXT")
    private String rulesJson;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Instant updatedAt;
}
