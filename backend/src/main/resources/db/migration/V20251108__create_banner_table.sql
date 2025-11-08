-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║  创建轮播图表（Banner）                                                  ║
-- ║  作者: BaSui 😎 | 日期: 2025-11-08                                      ║
-- ║  用途: 首页轮播图管理，支持定时上下线、点击统计                            ║
-- ╚═══════════════════════════════════════════════════════════════════════╝

-- ==================== 创建轮播图表 ====================

CREATE TABLE IF NOT EXISTS t_banner (
    -- 主键
    id BIGSERIAL PRIMARY KEY,
    
    -- 基本信息
    title VARCHAR(100) NOT NULL COMMENT '轮播图标题',
    description VARCHAR(200) COMMENT '轮播图描述',
    image_url VARCHAR(500) NOT NULL COMMENT '图片 URL',
    link_url VARCHAR(500) COMMENT '跳转链接（可选）',
    
    -- 排序和状态
    sort_order INTEGER NOT NULL DEFAULT 0 COMMENT '排序顺序（数字越小越靠前）',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态（ENABLED/DISABLED）',
    
    -- 定时上下线
    start_time TIMESTAMP COMMENT '开始时间（可选，用于定时上线）',
    end_time TIMESTAMP COMMENT '结束时间（可选，用于定时下线）',
    
    -- 统计数据
    click_count INTEGER NOT NULL DEFAULT 0 COMMENT '点击次数',
    view_count INTEGER NOT NULL DEFAULT 0 COMMENT '展示次数',
    
    -- 审计字段
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '软删除标记'
);

-- ==================== 创建索引 ====================

-- 状态索引（查询启用的轮播图）
CREATE INDEX idx_banner_status ON t_banner(status) WHERE deleted = FALSE;

-- 排序索引（按排序顺序查询）
CREATE INDEX idx_banner_sort_order ON t_banner(sort_order) WHERE deleted = FALSE;

-- 时间范围索引（查询在有效期内的轮播图）
CREATE INDEX idx_banner_start_time ON t_banner(start_time) WHERE deleted = FALSE;
CREATE INDEX idx_banner_end_time ON t_banner(end_time) WHERE deleted = FALSE;

-- 复合索引（状态 + 排序）
CREATE INDEX idx_banner_status_sort ON t_banner(status, sort_order) WHERE deleted = FALSE;

-- ==================== 添加表注释 ====================

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

-- ==================== 插入测试数据 ====================

-- 插入 3 张轮播图（使用 Unsplash 临时图片）
INSERT INTO t_banner (title, description, image_url, link_url, sort_order, status) VALUES
(
    '校园轻享集市',
    '让闲置物品找到新主人，让环保成为生活方式',
    'https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=1920&h=500&fit=crop&q=80',
    '/goods',
    1,
    'ENABLED'
),
(
    '安全交易，放心购物',
    '实名认证，交易保障，让每一笔交易都安心',
    'https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=1920&h=500&fit=crop&q=80',
    '/about',
    2,
    'ENABLED'
),
(
    '社区互动，分享生活',
    '不仅是交易平台，更是校园生活的分享社区',
    'https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=1920&h=500&fit=crop&q=80',
    '/community',
    3,
    'ENABLED'
);

-- ==================== 完成 ====================

-- 🎉 轮播图表创建完成！
-- 📊 已插入 3 条测试数据
-- 🔍 可以通过以下 SQL 查询：
--    SELECT * FROM t_banner WHERE status = 'ENABLED' ORDER BY sort_order;
