package com.campus.marketplace.common.dto.response;

import lombok.*;

/**
 * 申诉类型统计DTO
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppealTypeStatistics {

    /**
     * 用户封禁申诉数量
     */
    private long userBanCount;

    /**
     * 禁言申诉数量
     */
    private long userMuteCount;

    /**
     * 商品删除申诉数量
     */
    private long goodsDeleteCount;

    /**
     * 商品下架申诉数量
     */
    private long goodsOfflineCount;

    /**
     * 帖子删除申诉数量
     */
    private long postDeleteCount;

    /**
     * 回复删除申诉数量
     */
    private long replyDeleteCount;

    /**
     * 订单取消申诉数量
     */
    private long orderCancelCount;

    /**
     * 举报驳回申诉数量
     */
    long reportRejectCount;

    /**
     * 其他申诉数量
     */
    private long otherCount;

    /**
     * 误判申诉数量
     */
    long unjustCount;
    /**
     * 合规申诉数量
     long合规Count;

    /**
     * 获取总数
     * @return
     */
    public long getTotalCount() {
        return userBanCount + userMuteCount + goodsDeleteCount + goodsOfflineCount + 
               postDeleteCount + replyDeleteCount + orderCancelCount + 
               reportRejectCount + otherCount;
    }
}
