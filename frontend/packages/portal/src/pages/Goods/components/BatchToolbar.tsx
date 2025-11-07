/**
 * BatchToolbar - 批量操作工具栏
 * @author BaSui 😎
 */

import React from 'react';
import { useMutation } from '@tanstack/react-query';
import { getApi } from '@campus/shared/api';

interface BatchToolbarProps {
  selectedIds: number[];
  onSuccess?: () => void;
}

export const BatchToolbar: React.FC<BatchToolbarProps> = ({
  selectedIds,
  onSuccess,
}) => {
  const api = getApi();

  const batchOnlineMutation = useMutation({
    mutationFn: (ids: number[]) =>
      api.batchOnlineGoods({ operation: 'BATCH_ONLINE', targetIds: ids }),
    onSuccess,
  });

  const batchOfflineMutation = useMutation({
    mutationFn: (ids: number[]) =>
      api.batchOfflineGoods({ operation: 'BATCH_OFFLINE', targetIds: ids }),
    onSuccess,
  });

  const batchDeleteMutation = useMutation({
    mutationFn: (ids: number[]) =>
      api.batchDeleteGoods({ operation: 'BATCH_DELETE', targetIds: ids }),
    onSuccess,
  });

  const disabled = selectedIds.length === 0;

  return (
    <div className="batch-toolbar">
      <button
        onClick={() => batchOnlineMutation.mutate(selectedIds)}
        disabled={disabled || batchOnlineMutation.isPending}
        className="batch-toolbar__btn"
      >
        批量上架
      </button>
      <button
        onClick={() => batchOfflineMutation.mutate(selectedIds)}
        disabled={disabled || batchOfflineMutation.isPending}
        className="batch-toolbar__btn"
      >
        批量下架
      </button>
      <button
        onClick={() => batchDeleteMutation.mutate(selectedIds)}
        disabled={disabled || batchDeleteMutation.isPending}
        className="batch-toolbar__btn batch-toolbar__btn--danger"
      >
        批量删除
      </button>
    </div>
  );
};
