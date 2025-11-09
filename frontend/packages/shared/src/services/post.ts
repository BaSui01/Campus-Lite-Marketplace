/**
 * ✅ 帖子 API 服务 - 完全重构版
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi,零手写路径！
 *
 * 功能:
 * - 帖子 CRUD(创建/查询/更新/删除)
 * - 帖子点赞/取消点赞
 * - 帖子置顶/取消置顶
 * - 回复 CRUD
 * - 回复点赞/取消点赞
 */

import { getApi } from '../utils/apiClient';
import type {
  Post,
  PostReply,
  CreatePostRequest,
  UpdatePostRequest,
  CreatePostReplyRequest,
  PagePostResponse,
  PagePostReplyResponse,
} from '../api';

/**
 * 帖子列表查询参数
 */
export interface PostListParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: string;
}

/**
 * 回复列表查询参数
 */
export interface ReplyListParams {
  page?: number;
  size?: number;
}

/**
 * 帖子 API 服务类
 */
export class PostService {
  // ==================== 帖子相关接口 ====================

  /**
   * 创建帖子
   * @param data 创建帖子请求参数
   * @returns 帖子ID
   */
  async createPost(data: CreatePostRequest): Promise<number> {
    const api = getApi();
    const response = await api.createPost({ createPostRequest: data });
    return response.data.data as number;
  }

  /**
   * 更新帖子
   * @param id 帖子ID
   * @param data 更新帖子请求参数
   * @returns 更新后的帖子信息
   */
  async updatePost(id: number, data: UpdatePostRequest): Promise<Post> {
    const api = getApi();
    const response = await api.updatePost({ id, updatePostRequest: data });
    return response.data.data as Post;
  }

  /**
   * 删除帖子
   * @param id 帖子ID
   */
  async deletePost(id: number): Promise<void> {
    const api = getApi();
    await api.deletePost({ id });
  }

  /**
   * 获取帖子详情
   * @param id 帖子ID
   * @returns 帖子详情
   */
  async getPostById(id: number): Promise<Post> {
    const api = getApi();
    const response = await api.getPostDetail({ id });
    return response.data.data as Post;
  }

  /**
   * 获取帖子列表(分页)
   * @param params 查询参数
   * @returns 帖子列表(分页)
   */
  async getPosts(params?: PostListParams): Promise<PagePostResponse> {
    const api = getApi();
    const response = await api.listPosts({
      page: params?.page,
      size: params?.size,
      sortBy: params?.sortBy,
      sortDirection: params?.sortDirection,
    });
    return response.data.data as PagePostResponse;
  }

  /**
   * 获取作者的帖子列表
   * @param authorId 作者ID
   * @param params 查询参数
   * @returns 帖子列表(分页)
   */
  async getPostsByAuthor(authorId: number, params?: PostListParams): Promise<PagePostResponse> {
    const api = getApi();
    const response = await api.listPostsByAuthor({
      authorId,
      page: params?.page,
      size: params?.size,
    });
    return response.data.data as PagePostResponse;
  }

  /**
   * 点赞帖子
   * @param postId 帖子ID
   */
  async likePost(postId: number): Promise<void> {
    const api = getApi();
    await api.likePost({ postId });
  }

  /**
   * 取消点赞帖子
   * @param postId 帖子ID
   */
  async unlikePost(postId: number): Promise<void> {
    const api = getApi();
    await api.unlikePost({ postId });
  }

  /**
   * 获取帖子点赞数
   * @param postId 帖子ID
   * @returns 点赞数
   */
  async getPostLikeCount(postId: number): Promise<number> {
    const api = getApi();
    const response = await api.getPostLikeCount({ postId });
    return response.data.data as number;
  }

  /**
   * 获取帖子收藏数
   * @param postId 帖子ID
   * @returns 收藏数
   */
  async getPostCollectCount(postId: number): Promise<number> {
    const api = getApi();
    const response = await api.getPostCollectCount({ postId });
    return response.data.data as number;
  }

  // ==================== 回复相关接口 ====================

  /**
   * 创建回复
   * @param data 创建回复请求参数
   * @returns 回复ID
   */
  async createReply(data: CreatePostReplyRequest): Promise<number> {
    const api = getApi();
    const response = await api.createReply1({ createPostReplyRequest: data });
    return response.data.data as number;
  }

  /**
   * 删除回复
   * @param id 回复ID
   */
  async deleteReply(id: number): Promise<void> {
    const api = getApi();
    await api.deleteReply1({ id });
  }

  /**
   * 获取帖子的回复列表
   * @param postId 帖子ID
   * @param params 查询参数
   * @returns 回复列表(分页)
   */
  async getReplies(postId: number, params?: ReplyListParams): Promise<PagePostReplyResponse> {
    const api = getApi();
    const response = await api.listReplies({
      postId,
      page: params?.page,
      size: params?.size,
    });
    return response.data.data as PagePostReplyResponse;
  }

  /**
   * 审核帖子（管理员）
   * @param postId 帖子ID
   * @param data 审核数据
   */
  async auditPost(postId: number, data: { approved: boolean; reason?: string }): Promise<void> {
    const api = getApi();
    await api.auditPost({ postId, auditPostRequest: data });
  }
}

/**
 * 帖子服务实例
 */
export const postService = new PostService();

/**
 * 导出单例
 */
export default postService;
