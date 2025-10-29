package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.ExportJob;

import java.util.List;
/**
 * Export Service
 *
 * @author BaSui
 * @date 2025-10-29
 */


public interface ExportService {
    Long requestExport(String type, String paramsJson);
    List<ExportJob> listMyJobs();
    void cancel(Long jobId);
    byte[] download(String token);
}
