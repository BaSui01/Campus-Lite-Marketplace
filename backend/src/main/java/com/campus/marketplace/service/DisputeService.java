package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.DisputeDTO;
import com.campus.marketplace.common.dto.DisputeDetailDTO;
import com.campus.marketplace.common.dto.request.CreateDisputeRequest;
import com.campus.marketplace.common.enums.DisputeStatus;
import com.campus.marketplace.common.enums.DisputeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

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
     * 查询仲裁员处理的纠纷列表
     *
     * @param arbitratorId 仲裁员ID
     * @param status 纠纷状态（可选，null表示查询全部）
     * @param pageable 分页参数
     * @return 仲裁员的纠纷列表（分页）
     */
    Page<DisputeDTO> getArbitratorDisputes(Long arbitratorId, DisputeStatus status, Pageable pageable);

    /**
     * 多条件搜索纠纷列表（统一筛选架构）
     *
     * @param filterRequest 筛选参数
     * @return 纠纷列表（分页）
     */
    Page<DisputeDTO> searchDisputes(com.campus.marketplace.common.dto.request.DisputeFilterRequest filterRequest);

    /**
     * 多条件搜索纠纷列表（传统方式 - 保留向后兼容）
     *
     * @param keyword 搜索关键字（纠纷编号、订单号）
     * @param disputeType 纠纷类型
     * @param status 纠纷状态
     * @param arbitratorId 仲裁员ID
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @param minAmount 最小金额
     * @param maxAmount 最大金额
     * @param pageable 分页参数
     * @return 纠纷列表（分页）
     * @deprecated 建议使用 {@link #searchDisputes(com.campus.marketplace.common.dto.request.DisputeFilterRequest)}
     */
    @Deprecated
    Page<DisputeDTO> searchDisputes(
            String keyword,
            DisputeType disputeType,
            DisputeStatus status,
            Long arbitratorId,
            String startDate,
            String endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable
    );

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
     * 获取仲裁员列表
     *
     * @return 所有具有ADMIN角色的用户列表
     */
    java.util.List<com.campus.marketplace.common.entity.User> listArbitrators();

    /**
     * 删除纠纷（软删除）
     *
     * @param disputeId 纠纷ID
     * @throws com.campus.marketplace.common.exception.BusinessException 纠纷不存在
     */
    void deleteDispute(Long disputeId);

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
