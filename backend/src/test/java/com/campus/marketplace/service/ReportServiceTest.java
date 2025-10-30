package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateReportRequest;
import com.campus.marketplace.common.entity.Report;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.ReportStatus;
import com.campus.marketplace.common.enums.ReportType;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.ReportRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("举报服务测试")
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private User testUser;
    private CreateReportRequest validRequest;

    @BeforeEach
    void setUp() {
        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .status(UserStatus.ACTIVE)
                .build();
        validRequest = new CreateReportRequest(ReportType.GOODS, 100L, "该商品涉嫌欺诈");
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    @Test
    @DisplayName("举报成功")
    void createReport_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(reportRepository.countByReporterIdAndCreatedAtAfter(eq(1L), any(LocalDateTime.class))).thenReturn(0L);
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(200L);
            return report;
        });

        Long reportId = reportService.createReport(validRequest);

        assertThat(reportId).isNotNull().isEqualTo(200L);
        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(reportCaptor.capture());
        Report savedReport = reportCaptor.getValue();
        assertThat(savedReport.getReporterId()).isEqualTo(1L);
        assertThat(savedReport.getTargetType()).isEqualTo(ReportType.GOODS);
        assertThat(savedReport.getTargetId()).isEqualTo(100L);
        assertThat(savedReport.getReason()).isEqualTo("该商品涉嫌欺诈");
        assertThat(savedReport.getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    @DisplayName("用户不存在")
    void createReport_UserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.createReport(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    @DisplayName("超过每日举报限制")
    void createReport_ExceedsDailyLimit() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(reportRepository.countByReporterIdAndCreatedAtAfter(eq(1L), any(LocalDateTime.class))).thenReturn(10L);

        assertThatThrownBy(() -> reportService.createReport(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PARAM);

        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    @DisplayName("处理举报成功")
    void handleReport_Success() {
        Report report = Report.builder()
                .reporterId(1L)
                .targetType(ReportType.GOODS)
                .targetId(100L)
                .reason("测试原因")
                .status(ReportStatus.PENDING)
                .build();
        report.setId(200L);

        when(reportRepository.findById(200L)).thenReturn(Optional.of(report));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        reportService.handleReport(200L, true, "确认违规");

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(reportCaptor.capture());
        Report handled = reportCaptor.getValue();
        assertThat(handled.getStatus()).isEqualTo(ReportStatus.HANDLED);
        assertThat(handled.getHandlerId()).isEqualTo(1L);
        assertThat(handled.getHandleResult()).isEqualTo("确认违规");
    }

    @Test
    @DisplayName("举报不存在")
    void handleReport_NotFound() {
        when(reportRepository.findById(200L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.handleReport(200L, true, "test"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PARAM);
    }

    @Test
    @DisplayName("举报已处理")
    void handleReport_AlreadyHandled() {
        Report report = Report.builder()
                .reporterId(1L)
                .targetType(ReportType.GOODS)
                .targetId(100L)
                .reason("测试原因")
                .status(ReportStatus.HANDLED)
                .build();
        report.setId(200L);

        when(reportRepository.findById(200L)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> reportService.handleReport(200L, true, "test"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PARAM);
    }
}
