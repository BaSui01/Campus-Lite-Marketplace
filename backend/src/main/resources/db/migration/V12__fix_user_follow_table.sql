-- =====================================================
-- V12: 修复用户关注表结构
-- =====================================================
-- 目的：删除 t_user_follow 表中的 seller_id 字段约束，确保只有 following_id
-- 作者：BaSui 😎
-- 日期：2025-11-10
-- =====================================================

-- 检查并删除 seller_id 相关约束
DO $$
BEGIN
    -- 删除外键约束（如果存在）
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_user_follow_seller' 
        AND table_name = 't_user_follow'
    ) THEN
        ALTER TABLE t_user_follow DROP CONSTRAINT fk_user_follow_seller;
    END IF;

    -- 删除唯一约束（如果存在）
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'uk_user_follow_follower_seller' 
        AND table_name = 't_user_follow'
    ) THEN
        ALTER TABLE t_user_follow DROP CONSTRAINT uk_user_follow_follower_seller;
    END IF;

    -- 删除索引（如果存在）
    IF EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE indexname = 'idx_user_follow_seller'
    ) THEN
        DROP INDEX idx_user_follow_seller;
    END IF;

    -- 删除 seller_id 列（如果存在）
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 't_user_follow' 
        AND column_name = 'seller_id'
    ) THEN
        ALTER TABLE t_user_follow DROP COLUMN seller_id;
    END IF;

    -- 确保 following_id 列存在
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 't_user_follow' 
        AND column_name = 'following_id'
    ) THEN
        ALTER TABLE t_user_follow ADD COLUMN following_id BIGINT NOT NULL;
        ALTER TABLE t_user_follow ADD CONSTRAINT fk_user_follow_following 
            FOREIGN KEY (following_id) REFERENCES t_user(id) ON DELETE CASCADE;
    END IF;

    -- 确保正确的唯一约束存在
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'uk_user_follow' 
        AND table_name = 't_user_follow'
    ) THEN
        ALTER TABLE t_user_follow ADD CONSTRAINT uk_user_follow 
            UNIQUE (follower_id, following_id);
    END IF;

    -- 确保正确的索引存在
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE indexname = 'idx_user_follow_follower'
    ) THEN
        CREATE INDEX idx_user_follow_follower ON t_user_follow(follower_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE indexname = 'idx_user_follow_following'
    ) THEN
        CREATE INDEX idx_user_follow_following ON t_user_follow(following_id);
    END IF;
END $$;

-- 更新表注释
COMMENT ON TABLE t_user_follow IS '用户关注表（用户对用户的关注关系）';
COMMENT ON COLUMN t_user_follow.follower_id IS '关注者ID（粉丝）';
COMMENT ON COLUMN t_user_follow.following_id IS '被关注者ID（关注的人）';
