/**
 * ReplyInput - 评价回复输入框
 * @author BaSui 😎
 */

import React, { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { getApi } from '@campus/shared/utils/apiClient';

interface ReplyInputProps {
  reviewId: number;
  onSuccess?: () => void;
}

export const ReplyInput: React.FC<ReplyInputProps> = ({
  reviewId,
  onSuccess,
}) => {
  const [content, setContent] = useState('');
  const queryClient = useQueryClient();
  const api = getApi();

  const replyMutation = useMutation({
    mutationFn: (data: { content: string }) =>
      api.replyToReview(reviewId, data),
    onSuccess: () => {
      setContent('');
      queryClient.invalidateQueries({ queryKey: ['reviews', reviewId] });
      onSuccess?.();
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (content.trim()) {
      replyMutation.mutate({ content: content.trim() });
    }
  };

  return (
    <form onSubmit={handleSubmit} className="reply-input">
      <textarea
        value={content}
        onChange={(e) => setContent(e.target.value)}
        placeholder="写下你的回复..."
        maxLength={500}
        rows={3}
        disabled={replyMutation.isPending}
      />
      <div className="reply-input__footer">
        <span className="reply-input__count">{content.length}/500</span>
        <button
          type="submit"
          disabled={!content.trim() || replyMutation.isPending}
          className="reply-input__btn"
        >
          {replyMutation.isPending ? '发送中...' : '发送'}
        </button>
      </div>
    </form>
  );
};
