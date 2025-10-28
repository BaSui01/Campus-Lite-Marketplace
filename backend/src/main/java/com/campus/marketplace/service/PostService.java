package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreatePostRequest;
import com.campus.marketplace.common.dto.response.PostResponse;
import org.springframework.data.domain.Page;

/**
 * 帖子服务接口
 * 
 * 提供论坛帖子的发布、查询、审核等功能
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public interface PostService {

    /**
     * 发布帖子
     * 
     * 功能说明：
     * 1. 验证用户登录状态
     * 2. 对标题和内容进行敏感词过滤
     * 3. 检查每日发帖数量限制（Redis 限流）
     * 4. 创建帖子，状态设置为 PENDING（待审核）
     * 5. 更新 Redis 中的发帖计数
     * 
     * @param request 发帖请求
     * @return 帖子 ID
     * @throws com.campus.marketplace.common.exception.BusinessException 用户不存在、超过每日限制
     */
    Long createPost(CreatePostRequest request);

    /**
     * 查询帖子列表（分页）
     * 
     * @param page 页码（从 0 开始）
     * @param size 每页大小
     * @param sortBy 排序字段（createdAt, viewCount, replyCount）
     * @param sortDirection 排序方向（ASC, DESC）
     * @return 帖子分页结果
     */
    Page<PostResponse> listPosts(int page, int size, String sortBy, String sortDirection);

    /**
     * 查询指定用户的帖子列表
     * 
     * @param authorId 作者 ID
     * @param page 页码
     * @param size 每页大小
     * @return 帖子分页结果
     */
    Page<PostResponse> listPostsByAuthor(Long authorId, int page, int size);

    /**
     * 搜索帖子（按关键词）
     * 
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页大小
     * @return 帖子分页结果
     */
    Page<PostResponse> searchPosts(String keyword, int page, int size);

    /**
     * 获取帖子详情
     * 
     * @param id 帖子 ID
     * @return 帖子详情
     * @throws com.campus.marketplace.common.exception.BusinessException 帖子不存在
     */
    PostResponse getPostDetail(Long id);

    /**
     * 审核帖子（管理员）
     * 
     * @param id 帖子 ID
     * @param approved 是否通过
     * @param reason 拒绝原因（可选）
     * @throws com.campus.marketplace.common.exception.BusinessException 帖子不存在
     */
    void approvePost(Long id, boolean approved, String reason);

    /**
     * 修改帖子（作者或管理员）
     *
     * @param id 帖子ID
     * @param request 修改内容
     */
    void updatePost(Long id, com.campus.marketplace.common.dto.request.UpdatePostRequest request);

    /**
     * 删除帖子（作者或管理员）
     * 
     * @param id 帖子 ID
     * @throws com.campus.marketplace.common.exception.BusinessException 帖子不存在、无权限
     */
    void deletePost(Long id);
}
