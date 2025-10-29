package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.CreateReportRequest;
import com.campus.marketplace.common.dto.response.ReportResponse;
import com.campus.marketplace.common.entity.Report;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.ReportStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.ReportRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Report Service Impl
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    private static final int DAILY_REPORT_LIMIT = 10;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReport(CreateReportRequest request) {
        log.info("创建举报: targetType={}, targetId={}", request.targetType(), request.targetId());

        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        long todayReportCount = reportRepository.countByReporterIdAndCreatedAtAfter(user.getId(), startOfDay);
        
        if (todayReportCount >= DAILY_REPORT_LIMIT) {
            log.warn("用户超过每日举报限制: userId={}, count={}", user.getId(), todayReportCount);
            throw new BusinessException(ErrorCode.INVALID_PARAM, "超过每日举报限制");
        }

        Report report = Report.builder()
                .reporterId(user.getId())
                .targetType(request.targetType())
                .targetId(request.targetId())
                .reason(request.reason())
                .status(ReportStatus.PENDING)
                .build();

        reportRepository.save(report);

        log.info("举报创建成功: reportId={}, reporterId={}", report.getId(), user.getId());

        return report.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleReport(Long id, boolean approved, String handleResult) {
        log.info("处理举报: reportId={}, approved={}", id, approved);

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PARAM, "举报不存在"));

        if (report.isHandled()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "举报已处理");
        }

        String username = SecurityUtil.getCurrentUsername();
        User handler = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        report.setStatus(approved ? ReportStatus.HANDLED : ReportStatus.REJECTED);
        report.setHandlerId(handler.getId());
        report.setHandleResult(handleResult);

        reportRepository.save(report);

        log.info("举报处理完成: reportId={}, status={}, handlerId={}", id, report.getStatus(), handler.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> listPendingReports(int page, int size) {
        log.info("查询待处理举报列表: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Report> reportPage = reportRepository.findByStatus(ReportStatus.PENDING, pageable);

        return reportPage.map(ReportResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> listMyReports(int page, int size) {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        log.info("查询用户举报记录: userId={}, page={}, size={}", user.getId(), page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Report> reportPage = reportRepository.findByReporterId(user.getId(), pageable);

        return reportPage.map(ReportResponse::from);
    }
}
