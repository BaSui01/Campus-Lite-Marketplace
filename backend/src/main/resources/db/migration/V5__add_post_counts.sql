-- =====================================================
-- 数据库迁移脚本 V5：添加帖子统计字段
--
-- 功能：为 t_post 表添加点赞数、收藏数、置顶、热门标记
-- 作者：BaSui 😎
-- 日期：2025-11-09
-- =====================================================

-- 1. 添加点赞数字段
ALTER TABLE t_post ADD COLUMN IF NOT EXISTS like_count INTEGER NOT NULL DEFAULT 0;
COMMENT ON COLUMN t_post.like_count IS '点赞数（前端显示必需）';

-- 2. 添加收藏数字段
ALTER TABLE t_post ADD COLUMN IF NOT EXISTS collect_count INTEGER NOT NULL DEFAULT 0;
COMMENT ON COLUMN t_post.collect_count IS '收藏数（前端显示必需）';

-- 3. 添加置顶标记
ALTER TABLE t_post ADD COLUMN IF NOT EXISTS is_top BOOLEAN NOT NULL DEFAULT FALSE;
COMMENT ON COLUMN t_post.is_top IS '是否置顶（管理功能）';

-- 4. 添加热门标记
ALTER TABLE t_post ADD COLUMN IF NOT EXISTS is_hot BOOLEAN NOT NULL DEFAULT FALSE;
COMMENT ON COLUMN t_post.is_hot IS '是否热门（推荐算法）';

-- 5. 创建索引（提升查询性能）
CREATE INDEX IF NOT EXISTS idx_post_like_count ON t_post(like_count DESC);
CREATE INDEX IF NOT EXISTS idx_post_collect_count ON t_post(collect_count DESC);
CREATE INDEX IF NOT EXISTS idx_post_is_top ON t_post(is_top) WHERE is_top = TRUE;
CREATE INDEX IF NOT EXISTS idx_post_is_hot ON t_post(is_hot) WHERE is_hot = TRUE;

-- 6. 初始化现有数据的点赞数（从 t_post_like 表统计）
UPDATE t_post p
SET like_count = (
    SELECT COUNT(*)
    FROM t_post_like pl
    WHERE pl.post_id = p.id AND pl.deleted = FALSE
)
WHERE EXISTS (SELECT 1 FROM t_post_like pl WHERE pl.post_id = p.id);

-- 7. 初始化现有数据的收藏数（从 t_post_collect 表统计）
UPDATE t_post p
SET collect_count = (
    SELECT COUNT(*)
    FROM t_post_collect pc
    WHERE pc.post_id = p.id AND pc.deleted = FALSE
)
WHERE EXISTS (SELECT 1 FROM t_post_collect pc WHERE pc.post_id = p.id);

-- 8. 标记热门帖子（点赞数 >= 10 或 浏览量 >= 100）
UPDATE t_post
SET is_hot = TRUE
WHERE like_count >= 10 OR view_count >= 100;

-- =====================================================
-- 回滚脚本（如果需要回滚，执行以下语句）
-- =====================================================
-- DROP INDEX IF EXISTS idx_post_like_count;
-- DROP INDEX IF EXISTS idx_post_collect_count;
-- DROP INDEX IF EXISTS idx_post_is_top;
-- DROP INDEX IF EXISTS idx_post_is_hot;
-- ALTER TABLE t_post DROP COLUMN IF EXISTS like_count;
-- ALTER TABLE t_post DROP COLUMN IF EXISTS collect_count;
-- ALTER TABLE t_post DROP COLUMN IF EXISTS is_top;
-- ALTER TABLE t_post DROP COLUMN IF EXISTS is_hot;
