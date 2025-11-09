-- ==================== 登录设备管理表 ====================
-- 作者: BaSui 😎
-- 日期: 2025-11-09
-- 描述: 创建登录设备表，用于记录用户的登录设备信息

-- 创建登录设备表
CREATE TABLE IF NOT EXISTS login_devices (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    device_name VARCHAR(200) NOT NULL COMMENT '设备名称（如：Windows 11 - Chrome）',
    device_type VARCHAR(20) NOT NULL COMMENT '设备类型（mobile/desktop/tablet）',
    os VARCHAR(100) COMMENT '操作系统',
    browser VARCHAR(100) COMMENT '浏览器',
    ip VARCHAR(50) COMMENT 'IP 地址',
    location VARCHAR(200) COMMENT '地理位置',
    user_agent VARCHAR(500) COMMENT 'User-Agent',
    last_active_at TIMESTAMP NOT NULL COMMENT '最后活跃时间',
    is_current BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否当前设备',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',

    CONSTRAINT fk_login_device_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_login_device_user_id ON login_devices(user_id);
CREATE INDEX IF NOT EXISTS idx_login_device_last_active_at ON login_devices(last_active_at);

-- 添加注释
COMMENT ON TABLE login_devices IS '登录设备表 - 记录用户的登录设备信息';
COMMENT ON COLUMN login_devices.id IS '设备ID';
COMMENT ON COLUMN login_devices.user_id IS '用户ID';
COMMENT ON COLUMN login_devices.device_name IS '设备名称（如：Windows 11 - Chrome）';
COMMENT ON COLUMN login_devices.device_type IS '设备类型（mobile/desktop/tablet）';
COMMENT ON COLUMN login_devices.os IS '操作系统';
COMMENT ON COLUMN login_devices.browser IS '浏览器';
COMMENT ON COLUMN login_devices.ip IS 'IP 地址';
COMMENT ON COLUMN login_devices.location IS '地理位置';
COMMENT ON COLUMN login_devices.user_agent IS 'User-Agent';
COMMENT ON COLUMN login_devices.last_active_at IS '最后活跃时间';
COMMENT ON COLUMN login_devices.is_current IS '是否当前设备';
COMMENT ON COLUMN login_devices.created_at IS '创建时间';
COMMENT ON COLUMN login_devices.updated_at IS '更新时间';
