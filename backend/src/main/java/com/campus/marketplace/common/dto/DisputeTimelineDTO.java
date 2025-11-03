package com.campus.marketplace.common.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 纠纷时间线DTO
 *
 * 记录纠纷全流程的关键事件
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeTimelineDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 纠纷ID
     */
    private Long disputeId;

    /**
     * 纠纷编号
     */
    private String disputeCode;

    /**
     * 时间线事件列表
     */
    private List<TimelineEvent> events;

    /**
     * 时间线事件
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineEvent implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 事件类型
         */
        private EventType type;

        /**
         * 事件标题
         */
        private String title;

        /**
         * 事件描述
         */
        private String description;

        /**
         * 操作人ID
         */
        private Long operatorId;

        /**
         * 操作人昵称
         */
        private String operatorNickname;

        /**
         * 事件时间
         */
        private LocalDateTime timestamp;

        /**
         * 额外数据（JSON格式）
         */
        private String extraData;
    }

    /**
     * 事件类型枚举
     */
    public enum EventType {
        CREATED("纠纷创建"),
        EVIDENCE_UPLOADED("证据上传"),
        NEGOTIATION_MESSAGE("协商消息"),
        PROPOSAL_SENT("方案提出"),
        PROPOSAL_ACCEPTED("方案接受"),
        PROPOSAL_REJECTED("方案拒绝"),
        ESCALATED_TO_ARBITRATION("升级仲裁"),
        ARBITRATOR_ASSIGNED("分配仲裁员"),
        ARBITRATION_COMPLETED("仲裁完成"),
        REFUND_EXECUTED("退款执行"),
        COMPLETED("纠纷完成"),
        CLOSED("纠纷关闭");

        private final String description;

        EventType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
