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
     * DFA 根节点（类型安全实现，避免不受检转换）
     */
    private final Node root = new Node();

    private static final class Node {
        final Map<Character, Node> children = new HashMap<>();
        boolean isEnd;
    }

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
        Node curr = root;
        
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            Node next = curr.children.get(ch);
            if (next == null) {
                next = new Node();
                curr.children.put(ch, next);
            }
            curr = next;
            if (i == word.length() - 1) curr.isEnd = true;
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
        boolean end = false;
        int len = 0;

        Node curr = root;
        for (int i = beginIndex; i < text.length(); i++) {
            char ch = text.charAt(i);
            Node next = curr.children.get(ch);
            if (next == null) break;
            len++;
            if (next.isEnd) { end = true; break; }
            curr = next;
        }

        return end ? len : 0;
    }

    /**
     * 清空敏感词库
     */
    public void clear() {
        root.children.clear();
        root.isEnd = false;
        log.info("敏感词库已清空");
    }
}
