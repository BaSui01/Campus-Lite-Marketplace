/**
 * Dropdown 组件 - 下拉菜单专家！📋
 * @author BaSui 😎
 * @description 通用下拉菜单组件，支持多级菜单、禁用项、自定义触发器
 */

import React, { useState, useRef, useEffect } from 'react';
import './Dropdown.css';

/**
 * 下拉菜单项接口
 */
export interface DropdownMenuItem {
  /**
   * 菜单项键值
   */
  key: string;

  /**
   * 菜单项标签
   */
  label: React.ReactNode;

  /**
   * 菜单项图标
   */
  icon?: React.ReactNode;

  /**
   * 是否禁用
   * @default false
   */
  disabled?: boolean;

  /**
   * 是否显示分割线
   * @default false
   */
  divider?: boolean;

  /**
   * 子菜单项
   */
  children?: DropdownMenuItem[];

  /**
   * 点击回调
   */
  onClick?: () => void;
}

/**
 * 下拉菜单触发方式
 */
export type DropdownTrigger = 'click' | 'hover';

/**
 * 下拉菜单位置
 */
export type DropdownPlacement = 'bottom-left' | 'bottom-right' | 'top-left' | 'top-right';

/**
 * Dropdown 组件的 Props 接口
 */
export interface DropdownProps {
  /**
   * 菜单项列表
   */
  menu: DropdownMenuItem[];

  /**
   * 触发方式
   * @default 'hover'
   */
  trigger?: DropdownTrigger;

  /**
   * 下拉菜单位置
   * @default 'bottom-left'
   */
  placement?: DropdownPlacement;

  /**
   * 是否禁用
   * @default false
   */
  disabled?: boolean;

  /**
   * 触发元素
   */
  children: React.ReactNode;

  /**
   * 菜单项点击回调
   */
  onMenuClick?: (key: string) => void;

  /**
   * 下拉菜单显示/隐藏回调
   */
  onVisibleChange?: (visible: boolean) => void;

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
 * Dropdown 组件
 *
 * @example
 * ```tsx
 * // 基础用法
 * <Dropdown
 *   menu={[
 *     { key: '1', label: '编辑' },
 *     { key: '2', label: '删除', disabled: true },
 *     { key: '3', label: '导出', divider: true },
 *   ]}
 *   onMenuClick={(key) => console.log(key)}
 * >
 *   <Button>更多操作</Button>
 * </Dropdown>
 *
 * // 多级菜单
 * <Dropdown
 *   menu={[
 *     { key: '1', label: '新建', children: [
 *       { key: '1-1', label: '新建文件' },
 *       { key: '1-2', label: '新建文件夹' },
 *     ]},
 *     { key: '2', label: '打开' },
 *   ]}
 * >
 *   <Button>文件</Button>
 * </Dropdown>
 * ```
 */
export const Dropdown: React.FC<DropdownProps> = ({
  menu,
  trigger = 'hover',
  placement = 'bottom-left',
  disabled = false,
  children,
  onMenuClick,
  onVisibleChange,
  className = '',
  style,
}) => {
  // 下拉菜单显示状态
  const [visible, setVisible] = useState(false);

  // 容器引用
  const containerRef = useRef<HTMLDivElement>(null);

  /**
   * 处理菜单显示/隐藏
   */
  const handleVisibleChange = (newVisible: boolean) => {
    if (disabled) return;
    setVisible(newVisible);
    onVisibleChange?.(newVisible);
  };

  /**
   * 处理菜单项点击
   */
  const handleMenuItemClick = (item: DropdownMenuItem) => {
    if (item.disabled) return;

    // 如果有子菜单，不关闭下拉菜单
    if (item.children && item.children.length > 0) {
      return;
    }

    item.onClick?.();
    onMenuClick?.(item.key);
    handleVisibleChange(false);
  };

  /**
   * 处理点击外部关闭
   */
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        handleVisibleChange(false);
      }
    };

    if (visible) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [visible]);

  /**
   * 渲染菜单项
   */
  const renderMenuItem = (item: DropdownMenuItem, level: number = 0) => {
    const hasChildren = item.children && item.children.length > 0;

    return (
      <div key={item.key} className="campus-dropdown__item-wrapper">
        {item.divider && <div className="campus-dropdown__divider" />}

        <div
          className={`campus-dropdown__item ${
            item.disabled ? 'campus-dropdown__item--disabled' : ''
          } ${hasChildren ? 'campus-dropdown__item--has-children' : ''}`}
          onClick={() => handleMenuItemClick(item)}
          style={{ paddingLeft: `${12 + level * 16}px` }}
        >
          {item.icon && <span className="campus-dropdown__item-icon">{item.icon}</span>}
          <span className="campus-dropdown__item-label">{item.label}</span>
          {hasChildren && <span className="campus-dropdown__item-arrow">›</span>}
        </div>

        {hasChildren && (
          <div className="campus-dropdown__submenu">
            {item.children!.map((child) => renderMenuItem(child, level + 1))}
          </div>
        )}
      </div>
    );
  };

  // 组装 CSS 类名
  const classNames = [
    'campus-dropdown',
    `campus-dropdown--${placement}`,
    disabled ? 'campus-dropdown--disabled' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div
      ref={containerRef}
      className={classNames}
      style={style}
      onClick={() => {
        if (trigger === 'click') {
          handleVisibleChange(!visible);
        }
      }}
      onMouseEnter={() => {
        if (trigger === 'hover') {
          handleVisibleChange(true);
        }
      }}
      onMouseLeave={() => {
        if (trigger === 'hover') {
          handleVisibleChange(false);
        }
      }}
    >
      {/* 触发元素 */}
      <div className="campus-dropdown__trigger">{children}</div>

      {/* 下拉菜单 */}
      {visible && (
        <div className="campus-dropdown__menu">
          {menu.map((item) => renderMenuItem(item))}
        </div>
      )}
    </div>
  );
};

export default Dropdown;
