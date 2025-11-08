-- =====================================================
-- Flyway 数据库迁移脚本 V7 - 添加用户安全字段
-- =====================================================
-- 作者: BaSui 😎
-- 日期: 2025-11-08
-- 描述: 为 t_user 表添加邮箱验证、手机验证、两步验证相关字段
-- 数据库: PostgreSQL 14+
-- =====================================================

-- =====================================================
-- 1. 添加用户安全相关字段
-- =====================================================

-- 邮箱验证状态
ALTER TABLE t_user ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT false;

-- 手机号验证状态
ALTER TABLE t_user ADD COLUMN IF NOT EXISTS phone_verified BOOLEAN NOT NULL DEFAULT false;

-- 两步验证启用状态
ALTER TABLE t_user ADD COLUMN IF NOT EXISTS two_factor_enabled BOOLEAN NOT NULL DEFAULT false;

-- 两步验证密钥（TOTP Secret）
ALTER TABLE t_user ADD COLUMN IF NOT EXISTS two_factor_secret VARCHAR(32);

-- =====================================================
-- 2. 添加注释说明
-- =====================================================

COMMENT ON COLUMN t_user.email_verified IS '邮箱是否已验证';
COMMENT ON COLUMN t_user.phone_verified IS '手机号是否已验证';
COMMENT ON COLUMN t_user.two_factor_enabled IS '两步验证是否启用';
COMMENT ON COLUMN t_user.two_factor_secret IS '两步验证密钥（TOTP Secret）';

-- =====================================================
-- 3. 为已有用户设置默认值（数据迁移）
-- =====================================================

-- 将所有现有用户的验证状态设置为 false（安全起见）
UPDATE t_user
SET
    email_verified = false,
    phone_verified = false,
    two_factor_enabled = false
WHERE
    email_verified IS NULL
    OR phone_verified IS NULL
    OR two_factor_enabled IS NULL;

-- =====================================================
-- 完成！🎉
-- =====================================================
