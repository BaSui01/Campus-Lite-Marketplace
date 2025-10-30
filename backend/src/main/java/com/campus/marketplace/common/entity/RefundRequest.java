package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.RefundStatus;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_refund_request", indexes = {
        @Index(name = "uk_refund_no", columnList = "refund_no", unique = true),
        @Index(name = "uk_refund_order_no", columnList = "order_no", unique = true),
        @Index(name = "idx_refund_status", columnList = "status")
})
/**
 * Refund Request
 *
 * @author BaSui
 * @date 2025-10-29
 */

public class RefundRequest extends BaseEntity {

    @Column(name = "refund_no", nullable = false, length = 50, unique = true)
    private String refundNo;

    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo;

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Column(name = "reason", length = 255)
    private String reason;

    @Type(JsonBinaryType.class)
    @Column(name = "evidence", columnDefinition = "jsonb")
    private java.util.Map<String, Object> evidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RefundStatus status;

    @Column(name = "channel", length = 20)
    private String channel; // ALIPAY/WECHAT

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    /**
     * 渠道退款重试次数
     */
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * 最后一次错误信息（告警/排查用）
     */
    @Column(name = "last_error", length = 255)
    private String lastError;
}
