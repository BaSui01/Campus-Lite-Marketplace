package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.BatchReviewRequest;
import com.campus.marketplace.common.dto.request.CreateAppealRequest;
import com.campus.marketplace.common.dto.request.ReviewRequest;
import com.campus.marketplace.common.dto.response.AppealStatistics;
import com.campus.marketplace.common.dto.response.AppealDetailResponse;
import com.campus.marketplace.common.dto.response.BatchReviewResult;
import com.campus.marketplace.common.dto.response.BatchError;
import com.campus.marketplace.common.dto.response.MaterialUploadResponse;
import com.campus.marketplace.common.entity.Appeal;
import com.campus.marketplace.common.entity.AppealMaterial;
import com.campus.marketplace.common.enums.AppealStatus;
import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.service.AppealService;
import com.campus.marketplace.service.AppealMaterialService;
import com.campus.marketplace.service.AuditLogService;
import com.campus.marketplace.repository.AppealRepository;
import com.campus.marketplace.event.AppealCreatedEvent;
import com.campus.marketplace.event.AppealStatusChangedEvent;
import com.campus.marketplace.event.AppealHandledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 申诉服务实现类 - TDD驱动开发
 * 简化版专注于核心功能
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppealServiceImpl implements AppealService {

    private final AppealRepository appealRepository;
    private final AuditLogService auditLogService;
    private final AppealMaterialService appealMaterialService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Long submitAppeal(CreateAppealRequest request) {
        // 验证申诉资格 - 避免重复申诉
        if (appealRepository.existsByUserIdAndTargetId(request.getUserId(), request.getTargetId())) {
            log.warn("用户{}对目标{}已存在申诉，不允许重复提交", request.getUserId(), request.getTargetId());
            return null; // 返回null表示失败
        }
        
        // 创建申诉
        Appeal appeal = new Appeal();
        appeal.setUserId(request.getUserId());
        appeal.setTargetType(request.getTargetType());
        appeal.setTargetId(request.getTargetId());
        appeal.setAppealType(request.getAppealType());
        appeal.setReason(request.getReason());
        if (request.getAttachments() != null) {
            appeal.setAttachments(String.join(",", request.getAttachments()));
        }
        appeal.setStatus(AppealStatus.PENDING);
        
        // 保存申诉
        Appeal savedAppeal = appealRepository.save(appeal);
        
        // 记录审计日志
        if (auditLogService != null) {
            auditLogService.logEntityChange(
                request.getUserId(), 
                null, 
                AuditActionType.USER_APPEAL, 
                "Appeal",
                savedAppeal.getId(), 
                null, 
                savedAppeal
            );
        }
        
        log.info("用户{}提交申诉成功，申诉ID: {}, 目标类型: {}, 目标ID: {}", 
                request.getUserId(), savedAppeal.getId(), request.getTargetType(), request.getTargetId());
        
        // 发布申诉创建事件
        eventPublisher.publishEvent(new AppealCreatedEvent(this, savedAppeal));
        log.debug("发布申诉创建事件: appealId={}", savedAppeal.getId());
        
        return savedAppeal.getId();
    }

    @Override
    @Transactional
    public BatchReviewResult batchReviewAppeals(BatchReviewRequest request) {
        BatchReviewResult result = new BatchReviewResult();
        result.setBatchId(UUID.randomUUID().toString());
        result.setTotalCount(request.getAppealIds().size());
        long startTime = System.currentTimeMillis();
        
        for (Long appealId : request.getAppealIds()) {
            try {
                ReviewRequest reviewRequest = ReviewRequest.builder()
                    .appealId(appealId)
                    .status(request.getStatus())
                    .reviewComment(request.getReviewComment())
                    .reviewerId(request.getReviewerId())
                    .reviewerName(request.getReviewerName())
                    .build();
                    
                Appeal reviewedAppeal = reviewAppeal(reviewRequest);
                if (reviewedAppeal != null) {
                    result.getSuccessIds().add(appealId);
                } else {
                    result.getErrors().add(BatchError.builder()
                        .appealId(appealId)
                        .errorCode("REVIEW_FAILED")
                        .errorMessage("审核失败")
                        .build());
                }
            } catch (Exception e) {
                result.getErrors().add(BatchError.builder()
                    .appealId(appealId)
                    .errorCode("EXCEPTION")
                    .errorMessage(e.getMessage())
                    .details(e.toString())
                    .build());
            }
        }
        
        result.setSuccessCount(result.getSuccessIds().size());
        result.setFailureCount(result.getErrors().size());
        result.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        
        return result;
    }

    @Override
    public Page<Appeal> getUserAppeals(Long userId, Pageable pageable) {
        return appealRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional
    public Appeal reviewAppeal(ReviewRequest request) {
        return appealRepository.findById(request.getAppealId())
            .map(appeal -> {
                // 检查申诉状态
                if (!appeal.canReview()) {
                    log.warn("申诉{}当前状态{}不可审核", request.getAppealId(), appeal.getStatus());
                    return null;
                }
                
                // 保存旧状态，用于事件发布
                AppealStatus oldStatus = appeal.getStatus();
                
                // 更新申诉信息
                appeal.setStatus(request.getStatus());
                appeal.setReviewComment(request.getReviewComment());
                appeal.setReviewerId(request.getReviewerId());
                appeal.setReviewerName(request.getReviewerName());
                appeal.setReviewedAt(LocalDateTime.now());
                
                // 保存更新
                Appeal updatedAppeal = appealRepository.save(appeal);
                
                // 记录审计日志
                if (auditLogService != null) {
                    auditLogService.logEntityChange(
                        request.getReviewerId(),
                        null,
                        request.getStatus() == AppealStatus.APPROVED ? AuditActionType.APPEAL_APPROVE : AuditActionType.APPEAL_REJECT,
                        "Appeal",
                        request.getAppealId(),
                        appeal,
                        updatedAppeal
                    );
                }
                
                log.info("申诉{}审核完成，状态: {}, 审核人: {}", 
                        request.getAppealId(), request.getStatus(), request.getReviewerName());
                
                // 发布申诉状态变更事件
                eventPublisher.publishEvent(new AppealStatusChangedEvent(
                    this, updatedAppeal, oldStatus, request.getStatus()));
                log.debug("发布申诉状态变更事件: appealId={}, {} -> {}", 
                    updatedAppeal.getId(), oldStatus, request.getStatus());
                
                // 发布申诉处理完成事件
                eventPublisher.publishEvent(new AppealHandledEvent(
                    this, updatedAppeal, request.getReviewerId(), request.getReviewerName()));
                log.debug("发布申诉处理完成事件: appealId={}, handlerId={}", 
                    updatedAppeal.getId(), request.getReviewerId());
                
                return updatedAppeal;
            })
            .orElse(null);
    }

    @Override
    public boolean validateAppealEligibility(CreateAppealRequest request) {
        return !appealRepository.existsByUserIdAndTargetId(request.getUserId(), request.getTargetId());
    }

    @Override
    @Transactional
    public AppealDetailResponse getAppealDetail(Long appealId) {
        return appealRepository.findById(appealId)
            .map(appeal -> {
                List<AppealMaterial> materials = appealMaterialService != null 
                    ? appealMaterialService.getAppealMaterials(appealId.toString()) 
                    : List.of();
                
                return AppealDetailResponse.fromAppeal(appeal, materials);
            })
            .orElseThrow(() -> new RuntimeException("申诉不存在: " + appealId));
    }

    @Override
    @Transactional
    public MaterialUploadResponse uploadAppealMaterials(String appealId, MultipartFile[] files, Long uploadedBy, String uploadedByName) {
        if (appealMaterialService == null) {
            MaterialUploadResponse response = new MaterialUploadResponse();
            response.setSuccess(false);
            response.setMessage("材料服务不可用");
            return response;
        }
        
        return appealMaterialService.uploadMaterials(appealId, files, uploadedBy, uploadedByName);
    }

    @Override
    public boolean deleteAppealMaterial(Long materialId, Long deletedBy) {
        if (appealMaterialService == null) {
            return false;
        }
        return appealMaterialService.deleteMaterial(materialId, deletedBy);
    }

    @Override
    public List<AppealMaterial> getAppealMaterials(String appealId) {
        if (appealMaterialService == null) {
            return List.of();
        }
        return appealMaterialService.getAppealMaterials(appealId);
    }

    public int markExpiredAppeals() {
        List<Appeal> expiredAppeals = appealRepository.findExpiredAppeals(
            LocalDateTime.now(), 
            List.of(AppealStatus.PENDING, AppealStatus.REVIEWING)
        );
        
        int markedCount = 0;
        for (Appeal appeal : expiredAppeals) {
            appeal.setStatus(AppealStatus.EXPIRED);
            appealRepository.save(appeal);
            markedCount++;
        }
        
        if (markedCount > 0) {
            log.info("标记{}个过期申诉", markedCount);
        }
        
        return markedCount;
    }

    @Override
    @Transactional
    public boolean cancelAppeal(Long appealId) {
        return appealRepository.findById(appealId)
            .map(appeal -> {
                if (appeal.canCancel()) {
                    appeal.setStatus(AppealStatus.CANCELLED);
                    Appeal updatedAppeal = appealRepository.save(appeal);
                    
                    // 记录审计日志
                    if (auditLogService != null) {
                        auditLogService.logEntityChange(
                            appeal.getUserId(),
                            null,
                            AuditActionType.APPEAL_CANCEL,
                            "Appeal",
                            appealId,
                            appeal,
                            updatedAppeal
                        );
                    }
                    
                    log.info("申诉{}已被用户取消", appealId);
                    return true;
                }
                return false;
            })
            .orElse(false);
    }

    @Override
    public AppealStatistics getAppealStatistics() {
        long pendingCount = appealRepository.countByStatusIn(List.of(AppealStatus.PENDING));
        long processingCount = appealRepository.countByStatusIn(List.of(AppealStatus.REVIEWING));
        long completedCount = appealRepository.countByStatusIn(List.of(AppealStatus.APPROVED, AppealStatus.REJECTED));
        
        AppealStatistics statistics = new AppealStatistics();
        statistics.setPendingCount((int) pendingCount);
        statistics.setReviewingCount((int) processingCount);
        statistics.setCompletedCount((int) completedCount);
        statistics.setTotalCount((int) (pendingCount + processingCount + completedCount));
        
        return statistics;
    }
}
