package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreatePrivacyRequest;
import com.campus.marketplace.common.dto.response.PrivacyRequestResponse;

import java.util.List;

public interface PrivacyService {

    Long createRequest(CreatePrivacyRequest request);

    List<PrivacyRequestResponse> listMyRequests();

    List<PrivacyRequestResponse> listPendingRequests();

    void markCompleted(Long requestId, String resultPath);
}
