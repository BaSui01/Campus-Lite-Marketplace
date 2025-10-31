/**
 * 帖子 API 服务
 * @author BaSui 😎
 * @description 社区帖子、回复、点赞等接口
 */

import { http } from '../utils/http';
import type {
  ApiResponse,
  PageInfo,
  Post,
  Reply,
  CreatePostRequest,
  UpdatePostRequest,
  PostListQuery,
  CreateReplyRequest,
} from '../types';

/**
 * 帖子 API 服务类
 */
class PostService {
  // ==================== 帖子相关接口 ====================

  /**
   * 创建帖子
   * @param data 创建帖子请求参数
   * @returns 帖子信息
   */
  async createPost(data: CreatePostRequest): Promise<ApiResponse<Post>> {
    return http.post('/posts', data);
  }

  /**
   * 更新帖子
   * @param data 更新帖子请求参数
   * @returns 更新后的帖子信息
   */
  async updatePost(data: UpdatePostRequest): Promise<ApiResponse<Post>> {
    return http.put(`/posts/${data.id}`, data);
  }

  /**
   * 删除帖子
   * @param postId 帖子ID
   * @returns 删除结果
   */
  async deletePost(postId: number): Promise<ApiResponse<void>> {
    return http.delete(`/posts/${postId}`);
  }

  /**
   * 获取帖子详情
   * @param postId 帖子ID
   * @returns 帖子详情
   */
  async getPostById(postId: number): Promise<ApiResponse<Post>> {
    return http.get(`/posts/${postId}`);
  }

  /**
   * 获取帖子列表
   * @param params 查询参数
   * @returns 帖子列表
   */
  async getPosts(params?: PostListQuery): Promise<ApiResponse<PageInfo<Post>>> {
    return http.get('/posts', { params });
  }

  /**
   * 获取我的帖子
   * @param params 查询参数
   * @returns 帖子列表
   */
  async getMyPosts(params?: { page?: number; pageSize?: number }): Promise<ApiResponse<PageInfo<Post>>> {
    return http.get('/posts/my', { params });
  }

  /**
   * 点赞帖子
   * @param postId 帖子ID
   * @returns 点赞结果
   */
  async likePost(postId: number): Promise<ApiResponse<void>> {
    return http.post(`/posts/${postId}/like`);
  }

  /**
   * 取消点赞
   * @param postId 帖子ID
   * @returns 取消结果
   */
  async unlikePost(postId: number): Promise<ApiResponse<void>> {
    return http.delete(`/posts/${postId}/like`);
  }

  /**
   * 置顶帖子（管理员）
   * @param postId 帖子ID
   * @returns 置顶结果
   */
  async pinPost(postId: number): Promise<ApiResponse<void>> {
    return http.post(`/posts/${postId}/pin`);
  }

  /**
   * 取消置顶（管理员）
   * @param postId 帖子ID
   * @returns 取消结果
   */
  async unpinPost(postId: number): Promise<ApiResponse<void>> {
    return http.delete(`/posts/${postId}/pin`);
  }

  // ==================== 回复相关接口 ====================

  /**
   * 创建回复
   * @param data 创建回复请求参数
   * @returns 回复信息
   */
  async createReply(data: CreateReplyRequest): Promise<ApiResponse<Reply>> {
    return http.post('/replies', data);
  }

  /**
   * 删除回复
   * @param replyId 回复ID
   * @returns 删除结果
   */
  async deleteReply(replyId: number): Promise<ApiResponse<void>> {
    return http.delete(`/replies/${replyId}`);
  }

  /**
   * 获取帖子的回复列表
   * @param postId 帖子ID
   * @param params 查询参数
   * @returns 回复列表
   */
  async getReplies(postId: number, params?: { page?: number; pageSize?: number }): Promise<ApiResponse<PageInfo<Reply>>> {
    return http.get(`/posts/${postId}/replies`, { params });
  }

  /**
   * 点赞回复
   * @param replyId 回复ID
   * @returns 点赞结果
   */
  async likeReply(replyId: number): Promise<ApiResponse<void>> {
    return http.post(`/replies/${replyId}/like`);
  }

  /**
   * 取消点赞回复
   * @param replyId 回复ID
   * @returns 取消结果
   */
  async unlikeReply(replyId: number): Promise<ApiResponse<void>> {
    return http.delete(`/replies/${replyId}/like`);
  }
}

// 导出单例
export const postService = new PostService();
export default postService;
