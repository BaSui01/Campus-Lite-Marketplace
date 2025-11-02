/**
 * 文本高亮工具 - 让搜索结果更清晰！✨
 * @author BaSui 😎
 * @description 将关键词在文本中高亮显示
 */

/**
 * 高亮选项
 */
export interface HighlightOptions {
  /**
   * 高亮类名
   * @default 'highlight'
   */
  className?: string;

  /**
   * 是否区分大小写
   * @default false
   */
  caseSensitive?: boolean;

  /**
   * 是否全词匹配
   * @default false
   */
  wholeWord?: boolean;
}

/**
 * 将关键词在文本中高亮显示
 *
 * @param text 原始文本
 * @param keyword 关键词（支持空格分隔多个关键词）
 * @param options 高亮选项
 * @returns 带有高亮标记的 HTML 字符串
 *
 * @example
 * ```tsx
 * const text = "这是一个二手电脑，性能很好！";
 * const highlighted = highlightText(text, "电脑", { className: 'highlight' });
 * // 输出: "这是一个二手<span class="highlight">电脑</span>，性能很好！"
 *
 * // 使用 dangerouslySetInnerHTML 渲染
 * <div dangerouslySetInnerHTML={{ __html: highlighted }} />
 * ```
 */
export function highlightText(
  text: string,
  keyword: string,
  options: HighlightOptions = {}
): string {
  if (!text || !keyword) {
    return text;
  }

  const {
    className = 'highlight',
    caseSensitive = false,
    wholeWord = false,
  } = options;

  // 分割多个关键词（空格分隔）
  const keywords = keyword
    .trim()
    .split(/\s+/)
    .filter((k) => k.length > 0);

  if (keywords.length === 0) {
    return text;
  }

  // 转义正则特殊字符
  const escapeRegex = (str: string) => {
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  };

  // 构建正则表达式
  const regexPattern = keywords
    .map((k) => {
      const escaped = escapeRegex(k);
      return wholeWord ? `\\b${escaped}\\b` : escaped;
    })
    .join('|');

  const flags = caseSensitive ? 'g' : 'gi';
  const regex = new RegExp(regexPattern, flags);

  // 替换匹配的关键词
  return text.replace(regex, (match) => {
    return `<span class="${className}">${match}</span>`;
  });
}

/**
 * 默认导出
 */
export default {
  highlightText,
};
