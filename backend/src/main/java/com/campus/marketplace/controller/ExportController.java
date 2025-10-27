package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.ExportJob;
import com.campus.marketplace.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/exports")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @PostMapping
    public ApiResponse<Long> request(@RequestParam("type") String type,
                                     @RequestParam(value = "params", required = false) String paramsJson) {
        return ApiResponse.success(exportService.requestExport(type, paramsJson));
    }

    @GetMapping
    public ApiResponse<List<ExportJob>> list() {
        return ApiResponse.success(exportService.listMyJobs());
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable("id") Long id) {
        exportService.cancel(id);
        return ApiResponse.success();
    }

    @GetMapping("/download/{token}")
    public void download(@PathVariable("token") String token, HttpServletResponse response) throws Exception {
        byte[] bytes = exportService.download(token);
        String fileName = URLEncoder.encode("export.csv", StandardCharsets.UTF_8);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.getOutputStream().write(bytes);
    }
}
