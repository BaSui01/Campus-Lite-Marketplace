/**
 * UserAvatar 组件 - 用户头像专家！👨‍💼
 * @author BaSui 😎
 * @description 用户头像组件，基于 Avatar 和 Badge 封装，支持在线状态、认证标识
 */

import React from 'react';
import { Avatar, type AvatarProps } from '../Avatar';
import { Badge, type BadgeStatus } from '../Badge';
import './UserAvatar.css';

/**
 * 用户在线状态
 */
export type UserOnlineStatus = 'online' | 'offline' | 'busy' | 'away';

/**
 * UserAvatar 组件的 Props 接口
 */
export interface UserAvatarProps extends Omit<AvatarProps, 'src' | 'text'> {
  /**
   * 用户 ID
   */
  userId: string;

  /**
   * 用户名
   */
  username: string;

  /**
   * 用户头像 URL
   */
  avatarUrl?: string;

  /**
   * 是否显示在线状态
   * @default false
   */
  showOnlineStatus?: boolean;

  /**
   * 在线状态
   * @default 'offline'
   */
  onlineStatus?: UserOnlineStatus;

  /**
   * 是否已认证
   * @default false
   */
  verified?: boolean;

  /**
   * 认证标识图标
   */
  verifiedIcon?: React.ReactNode;

  /**
   * 是否显示用户名
   * @default false
   */
  showUsername?: boolean;

  /**
   * 用户名位置
   * @default 'right'
   */
  usernamePosition?: 'top' | 'right' | 'bottom' | 'left';

  /**
   * 头像点击回调
   */
  onAvatarClick?: (userId: string) => void;

  /**
   * 用户名点击回调
   */
  onUsernameClick?: (userId: string) => void;

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
 * 获取在线状态配置
 */
const getOnlineStatusConfig = (status: UserOnlineStatus): { color: string; badge: BadgeStatus } => {
  const configs = {
    online: { color: '#52c41a', badge: 'success' as const },
    offline: { color: '#d9d9d9', badge: 'default' as const },
    busy: { color: '#ff4d4f', badge: 'error' as const },
    away: { color: '#faad14', badge: 'warning' as const },
  };
  return configs[status] || configs.offline;
};

/**
 * UserAvatar 组件
 *
 * @example
 * ```tsx
 * // 基础用法
 * <UserAvatar
 *   userId="1"
 *   username="张三"
 *   avatarUrl="/avatar.jpg"
 * />
 *
 * // 显示在线状态
 * <UserAvatar
 *   userId="1"
 *   username="张三"
 *   avatarUrl="/avatar.jpg"
 *   showOnlineStatus
 *   onlineStatus="online"
 * />
 *
 * // 显示认证标识和用户名
 * <UserAvatar
 *   userId="1"
 *   username="张三"
 *   avatarUrl="/avatar.jpg"
 *   verified
 *   showUsername
 *   onAvatarClick={(id) => navigate(`/user/${id}`)}
 * />
 *
 * // 自定义尺寸和用户名位置
 * <UserAvatar
 *   userId="1"
 *   username="张三"
 *   size="large"
 *   showUsername
 *   usernamePosition="bottom"
 * />
 * ```
 */
export const UserAvatar: React.FC<UserAvatarProps> = ({
  userId,
  username,
  avatarUrl,
  showOnlineStatus = false,
  onlineStatus = 'offline',
  verified = false,
  verifiedIcon,
  showUsername = false,
  usernamePosition = 'right',
  onAvatarClick,
  onUsernameClick,
  className = '',
  style,
  ...avatarProps
}) => {
  const statusConfig = showOnlineStatus ? getOnlineStatusConfig(onlineStatus) : null;

  /**
   * 处理头像点击
   */
  const handleAvatarClick = () => {
    onAvatarClick?.(userId);
  };

  /**
   * 处理用户名点击
   */
  const handleUsernameClick = () => {
    onUsernameClick?.(userId);
  };

  // 头像元素
  const avatarElement = (
    <div className="campus-user-avatar__avatar-wrapper">
      {showOnlineStatus && statusConfig ? (
        <Badge status={statusConfig.badge} dot>
          <Avatar
            {...avatarProps}
            src={avatarUrl}
            text={username}
            onClick={onAvatarClick ? handleAvatarClick : undefined}
          />
        </Badge>
      ) : (
        <Avatar
          {...avatarProps}
          src={avatarUrl}
          text={username}
          onClick={onAvatarClick ? handleAvatarClick : undefined}
        />
      )}
      {/* 认证标识 */}
      {verified && (
        <div className="campus-user-avatar__verified">
          {verifiedIcon || (
            <svg
              viewBox="0 0 24 24"
              fill="currentColor"
              className="campus-user-avatar__verified-icon"
            >
              <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" />
            </svg>
          )}
        </div>
      )}
    </div>
  );

  // 用户名元素
  const usernameElement = showUsername && (
    <span
      className={`campus-user-avatar__username ${
        onUsernameClick ? 'campus-user-avatar__username--clickable' : ''
      }`}
      onClick={onUsernameClick ? handleUsernameClick : undefined}
    >
      {username}
    </span>
  );

  // 组装 CSS 类名
  const classNames = [
    'campus-user-avatar',
    `campus-user-avatar--username-${usernamePosition}`,
    showUsername ? 'campus-user-avatar--with-username' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={classNames} style={style}>
      {usernamePosition === 'top' && usernameElement}
      {usernamePosition === 'left' && usernameElement}
      {avatarElement}
      {usernamePosition === 'right' && usernameElement}
      {usernamePosition === 'bottom' && usernameElement}
    </div>
  );
};

export default UserAvatar;
