/**
 * LikeButton - 评价点赞按钮
 * @author BaSui 😎
 */

import React from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { getApi } from '@campus/shared/utils/apiClient';

interface LikeButtonProps {
  reviewId: number;
  isLiked: boolean;
  likeCount: number;
}

export const LikeButton: React.FC<LikeButtonProps> = ({
  reviewId,
  isLiked,
  likeCount,
}) => {
  const queryClient = useQueryClient();
  const api = getApi();

  const likeMutation = useMutation({
    mutationFn: () => api.likeReview(reviewId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reviews'] });
    },
  });

  const unlikeMutation = useMutation({
    mutationFn: () => api.unlikeReview(reviewId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reviews'] });
    },
  });

  const handleClick = () => {
    if (isLiked) {
      unlikeMutation.mutate();
    } else {
      likeMutation.mutate();
    }
  };

  return (
    <button
      onClick={handleClick}
      disabled={likeMutation.isPending || unlikeMutation.isPending}
      className={`like-btn ${isLiked ? 'liked' : ''}`}
    >
      <span className="like-icon">{isLiked ? '❤️' : '🤍'}</span>
      <span className="like-count">{likeCount}</span>
    </button>
  );
};
