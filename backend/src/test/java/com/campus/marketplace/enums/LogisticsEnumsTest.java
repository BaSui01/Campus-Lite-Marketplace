package com.campus.marketplace.enums;

import com.campus.marketplace.common.enums.LogisticsCompany;
import com.campus.marketplace.common.enums.LogisticsStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 物流枚举测试类
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
class LogisticsEnumsTest {

    @Test
    @DisplayName("物流状态枚举应该包含所有必需状态")
    void shouldContainAllRequiredLogisticsStatuses() {
        // 验证枚举值完整性
        assertThat(LogisticsStatus.values()).hasSize(7);
        assertThat(LogisticsStatus.PENDING).isNotNull();
        assertThat(LogisticsStatus.PICKED_UP).isNotNull();
        assertThat(LogisticsStatus.IN_TRANSIT).isNotNull();
        assertThat(LogisticsStatus.DELIVERING).isNotNull();
        assertThat(LogisticsStatus.DELIVERED).isNotNull();
        assertThat(LogisticsStatus.REJECTED).isNotNull();
        assertThat(LogisticsStatus.LOST).isNotNull();
    }

    @Test
    @DisplayName("物流状态应该有正确的中文描述")
    void logisticsStatusShouldHaveCorrectDescription() {
        assertThat(LogisticsStatus.PENDING.getDescription()).isEqualTo("待发货");
        assertThat(LogisticsStatus.PICKED_UP.getDescription()).isEqualTo("已揽件");
        assertThat(LogisticsStatus.IN_TRANSIT.getDescription()).isEqualTo("运输中");
        assertThat(LogisticsStatus.DELIVERING.getDescription()).isEqualTo("派送中");
        assertThat(LogisticsStatus.DELIVERED.getDescription()).isEqualTo("已签收");
        assertThat(LogisticsStatus.REJECTED.getDescription()).isEqualTo("已拒签");
        assertThat(LogisticsStatus.LOST.getDescription()).isEqualTo("疑似丢失");
    }

    @Test
    @DisplayName("物流公司枚举应该包含主流快递公司")
    void shouldContainMainLogisticsCompanies() {
        assertThat(LogisticsCompany.values()).hasSize(8);
        assertThat(LogisticsCompany.SHUNFENG).isNotNull();
        assertThat(LogisticsCompany.ZHONGTONG).isNotNull();
        assertThat(LogisticsCompany.YUANTONG).isNotNull();
        assertThat(LogisticsCompany.YUNDA).isNotNull();
        assertThat(LogisticsCompany.EMS).isNotNull();
        assertThat(LogisticsCompany.JINGDONG).isNotNull();
        assertThat(LogisticsCompany.DEBANG).isNotNull();
        assertThat(LogisticsCompany.SHENTONG).isNotNull();
    }

    @Test
    @DisplayName("物流公司应该有正确的名称和代码")
    void logisticsCompanyShouldHaveCorrectNameAndCode() {
        assertThat(LogisticsCompany.SHUNFENG.getDisplayName()).isEqualTo("顺丰速运");
        assertThat(LogisticsCompany.SHUNFENG.getCode()).isEqualTo("SF");

        assertThat(LogisticsCompany.ZHONGTONG.getDisplayName()).isEqualTo("中通快递");
        assertThat(LogisticsCompany.ZHONGTONG.getCode()).isEqualTo("ZTO");

        assertThat(LogisticsCompany.YUANTONG.getDisplayName()).isEqualTo("圆通速递");
        assertThat(LogisticsCompany.YUANTONG.getCode()).isEqualTo("YTO");

        assertThat(LogisticsCompany.YUNDA.getDisplayName()).isEqualTo("韵达快递");
        assertThat(LogisticsCompany.YUNDA.getCode()).isEqualTo("YD");

        assertThat(LogisticsCompany.EMS.getDisplayName()).isEqualTo("邮政EMS");
        assertThat(LogisticsCompany.EMS.getCode()).isEqualTo("EMS");

        assertThat(LogisticsCompany.JINGDONG.getDisplayName()).isEqualTo("京东物流");
        assertThat(LogisticsCompany.JINGDONG.getCode()).isEqualTo("JD");

        assertThat(LogisticsCompany.DEBANG.getDisplayName()).isEqualTo("德邦物流");
        assertThat(LogisticsCompany.DEBANG.getCode()).isEqualTo("DBL");

        assertThat(LogisticsCompany.SHENTONG.getDisplayName()).isEqualTo("申通快递");
        assertThat(LogisticsCompany.SHENTONG.getCode()).isEqualTo("STO");
    }

    @Test
    @DisplayName("应该能通过代码查找物流公司")
    void shouldFindLogisticsCompanyByCode() {
        assertThat(LogisticsCompany.fromCode("SF")).isEqualTo(LogisticsCompany.SHUNFENG);
        assertThat(LogisticsCompany.fromCode("ZTO")).isEqualTo(LogisticsCompany.ZHONGTONG);
        assertThat(LogisticsCompany.fromCode("YTO")).isEqualTo(LogisticsCompany.YUANTONG);
        assertThat(LogisticsCompany.fromCode("YD")).isEqualTo(LogisticsCompany.YUNDA);
        assertThat(LogisticsCompany.fromCode("EMS")).isEqualTo(LogisticsCompany.EMS);
        assertThat(LogisticsCompany.fromCode("JD")).isEqualTo(LogisticsCompany.JINGDONG);
        assertThat(LogisticsCompany.fromCode("DBL")).isEqualTo(LogisticsCompany.DEBANG);
        assertThat(LogisticsCompany.fromCode("STO")).isEqualTo(LogisticsCompany.SHENTONG);
    }

    @Test
    @DisplayName("通过代码查找物流公司时，代码不区分大小写")
    void shouldFindLogisticsCompanyByCodeCaseInsensitive() {
        assertThat(LogisticsCompany.fromCode("sf")).isEqualTo(LogisticsCompany.SHUNFENG);
        assertThat(LogisticsCompany.fromCode("Sf")).isEqualTo(LogisticsCompany.SHUNFENG);
        assertThat(LogisticsCompany.fromCode("zto")).isEqualTo(LogisticsCompany.ZHONGTONG);
    }

    @Test
    @DisplayName("当代码不存在时，fromCode应该抛出异常")
    void shouldThrowExceptionWhenCodeNotFound() {
        assertThatThrownBy(() -> LogisticsCompany.fromCode("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown logistics company code: INVALID");
    }
}
