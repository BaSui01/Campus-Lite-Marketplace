package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.ExportJob;

import java.util.List;

public interface ExportService {
    Long requestExport(String type, String paramsJson);
    List<ExportJob> listMyJobs();
    void cancel(Long jobId);
    byte[] download(String token);
}
