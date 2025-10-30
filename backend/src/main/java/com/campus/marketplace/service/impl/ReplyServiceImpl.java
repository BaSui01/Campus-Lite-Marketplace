package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.CreateReplyRequest;
import com.campus.marketplace.common.dto.response.ReplyResponse;
import com.campus.marketplace.common.entity.Post;
import com.campus.marketplace.common.entity.Reply;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.common.utils.SensitiveWordFilter;
import com.campus.marketplace.common.component.NotificationDispatcher;
import com.campus.marketplace.service.ComplianceService;
import com.campus.marketplace.repository.PostRepository;
import com.campus.marketplace.repository.ReplyRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.ReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Reply Service Impl
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService {

    private final ReplyRepository replyRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final ComplianceService complianceService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final NotificationDispatcher notificationDispatcher;

    private static final int REPLY_RATE_LIMIT = 5;
    private static final String REPLY_RATE_KEY_PREFIX = "reply:rate:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReply(CreateReplyRequest request) {
        log.info("用户回复帖子: postId={}, contentLength={}", request.postId(), request.content().length());

        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (!post.isApproved()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "帖子未审核通过，无法回复");
        }

        String rateLimitKey = REPLY_RATE_KEY_PREFIX + user.getId();
        Long replyCount = redisTemplate.opsForValue().increment(rateLimitKey, 0L);
        if (replyCount == null) {
            replyCount = 0L;
        }
        if (replyCount >= REPLY_RATE_LIMIT) {
            log.warn("用户回复频率过高: userId={}, replyCount={}", user.getId(), replyCount);
            throw new BusinessException(ErrorCode.INVALID_PARAM, "回复频率过高，请稍后再试");
        }

        String filteredContent;
        if (complianceService != null) {
            var mod = complianceService.moderateText(request.content(), "REPLY_CONTENT");
            if (mod.hit() && mod.action() == com.campus.marketplace.common.enums.ComplianceAction.BLOCK) {
                throw new BusinessException(ErrorCode.INVALID_PARAM, "包含敏感词，请修改后再发布");
            }
            filteredContent = mod.filteredText();
        } else {
            filteredContent = sensitiveWordFilter.filter(request.content());
        }

        Reply reply = Reply.builder()
                .postId(request.postId())
                .content(filteredContent)
                .authorId(user.getId())
                .parentId(request.parentId())
                .toUserId(request.toUserId())
                .likeCount(0)
                .build();

        if (request.parentId() != null) {
            Reply parentReply = replyRepository.findById(request.parentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PARAM, "父回复不存在"));
            if (!parentReply.getPostId().equals(request.postId())) {
                throw new BusinessException(ErrorCode.INVALID_PARAM, "父回复与帖子不匹配");
            }
        }

        replyRepository.save(reply);

        post.incrementReplyCount();
        postRepository.save(post);

        redisTemplate.opsForValue().increment(rateLimitKey, 1L);
        redisTemplate.expire(rateLimitKey, 1, TimeUnit.MINUTES);

        log.info("回复创建成功: replyId={}, postId={}, authorId={}", reply.getId(), request.postId(), user.getId());

        // 通知帖子作者
        try {
            if (!user.getId().equals(post.getAuthorId())) {
                notificationDispatcher.enqueueTemplate(
                        post.getAuthorId(),
                        "POST_REPLIED",
                        java.util.Map.of(
                                "postTitle", post.getTitle(),
                                "replyPreview", filteredContent.length() > 20 ? filteredContent.substring(0, 20) : filteredContent
                        ),
                        com.campus.marketplace.common.enums.NotificationType.POST_REPLIED.name(),
                        post.getId(),
                        "POST",
                        "/posts/" + post.getId()
                );
            }
        } catch (Exception ignored) {}

        // 解析 @提及 并通知
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("@([a-zA-Z0-9_\\u4e00-\\u9fa5]{2,20})").matcher(filteredContent);
            java.util.Set<String> names = new java.util.HashSet<>();
            while (m.find()) names.add(m.group(1));
            for (String name : names) {
                userRepository.findByUsername(name).ifPresent(mentioned -> {
                    if (!mentioned.getId().equals(user.getId())) {
                        notificationDispatcher.enqueueTemplate(
                                mentioned.getId(),
                                "POST_MENTIONED",
                                java.util.Map.of("postTitle", post.getTitle()),
                                com.campus.marketplace.common.enums.NotificationType.POST_MENTIONED.name(),
                                post.getId(),
                                "POST",
                                "/posts/" + post.getId()
                        );
                    }
                });
            }
        } catch (Exception ignored) {}

        return reply.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReplyResponse> listReplies(Long postId, int page, int size) {
        log.info("查询帖子回复列表: postId={}, page={}, size={}", postId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Reply> replyPage = replyRepository.findByPostId(postId, pageable);
        return replyPage.map(ReplyResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReplyResponse> listSubReplies(Long parentId) {
        log.info("查询子回复列表: parentId={}", parentId);
        List<Reply> replies = replyRepository.findByParentId(parentId);
        return replies.stream().map(ReplyResponse::from).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReply(Long id) {
        log.info("删除回复: replyId={}", id);
        Reply reply = replyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PARAM, "回复不存在"));

        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!reply.getAuthorId().equals(user.getId()) && !user.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        replyRepository.delete(reply);

        Post post = postRepository.findById(reply.getPostId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if (post.getReplyCount() > 0) {
            post.setReplyCount(post.getReplyCount() - 1);
            postRepository.save(post);
        }

        log.info("回复删除成功: replyId={}", id);
    }
}
