package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.CouponResponse;
import com.campus.marketplace.common.entity.Coupon;
import com.campus.marketplace.common.entity.CouponUserRelation;
import com.campus.marketplace.common.entity.ExportJob;
import com.campus.marketplace.common.enums.CouponStatus;
import com.campus.marketplace.common.enums.CouponType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.CouponRepository;
import com.campus.marketplace.repository.CouponUserRelationRepository;
import com.campus.marketplace.repository.ExportJobRepository;
import com.campus.marketplace.service.CouponService;
import com.campus.marketplace.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Coupon Service Impl
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponUserRelationRepository relationRepository;
    private final @org.springframework.context.annotation.Lazy ExportService exportService;
    private final ExportJobRepository exportJobRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Coupon createCoupon(String code, String name, CouponType type,
                                BigDecimal discountAmount, BigDecimal discountRate, BigDecimal minAmount,
                                Integer totalCount, Integer limitPerUser,
                                LocalDateTime startTime, LocalDateTime endTime, String description) {
        log.info("创建优惠券: code={}, name={}, type={}", code, name, type);

        // 检查代码是否重复
        if (couponRepository.existsByCode(code)) {
            log.warn("优惠券代码已存在: code={}", code);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券代码已存在");
        }

        // 验证时间
        if (endTime.isBefore(startTime)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "结束时间不能早于开始时间");
        }

        Coupon coupon = Coupon.builder()
                .code(code)
                .name(name)
                .type(type)
                .discountAmount(discountAmount)
                .discountRate(discountRate)
                .minAmount(minAmount)
                .totalCount(totalCount)
                .receivedCount(0)
                .usedCount(0)
                .limitPerUser(limitPerUser)
                .startTime(startTime)
                .endTime(endTime)
                .description(description)
                .isActive(true)
                .build();

        couponRepository.save(coupon);
        log.info("优惠券创建成功: id={}, code={}", coupon.getId(), coupon.getCode());

        return coupon;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponUserRelation receiveCoupon(Long userId, Long couponId) {
        log.info("领取优惠券: userId={}, couponId={}", userId, couponId);

        // 查询优惠券
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在"));

        // 检查是否已过期
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券未开始");
        }
        if (now.isAfter(coupon.getEndTime())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券已过期");
        }

        // 检查是否激活
        if (!coupon.getIsActive()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券已停用");
        }

        // 检查库存
        if (coupon.getReceivedCount() >= coupon.getTotalCount()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券已领完");
        }

        // 检查领取限制
        if (coupon.getLimitPerUser() != null) {
            long receivedCount = relationRepository.countByUserIdAndCouponId(userId, couponId);
            if (receivedCount >= coupon.getLimitPerUser()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "已达领取上限");
            }
        }

        // 创建领取记录
        CouponUserRelation relation = CouponUserRelation.builder()
                .userId(userId)
                .couponId(couponId)
                .status(CouponStatus.AVAILABLE)
                .receiveTime(now)
                .expireTime(coupon.getEndTime())
                .build();

        relationRepository.save(relation);

        // 更新领取数量
        coupon.setReceivedCount(coupon.getReceivedCount() + 1);
        couponRepository.save(coupon);

        log.info("优惠券领取成功: userId={}, couponId={}, relationId={}", userId, couponId, relation.getId());

        return relation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useCoupon(Long userId, Long relationId, Long orderId) {
        log.info("使用优惠券: userId={}, relationId={}, orderId={}", userId, relationId, orderId);

        // 查询用户优惠券
        CouponUserRelation relation = relationRepository.findByIdAndUserIdAndStatus(
                relationId, userId, CouponStatus.AVAILABLE
        ).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在或不可用"));

        // 检查是否过期
        if (LocalDateTime.now().isAfter(relation.getExpireTime())) {
            relation.setStatus(CouponStatus.EXPIRED);
            relationRepository.save(relation);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券已过期");
        }

        // 标记为已使用
        relation.setStatus(CouponStatus.USED);
        relation.setUseTime(LocalDateTime.now());
        relation.setOrderId(orderId);
        relationRepository.save(relation);

        // 更新使用数量
        Coupon coupon = couponRepository.findById(relation.getCouponId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在"));
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);

        log.info("优惠券使用成功: userId={}, relationId={}, orderId={}", userId, relationId, orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundCoupon(Long relationId) {
        log.info("退还优惠券: relationId={}", relationId);

        CouponUserRelation relation = relationRepository.findById(relationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券记录不存在"));

        if (relation.getStatus() != CouponStatus.USED) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优惠券状态不正确");
        }

        // 检查是否过期
        if (LocalDateTime.now().isAfter(relation.getExpireTime())) {
            relation.setStatus(CouponStatus.EXPIRED);
        } else {
            relation.setStatus(CouponStatus.AVAILABLE);
        }
        
        relation.setUseTime(null);
        relation.setOrderId(null);
        relationRepository.save(relation);

        // 更新使用数量
        Coupon coupon = couponRepository.findById(relation.getCouponId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在"));
        coupon.setUsedCount(coupon.getUsedCount() - 1);
        couponRepository.save(coupon);

        log.info("优惠券退还成功: relationId={}", relationId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponResponse> listAvailableCoupons(int page, int size) {
        log.info("查询可用优惠券列表: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Coupon> couponPage = couponRepository.findAll(pageable);

        return couponPage.map(CouponResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponUserRelation> listUserCoupons(Long userId, int page, int size) {
        log.info("查询用户优惠券列表: userId={}, page={}, size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "receiveTime"));
        return relationRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(Long couponId, BigDecimal originalAmount) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在"));

        // 检查最低消费
        if (coupon.getMinAmount() != null && originalAmount.compareTo(coupon.getMinAmount()) < 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, 
                    "订单金额未达到优惠券使用条件（最低" + coupon.getMinAmount() + "元）");
        }

        BigDecimal discount = BigDecimal.ZERO;

        switch (coupon.getType()) {
            case FIXED:
                // 满减券
                discount = coupon.getDiscountAmount();
                break;
            case PERCENT:
                // 折扣券
                discount = originalAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscountRate()))
                        .setScale(2, RoundingMode.HALF_UP);
                break;
            case FREE_SHIPPING:
                // 包邮券不计算折扣
                discount = BigDecimal.ZERO;
                break;
        }

        log.info("计算优惠金额: couponId={}, originalAmount={}, discount={}",
                couponId, originalAmount, discount);

        return discount;
    }

    @Override
    @Transactional(readOnly = true)
    public Coupon getCouponById(Long couponId) {
        log.info("查询优惠券详情: couponId={}", couponId);

        return couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deactivateCoupon(Long couponId) {
        log.info("停用优惠券: couponId={}", couponId);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在"));

        coupon.setIsActive(false);
        couponRepository.save(coupon);

        log.info("优惠券停用成功: couponId={}", couponId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateCoupon(Long couponId) {
        log.info("启用优惠券: couponId={}", couponId);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在"));

        coupon.setIsActive(true);
        couponRepository.save(coupon);

        log.info("优惠券启用成功: couponId={}", couponId);
    }

    @Override
    @Transactional(readOnly = true)
    public com.campus.marketplace.common.dto.response.CouponStatisticsResponse getCouponStatistics(Long couponId) {
        log.info("获取优惠券统计信息: couponId={}", couponId);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在"));

        // 计算领取率
        double receiveRate = coupon.getTotalCount() > 0
                ? (double) coupon.getReceivedCount() / coupon.getTotalCount()
                : 0.0;

        // 计算使用率
        double useRate = coupon.getReceivedCount() > 0
                ? (double) coupon.getUsedCount() / coupon.getReceivedCount()
                : 0.0;

        // 计算总优惠金额和平均优惠金额
        java.util.List<CouponUserRelation> usedRelations = relationRepository.findByCouponIdAndStatus(
                couponId, CouponStatus.USED);

        BigDecimal totalDiscountAmount = BigDecimal.ZERO;
        for (CouponUserRelation relation : usedRelations) {
            if (relation.getOrderId() != null) {
                // 这里简化处理，实际应该从订单中获取优惠金额
                // 暂时使用优惠券的折扣金额
                if (coupon.getType() == CouponType.FIXED) {
                    totalDiscountAmount = totalDiscountAmount.add(coupon.getDiscountAmount());
                }
            }
        }

        BigDecimal avgDiscountAmount = coupon.getUsedCount() > 0
                ? totalDiscountAmount.divide(BigDecimal.valueOf(coupon.getUsedCount()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return com.campus.marketplace.common.dto.response.CouponStatisticsResponse.builder()
                .couponId(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .totalCount(coupon.getTotalCount())
                .receivedCount(coupon.getReceivedCount())
                .usedCount(coupon.getUsedCount())
                .receiveRate(receiveRate)
                .useRate(useRate)
                .totalDiscountAmount(totalDiscountAmount)
                .avgDiscountAmount(avgDiscountAmount)
                .createdAt(coupon.getCreatedAt())
                .startTime(coupon.getStartTime())
                .endTime(coupon.getEndTime())
                .isActive(coupon.getIsActive())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.campus.marketplace.common.dto.response.CouponStatisticsResponse> getAllCouponStatistics() {
        log.info("获取所有优惠券统计列表");

        java.util.List<Coupon> coupons = couponRepository.findAll();

        return coupons.stream()
                .map(coupon -> getCouponStatistics(coupon.getId()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public com.campus.marketplace.common.dto.response.CouponTrendStatisticsResponse getCouponTrendStatistics(
            Long couponId, String periodType, int days) {
        log.info("获取优惠券趋势统计: couponId={}, periodType={}, days={}", couponId, periodType, days);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在"));

        // 获取所有使用记录
        java.util.List<CouponUserRelation> allRelations = relationRepository.findByCouponIdAndStatus(
                couponId, CouponStatus.USED);

        // 按日期分组统计
        java.time.LocalDate endDate = java.time.LocalDate.now();
        java.time.LocalDate startDate = endDate.minusDays(days);

        java.util.List<com.campus.marketplace.common.dto.response.CouponTrendStatisticsResponse.TrendDataPoint> trendData =
                new java.util.ArrayList<>();

        for (java.time.LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final java.time.LocalDate currentDate = date;

            // 统计当天领取数量
            long receivedCount = allRelations.stream()
                    .filter(r -> r.getReceiveTime() != null &&
                            r.getReceiveTime().toLocalDate().equals(currentDate))
                    .count();

            // 统计当天使用数量
            long usedCount = allRelations.stream()
                    .filter(r -> r.getUseTime() != null &&
                            r.getUseTime().toLocalDate().equals(currentDate))
                    .count();

            // 计算使用率
            double useRate = receivedCount > 0 ? (double) usedCount / receivedCount : 0.0;

            trendData.add(com.campus.marketplace.common.dto.response.CouponTrendStatisticsResponse.TrendDataPoint.builder()
                    .date(currentDate)
                    .receivedCount((int) receivedCount)
                    .usedCount((int) usedCount)
                    .useRate(useRate)
                    .build());
        }

        return com.campus.marketplace.common.dto.response.CouponTrendStatisticsResponse.builder()
                .couponId(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .periodType(periodType)
                .trendData(trendData)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public com.campus.marketplace.common.dto.response.CouponUserRankingResponse getCouponUserRanking(
            Long couponId, int topN) {
        log.info("获取优惠券用户排行: couponId={}, topN={}", couponId, topN);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在"));

        // 获取所有使用记录
        java.util.List<CouponUserRelation> usedRelations = relationRepository.findByCouponIdAndStatus(
                couponId, CouponStatus.USED);

        // 按用户分组统计
        java.util.Map<Long, java.util.List<CouponUserRelation>> userRelationsMap = usedRelations.stream()
                .collect(java.util.stream.Collectors.groupingBy(CouponUserRelation::getUserId));

        // 计算每个用户的使用次数和总优惠金额
        java.util.List<com.campus.marketplace.common.dto.response.CouponUserRankingResponse.UserRankingItem> ranking =
                new java.util.ArrayList<>();

        for (java.util.Map.Entry<Long, java.util.List<CouponUserRelation>> entry : userRelationsMap.entrySet()) {
            Long userId = entry.getKey();
            java.util.List<CouponUserRelation> relations = entry.getValue();

            int useCount = relations.size();

            // 计算总优惠金额（简化处理）
            BigDecimal totalDiscountAmount = BigDecimal.ZERO;
            if (coupon.getType() == CouponType.FIXED) {
                totalDiscountAmount = coupon.getDiscountAmount()
                        .multiply(BigDecimal.valueOf(useCount));
            }

            // 获取用户名（简化处理，实际应该从 UserRepository 查询）
            String username = "User_" + userId;

            ranking.add(com.campus.marketplace.common.dto.response.CouponUserRankingResponse.UserRankingItem.builder()
                    .userId(userId)
                    .username(username)
                    .useCount(useCount)
                    .totalDiscountAmount(totalDiscountAmount)
                    .build());
        }

        // 按使用次数降序排序，取前N名
        ranking.sort((a, b) -> b.getUseCount().compareTo(a.getUseCount()));
        java.util.List<com.campus.marketplace.common.dto.response.CouponUserRankingResponse.UserRankingItem> topRanking =
                ranking.stream().limit(topN).collect(java.util.stream.Collectors.toList());

        // 设置排名
        for (int i = 0; i < topRanking.size(); i++) {
            topRanking.get(i).setRank(i + 1);
        }

        return com.campus.marketplace.common.dto.response.CouponUserRankingResponse.builder()
                .couponId(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .userRanking(topRanking)
                .build();
    }

    @Override
    public String exportCouponStatistics(Long couponId, String format) {
        log.info("🎯 导出优惠券统计: couponId={}, format={}", couponId, format);

        // ✅ BaSui: 复用现有 ExportService 框架
        // 1. 构建导出参数（支持按优惠券ID筛选、日期范围、导出格式）
        // 2. 调用 ExportService.requestExport() 创建导出任务
        // 3. 异步执行导出（由 TaskService 调度）
        // 4. 返回任务ID供前端轮询

        try {
            // 构建导出参数 JSON
            Map<String, Object> params = new HashMap<>();
            if (couponId != null) {
                params.put("couponId", couponId);
            }
            if (format != null && !format.isBlank()) {
                params.put("format", format.toUpperCase());
            } else {
                params.put("format", "EXCEL");
            }

            // 转换为 JSON 字符串
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String paramsJson = mapper.writeValueAsString(params);

            // 创建导出任务
            Long jobId = exportService.requestExport("COUPON_STATISTICS", paramsJson);

            log.info("✅ 导出任务已创建: jobId={}, params={}", jobId, paramsJson);
            return String.valueOf(jobId);
        } catch (Exception e) {
            log.error("❌ 创建导出任务失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "创建导出任务失败: " + e.getMessage());
        }
    }

    @Override
    public com.campus.marketplace.common.dto.response.ExportTaskResponse getExportTaskStatus(String taskId) {
        log.info("🔍 获取导出任务状态: taskId={}", taskId);

        // ✅ BaSui: 从导出任务表中查询真实状态
        Long jobId = Long.parseLong(taskId);
        ExportJob job = exportJobRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "导出任务不存在"));

        // 映射状态
        String status = mapJobStatus(job.getStatus());
        Integer progress = calculateProgress(job.getStatus());

        return com.campus.marketplace.common.dto.response.ExportTaskResponse.builder()
                .taskId(taskId)
                .taskType("COUPON_STATISTICS")
                .format("EXCEL")
                .status(status)
                .progress(progress)
                .downloadUrl(job.getDownloadToken() != null ? "/api/exports/download/" + job.getDownloadToken() : null)
                .errorMessage(job.getError())
                .createdAt(job.getCreatedAt() != null ? LocalDateTime.ofInstant(job.getCreatedAt(), java.time.ZoneId.systemDefault()) : null)
                .completedAt(job.getCompletedAt() != null ? LocalDateTime.ofInstant(job.getCompletedAt(), java.time.ZoneId.systemDefault()) : null)
                .build();
    }

    /**
     * 映射任务状态
     */
    private String mapJobStatus(String jobStatus) {
        return switch (jobStatus) {
            case "PENDING" -> "PENDING";
            case "RUNNING" -> "PROCESSING";
            case "SUCCESS" -> "COMPLETED";
            case "FAILED" -> "FAILED";
            case "CANCELLED" -> "FAILED";
            default -> "PENDING";
        };
    }

    /**
     * 计算进度
     */
    private Integer calculateProgress(String jobStatus) {
        return switch (jobStatus) {
            case "PENDING" -> 0;
            case "RUNNING" -> 50;
            case "SUCCESS" -> 100;
            case "FAILED", "CANCELLED" -> 0;
            default -> 0;
        };
    }
}
