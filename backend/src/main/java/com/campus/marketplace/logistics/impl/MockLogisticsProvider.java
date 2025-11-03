package com.campus.marketplace.logistics.impl;

import com.campus.marketplace.common.entity.LogisticsTrackRecord;
import com.campus.marketplace.common.enums.LogisticsCompany;
import com.campus.marketplace.common.enums.LogisticsStatus;
import com.campus.marketplace.logistics.LogisticsProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 模拟物流服务提供商（用于开发和测试）
 * <p>
 * 此实现类不调用真实的快递API，而是返回模拟数据。
 * 仅在配置 logistics.mock.enabled=true 时启用。
 * </p>
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "logistics.mock.enabled", havingValue = "true", matchIfMissing = true)
public class MockLogisticsProvider implements LogisticsProvider {

    @Override
    public List<LogisticsTrackRecord> queryTrackRecords(String trackingNumber) {
        log.info("模拟查询物流轨迹: {}", trackingNumber);

        // 模拟物流轨迹数据
        List<LogisticsTrackRecord> records = new ArrayList<>();

        records.add(new LogisticsTrackRecord(
                LocalDateTime.now().minusDays(3),
                "广东省深圳市",
                "【深圳市】已揽件",
                "张三"
        ));

        records.add(new LogisticsTrackRecord(
                LocalDateTime.now().minusDays(2),
                "广东省广州市",
                "【广州市】运输中",
                "李四"
        ));

        records.add(new LogisticsTrackRecord(
                LocalDateTime.now().minusDays(1),
                "湖北省武汉市",
                "【武汉市】到达武汉转运中心",
                "王五"
        ));

        records.add(new LogisticsTrackRecord(
                LocalDateTime.now().minusHours(6),
                "湖北省武汉市",
                "【武汉市】派送中，快递员：赵六，电话：138****1234",
                "赵六"
        ));

        records.add(new LogisticsTrackRecord(
                LocalDateTime.now().minusHours(1),
                "湖北省武汉市",
                "【武汉市】已签收，签收人：本人",
                "赵六"
        ));

        return records;
    }

    @Override
    public LogisticsStatus queryStatus(String trackingNumber) {
        log.info("模拟查询物流状态: {}", trackingNumber);

        // 模拟返回已签收状态
        return LogisticsStatus.DELIVERED;
    }

    @Override
    public LogisticsCompany getSupportedCompany() {
        // 模拟提供商支持所有快递公司
        return LogisticsCompany.SHUNFENG;
    }

    @Override
    public boolean isValidTrackingNumber(String trackingNumber) {
        // 简单校验：快递单号长度在10-20位之间
        return trackingNumber != null
                && trackingNumber.length() >= 10
                && trackingNumber.length() <= 20;
    }
}
