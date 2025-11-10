package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.PaymentRecordDTO;
import com.campus.marketplace.common.dto.response.PaymentStatisticsDTO;
import org.springframework.data.domain.Page;

/**
 * 支付管理服务接口（管理员）
 * 
 * @author BaSui 😎
 * @date 2025-11-10
 */
public interface PaymentAdminService {

    /**
     * 查询支付记录列表（管理员）
     * 
     * @param keyword 关键词（订单号/用户名/商品名）
     * @param status 订单状态（PAID/COMPLETED/REFUNDED等）
     * @param paymentMethod 支付方式（WECHAT/ALIPAY）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param page 页码
     * @param size 每页大小
     * @return 支付记录分页
     */
    Page<PaymentRecordDTO> listPayments(
        String keyword,
        String status,
        String paymentMethod,
        String startDate,
        String endDate,
        int page,
        int size
    );

    /**
     * 查询支付详情
     * 
     * @param orderNo 订单号
     * @return 支付详情
     */
    PaymentRecordDTO getPaymentDetail(String orderNo);

    /**
     * 查询支付统计
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 支付统计
     */
    PaymentStatisticsDTO getStatistics(String startDate, String endDate);
}
