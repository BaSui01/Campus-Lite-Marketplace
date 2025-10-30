package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.SendMessageRequest;
import com.campus.marketplace.common.dto.response.ConversationResponse;
import com.campus.marketplace.common.dto.response.MessageResponse;
import com.campus.marketplace.common.entity.Conversation;
import com.campus.marketplace.common.entity.Message;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.MessageStatus;
import com.campus.marketplace.common.enums.MessageType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.security.PermissionCodes;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.common.utils.SensitiveWordFilter;
import com.campus.marketplace.service.ComplianceService;
import com.campus.marketplace.repository.BlacklistRepository;
import com.campus.marketplace.repository.ConversationRepository;
import com.campus.marketplace.repository.MessageRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.MessageService;
import com.campus.marketplace.websocket.WebSocketSessionManager;
import com.campus.marketplace.common.dto.websocket.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 消息服务实现类
 *
 * 实现私信发送、查询、未读消息数管理等功能
 *
 * 技术亮点：
 * 1. 使用 WebSocket 实时推送消息
 * 2. 使用 Redis 缓存未读消息数
 * 3. 集成敏感词过滤
 * 4. 支持黑名单拦截
 * 5. 自动创建/获取会话
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final BlacklistRepository blacklistRepository;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final ComplianceService complianceService;
    private final WebSocketSessionManager sessionManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Redis Key 前缀：未读消息数
     */
    private static final String UNREAD_COUNT_KEY = "msg:unread:";

    /**
     * 未读消息数缓存过期时间（秒）- 1小时
     */
    private static final long UNREAD_COUNT_EXPIRE_SECONDS = 3600;

    /**
     * 发送消息
     *
     * 流程说明：
     * 1. 验证发送者和接收者
     * 2. 检查黑名单
     * 3. 敏感词过滤
     * 4. 创建或获取会话
     * 5. 保存消息到数据库
     * 6. 更新会话最后消息
     * 7. 更新未读消息数（Redis）
     * 8. 通过 WebSocket 实时推送消息
     *
     * @param request 发送消息请求
     * @return 消息ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(SendMessageRequest request) {
        // 1. 获取当前用户
        String username = SecurityUtil.getCurrentUsername();
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 验证接收者
        User receiver = userRepository.findById(request.receiverId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "接收者不存在"));

        // 2.1 校区隔离：无跨校权限禁止跨校通信
        try {
            if (!SecurityUtil.hasAuthority(PermissionCodes.SYSTEM_CAMPUS_CROSS)) {
                if (sender.getCampusId() != null && receiver.getCampusId() != null
                        && !sender.getCampusId().equals(receiver.getCampusId())) {
                    throw new BusinessException(ErrorCode.FORBIDDEN, "跨校区通信被禁止");
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception ignored) { }

        // 3. 不能给自己发消息
        if (sender.getId().equals(receiver.getId())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "不能给自己发消息");
        }

        // 4. 检查是否被拉黑
        boolean isBlocked = blacklistRepository.existsByUserIdAndBlockedUserId(receiver.getId(), sender.getId());
        if (isBlocked) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "对方已将你拉黑，无法发送消息");
        }

        // 5. 敏感词过滤
        String content = request.content();
        if (complianceService != null) {
            var mod = complianceService.moderateText(content, "MESSAGE_CONTENT");
            if (mod.hit() && mod.action() == com.campus.marketplace.common.enums.ComplianceAction.BLOCK) {
                throw new BusinessException(ErrorCode.INVALID_PARAM, "消息包含敏感词，已被拦截");
            }
            content = mod.filteredText();
        } else if (sensitiveWordFilter.contains(content)) {
            log.warn("检测到敏感词：senderId={}, content={}", sender.getId(), content);
            content = sensitiveWordFilter.filter(content);
        }

        // 6. 创建或获取会话
        Conversation conversation = getOrCreateConversation(sender.getId(), receiver.getId());

        // 7. 创建消息
        Message message = Message.builder()
                .conversationId(conversation.getId())
                .senderId(sender.getId())
                .receiverId(receiver.getId())
                .messageType(MessageType.TEXT)
                .content(content)
                .status(MessageStatus.UNREAD)
                .isRecalled(false)
                .build();

        // 8. 保存消息
        message = messageRepository.save(message);
        log.info("💬 消息已发送：messageId={}, senderId={}, receiverId={}, content={}",
                message.getId(), sender.getId(), receiver.getId(), content);

        // 9. 更新会话最后消息
        conversation.updateLastMessage(message.getId(), message.getCreatedAt());
        conversationRepository.save(conversation);

        // 10. 更新未读消息数（Redis）
        incrementUnreadCount(receiver.getId());

        // 11. 通过 WebSocket 实时推送消息
        pushMessageViaWebSocket(receiver.getId(), message, sender);

        return message.getId();
    }

    /**
     * 获取当前登录用户的未读消息数
     */
    @Override
    public int getUnreadCount() {
        String username = SecurityUtil.getCurrentUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return getUnreadCount(currentUser.getId());
    }

    /**
     * 获取指定用户的未读消息数
     *
     * 优先从 Redis 缓存读取，缓存未命中则从数据库查询并更新缓存
     *
     * @param userId 用户ID
     * @return 未读消息数
     */
    @Override
    public int getUnreadCount(Long userId) {
        String key = UNREAD_COUNT_KEY + userId;

        // 1. 尝试从 Redis 获取
        Object cached = null;
        try {
            var ops = redisTemplate.opsForValue();
            if (ops != null) {
                cached = ops.get(key);
            }
        } catch (Exception e) {
            log.warn("Redis 读取未读数失败，降级为数据库查询: {}", e.getMessage());
        }
        if (cached != null) {
            return ((Number) cached).intValue();
        }

        // 2. 从数据库查询
        long count = messageRepository.countByReceiverIdAndStatus(userId, MessageStatus.UNREAD);

        // 3. 更新缓存
        try {
            var ops = redisTemplate.opsForValue();
            if (ops != null) {
                ops.set(key, count, Duration.ofSeconds(UNREAD_COUNT_EXPIRE_SECONDS));
            }
        } catch (Exception e) {
            log.warn("Redis 回填未读数失败: {}", e.getMessage());
        }

        log.debug("📊 查询未读消息数：userId={}, count={}", userId, count);
        return (int) count;
    }

    /**
     * 查询会话列表
     *
     * 返回当前用户的所有会话，按最后消息时间倒序
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 会话列表
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponse> listConversations(int page, int size) {
        // 1. 获取当前用户
        String username = SecurityUtil.getCurrentUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 分页查询会话
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastMessageTime"));
        Page<Conversation> conversationPage = conversationRepository.findByUserIdPaginated(currentUser.getId(), pageable);

        // 3. 转换为 DTO
        return conversationPage.map(conversation -> {
            // 获取对方用户ID
            Long otherUserId = conversation.getOtherUserId(currentUser.getId());
            User otherUser = userRepository.findById(otherUserId)
                    .orElse(null);

            // 查询未读消息数
            int unreadCount = (int) messageRepository.countByConversationIdAndReceiverIdAndStatus(
                    conversation.getId(), currentUser.getId(), MessageStatus.UNREAD);

            return ConversationResponse.builder()
                    .conversationId(conversation.getId())
                    .otherUserId(otherUserId)
                    .otherUsername(otherUser != null ? otherUser.getUsername() : "未知用户")
                    .otherAvatar(otherUser != null ? otherUser.getAvatar() : null)
                    .lastMessageId(conversation.getLastMessageId())
                    .lastMessageTime(conversation.getLastMessageTime())
                    .unreadCount(unreadCount)
                    .build();
        });
    }

    /**
     * 查询聊天记录
     *
     * 返回指定会话的消息列表，按创建时间倒序
     *
     * @param conversationId 会话ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 消息列表
     */
    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> listMessages(Long conversationId, int page, int size) {
        // 1. 获取当前用户
        String username = SecurityUtil.getCurrentUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 验证会话权限
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "会话不存在"));

        if (!conversation.getUser1Id().equals(currentUser.getId()) &&
                !conversation.getUser2Id().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看该会话");
        }

        // 3. 分页查询消息
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> messagePage = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);

        // 4. 转换为 DTO
        return messagePage.map(message -> MessageResponse.builder()
                .messageId(message.getId())
                .conversationId(message.getConversationId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .messageType(message.getMessageType())
                .content(message.getContent())
                .status(message.getStatus())
                .isRecalled(message.getIsRecalled())
                .createdAt(message.getCreatedAt())
                .readAt(message.getReadAt())
                .build());
    }

    /**
     * 创建或获取会话
     *
     * 规则：会话的 user1Id < user2Id，保证唯一性
     *
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 会话
     */
    private Conversation getOrCreateConversation(Long userId1, Long userId2) {
        // 查询会话
        return conversationRepository.findByTwoUsers(userId1, userId2)
                .orElseGet(() -> {
                    // 会话不存在，创建新会话
                    // 保证 user1Id < user2Id
                    Long smallerId = Math.min(userId1, userId2);
                    Long largerId = Math.max(userId1, userId2);

                    Conversation newConversation = Conversation.builder()
                            .user1Id(smallerId)
                            .user2Id(largerId)
                            .lastMessageTime(LocalDateTime.now())
                            .build();

                    newConversation = conversationRepository.save(newConversation);
                    log.info("🆕 创建新会话：conversationId={}, user1Id={}, user2Id={}",
                            newConversation.getId(), smallerId, largerId);

                    return newConversation;
                });
    }

    /**
     * 增加用户的未读消息数（Redis）
     *
     * @param userId 用户ID
     */
    private void incrementUnreadCount(Long userId) {
        String key = UNREAD_COUNT_KEY + userId;
        try {
            var ops = redisTemplate.opsForValue();
            if (ops != null) {
                ops.increment(key);
                redisTemplate.expire(key, Duration.ofSeconds(UNREAD_COUNT_EXPIRE_SECONDS));
            }
        } catch (Exception e) {
            log.warn("Redis 未读数自增失败，忽略: {}", e.getMessage());
        }
        log.debug("📈 未读消息数 +1：userId={}", userId);
    }

    /**
     * 通过 WebSocket 实时推送消息
     *
     * @param receiverId 接收者ID
     * @param message 消息实体
     * @param sender 发送者
     */
    private void pushMessageViaWebSocket(Long receiverId, Message message, User sender) {
        try {
            // 1. 检查接收者是否在线
            WebSocketSession receiverSession = sessionManager.getSession(receiverId);
            if (receiverSession == null || !receiverSession.isOpen()) {
                log.debug("📴 接收者不在线，无法实时推送：receiverId={}", receiverId);
                return;
            }

            // 2. 构建 WebSocket 消息
            WebSocketMessage wsMessage = WebSocketMessage.builder()
                    .type("NEW_MESSAGE")
                    .content(message.getContent())
                    .fromUserId(sender.getId())
                    .toUserId(receiverId)
                    .messageId(message.getId())
                    .timestamp(System.currentTimeMillis())
                    .build();

            // 3. 发送消息
            String json = objectMapper.writeValueAsString(wsMessage);
            receiverSession.sendMessage(new TextMessage(json));

            log.info("📡 消息已实时推送：receiverId={}, messageId={}", receiverId, message.getId());
        } catch (Exception e) {
            log.error("❌ WebSocket 推送消息失败：receiverId={}, messageId={}", receiverId, message.getId(), e);
        }
    }

    /**
     * 标记会话消息为已读
     *
     * 功能说明：
     * 1. 验证会话权限
     * 2. 批量标记未读消息为已读
     * 3. 更新 Redis 未读消息数
     * 4. 通过 WebSocket 通知对方（可选）
     *
     * @param conversationId 会话ID
     * @return 已读消息数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markConversationAsRead(Long conversationId) {
        // 1. 获取当前用户
        String username = SecurityUtil.getCurrentUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 验证会话权限
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "会话不存在"));

        if (!conversation.getUser1Id().equals(currentUser.getId()) &&
                !conversation.getUser2Id().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该会话");
        }

        // 3. 批量标记消息为已读
        int count = messageRepository.markAsReadByConversation(
                conversationId, currentUser.getId(), MessageStatus.READ);

        // 4. 更新 Redis 未读消息数（减少count）
        if (count > 0) {
            String key = UNREAD_COUNT_KEY + currentUser.getId();
            try {
                var ops = redisTemplate.opsForValue();
                if (ops != null) {
                    ops.decrement(key, count);
                }
            } catch (Exception e) {
                log.warn("Redis 未读数递减失败，忽略: {}", e.getMessage());
            }
            log.info("📖 消息已标记为已读：conversationId={}, userId={}, count={}",
                    conversationId, currentUser.getId(), count);
        }

        return count;
    }

    /**
     * 撤回消息
     *
     * 功能说明：
     * 1. 验证消息所有权
     * 2. 检查撤回时限（2分钟内）
     * 3. 更新消息撤回状态
     * 4. 通过 WebSocket 通知对方
     *
     * @param messageId 消息ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recallMessage(Long messageId) {
        // 1. 获取当前用户
        String username = SecurityUtil.getCurrentUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 查询消息
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "消息不存在"));

        // 3. 验证消息所有权
        if (!message.getSenderId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能撤回自己发送的消息");
        }

        // 4. 检查是否已撤回
        if (message.getIsRecalled()) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "消息已撤回");
        }

        // 5. 检查撤回时限
        if (!message.canRecall()) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "超过撤回时限（1分钟内）");
        }

        // 6. 撤回消息
        message.recall();
        messageRepository.save(message);

        log.info("🔙 消息已撤回：messageId={}, senderId={}", messageId, currentUser.getId());

        // 7. 通过 WebSocket 通知接收者
        notifyMessageRecalled(message.getReceiverId(), messageId);
    }

    /**
     * 通过 WebSocket 通知消息撤回
     *
     * @param receiverId 接收者ID
     * @param messageId 消息ID
     */
    private void notifyMessageRecalled(Long receiverId, Long messageId) {
        try {
            WebSocketSession receiverSession = sessionManager.getSession(receiverId);
            if (receiverSession == null || !receiverSession.isOpen()) {
                log.debug("📴 接收者不在线，无法通知撤回：receiverId={}", receiverId);
                return;
            }

            WebSocketMessage wsMessage = WebSocketMessage.builder()
                    .type("MESSAGE_RECALLED")
                    .messageId(messageId)
                    .timestamp(System.currentTimeMillis())
                    .build();

            String json = objectMapper.writeValueAsString(wsMessage);
            receiverSession.sendMessage(new TextMessage(json));

            log.info("🔙 撤回通知已发送：receiverId={}, messageId={}", receiverId, messageId);
        } catch (Exception e) {
            log.error("❌ WebSocket 通知撤回失败：receiverId={}, messageId={}", receiverId, messageId, e);
        }
    }
}
