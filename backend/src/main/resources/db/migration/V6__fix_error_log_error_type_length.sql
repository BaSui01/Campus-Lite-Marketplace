-- 修复错误日志表的error_type字段长度
-- 原长度50不足以存储完整的异常类名（如org.springframework.web.servlet.resource.NoResourceFoundException）
-- 
-- @author BaSui 😎
-- @date 2025-11-06

-- 修改error_type字段长度从50改为255
ALTER TABLE t_error_log ALTER COLUMN error_type TYPE VARCHAR(255);

-- 添加注释说明
COMMENT ON COLUMN t_error_log.error_type IS '错误类型（异常类全限定名，最长255字符）';
