/**
 * MarkdownRenderer 组件 - Markdown 渲染器
 * @author BaSui 😎
 * @description 简单的 Markdown 渲染组件，支持基础语法
 */

import React, { useMemo } from 'react';
import './MarkdownRenderer.css';

interface MarkdownRendererProps {
  content: string;
  className?: string;
  maxLength?: number;
}

/**
 * 简单的 Markdown 解析器
 * 支持：加粗、斜体、代码块、链接、图片、标题、列表
 */
const parseMarkdown = (markdown: string): string => {
  let html = markdown;

  // 1. 代码块（多行）```code```
  html = html.replace(/```([^\n]*)\n([\s\S]*?)```/g, (_, lang, code) => {
    return `<pre><code class="language-${lang || 'text'}">${escapeHtml(code.trim())}</code></pre>`;
  });

  // 2. 行内代码 `code`
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>');

  // 3. 图片 ![alt](url)
  html = html.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" />');

  // 4. 链接 [text](url)
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>');

  // 5. 标题 # Heading
  html = html.replace(/^### (.*$)/gim, '<h3>$1</h3>');
  html = html.replace(/^## (.*$)/gim, '<h2>$1</h2>');
  html = html.replace(/^# (.*$)/gim, '<h1>$1</h1>');

  // 6. 加粗 **text** 或 __text__
  html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
  html = html.replace(/__([^_]+)__/g, '<strong>$1</strong>');

  // 7. 斜体 *text* 或 _text_
  html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');
  html = html.replace(/_([^_]+)_/g, '<em>$1</em>');

  // 8. 删除线 ~~text~~
  html = html.replace(/~~([^~]+)~~/g, '<del>$1</del>');

  // 9. 无序列表 - item 或 * item
  html = html.replace(/^\s*[-*]\s+(.*)$/gim, '<li>$1</li>');
  html = html.replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>');

  // 10. 引用 > quote
  html = html.replace(/^>\s+(.*)$/gim, '<blockquote>$1</blockquote>');

  // 11. 换行
  html = html.replace(/\n\n/g, '</p><p>');
  html = html.replace(/\n/g, '<br />');

  // 12. 包裹段落
  if (!html.startsWith('<')) {
    html = `<p>${html}</p>`;
  }

  return html;
};

/**
 * HTML 转义
 */
const escapeHtml = (text: string): string => {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
};

/**
 * MarkdownRenderer 组件
 */
const MarkdownRenderer: React.FC<MarkdownRendererProps> = ({
  content,
  className = '',
  maxLength,
}) => {
  const renderedHtml = useMemo(() => {
    let text = content || '';
    
    // 截断文本（如果指定了最大长度）
    if (maxLength && text.length > maxLength) {
      text = text.substring(0, maxLength) + '...';
    }

    return parseMarkdown(text);
  }, [content, maxLength]);

  return (
    <div
      className={`markdown-renderer ${className}`}
      dangerouslySetInnerHTML={{ __html: renderedHtml }}
    />
  );
};

export default MarkdownRenderer;
