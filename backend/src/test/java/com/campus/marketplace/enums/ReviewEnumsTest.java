package com.campus.marketplace.enums;

import com.campus.marketplace.common.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Review相关枚举测试
 *
 * @author BaSui 😎 - 枚举测试，确保每个值都有描述！
 * @since 2025-11-03
 */
@DisplayName("Review Enums Test")
class ReviewEnumsTest {

    @Test
    @DisplayName("SentimentType - 情感类型枚举测试")
    void testSentimentType() {
        // 验证枚举值数量
        assertEquals(3, SentimentType.values().length);

        // 验证每个枚举值的描述
        assertEquals("积极", SentimentType.POSITIVE.getDescription());
        assertEquals("中性", SentimentType.NEUTRAL.getDescription());
        assertEquals("消极", SentimentType.NEGATIVE.getDescription());

        // 验证valueOf正常工作
        assertEquals(SentimentType.POSITIVE, SentimentType.valueOf("POSITIVE"));
        assertEquals(SentimentType.NEUTRAL, SentimentType.valueOf("NEUTRAL"));
        assertEquals(SentimentType.NEGATIVE, SentimentType.valueOf("NEGATIVE"));
    }

    @Test
    @DisplayName("MediaType - 媒体类型枚举测试")
    void testMediaType() {
        // 验证枚举值数量
        assertEquals(2, MediaType.values().length);

        // 验证每个枚举值的描述
        assertEquals("图片", MediaType.IMAGE.getDescription());
        assertEquals("视频", MediaType.VIDEO.getDescription());

        // 验证valueOf正常工作
        assertEquals(MediaType.IMAGE, MediaType.valueOf("IMAGE"));
        assertEquals(MediaType.VIDEO, MediaType.valueOf("VIDEO"));
    }

    @Test
    @DisplayName("ReplyType - 回复类型枚举测试")
    void testReplyType() {
        // 验证枚举值数量
        assertEquals(2, ReplyType.values().length);

        // 验证每个枚举值的描述
        assertEquals("卖家回复", ReplyType.SELLER_REPLY.getDescription());
        assertEquals("管理员回复", ReplyType.ADMIN_REPLY.getDescription());

        // 验证valueOf正常工作
        assertEquals(ReplyType.SELLER_REPLY, ReplyType.valueOf("SELLER_REPLY"));
        assertEquals(ReplyType.ADMIN_REPLY, ReplyType.valueOf("ADMIN_REPLY"));
    }

    @Test
    @DisplayName("TagType - 标签类型枚举测试")
    void testTagType() {
        // 验证枚举值数量
        assertEquals(5, TagType.values().length);

        // 验证每个枚举值的描述
        assertEquals("物品质量", TagType.QUALITY.getDescription());
        assertEquals("服务态度", TagType.SERVICE.getDescription());
        assertEquals("物流速度", TagType.DELIVERY.getDescription());
        assertEquals("性价比", TagType.PRICE.getDescription());
        assertEquals("其他", TagType.OTHER.getDescription());

        // 验证valueOf正常工作
        assertEquals(TagType.QUALITY, TagType.valueOf("QUALITY"));
        assertEquals(TagType.SERVICE, TagType.valueOf("SERVICE"));
        assertEquals(TagType.DELIVERY, TagType.valueOf("DELIVERY"));
        assertEquals(TagType.PRICE, TagType.valueOf("PRICE"));
        assertEquals(TagType.OTHER, TagType.valueOf("OTHER"));
    }

    @Test
    @DisplayName("TagSource - 标签来源枚举测试")
    void testTagSource() {
        // 验证枚举值数量
        assertEquals(2, TagSource.values().length);

        // 验证每个枚举值的描述
        assertEquals("系统提取", TagSource.SYSTEM.getDescription());
        assertEquals("用户输入", TagSource.USER_INPUT.getDescription());

        // 验证valueOf正常工作
        assertEquals(TagSource.SYSTEM, TagSource.valueOf("SYSTEM"));
        assertEquals(TagSource.USER_INPUT, TagSource.valueOf("USER_INPUT"));
    }

    @Test
    @DisplayName("ReviewStatus - 评价状态枚举测试")
    void testReviewStatus() {
        // 验证枚举值数量
        assertEquals(3, ReviewStatus.values().length);

        // 验证每个枚举值的描述
        assertEquals("正常", ReviewStatus.NORMAL.getDescription());
        assertEquals("已隐藏", ReviewStatus.HIDDEN.getDescription());
        assertEquals("已举报", ReviewStatus.REPORTED.getDescription());

        // 验证valueOf正常工作
        assertEquals(ReviewStatus.NORMAL, ReviewStatus.valueOf("NORMAL"));
        assertEquals(ReviewStatus.HIDDEN, ReviewStatus.valueOf("HIDDEN"));
        assertEquals(ReviewStatus.REPORTED, ReviewStatus.valueOf("REPORTED"));
    }

    @Test
    @DisplayName("边界测试 - valueOf抛出异常")
    void testValueOfThrowsException() {
        // 验证不存在的枚举值会抛出异常
        assertThrows(IllegalArgumentException.class, () -> SentimentType.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> MediaType.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> ReplyType.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> TagType.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> TagSource.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> ReviewStatus.valueOf("INVALID"));
    }

    @Test
    @DisplayName("边界测试 - null值处理")
    void testNullValueOf() {
        // 验证null值会抛出异常
        assertThrows(NullPointerException.class, () -> SentimentType.valueOf(null));
        assertThrows(NullPointerException.class, () -> MediaType.valueOf(null));
        assertThrows(NullPointerException.class, () -> ReplyType.valueOf(null));
        assertThrows(NullPointerException.class, () -> TagType.valueOf(null));
        assertThrows(NullPointerException.class, () -> TagSource.valueOf(null));
        assertThrows(NullPointerException.class, () -> ReviewStatus.valueOf(null));
    }
}
