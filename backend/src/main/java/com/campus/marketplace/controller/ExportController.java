package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.ExportJob;
import com.campus.marketplace.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Export Controller
 *
 * @author BaSui
 * @date 2025-10-29
 */
// /http://localhost:8211/exports/{id}/cancel
@RestController
@RequestMapping("/exports")
@RequiredArgsConstructor
@Tag(name = "导出管理", description = "数据导出任务申请、查询与下载")
public class ExportController {

    private final ExportService exportService;

    @PostMapping
    @Operation(summary = "申请导出", description = "发起导出任务，系统异步生成文件")
    public ApiResponse<Long> request(
            @Parameter(description = "导出类型", example = "orders") @RequestParam("type") String type,
            @Parameter(description = "导出参数(JSON字符串)", example = """
                    {"dateFrom":"2025-01-01","dateTo":"2025-01-31"}
                    """)
            @RequestParam(value = "params", required = false) String paramsJson) {
        return ApiResponse.success(exportService.requestExport(type, paramsJson));
    }

    @GetMapping
    @Operation(summary = "我的导出任务", description = "查询当前用户的导出任务列表")
    public ApiResponse<List<ExportJob>> list() {
        return ApiResponse.success(exportService.listMyJobs());
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消导出任务")
    public ApiResponse<Void> cancel(@Parameter(description = "任务ID", example = "123") @PathVariable("id") Long id) {
        exportService.cancel(id);
        return ApiResponse.success();
    }

    @GetMapping("/download/{token}")
    @Operation(summary = "下载导出文件")
    public void download(@Parameter(description = "下载令牌", example = "DL-20251027-abcdef") @PathVariable("token") String token, HttpServletResponse response) throws Exception {
        byte[] bytes = exportService.download(token);
        String fileName = URLEncoder.encode("export.csv", StandardCharsets.UTF_8);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.getOutputStream().write(bytes);
    }
}
