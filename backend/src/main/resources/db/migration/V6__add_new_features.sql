-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║  数据库迁移脚本 V6 - 新增功能合集                                        ║
-- ║  作者: BaSui 😎 | 日期: 2025-11-08                                      ║
-- ║  描述: 合并多个小功能的迁移脚本                                           ║
-- ╚═══════════════════════════════════════════════════════════════════════╝

-- =====================================================
-- 1. 修复错误日志表的 error_type 字段长度
-- =====================================================
-- 原长度50不足以存储完整的异常类名（如org.springframework.web.servlet.resource.NoResourceFoundException）

ALTER TABLE t_error_log ALTER COLUMN error_type TYPE VARCHAR(255);

COMMENT ON COLUMN t_error_log.error_type IS '错误类型（异常类全限定名，最长255字符）';

-- =====================================================
-- 2. 创建消息搜索功能相关表
-- =====================================================

-- 消息搜索历史表
CREATE TABLE IF NOT EXISTS message_search_history (
    id VARCHAR(36) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    dispute_id BIGINT NOT NULL,
    keyword VARCHAR(200) NOT NULL,
    result_count INT DEFAULT 0,
    filters JSONB,
    searched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_msh_user_dispute ON message_search_history(user_id, dispute_id);
CREATE INDEX IF NOT EXISTS idx_msh_searched_at ON message_search_history(searched_at);
CREATE INDEX IF NOT EXISTS idx_msh_keyword ON message_search_history(keyword);

-- 消息搜索统计表
CREATE TABLE IF NOT EXISTS message_search_statistics (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    dispute_id BIGINT NOT NULL,
    search_date DATE NOT NULL,
    total_searches INT DEFAULT 0,
    successful_searches INT DEFAULT 0,
    popular_keywords TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_dispute_date UNIQUE (user_id, dispute_id, search_date)
);

CREATE INDEX IF NOT EXISTS idx_mss_search_date ON message_search_statistics(search_date);
CREATE INDEX IF NOT EXISTS idx_mss_user_dispute ON message_search_statistics(user_id, dispute_id);

-- =====================================================
-- 3. 创建轮播图表（Banner）
-- =====================================================

CREATE TABLE IF NOT EXISTS t_banner (
    -- 主键
    id BIGSERIAL PRIMARY KEY,

    -- 基本信息
    title VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    image_url VARCHAR(500) NOT NULL,
    link_url VARCHAR(500),

    -- 排序和状态
    sort_order INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',

    -- 定时上下线
    start_time TIMESTAMP,
    end_time TIMESTAMP,

    -- 统计数据
    click_count INTEGER NOT NULL DEFAULT 0,
    view_count INTEGER NOT NULL DEFAULT 0,

    -- 审计字段
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_banner_status ON t_banner(status) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_banner_sort_order ON t_banner(sort_order) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_banner_start_time ON t_banner(start_time) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_banner_end_time ON t_banner(end_time) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_banner_status_sort ON t_banner(status, sort_order) WHERE deleted = FALSE;

-- 添加表注释
COMMENT ON TABLE t_banner IS '轮播图表 - 用于首页轮播图管理';
COMMENT ON COLUMN t_banner.id IS '主键ID';
COMMENT ON COLUMN t_banner.title IS '轮播图标题';
COMMENT ON COLUMN t_banner.description IS '轮播图描述';
COMMENT ON COLUMN t_banner.image_url IS '图片 URL';
COMMENT ON COLUMN t_banner.link_url IS '跳转链接（可选）';
COMMENT ON COLUMN t_banner.sort_order IS '排序顺序（数字越小越靠前）';
COMMENT ON COLUMN t_banner.status IS '状态（ENABLED/DISABLED）';
COMMENT ON COLUMN t_banner.start_time IS '开始时间（可选，用于定时上线）';
COMMENT ON COLUMN t_banner.end_time IS '结束时间（可选，用于定时下线）';
COMMENT ON COLUMN t_banner.click_count IS '点击次数';
COMMENT ON COLUMN t_banner.view_count IS '展示次数';
COMMENT ON COLUMN t_banner.created_at IS '创建时间';
COMMENT ON COLUMN t_banner.updated_at IS '更新时间';
COMMENT ON COLUMN t_banner.deleted IS '软删除标记';

-- 插入测试数据（使用 Unsplash 临时图片）
INSERT INTO t_banner (title, description, image_url, link_url, sort_order, status, created_at, updated_at) VALUES
(
    '校园轻享集市',
    '让闲置物品找到新主人，让环保成为生活方式',
    'https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=1920&h=500&fit=crop&q=80',
    '/goods',
    1,
    'ENABLED',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    '安全交易，放心购物',
    '实名认证，交易保障，让每一笔交易都安心',
    'https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=1920&h=500&fit=crop&q=80',
    '/about',
    2,
    'ENABLED',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    '社区互动，分享生活',
    '不仅是交易平台，更是校园生活的分享社区',
    'https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=1920&h=500&fit=crop&q=80',
    '/community',
    3,
    'ENABLED',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- =====================================================
-- 完成！🎉
-- =====================================================
-- ✅ 修复了错误日志表字段长度
-- ✅ 创建了消息搜索功能表（2个表）
-- ✅ 创建了轮播图表（含3条测试数据）
-- =====================================================
