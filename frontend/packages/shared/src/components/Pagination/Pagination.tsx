/**
 * Pagination 组件 - 分页导航专家！📄
 * @author BaSui 😎
 * @description 通用分页组件，支持页码跳转、每页条数选择、总数显示
 */

import React from 'react';
import './Pagination.css';

/**
 * Pagination 尺寸枚举
 */
export type PaginationSize = 'small' | 'medium' | 'large';

/**
 * Pagination 组件的 Props 接口
 */
export interface PaginationProps {
  /**
   * 当前页码（从 1 开始）
   */
  current: number;

  /**
   * 每页条数
   * @default 10
   */
  pageSize?: number;

  /**
   * 总条数
   */
  total: number;

  /**
   * 每页条数选项
   * @default [10, 20, 50, 100]
   */
  pageSizeOptions?: number[];

  /**
   * 是否显示每页条数选择器
   * @default true
   */
  showSizeChanger?: boolean;

  /**
   * 是否显示快速跳转
   * @default true
   */
  showQuickJumper?: boolean;

  /**
   * 是否显示总数
   * @default true
   */
  showTotal?: boolean;

  /**
   * 是否禁用
   * @default false
   */
  disabled?: boolean;

  /**
   * 尺寸
   * @default 'medium'
   */
  size?: PaginationSize;

  /**
   * 页码改变回调
   */
  onChange?: (page: number, pageSize: number) => void;

  /**
   * 每页条数改变回调
   */
  onShowSizeChange?: (current: number, size: number) => void;

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
 * Pagination 组件
 *
 * @example
 * ```tsx
 * // 基础用法
 * <Pagination
 *   current={page}
 *   pageSize={20}
 *   total={100}
 *   onChange={(page, pageSize) => setPage(page)}
 * />
 *
 * // 完整配置
 * <Pagination
 *   current={page}
 *   pageSize={pageSize}
 *   total={total}
 *   pageSizeOptions={[10, 20, 50, 100]}
 *   showSizeChanger
 *   showQuickJumper
 *   showTotal
 *   onChange={handlePageChange}
 *   onShowSizeChange={handleSizeChange}
 * />
 * ```
 */
export const Pagination: React.FC<PaginationProps> = ({
  current,
  pageSize = 10,
  total,
  pageSizeOptions = [10, 20, 50, 100],
  showSizeChanger = true,
  showQuickJumper = true,
  showTotal = true,
  disabled = false,
  size = 'medium',
  onChange,
  onShowSizeChange,
  className = '',
  style,
}) => {
  // 计算总页数
  const totalPages = Math.ceil(total / pageSize);

  // 当前页码（确保在有效范围内）
  const currentPage = Math.min(Math.max(1, current), totalPages || 1);

  // 快速跳转输入值
  const [jumpValue, setJumpValue] = React.useState('');

  /**
   * 处理页码改变
   */
  const handlePageChange = (newPage: number) => {
    if (disabled) return;
    if (newPage < 1 || newPage > totalPages) return;
    if (newPage === currentPage) return;
    onChange?.(newPage, pageSize);
  };

  /**
   * 处理每页条数改变
   */
  const handleSizeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    if (disabled) return;
    const newSize = Number(e.target.value);
    onShowSizeChange?.(1, newSize);
    onChange?.(1, newSize);
  };

  /**
   * 处理快速跳转
   */
  const handleQuickJump = () => {
    const page = Number(jumpValue);
    if (page >= 1 && page <= totalPages) {
      handlePageChange(page);
      setJumpValue('');
    }
  };

  /**
   * 生成页码按钮列表
   */
  const renderPageNumbers = () => {
    const pages: (number | string)[] = [];
    const showPages = 5; // 显示的页码按钮数量

    if (totalPages <= showPages + 2) {
      // 总页数较少，全部显示
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      // 总页数较多，显示部分页码
      pages.push(1);

      if (currentPage <= 3) {
        // 当前页在前面
        for (let i = 2; i <= Math.min(showPages, totalPages - 1); i++) {
          pages.push(i);
        }
        pages.push('...');
      } else if (currentPage >= totalPages - 2) {
        // 当前页在后面
        pages.push('...');
        for (let i = totalPages - showPages + 1; i < totalPages; i++) {
          pages.push(i);
        }
      } else {
        // 当前页在中间
        pages.push('...');
        for (let i = currentPage - 1; i <= currentPage + 1; i++) {
          pages.push(i);
        }
        pages.push('...');
      }

      pages.push(totalPages);
    }

    return pages;
  };

  // 组装 CSS 类名
  const classNames = [
    'campus-pagination',
    `campus-pagination--${size}`,
    disabled ? 'campus-pagination--disabled' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  if (totalPages === 0) {
    return null;
  }

  return (
    <div className={classNames} style={style}>
      {/* 总数显示 */}
      {showTotal && (
        <div className="campus-pagination__total">
          共 {total} 条
        </div>
      )}

      {/* 每页条数选择器 */}
      {showSizeChanger && (
        <div className="campus-pagination__size-changer">
          <select
            value={pageSize}
            onChange={handleSizeChange}
            disabled={disabled}
            className="campus-pagination__select"
          >
            {pageSizeOptions.map((option) => (
              <option key={option} value={option}>
                {option} 条/页
              </option>
            ))}
          </select>
        </div>
      )}

      {/* 分页按钮 */}
      <div className="campus-pagination__pages">
        {/* 上一页 */}
        <button
          className="campus-pagination__btn campus-pagination__btn--prev"
          onClick={() => handlePageChange(currentPage - 1)}
          disabled={disabled || currentPage === 1}
        >
          ‹
        </button>

        {/* 页码按钮 */}
        {renderPageNumbers().map((page, index) => {
          if (page === '...') {
            return (
              <span key={`ellipsis-${index}`} className="campus-pagination__ellipsis">
                ...
              </span>
            );
          }

          return (
            <button
              key={page}
              className={`campus-pagination__btn ${
                page === currentPage ? 'campus-pagination__btn--active' : ''
              }`}
              onClick={() => handlePageChange(page as number)}
              disabled={disabled}
            >
              {page}
            </button>
          );
        })}

        {/* 下一页 */}
        <button
          className="campus-pagination__btn campus-pagination__btn--next"
          onClick={() => handlePageChange(currentPage + 1)}
          disabled={disabled || currentPage === totalPages}
        >
          ›
        </button>
      </div>

      {/* 快速跳转 */}
      {showQuickJumper && (
        <div className="campus-pagination__jumper">
          <span>跳转到</span>
          <input
            type="number"
            min="1"
            max={totalPages}
            value={jumpValue}
            onChange={(e) => setJumpValue(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                handleQuickJump();
              }
            }}
            disabled={disabled}
            className="campus-pagination__input"
          />
          <span>页</span>
          <button
            onClick={handleQuickJump}
            disabled={disabled}
            className="campus-pagination__jump-btn"
          >
            确定
          </button>
        </div>
      )}
    </div>
  );
};

export default Pagination;
