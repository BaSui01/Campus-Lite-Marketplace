package com.campus.marketplace.common.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("敏感词过滤器测试")
class SensitiveWordFilterTest {

    private SensitiveWordFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SensitiveWordFilter();
        filter.init();
    }

    @Test
    @DisplayName("初始化后默认敏感词可以被识别")
    void contains_shouldDetectDefaultWords() {
        assertThat(filter.contains("这个卖家是骗子")).isTrue();
        assertThat(filter.contains("普通文本")).isFalse();
    }

    @Test
    @DisplayName("过滤文本会将敏感词替换为星号")
    void filter_shouldMaskSensitiveWords() {
        String result = filter.filter("你这个骗子太垃圾了");
        assertThat(result).isEqualTo("你这个**太**了");
    }

    @Test
    @DisplayName("可以提取文本中的全部敏感词")
    void getSensitiveWords_shouldReturnAllMatches() {
        Set<String> words = filter.getSensitiveWords("骗子太多了，都是垃圾");
        assertThat(words).containsExactlyInAnyOrder("骗子", "垃圾");
    }

    @Test
    @DisplayName("自定义敏感词可在清空后重新加载")
    void clearAndAdd_shouldReloadWords() {
        filter.clear();
        assertFalse(filter.contains("骗子"));

        filter.addSensitiveWords(Set.of("违规", "套现"));

        assertThat(filter.contains("存在违规操作")).isTrue();
        assertThat(filter.filter("请勿套现")).isEqualTo("请勿**");
    }
}
