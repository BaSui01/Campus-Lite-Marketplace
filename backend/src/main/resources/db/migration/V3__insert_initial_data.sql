-- =====================================================
-- Flyway 数据库迁移脚本 V3 - 初始化数据
-- =====================================================
-- 作者: BaSui 😎
-- 日期: 2025-11-05
-- 描述: 插入系统初始化数据（仅基础数据，角色权限由 DatabaseSeeder 管理）
-- 数据库: PostgreSQL 14+
--
-- ⚠️ 重要说明：
-- 1. 本脚本只插入基础数据（校区、分类、标签）
-- 2. 角色、权限、用户由 DatabaseSeeder 代码管理（支持动态更新）
-- 3. 密码使用 BCrypt 加密（与代码逻辑一致）
-- =====================================================

-- =====================================================
-- 1. 插入校区数据（与 DatabaseSeeder 保持一致）
-- =====================================================
-- 注意：使用 ON CONFLICT DO NOTHING 实现幂等性
INSERT INTO t_campus (code, name, status, created_at, updated_at, deleted) VALUES
('DEFAULT', '默认校区', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('NORTH', '北校区', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('SOUTH', '南校区', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- 2. 插入商品分类数据（与 DatabaseSeeder 保持一致）
-- =====================================================
-- 一级分类（6个）
INSERT INTO t_category (name, description, parent_id, sort_order, created_at, updated_at, deleted) VALUES
('数码电子', '手机、电脑、数码配件等', NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('图书教材', '教材、课外书、考研资料等', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('运动户外', '运动器材、户外装备等', NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('服饰鞋包', '衣服、鞋子、包包等', NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('美妆个护', '化妆品、护肤品等', NULL, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('生活用品', '日用品、家居用品等', NULL, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false)
ON CONFLICT (name) DO NOTHING;

-- =====================================================
-- 3. 插入常用标签数据
-- =====================================================
INSERT INTO t_tag (name, created_at, updated_at, deleted) VALUES
('全新', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('九成新', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('八成新', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('急售', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('可议价', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('包邮', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('自提', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('正品保证', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('限时优惠', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('热门', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false)
ON CONFLICT (name) DO NOTHING;

-- =====================================================
-- 完成！🎉
-- =====================================================
--
-- 📝 说明：
-- 1. ✅ 校区数据：3个（DEFAULT, NORTH, SOUTH）
-- 2. ✅ 分类数据：6个一级分类
-- 3. ✅ 标签数据：10个常用标签
-- 4. ⚠️ 角色、权限、用户由 DatabaseSeeder 管理（代码初始化）
--
-- 🔐 密码说明：
-- - 管理员密码：admin123（由 DatabaseSeeder 自动加密）
-- - 测试用户密码：password123（由 DatabaseSeeder 自动加密）
--
-- 🚀 启动流程：
-- 1. Flyway 执行 V1（创建表）→ V2（创建索引）→ V3（插入基础数据）
-- 2. DatabaseSeeder 执行（插入角色、权限、用户）
--
-- =====================================================
