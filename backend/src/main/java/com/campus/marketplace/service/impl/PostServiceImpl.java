package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.CreatePostRequest;
import com.campus.marketplace.common.dto.response.PostResponse;
import com.campus.marketplace.common.entity.Post;
import com.campus.marketplace.common.entity.PostTag;
import com.campus.marketplace.common.entity.Tag;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.security.PermissionCodes;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.common.utils.SensitiveWordFilter;
import com.campus.marketplace.repository.PostRepository;
import com.campus.marketplace.repository.PostTagRepository;
import com.campus.marketplace.repository.TagRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.PostService;
import com.campus.marketplace.service.MessageService;
import com.campus.marketplace.common.dto.request.SendMessageRequest;
import com.campus.marketplace.common.dto.request.UpdatePostRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.data.domain.PageImpl;

/**
 * 帖子服务实现类
 *
 * 提供论坛帖子的发布、查询、审核等功能
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final com.campus.marketplace.repository.PostLikeRepository postLikeRepository;
    private final com.campus.marketplace.repository.PostCollectRepository postCollectRepository;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final com.campus.marketplace.service.ComplianceService complianceService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MessageService messageService;

    /**
     * 每日发帖限制（可配置化）
     */
    private static final int DAILY_POST_LIMIT = 10;

    /**
     * Redis 键前缀：每日发帖计数
     */
    private static final String POST_LIMIT_KEY_PREFIX = "post:limit:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(CreatePostRequest request) {
        log.info("用户发帖: title={}, contentLength={}", request.title(), request.content().length());

        // 1. 获取当前登录用户
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 检查每日发帖限制（Redis 限流）
        String limitKey = POST_LIMIT_KEY_PREFIX + user.getId();
        Long postCount = redisTemplate.opsForValue().increment(limitKey, 0L); // 先读取当前值
        
        if (postCount == null) {
            postCount = 0L;
        }
        
        if (postCount >= DAILY_POST_LIMIT) {
            log.warn("用户超过每日发帖限制: userId={}, postCount={}", user.getId(), postCount);
            throw new BusinessException(ErrorCode.POST_LIMIT_EXCEEDED);
        }

        // 3. 文本合规
        String filteredTitle;
        String filteredContent;
        if (complianceService != null) {
            var titleMod = complianceService.moderateText(request.title(), "POST_TITLE");
            var contentMod = complianceService.moderateText(request.content(), "POST_CONTENT");
            if (titleMod.hit() && titleMod.action() == com.campus.marketplace.common.enums.ComplianceAction.BLOCK
                    || contentMod.hit() && contentMod.action() == com.campus.marketplace.common.enums.ComplianceAction.BLOCK) {
                throw new com.campus.marketplace.common.exception.BusinessException(
                        com.campus.marketplace.common.exception.ErrorCode.INVALID_PARAM, "包含敏感词，请修改后再发布");
            }
            filteredTitle = titleMod.filteredText();
            filteredContent = contentMod.filteredText();
        } else {
            filteredTitle = sensitiveWordFilter.filter(request.title());
            filteredContent = sensitiveWordFilter.filter(request.content());
        }

        // 4. 创建帖子
        Post post = Post.builder()
                .title(filteredTitle)
                .content(filteredContent)
                .authorId(user.getId())
                .campusId(user.getCampusId())
                .status(GoodsStatus.PENDING) // 默认待审核；命中REVIEW保持待审核
                .viewCount(0)
                .replyCount(0)
                .images(request.images() != null && !request.images().isEmpty() 
                        ? request.images().toArray(new String[0]) 
                        : null)
                .build();

        // 3.1 图片合规
        if (request.images() != null && !request.images().isEmpty() && complianceService != null) {
            var imgRes = complianceService.scanImages(request.images(), "POST_IMAGES");
            if (imgRes.action() == com.campus.marketplace.service.ComplianceService.ImageAction.REJECT) {
                throw new com.campus.marketplace.common.exception.BusinessException(
                        com.campus.marketplace.common.exception.ErrorCode.INVALID_PARAM, "图片未通过安全检测");
            }
            // REVIEW 情况保留 PENDING 状态
        }

        // 5. 保存帖子
        postRepository.save(post);
        syncPostTags(post.getId(), request.tagIds());

        // 6. 更新 Redis 发帖计数（+1）
        redisTemplate.opsForValue().increment(limitKey, 1L);
        // 设置过期时间为 1 天
        redisTemplate.expire(limitKey, 1, TimeUnit.DAYS);

        log.info("帖子发布成功: postId={}, authorId={}, title={}", post.getId(), user.getId(), post.getTitle());

        return post.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> listPosts(int page, int size, String sortBy, String sortDirection) {
        log.info("查询帖子列表: page={}, size={}, sortBy={}, sortDirection={}", page, size, sortBy, sortDirection);

        // 构建排序
        Sort sort = Sort.by(sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 校区过滤：普通用户仅看本校，跨校权限不过滤
        Long campusFilter = null;
        try {
            if (SecurityUtil.isAuthenticated() && !SecurityUtil.hasAuthority(PermissionCodes.SYSTEM_CAMPUS_CROSS)) {
                String username = SecurityUtil.getCurrentUsername();
                User u = userRepository.findByUsername(username).orElse(null);
                campusFilter = u != null ? u.getCampusId() : null;
            }
        } catch (Exception ignored) { }

        Page<Post> postPage = (campusFilter == null)
                ? postRepository.findByStatus(GoodsStatus.APPROVED, pageable)
                : postRepository.findByStatusAndCampusId(GoodsStatus.APPROVED, campusFilter, pageable);

        return postPage.map(PostResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> listPostsByAuthor(Long authorId, int page, int size) {
        log.info("查询指定用户的帖子列表: authorId={}, page={}, size={}", authorId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage = postRepository.findByAuthorId(authorId, pageable);

        return postPage.map(PostResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(String keyword, int page, int size) {
        log.info("搜索帖子: keyword={}, page={}, size={}", keyword, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Long campusFilter = null;
        try {
            if (SecurityUtil.isAuthenticated() && !SecurityUtil.hasAuthority(PermissionCodes.SYSTEM_CAMPUS_CROSS)) {
                String username = SecurityUtil.getCurrentUsername();
                User u = userRepository.findByUsername(username).orElse(null);
                campusFilter = u != null ? u.getCampusId() : null;
            }
        } catch (Exception ignored) { }

        Page<Post> postPage = (campusFilter == null)
                ? postRepository.searchPosts(GoodsStatus.APPROVED, keyword, pageable)
                : postRepository.searchPostsWithCampus(GoodsStatus.APPROVED, keyword, campusFilter, pageable);

        return postPage.map(PostResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostDetail(Long id) {
        log.info("获取帖子详情: postId={}", id);

        Post post = postRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 校区鉴权
        try {
            if (SecurityUtil.isAuthenticated() && !SecurityUtil.hasAuthority(PermissionCodes.SYSTEM_CAMPUS_CROSS)) {
                String username = SecurityUtil.getCurrentUsername();
                User current = userRepository.findByUsername(username).orElse(null);
                if (current != null && post.getCampusId() != null && current.getCampusId() != null
                        && !post.getCampusId().equals(current.getCampusId())) {
                    throw new BusinessException(ErrorCode.FORBIDDEN, "跨校区访问被禁止");
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception ignored) { }

        // 异步增加浏览量（避免影响查询性能）
        incrementViewCountAsync(post);

        return PostResponse.fromWithAuthor(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approvePost(Long id, boolean approved, String reason) {
        log.info("审核帖子: postId={}, approved={}, reason={}", id, approved, reason);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 更新状态
        post.setStatus(approved ? GoodsStatus.APPROVED : GoodsStatus.REJECTED);
        postRepository.save(post);

        // 发送审核结果通知给作者（需求 37）
        try {
            String content = approved
                    ? String.format("您发布的帖子《%s》已审核通过！", post.getTitle())
                    : String.format("您发布的帖子《%s》未通过审核。原因：%s", post.getTitle(), reason);

            SendMessageRequest msg = new SendMessageRequest(
                    post.getAuthorId(),
                    com.campus.marketplace.common.enums.MessageType.SYSTEM,
                    content
            );
            messageService.sendMessage(msg);
            log.info("✅ 帖子审核通知已发送: postId={}, authorId={}, approved={}", id, post.getAuthorId(), approved);
        } catch (Exception e) {
            log.error("⚠️ 发送帖子审核通知失败: postId={}, authorId={}", id, post.getAuthorId(), e);
        }
        log.info("帖子审核完成: postId={}, status={}", id, post.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long id) {
        log.info("删除帖子: postId={}", id);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 验证权限（作者或管理员）
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!post.getAuthorId().equals(user.getId()) && !user.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 软删除
        post.markDeleted();
        postRepository.save(post);
        log.info("帖子软删除成功: postId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(Long id, UpdatePostRequest request) {
        log.info("修改帖子: postId={}", id);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 鉴权：作者或管理员
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!post.getAuthorId().equals(user.getId()) && !user.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        String newTitle = request.title() != null ? request.title() : post.getTitle();
        String newContent = request.content() != null ? request.content() : post.getContent();

        // 合规/敏感词过滤
        String filteredTitle;
        String filteredContent;
        if (complianceService != null) {
            var titleMod = complianceService.moderateText(newTitle, "POST_TITLE");
            var contentMod = complianceService.moderateText(newContent, "POST_CONTENT");
            if (titleMod.hit() && titleMod.action() == com.campus.marketplace.common.enums.ComplianceAction.BLOCK
                    || contentMod.hit() && contentMod.action() == com.campus.marketplace.common.enums.ComplianceAction.BLOCK) {
                throw new com.campus.marketplace.common.exception.BusinessException(
                        com.campus.marketplace.common.exception.ErrorCode.INVALID_PARAM, "包含敏感词，请修改后再提交");
            }
            filteredTitle = titleMod.filteredText();
            filteredContent = contentMod.filteredText();
        } else {
            filteredTitle = sensitiveWordFilter.filter(newTitle);
            filteredContent = sensitiveWordFilter.filter(newContent);
        }

        boolean contentChanged = !filteredTitle.equals(post.getTitle()) || !filteredContent.equals(post.getContent());

        post.setTitle(filteredTitle);
        post.setContent(filteredContent);
        if (request.images() != null) {
            post.setImages(request.images().isEmpty() ? null : request.images().toArray(new String[0]));
        }

        // 内容变更后重置为待审核
        if (contentChanged) {
            post.setStatus(GoodsStatus.PENDING);
        }

        postRepository.save(post);
        syncPostTags(post.getId(), request.tagIds());
        log.info("帖子修改成功: postId={}, resetToPending={}", id, contentChanged);
    }

    /**
     * 异步增加浏览量（避免阻塞查询）
     */
    private void incrementViewCountAsync(Post post) {
        // 使用虚拟线程异步执行
        Thread.ofVirtual().start(() -> {
            try {
                post.incrementViewCount();
                postRepository.save(post);
            } catch (Exception e) {
                log.warn("增加浏览量失败: postId={}, error={}", post.getId(), e.getMessage());
            }
        });
    }

    /**
     * 同步帖子标签
     *
     * @param postId 帖子ID
     * @param tagIds 标签ID列表
     * @author BaSui 😎
     */
    private void syncPostTags(Long postId, List<Long> tagIds) {
        // 1. 先删除旧关联
        postTagRepository.deleteByPostId(postId);

        // 2. 如果标签列表为空，直接返回
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        // 3. 去重并过滤空值
        List<Long> distinct = tagIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 4. 校验标签数量限制
        if (distinct.size() > 10) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "最多绑定 10 个标签");
        }

        // 5. 校验标签是否存在
        List<Tag> tags = StreamSupport.stream(
                        tagRepository.findAllById(distinct).spliterator(), false)
                .toList();

        if (tags.size() != distinct.size()) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND, "存在已失效的标签");
        }

        // 6. 校验标签是否被禁用
        tags.forEach(tag -> {
            if (Boolean.FALSE.equals(tag.getEnabled())) {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "标签已被禁用: " + tag.getName());
            }
        });

        // 7. 创建新关联
        distinct.forEach(tagId -> postTagRepository.save(
                PostTag.builder().postId(postId).tagId(tagId).build()
        ));

        log.info("帖子标签同步成功: postId=, tagIds={}", postId, distinct);
    }

    // ==================== 新增方法实现（2025-11-09 - BaSui 😎）====================

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> listPendingPosts(int page, int size) {
        log.info("查询待审核帖子列表: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findByStatus(GoodsStatus.PENDING, pageable);

        return posts.map(PostResponse::fromWithAuthor);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> listHotPosts(int page, int size) {
        log.info("查询热门帖子列表: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findHotPostsWithAuthor(GoodsStatus.APPROVED, pageable);

        return posts.map(PostResponse::fromWithAuthor);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> listUserLikes(Long userId, int page, int size) {
        log.info("查询用户点赞列表: userId={}, page={}, size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Long> postIds = postRepository.findLikedPostIdsByUserId(userId, pageable);

        if (postIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 查询帖子详情（保持点赞顺序）
        List<Post> posts = postRepository.findByIdInWithAuthor(postIds.getContent());
        Map<Long, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));

        // 按点赞顺序排列
        List<PostResponse> responses = postIds.getContent().stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
                .map(PostResponse::fromWithAuthor)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, postIds.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> listUserCollects(Long userId, int page, int size) {
        log.info("查询用户收藏列表: userId={}, page={}, size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Long> postIds = postRepository.findCollectedPostIdsByUserId(userId, pageable);

        if (postIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 查询帖子详情（保持收藏顺序）
        List<Post> posts = postRepository.findByIdInWithAuthor(postIds.getContent());
        Map<Long, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));

        // 按收藏顺序排列
        List<PostResponse> responses = postIds.getContent().stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
                .map(PostResponse::fromWithAuthor)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, postIds.getTotalElements());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleTopPost(Long id, boolean isTop) {
        log.info("置顶/取消置顶帖子: postId={}, isTop={}", id, isTop);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        post.setIsTop(isTop);
        postRepository.save(post);

        log.info("帖子置顶状态更新成功: postId={}, isTop={}", id, isTop);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchApprovePosts(List<Long> ids, boolean approved, String reason) {
        log.info("批量审核帖子: ids={}, approved={}, reason={}", ids, approved, reason);

        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (Long id : ids) {
            try {
                approvePost(id, approved, reason);
                successCount++;
            } catch (Exception e) {
                log.warn("批量审核失败: postId={}, error={}", id, e.getMessage());
            }
        }

        log.info("批量审核完成: total={}, success={}", ids.size(), successCount);
        return successCount;
    }

    @Override
    @Transactional(readOnly = true)
    public com.campus.marketplace.common.dto.response.PostStatsResponse getPostStats(Long id) {
        log.info("获取帖子统计信息: postId={}", id);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 查询点赞用户（最多10个）
        List<com.campus.marketplace.common.dto.response.PostStatsResponse.UserBriefInfo> likeUsers =
                postLikeRepository.findByPostId(id).stream()
                        .limit(10)
                        .map(like -> {
                            User user = like.getUser();
                            return com.campus.marketplace.common.dto.response.PostStatsResponse.UserBriefInfo.builder()
                                    .userId(user.getId())
                                    .username(user.getUsername())
                                    .avatar(user.getAvatar())
                                    .build();
                        })
                        .collect(Collectors.toList());

        // 查询收藏用户（最多10个）
        List<com.campus.marketplace.common.dto.response.PostStatsResponse.UserBriefInfo> collectUsers =
                postCollectRepository.findByPostId(id).stream()
                        .limit(10)
                        .map(collect -> {
                            User user = collect.getUser();
                            return com.campus.marketplace.common.dto.response.PostStatsResponse.UserBriefInfo.builder()
                                    .userId(user.getId())
                                    .username(user.getUsername())
                                    .avatar(user.getAvatar())
                                    .build();
                        })
                        .collect(Collectors.toList());

        return com.campus.marketplace.common.dto.response.PostStatsResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .viewCount(post.getViewCount())
                .replyCount(post.getReplyCount())
                .likeCount(post.getLikeCount())
                .collectCount(post.getCollectCount())
                .likeUsers(likeUsers)
                .collectUsers(collectUsers)
                .build();
    }
}
