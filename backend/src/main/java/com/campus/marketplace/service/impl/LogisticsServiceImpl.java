package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.LogisticsDTO;
import com.campus.marketplace.common.dto.LogisticsStatisticsDTO;
import com.campus.marketplace.common.entity.Logistics;
import com.campus.marketplace.common.entity.LogisticsTrackRecord;
import com.campus.marketplace.common.enums.LogisticsCompany;
import com.campus.marketplace.common.enums.LogisticsStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.logistics.LogisticsApiException;
import com.campus.marketplace.logistics.LogisticsProvider;
import com.campus.marketplace.logistics.LogisticsProviderFactory;
import com.campus.marketplace.repository.LogisticsRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.common.component.NotificationDispatcher;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 物流服务实现类
 * <p>
 * 提供物流信息的查询、更新、统计等功能。
 * 使用 Redis 缓存物流信息，减少快递API调用频率。
 * </p>
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsServiceImpl implements LogisticsService {

    private final LogisticsRepository logisticsRepository;
    private final LogisticsProviderFactory providerFactory;
    private final OrderRepository orderRepository;
    private final NotificationDispatcher notificationDispatcher;

    /**
     * 同步间隔时间（小时）
     */
    private static final int SYNC_INTERVAL_HOURS = 2;

    /**
     * 预计送达时间（天）
     */
    private static final int ESTIMATED_DELIVERY_DAYS = 3;

    @Override
    @Transactional
    public LogisticsDTO createLogistics(Long orderId, String trackingNumber, LogisticsCompany company) {
        log.info("创建物流信息: orderId={}, trackingNumber={}, company={}", orderId, trackingNumber, company);

        // 1. 校验参数
        if (orderId == null || trackingNumber == null || company == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "参数不能为空");
        }

        // 2. 检查订单是否已有物流信息
        Optional<Logistics> existingLogistics = logisticsRepository.findByOrderId(orderId);
        if (existingLogistics.isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "订单已有物流信息，无法重复创建");
        }

        // 3. 校验快递单号格式
        LogisticsProvider provider = providerFactory.getProvider(company);
        if (!provider.isValidTrackingNumber(trackingNumber)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "快递单号格式不正确");
        }

        // 4. 创建物流记录
        Logistics logistics = Logistics.builder()
                .orderId(orderId)
                .trackingNumber(trackingNumber)
                .logisticsCompany(company)
                .status(LogisticsStatus.PENDING)
                .isOvertime(false)
                .syncCount(0)
                .estimatedDeliveryTime(LocalDateTime.now().plusDays(ESTIMATED_DELIVERY_DAYS))
                .build();

        // 5. 保存到数据库
        Logistics savedLogistics = logisticsRepository.save(logistics);

        // 6. 立即同步一次物流信息
        try {
            syncLogisticsInternal(savedLogistics);
        } catch (Exception e) {
            log.warn("创建物流信息后首次同步失败: {}", e.getMessage());
            // 首次同步失败不影响创建流程
        }

        // 7. 转换为DTO并返回
        return convertToDTO(savedLogistics);
    }

    @Override
    @Cacheable(value = "logistics", key = "'order:' + #orderId", unless = "#result == null")
    public LogisticsDTO getLogisticsByOrderId(Long orderId) {
        log.info("查询物流信息: orderId={}", orderId);

        Logistics logistics = logisticsRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "物流信息不存在"));

        return convertToDTO(logistics);
    }

    @Override
    @Cacheable(value = "logistics", key = "'tracking:' + #trackingNumber", unless = "#result == null")
    public LogisticsDTO getLogisticsByTrackingNumber(String trackingNumber) {
        log.info("查询物流信息: trackingNumber={}", trackingNumber);

        Logistics logistics = logisticsRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "物流信息不存在"));

        return convertToDTO(logistics);
    }

    @Override
    @Transactional
    @CacheEvict(value = "logistics", allEntries = true)
    public LogisticsDTO syncLogistics(Long orderId) {
        log.info("手动同步物流信息: orderId={}", orderId);

        Logistics logistics = logisticsRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "物流信息不存在"));

        // 同步物流信息
        syncLogisticsInternal(logistics);

        // 保存更新
        Logistics updatedLogistics = logisticsRepository.save(logistics);

        return convertToDTO(updatedLogistics);
    }

    @Override
    @Transactional
    public int batchSyncLogistics() {
        log.info("批量同步物流信息");

        // 1. 查询待同步的物流信息
        LocalDateTime threshold = LocalDateTime.now().minusHours(SYNC_INTERVAL_HOURS);
        List<LogisticsStatus> syncStatuses = Arrays.asList(
                LogisticsStatus.PICKED_UP,
                LogisticsStatus.IN_TRANSIT,
                LogisticsStatus.DELIVERING
        );

        List<Logistics> pendingLogistics = logisticsRepository.findPendingLogistics(threshold, syncStatuses);
        log.info("找到 {} 条待同步的物流信息", pendingLogistics.size());

        // 2. 批量同步
        int successCount = 0;
        for (Logistics logistics : pendingLogistics) {
            try {
                syncLogisticsInternal(logistics);
                logisticsRepository.save(logistics);
                successCount++;
            } catch (Exception e) {
                log.error("同步物流信息失败: orderId={}, error={}", logistics.getOrderId(), e.getMessage());
            }
        }

        log.info("批量同步完成: 成功 {}/{}", successCount, pendingLogistics.size());
        return successCount;
    }

    @Override
    @Transactional
    public int markOvertimeLogistics() {
        log.info("检查并标记超时物流");

        // 1. 查询所有未签收的物流信息
        List<LogisticsStatus> excludeStatuses = Arrays.asList(
                LogisticsStatus.DELIVERED,
                LogisticsStatus.REJECTED,
                LogisticsStatus.LOST
        );

        List<Logistics> allLogistics = logisticsRepository.findOvertimeLogistics(excludeStatuses);

        // 2. 筛选出超时的物流
        LocalDateTime now = LocalDateTime.now();
        int overtimeCount = 0;

        for (Logistics logistics : allLogistics) {
            if (logistics.getEstimatedDeliveryTime() != null
                    && logistics.getEstimatedDeliveryTime().isBefore(now)
                    && !logistics.getIsOvertime()) {

                logistics.setIsOvertime(true);
                logisticsRepository.save(logistics);
                overtimeCount++;

                log.warn("物流超时: orderId={}, trackingNumber={}, estimatedTime={}",
                        logistics.getOrderId(),
                        logistics.getTrackingNumber(),
                        logistics.getEstimatedDeliveryTime());
            }
        }

        log.info("标记超时物流完成: 超时 {}/{}", overtimeCount, allLogistics.size());
        return overtimeCount;
    }

    @Override
    public LogisticsStatisticsDTO getLogisticsStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("获取物流统计数据: startDate={}, endDate={}", startDate, endDate);

        // 1. 初始化统计数据
        Map<LogisticsCompany, Double> averageDeliveryTime = new HashMap<>();
        Map<LogisticsCompany, Double> overtimeRate = new HashMap<>();
        Map<LogisticsCompany, Double> userRating = new HashMap<>();

        int totalOrders = 0;
        int overtimeOrders = 0;

        // 2. 遍历所有快递公司，统计数据
        for (LogisticsCompany company : LogisticsCompany.values()) {
            // 统计订单数
            long companyOrders = logisticsRepository.countByCompanyAndDateRange(company, startDate, endDate);
            totalOrders += companyOrders;

            // 统计超时订单数
            long companyOvertimeOrders = logisticsRepository.countOvertimeByCompanyAndDateRange(company, startDate, endDate);
            overtimeOrders += companyOvertimeOrders;

            // 计算延误率
            double companyOvertimeRate = companyOrders > 0
                    ? (double) companyOvertimeOrders / companyOrders * 100
                    : 0.0;
            overtimeRate.put(company, companyOvertimeRate);

            // 计算平均送达时间（暂时使用模拟数据，后续可从数据库计算）
            averageDeliveryTime.put(company, calculateAverageDeliveryTime(company, startDate, endDate));

            // 用户评分（暂时使用模拟数据，后续可从评价系统获取）
            userRating.put(company, 4.5);
        }

        // 3. 构建统计DTO
        return LogisticsStatisticsDTO.builder()
                .averageDeliveryTime(averageDeliveryTime)
                .overtimeRate(overtimeRate)
                .userRating(userRating)
                .totalOrders(totalOrders)
                .overtimeOrders(overtimeOrders)
                .build();
    }

    @Override
    public Page<LogisticsDTO> listLogistics(com.campus.marketplace.common.dto.request.LogisticsFilterRequest filterRequest) {
        log.info("🎯 BaSui：分页查询物流列表（统一筛选） - keyword={}, status={}, page={}, size={}",
                filterRequest.getKeyword(), filterRequest.getStatus(), filterRequest.getPage(), filterRequest.getSize());

        // 构建分页和排序参数
        Sort.Direction direction = "ASC".equalsIgnoreCase(filterRequest.getSortDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(
                filterRequest.getPageOrDefault(),
                filterRequest.getSizeOrDefault(),
                Sort.by(direction, filterRequest.getSortBy() != null ? filterRequest.getSortBy() : "createdAt")
        );

        // 调用传统方法（复用现有逻辑）
        return listLogistics(filterRequest.getKeyword(), filterRequest.getStatus(), pageable);
    }

    @Override
    public Page<LogisticsDTO> listLogistics(String keyword, LogisticsStatus status, Pageable pageable) {
        log.info("🎯 BaSui：分页查询物流列表 - keyword={}, status={}, page={}, size={}",
                keyword, status, pageable.getPageNumber(), pageable.getPageSize());

        // 构建动态查询条件
        Specification<Logistics> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 关键词搜索（订单ID或快递单号）
            if (keyword != null && !keyword.isBlank()) {
                try {
                    // 尝试解析为订单ID
                    Long orderId = Long.parseLong(keyword.trim());
                    predicates.add(cb.or(
                            cb.equal(root.get("orderId"), orderId),
                            cb.like(root.get("trackingNumber"), "%" + keyword.trim() + "%")
                    ));
                } catch (NumberFormatException e) {
                    // 如果不是数字，只搜索快递单号
                    predicates.add(cb.like(root.get("trackingNumber"), "%" + keyword.trim() + "%"));
                }
            }

            // 状态筛选
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行分页查询
        Page<Logistics> logisticsPage = logisticsRepository.findAll(spec, pageable);

        log.info("✅ BaSui：查询完成 - 共 {} 条记录", logisticsPage.getTotalElements());

        // 转换为DTO
        return logisticsPage.map(this::convertToDTO);
    }

    /**
     * 同步物流信息（内部方法）
     *
     * @param logistics 物流实体
     */
    private void syncLogisticsInternal(Logistics logistics) {
        try {
            // 记录旧状态，用于判断是否需要发送通知
            LogisticsStatus oldStatus = logistics.getStatus();

            // 1. 获取快递公司的API实现
            LogisticsProvider provider = providerFactory.getProvider(logistics.getLogisticsCompany());

            // 2. 查询物流轨迹
            List<LogisticsTrackRecord> trackRecords = provider.queryTrackRecords(logistics.getTrackingNumber());
            logistics.setTrackRecords(trackRecords);

            // 3. 查询物流状态
            LogisticsStatus newStatus = provider.queryStatus(logistics.getTrackingNumber());
            logistics.setStatus(newStatus);

            // 4. 更新当前位置（取最新一条轨迹的位置）
            if (!trackRecords.isEmpty()) {
                LogisticsTrackRecord latestRecord = trackRecords.get(trackRecords.size() - 1);
                logistics.setCurrentLocation(latestRecord.getLocation());
            }

            // 5. 如果已签收，记录实际送达时间
            if (newStatus == LogisticsStatus.DELIVERED && logistics.getActualDeliveryTime() == null) {
                logistics.setActualDeliveryTime(LocalDateTime.now());
            }

            // 6. 更新同步信息
            logistics.setSyncCount(logistics.getSyncCount() + 1);
            logistics.setLastSyncTime(LocalDateTime.now());

            log.info("同步物流信息成功: orderId={}, status={}, syncCount={}",
                    logistics.getOrderId(), newStatus, logistics.getSyncCount());

            // 🎯 BaSui 新增：物流状态变更通知
            if (oldStatus != newStatus) {
                sendLogisticsStatusChangeNotification(logistics, oldStatus, newStatus);
            }

        } catch (LogisticsApiException e) {
            log.error("调用快递API失败: orderId={}, error={}", logistics.getOrderId(), e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "物流信息同步失败: " + e.getMessage());
        }
    }

    /**
     * 发送物流状态变更通知
     *
     * @param logistics 物流实体
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     */
    private void sendLogisticsStatusChangeNotification(Logistics logistics, LogisticsStatus oldStatus, LogisticsStatus newStatus) {
        try {
            // 查询订单信息
            Order order = orderRepository.findById(logistics.getOrderId()).orElse(null);
            if (order == null) {
                log.warn("订单不存在，跳过物流通知: orderId={}", logistics.getOrderId());
                return;
            }

            // 构建通知参数
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("orderNo", order.getOrderNo());
            params.put("trackingNumber", logistics.getTrackingNumber());
            params.put("expressCompany", logistics.getLogisticsCompany().getDisplayName());
            params.put("oldStatus", oldStatus.getDescription());
            params.put("newStatus", newStatus.getDescription());
            params.put("currentLocation", logistics.getCurrentLocation());

            // 根据新状态发送不同的通知
            String templateCode = getNotificationTemplateCode(newStatus);
            com.campus.marketplace.common.enums.NotificationType notificationType = getNotificationType(newStatus);

            // 通知买家
            if (notificationDispatcher != null) {
                notificationDispatcher.enqueueTemplate(
                        order.getBuyerId(),
                        templateCode,
                        params,
                        notificationType.name(),
                        order.getId(),
                        "ORDER",
                        "/orders/" + order.getOrderNo()
                );
            }

            log.info("物流状态变更通知已发送: orderId={}, oldStatus={}, newStatus={}",
                    logistics.getOrderId(), oldStatus, newStatus);

        } catch (Exception e) {
            log.warn("发送物流状态变更通知失败: orderId={}, error={}",
                    logistics.getOrderId(), e.getMessage());
        }
    }

    /**
     * 根据物流状态获取通知模板代码
     */
    private String getNotificationTemplateCode(LogisticsStatus status) {
        return switch (status) {
            case PICKED_UP -> "LOGISTICS_PICKED_UP";
            case IN_TRANSIT -> "LOGISTICS_IN_TRANSIT";
            case DELIVERING -> "LOGISTICS_DELIVERING";
            case DELIVERED -> "LOGISTICS_DELIVERED";
            case REJECTED -> "LOGISTICS_REJECTED";
            case LOST -> "LOGISTICS_LOST";
            default -> "LOGISTICS_STATUS_CHANGED";
        };
    }

    /**
     * 根据物流状态获取通知类型
     */
    private com.campus.marketplace.common.enums.NotificationType getNotificationType(LogisticsStatus status) {
        return switch (status) {
            case DELIVERED -> com.campus.marketplace.common.enums.NotificationType.ORDER_DELIVERED;
            case REJECTED, LOST -> com.campus.marketplace.common.enums.NotificationType.ORDER_EXCEPTION;
            default -> com.campus.marketplace.common.enums.NotificationType.ORDER_SHIPPED;
        };
    }

    /**
     * 计算平均送达时间（小时）
     * <p>
     * 注意：由于 Repository 中的 EXTRACT(EPOCH FROM ...) 函数仅在 PostgreSQL 中有效，
     * 这里使用 Java 代码计算平均送达时长。
     * </p>
     *
     * @param company   快递公司
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 平均送达时长（小时）
     */
    private Double calculateAverageDeliveryTime(LogisticsCompany company, LocalDateTime startDate, LocalDateTime endDate) {
        // 查询指定快递公司在时间范围内已签收的物流记录
        List<Logistics> deliveredLogistics = logisticsRepository.findDeliveredLogistics(
                company,
                LogisticsStatus.DELIVERED,
                startDate,
                endDate
        );

        // 如果没有已签收的物流记录，返回0.0
        if (deliveredLogistics.isEmpty()) {
            log.debug("快递公司{}在时间范围内没有已签收的物流记录", company);
            return 0.0;
        }

        // 计算平均送达时长（小时）
        double totalHours = 0.0;
        int validCount = 0;

        for (Logistics logistics : deliveredLogistics) {
            if (logistics.getCreatedAt() != null && logistics.getActualDeliveryTime() != null) {
                Duration duration = Duration.between(logistics.getCreatedAt(), logistics.getActualDeliveryTime());
                totalHours += duration.toHours();
                validCount++;
            }
        }

        // 如果没有有效的记录，返回0.0
        if (validCount == 0) {
            log.debug("快递公司{}在时间范围内没有有效的送达时长数据", company);
            return 0.0;
        }

        double averageHours = totalHours / validCount;
        log.debug("快递公司{}的平均送达时长：{}小时（基于{}条记录）", company, averageHours, validCount);

        return averageHours;
    }

    /**
     * 转换为DTO
     *
     * @param logistics 物流实体
     * @return 物流DTO
     */
    private LogisticsDTO convertToDTO(Logistics logistics) {
        return LogisticsDTO.builder()
                .id(logistics.getId())
                .orderId(logistics.getOrderId())
                .trackingNumber(logistics.getTrackingNumber())
                .logisticsCompany(logistics.getLogisticsCompany())
                .status(logistics.getStatus())
                .currentLocation(logistics.getCurrentLocation())
                .estimatedDeliveryTime(logistics.getEstimatedDeliveryTime())
                .actualDeliveryTime(logistics.getActualDeliveryTime())
                .isOvertime(logistics.getIsOvertime())
                .trackRecords(logistics.getTrackRecords())
                .syncCount(logistics.getSyncCount())
                .lastSyncTime(logistics.getLastSyncTime())
                .createdAt(logistics.getCreatedAt())
                .updatedAt(logistics.getUpdatedAt())
                .build();
    }
}
