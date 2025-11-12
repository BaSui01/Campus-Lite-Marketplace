package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.CouponResponse;
import com.campus.marketplace.common.entity.Coupon;
import com.campus.marketplace.common.entity.CouponUserRelation;
import com.campus.marketplace.common.enums.CouponType;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券服务接口
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public interface CouponService {

    /**
     * 创建优惠券
     */
    Coupon createCoupon(String code, String name, CouponType type,
                        BigDecimal discountAmount, BigDecimal discountRate, BigDecimal minAmount,
                        Integer totalCount, Integer limitPerUser,
                        LocalDateTime startTime, LocalDateTime endTime, String description);

    /**
     * 领取优惠券
     */
    CouponUserRelation receiveCoupon(Long userId, Long couponId);

    /**
     * 使用优惠券
     */
    void useCoupon(Long userId, Long relationId, Long orderId);

    /**
     * 退还优惠券（订单取消时）
     */
    void refundCoupon(Long relationId);

    /**
     * 查询可用优惠券列表
     */
    Page<CouponResponse> listAvailableCoupons(int page, int size);

    /**
     * 查询用户的优惠券列表
     */
    Page<CouponUserRelation> listUserCoupons(Long userId, int page, int size);

    /**
     * 计算优惠金额
     */
    BigDecimal calculateDiscount(Long couponId, BigDecimal originalAmount);

    /**
     * 根据ID查询优惠券
     */
    Coupon getCouponById(Long couponId);

    /**
     * 停用优惠券
     */
    void deactivateCoupon(Long couponId);

    /**
     * 启用优惠券
     */
    void activateCoupon(Long couponId);

    /**
     * 获取优惠券统计信息
     */
    com.campus.marketplace.common.dto.response.CouponStatisticsResponse getCouponStatistics(Long couponId);

    /**
     * 获取所有优惠券统计列表
     */
    java.util.List<com.campus.marketplace.common.dto.response.CouponStatisticsResponse> getAllCouponStatistics();

    /**
     * 获取优惠券趋势统计（时间维度）
     *
     * @param couponId   优惠券ID
     * @param periodType 周期类型（DAILY/WEEKLY/MONTHLY）
     * @param days       统计天数
     */
    com.campus.marketplace.common.dto.response.CouponTrendStatisticsResponse getCouponTrendStatistics(
            Long couponId, String periodType, int days);

    /**
     * 获取优惠券用户排行（用户维度）
     *
     * @param couponId 优惠券ID
     * @param topN     返回前N名用户
     */
    com.campus.marketplace.common.dto.response.CouponUserRankingResponse getCouponUserRanking(
            Long couponId, int topN);

    /**
     * 导出优惠券统计（异步）
     *
     * @param couponId 优惠券ID（可选，为空则导出所有）
     * @param format   导出格式（EXCEL/CSV）
     * @return 导出任务ID
     */
    String exportCouponStatistics(Long couponId, String format);

    /**
     * 获取导出任务状态
     *
     * @param taskId 任务ID
     * @return 导出任务响应
     */
    com.campus.marketplace.common.dto.response.ExportTaskResponse getExportTaskStatus(String taskId);
}
