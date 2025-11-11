/**
 * 可撤销操作列表页面 - Portal端
 * @author BaSui 😎
 * @description 用户查看和申请可撤销的操作
 */

import React, { useState, useCallback } from 'react';
import { 
  RevertOperationsList, 
  RevertPreviewModal,
  Loading,
  toast,
  Services,
  type RevertableOperation,
  type RevertListParams,
  type RevertPreviewData
} from '@campus/shared';
import './index.css';

// 🔧 BaSui 修复：从 Services 命名空间解构
const { revertService } = Services;

/**
 * 可撤销操作页面
 */
const RevertOperations: React.FC = () => {
  // 状态管理
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<RevertableOperation[]>([]);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  
  const [previewModalVisible, setPreviewModalVisible] = useState(false);
  const [previewData, setPreviewData] = useState<RevertPreviewData | undefined>();
  const [previewLoading, setPreviewLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [selectedOperation, setSelectedOperation] = useState<RevertableOperation | null>(null);

  // 模拟数据（后续接入真实API）
  const mockData: RevertableOperation[] = [
    {
      auditLogId: 1001,
      entityType: 'Goods',
      entityId: 123,
      entityName: 'iPhone 13 Pro Max 256GB',
      actionType: 'DELETE',
      actionDescription: '删除商品',
      actionTime: '2025-11-01T10:30:00',
      revertDeadline: '2025-12-01T10:30:00',
      remainingDays: 28,
      isReversible: true,
      requiresApproval: false
    },
    {
      auditLogId: 1002,
      entityType: 'Order',
      entityId: 456,
      entityName: '订单号: ORD20251101001',
      actionType: 'UPDATE',
      actionDescription: '更新订单状态为已完成',
      actionTime: '2025-11-02T14:20:00',
      revertDeadline: '2025-11-09T14:20:00',
      remainingDays: 5,
      isReversible: true,
      requiresApproval: true,
      existingRequest: {
        requestId: 2001,
        status: 'PENDING',
        requestedAt: '2025-11-03T09:00:00'
      }
    },
    {
      auditLogId: 1003,
      entityType: 'Goods',
      entityId: 789,
      entityName: 'MacBook Pro 14寸 M3',
      actionType: 'UPDATE',
      actionDescription: '修改商品价格',
      actionTime: '2025-10-28T16:45:00',
      revertDeadline: '2025-11-27T16:45:00',
      remainingDays: 24,
      isReversible: true,
      requiresApproval: false
    }
  ];

  // 加载数据（模拟）
  React.useEffect(() => {
    loadData();
  }, [currentPage, pageSize]);

  const loadData = async () => {
    setLoading(true);
    
    try {
      // TODO: 接入真实API
      // const response = await revertService.getUserRevertRequests({
      //   page: currentPage - 1,
      //   size: pageSize
      // });
      
      // 模拟网络延迟
      await new Promise(resolve => setTimeout(resolve, 500));
      
      setData(mockData);
      setTotal(mockData.length);
    } catch (error: any) {
      toast.error(error.message || '加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  // 处理预览
  const handlePreview = useCallback(async (operation: RevertableOperation) => {
    setSelectedOperation(operation);
    setPreviewModalVisible(true);
    setPreviewLoading(true);

    try {
      // TODO: 调用预览API
      // const preview = await revertService.previewRevert(operation.auditLogId);
      
      // 模拟网络延迟
      await new Promise(resolve => setTimeout(resolve, 800));
      
      // 模拟预览数据
      const mockPreview: RevertPreviewData = {
        canRevert: operation.isReversible,
        entityType: operation.entityType,
        entityId: operation.entityId,
        actionType: operation.actionType,
        actionTime: operation.actionTime,
        remainingDays: operation.remainingDays,
        requiresApproval: operation.requiresApproval,
        validationResult: {
          valid: true,
          message: '验证通过，可以撤销此操作',
          level: 'SUCCESS'
        },
        impactDescription: '撤销此操作将恢复实体的原始状态，不会影响其他关联数据',
        warnings: operation.requiresApproval ? [
          '此操作需要管理员审批，可能需要1-3个工作日',
          '撤销后实体将恢复到操作前的状态'
        ] : undefined,
        oldValue: operation.actionType === 'UPDATE' ? '{"status": "PAID", "amount": 9999}' : undefined,
        newValue: operation.actionType === 'UPDATE' ? '{"status": "COMPLETED", "amount": 9999}' : undefined
      };
      
      setPreviewData(mockPreview);
    } catch (error: any) {
      toast.error(error.message || '加载预览失败');
      setPreviewModalVisible(false);
    } finally {
      setPreviewLoading(false);
    }
  }, []);

  // 处理申请撤销
  const handleRequestRevert = useCallback((operation: RevertableOperation) => {
    handlePreview(operation);
  }, [handlePreview]);

  // 处理撤销确认
  const handleConfirmRevert = useCallback(async (reason: string) => {
    if (!selectedOperation) return;

    setSubmitting(true);
    
    try {
      // TODO: 调用撤销申请API
      // await revertService.requestRevert(selectedOperation.auditLogId, { reason });
      
      // 模拟网络延迟
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      toast.success(
        selectedOperation.requiresApproval 
          ? '撤销申请已提交，等待审批' 
          : '撤销操作已提交，正在处理中'
      );
      
      setPreviewModalVisible(false);
      setSelectedOperation(null);
      
      // 重新加载数据
      loadData();
    } catch (error: any) {
      toast.error(error.message || '提交撤销申请失败');
    } finally {
      setSubmitting(false);
    }
  }, [selectedOperation, loadData]);

  // 处理分页变化
  const handlePageChange = useCallback((page: number, size: number) => {
    setCurrentPage(page);
    setPageSize(size);
  }, []);

  // 关闭预览弹窗
  const handleClosePreview = useCallback(() => {
    setPreviewModalVisible(false);
    setSelectedOperation(null);
    setPreviewData(undefined);
  }, []);

  return (
    <div className="revert-operations-page">
      <div className="page-header">
        <h1 className="page-title">🔄 我的可撤销操作</h1>
        <p className="page-description">
          查看您可以撤销的操作，支持商品删除、订单状态变更、用户信息修改等操作的撤销
        </p>
      </div>

      <div className="page-content">
        <RevertOperationsList
          loading={loading}
          data={data}
          total={total}
          currentPage={currentPage}
          pageSize={pageSize}
          onPageChange={handlePageChange}
          onPreview={handlePreview}
          onRequestRevert={handleRequestRevert}
        />
      </div>

      {/* 撤销预览弹窗 */}
      <RevertPreviewModal
        visible={previewModalVisible}
        onClose={handleClosePreview}
        previewData={previewData}
        loading={previewLoading}
        onConfirm={handleConfirmRevert}
        submitting={submitting}
      />
    </div>
  );
};

export default RevertOperations;
