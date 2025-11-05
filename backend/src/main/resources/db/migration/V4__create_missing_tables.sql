-- =====================================================
-- Flyway 数据库迁移脚本 V4 - 补充缺失表
-- =====================================================
-- 作者: BaSui 😎
-- 日期: 2025-11-05
-- 描述: 创建所有缺失的业务表（聊天、论坛、物流、纠纷等46个表）
-- 数据库: PostgreSQL 14+
-- =====================================================

-- =====================================================
-- 聊天模块 (2个表)
-- =====================================================

-- 1. 会话表
CREATE TABLE t_conversation (
    id BIGSERIAL PRIMARY KEY,
    user1_id BIGINT NOT NULL,
    user2_id BIGINT NOT NULL,
    last_message_id BIGINT,
    last_message_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_conversation_user1 FOREIGN KEY (user1_id) REFERENCES t_user(id),
    CONSTRAINT fk_conversation_user2 FOREIGN KEY (user2_id) REFERENCES t_user(id),
    CONSTRAINT uk_conversation_users UNIQUE (user1_id, user2_id)
);

COMMENT ON TABLE t_conversation IS '会话表（用户之间的聊天会话）';
COMMENT ON COLUMN t_conversation.user1_id IS '用户1 ID（较小的用户ID）';
COMMENT ON COLUMN t_conversation.user2_id IS '用户2 ID（较大的用户ID）';
COMMENT ON COLUMN t_conversation.last_message_time IS '最后一条消息时间';

-- 2. 消息表（不继承BaseEntity，只有id和created_at）
CREATE TABLE t_message (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UNREAD',
    is_recalled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id) REFERENCES t_conversation(id),
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES t_user(id),
    CONSTRAINT fk_message_receiver FOREIGN KEY (receiver_id) REFERENCES t_user(id)
);

COMMENT ON TABLE t_message IS '消息表（用户之间的聊天消息，不继承BaseEntity）';
COMMENT ON COLUMN t_message.message_type IS '消息类型：TEXT=文本, IMAGE=图片, VOICE=语音';
COMMENT ON COLUMN t_message.status IS '消息状态：UNREAD=未读, READ=已读';

-- =====================================================
-- 论坛模块 (7个表)
-- =====================================================

-- 3. 帖子表
CREATE TABLE t_post (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    campus_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    view_count INTEGER NOT NULL DEFAULT 0,
    reply_count INTEGER NOT NULL DEFAULT 0,
    images TEXT[],
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES t_user(id),
    CONSTRAINT fk_post_campus FOREIGN KEY (campus_id) REFERENCES t_campus(id)
);

COMMENT ON TABLE t_post IS '帖子表（论坛帖子）';
COMMENT ON COLUMN t_post.status IS '帖子状态：PENDING=待审核, APPROVED=已通过, REJECTED=已拒绝';

-- 4. 帖子点赞表
CREATE TABLE t_post_like (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_post_like_post FOREIGN KEY (post_id) REFERENCES t_post(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_like_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_post_like UNIQUE (post_id, user_id)
);

COMMENT ON TABLE t_post_like IS '帖子点赞表';

-- 5. 帖子收藏表
CREATE TABLE t_post_collect (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_post_collect_post FOREIGN KEY (post_id) REFERENCES t_post(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_collect_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_post_collect UNIQUE (post_id, user_id)
);

COMMENT ON TABLE t_post_collect IS '帖子收藏表';

-- 6. 帖子回复表
CREATE TABLE t_reply (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    parent_id BIGINT,
    to_user_id BIGINT,
    like_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_reply_post FOREIGN KEY (post_id) REFERENCES t_post(id) ON DELETE CASCADE,
    CONSTRAINT fk_reply_author FOREIGN KEY (author_id) REFERENCES t_user(id),
    CONSTRAINT fk_reply_parent FOREIGN KEY (parent_id) REFERENCES t_reply(id),
    CONSTRAINT fk_reply_to_user FOREIGN KEY (to_user_id) REFERENCES t_user(id)
);

COMMENT ON TABLE t_reply IS '帖子回复表（支持楼中楼）';
COMMENT ON COLUMN t_reply.parent_id IS '父回复ID（NULL表示直接回复帖子）';
COMMENT ON COLUMN t_reply.to_user_id IS '回复目标用户ID（楼中楼时有值）';

-- 7. 话题表
CREATE TABLE t_topic (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    hotness INTEGER NOT NULL DEFAULT 0,
    post_count INTEGER NOT NULL DEFAULT 0,
    follower_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

COMMENT ON TABLE t_topic IS '话题表（如#数码评测、#好物分享）';
COMMENT ON COLUMN t_topic.hotness IS '话题热度（根据参与人数和讨论量计算）';

-- 8. 话题关注表
CREATE TABLE t_topic_follow (
    id BIGSERIAL PRIMARY KEY,
    topic_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_topic_follow_topic FOREIGN KEY (topic_id) REFERENCES t_topic(id) ON DELETE CASCADE,
    CONSTRAINT fk_topic_follow_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_topic_follow UNIQUE (topic_id, user_id)
);

COMMENT ON TABLE t_topic_follow IS '话题关注表';

-- 9. 话题标签关联表
CREATE TABLE t_topic_tag (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_topic_tag_post FOREIGN KEY (post_id) REFERENCES t_post(id) ON DELETE CASCADE,
    CONSTRAINT fk_topic_tag_topic FOREIGN KEY (topic_id) REFERENCES t_topic(id) ON DELETE CASCADE,
    CONSTRAINT uk_topic_tag UNIQUE (post_id, topic_id)
);

COMMENT ON TABLE t_topic_tag IS '话题标签关联表（帖子与话题的多对多关系）';

-- =====================================================
-- 订单相关 (8个表)
-- =====================================================

-- 10. 物流表
CREATE TABLE t_logistics (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    tracking_number VARCHAR(50) NOT NULL,
    logistics_company VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    current_location VARCHAR(200),
    estimated_delivery_time TIMESTAMP,
    actual_delivery_time TIMESTAMP,
    is_overtime BOOLEAN NOT NULL DEFAULT FALSE,
    track_records JSONB,
    sync_count INTEGER NOT NULL DEFAULT 0,
    last_sync_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_logistics_order FOREIGN KEY (order_id) REFERENCES t_order(id)
);

COMMENT ON TABLE t_logistics IS '物流表（支持多家快递公司）';
COMMENT ON COLUMN t_logistics.logistics_company IS '快递公司：SF=顺丰, ZTO=中通, YTO=圆通, STO=申通, EMS=EMS';
COMMENT ON COLUMN t_logistics.status IS '物流状态：PENDING=待发货, PICKED_UP=已揽件, IN_TRANSIT=运输中, DELIVERING=派送中, DELIVERED=已签收';
COMMENT ON COLUMN t_logistics.track_records IS '物流轨迹（JSONB格式）';

-- 11. 支付日志表
CREATE TABLE t_payment_log (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL,
    trade_no VARCHAR(64),
    channel VARCHAR(20),
    type VARCHAR(10),
    payload JSONB,
    success BOOLEAN,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

COMMENT ON TABLE t_payment_log IS '支付日志表';
COMMENT ON COLUMN t_payment_log.channel IS '支付渠道：ALIPAY=支付宝, WECHAT=微信支付';
COMMENT ON COLUMN t_payment_log.type IS '操作类型：PAY=支付, REFUND=退款';

-- 12. 退款申请表
CREATE TABLE t_refund_request (
    id BIGSERIAL PRIMARY KEY,
    refund_no VARCHAR(50) NOT NULL UNIQUE,
    order_no VARCHAR(50) NOT NULL,
    applicant_id BIGINT NOT NULL,
    reason VARCHAR(255),
    evidence JSONB,
    status VARCHAR(20) NOT NULL,
    channel VARCHAR(20),
    amount DECIMAL(10, 2) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_error VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_refund_applicant FOREIGN KEY (applicant_id) REFERENCES t_user(id)
);

COMMENT ON TABLE t_refund_request IS '退款申请表';
COMMENT ON COLUMN t_refund_request.status IS '退款状态：PENDING=待审核, APPROVED=已同意, REJECTED=已拒绝, PROCESSING=处理中, COMPLETED=已完成';

-- 13. 撤销申请表
CREATE TABLE t_revert_request (
    id BIGSERIAL PRIMARY KEY,
    refund_no VARCHAR(50) NOT NULL UNIQUE,
    applicant_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    handler_id BIGINT,
    handle_result VARCHAR(500),
    handled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_revert_applicant FOREIGN KEY (applicant_id) REFERENCES t_user(id),
    CONSTRAINT fk_revert_handler FOREIGN KEY (handler_id) REFERENCES t_user(id)
);

COMMENT ON TABLE t_revert_request IS '撤销申请表（撤销退款申请）';
COMMENT ON COLUMN t_revert_request.status IS '审核状态：PENDING=待审核, APPROVED=已同意, REJECTED=已拒绝';

-- 14. 纠纷表
CREATE TABLE t_dispute (
    id BIGSERIAL PRIMARY KEY,
    dispute_code VARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    initiator_id BIGINT NOT NULL,
    initiator_role VARCHAR(20) NOT NULL,
    respondent_id BIGINT NOT NULL,
    dispute_type VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    negotiation_deadline TIMESTAMP,
    arbitration_deadline TIMESTAMP,
    arbitrator_id BIGINT,
    arbitration_result VARCHAR(30),
    completed_at TIMESTAMP,
    closed_at TIMESTAMP,
    close_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_dispute_order FOREIGN KEY (order_id) REFERENCES t_order(id),
    CONSTRAINT fk_dispute_initiator FOREIGN KEY (initiator_id) REFERENCES t_user(id),
    CONSTRAINT fk_dispute_respondent FOREIGN KEY (respondent_id) REFERENCES t_user(id),
    CONSTRAINT fk_dispute_arbitrator FOREIGN KEY (arbitrator_id) REFERENCES t_user(id)
);

COMMENT ON TABLE t_dispute IS '纠纷表（买卖双方纠纷）';
COMMENT ON COLUMN t_dispute.dispute_type IS '纠纷类型：QUALITY=质量问题, DESCRIPTION_MISMATCH=描述不符, LOGISTICS=物流问题, SERVICE=服务问题, OTHER=其他';
COMMENT ON COLUMN t_dispute.status IS '纠纷状态：SUBMITTED=已提交, NEGOTIATING=协商中, ARBITRATING=仲裁中, COMPLETED=已完成, CLOSED=已关闭';
COMMENT ON COLUMN t_dispute.arbitration_result IS '仲裁结果：FULL_REFUND=全额退款, PARTIAL_REFUND=部分退款, REJECT=驳回申请, NEED_MORE_EVIDENCE=需补充证据';

-- 15. 纠纷协商表
CREATE TABLE t_dispute_negotiation (
    id BIGSERIAL PRIMARY KEY,
    dispute_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    sender_role VARCHAR(20) NOT NULL,
    message_type VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    proposed_refund_amount DECIMAL(10, 2),
    proposal_status VARCHAR(20),
    responded_at TIMESTAMP,
    responded_by BIGINT,
    response_note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_negotiation_dispute FOREIGN KEY (dispute_id) REFERENCES t_dispute(id) ON DELETE CASCADE,
    CONSTRAINT fk_negotiation_sender FOREIGN KEY (sender_id) REFERENCES t_user(id)
);

COMMENT ON TABLE t_dispute_negotiation IS '纠纷协商表（买卖双方的协商消息）';
COMMENT ON COLUMN t_dispute_negotiation.message_type IS '消息类型：TEXT=文字消息, PROPOSAL=解决方案';
COMMENT ON COLUMN t_dispute_negotiation.proposal_status IS '方案状态：PENDING=待响应, ACCEPTED=已接受, REJECTED=已拒绝';

-- 16. 纠纷仲裁表
CREATE TABLE t_dispute_arbitration (
    id BIGSERIAL PRIMARY KEY,
    dispute_id BIGINT NOT NULL UNIQUE,
    arbitrator_id BIGINT NOT NULL,
    result VARCHAR(30) NOT NULL,
    refund_amount DECIMAL(10, 2),
    reason TEXT NOT NULL,
    buyer_evidence_analysis TEXT,
    seller_evidence_analysis TEXT,
    arbitrated_at TIMESTAMP NOT NULL,
    executed BOOLEAN NOT NULL DEFAULT FALSE,
    executed_at TIMESTAMP,
    execution_note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_arbitration_dispute FOREIGN KEY (dispute_id) REFERENCES t_dispute(id) ON DELETE CASCADE,
    CONSTRAINT fk_arbitration_arbitrator FOREIGN KEY (arbitrator_id) REFERENCES t_user(id)
);

COMMENT ON TABLE t_dispute_arbitration IS '纠纷仲裁表（仲裁员的仲裁决定）';
COMMENT ON COLUMN t_dispute_arbitration.result IS '仲裁结果：FULL_REFUND=全额退款, PARTIAL_REFUND=部分退款, REJECT=驳回申请, NEED_MORE_EVIDENCE=需补充证据';

-- 17. 纠纷证据表
CREATE TABLE t_dispute_evidence (
    id BIGSERIAL PRIMARY KEY,
    dispute_id BIGINT NOT NULL,
    uploader_id BIGINT NOT NULL,
    uploader_role VARCHAR(20) NOT NULL,
    evidence_type VARCHAR(30) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_size BIGINT NOT NULL,
    description TEXT,
    validity VARCHAR(20),
    validity_reason TEXT,
    evaluated_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_evidence_dispute FOREIGN KEY (dispute_id) REFERENCES t_dispute(id) ON DELETE CASCADE,
    CONSTRAINT fk_evidence_uploader FOREIGN KEY (uploader_id) REFERENCES t_user(id)
);

COMMENT ON TABLE t_dispute_evidence IS '纠纷证据表（图片、视频、聊天记录等）';
COMMENT ON COLUMN t_dispute_evidence.evidence_type IS '证据类型：IMAGE=图片, VIDEO=视频, CHAT_RECORD=聊天记录, LOGISTICS_PROOF=物流凭证, TRANSACTION_RECORD=交易记录';
COMMENT ON COLUMN t_dispute_evidence.validity IS '证据有效性：VALID=有效, INVALID=无效, DOUBTFUL=存疑';

-- =====================================================
-- 评价扩展 (2个表)
-- =====================================================

-- 18. 评价标签表
CREATE TABLE t_review_tag (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL,
    tag_name VARCHAR(50) NOT NULL,
    tag_type VARCHAR(20) NOT NULL DEFAULT 'OTHER',
    tag_source VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',
    weight DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_review_tag_review FOREIGN KEY (review_id) REFERENCES t_review(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_review_tag IS '评价标签表（NLP自动提取或用户输入）';
COMMENT ON COLUMN t_review_tag.tag_type IS '标签类型：QUALITY=物品质量, SERVICE=服务态度, LOGISTICS=物流速度, PRICE=性价比, OTHER=其他';
COMMENT ON COLUMN t_review_tag.tag_source IS '标签来源：SYSTEM=NLP自动提取, USER_INPUT=用户手动输入';
COMMENT ON COLUMN t_review_tag.weight IS '标签权重（0.0~1.0，越高越重要）';

-- 19. 评价情感分析表
CREATE TABLE t_review_sentiment (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL UNIQUE,
    sentiment_type VARCHAR(20) NOT NULL DEFAULT 'NEUTRAL',
    sentiment_score DOUBLE PRECISION NOT NULL DEFAULT 0.5,
    positive_word_count INTEGER NOT NULL DEFAULT 0,
    negative_word_count INTEGER NOT NULL DEFAULT 0,
    neutral_word_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_sentiment_review FOREIGN KEY (review_id) REFERENCES t_review(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_review_sentiment IS '评价情感分析表（NLP情感分析结果）';
COMMENT ON COLUMN t_review_sentiment.sentiment_type IS '情感类型：POSITIVE=积极, NEUTRAL=中性, NEGATIVE=消极';
COMMENT ON COLUMN t_review_sentiment.sentiment_score IS '情感得分（0.0~1.0，越高越积极）';

-- =====================================================
-- 用户关系 (1个表)
-- =====================================================

-- 20. 用户关注表
CREATE TABLE t_user_follow (
    id BIGSERIAL PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_user_follow_follower FOREIGN KEY (follower_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_follow_following FOREIGN KEY (following_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_follow UNIQUE (follower_id, following_id)
);

COMMENT ON TABLE t_user_follow IS '用户关注表（用户对用户的关注关系）';
COMMENT ON COLUMN t_user_follow.follower_id IS '关注者ID（粉丝）';
COMMENT ON COLUMN t_user_follow.following_id IS '被关注者ID（关注的人）';

-- =====================================================
-- 通知扩展 (3个表)
-- =====================================================

-- 21. 通知模板表（继承BaseEntity）
CREATE TABLE t_notification_template (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    title_key VARCHAR(200) NOT NULL,
    content_key VARCHAR(200) NOT NULL,
    channels VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

COMMENT ON TABLE t_notification_template IS '通知模板表（支持软删除，便于版本管理）';
COMMENT ON COLUMN t_notification_template.code IS '模板编码（唯一标识），例如：ORDER_PAID, REVIEW_RECEIVED';
COMMENT ON COLUMN t_notification_template.channels IS '可用渠道（逗号分隔）：IN_APP=应用内, EMAIL=邮件, WEB_PUSH=网页推送';

-- 22. 通知偏好设置表（不继承BaseEntity，只有id）
CREATE TABLE t_notification_preference (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    quiet_start TIME,
    quiet_end TIME,
    CONSTRAINT uk_preference_user_channel UNIQUE (user_id, channel),
    CONSTRAINT fk_preference_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_notification_preference IS '通知偏好设置表（用户自定义通知渠道和免打扰时间，不继承BaseEntity）';
COMMENT ON COLUMN t_notification_preference.channel IS '通知渠道：IN_APP=应用内, EMAIL=邮件, WEB_PUSH=网页推送';

-- 23. 通知退订表（不继承BaseEntity，只有id）
CREATE TABLE t_notification_unsubscribe (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    template_code VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    CONSTRAINT uk_unsubscribe_user_template_channel UNIQUE (user_id, template_code, channel),
    CONSTRAINT fk_unsubscribe_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_notification_unsubscribe IS '通知退订表（不继承BaseEntity）';
COMMENT ON COLUMN t_notification_unsubscribe.template_code IS '模板编码';
COMMENT ON COLUMN t_notification_unsubscribe.channel IS '渠道：IN_APP=应用内, EMAIL=邮件, WEB_PUSH=网页推送';

-- =====================================================
-- 搜索扩展 (2个表)
-- =====================================================

-- 24. 搜索日志表
CREATE TABLE t_search_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    keyword VARCHAR(100) NOT NULL,
    result_count INTEGER NOT NULL DEFAULT 0,
    clicked_goods_id BIGINT,
    search_time BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_search_log_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_search_log IS '搜索日志表（详细搜索行为分析）';
COMMENT ON COLUMN t_search_log.search_time IS '搜索耗时（毫秒）';

-- 25. 热门搜索关键词表
CREATE TABLE t_search_keyword (
    id BIGSERIAL PRIMARY KEY,
    keyword VARCHAR(100) NOT NULL UNIQUE,
    search_count INTEGER NOT NULL DEFAULT 0,
    hotness INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

COMMENT ON TABLE t_search_keyword IS '热门搜索关键词表';
COMMENT ON COLUMN t_search_keyword.hotness IS '热度值（根据搜索量和时间衰减计算）';

-- =====================================================
-- 推荐系统 (4个表)
-- =====================================================

-- 26. 用户行为日志表
CREATE TABLE t_user_behavior_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    behavior_type VARCHAR(20) NOT NULL,
    goods_id BIGINT,
    post_id BIGINT,
    duration INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_behavior_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_behavior_goods FOREIGN KEY (goods_id) REFERENCES t_goods(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_user_behavior_log IS '用户行为日志表（浏览、点击、收藏、购买等行为）';
COMMENT ON COLUMN t_user_behavior_log.behavior_type IS '行为类型：VIEW=浏览, CLICK=点击, FAVORITE=收藏, PURCHASE=购买, SEARCH=搜索';
COMMENT ON COLUMN t_user_behavior_log.duration IS '行为时长（秒）';

-- 27. 用户Feed流表
CREATE TABLE t_user_feed (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_type VARCHAR(20) NOT NULL,
    item_id BIGINT NOT NULL,
    score DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    reason VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_feed_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_user_feed IS '用户Feed流表（个性化推荐内容）';
COMMENT ON COLUMN t_user_feed.item_type IS '内容类型：GOODS=商品, POST=帖子';
COMMENT ON COLUMN t_user_feed.score IS '推荐分数';
COMMENT ON COLUMN t_user_feed.reason IS '推荐原因';

-- 28. 用户画像表
CREATE TABLE t_user_persona (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    age_group VARCHAR(20),
    gender VARCHAR(10),
    campus_id BIGINT,
    interests JSONB,
    favorite_categories JSONB,
    price_range_min DECIMAL(10, 2),
    price_range_max DECIMAL(10, 2),
    active_time_slots JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_persona_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_user_persona IS '用户画像表（用户特征和偏好）';
COMMENT ON COLUMN t_user_persona.interests IS '兴趣标签（JSONB格式）';
COMMENT ON COLUMN t_user_persona.active_time_slots IS '活跃时间段（JSONB格式）';

-- 29. 用户相似度表
CREATE TABLE t_user_similarity (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    similar_user_id BIGINT NOT NULL,
    similarity_score DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_similarity_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_similarity_similar_user FOREIGN KEY (similar_user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_similarity UNIQUE (user_id, similar_user_id)
);

COMMENT ON TABLE t_user_similarity IS '用户相似度表（协同过滤推荐）';
COMMENT ON COLUMN t_user_similarity.similarity_score IS '相似度分数（0.0~1.0）';

-- =====================================================
-- 系统管理 (11个表)
-- =====================================================

-- 30. 封禁日志表
CREATE TABLE t_ban_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reason VARCHAR(500) NOT NULL,
    ban_type VARCHAR(20) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    operator_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_ban_user FOREIGN KEY (user_id) REFERENCES t_user(id),
    CONSTRAINT fk_ban_operator FOREIGN KEY (operator_id) REFERENCES t_user(id)
);

COMMENT ON TABLE t_ban_log IS '封禁日志表';
COMMENT ON COLUMN t_ban_log.ban_type IS '封禁类型：TEMPORARY=临时封禁, PERMANENT=永久封禁';

-- 31. 黑名单表（不继承BaseEntity，只有id和created_at）
CREATE TABLE t_blacklist (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    blocked_user_id BIGINT NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_blacklist_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_blacklist_blocked_user FOREIGN KEY (blocked_user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_blacklist_users UNIQUE (user_id, blocked_user_id)
);

COMMENT ON TABLE t_blacklist IS '黑名单表（用户拉黑其他用户，不继承BaseEntity）';
COMMENT ON COLUMN t_blacklist.user_id IS '拉黑的用户ID';
COMMENT ON COLUMN t_blacklist.blocked_user_id IS '被拉黑的用户ID';

-- 32. 合规审计日志表
CREATE TABLE t_compliance_audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id BIGINT,
    ip_address VARCHAR(50),
    location VARCHAR(100),
    risk_level VARCHAR(20) NOT NULL DEFAULT 'LOW',
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_compliance_user FOREIGN KEY (user_id) REFERENCES t_user(id)
);

COMMENT ON TABLE t_compliance_audit_log IS '合规审计日志表（敏感操作审计）';
COMMENT ON COLUMN t_compliance_audit_log.risk_level IS '风险等级：LOW=低, MEDIUM=中, HIGH=高, CRITICAL=严重';

-- 33. 合规白名单表（不继承BaseEntity，只有id）
CREATE TABLE t_compliance_whitelist (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    CONSTRAINT uk_whitelist_type_target UNIQUE (type, target_id)
);

COMMENT ON TABLE t_compliance_whitelist IS '合规白名单表（不继承BaseEntity）';
COMMENT ON COLUMN t_compliance_whitelist.type IS '白名单类型：USER=用户, POST=帖子, GOODS=商品';
COMMENT ON COLUMN t_compliance_whitelist.target_id IS '白名单目标ID';

-- 34. 申诉表
CREATE TABLE t_appeal (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    appeal_type VARCHAR(50) NOT NULL,
    reason TEXT NOT NULL,
    deadline TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewer_id BIGINT,
    reviewer_name VARCHAR(50),
    review_comment TEXT,
    reviewed_at TIMESTAMP,
    attachments TEXT,
    result_details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_appeal_user FOREIGN KEY (user_id) REFERENCES t_user(id),
    CONSTRAINT fk_appeal_reviewer FOREIGN KEY (reviewer_id) REFERENCES t_user(id)
);

COMMENT ON TABLE t_appeal IS '申诉表（封禁、删帖等申诉）';
COMMENT ON COLUMN t_appeal.target_type IS '申诉对象类型：BAN=封禁, POST_DELETE=帖子删除, GOODS_REJECT=商品拒绝';
COMMENT ON COLUMN t_appeal.appeal_type IS '申诉类型';
COMMENT ON COLUMN t_appeal.deadline IS '截止时间';
COMMENT ON COLUMN t_appeal.status IS '处理状态：PENDING=待处理, APPROVED=已通过, REJECTED=已拒绝';
COMMENT ON COLUMN t_appeal.reviewer_id IS '审核人ID';
COMMENT ON COLUMN t_appeal.reviewer_name IS '审核人用户名';
COMMENT ON COLUMN t_appeal.review_comment IS '审核意见';
COMMENT ON COLUMN t_appeal.reviewed_at IS '审核时间';
COMMENT ON COLUMN t_appeal.attachments IS '附件列表（JSON格式）';
COMMENT ON COLUMN t_appeal.result_details IS '处理结果详情';

-- 35. 申诉材料表
CREATE TABLE t_appeal_material (
    id BIGSERIAL PRIMARY KEY,
    appeal_id VARCHAR(255) NOT NULL,
    file_type VARCHAR(20) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100),
    thumbnail_path VARCHAR(500),
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'UPLOADED',
    uploaded_by BIGINT,
    uploaded_by_name VARCHAR(50),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    file_hash VARCHAR(64),
    virus_scan_result VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

COMMENT ON TABLE t_appeal_material IS '申诉材料表（申诉证据）';
COMMENT ON COLUMN t_appeal_material.file_type IS '文件类型 (image/pdf/document等)';
COMMENT ON COLUMN t_appeal_material.file_path IS '文件路径';
COMMENT ON COLUMN t_appeal_material.mime_type IS 'MIME类型';
COMMENT ON COLUMN t_appeal_material.thumbnail_path IS '缩略图路径';
COMMENT ON COLUMN t_appeal_material.status IS '文件状态';
COMMENT ON COLUMN t_appeal_material.uploaded_by IS '上传用户ID';
COMMENT ON COLUMN t_appeal_material.uploaded_by_name IS '上传用户名';
COMMENT ON COLUMN t_appeal_material.uploaded_at IS '上传时间';
COMMENT ON COLUMN t_appeal_material.is_primary IS '是否为主文件';
COMMENT ON COLUMN t_appeal_material.file_hash IS '文件哈希值（用于去重）';
COMMENT ON COLUMN t_appeal_material.virus_scan_result IS '病毒扫描结果';

-- 36. 错误日志表
CREATE TABLE t_error_log (
    id BIGSERIAL PRIMARY KEY,
    error_type VARCHAR(50) NOT NULL,
    error_message TEXT NOT NULL,
    stack_trace TEXT,
    request_url VARCHAR(500),
    request_method VARCHAR(10),
    user_id BIGINT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    severity VARCHAR(20) NOT NULL DEFAULT 'ERROR',
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    resolved_at TIMESTAMP,
    resolved_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

COMMENT ON TABLE t_error_log IS '错误日志表（系统错误记录）';
COMMENT ON COLUMN t_error_log.severity IS '严重程度：DEBUG=调试, INFO=信息, WARN=警告, ERROR=错误, FATAL=致命';

-- 37. 健康检查记录表
CREATE TABLE t_health_check_record (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(50) NOT NULL,
    check_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    response_time BIGINT NOT NULL,
    error_message TEXT,
    details JSONB,
    checked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

COMMENT ON TABLE t_health_check_record IS '健康检查记录表（服务健康状态监控）';
COMMENT ON COLUMN t_health_check_record.check_type IS '检查类型：DATABASE=数据库, REDIS=缓存, API=接口, EXTERNAL_SERVICE=外部服务';
COMMENT ON COLUMN t_health_check_record.status IS '健康状态：HEALTHY=健康, DEGRADED=降级, UNHEALTHY=不健康';

-- 38. 功能开关表（继承BaseEntity）
CREATE TABLE t_feature_flag (
    id BIGSERIAL PRIMARY KEY,
    feature_key VARCHAR(128) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    rules_json TEXT,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

COMMENT ON TABLE t_feature_flag IS '功能开关表（支持软删除，便于灰度发布和审计）';
COMMENT ON COLUMN t_feature_flag.feature_key IS '功能开关Key（唯一标识）';
COMMENT ON COLUMN t_feature_flag.rules_json IS '规则配置（JSON格式），可配置：目标用户、灰度比例、生效时间等';

-- 39. 数据备份表
CREATE TABLE t_data_backup (
    id BIGSERIAL PRIMARY KEY,
    backup_type VARCHAR(20) NOT NULL,
    table_name VARCHAR(100) NOT NULL,
    backup_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    record_count BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

COMMENT ON TABLE t_data_backup IS '数据备份表';
COMMENT ON COLUMN t_data_backup.backup_type IS '备份类型：FULL=全量, INCREMENTAL=增量';
COMMENT ON COLUMN t_data_backup.status IS '备份状态：IN_PROGRESS=进行中, COMPLETED=已完成, FAILED=失败';

-- 40. 隐私请求表
CREATE TABLE t_privacy_request (
    id BIGSERIAL PRIMARY KEY,
    request_no VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    request_type VARCHAR(30) NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    handler_id BIGINT,
    handle_result VARCHAR(500),
    handled_at TIMESTAMP,
    executed BOOLEAN NOT NULL DEFAULT FALSE,
    executed_at TIMESTAMP,
    scheduled_at TIMESTAMP,
    completed_at TIMESTAMP,
    result_path VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_privacy_user FOREIGN KEY (user_id) REFERENCES t_user(id),
    CONSTRAINT fk_privacy_handler FOREIGN KEY (handler_id) REFERENCES t_user(id)
);

COMMENT ON TABLE t_privacy_request IS '隐私请求表（数据导出、删除等）';
COMMENT ON COLUMN t_privacy_request.request_type IS '请求类型：DATA_EXPORT=数据导出, DATA_DELETE=数据删除, ACCOUNT_DELETION=账号注销';
COMMENT ON COLUMN t_privacy_request.status IS '处理状态：PENDING=待处理, APPROVED=已通过, REJECTED=已拒绝, EXECUTED=已执行';
COMMENT ON COLUMN t_privacy_request.scheduled_at IS '预定处理时间';
COMMENT ON COLUMN t_privacy_request.completed_at IS '完成时间';
COMMENT ON COLUMN t_privacy_request.result_path IS '结果文件路径（数据导出）';

-- =====================================================
-- 任务管理 (4个表)
-- =====================================================

-- 41. 批量任务表
CREATE TABLE t_batch_task (
    id BIGSERIAL PRIMARY KEY,
    task_code VARCHAR(50) NOT NULL UNIQUE,
    task_type VARCHAR(30) NOT NULL,
    description VARCHAR(200),
    total_count INTEGER NOT NULL DEFAULT 0,
    success_count INTEGER NOT NULL DEFAULT 0,
    failed_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_batch_task_creator FOREIGN KEY (created_by) REFERENCES t_user(id)
);

COMMENT ON TABLE t_batch_task IS '批量任务表';
COMMENT ON COLUMN t_batch_task.task_type IS '任务类型：GOODS_IMPORT=商品导入, USER_IMPORT=用户导入, DATA_EXPORT=数据导出';
COMMENT ON COLUMN t_batch_task.status IS '任务状态：PENDING=待处理, RUNNING=运行中, COMPLETED=已完成, FAILED=失败';

-- 42. 批量任务明细表
CREATE TABLE t_batch_task_item (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    item_data JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_batch_item_task FOREIGN KEY (task_id) REFERENCES t_batch_task(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_batch_task_item IS '批量任务明细表';
COMMENT ON COLUMN t_batch_task_item.status IS '状态：PENDING=待处理, SUCCESS=成功, FAILED=失败';

-- 43. 定时任务表（继承BaseEntity）
CREATE TABLE t_scheduled_task (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

COMMENT ON TABLE t_scheduled_task IS '定时任务表（支持软删除，便于任务历史追踪）';
COMMENT ON COLUMN t_scheduled_task.name IS '任务名称（唯一标识）';
COMMENT ON COLUMN t_scheduled_task.status IS '任务状态：ENABLED=启用, PAUSED=暂停';

-- 44. 任务执行记录表（不继承BaseEntity，只有id）
CREATE TABLE t_task_execution (
    id BIGSERIAL PRIMARY KEY,
    task_name VARCHAR(128) NOT NULL,
    status VARCHAR(20) NOT NULL,
    params TEXT,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    node VARCHAR(64),
    error TEXT
);

COMMENT ON TABLE t_task_execution IS '任务执行记录表（不继承BaseEntity）';
COMMENT ON COLUMN t_task_execution.status IS '执行状态：RUNNING=运行中, SUCCESS=成功, FAILED=失败, CANCELLED=已取消';
COMMENT ON COLUMN t_task_execution.node IS '执行节点标识';

-- =====================================================
-- 运营工具 (4个表)
-- =====================================================

-- 45. 营销活动表
CREATE TABLE t_marketing_campaign (
    id BIGSERIAL PRIMARY KEY,
    campaign_code VARCHAR(50) NOT NULL UNIQUE,
    campaign_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    campaign_type VARCHAR(30) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    budget DECIMAL(12, 2),
    target_users JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_campaign_creator FOREIGN KEY (created_by) REFERENCES t_user(id)
);

COMMENT ON TABLE t_marketing_campaign IS '营销活动表';
COMMENT ON COLUMN t_marketing_campaign.campaign_type IS '活动类型：DISCOUNT=折扣活动, COUPON=优惠券发放, POINTS=积分活动';
COMMENT ON COLUMN t_marketing_campaign.status IS '活动状态：DRAFT=草稿, ACTIVE=进行中, PAUSED=已暂停, COMPLETED=已结束';

-- 46. 商家看板表
CREATE TABLE t_merchant_dashboard (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    total_goods INTEGER NOT NULL DEFAULT 0,
    on_sale_goods INTEGER NOT NULL DEFAULT 0,
    total_orders INTEGER NOT NULL DEFAULT 0,
    total_revenue DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    avg_rating DECIMAL(3, 2) NOT NULL DEFAULT 0.00,
    total_reviews INTEGER NOT NULL DEFAULT 0,
    follower_count INTEGER NOT NULL DEFAULT 0,
    response_rate DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    avg_response_time BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_merchant_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_merchant_dashboard IS '商家看板表（卖家数据统计）';
COMMENT ON COLUMN t_merchant_dashboard.avg_response_time IS '平均回复时长（分钟）';

-- 47. 导出任务表
CREATE TABLE t_export_job (
    id BIGSERIAL PRIMARY KEY,
    job_code VARCHAR(50) NOT NULL UNIQUE,
    export_type VARCHAR(30) NOT NULL,
    query_params JSONB,
    file_name VARCHAR(200) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT,
    record_count BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_export_creator FOREIGN KEY (created_by) REFERENCES t_user(id)
);

COMMENT ON TABLE t_export_job IS '导出任务表（Excel、CSV等）';
COMMENT ON COLUMN t_export_job.export_type IS '导出类型：GOODS=商品, ORDER=订单, USER=用户, REVIEW=评价';
COMMENT ON COLUMN t_export_job.status IS '任务状态：PENDING=待处理, RUNNING=运行中, COMPLETED=已完成, FAILED=失败';

-- 48. 订阅表
CREATE TABLE t_subscription (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    subscription_type VARCHAR(30) NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_subscription_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_subscription UNIQUE (user_id, subscription_type, target_id)
);

COMMENT ON TABLE t_subscription IS '订阅表（价格变动、上新提醒等）';
COMMENT ON COLUMN t_subscription.subscription_type IS '订阅类型：PRICE_DROP=降价提醒, NEW_GOODS=上新提醒, SELLER=卖家动态';
COMMENT ON COLUMN t_subscription.target_type IS '目标类型：GOODS=商品, SELLER=卖家, CATEGORY=分类';

-- =====================================================
-- 完成！🎉 所有缺失的表已创建完毕！
-- =====================================================
