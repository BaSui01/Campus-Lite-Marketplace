package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.context.CampusContextHolder;
import com.campus.marketplace.common.entity.ExportJob;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.ExportJobRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.service.ExportService;
import com.campus.marketplace.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService, InitializingBean {

    private final ExportJobRepository jobRepo;
    private final GoodsRepository goodsRepository;
    private final OrderRepository orderRepository;
    private final TaskService taskService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final long MAX_ROWS = 200_000; // 阈值限制
    private static final long DOWNLOAD_TTL_MINUTES = 60;

    @Override
    public void afterPropertiesSet() {
        taskService.register("export.run", "执行导出任务", params -> runExport(Long.parseLong(params)));
        taskService.register("export.cleanup", "清理过期导出文件", params -> cleanupExpired());
    }

    @Override
    public Long requestExport(String type, String paramsJson) {
        ensureAdmin();
        Long campusId = CampusContextHolder.getCampusId();
        String user = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUsername() : "system";

        ExportJob job = ExportJob.builder()
                .type(type)
                .paramsJson(paramsJson)
                .status("PENDING")
                .requestedBy(user)
                .campusId(campusId)
                .createdAt(Instant.now())
                .build();
        job = jobRepo.save(job);
        taskService.trigger("export.run", String.valueOf(job.getId()));
        return job.getId();
    }

    @Override
    public List<ExportJob> listMyJobs() {
        // 简化：返回所有，实际应按当前用户过滤
        return jobRepo.findAll();
    }

    @Override
    public void cancel(Long jobId) {
        ExportJob job = jobRepo.findById(jobId).orElseThrow(() -> new IllegalArgumentException("job not found"));
        if (!Objects.equals(job.getRequestedBy(), SecurityUtil.getCurrentUsername()) && !SecurityUtil.hasRole("ADMIN")) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (Objects.equals(job.getStatus(), "SUCCESS") || Objects.equals(job.getStatus(), "FAILED")) {
            return;
        }
        job.setStatus("CANCELLED");
        job.setCompletedAt(Instant.now());
        jobRepo.save(job);
    }

    @Override
    public byte[] download(String token) {
        ExportJob job = jobRepo.findByDownloadToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (job.getExpireAt() == null || job.getExpireAt().isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "下载链接已过期");
        }
        try {
            return FileUtils.readFileToByteArray(new File(job.getFilePath()));
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "文件读取失败");
        }
    }

    private void runExport(Long jobId) throws Exception {
        ExportJob job = jobRepo.findById(jobId).orElseThrow();
        if (!"PENDING".equals(job.getStatus())) return;
        job.setStatus("RUNNING");
        job.setStartedAt(Instant.now());
        jobRepo.save(job);

        File temp = File.createTempFile("export-" + job.getType() + "-", ".csv");
        long rowCount = 0;
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(temp), StandardCharsets.UTF_8))) {
            switch (job.getType()) {
                case "GOODS" -> rowCount = exportGoods(pw, job);
                case "ORDERS" -> rowCount = exportOrders(pw, job);
                default -> throw new IllegalArgumentException("unsupported type");
            }
        } catch (Exception e) {
            job.setStatus("FAILED");
            job.setError(e.getMessage());
            job.setCompletedAt(Instant.now());
            jobRepo.save(job);
            throw e;
        }

        job.setFilePath(temp.getAbsolutePath());
        job.setFileSize(temp.length());
        job.setStatus("SUCCESS");
        job.setCompletedAt(Instant.now());
        job.setDownloadToken(UUID.randomUUID().toString().replace("-", ""));
        job.setExpireAt(Instant.now().plusSeconds(DOWNLOAD_TTL_MINUTES * 60));
        jobRepo.save(job);
        log.info("导出完成: job={}, rows={}, path={}", jobId, rowCount, temp.getAbsolutePath());
    }

    private long exportGoods(PrintWriter pw, ExportJob job) {
        pw.println("id,title,price,status,createdAt");
        int page = 0;
        long total = 0;
        while (true) {
            if (isCancelled(job.getId())) break;
            Page<Goods> p = goodsRepository.findAll(PageRequest.of(page, 1000));
            if (p.isEmpty()) break;
            for (Goods g : p.getContent()) {
                pw.printf(Locale.ROOT, "%d,%s,%.2f,%s,%s%n", g.getId(), escape(g.getTitle()), g.getPrice(), g.getStatus(), g.getCreatedAt());
                total++;
                if (total > MAX_ROWS) throw new BusinessException(ErrorCode.OPERATION_FAILED, "超出导出上限");
            }
            page++;
        }
        return total;
    }

    private long exportOrders(PrintWriter pw, ExportJob job) {
        pw.println("id,orderNo,amount,status,createdAt");
        int page = 0;
        long total = 0;
        while (true) {
            if (isCancelled(job.getId())) break;
            Page<Order> p = orderRepository.findAll(PageRequest.of(page, 1000));
            if (p.isEmpty()) break;
            for (Order o : p.getContent()) {
                pw.printf(Locale.ROOT, "%d,%s,%.2f,%s,%s%n", o.getId(), o.getOrderNo(), o.getAmount(), o.getStatus(), o.getCreatedAt());
                total++;
                if (total > MAX_ROWS) throw new BusinessException(ErrorCode.OPERATION_FAILED, "超出导出上限");
            }
            page++;
        }
        return total;
    }

    private boolean isCancelled(Long jobId) {
        return jobRepo.findById(jobId).map(j -> "CANCELLED".equals(j.getStatus())).orElse(true);
    }

    private void cleanupExpired() {
        List<ExportJob> jobs = jobRepo.findAll();
        Instant now = Instant.now();
        for (ExportJob j : jobs) {
            if (j.getExpireAt() != null && j.getFilePath() != null && j.getExpireAt().isBefore(now)) {
                try {
                    File f = new File(j.getFilePath());
                    if (f.exists()) {
                        if (!f.delete()) {
                            log.warn("删除过期导出文件失败: {}", f.getAbsolutePath());
                        }
                    }
                } catch (Exception e) {
                    log.warn("清理导出文件失败: {}", e.getMessage());
                }
            }
        }
    }

    private void ensureAdmin() {
        if (!SecurityUtil.hasRole("ADMIN")) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private static String escape(String v) {
        if (v == null) return "";
        String s = v.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\n") || s.contains("\r")) {
            return '"' + s + '"';
        }
        return s;
    }
}
