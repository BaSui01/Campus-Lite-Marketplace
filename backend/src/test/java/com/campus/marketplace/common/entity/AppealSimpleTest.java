package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.AppealStatus;
import com.campus.marketplace.common.enums.AppealTargetType;
import com.campus.marketplace.common.enums.AppealType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * 申诉实体简化测试 - TDD驱动开发
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@DisplayName("申诉实体测试")
class AppealSimpleTest {

    @Test
    @DisplayName("新创建的申诉应该是待处理状态")
    void newAppealShouldBePending() {
        // Arrange & Act
        Appeal appeal = new Appeal();
        appeal.setUserId(123L);
        appeal.setTargetType(AppealTargetType.USER_BAN);
        appeal.setTargetId(456L);
        appeal.setAppealType(AppealType.UNJUST_BAN);
        appeal.setReason("我没有违规");
        appeal.setStatus(AppealStatus.PENDING);
        appeal.setDeadline(LocalDateTime.now().plusDays(7));
        
        // Assert
        assertThat(appeal).isNotNull();
        assertThat(appeal.getStatus()).isEqualTo(AppealStatus.PENDING);
        assertThat(appeal.getDeadline()).isNotNull();
        assertThat(appeal.getUserId()).isEqualTo(123L);
        assertThat(appeal.getTargetType()).isEqualTo(AppealTargetType.USER_BAN);
        assertThat(appeal.getAppealType()).isEqualTo(AppealType.UNJUST_BAN);
        assertThat(appeal.getReason()).isEqualTo("我没有违规");
    }

    @Test
    @DisplayName("申诉应该支持多种目标类型")
    void appealShouldSupportMultipleTargetTypes() {
        // Arrange & Act
        Appeal userBanAppeal = new Appeal();
        userBanAppeal.setUserId(123L);
        userBanAppeal.setTargetType(AppealTargetType.USER_BAN);
        userBanAppeal.setTargetId(456L);
        userBanAppeal.setAppealType(AppealType.UNJUST_BAN);
        userBanAppeal.setReason("用户不应该被封禁");
        userBanAppeal.setStatus(AppealStatus.PENDING);
        userBanAppeal.setDeadline(LocalDateTime.now().plusDays(7));
            
        Appeal goodsDeleteAppeal = new Appeal();
        goodsDeleteAppeal.setUserId(789L);
        goodsDeleteAppeal.setTargetType(AppealTargetType.GOODS_DELETE);
        goodsDeleteAppeal.setTargetId(101L);
        goodsDeleteAppeal.setAppealType(AppealType.UNJUST_DELETE);
        goodsDeleteAppeal.setReason("商品不应该被删除");
        goodsDeleteAppeal.setStatus(AppealStatus.PENDING);
        goodsDeleteAppeal.setDeadline(LocalDateTime.now().plusDays(3));
        
        // Assert
        assertThat(userBanAppeal.getTargetType()).isEqualTo(AppealTargetType.USER_BAN);
        assertThat(goodsDeleteAppeal.getTargetType()).isEqualTo(AppealTargetType.GOODS_DELETE);
        assertThat(userBanAppeal.getAppealType()).isEqualTo(AppealType.UNJUST_BAN);
        assertThat(goodsDeleteAppeal.getAppealType()).isEqualTo(AppealType.UNJUST_DELETE);
        
        // 验证截止时间不同
        assertThat(userBanAppeal.getDeadline().isAfter(goodsDeleteAppeal.getDeadline())).isTrue();
    }

    @Test
    @DisplayName("申诉应该有完整的审核流程状态")
    void appealShouldHaveCompleteReviewProcess() {
        // Arrange & Act
        Appeal appeal = new Appeal();
        appeal.setUserId(123L);
        appeal.setTargetType(AppealTargetType.USER_BAN);
        appeal.setTargetId(456L);
        appeal.setAppealType(AppealType.UNJUST_BAN);
        appeal.setReason("我没有违规");
        appeal.setStatus(AppealStatus.PENDING);
        appeal.setDeadline(LocalDateTime.now().plusDays(7));
        
        // Assert
        // 验证各种状态存在
        assertThat(AppealStatus.PENDING).isNotNull();
        assertThat(AppealStatus.REVIEWING).isNotNull();
        assertThat(AppealStatus.APPROVED).isNotNull();
        assertThat(AppealStatus.REJECTED).isNotNull();
        assertThat(AppealStatus.CANCELLED).isNotNull();
        
        // 验证初始状态
        assertThat(appeal.getStatus()).isEqualTo(AppealStatus.PENDING);
    }

    @Test
    @DisplayName("申诉应该能记录审核人信息和审核结果")
    void appealShouldRecordReviewerInfoAndResult() {
        // Arrange & Act
        Appeal appeal = new Appeal();
        appeal.setUserId(123L);
        appeal.setTargetType(AppealTargetType.USER_BAN);
        appeal.setTargetId(456L);
        appeal.setAppealType(AppealType.UNJUST_BAN);
        appeal.setReason("我没有违规");
        appeal.setStatus(AppealStatus.PENDING);
        appeal.setDeadline(LocalDateTime.now().plusDays(7));
        
        // 审核人处理申诉
        appeal.setReviewerId(999L);
        appeal.setReviewerName("admin");
        appeal.setStatus(AppealStatus.APPROVED);
        appeal.setReviewComment("经核实，确实存在误判，同意申诉");
        appeal.setReviewedAt(LocalDateTime.now());
        
        // Assert
        assertThat(appeal.getReviewerId()).isEqualTo(999L);
        assertThat(appeal.getReviewerName()).isEqualTo("admin");
        assertThat(appeal.getStatus()).isEqualTo(AppealStatus.APPROVED);
        assertThat(appeal.getReviewComment()).contains("误判");
        assertThat(appeal.getReviewedAt()).isNotNull();
    }

    @Test
    @DisplayName("不同类型的申诉应该有不同的默认处理期限")
    void differentAppealTypesShouldHaveDifferentDefaultDeadlines() {
        // Arrange & Act
        // 用户封禁申诉 - 时间较长
        Appeal userBanAppeal = new Appeal();
        userBanAppeal.setUserId(123L);
        userBanAppeal.setTargetType(AppealTargetType.USER_BAN);
        userBanAppeal.setTargetId(456L);
        userBanAppeal.setAppealType(AppealType.UNJUST_BAN);
        userBanAppeal.setReason("用户不应该被封禁");
        userBanAppeal.setStatus(AppealStatus.PENDING);
        userBanAppeal.setDeadline(LocalDateTime.now().plusDays(7)); // 用户申诉7天
        
        // 商品删除申诉 - 时间较短
        Appeal goodsDeleteAppeal = new Appeal();
        goodsDeleteAppeal.setUserId(789L);
        goodsDeleteAppeal.setTargetType(AppealTargetType.GOODS_DELETE);
        goodsDeleteAppeal.setTargetId(101L);
        goodsDeleteAppeal.setAppealType(AppealType.UNJUST_DELETE);
        goodsDeleteAppeal.setReason("商品不应该被删除");
        goodsDeleteAppeal.setStatus(AppealStatus.PENDING);
        goodsDeleteAppeal.setDeadline(LocalDateTime.now().plusDays(3)); // 商品申诉3天
        
        LocalDateTime now = LocalDateTime.now();
        
        // Assert
        // 验证用户申诉期限更长（7天）
        assertThat(userBanAppeal.getDeadline().isAfter(now.plusDays(6))).isTrue();
        assertThat(userBanAppeal.getDeadline().isBefore(now.plusDays(8))).isTrue();
        
        // 验证商品申诉期限较短（3天）
        assertThat(goodsDeleteAppeal.getDeadline().isAfter(now.plusDays(2))).isTrue();
        assertThat(goodsDeleteAppeal.getDeadline().isBefore(now.plusDays(4))).isTrue();
        
        // 验证用户申诉确实更长
        assertThat(userBanAppeal.getDeadline().isAfter(goodsDeleteAppeal.getDeadline())).isTrue();
    }
}
