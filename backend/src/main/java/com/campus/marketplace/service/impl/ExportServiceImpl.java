package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.context.CampusContextHolder;
import com.campus.marketplace.common.dto.response.CouponStatisticsResponse;
import com.campus.marketplace.common.entity.ExportJob;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.ExportJobRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.service.CouponService;
import com.campus.marketplace.service.ExportService;
import com.campus.marketplace.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Export Service Impl
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService, InitializingBean {

    private final ExportJobRepository jobRepo;
    private final GoodsRepository goodsRepository;
    private final OrderRepository orderRepository;
    private final @org.springframework.context.annotation.Lazy CouponService couponService;
    private final TaskService taskService;

    // 可通过测试反射或配置覆盖
    private long maxRows = 200_000; // 阈值限制
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
        // ✅ 真实实现：根据当前用户和角色返回导出任务列表
        String currentUsername = SecurityUtil.getCurrentUsername();

        // 管理员：返回当前校区的所有任务
        if (SecurityUtil.hasRole("ADMIN")) {
            Long campusId = CampusContextHolder.getCampusId();
            if (campusId != null) {
                log.debug("🔍 管理员查询校区导出任务: campusId={}", campusId);
                return jobRepo.findByCampusIdOrderByCreatedAtDesc(campusId);
            } else {
                // 超级管理员：返回所有任务
                log.debug("🔍 超级管理员查询所有导出任务");
                return jobRepo.findAll();
            }
        }

        // 普通用户：只返回自己的任务
        log.debug("🔍 用户查询自己的导出任务: username={}", currentUsername);
        return jobRepo.findByRequestedByOrderByCreatedAtDesc(currentUsername);
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

        File temp;
        long rowCount = 0;

        // 🎯 BaSui: 根据导出类型和格式选择文件格式（CSV 或 Excel）
        if ("COUPON_STATISTICS".equals(job.getType())) {
            // 解析导出参数，获取格式
            ExportParams params = parseExportParams(job.getParamsJson());
            String format = params.getFormat();

            if ("CSV".equalsIgnoreCase(format)) {
                // CSV 格式导出
                temp = File.createTempFile("export-" + job.getType() + "-", ".csv");
                try {
                    rowCount = exportCouponStatisticsToCSV(temp, job);
                } catch (Exception e) {
                    job.setStatus("FAILED");
                    job.setError(e.getMessage());
                    job.setCompletedAt(Instant.now());
                    jobRepo.save(job);
                    throw e;
                }
            } else {
                // Excel 格式导出（默认）
                temp = File.createTempFile("export-" + job.getType() + "-", ".xlsx");
                try {
                    rowCount = exportCouponStatisticsToExcel(temp, job);
                } catch (Exception e) {
                    job.setStatus("FAILED");
                    job.setError(e.getMessage());
                    job.setCompletedAt(Instant.now());
                    jobRepo.save(job);
                    throw e;
                }
            }
        } else {
            // 其他导出使用 CSV 格式
            temp = File.createTempFile("export-" + job.getType() + "-", ".csv");
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
                if (total > maxRows) throw new BusinessException(ErrorCode.OPERATION_FAILED, "超出导出上限");
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
                if (total > maxRows) throw new BusinessException(ErrorCode.OPERATION_FAILED, "超出导出上限");
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

    /**
     * 导出优惠券统计到 Excel
     *
     * @param file 输出文件
     * @param job 导出任务
     * @return 导出行数
     * @throws Exception 导出异常
     */
    private long exportCouponStatisticsToExcel(File file, ExportJob job) throws Exception {
        log.info("🎯 开始导出优惠券统计到 Excel: jobId={}", job.getId());

        // 🎯 BaSui: 解析导出参数（支持按优惠券ID、日期范围筛选）
        ExportParams params = parseExportParams(job.getParamsJson());

        // 获取优惠券统计数据（支持筛选）
        List<CouponStatisticsResponse> statistics = fetchCouponStatistics(params);

        // 创建 Excel 工作簿
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(file)) {

            // 创建工作表
            Sheet sheet = workbook.createSheet("优惠券统计");

            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "优惠券ID", "优惠券代码", "优惠券名称", "总发行数量", "已领取数量",
                "已使用数量", "领取率", "使用率", "总优惠金额", "平均优惠金额",
                "创建时间", "开始时间", "结束时间", "是否激活"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (CouponStatisticsResponse stat : statistics) {
                if (isCancelled(job.getId())) {
                    log.warn("⚠️ 导出任务被取消: jobId={}", job.getId());
                    break;
                }

                Row row = sheet.createRow(rowNum++);

                // 优惠券ID
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(stat.getCouponId());
                cell0.setCellStyle(dataStyle);

                // 优惠券代码
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(stat.getCode());
                cell1.setCellStyle(dataStyle);

                // 优惠券名称
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(stat.getName());
                cell2.setCellStyle(dataStyle);

                // 总发行数量
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(stat.getTotalCount());
                cell3.setCellStyle(dataStyle);

                // 已领取数量
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(stat.getReceivedCount());
                cell4.setCellStyle(dataStyle);

                // 已使用数量
                Cell cell5 = row.createCell(5);
                cell5.setCellValue(stat.getUsedCount());
                cell5.setCellStyle(dataStyle);

                // 领取率
                Cell cell6 = row.createCell(6);
                cell6.setCellValue(stat.getReceiveRate());
                cell6.setCellStyle(percentStyle);

                // 使用率
                Cell cell7 = row.createCell(7);
                cell7.setCellValue(stat.getUseRate());
                cell7.setCellStyle(percentStyle);

                // 总优惠金额
                Cell cell8 = row.createCell(8);
                cell8.setCellValue(stat.getTotalDiscountAmount().doubleValue());
                cell8.setCellStyle(currencyStyle);

                // 平均优惠金额
                Cell cell9 = row.createCell(9);
                cell9.setCellValue(stat.getAvgDiscountAmount().doubleValue());
                cell9.setCellStyle(currencyStyle);

                // 创建时间
                Cell cell10 = row.createCell(10);
                cell10.setCellValue(stat.getCreatedAt() != null ? stat.getCreatedAt().format(formatter) : "");
                cell10.setCellStyle(dateStyle);

                // 开始时间
                Cell cell11 = row.createCell(11);
                cell11.setCellValue(stat.getStartTime() != null ? stat.getStartTime().format(formatter) : "");
                cell11.setCellStyle(dateStyle);

                // 结束时间
                Cell cell12 = row.createCell(12);
                cell12.setCellValue(stat.getEndTime() != null ? stat.getEndTime().format(formatter) : "");
                cell12.setCellStyle(dateStyle);

                // 是否激活
                Cell cell13 = row.createCell(13);
                cell13.setCellValue(stat.getIsActive() ? "是" : "否");
                cell13.setCellStyle(dataStyle);

                // 检查是否超出导出上限
                if (rowNum > maxRows) {
                    throw new BusinessException(ErrorCode.OPERATION_FAILED, "超出导出上限");
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // 设置最小宽度（防止中文列宽过窄）
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.max(currentWidth, 3000));
            }

            // 写入文件
            workbook.write(fos);

            log.info("✅ 优惠券统计导出完成: jobId={}, rows={}", job.getId(), rowNum - 1);
            return rowNum - 1;
        }
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建百分比样式
     */
    private CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00%"));
        return style;
    }

    /**
     * 创建货币样式
     */
    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("¥#,##0.00"));
        return style;
    }

    /**
     * 创建日期样式
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * 导出优惠券统计到 CSV
     *
     * @param file 输出文件
     * @param job 导出任务
     * @return 导出行数
     * @throws Exception 导出异常
     */
    private long exportCouponStatisticsToCSV(File file, ExportJob job) throws Exception {
        log.info("🎯 开始导出优惠券统计到 CSV: jobId={}", job.getId());

        // 解析导出参数
        ExportParams params = parseExportParams(job.getParamsJson());

        // 获取优惠券统计数据（支持筛选）
        List<CouponStatisticsResponse> statistics = fetchCouponStatistics(params);

        // 写入 CSV 文件
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            // 写入 BOM（解决 Excel 打开 CSV 中文乱码问题）
            pw.write('\ufeff');

            // 写入表头
            pw.println("优惠券ID,优惠券代码,优惠券名称,总发行数量,已领取数量,已使用数量,领取率,使用率,总优惠金额,平均优惠金额,创建时间,开始时间,结束时间,是否激活");

            // 写入数据
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int rowNum = 0;

            for (CouponStatisticsResponse stat : statistics) {
                if (isCancelled(job.getId())) {
                    log.warn("⚠️ 导出任务被取消: jobId={}", job.getId());
                    break;
                }

                pw.printf(Locale.ROOT, "%d,%s,%s,%d,%d,%d,%.2f%%,%.2f%%,%.2f,%.2f,%s,%s,%s,%s%n",
                        stat.getCouponId(),
                        escape(stat.getCode()),
                        escape(stat.getName()),
                        stat.getTotalCount(),
                        stat.getReceivedCount(),
                        stat.getUsedCount(),
                        stat.getReceiveRate() * 100,
                        stat.getUseRate() * 100,
                        stat.getTotalDiscountAmount().doubleValue(),
                        stat.getAvgDiscountAmount().doubleValue(),
                        stat.getCreatedAt() != null ? stat.getCreatedAt().format(formatter) : "",
                        stat.getStartTime() != null ? stat.getStartTime().format(formatter) : "",
                        stat.getEndTime() != null ? stat.getEndTime().format(formatter) : "",
                        stat.getIsActive() ? "是" : "否"
                );

                rowNum++;

                // 检查是否超出导出上限
                if (rowNum > maxRows) {
                    throw new BusinessException(ErrorCode.OPERATION_FAILED, "超出导出上限");
                }
            }

            log.info("✅ 优惠券统计导出完成（CSV）: jobId={}, rows={}", job.getId(), rowNum);
            return rowNum;
        }
    }

    /**
     * 解析导出参数
     */
    private ExportParams parseExportParams(String paramsJson) {
        if (paramsJson == null || paramsJson.isBlank()) {
            return new ExportParams();
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(paramsJson, ExportParams.class);
        } catch (Exception e) {
            log.warn("⚠️ 解析导出参数失败，使用默认参数: {}", e.getMessage());
            return new ExportParams();
        }
    }

    /**
     * 获取优惠券统计数据（支持筛选和缓存）
     */
    @Cacheable(value = "coupon:statistics", key = "#params.cacheKey()", unless = "#result == null || #result.isEmpty()")
    private List<CouponStatisticsResponse> fetchCouponStatistics(ExportParams params) {
        log.info("📊 获取优惠券统计数据: params={}", params);

        List<CouponStatisticsResponse> statistics;

        // 🎯 BaSui: 支持按优惠券ID筛选
        if (params.getCouponId() != null) {
            log.info("🔍 按优惠券ID筛选: couponId={}", params.getCouponId());
            CouponStatisticsResponse stat = couponService.getCouponStatistics(params.getCouponId());
            statistics = Collections.singletonList(stat);
        } else {
            log.info("📋 获取所有优惠券统计");
            statistics = couponService.getAllCouponStatistics();
        }

        // 🎯 BaSui: 支持日期范围筛选
        if (params.getStartDate() != null || params.getEndDate() != null) {
            log.info("📅 按日期范围筛选: startDate={}, endDate={}", params.getStartDate(), params.getEndDate());
            statistics = statistics.stream()
                    .filter(stat -> isWithinDateRange(stat, params.getStartDate(), params.getEndDate()))
                    .collect(Collectors.toList());
        }

        log.info("✅ 获取到 {} 条优惠券统计数据", statistics.size());
        return statistics;
    }

    /**
     * 检查优惠券是否在日期范围内
     */
    private boolean isWithinDateRange(CouponStatisticsResponse stat, LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime createdAt = stat.getCreatedAt();
        if (createdAt == null) {
            return false;
        }

        if (startDate != null && createdAt.isBefore(startDate)) {
            return false;
        }

        if (endDate != null && createdAt.isAfter(endDate)) {
            return false;
        }

        return true;
    }

    private static String escape(String v) {
        if (v == null) return "";
        String s = v.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\n") || s.contains("\r")) {
            return '"' + s + '"';
        }
        return s;
    }

    /**
     * 导出参数 DTO
     */
    @Data
    public static class ExportParams {
        /**
         * 优惠券ID（可选，为空则导出所有）
         */
        private Long couponId;

        /**
         * 开始日期（可选）
         */
        private LocalDateTime startDate;

        /**
         * 结束日期（可选）
         */
        private LocalDateTime endDate;

        /**
         * 导出格式（EXCEL/CSV，默认EXCEL）
         */
        private String format = "EXCEL";

        /**
         * 生成缓存键
         */
        public String cacheKey() {
            return String.format("coupon:%s:start:%s:end:%s",
                    couponId != null ? couponId : "all",
                    startDate != null ? startDate : "null",
                    endDate != null ? endDate : "null");
        }
    }
}
