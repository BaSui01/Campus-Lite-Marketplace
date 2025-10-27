package com.campus.marketplace.common.utils;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 敏感词过滤器
 * 
 * 使用 DFA（确定有限状态自动机）算法实现高效的敏感词过滤
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Slf4j
@Component
public class SensitiveWordFilter {

    /**
     * 敏感词库（DFA 树）
     */
    private final Map<String, Object> sensitiveWordMap = new HashMap<>();

    /**
     * 是否为敏感词结尾的标识
     */
    private static final String IS_END = "isEnd";

    /**
     * 替换字符
     */
    private static final char REPLACE_CHAR = '*';

    /**
     * 初始化敏感词库
     */
    @PostConstruct
    public void init() {
        // 这里可以从数据库或文件加载敏感词
        // 为了演示，先添加一些示例敏感词
        Set<String> sensitiveWords = new HashSet<>();
        sensitiveWords.add("fuck");
        sensitiveWords.add("shit");
        sensitiveWords.add("傻逼");
        sensitiveWords.add("垃圾");
        sensitiveWords.add("骗子");
        sensitiveWords.add("诈骗");
        
        addSensitiveWords(sensitiveWords);
        log.info("敏感词库初始化完成，共加载 {} 个敏感词", sensitiveWords.size());
    }

    /**
     * 添加敏感词到词库
     */
    public void addSensitiveWords(Set<String> words) {
        for (String word : words) {
            addWord(word);
        }
    }

    /**
     * 添加单个敏感词
     */
    private void addWord(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }

        Map<String, Object> currentMap = sensitiveWordMap;
        
        for (int i = 0; i < word.length(); i++) {
            String key = String.valueOf(word.charAt(i));
            
            // 如果当前字符不存在，创建新节点
            Map<String, Object> nextMap = (Map<String, Object>) currentMap.get(key);
            if (nextMap == null) {
                nextMap = new HashMap<>();
                currentMap.put(key, nextMap);
            }
            
            currentMap = nextMap;
            
            // 最后一个字符，标记为结尾
            if (i == word.length() - 1) {
                currentMap.put(IS_END, true);
            }
        }
    }

    /**
     * 检查文本是否包含敏感词
     */
    public boolean contains(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        for (int i = 0; i < text.length(); i++) {
            int length = checkSensitiveWord(text, i);
            if (length > 0) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 过滤敏感词（替换为 ***）
     */
    public String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder(text);
        
        for (int i = 0; i < result.length(); i++) {
            int length = checkSensitiveWord(result.toString(), i);
            if (length > 0) {
                // 替换敏感词为 *
                for (int j = 0; j < length; j++) {
                    result.setCharAt(i + j, REPLACE_CHAR);
                }
                i += length - 1;
            }
        }
        
        return result.toString();
    }

    /**
     * 获取文本中的所有敏感词
     */
    public Set<String> getSensitiveWords(String text) {
        Set<String> words = new HashSet<>();
        
        if (text == null || text.isEmpty()) {
            return words;
        }

        for (int i = 0; i < text.length(); i++) {
            int length = checkSensitiveWord(text, i);
            if (length > 0) {
                words.add(text.substring(i, i + length));
                i += length - 1;
            }
        }
        
        return words;
    }

    /**
     * 检查从指定位置开始是否存在敏感词
     * 
     * @param text 文本
     * @param beginIndex 开始位置
     * @return 敏感词长度，0 表示不存在
     */
    private int checkSensitiveWord(String text, int beginIndex) {
        boolean isEnd = false;
        int matchLength = 0;
        
        Map<String, Object> currentMap = sensitiveWordMap;
        
        for (int i = beginIndex; i < text.length(); i++) {
            String key = String.valueOf(text.charAt(i));
            
            // 获取下一个节点
            currentMap = (Map<String, Object>) currentMap.get(key);
            
            if (currentMap == null) {
                // 没有匹配到，退出
                break;
            }
            
            matchLength++;
            
            // 检查是否到达敏感词结尾
            if (Boolean.TRUE.equals(currentMap.get(IS_END))) {
                isEnd = true;
                break;
            }
        }
        
        // 如果没有到达结尾，说明不是完整的敏感词
        if (!isEnd) {
            matchLength = 0;
        }
        
        return matchLength;
    }

    /**
     * 清空敏感词库
     */
    public void clear() {
        sensitiveWordMap.clear();
        log.info("敏感词库已清空");
    }
}
