-- =====================================================
-- V11: 创建卖家关注表
-- =====================================================
-- 目的：将 Follow 实体从 t_user_follow 分离，使用独立的 t_seller_follow 表
-- 作者：BaSui 😎
-- 日期：2025-11-10
-- =====================================================

-- 创建卖家关注表
CREATE TABLE IF NOT EXISTS t_seller_follow (
    id BIGSERIAL PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_seller_follow_follower FOREIGN KEY (follower_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_seller_follow_seller FOREIGN KEY (seller_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_seller_follow_follower_seller UNIQUE (follower_id, seller_id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_seller_follow_follower ON t_seller_follow(follower_id);
CREATE INDEX IF NOT EXISTS idx_seller_follow_seller ON t_seller_follow(seller_id);

-- 表注释
COMMENT ON TABLE t_seller_follow IS '卖家关注表（用户对卖家的关注关系）';
COMMENT ON COLUMN t_seller_follow.follower_id IS '关注者ID（粉丝）';
COMMENT ON COLUMN t_seller_follow.seller_id IS '被关注者ID（卖家）';

-- =====================================================
-- 数据迁移（如果存在旧数据）
-- =====================================================
-- 如果 t_user_follow 表中存在 seller_id 字段的旧数据，迁移到新表
-- 注意：这里假设旧数据已经被清理，如果需要迁移，请手动执行
-- =====================================================
