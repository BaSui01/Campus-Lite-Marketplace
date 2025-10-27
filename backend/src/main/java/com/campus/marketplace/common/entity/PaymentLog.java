package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.PaymentLogType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_payment_log", indexes = {
        @Index(name = "idx_payment_order_no", columnList = "order_no"),
        @Index(name = "idx_payment_trade_no", columnList = "trade_no")
})
public class PaymentLog extends BaseEntity {

    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo;

    @Column(name = "trade_no", length = 64)
    private String tradeNo;

    @Column(name = "channel", length = 20)
    private String channel; // ALIPAY/WECHAT

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 10)
    private PaymentLogType type; // PAY/REFUND

    @Type(JsonBinaryType.class)
    @Column(name = "payload", columnDefinition = "jsonb")
    private java.util.Map<String, Object> payload;

    @Column(name = "success")
    private Boolean success;
}
