/**
 * 弹窗状态管理 Hook
 * 
 * 功能：
 * - 弹窗显示/隐藏状态管理
 * - 弹窗数据管理（编辑、查看场景）
 * - 弹窗确认/取消操作
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import { useState, useCallback } from 'react';

/**
 * useModal Hook 返回值
 */
export interface UseModalResult<T = any> {
  /** 弹窗是否可见 */
  visible: boolean;
  /** 弹窗数据 */
  data: T | null;
  /** 打开弹窗 */
  open: (data?: T) => void;
  /** 关闭弹窗 */
  close: () => void;
  /** 设置弹窗数据 */
  setData: (data: T | null) => void;
}

/**
 * 弹窗状态管理 Hook
 * 
 * @example
 * ```tsx
 * const { visible, data, open, close } = useModal<User>();
 * 
 * // 新增场景
 * <Button onClick={() => open()}>新增</Button>
 * 
 * // 编辑场景
 * <Button onClick={() => open(record)}>编辑</Button>
 * 
 * // 弹窗组件
 * <Modal
 *   visible={visible}
 *   onCancel={close}
 *   onOk={handleSubmit}
 * >
 *   {data ? '编辑' : '新增'}
 * </Modal>
 * ```
 */
export const useModal = <T = any>(): UseModalResult<T> => {
  const [visible, setVisible] = useState(false);
  const [data, setData] = useState<T | null>(null);

  /**
   * 打开弹窗
   */
  const open = useCallback((modalData?: T) => {
    setVisible(true);
    setData(modalData || null);
  }, []);

  /**
   * 关闭弹窗
   */
  const close = useCallback(() => {
    setVisible(false);
    setData(null);
  }, []);

  return {
    visible,
    data,
    open,
    close,
    setData,
  };
};
