-- =====================================================
-- V7: 修复 Appeal 表缺失的字段
-- =====================================================
-- 根据 Appeal 实体类补充缺失的字段

-- 1. 添加申诉类型字段
ALTER TABLE t_appeal ADD COLUMN appeal_type VARCHAR(50);
UPDATE t_appeal SET appeal_type = 'GENERAL' WHERE appeal_type IS NULL;
ALTER TABLE t_appeal ALTER COLUMN appeal_type SET NOT NULL;

-- 2. 添加截止时间字段
ALTER TABLE t_appeal ADD COLUMN deadline TIMESTAMP;
UPDATE t_appeal SET deadline = created_at + INTERVAL '5 days' WHERE deadline IS NULL;
ALTER TABLE t_appeal ALTER COLUMN deadline SET NOT NULL;

-- 3. 添加审核人字段（将 handler_id 重命名为 reviewer_id）
ALTER TABLE t_appeal RENAME COLUMN handler_id TO reviewer_id;

-- 4. 添加审核人用户名字段
ALTER TABLE t_appeal ADD COLUMN reviewer_name VARCHAR(50);

-- 5. 添加审核时间字段（将 handled_at 重命名为 reviewed_at）
ALTER TABLE t_appeal RENAME COLUMN handled_at TO reviewed_at;

-- 6. 添加审核意见字段（将 handle_result 重命名并改为TEXT类型）
ALTER TABLE t_appeal RENAME COLUMN handle_result TO review_comment;
ALTER TABLE t_appeal ALTER COLUMN review_comment TYPE TEXT;

-- 7. 添加附件列表字段（JSON格式）
ALTER TABLE t_appeal ADD COLUMN attachments TEXT;

-- 8. 添加处理结果详情字段
ALTER TABLE t_appeal ADD COLUMN result_details TEXT;

-- 9. 将 reason 字段类型改为TEXT（从VARCHAR(500)扩展）
ALTER TABLE t_appeal ALTER COLUMN reason TYPE TEXT;

-- 10. 删除 appeal_code 字段（实体类中没有此字段）
ALTER TABLE t_appeal DROP COLUMN appeal_code;

-- 11. 将 target_type 和 appeal_type 字段长度扩展为50（支持枚举）
ALTER TABLE t_appeal ALTER COLUMN target_type TYPE VARCHAR(50);

-- 更新注释
COMMENT ON COLUMN t_appeal.appeal_type IS '申诉类型';
COMMENT ON COLUMN t_appeal.deadline IS '截止时间';
COMMENT ON COLUMN t_appeal.reviewer_id IS '审核人ID';
COMMENT ON COLUMN t_appeal.reviewer_name IS '审核人用户名';
COMMENT ON COLUMN t_appeal.reviewed_at IS '审核时间';
COMMENT ON COLUMN t_appeal.review_comment IS '审核意见';
COMMENT ON COLUMN t_appeal.attachments IS '附件列表（JSON格式）';
COMMENT ON COLUMN t_appeal.result_details IS '处理结果详情';

-- =====================================================
-- 修复 t_appeal_material 表的 appeal_id 类型
-- =====================================================
-- 将 appeal_id 字段从 BIGINT 改为 VARCHAR (实体类中定义为 String)

-- 1. 临时删除外键约束
ALTER TABLE t_appeal_material DROP CONSTRAINT IF EXISTS fk_material_appeal;

-- 2. 修改字段类型
ALTER TABLE t_appeal_material ALTER COLUMN appeal_id TYPE VARCHAR(255);

-- 3. V4脚本的AppealMaterial表与实体类不匹配，需要修正字段
-- 将 material_type 重命名为 file_type
ALTER TABLE t_appeal_material RENAME COLUMN material_type TO file_type;

-- 将 file_url 重命名为 file_path
ALTER TABLE t_appeal_material RENAME COLUMN file_url TO file_path;

-- 添加缺失字段
ALTER TABLE t_appeal_material ADD COLUMN IF NOT EXISTS mime_type VARCHAR(100);
ALTER TABLE t_appeal_material ADD COLUMN IF NOT EXISTS thumbnail_path VARCHAR(500);
ALTER TABLE t_appeal_material ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'UPLOADED';
ALTER TABLE t_appeal_material ADD COLUMN IF NOT EXISTS uploaded_by BIGINT;
ALTER TABLE t_appeal_material ADD COLUMN IF NOT EXISTS uploaded_by_name VARCHAR(50);
ALTER TABLE t_appeal_material ADD COLUMN IF NOT EXISTS uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE t_appeal_material ADD COLUMN IF NOT EXISTS is_primary BOOLEAN DEFAULT FALSE;
ALTER TABLE t_appeal_material ADD COLUMN IF NOT EXISTS file_hash VARCHAR(64);
ALTER TABLE t_appeal_material ADD COLUMN IF NOT EXISTS virus_scan_result VARCHAR(20) DEFAULT 'PENDING';

-- 更新注释
COMMENT ON COLUMN t_appeal_material.file_type IS '文件类型 (image/pdf/document等)';
COMMENT ON COLUMN t_appeal_material.mime_type IS 'MIME类型';
COMMENT ON COLUMN t_appeal_material.thumbnail_path IS '缩略图路径';
COMMENT ON COLUMN t_appeal_material.status IS '文件状态';
COMMENT ON COLUMN t_appeal_material.uploaded_by IS '上传用户ID';
COMMENT ON COLUMN t_appeal_material.uploaded_by_name IS '上传用户名';
COMMENT ON COLUMN t_appeal_material.uploaded_at IS '上传时间';
COMMENT ON COLUMN t_appeal_material.is_primary IS '是否为主文件';
COMMENT ON COLUMN t_appeal_material.file_hash IS '文件哈希值（用于去重）';
COMMENT ON COLUMN t_appeal_material.virus_scan_result IS '病毒扫描结果';
