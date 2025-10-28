package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.CreatePrivacyRequest;
import com.campus.marketplace.common.dto.response.PrivacyRequestResponse;
import com.campus.marketplace.common.entity.PrivacyRequest;
import com.campus.marketplace.common.enums.PrivacyRequestStatus;
import com.campus.marketplace.common.enums.PrivacyRequestType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.PrivacyRequestRepository;
import com.campus.marketplace.service.PrivacyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 隐私合规服务实现
 *
 * 提供隐私请求的创建、查询与完成标记逻辑
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PrivacyServiceImpl implements PrivacyService {

    private static final Set<PrivacyRequestStatus> ACTIVE_STATUSES = Set.of(
            PrivacyRequestStatus.PENDING,
            PrivacyRequestStatus.PROCESSING
    );

    private final PrivacyRequestRepository privacyRequestRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRequest(CreatePrivacyRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        privacyRequestRepository.findFirstByUserIdAndTypeAndStatusIn(
                        currentUserId,
                        request.type(),
                        ACTIVE_STATUSES.stream().toList()
                )
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCode.PRIVACY_REQUEST_CONFLICT,
                            "已有未完成的" + request.type() + "请求，无法重复申请");
                });

        PrivacyRequest privacyRequest = PrivacyRequest.builder()
                .userId(currentUserId)
                .type(request.type())
                .status(PrivacyRequestStatus.PENDING)
                .reason(request.reason())
                .scheduledAt(calculateScheduleTime(request.type()))
                .build();
        privacyRequestRepository.save(privacyRequest);

        log.info("创建隐私请求成功 requestId={}, userId={}, type={}",
                privacyRequest.getId(), currentUserId, request.type());
        return privacyRequest.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrivacyRequestResponse> listMyRequests() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        return privacyRequestRepository.findByUserId(currentUserId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrivacyRequestResponse> listPendingRequests() {
        return privacyRequestRepository.findByStatus(PrivacyRequestStatus.PENDING).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markCompleted(Long requestId, String resultPath) {
        PrivacyRequest request = privacyRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRIVACY_REQUEST_NOT_FOUND));
        request.setStatus(PrivacyRequestStatus.COMPLETED);
        request.setResultPath(resultPath);
        request.setCompletedAt(LocalDateTime.now());
        privacyRequestRepository.save(request);
        log.info("隐私请求已完成 requestId={}, resultPath={}", requestId, resultPath);
    }

    private LocalDateTime calculateScheduleTime(PrivacyRequestType type) {
        LocalDateTime now = LocalDateTime.now();
        // 导出立即排期，删除延迟 7 天，满足“延迟删除”要求
        return switch (type) {
            case EXPORT -> now;
            case DELETE -> now.plusDays(7);
        };
    }

    private PrivacyRequestResponse toResponse(PrivacyRequest request) {
        return PrivacyRequestResponse.builder()
                .id(request.getId())
                .type(request.getType())
                .status(request.getStatus())
                .reason(request.getReason())
                .resultPath(request.getResultPath())
                .scheduledAt(request.getScheduledAt())
                .completedAt(request.getCompletedAt())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
