package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.NegotiationMessageDTO;
import com.campus.marketplace.common.dto.request.ProposeDisputeRequest;
import com.campus.marketplace.common.dto.request.RespondProposalRequest;
import com.campus.marketplace.common.dto.request.SendNegotiationRequest;

import java.util.List;
import java.util.Optional;

/**
 * 纠纷协商服务接口
 *
 * 负责买卖双方的协商沟通、解决方案提议和响应
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
public interface DisputeNegotiationService {

    /**
     * 发送文字消息
     *
     * 用于买卖双方在纠纷中进行沟通交流
     *
     * @param request 消息请求
     * @param senderId 发送者ID
     * @return 消息ID
     * @throws com.campus.marketplace.common.exception.BusinessException 纠纷不存在、用户非纠纷参与方等
     */
    Long sendTextMessage(SendNegotiationRequest request, Long senderId);

    /**
     * 提出解决方案
     *
     * 任何一方都可以提出解决方案，但同一时间只能有一个待响应的方案
     *
     * @param request 方案请求
     * @param proposerId 提议人ID
     * @return 方案ID
     * @throws com.campus.marketplace.common.exception.BusinessException 已有待响应方案、用户非纠纷参与方等
     */
    Long proposeResolution(ProposeDisputeRequest request, Long proposerId);

    /**
     * 响应解决方案
     *
     * 对方可以接受或拒绝方案，接受后纠纷状态变为已解决
     *
     * @param request 响应请求
     * @param responderId 响应人ID
     * @return 是否成功
     * @throws com.campus.marketplace.common.exception.BusinessException 方案不存在、方案已响应、用户无权限响应等
     */
    boolean respondToProposal(RespondProposalRequest request, Long responderId);

    /**
     * 查询纠纷的协商历史
     *
     * 返回所有消息和方案，按时间升序排列
     *
     * @param disputeId 纠纷ID
     * @return 消息列表
     */
    List<NegotiationMessageDTO> getNegotiationHistory(Long disputeId);

    /**
     * 查询纠纷的待响应方案
     *
     * @param disputeId 纠纷ID
     * @return 待响应方案（可能为空）
     */
    Optional<NegotiationMessageDTO> getPendingProposal(Long disputeId);

    /**
     * 查询纠纷的已接受方案
     *
     * @param disputeId 纠纷ID
     * @return 已接受方案（可能为空）
     */
    Optional<NegotiationMessageDTO> getAcceptedProposal(Long disputeId);
}
