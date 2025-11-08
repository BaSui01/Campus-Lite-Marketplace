package com.campus.marketplace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 登录设备实体
 *
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Entity
@Table(name = "login_devices", indexes = {
    @Index(name = "idx_login_device_user_id", columnList = "user_id"),
    @Index(name = "idx_login_device_last_active_at", columnList = "last_active_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 设备名称（如：Windows 11 - Chrome）
     */
    @Column(name = "device_name", nullable = false, length = 200)
    private String deviceName;

    /**
     * 设备类型（mobile/desktop/tablet）
     */
    @Column(name = "device_type", nullable = false, length = 20)
    private String deviceType;

    /**
     * 操作系统
     */
    @Column(name = "os", length = 100)
    private String os;

    /**
     * 浏览器
     */
    @Column(name = "browser", length = 100)
    private String browser;

    /**
     * IP 地址
     */
    @Column(name = "ip", length = 50)
    private String ip;

    /**
     * 地理位置
     */
    @Column(name = "location", length = 200)
    private String location;

    /**
     * User-Agent
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 最后活跃时间
     */
    @Column(name = "last_active_at", nullable = false)
    private LocalDateTime lastActiveAt;

    /**
     * 是否当前设备
     */
    @Builder.Default
    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = false;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
