package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.DisputeDTO;
import com.campus.marketplace.common.dto.DisputeDetailDTO;
import com.campus.marketplace.common.dto.request.CreateDisputeRequest;
import com.campus.marketplace.common.enums.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 纠纷核心业务服务接口
 *
 * 负责纠纷的创建、查询、状态变更等核心功能
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
public interface DisputeService {

    /**
     * 提交纠纷
     *
     * @param request 纠纷创建请求
     * @param userId 发起人ID
     * @return 纠纷ID
     * @throws com.campus.marketplace.common.exception.BusinessException 订单已有纠纷、用户非订单参与方等
     */
    Long submitDispute(CreateDisputeRequest request, Long userId);

    /**
     * 查询用户纠纷列表
     *
     * @param userId 用户ID
     * @param status 纠纷状态（可选，null表示查询全部）
     * @param pageable 分页参数
     * @return 纠纷列表（分页）
     */
    Page<DisputeDTO> getUserDisputes(Long userId, DisputeStatus status, Pageable pageable);

    /**
     * 查询纠纷详情
     *
     * @param disputeId 纠纷ID
     * @return 纠纷详情（包含证据、协商记录等）
     * @throws com.campus.marketplace.common.exception.BusinessException 纠纷不存在
     */
    DisputeDetailDTO getDisputeDetail(Long disputeId);

    /**
     * 升级纠纷为仲裁状态
     *
     * 将纠纷从协商中/待仲裁状态升级为待仲裁状态，设置仲裁截止时间
     *
     * @param disputeId 纠纷ID
     * @return 是否成功
     * @throws com.campus.marketplace.common.exception.BusinessException 纠纷不存在、状态不允许升级等
     */
    boolean escalateToArbitration(Long disputeId);

    /**
     * 关闭纠纷
     *
     * 将纠纷状态设置为已关闭，记录关闭原因和时间
     *
     * @param disputeId 纠纷ID
     * @param closeReason 关闭原因
     * @return 是否成功
     * @throws com.campus.marketplace.common.exception.BusinessException 纠纷不存在
     */
    boolean closeDispute(Long disputeId, String closeReason);

    /**
     * 标记协商期到期的纠纷，自动升级为待仲裁
     *
     * 定时任务调用，查找协商截止时间已过的纠纷，自动升级为待仲裁状态
     *
     * @return 升级数量
     */
    int markExpiredNegotiations();

    /**
     * 标记仲裁期到期的纠纷，自动关闭
     *
     * 定时任务调用，查找仲裁截止时间已过且未处理的纠纷，自动关闭
     *
     * @return 关闭数量
     */
    int markExpiredArbitrations();
}
