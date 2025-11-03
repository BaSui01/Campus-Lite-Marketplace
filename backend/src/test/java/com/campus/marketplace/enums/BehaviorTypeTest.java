package com.campus.marketplace.enums;

import com.campus.marketplace.common.enums.BehaviorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用户行为类型枚举测试
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@DisplayName("用户行为类型枚举测试")
class BehaviorTypeTest {

    @Test
    @DisplayName("行为类型枚举应该包含所有必需类型")
    void shouldContainAllRequiredBehaviorTypes() {
        // 验证枚举值完整性
        assertThat(BehaviorType.values()).hasSize(8);
        assertThat(BehaviorType.VIEW).isNotNull();
        assertThat(BehaviorType.SEARCH).isNotNull();
        assertThat(BehaviorType.FAVORITE).isNotNull();
        assertThat(BehaviorType.PURCHASE).isNotNull();
        assertThat(BehaviorType.CLICK).isNotNull();
        assertThat(BehaviorType.SHARE).isNotNull();
        assertThat(BehaviorType.COMMENT).isNotNull();
        assertThat(BehaviorType.LIKE).isNotNull();
    }

    @Test
    @DisplayName("行为类型应该有正确的显示名称")
    void shouldHaveCorrectDisplayNames() {
        assertThat(BehaviorType.VIEW.getDisplayName()).isEqualTo("浏览");
        assertThat(BehaviorType.SEARCH.getDisplayName()).isEqualTo("搜索");
        assertThat(BehaviorType.FAVORITE.getDisplayName()).isEqualTo("收藏");
        assertThat(BehaviorType.PURCHASE.getDisplayName()).isEqualTo("购买");
        assertThat(BehaviorType.CLICK.getDisplayName()).isEqualTo("点击");
        assertThat(BehaviorType.SHARE.getDisplayName()).isEqualTo("分享");
        assertThat(BehaviorType.COMMENT.getDisplayName()).isEqualTo("评论");
        assertThat(BehaviorType.LIKE.getDisplayName()).isEqualTo("点赞");
    }

    @Test
    @DisplayName("行为类型应该有正确的权重值")
    void shouldHaveCorrectWeightValues() {
        // 验证不同行为类型的权重
        assertThat(BehaviorType.PURCHASE.getWeight()).isEqualTo(10);  // 购买权重最高
        assertThat(BehaviorType.FAVORITE.getWeight()).isEqualTo(5);   // 收藏次之
        assertThat(BehaviorType.VIEW.getWeight()).isEqualTo(1);       // 浏览权重最低
        assertThat(BehaviorType.SEARCH.getWeight()).isEqualTo(2);     // 搜索权重
        assertThat(BehaviorType.CLICK.getWeight()).isEqualTo(2);      // 点击权重
        assertThat(BehaviorType.SHARE.getWeight()).isEqualTo(3);      // 分享权重
        assertThat(BehaviorType.COMMENT.getWeight()).isEqualTo(3);    // 评论权重
        assertThat(BehaviorType.LIKE.getWeight()).isEqualTo(1);       // 点赞权重
    }

    @Test
    @DisplayName("应该能够根据行为类型判断是否为高价值行为")
    void shouldDetermineHighValueBehavior() {
        // 高价值行为：购买、收藏
        assertThat(BehaviorType.PURCHASE.isHighValue()).isTrue();
        assertThat(BehaviorType.FAVORITE.isHighValue()).isTrue();

        // 中等价值行为：分享、评论
        assertThat(BehaviorType.SHARE.isHighValue()).isFalse();
        assertThat(BehaviorType.COMMENT.isHighValue()).isFalse();

        // 低价值行为：浏览、点击、搜索、点赞
        assertThat(BehaviorType.VIEW.isHighValue()).isFalse();
        assertThat(BehaviorType.CLICK.isHighValue()).isFalse();
        assertThat(BehaviorType.SEARCH.isHighValue()).isFalse();
        assertThat(BehaviorType.LIKE.isHighValue()).isFalse();
    }
}
