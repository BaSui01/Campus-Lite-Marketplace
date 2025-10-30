package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 浏览日志。
 *
 * 记录用户（或匿名）浏览商品的轨迹。
 *
 * @author BaSui
 * @date 2025-10-28
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_view_log")
public class ViewLog {

    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户名（允许匿名） */
    private String username;

    /** 商品 ID */
    private Long goodsId;

    /** 浏览时间 */
    private LocalDateTime viewedAt;
}
