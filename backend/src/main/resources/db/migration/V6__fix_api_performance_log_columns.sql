-- =====================================================
-- V6: 修复 API 性能日志表的字段命名
-- =====================================================
-- 将字段名从旧命名改为符合 JPA 实体类的命名

-- 1. 重命名 method → http_method
ALTER TABLE t_api_performance_log RENAME COLUMN method TO http_method;

-- 2. 重命名 uri → api_path，并修改长度限制
ALTER TABLE t_api_performance_log RENAME COLUMN uri TO api_path;
ALTER TABLE t_api_performance_log ALTER COLUMN api_path TYPE VARCHAR(200);

-- 3. 重命名 duration → response_time，并修改类型
ALTER TABLE t_api_performance_log RENAME COLUMN duration TO response_time;
ALTER TABLE t_api_performance_log ALTER COLUMN response_time TYPE INTEGER;

COMMENT ON COLUMN t_api_performance_log.http_method IS 'HTTP方法 (GET/POST/PUT/DELETE)';
COMMENT ON COLUMN t_api_performance_log.api_path IS 'API路径';
COMMENT ON COLUMN t_api_performance_log.response_time IS '响应时间（毫秒）';
