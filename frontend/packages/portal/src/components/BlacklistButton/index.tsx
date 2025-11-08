/**
 * 黑名单按钮组件 🚫
 * @author BaSui 😎
 * @description 拉黑/解除拉黑用户的交互按钮，支持确认弹窗和状态同步
 */

import React, { useState, useEffect } from 'react';
import { Button, Modal, Input, message as antMessage } from 'antd';
import { UserDeleteOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { blacklistService } from '../../services';
import './index.css';

const { confirm } = Modal;
const { TextArea } = Input;

export interface BlacklistButtonProps {
  userId: number;                // 目标用户ID
  userName: string;              // 目标用户昵称
  size?: 'small' | 'middle' | 'large';  // 按钮尺寸
  block?: boolean;               // 是否块级按钮
  onStatusChange?: (isBlocked: boolean) => void;  // 状态变化回调
}

export const BlacklistButton: React.FC<BlacklistButtonProps> = ({
  userId,
  userName,
  size = 'middle',
  block = false,
  onStatusChange,
}) => {
  const [isBlocked, setIsBlocked] = useState(false);
  const [loading, setLoading] = useState(true);
  const [operating, setOperating] = useState(false);
  const [blockReason, setBlockReason] = useState('');
  const [blockModalVisible, setBlockModalVisible] = useState(false);

  // ==================== 加载黑名单状态 ====================
  useEffect(() => {
    checkBlockStatus();
  }, [userId]);

  const checkBlockStatus = async () => {
    setLoading(true);
    try {
      const blocked = await blacklistService.isBlocked(userId);
      setIsBlocked(blocked);
      console.log(`[BlacklistButton] ✅ 用户 ${userId} 拉黑状态:`, blocked);
    } catch (error) {
      console.error('[BlacklistButton] ❌ 检查拉黑状态失败:', error);
      // 失败时默认为未拉黑
      setIsBlocked(false);
    } finally {
      setLoading(false);
    }
  };

  // ==================== 拉黑用户 ====================
  const handleBlock = () => {
    setBlockModalVisible(true);
  };

  const confirmBlock = async () => {
    if (!blockReason.trim()) {
      antMessage.warning('请输入拉黑原因！');
      return;
    }

    setOperating(true);
    try {
      await blacklistService.blockUser({
        blockedUserId: userId,
        reason: blockReason.trim(),
      });

      antMessage.success(`已将 ${userName} 加入黑名单`);
      setIsBlocked(true);
      setBlockModalVisible(false);
      setBlockReason('');

      // 触发回调
      onStatusChange?.(true);

      console.log(`[BlacklistButton] ✅ 拉黑成功: ${userId}`);
    } catch (error) {
      console.error('[BlacklistButton] ❌ 拉黑失败:', error);
      antMessage.error('拉黑失败，请重试');
    } finally {
      setOperating(false);
    }
  };

  const cancelBlock = () => {
    setBlockModalVisible(false);
    setBlockReason('');
  };

  // ==================== 解除拉黑 ====================
  const handleUnblock = () => {
    confirm({
      title: '确认解除拉黑',
      icon: <ExclamationCircleOutlined />,
      content: (
        <div>
          <p>确定要将 <strong>{userName}</strong> 从黑名单中移除吗？</p>
          <p className="unblock-hint">解除后您将能再次看到该用户的消息和内容</p>
        </div>
      ),
      okText: '确认解除',
      cancelText: '取消',
      onOk: async () => {
        setOperating(true);
        try {
          await blacklistService.unblockUser(userId);
          antMessage.success(`已解除对 ${userName} 的拉黑`);
          setIsBlocked(false);

          // 触发回调
          onStatusChange?.(false);

          console.log(`[BlacklistButton] ✅ 解除拉黑成功: ${userId}`);
        } catch (error) {
          console.error('[BlacklistButton] ❌ 解除拉黑失败:', error);
          antMessage.error('解除拉黑失败，请重试');
        } finally {
          setOperating(false);
        }
      },
    });
  };

  // ==================== 渲染 ====================

  if (loading) {
    return (
      <Button size={size} block={block} loading disabled>
        加载中...
      </Button>
    );
  }

  return (
    <>
      {isBlocked ? (
        <Button
          type="default"
          size={size}
          block={block}
          icon={<UserDeleteOutlined />}
          onClick={handleUnblock}
          loading={operating}
          className="blacklist-button unblock"
        >
          已拉黑
        </Button>
      ) : (
        <Button
          type="default"
          danger
          size={size}
          block={block}
          icon={<UserDeleteOutlined />}
          onClick={handleBlock}
          loading={operating}
          className="blacklist-button block"
        >
          拉黑
        </Button>
      )}

      {/* 拉黑确认弹窗 */}
      <Modal
        title="拉黑用户"
        open={blockModalVisible}
        onOk={confirmBlock}
        onCancel={cancelBlock}
        confirmLoading={operating}
        okText="确认拉黑"
        cancelText="取消"
        width={480}
      >
        <div className="block-modal-content">
          <div className="block-info">
            <ExclamationCircleOutlined className="warning-icon" />
            <div className="info-text">
              <p className="target-user">确定要拉黑 <strong>{userName}</strong> 吗？</p>
              <p className="block-effect">拉黑后您将无法收到对方的消息，也不会看到对方发布的内容</p>
            </div>
          </div>

          <div className="reason-input">
            <label className="reason-label">拉黑原因 <span className="required">*</span></label>
            <TextArea
              placeholder="请输入拉黑原因（例如：骚扰、广告等）"
              value={blockReason}
              onChange={(e) => setBlockReason(e.target.value)}
              maxLength={200}
              showCount
              rows={4}
              autoFocus
            />
          </div>
        </div>
      </Modal>
    </>
  );
};

export default BlacklistButton;
