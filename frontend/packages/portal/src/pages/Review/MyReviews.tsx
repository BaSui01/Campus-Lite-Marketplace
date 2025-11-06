/**
 * MyReviews - 我的评价页
 * @author BaSui 😎
 * @description 展示用户发布的所有评价，支持编辑、删除
 */

import React, { useState, useEffect, useMemo } from 'react';
import { Empty, Skeleton, Pagination, Modal } from '@campus/shared/components';
import { useReviewStore } from '../../store/useReviewStore';
import { ReviewCard } from '../../components/ReviewCard';
import './MyReviews.css';

/**
 * 判断评价是否可编辑（24小时内）
 */
const isReviewEditable = (createdAt: string): boolean => {
  const now = new Date();
  const created = new Date(createdAt);
  const diffInHours = (now.getTime() - created.getTime()) / (1000 * 60 * 60);
  return diffInHours < 24;
};

/**
 * MyReviews 组件
 */
const MyReviews: React.FC = () => {
  // 状态管理
  const {
    myReviews,
    totalPages,
    totalElements,
    currentPage,
    loading,
    fetchMyReviews,
    deleteReview,
  } = useReviewStore();

  // 本地状态
  const [page, setPage] = useState(0);
  const [editingReviewId, setEditingReviewId] = useState<number | null>(null);
  const [deletingReviewId, setDeletingReviewId] = useState<number | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);

  // 初始化：加载评价列表
  useEffect(() => {
    fetchMyReviews({ page, size: 10 });
  }, [page, fetchMyReviews]);

  // 当前编辑的评价
  const editingReview = useMemo(() => {
    return myReviews.find((r) => r.id === editingReviewId);
  }, [myReviews, editingReviewId]);

  // ==================== 事件处理 ====================

  /**
   * 处理编辑
   */
  const handleEdit = (reviewId: number) => {
    setEditingReviewId(reviewId);
    setShowEditModal(true);
  };

  /**
   * 处理删除
   */
  const handleDelete = (reviewId: number) => {
    setDeletingReviewId(reviewId);
    setShowDeleteConfirm(true);
  };

  /**
   * 确认删除
   */
  const handleConfirmDelete = async () => {
    if (deletingReviewId) {
      try {
        await deleteReview(deletingReviewId);
        setShowDeleteConfirm(false);
        setDeletingReviewId(null);
        // 刷新列表
        fetchMyReviews({ page, size: 10 });
      } catch (error) {
        console.error('删除失败:', error);
      }
    }
  };

  /**
   * 取消删除
   */
  const handleCancelDelete = () => {
    setShowDeleteConfirm(false);
    setDeletingReviewId(null);
  };

  /**
   * 关闭编辑弹窗
   */
  const handleCloseEditModal = () => {
    setShowEditModal(false);
    setEditingReviewId(null);
  };

  /**
   * 切换页码
   */
  const handlePageChange = (newPage: number) => {
    setPage(newPage - 1); // Pagination 组件从1开始，store从0开始
  };

  // ==================== 渲染逻辑 ====================

  // 加载状态
  if (loading && myReviews.length === 0) {
    return (
      <div className="my-reviews">
        <div className="my-reviews__container">
          <h1 className="my-reviews__title">我的评价</h1>
          <Skeleton type="list" count={3} />
        </div>
      </div>
    );
  }

  // 空状态
  if (myReviews.length === 0) {
    return (
      <div className="my-reviews">
        <div className="my-reviews__container">
          <h1 className="my-reviews__title">我的评价</h1>
          <Empty
            icon="💬"
            title="暂无评价"
            description="您还没有发布任何评价"
          />
        </div>
      </div>
    );
  }

  return (
    <div className="my-reviews">
      <div className="my-reviews__container">
        {/* 页面标题 */}
        <div className="my-reviews__header">
          <h1 className="my-reviews__title">我的评价</h1>
          <span className="my-reviews__count">共 {totalElements} 条</span>
        </div>

        {/* 评价列表 */}
        <div className="my-reviews__list">
          {myReviews.map((review) => {
            const canEdit = isReviewEditable(review.createdAt || '');
            
            return (
              <ReviewCard
                key={review.id}
                review={review}
                showActions
                onEdit={canEdit ? handleEdit : undefined}
                onDelete={handleDelete}
              />
            );
          })}
        </div>

        {/* 分页 */}
        {totalPages > 1 && (
          <div className="my-reviews__pagination">
            <Pagination
              current={currentPage + 1}
              total={totalElements}
              pageSize={10}
              onChange={handlePageChange}
            />
          </div>
        )}
      </div>

      {/* 删除确认弹窗 */}
      <Modal
        open={showDeleteConfirm}
        title="确认删除"
        onClose={handleCancelDelete}
        size="small"
      >
        <div className="my-reviews__delete-confirm">
          <p>确定要删除这条评价吗？删除后无法恢复。</p>
          <div className="my-reviews__delete-actions">
            <button
              className="my-reviews__btn my-reviews__btn--cancel"
              onClick={handleCancelDelete}
            >
              取消
            </button>
            <button
              className="my-reviews__btn my-reviews__btn--danger"
              onClick={handleConfirmDelete}
            >
              确认
            </button>
          </div>
        </div>
      </Modal>

      {/* 编辑弹窗 */}
      <Modal
        open={showEditModal}
        title="编辑评价"
        onClose={handleCloseEditModal}
        size="medium"
      >
        <div className="my-reviews__edit-form">
          <p className="my-reviews__edit-notice">
            编辑功能开发中，敬请期待...
          </p>
          {editingReview && (
            <div className="my-reviews__edit-preview">
              <p><strong>当前评价内容：</strong></p>
              <p>{editingReview.content}</p>
            </div>
          )}
          <div className="my-reviews__edit-actions">
            <button
              className="my-reviews__btn my-reviews__btn--primary"
              onClick={handleCloseEditModal}
            >
              关闭
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default MyReviews;
