package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreatePrivacyRequest;
import com.campus.marketplace.common.dto.response.PrivacyRequestResponse;
import com.campus.marketplace.common.entity.PrivacyRequest;
import com.campus.marketplace.common.enums.PrivacyRequestStatus;
import com.campus.marketplace.common.enums.PrivacyRequestType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.PrivacyRequestRepository;
import com.campus.marketplace.service.impl.PrivacyServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("隐私请求服务实现测试")
class PrivacyServiceImplTest {

    @Mock private PrivacyRequestRepository privacyRequestRepository;

    @InjectMocks
    private PrivacyServiceImpl privacyService;

    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    void setUp() {
        securityUtilMock = Mockito.mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(66L);
    }

    @AfterEach
    void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    @Test
    @DisplayName("创建隐私请求成功并保存排期")
    void createRequest_success() {
        when(privacyRequestRepository.findFirstByUserIdAndTypeAndStatusIn(eq(66L), eq(PrivacyRequestType.EXPORT), any()))
                .thenReturn(Optional.empty());
        when(privacyRequestRepository.save(any(PrivacyRequest.class))).thenAnswer(inv -> {
            PrivacyRequest saved = inv.getArgument(0);
            saved.setId(900L);
            return saved;
        });

        Long id = privacyService.createRequest(new CreatePrivacyRequest(PrivacyRequestType.EXPORT, "下载数据"));

        assertThat(id).isEqualTo(900L);
        verify(privacyRequestRepository).save(argThat(req ->
                req.getStatus() == PrivacyRequestStatus.PENDING && req.getScheduledAt() != null));
    }

    @Test
    @DisplayName("创建隐私请求存在冲突抛异常")
    void createRequest_conflict() {
        PrivacyRequest existing = PrivacyRequest.builder()
                .userId(66L)
                .type(PrivacyRequestType.DELETE)
                .status(PrivacyRequestStatus.PENDING)
                .build();
        existing.setId(1L);
        when(privacyRequestRepository.findFirstByUserIdAndTypeAndStatusIn(eq(66L), eq(PrivacyRequestType.DELETE), any()))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> privacyService.createRequest(new CreatePrivacyRequest(PrivacyRequestType.DELETE, "")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PRIVACY_REQUEST_CONFLICT.getCode());
    }

    @Test
    @DisplayName("查询我的隐私请求返回响应")
    void listMyRequests() {
        PrivacyRequest request = PrivacyRequest.builder()
                .userId(66L)
                .type(PrivacyRequestType.EXPORT)
                .status(PrivacyRequestStatus.PROCESSING)
                .reason("test")
                .build();
        request.setId(2L);
        when(privacyRequestRepository.findByUserId(66L)).thenReturn(List.of(request));

        List<PrivacyRequestResponse> responses = privacyService.listMyRequests();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().type()).isEqualTo(PrivacyRequestType.EXPORT);
    }

    @Test
    @DisplayName("查询待处理请求返回列表")
    void listPendingRequests() {
        PrivacyRequest request = PrivacyRequest.builder()
                .status(PrivacyRequestStatus.PENDING)
                .type(PrivacyRequestType.DELETE)
                .build();
        request.setId(3L);
        when(privacyRequestRepository.findByStatus(PrivacyRequestStatus.PENDING)).thenReturn(List.of(request));

        List<PrivacyRequestResponse> responses = privacyService.listPendingRequests();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().status()).isEqualTo(PrivacyRequestStatus.PENDING);
    }

    @Test
    @DisplayName("标记请求完成写回仓库")
    void markCompleted() {
        PrivacyRequest request = PrivacyRequest.builder()
                .status(PrivacyRequestStatus.PROCESSING)
                .build();
        request.setId(4L);
        when(privacyRequestRepository.findById(4L)).thenReturn(Optional.of(request));

        privacyService.markCompleted(4L, "/tmp/export.zip");

        assertThat(request.getStatus()).isEqualTo(PrivacyRequestStatus.COMPLETED);
        assertThat(request.getResultPath()).isEqualTo("/tmp/export.zip");
        assertThat(request.getCompletedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        verify(privacyRequestRepository).save(request);
    }
}
