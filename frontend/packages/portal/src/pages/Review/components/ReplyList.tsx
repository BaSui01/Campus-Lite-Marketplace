/**
 * ReplyList - 评价回复列表
 * @author BaSui 😎
 */

import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { getApi } from '@campus/shared/utils/apiClient';

interface ReplyListProps {
  reviewId: number;
}

export const ReplyList: React.FC<ReplyListProps> = ({ reviewId }) => {
  const api = getApi();

  const { data: replies, isLoading } = useQuery({
    queryKey: ['reviews', reviewId, 'replies'],
    queryFn: () => api.getReviewReplies(reviewId),
  });

  if (isLoading) {
    return <div className="reply-list__loading">加载中...</div>;
  }

  if (!replies || replies.length === 0) {
    return <div className="reply-list__empty">暂无回复</div>;
  }

  return (
    <div className="reply-list">
      {replies.map((reply: any) => (
        <div key={reply.id} className="reply-item">
          <div className="reply-item__header">
            <span className="reply-item__author">{reply.replierUsername}</span>
            <span className="reply-item__time">
              {new Date(reply.createdAt).toLocaleString()}
            </span>
          </div>
          <div className="reply-item__content">{reply.content}</div>
        </div>
      ))}
    </div>
  );
};
