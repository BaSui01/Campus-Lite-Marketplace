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

    // ==================== 新增接口（2025-11-09 - BaSui 😎）====================

    /**
     * 查询待审核帖子列表（管理员）
     *
     * @param page 页码（从 0 开始）
     * @param size 每页大小
     * @return 待审核帖子分页结果
     * @since 2025-11-09
     */
    Page<PostResponse> listPendingPosts(int page, int size);

    /**
     * 查询热门帖子列表
     *
     * 热度计算：点赞数 * 2 + 浏览量 + 回复数 * 3
     *
     * @param page 页码（从 0 开始）
     * @param size 每页大小
     * @return 热门帖子分页结果
     * @since 2025-11-09
     */
    Page<PostResponse> listHotPosts(int page, int size);

    /**
     * 查询我的点赞列表
     *
     * @param userId 用户 ID
     * @param page 页码
     * @param size 每页大小
     * @return 点赞的帖子分页结果
     * @since 2025-11-09
     */
    Page<PostResponse> listUserLikes(Long userId, int page, int size);

    /**
     * 查询我的收藏列表
     *
     * @param userId 用户 ID
     * @param page 页码
     * @param size 每页大小
     * @return 收藏的帖子分页结果
     * @since 2025-11-09
     */
    Page<PostResponse> listUserCollects(Long userId, int page, int size);

    /**
     * 置顶/取消置顶帖子（管理员）
     *
     * @param id 帖子 ID
     * @param isTop 是否置顶
     * @throws com.campus.marketplace.common.exception.BusinessException 帖子不存在
     * @since 2025-11-09
     */
    void toggleTopPost(Long id, boolean isTop);

    /**
     * 批量审核帖子（管理员）
     *
     * @param ids 帖子 ID 列表
     * @param approved 是否通过
     * @param reason 拒绝原因（可选）
     * @return 成功审核的数量
     * @since 2025-11-09
     */
    int batchApprovePosts(java.util.List<Long> ids, boolean approved, String reason);

    /**
     * 获取帖子统计信息
     *
     * @param id 帖子 ID
     * @return 统计信息（点赞用户列表、收藏用户列表等）
     * @throws com.campus.marketplace.common.exception.BusinessException 帖子不存在
     * @since 2025-11-09
     */
    com.campus.marketplace.common.dto.response.PostStatsResponse getPostStats(Long id);
}
