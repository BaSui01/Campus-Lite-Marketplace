/**
 * Tabs 组件 - 标签页切换专家！📑
 * @author BaSui 😎
 * @description 通用标签页组件，支持水平/垂直布局、禁用、徽标
 */

import React, { useState } from 'react';
import './Tabs.css';

/**
 * 标签页项接口
 */
export interface TabItem {
  /**
   * 标签页键值
   */
  key: string;

  /**
   * 标签页标题
   */
  label: React.ReactNode;

  /**
   * 标签页内容
   */
  children: React.ReactNode;

  /**
   * 是否禁用
   * @default false
   */
  disabled?: boolean;

  /**
   * 徽标（显示在标题右侧）
   */
  badge?: React.ReactNode;

  /**
   * 图标
   */
  icon?: React.ReactNode;
}

/**
 * Tabs 布局类型
 */
export type TabsLayout = 'horizontal' | 'vertical';

/**
 * Tabs 尺寸
 */
export type TabsSize = 'small' | 'medium' | 'large';

/**
 * Tabs 组件的 Props 接口
 */
export interface TabsProps {
  /**
   * 标签页列表
   */
  items: TabItem[];

  /**
   * 当前激活的标签页键值
   */
  activeKey?: string;

  /**
   * 默认激活的标签页键值
   */
  defaultActiveKey?: string;

  /**
   * 布局方向
   * @default 'horizontal'
   */
  layout?: TabsLayout;

  /**
   * 尺寸
   * @default 'medium'
   */
  size?: TabsSize;

  /**
   * 是否显示下划线动画
   * @default true
   */
  animated?: boolean;

  /**
   * 标签页切换回调
   */
  onChange?: (key: string) => void;

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
 * Tabs 组件
 *
 * @example
 * ```tsx
 * // 基础用法
 * <Tabs
 *   items={[
 *     { key: '1', label: '标签一', children: <div>内容一</div> },
 *     { key: '2', label: '标签二', children: <div>内容二</div> },
 *     { key: '3', label: '标签三', children: <div>内容三</div>, disabled: true },
 *   ]}
 * />
 *
 * // 受控模式
 * <Tabs
 *   items={items}
 *   activeKey={activeKey}
 *   onChange={setActiveKey}
 * />
 *
 * // 垂直布局
 * <Tabs
 *   items={items}
 *   layout="vertical"
 * />
 *
 * // 带图标和徽标
 * <Tabs
 *   items={[
 *     { key: '1', label: '消息', icon: <MailIcon />, badge: <Badge count={5} /> },
 *     { key: '2', label: '通知', icon: <BellIcon />, badge: <Badge dot /> },
 *   ]}
 * />
 * ```
 */
export const Tabs: React.FC<TabsProps> = ({
  items = [], // ✅ 默认空数组,防御性编程!
  activeKey: controlledActiveKey,
  defaultActiveKey,
  layout = 'horizontal',
  size = 'medium',
  animated = true,
  onChange,
  className = '',
  style,
}) => {
  // 内部状态：当前激活的标签页
  const [internalActiveKey, setInternalActiveKey] = useState<string>(
    defaultActiveKey || items[0]?.key || ''
  );

  // 实际使用的激活键值（受控/非受控）
  const activeKey = controlledActiveKey !== undefined ? controlledActiveKey : internalActiveKey;

  // 当前激活的标签页项
  const activeItem = items.find((item) => item.key === activeKey);

  /**
   * 处理标签页切换
   */
  const handleTabClick = (item: TabItem) => {
    if (item.disabled) return;

    if (controlledActiveKey === undefined) {
      setInternalActiveKey(item.key);
    }

    onChange?.(item.key);
  };

  // 组装 CSS 类名
  const classNames = [
    'campus-tabs',
    `campus-tabs--${layout}`,
    `campus-tabs--${size}`,
    animated ? 'campus-tabs--animated' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={classNames} style={style}>
      {/* 标签页头部 */}
      <div className="campus-tabs__nav">
        {items.map((item) => {
          const isActive = item.key === activeKey;

          return (
            <div
              key={item.key}
              className={`campus-tabs__tab ${
                isActive ? 'campus-tabs__tab--active' : ''
              } ${item.disabled ? 'campus-tabs__tab--disabled' : ''}`}
              onClick={() => handleTabClick(item)}
            >
              {item.icon && <span className="campus-tabs__tab-icon">{item.icon}</span>}
              <span className="campus-tabs__tab-label">{item.label}</span>
              {item.badge && <span className="campus-tabs__tab-badge">{item.badge}</span>}
            </div>
          );
        })}

        {/* 激活指示器 */}
        {animated && (
          <div
            className="campus-tabs__indicator"
            style={{
              transform:
                layout === 'horizontal'
                  ? `translateX(${items.findIndex((item) => item.key === activeKey) * 100}%)`
                  : `translateY(${items.findIndex((item) => item.key === activeKey) * 100}%)`,
            }}
          />
        )}
      </div>

      {/* 标签页内容 */}
      <div className="campus-tabs__content">
        {activeItem && (
          <div className="campus-tabs__pane" key={activeKey}>
            {activeItem.children}
          </div>
        )}
      </div>
    </div>
  );
};

export default Tabs;
