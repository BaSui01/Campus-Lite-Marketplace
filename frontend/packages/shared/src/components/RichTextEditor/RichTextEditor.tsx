/**
 * RichTextEditor 组件 - 富文本编辑器专家！✏️
 * @author BaSui 😎
 * @description 富文本编辑器组件，基于 contentEditable 实现，支持基础格式化功能
 */

import React, { useRef, useCallback, useEffect } from 'react';
import './RichTextEditor.css';

/**
 * RichTextEditor 组件的 Props 接口
 */
export interface RichTextEditorProps {
  /**
   * 编辑器内容（HTML 格式）
   */
  value?: string;

  /**
   * 占位符文本
   * @default '请输入内容...'
   */
  placeholder?: string;

  /**
   * 是否禁用
   * @default false
   */
  disabled?: boolean;

  /**
   * 是否只读
   * @default false
   */
  readOnly?: boolean;

  /**
   * 编辑器高度
   * @default 300
   */
  height?: number;

  /**
   * 是否显示工具栏
   * @default true
   */
  showToolbar?: boolean;

  /**
   * 内容改变回调
   */
  onChange?: (html: string) => void;

  /**
   * 自定义类名
   */
  className?: string;

  /**
   * 自定义样式
   */
  style?: React.CSSProperties;
}

/**
 * 工具栏按钮配置
 */
interface ToolbarButton {
  command: string;
  icon: string;
  title: string;
  value?: string;
}

/**
 * 工具栏按钮列表
 */
const toolbarButtons: ToolbarButton[] = [
  { command: 'bold', icon: 'B', title: '粗体' },
  { command: 'italic', icon: 'I', title: '斜体' },
  { command: 'underline', icon: 'U', title: '下划线' },
  { command: 'strikeThrough', icon: 'S', title: '删除线' },
  { command: 'justifyLeft', icon: '≡', title: '左对齐' },
  { command: 'justifyCenter', icon: '≣', title: '居中对齐' },
  { command: 'justifyRight', icon: '≡', title: '右对齐' },
  { command: 'insertUnorderedList', icon: '•', title: '无序列表' },
  { command: 'insertOrderedList', icon: '1.', title: '有序列表' },
  { command: 'removeFormat', icon: '✕', title: '清除格式' },
];

/**
 * RichTextEditor 组件
 *
 * @example
 * ```tsx
 * // 基础用法
 * <RichTextEditor
 *   value={content}
 *   onChange={setContent}
 * />
 *
 * // 自定义高度和占位符
 * <RichTextEditor
 *   value={content}
 *   height={400}
 *   placeholder="请输入商品描述..."
 *   onChange={setContent}
 * />
 *
 * // 只读模式
 * <RichTextEditor
 *   value={content}
 *   readOnly
 *   showToolbar={false}
 * />
 * ```
 */
export const RichTextEditor: React.FC<RichTextEditorProps> = ({
  value = '',
  placeholder = '请输入内容...',
  disabled = false,
  readOnly = false,
  height = 300,
  showToolbar = true,
  onChange,
  className = '',
  style,
}) => {
  const editorRef = useRef<HTMLDivElement>(null);
  const isComposingRef = useRef(false);

  /**
   * 执行编辑命令
   */
  const execCommand = useCallback((command: string, value?: string) => {
    if (disabled || readOnly) return;

    document.execCommand(command, false, value);
    editorRef.current?.focus();

    // 触发内容变化
    if (editorRef.current && onChange) {
      onChange(editorRef.current.innerHTML);
    }
  }, [disabled, readOnly, onChange]);

  /**
   * 处理输入
   */
  const handleInput = useCallback(() => {
    if (isComposingRef.current) return;

    if (editorRef.current && onChange) {
      onChange(editorRef.current.innerHTML);
    }
  }, [onChange]);

  /**
   * 处理粘贴
   */
  const handlePaste = useCallback((e: React.ClipboardEvent) => {
    if (disabled || readOnly) return;

    e.preventDefault();

    // 只粘贴纯文本
    const text = e.clipboardData.getData('text/plain');
    document.execCommand('insertText', false, text);
  }, [disabled, readOnly]);

  /**
   * 处理组合输入开始
   */
  const handleCompositionStart = useCallback(() => {
    isComposingRef.current = true;
  }, []);

  /**
   * 处理组合输入结束
   */
  const handleCompositionEnd = useCallback(() => {
    isComposingRef.current = false;
    handleInput();
  }, [handleInput]);

  /**
   * 初始化内容
   */
  useEffect(() => {
    if (editorRef.current && editorRef.current.innerHTML !== value) {
      editorRef.current.innerHTML = value;
    }
  }, [value]);

  // 组装 CSS 类名
  const classNames = [
    'campus-rich-text-editor',
    disabled ? 'campus-rich-text-editor--disabled' : '',
    readOnly ? 'campus-rich-text-editor--readonly' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={classNames} style={style}>
      {/* 工具栏 */}
      {showToolbar && !readOnly && (
        <div className="campus-rich-text-editor__toolbar">
          {toolbarButtons.map((button) => (
            <button
              key={button.command}
              type="button"
              className="campus-rich-text-editor__toolbar-btn"
              title={button.title}
              onClick={() => execCommand(button.command, button.value)}
              disabled={disabled}
            >
              {button.icon}
            </button>
          ))}
        </div>
      )}

      {/* 编辑区域 */}
      <div
        ref={editorRef}
        className="campus-rich-text-editor__content"
        contentEditable={!disabled && !readOnly}
        onInput={handleInput}
        onPaste={handlePaste}
        onCompositionStart={handleCompositionStart}
        onCompositionEnd={handleCompositionEnd}
        data-placeholder={placeholder}
        style={{ minHeight: `${height}px` }}
      />
    </div>
  );
};

export default RichTextEditor;
