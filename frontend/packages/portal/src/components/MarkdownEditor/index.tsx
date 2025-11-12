/**
 * MarkdownEditor 组件 - Markdown 编辑器（带表情包）
 * @author BaSui 😎
 * @description 富文本编辑器，支持 Markdown 语法和表情包选择
 */

import React, { useState, useRef, useCallback } from 'react';
import { EmojiPicker } from '../EmojiPicker';
import MarkdownRenderer from '../MarkdownRenderer';
import type { EmojiItem } from '@campus/shared/types/emoji';
import './MarkdownEditor.css';

interface MarkdownEditorProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  maxLength?: number;
  minHeight?: number;
  showToolbar?: boolean;
  showEmojiPicker?: boolean;
  showPreview?: boolean; // 是否显示预览标签
  className?: string;
}

/**
 * MarkdownEditor 组件
 */
const MarkdownEditor: React.FC<MarkdownEditorProps> = ({
  value,
  onChange,
  placeholder = '支持 Markdown 语法，输入你的想法...',
  maxLength = 5000,
  minHeight = 120,
  showToolbar = true,
  showEmojiPicker = true,
  showPreview = true,
  className = '',
}) => {
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const [showEmoji, setShowEmoji] = useState(false);
  const [activeTab, setActiveTab] = useState<'edit' | 'preview'>('edit'); // 编辑/预览标签

  /**
   * 插入文本到光标位置
   */
  const insertText = useCallback((text: string) => {
    const textarea = textareaRef.current;
    if (!textarea) return;

    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const before = value.substring(0, start);
    const after = value.substring(end);

    const newValue = before + text + after;
    onChange(newValue);

    // 恢复光标位置
    setTimeout(() => {
      textarea.focus();
      textarea.setSelectionRange(start + text.length, start + text.length);
    }, 0);
  }, [value, onChange]);

  /**
   * 包裹选中文本
   */
  const wrapText = useCallback((before: string, after: string = before) => {
    const textarea = textareaRef.current;
    if (!textarea) return;

    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const selectedText = value.substring(start, end);

    if (selectedText) {
      // 有选中文本，包裹
      const newValue = value.substring(0, start) + before + selectedText + after + value.substring(end);
      onChange(newValue);
      setTimeout(() => {
        textarea.focus();
        textarea.setSelectionRange(start + before.length, end + before.length);
      }, 0);
    } else {
      // 无选中文本，插入
      insertText(before + after);
    }
  }, [value, onChange, insertText]);

  /**
   * 选择表情
   */
  const handleEmojiSelect = useCallback((emoji: EmojiItem) => {
    const emojiText = emoji.contentType === 'text' ? emoji.content : `![${emoji.name}](${emoji.content})`;
    insertText(emojiText);
    setShowEmoji(false);
  }, [insertText]);

  /**
   * 工具栏按钮配置
   */
  const toolbarButtons = [
    { icon: '𝗕', title: '加粗', action: () => wrapText('**') },
    { icon: '𝘐', title: '斜体', action: () => wrapText('*') },
    { icon: '~~', title: '删除线', action: () => wrapText('~~') },
    { icon: '<>', title: '代码', action: () => wrapText('`') },
    { icon: '🔗', title: '链接', action: () => insertText('[链接文字](https://example.com)') },
    { icon: '🖼️', title: '图片', action: () => insertText('![图片描述](图片URL)') },
    { icon: '📝', title: '引用', action: () => insertText('\n> ') },
    { icon: '📋', title: '代码块', action: () => insertText('\n```\n代码\n```\n') },
  ];

  return (
    <div className={`markdown-editor ${className}`}>
      {/* 标签栏（编辑/预览） */}
      {showPreview && (
        <div className="markdown-editor__tabs">
          <button
            type="button"
            className={`markdown-editor__tab ${activeTab === 'edit' ? 'active' : ''}`}
            onClick={() => setActiveTab('edit')}
          >
            ✍️ 编辑
          </button>
          <button
            type="button"
            className={`markdown-editor__tab ${activeTab === 'preview' ? 'active' : ''}`}
            onClick={() => setActiveTab('preview')}
          >
            👁️ 预览
          </button>
        </div>
      )}

      {/* 工具栏 */}
      {showToolbar && activeTab === 'edit' && (
        <div className="markdown-editor__toolbar">
          <div className="markdown-editor__toolbar-group">
            {toolbarButtons.map((btn, index) => (
              <button
                key={index}
                type="button"
                className="markdown-editor__toolbar-btn"
                title={btn.title}
                onClick={btn.action}
              >
                {btn.icon}
              </button>
            ))}
          </div>

          {/* 表情包按钮 */}
          {showEmojiPicker && (
            <div className="markdown-editor__toolbar-group">
              <button
                type="button"
                className={`markdown-editor__toolbar-btn ${showEmoji ? 'active' : ''}`}
                title="表情"
                onClick={() => setShowEmoji(!showEmoji)}
              >
                😊
              </button>
            </div>
          )}

          {/* 字数统计 */}
          <div className="markdown-editor__count">
            {value.length} / {maxLength}
          </div>
        </div>
      )}

      {/* 编辑/预览区域 */}
      <div className="markdown-editor__content" style={{ position: 'relative' }}>
        {activeTab === 'edit' ? (
          <>
            <textarea
              ref={textareaRef}
              className="markdown-editor__textarea"
              value={value}
              onChange={(e) => onChange(e.target.value)}
              placeholder={placeholder}
              maxLength={maxLength}
              style={{ minHeight: `${minHeight}px` }}
            />

            {/* 表情选择器 */}
            {showEmoji && (
              <div className="markdown-editor__emoji-picker">
                <EmojiPicker
                  visible={showEmoji}
                  onEmojiSelect={handleEmojiSelect}
                  onClose={() => setShowEmoji(false)}
                  config={{
                    showFavoriteTab: true,
                    showSearch: true,
                    emojisPerRow: 8,
                    maxRows: 4,
                    theme: 'light',
                  }}
                />
              </div>
            )}
          </>
        ) : (
          <div
            className="markdown-editor__preview"
            style={{ minHeight: `${minHeight}px` }}
          >
            {value ? (
              <MarkdownRenderer content={value} />
            ) : (
              <div className="markdown-editor__preview-empty">
                暂无内容，切换到编辑模式开始写作吧~ ✨
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default MarkdownEditor;
