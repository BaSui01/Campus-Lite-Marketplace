/**
 * BatchSelector - 批量选择组件
 * @author BaSui 😎
 */

import React from 'react';

interface BatchSelectorProps {
  selectedIds: number[];
  onSelectAll: () => void;
  onClearAll: () => void;
  totalCount: number;
}

export const BatchSelector: React.FC<BatchSelectorProps> = ({
  selectedIds,
  onSelectAll,
  onClearAll,
  totalCount,
}) => {
  const selectedCount = selectedIds.length;
  const isAllSelected = selectedCount === totalCount && totalCount > 0;

  return (
    <div className="batch-selector">
      <input
        type="checkbox"
        checked={isAllSelected}
        onChange={isAllSelected ? onClearAll : onSelectAll}
      />
      <span className="batch-selector__text">
        {selectedCount > 0 ? `已选 ${selectedCount} 项` : '全选'}
      </span>
      {selectedCount > 0 && (
        <button onClick={onClearAll} className="batch-selector__clear">
          清空
        </button>
      )}
    </div>
  );
};
