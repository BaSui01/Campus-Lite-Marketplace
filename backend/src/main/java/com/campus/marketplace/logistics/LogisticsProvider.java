package com.campus.marketplace.logistics;

import com.campus.marketplace.common.entity.LogisticsTrackRecord;
import com.campus.marketplace.common.enums.LogisticsCompany;
import com.campus.marketplace.common.enums.LogisticsStatus;

import java.util.List;

/**
 * 物流服务提供商接口
 * <p>
 * 统一封装各快递公司的API调用，屏蔽底层差异。
 * 所有快递公司的实现类都必须实现此接口。
 * </p>
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
public interface LogisticsProvider {

    /**
     * 查询物流轨迹
     * <p>
     * 调用快递公司API，获取最新的物流轨迹信息。
     * </p>
     *
     * @param trackingNumber 快递单号
     * @return 物流轨迹列表（按时间倒序）
     * @throws LogisticsApiException 当API调用失败时抛出异常
     */
    List<LogisticsTrackRecord> queryTrackRecords(String trackingNumber);

    /**
     * 查询物流状态
     * <p>
     * 根据快递公司返回的状态码，映射到系统内部的物流状态枚举。
     * </p>
     *
     * @param trackingNumber 快递单号
     * @return 物流状态
     * @throws LogisticsApiException 当API调用失败时抛出异常
     */
    LogisticsStatus queryStatus(String trackingNumber);

    /**
     * 获取支持的快递公司
     * <p>
     * 返回当前实现类支持的快递公司枚举。
     * </p>
     *
     * @return 快递公司枚举
     */
    LogisticsCompany getSupportedCompany();

    /**
     * 检查快递单号格式是否有效
     * <p>
     * 不同快递公司的单号格式不同，此方法用于快速校验。
     * </p>
     *
     * @param trackingNumber 快递单号
     * @return true=有效，false=无效
     */
    boolean isValidTrackingNumber(String trackingNumber);
}
