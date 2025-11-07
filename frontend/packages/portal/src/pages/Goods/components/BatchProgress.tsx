/**
 * BatchProgress - 批量操作进度展示
 * @author BaSui 😎
 */

import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { getApi } from '@campus/shared/api';

interface BatchProgressProps {
  taskId: number;
}

export const BatchProgress: React.FC<BatchProgressProps> = ({ taskId }) => {
  const api = getApi();

  const { data: progress } = useQuery({
    queryKey: ['batch-task', taskId],
    queryFn: () => api.getBatchTaskProgress(taskId),
    refetchInterval: 1000,
    enabled: !!taskId,
  });

  if (!progress) return null;

  const percent = Math.round(
    (progress.processedCount / progress.totalCount) * 100
  );

  return (
    <div className="batch-progress">
      <div className="batch-progress__bar">
        <div
          className="batch-progress__fill"
          style={{ width: `${percent}%` }}
        />
      </div>
      <div className="batch-progress__text">
        {progress.processedCount} / {progress.totalCount} ({percent}%)
      </div>
    </div>
  );
};
