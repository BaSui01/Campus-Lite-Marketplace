package com.campus.marketplace.enums;

import com.campus.marketplace.common.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 纠纷仲裁系统枚举类型测试
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@DisplayName("纠纷仲裁系统枚举测试")
class DisputeEnumsTest {

    @Test
    @DisplayName("应该包含所有纠纷状态枚举")
    void shouldContainAllDisputeStatuses() {
        // Act
        Set<String> statuses = Arrays.stream(DisputeStatus.values())
            .map(DisputeStatus::name)
            .collect(Collectors.toSet());

        // Assert
        assertThat(statuses).containsExactlyInAnyOrder(
            "SUBMITTED",           // 已提交
            "NEGOTIATING",         // 协商中
            "PENDING_ARBITRATION", // 待仲裁
            "ARBITRATING",         // 仲裁中
            "COMPLETED",           // 已完成
            "CLOSED"               // 已关闭
        );
    }

    @Test
    @DisplayName("应该包含所有纠纷类型枚举")
    void shouldContainAllDisputeTypes() {
        // Act
        Set<String> types = Arrays.stream(DisputeType.values())
            .map(DisputeType::name)
            .collect(Collectors.toSet());

        // Assert
        assertThat(types).containsExactlyInAnyOrder(
            "GOODS_MISMATCH",      // 商品不符
            "QUALITY_ISSUE",       // 质量问题
            "LOGISTICS_DELAY",     // 物流延误
            "FALSE_ADVERTISING",   // 虚假宣传
            "OTHER"                // 其他
        );
    }

    @Test
    @DisplayName("应该包含所有仲裁结果枚举")
    void shouldContainAllArbitrationResults() {
        // Act
        Set<String> results = Arrays.stream(ArbitrationResult.values())
            .map(ArbitrationResult::name)
            .collect(Collectors.toSet());

        // Assert
        assertThat(results).containsExactlyInAnyOrder(
            "FULL_REFUND",         // 全额退款
            "PARTIAL_REFUND",      // 部分退款
            "REJECT",              // 驳回申请
            "NEED_MORE_EVIDENCE"   // 需补充证据
        );
    }

    @Test
    @DisplayName("应该包含所有纠纷角色枚举")
    void shouldContainAllDisputeRoles() {
        // Act
        Set<String> roles = Arrays.stream(DisputeRole.values())
            .map(DisputeRole::name)
            .collect(Collectors.toSet());

        // Assert
        assertThat(roles).containsExactlyInAnyOrder(
            "BUYER",  // 买家
            "SELLER"  // 卖家
        );
    }

    @Test
    @DisplayName("应该包含所有证据类型枚举")
    void shouldContainAllEvidenceTypes() {
        // Act
        Set<String> types = Arrays.stream(EvidenceType.values())
            .map(EvidenceType::name)
            .collect(Collectors.toSet());

        // Assert
        assertThat(types).containsExactlyInAnyOrder(
            "IMAGE",         // 图片
            "VIDEO",         // 视频
            "CHAT_RECORD"    // 聊天记录截图
        );
    }

    @Test
    @DisplayName("应该包含所有证据有效性枚举")
    void shouldContainAllEvidenceValidities() {
        // Act
        Set<String> validities = Arrays.stream(EvidenceValidity.values())
            .map(EvidenceValidity::name)
            .collect(Collectors.toSet());

        // Assert
        assertThat(validities).containsExactlyInAnyOrder(
            "VALID",      // 有效
            "INVALID",    // 无效
            "DOUBTFUL"    // 存疑
        );
    }

    @Test
    @DisplayName("应该包含所有协商消息类型枚举")
    void shouldContainAllNegotiationMessageTypes() {
        // Act
        Set<String> types = Arrays.stream(NegotiationMessageType.values())
            .map(NegotiationMessageType::name)
            .collect(Collectors.toSet());

        // Assert
        assertThat(types).containsExactlyInAnyOrder(
            "TEXT",      // 文字消息
            "PROPOSAL"   // 解决方案
        );
    }

    @Test
    @DisplayName("应该包含所有方案状态枚举")
    void shouldContainAllProposalStatuses() {
        // Act
        Set<String> statuses = Arrays.stream(ProposalStatus.values())
            .map(ProposalStatus::name)
            .collect(Collectors.toSet());

        // Assert
        assertThat(statuses).containsExactlyInAnyOrder(
            "PENDING",   // 待响应
            "ACCEPTED",  // 已接受
            "REJECTED"   // 已拒绝
        );
    }

    @Test
    @DisplayName("每个枚举都应该有中文描述")
    void everyEnumShouldHaveChineseDescription() {
        // Assert - DisputeStatus
        assertThat(DisputeStatus.SUBMITTED.getDescription()).isEqualTo("已提交");
        assertThat(DisputeStatus.NEGOTIATING.getDescription()).isEqualTo("协商中");
        assertThat(DisputeStatus.COMPLETED.getDescription()).isEqualTo("已完成");

        // Assert - DisputeType
        assertThat(DisputeType.GOODS_MISMATCH.getDescription()).isEqualTo("商品不符");
        assertThat(DisputeType.QUALITY_ISSUE.getDescription()).isEqualTo("质量问题");

        // Assert - ArbitrationResult
        assertThat(ArbitrationResult.FULL_REFUND.getDescription()).isEqualTo("全额退款");
        assertThat(ArbitrationResult.REJECT.getDescription()).isEqualTo("驳回申请");

        // Assert - DisputeRole
        assertThat(DisputeRole.BUYER.getDescription()).isEqualTo("买家");
        assertThat(DisputeRole.SELLER.getDescription()).isEqualTo("卖家");
    }
}
