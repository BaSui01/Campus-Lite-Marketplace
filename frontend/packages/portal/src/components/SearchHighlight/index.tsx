/**
 * 搜索高亮组件 - 文本高亮专家！✨
 *
 * @author BaSui 😎
 * @description 在文本中高亮显示搜索关键词，支持多个关键词和不同的高亮样式
 * @date 2025-11-07
 */

import React from 'react';
import { searchService } from '@/services/search';

/**
 * 搜索高亮组件属性
 */
export interface SearchHighlightProps {
  /** 原始文本 */
  text: string;
  /** 搜索关键词 */
  keyword: string;
  /** 高亮样式类名 */
  highlightClassName?: string;
  /** 普通文本样式类名 */
  textClassName?: string;
  /** 是否启用智能高亮 */
  smartHighlight?: boolean;
  /** 最大显示长度 */
  maxLength?: number;
  /** 是否截断并显示省略号 */
  showEllipsis?: boolean;
  /** 自定义高亮渲染函数 */
  customHighlightRenderer?: (text: string, isHighlight: boolean, index: number) => React.ReactNode;
}

/**
 * 搜索高亮组件
 */
export const SearchHighlight: React.FC<SearchHighlightProps> = ({
  text,
  keyword,
  highlightClassName = 'bg-yellow-200 text-yellow-900 font-medium px-1 py-0.5 rounded',
  textClassName = '',
  smartHighlight = true,
  maxLength,
  showEllipsis = true,
  customHighlightRenderer,
}) => {
  // 如果没有关键词或文本为空，直接返回原文本
  if (!keyword.trim() || !text) {
    const displayText = maxLength && text.length > maxLength ?
      (showEllipsis ? text.substring(0, maxLength) + '...' : text.substring(0, maxLength)) :
      text;

    return <span className={textClassName}>{displayText}</span>;
  }

  // 使用搜索服务获取高亮信息
  const highlights = searchService.highlightSearchText(text, keyword);

  // 如果没有匹配项，返回原文本
  if (!highlights.some(h => h.isMatch)) {
    const displayText = maxLength && text.length > maxLength ?
      (showEllipsis ? text.substring(0, maxLength) + '...' : text.substring(0, maxLength)) :
      text;

    return <span className={textClassName}>{displayText}</span>;
  }

  // 智能截断：优先显示包含关键词的部分
  let processedHighlights = highlights;
  if (maxLength && text.length > maxLength) {
    processedHighlights = smartHighlight ?
      smartTruncateHighlights(highlights, maxLength, showEllipsis) :
      simpleTruncateHighlights(highlights, maxLength, showEllipsis);
  }

  // 渲染高亮文本
  return (
    <span className={textClassName}>
      {processedHighlights.map((highlight, index) => {
        if (customHighlightRenderer) {
          return customHighlightRenderer(highlight.text, highlight.isMatch, index);
        }

        return (
          <span
            key={index}
            className={highlight.isMatch ? highlightClassName : ''}
          >
            {highlight.text}
          </span>
        );
      })}
    </span>
  );
};

/**
 * 简单截断高亮片段
 */
function simpleTruncateHighlights(
  highlights: Array<{ text: string; isMatch: boolean }>,
  maxLength: number,
  showEllipsis: boolean
): Array<{ text: string; isMatch: boolean }> {
  let currentLength = 0;
  const result: Array<{ text: string; isMatch: boolean }> = [];

  for (const highlight of highlights) {
    if (currentLength >= maxLength) {
      break;
    }

    const remainingLength = maxLength - currentLength;
    if (highlight.text.length <= remainingLength) {
      result.push(highlight);
      currentLength += highlight.text.length;
    } else {
      result.push({
        text: highlight.text.substring(0, remainingLength),
        isMatch: highlight.isMatch,
      });
      break;
    }
  }

  // 添加省略号
  if (showEllipsis && result.length < highlights.length) {
    const lastItem = result[result.length - 1];
    lastItem.text += '...';
  }

  return result;
}

/**
 * 智能截断高亮片段：优先显示包含关键词的部分
 */
function smartTruncateHighlights(
  highlights: Array<{ text: string; isMatch: boolean }>,
  maxLength: number,
  showEllipsis: boolean
): Array<{ text: string; isMatch: boolean }> {
  // 找到第一个匹配项
  const firstMatchIndex = highlights.findIndex(h => h.isMatch);
  if (firstMatchIndex === -1) {
    return simpleTruncateHighlights(highlights, maxLength, showEllipsis);
  }

  // 计算显示范围，优先显示匹配项周围的文本
  const halfLength = Math.floor(maxLength / 2);
  let startIndex = Math.max(0, firstMatchIndex - 2);
  let endIndex = startIndex;
  let currentLength = 0;

  // 扩展范围以适应最大长度
  while (endIndex < highlights.length && currentLength < maxLength) {
    currentLength += highlights[endIndex].text.length;
    if (currentLength >= maxLength) {
      break;
    }
    endIndex++;
  }

  // 如果还有空间，尝试向后扩展
  while (startIndex > 0 && endIndex < highlights.length && currentLength < maxLength) {
    const prevLength = highlights[startIndex - 1].text.length;
    if (currentLength + prevLength > maxLength) {
      break;
    }
    startIndex--;
    currentLength += prevLength;
  }

  const result = highlights.slice(startIndex, endIndex + 1);

  // 添加省略号
  if (showEllipsis) {
    if (startIndex > 0) {
      result[0].text = '...' + result[0].text;
    }
    if (endIndex < highlights.length - 1) {
      const lastItem = result[result.length - 1];
      lastItem.text = lastItem.text + '...';
    }
  }

  return result;
}

/**
 * 多关键词高亮组件
 */
export interface MultiKeywordHighlightProps {
  text: string;
  keywords: string[];
  highlightClassName?: string;
  textClassName?: string;
  differentColors?: boolean;
}

export const MultiKeywordHighlight: React.FC<MultiKeywordHighlightProps> = ({
  text,
  keywords,
  highlightClassName = 'bg-yellow-200 text-yellow-900 font-medium px-1 py-0.5 rounded',
  textClassName = '',
  differentColors = false,
}) => {
  if (!keywords.length || !text) {
    return <span className={textClassName}>{text}</span>;
  }

  // 定义不同的高亮颜色
  const colorClasses = [
    'bg-yellow-200 text-yellow-900 font-medium px-1 py-0.5 rounded',
    'bg-blue-200 text-blue-900 font-medium px-1 py-0.5 rounded',
    'bg-green-200 text-green-900 font-medium px-1 py-0.5 rounded',
    'bg-purple-200 text-purple-900 font-medium px-1 py-0.5 rounded',
    'bg-pink-200 text-pink-900 font-medium px-1 py-0.5 rounded',
    'bg-indigo-200 text-indigo-900 font-medium px-1 py-0.5 rounded',
  ];

  // 为每个关键词获取高亮信息
  let combinedHighlights = Array<{ text: string; isMatch: boolean; keywordIndex?: number }>(
    [{ text, isMatch: false }]
  );

  keywords.forEach((keyword, keywordIndex) => {
    if (!keyword.trim()) return;

    const newHighlights: Array<{ text: string; isMatch: boolean; keywordIndex?: number }> = [];

    combinedHighlights.forEach(highlight => {
      if (highlight.isMatch) {
        newHighlights.push(highlight);
        return;
      }

      const currentHighlights = searchService.highlightSearchText(highlight.text, keyword);
      currentHighlights.forEach(h => {
        newHighlights.push({
          ...h,
          keywordIndex: h.isMatch ? keywordIndex : undefined,
        });
      });
    });

    combinedHighlights = newHighlights;
  });

  // 渲染高亮文本
  return (
    <span className={textClassName}>
      {combinedHighlights.map((highlight, index) => (
        <span
          key={index}
          className={
            highlight.isMatch
              ? differentColors && highlight.keywordIndex !== undefined
                ? colorClasses[highlight.keywordIndex % colorClasses.length]
                : highlightClassName
              : ''
          }
        >
          {highlight.text}
        </span>
      ))}
    </span>
  );
};

export default SearchHighlight;