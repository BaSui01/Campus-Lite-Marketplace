package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreatePrivacyRequest;
import com.campus.marketplace.common.dto.response.PrivacyRequestResponse;

import java.util.List;

/**
 * 隐私合规服务接口
 *
 * 提供隐私请求的创建、查询与完成标记
 *
 * @author BaSui
 * @date 2025-10-27
 */
public interface PrivacyService {

    Long createRequest(CreatePrivacyRequest request);

    List<PrivacyRequestResponse> listMyRequests();

    List<PrivacyRequestResponse> listPendingRequests();

    void markCompleted(Long requestId, String resultPath);
}
