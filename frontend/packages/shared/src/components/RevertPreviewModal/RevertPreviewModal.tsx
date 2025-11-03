/**
 * 撤销预览弹窗组件
 * @author BaSui 😎
 * @description 展示撤销操作的影响预览和确认
 */

import React, { useState } from 'react';
import { Modal } from '../Modal';
import { Button } from '../Button';
import { Form, FormItem } from '../Form';
import { Input } from '../Input';
import { Tag } from '../Tag';
import { Badge } from '../Badge';
import { toast } from '../Toast';
import type { RevertableOperation } from '../RevertOperationsList';
import './RevertPreviewModal.css';

/**
 * 验证级别
 */
export type ValidationLevel = 'SUCCESS' | 'WARNING' | 'ERROR';

/**
 * 验证结果
 */
export interface ValidationResult {
  valid: boolean;
  message: string;
  level: ValidationLevel;
}

/**
 * 撤销预览数据
 */
export interface RevertPreviewData {
  canRevert: boolean;
  entityType: string;
  entityId: number;
  actionType: string;
  actionTime: string;
  remainingDays: number;
  requiresApproval: boolean;
  validationResult: ValidationResult;
  impactDescription?: string;
  oldValue?: string;
  newValue?: string;
  warnings?: string[];
}

/**
 * RevertPreviewModal 组件属性
 */
export interface RevertPreviewModalProps {
  /** 是否可见 */
  visible: boolean;
  
  /** 关闭回调 */
  onClose: () => void;
  
  /** 预览数据 */
  previewData?: RevertPreviewData;
  
  /** 加载状态 */
  loading?: boolean;
  
  /** 确认撤销回调 */
  onConfirm?: (reason: string) => void | Promise<void>;
  
  /** 提交中状态 */
  submitting?: boolean;
}

/**
 * 撤销预览弹窗组件
 */
export const RevertPreviewModal: React.FC<RevertPreviewModalProps> = ({
  visible,
  onClose,
  previewData,
  loading = false,
  onConfirm,
  submitting = false
}) => {
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');

  // 处理确认
  const handleConfirm = async () => {
    // 验证原因
    if (!reason || reason.trim().length < 10) {
      setError('撤销原因至少需要10个字符');
      return;
    }
    
    if (reason.length > 500) {
      setError('撤销原因不能超过500个字符');
      return;
    }

    try {
      await onConfirm?.(reason.trim());
      setReason('');
      setError('');
    } catch (err: any) {
      toast.error(err.message || '撤销申请失败');
    }
  };

  // 处理取消
  const handleCancel = () => {
    setReason('');
    setError('');
    onClose();
  };

  // 渲染验证结果
  const renderValidationResult = (validation: ValidationResult) => {
    const levelColors = {
      SUCCESS: 'green',
      WARNING: 'orange',
      ERROR: 'red'
    };
    
    const levelIcons = {
      SUCCESS: '✓',
      WARNING: '⚠',
      ERROR: '✗'
    };

    return (
      <div className={`revert-validation revert-validation-${validation.level.toLowerCase()}`}>
        <span className="revert-validation-icon">
          {levelIcons[validation.level]}
        </span>
        <span className="revert-validation-message">
          {validation.message}
        </span>
      </div>
    );
  };

  // 渲染影响描述
  const renderImpactDescription = () => {
    if (!previewData?.impactDescription) return null;

    return (
      <div className="revert-impact-section">
        <h4 className="revert-section-title">影响范围</h4>
        <div className="revert-impact-description">
          {previewData.impactDescription}
        </div>
      </div>
    );
  };

  // 渲染警告信息
  const renderWarnings = () => {
    if (!previewData?.warnings || previewData.warnings.length === 0) return null;

    return (
      <div className="revert-warnings-section">
        <h4 className="revert-section-title">⚠️ 注意事项</h4>
        <ul className="revert-warnings-list">
          {previewData.warnings.map((warning, index) => (
            <li key={index} className="revert-warning-item">
              {warning}
            </li>
          ))}
        </ul>
      </div>
    );
  };

  // 渲染数据对比
  const renderDataComparison = () => {
    if (!previewData?.oldValue && !previewData?.newValue) return null;

    return (
      <div className="revert-comparison-section">
        <h4 className="revert-section-title">数据对比</h4>
        <div className="revert-comparison-content">
          {previewData.oldValue && (
            <div className="revert-comparison-item">
              <div className="revert-comparison-label">
                <Tag color="blue" size="small">原始值</Tag>
              </div>
              <pre className="revert-comparison-value">
                {previewData.oldValue}
              </pre>
            </div>
          )}
          
          {previewData.newValue && (
            <div className="revert-comparison-item">
              <div className="revert-comparison-label">
                <Tag color="green" size="small">当前值</Tag>
              </div>
              <pre className="revert-comparison-value">
                {previewData.newValue}
              </pre>
            </div>
          )}
        </div>
      </div>
    );
  };

  return (
    <Modal
      visible={visible}
      title="撤销操作预览"
      size="large"
      onClose={handleCancel}
      footer={
        <div className="revert-modal-footer">
          <Button type="default" onClick={handleCancel}>
            取消
          </Button>
          <Button
            type="primary"
            onClick={handleConfirm}
            disabled={!previewData?.canRevert || submitting}
            loading={submitting}
          >
            {submitting ? '提交中...' : '确认撤销'}
          </Button>
        </div>
      }
    >
      {loading ? (
        <div className="revert-preview-loading">
          <p>加载预览数据中...</p>
        </div>
      ) : !previewData ? (
        <div className="revert-preview-error">
          <p>无法加载预览数据</p>
        </div>
      ) : (
        <div className="revert-preview-content">
          {/* 基本信息 */}
          <div className="revert-basic-info">
            <div className="revert-info-row">
              <span className="revert-info-label">实体类型：</span>
              <Tag color="blue">{previewData.entityType}</Tag>
            </div>
            <div className="revert-info-row">
              <span className="revert-info-label">实体ID：</span>
              <span>{previewData.entityId}</span>
            </div>
            <div className="revert-info-row">
              <span className="revert-info-label">操作类型：</span>
              <Tag color="orange">{previewData.actionType}</Tag>
            </div>
            <div className="revert-info-row">
              <span className="revert-info-label">操作时间：</span>
              <span>{previewData.actionTime}</span>
            </div>
            <div className="revert-info-row">
              <span className="revert-info-label">剩余时限：</span>
              <Badge 
                status={previewData.remainingDays > 7 ? 'success' : previewData.remainingDays > 3 ? 'warning' : 'error'} 
                text={`${previewData.remainingDays} 天`}
              />
            </div>
            <div className="revert-info-row">
              <span className="revert-info-label">需要审批：</span>
              <Tag color={previewData.requiresApproval ? 'orange' : 'gray'}>
                {previewData.requiresApproval ? '是' : '否'}
              </Tag>
            </div>
          </div>

          {/* 验证结果 */}
          {renderValidationResult(previewData.validationResult)}

          {/* 影响描述 */}
          {renderImpactDescription()}

          {/* 警告信息 */}
          {renderWarnings()}

          {/* 数据对比 */}
          {renderDataComparison()}

          {/* 撤销原因输入 */}
          {previewData.canRevert && (
            <div className="revert-reason-section">
              <h4 className="revert-section-title">
                撤销原因 <span className="required">*</span>
              </h4>
              <Input
                type="textarea"
                value={reason}
                onChange={(e) => {
                  setReason(e.target.value);
                  setError('');
                }}
                placeholder="请详细说明撤销原因（至少10个字符，最多500字符）"
                rows={4}
                maxLength={500}
              />
              {error && (
                <div className="revert-reason-error">{error}</div>
              )}
              <div className="revert-reason-hint">
                {reason.length} / 500 字符
              </div>
            </div>
          )}

          {/* 不可撤销提示 */}
          {!previewData.canRevert && (
            <div className="revert-cannot-revert">
              <p>该操作当前无法撤销</p>
            </div>
          )}
        </div>
      )}
    </Modal>
  );
};

// 类型导出
export type { ValidationResult, RevertPreviewData, ValidationLevel };
