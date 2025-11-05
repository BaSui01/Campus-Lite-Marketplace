-- =====================================================
-- V8: 修复 AuditLog 表的Schema不匹配问题
-- =====================================================
-- 将V1的简化Schema升级为完整的增强版Schema

-- 1. 重命名现有字段以匹配实体类
ALTER TABLE t_audit_log RENAME COLUMN user_id TO operator_id;
ALTER TABLE t_audit_log RENAME COLUMN action TO action_type;
ALTER TABLE t_audit_log RENAME COLUMN resource_type TO target_type;
ALTER TABLE t_audit_log RENAME COLUMN resource_id TO target_id;

-- 2. 添加操作人用户名字段
ALTER TABLE t_audit_log ADD COLUMN IF NOT EXISTS operator_name VARCHAR(50);

-- 3. 扩展 action_type 字段长度（支持更长的枚举值）
ALTER TABLE t_audit_log ALTER COLUMN action_type TYPE VARCHAR(50);

-- 4. 添加批量操作ID列表字段
ALTER TABLE t_audit_log ADD COLUMN IF NOT EXISTS target_ids TEXT;

-- 5. 添加操作结果字段
ALTER TABLE t_audit_log ADD COLUMN IF NOT EXISTS result VARCHAR(20);

-- 6. 添加数据追踪字段
ALTER TABLE t_audit_log ADD COLUMN IF NOT EXISTS old_value TEXT;
ALTER TABLE t_audit_log ADD COLUMN IF NOT EXISTS new_value TEXT;

-- 7. 添加实体信息字段
ALTER TABLE t_audit_log ADD COLUMN IF NOT EXISTS entity_name VARCHAR(50);
ALTER TABLE t_audit_log ADD COLUMN IF NOT EXISTS entity_type VARCHAR(20);
ALTER TABLE t_audit_log ADD COLUMN IF NOT EXISTS entity_id BIGINT;

-- 8. 更新 NOT NULL 约束
-- entity_name 和 entity_type 需要设置默认值后再设置为NOT NULL
UPDATE t_audit_log SET entity_name = target_type WHERE entity_name IS NULL;
UPDATE t_audit_log SET entity_type = 'UNKNOWN' WHERE entity_type IS NULL;
ALTER TABLE t_audit_log ALTER COLUMN entity_name SET NOT NULL;
ALTER TABLE t_audit_log ALTER COLUMN entity_type SET NOT NULL;

-- 9. 添加撤销功能相关字段
ALTER TABLE t_audit_log ADD COLUMN IF NOT EXISTS is_reversible BOOLEAN DEFAULT FALSE;
ALTER TABLE t_audit_log ADD COLUMN IF NOT EXISTS revert_deadline TIMESTAMP;
ALTER TABLE t_audit_log ADD COLUMN IF NOT EXISTS reverted_by_log_id BIGINT;
ALTER TABLE t_audit_log ADD COLUMN IF NOT EXISTS reverted_at TIMESTAMP;
ALTER TABLE t_audit_log ADD COLUMN IF NOT EXISTS revert_count INTEGER DEFAULT 0;

-- 10. 创建新的索引（匹配实体类中的@Index定义）
CREATE INDEX IF NOT EXISTS idx_audit_operator ON t_audit_log(operator_id);
CREATE INDEX IF NOT EXISTS idx_audit_action ON t_audit_log(action_type);
CREATE INDEX IF NOT EXISTS idx_audit_target ON t_audit_log(target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_audit_created_at ON t_audit_log(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON t_audit_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_reversible ON t_audit_log(is_reversible);

-- 11. 更新外键约束名称（从 fk_audit_log_user 改为 fk_audit_log_operator）
ALTER TABLE t_audit_log DROP CONSTRAINT IF EXISTS fk_audit_log_user;
ALTER TABLE t_audit_log ADD CONSTRAINT fk_audit_log_operator 
    FOREIGN KEY (operator_id) REFERENCES t_user(id);

-- 12. 更新列注释
COMMENT ON COLUMN t_audit_log.operator_id IS '操作人ID';
COMMENT ON COLUMN t_audit_log.operator_name IS '操作人用户名';
COMMENT ON COLUMN t_audit_log.action_type IS '操作类型（枚举）';
COMMENT ON COLUMN t_audit_log.target_type IS '目标对象类型';
COMMENT ON COLUMN t_audit_log.target_id IS '目标对象ID';
COMMENT ON COLUMN t_audit_log.target_ids IS '批量操作的ID列表（逗号分隔）';
COMMENT ON COLUMN t_audit_log.result IS '操作结果（SUCCESS/FAILED）';
COMMENT ON COLUMN t_audit_log.old_value IS '修改前的数据（JSON格式）';
COMMENT ON COLUMN t_audit_log.new_value IS '修改后的数据（JSON格式）';
COMMENT ON COLUMN t_audit_log.entity_name IS '实体名称';
COMMENT ON COLUMN t_audit_log.entity_type IS '实体类型（枚举）';
COMMENT ON COLUMN t_audit_log.entity_id IS '被操作实体的ID';
COMMENT ON COLUMN t_audit_log.is_reversible IS '是否可撤销';
COMMENT ON COLUMN t_audit_log.revert_deadline IS '撤销截止时间';
COMMENT ON COLUMN t_audit_log.reverted_by_log_id IS '撤销操作的审计日志ID';
COMMENT ON COLUMN t_audit_log.reverted_at IS '撤销时间';
COMMENT ON COLUMN t_audit_log.revert_count IS '撤销次数';
