-- ═══════════════════════════════════════════════════════════════════════
-- 校园轻享集市系统 - 聊天功能表创建脚本
-- Campus Lite Marketplace - Chat Tables Creation Script
-- ═══════════════════════════════════════════════════════════════════════
--
-- 📝 作者: BaSui 😎 | 创建日期: 2025-11-04
-- 🎯 用途: 创建聊天会话和消息表
-- 🚀 版本: V4 - 聊天功能表
-- 📦 数据库: PostgreSQL 14+
-- 🔧 Flyway: 自动执行此脚本
--
-- ⚠️ 重要提醒:
--    - 此脚本由 Flyway 自动执行，请勿手动修改！
--    - 支持文本、图片、语音、视频等多种消息类型
--    - 支持消息撤回功能（1分钟内）
--    - 支持消息已读/未读状态
--
-- ═══════════════════════════════════════════════════════════════════════

-- ==================== 1. 会话表 ====================

CREATE TABLE IF NOT EXISTS t_conversation (
    id BIGSERIAL PRIMARY KEY,
    user1_id BIGINT NOT NULL,
    user2_id BIGINT NOT NULL,
    last_message_id BIGINT,
    last_message_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    FOREIGN KEY (user1_id) REFERENCES t_user(id) ON DELETE CASCADE,
    FOREIGN KEY (user2_id) REFERENCES t_user(id) ON DELETE CASCADE,
    UNIQUE (user1_id, user2_id)
);

COMMENT ON TABLE t_conversation IS '聊天会话表';
COMMENT ON COLUMN t_conversation.user1_id IS '用户1 ID（较小的用户 ID）';
COMMENT ON COLUMN t_conversation.user2_id IS '用户2 ID（较大的用户 ID）';
COMMENT ON COLUMN t_conversation.last_message_id IS '最后一条消息 ID';
COMMENT ON COLUMN t_conversation.last_message_time IS '最后一条消息时间';

-- ==================== 2. 消息表 ====================

CREATE TABLE IF NOT EXISTS t_message (
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
    FOREIGN KEY (conversation_id) REFERENCES t_conversation(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES t_user(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES t_user(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_message IS '聊天消息表';
COMMENT ON COLUMN t_message.message_type IS '消息类型（TEXT文本/IMAGE图片/VOICE语音/VIDEO视频/FILE文件）';
COMMENT ON COLUMN t_message.content IS '消息内容（文本内容或文件URL）';
COMMENT ON COLUMN t_message.status IS '消息状态（UNREAD未读/READ已读）';
COMMENT ON COLUMN t_message.is_recalled IS '是否已撤回';

-- ==================== 3. 创建索引 ====================

-- 会话表索引
CREATE INDEX IF NOT EXISTS idx_conversation_user1 ON t_conversation(user1_id);
CREATE INDEX IF NOT EXISTS idx_conversation_user2 ON t_conversation(user2_id);
CREATE INDEX IF NOT EXISTS idx_conversation_last_time ON t_conversation(last_message_time DESC);

-- 消息表索引
CREATE INDEX IF NOT EXISTS idx_message_conversation ON t_message(conversation_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_message_sender ON t_message(sender_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_message_receiver ON t_message(receiver_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_message_status ON t_message(status) WHERE status = 'UNREAD';
CREATE INDEX IF NOT EXISTS idx_message_created_at ON t_message(created_at DESC);

-- 未读消息查询优化
CREATE INDEX IF NOT EXISTS idx_message_receiver_unread ON t_message(receiver_id, status, created_at DESC) WHERE status = 'UNREAD';

-- ═══════════════════════════════════════════════════════════════════════
-- 🎉 聊天功能表创建完成！
--
-- 📊 创建的表：
--    - t_conversation（会话表）
--    - t_message（消息表）
--
-- 📊 创建的索引：
--    - 会话表索引：3 个
--    - 消息表索引：6 个
--
-- 💡 功能特性：
--    - ✅ 支持多种消息类型（文本、图片、语音、视频、文件）
--    - ✅ 支持消息撤回（1分钟内）
--    - ✅ 支持消息已读/未读状态
--    - ✅ 支持会话列表排序（按最后消息时间）
--    - ✅ 支持未读消息统计
--
-- ═══════════════════════════════════════════════════════════════════════
