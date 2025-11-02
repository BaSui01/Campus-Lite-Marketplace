/**
 * Empty 组件 - 空状态的艺术家！🎨
 * @author BaSui 😎
 * @description 空状态展示组件，支持自定义图片、描述、操作按钮
 */

import React from 'react';
import './Empty.css';

/**
 * Empty 组件的 Props 接口
 */
export interface EmptyProps {
  /**
   * 空状态图片
   * @default '默认空状态图片'
   */
  image?: string | React.ReactNode;

  /**
   * 图片样式
   */
  imageStyle?: React.CSSProperties;

  /**
   * 描述文本
   * @default '暂无数据'
   */
  description?: React.ReactNode;

  /**
   * 子元素（操作按钮等）
   */
  children?: React.ReactNode;

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
 * Empty 组件
 *
 * @example
 * ```tsx
 * // 基础用法
 * <Empty />
 *
 * // 自定义描述
 * <Empty description="暂无订单记录" />
 *
 * // 自定义图片和操作
 * <Empty 
 *   image="https://example.com/custom-empty.png"
 *   description="还没有商品，快去添加吧！"
 * >
 *   <Button type="primary">添加商品</Button>
 * </Empty>
 *
 * // 自定义图片组件
 * <Empty
 *   image={
 *     <div className="custom-empty-image">
 *       <span className="empty-emoji">📦</span>
 *     </div>
 *   }
 *   description="物流信息为空"
 * />
 * ```
 */
export const Empty: React.FC<EmptyProps> = ({
  image,
  imageStyle,
  description = '暂无数据',
  children,
  className = '',
  style
}) => {
  // 默认空状态图片
  const defaultImage = (
    <svg
      width="184"
      height="152"
      viewBox="0 0 184 152"
      xmlns="http://www.w3.org/2000/svg"
    >
      <g fill="none" fillRule="evenodd">
        <g transform="translate(24 31.67)">
          <ellipse
            fillOpacity=".8"
            fill="#F5F5F7"
            cx="67.797"
            cy="106.89"
            rx="67.797"
            ry="12.668"
          />
          <path
            d="M122.034 69.674L98.109 40.229c-1.148-1.386-2.826-2.225-4.593-2.225h-51.44c-1.766 0-3.444.839-4.592 2.225L13.56 69.674v15.383h108.475V69.674z"
            fill="#AEB8C2"
          />
          <path
            d="M101.537 86.214L80.63 61.102c-1.001-1.228-2.507-1.93-4.048-1.93H22.034c-1.54 0-3.047.702-4.048 1.93L-2.545 86.214v13.628h104.082V86.214z"
            fill="#E4E6EB"
          />
          <path d="M33.83 0h67.933a4 4 0 0 1 4 4v93.344a4 4 0 0 1-4 4H33.83a4 4 0 0 1-4-4V4a4 4 0 0 1 4-4z" fill="#F5F5F7" />
          <path d="M36.828 10.956l5.266-2.584a2 2 0 0 1 1.708 0l5.265 2.584a2 2 0 0 1 1.125 1.79v5.8a2 2 0 0 1-1.125 1.79l-5.266 2.584a2 2 0 0 1-1.708 0l-5.265-2.584a2 2 0 0 1-1.125-1.79v-5.8a2 2 0 0 1 1.125-1.79z" fill="#DCDDE0" />
        </g>
        <path d="M148.09 122.854c0 7.008-5.92 12.688-13.22 12.688H54.33c-7.3 0-13.22-5.68-13.22-12.688V31.136c0-7.008 5.92-12.688 13.22-12.688h80.54c7.3 0 13.22 5.68 13.22 12.688v91.718z" fill="#FFF" />
        <g>
          <path d="M149.626 139.497H38.967V40.282h110.659a4 4 0 0 1 4 4v91.215a4 4 0 0 1-4 4z" fill="#FFF" />
          <g transform="translate(31 23)">
            <ellipse fill="#F5F5F7" cx="20.657" cy="106.89" rx="20.657" ry="12.668" />
            <path d="M5.987 86.214l-.84 20.475a2 2 0 0 0 1.988 2.212h37.855a2 2 0 0 0 1.988-2.212l-.839-20.475a4 4 0 0 0-3.977-3.784H9.963a4 4 0 0 0-3.976 3.784z" fill="#DCDDE0" />
            <path d="M7 40.282v95.215a4 4 0 0 0 4 4h37a4 4 0 0 0 4-4V40.282a4 4 0 0 0-4-4H11a4 4 0 0 0-4 4z" fill="#FFF" />
          </g>
        </g>
      </g>
    </svg>
  );

  // 渲染图片
  const renderImage = () => {
    if (typeof image === 'string') {
      return <img src={image} alt="empty" className="campus-empty__image" style={imageStyle} />;
    }
    
    if (React.isValidElement(image)) {
      return <div className="campus-empty__image" style={imageStyle}>{image}</div>;
    }

    return <div className="campus-empty__image" style={imageStyle}>{defaultImage}</div>;
  };

  // 渲染描述
  const renderDescription = () => {
    if (typeof description === 'string') {
      return <p className="campus-empty__description">{description}</p>;
    }

    return <div className="campus-empty__description">{description}</div>;
  };

  // 组装 CSS 类名
  const classNames = [
    'campus-empty',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={classNames} style={style}>
      {/* 空状态图片 */}
      <div className="campus-empty__image-wrapper">
        {renderImage()}
      </div>

      {/* 描述文本 */}
      <div className="campus-empty__description-wrapper">
        {renderDescription()}
      </div>

      {/* 操作区域 */}
      {children && (
        <div className="campus-empty__footer">
          {children}
        </div>
      )}
    </div>
  );
};

export default Empty;
