-- 为用户动态表增加目标类型字段（POST/GOODS），用于前端正确展示与后续扩展
-- 兼容性：为已有数据填充默认值 POST

ALTER TABLE t_user_feed
    ADD COLUMN IF NOT EXISTS target_type VARCHAR(20);

UPDATE t_user_feed
   SET target_type = 'POST'
 WHERE target_type IS NULL;

-- 可选索引：如需基于目标类型做统计或过滤，可后续按需添加
-- CREATE INDEX IF NOT EXISTS idx_user_feed_target_type ON t_user_feed(target_type);

