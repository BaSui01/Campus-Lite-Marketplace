package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateReportRequest;
import com.campus.marketplace.common.dto.response.ReportResponse;
import org.springframework.data.domain.Page;

/**
 * 举报服务接口
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public interface ReportService {

    /**
     * 创建举报
     * 
     * @param request 举报请求
     * @return 举报 ID
     */
    Long createReport(CreateReportRequest request);

    /**
     * 处理举报（管理员）
     * 
     * @param id 举报 ID
     * @param approved 是否通过（true=通过举报，false=驳回举报）
     * @param handleResult 处理结果
     */
    void handleReport(Long id, boolean approved, String handleResult);

    /**
     * 查询待处理的举报列表（管理员）
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 举报分页结果
     */
    Page<ReportResponse> listPendingReports(int page, int size);

    /**
     * 查询用户的举报记录
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 举报分页结果
     */
    Page<ReportResponse> listMyReports(int page, int size);
}
