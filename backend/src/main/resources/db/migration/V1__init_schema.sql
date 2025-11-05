-- =====================================================
-- Flyway 数据库迁移脚本 V1 - 基础表结构
-- =====================================================
-- 作者: BaSui 😎
-- 日期: 2025-11-05
-- 描述: 创建所有核心业务表（用户、商品、订单、评价等）
-- 数据库: PostgreSQL 14+
-- =====================================================

-- =====================================================
-- 1. 校区表 (t_campus)
-- =====================================================
CREATE TABLE t_campus (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

COMMENT ON TABLE t_campus IS '校区表';
COMMENT ON COLUMN t_campus.code IS '校区编码（唯一）';
COMMENT ON COLUMN t_campus.name IS '校区名称';
COMMENT ON COLUMN t_campus.status IS '校区状态：ACTIVE=启用, INACTIVE=停用';

-- =====================================================
-- 2. 用户表 (t_user)
-- =====================================================
CREATE TABLE t_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    avatar VARCHAR(500),
    nickname VARCHAR(100),
    points INTEGER NOT NULL DEFAULT 0,
    credit_score INTEGER NOT NULL DEFAULT 100,
    campus_id BIGINT,
    student_id VARCHAR(50) UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_user_campus FOREIGN KEY (campus_id) REFERENCES t_campus(id)
);

COMMENT ON TABLE t_user IS '用户表';
COMMENT ON COLUMN t_user.username IS '用户名（唯一）';
COMMENT ON COLUMN t_user.password IS '密码（加密存储）';
COMMENT ON COLUMN t_user.points IS '用户积分';
COMMENT ON COLUMN t_user.credit_score IS '信誉分（0-200，初始100）';
COMMENT ON COLUMN t_user.status IS '用户状态：ACTIVE=正常, BANNED=封禁, DELETED=注销';

-- =====================================================
-- 3. 角色表 (t_role)
-- =====================================================
CREATE TABLE t_role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_role IS '角色表';
COMMENT ON COLUMN t_role.name IS '角色名称（如 ROLE_ADMIN, ROLE_STUDENT）';

-- =====================================================
-- 4. 权限表 (t_permission)
-- =====================================================
CREATE TABLE t_permission (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_permission IS '权限表';
COMMENT ON COLUMN t_permission.name IS '权限名称（如 goods:create, order:manage）';

-- =====================================================
-- 5. 用户-角色关联表 (t_user_role)
-- =====================================================
CREATE TABLE t_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES t_role(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_user_role IS '用户-角色关联表（多对多）';

-- =====================================================
-- 6. 角色-权限关联表 (t_role_permission)
-- =====================================================
CREATE TABLE t_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES t_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES t_permission(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_role_permission IS '角色-权限关联表（多对多）';

-- =====================================================
-- 7. 分类表 (t_category)
-- =====================================================
CREATE TABLE t_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    parent_id BIGINT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES t_category(id)
);

COMMENT ON TABLE t_category IS '物品分类表（支持树形结构）';
COMMENT ON COLUMN t_category.parent_id IS '父级分类ID（NULL表示顶级分类）';
COMMENT ON COLUMN t_category.sort_order IS '排序权重（数字越大越靠前）';

-- =====================================================
-- 8. 商品表 (t_goods)
-- =====================================================
CREATE TABLE t_goods (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    category_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    campus_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    view_count INTEGER NOT NULL DEFAULT 0,
    favorite_count INTEGER NOT NULL DEFAULT 0,
    images TEXT[],
    extra_attrs JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_goods_category FOREIGN KEY (category_id) REFERENCES t_category(id),
    CONSTRAINT fk_goods_seller FOREIGN KEY (seller_id) REFERENCES t_user(id),
    CONSTRAINT fk_goods_campus FOREIGN KEY (campus_id) REFERENCES t_campus(id)
);

COMMENT ON TABLE t_goods IS '商品表';
COMMENT ON COLUMN t_goods.status IS '商品状态：PENDING=待审核, APPROVED=已上架, REJECTED=已拒绝, SOLD=已售出';
COMMENT ON COLUMN t_goods.images IS '商品图片URL数组';
COMMENT ON COLUMN t_goods.extra_attrs IS '扩展属性（JSONB格式）';

-- =====================================================
-- 9. 标签表 (t_tag)
-- =====================================================
CREATE TABLE t_tag (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

COMMENT ON TABLE t_tag IS '标签表';

-- =====================================================
-- 10. 商品-标签关联表 (t_goods_tag)
-- =====================================================
CREATE TABLE t_goods_tag (
    id BIGSERIAL PRIMARY KEY,
    goods_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_goods_tag_goods FOREIGN KEY (goods_id) REFERENCES t_goods(id) ON DELETE CASCADE,
    CONSTRAINT fk_goods_tag_tag FOREIGN KEY (tag_id) REFERENCES t_tag(id) ON DELETE CASCADE,
    CONSTRAINT uk_goods_tag UNIQUE (goods_id, tag_id)
);

COMMENT ON TABLE t_goods_tag IS '商品-标签关联表';

-- =====================================================
-- 11. 订单表 (t_order)
-- =====================================================
CREATE TABLE t_order (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    goods_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    campus_id BIGINT,
    amount DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    actual_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAYMENT',
    payment_method VARCHAR(20),
    payment_time TIMESTAMP,
    coupon_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_order_goods FOREIGN KEY (goods_id) REFERENCES t_goods(id),
    CONSTRAINT fk_order_buyer FOREIGN KEY (buyer_id) REFERENCES t_user(id),
    CONSTRAINT fk_order_seller FOREIGN KEY (seller_id) REFERENCES t_user(id),
    CONSTRAINT fk_order_campus FOREIGN KEY (campus_id) REFERENCES t_campus(id)
);

COMMENT ON TABLE t_order IS '订单表';
COMMENT ON COLUMN t_order.order_no IS '订单号（唯一）';
COMMENT ON COLUMN t_order.status IS '订单状态：PENDING_PAYMENT=待支付, PAID=已支付, COMPLETED=已完成, CANCELLED=已取消';

-- =====================================================
-- 12. 评价表 (t_review)
-- =====================================================
CREATE TABLE t_review (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    rating INTEGER NOT NULL,
    content VARCHAR(500) NOT NULL,
    quality_score INTEGER NOT NULL DEFAULT 5,
    service_score INTEGER NOT NULL DEFAULT 5,
    delivery_score INTEGER NOT NULL DEFAULT 5,
    has_append_review BOOLEAN NOT NULL DEFAULT FALSE,
    append_content VARCHAR(500),
    append_at TIMESTAMP,
    like_count INTEGER NOT NULL DEFAULT 0,
    reply_count INTEGER NOT NULL DEFAULT 0,
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES t_order(id),
    CONSTRAINT fk_review_buyer FOREIGN KEY (buyer_id) REFERENCES t_user(id),
    CONSTRAINT fk_review_seller FOREIGN KEY (seller_id) REFERENCES t_user(id)
);

COMMENT ON TABLE t_review IS '评价表（支持三维评分+追评）';
COMMENT ON COLUMN t_review.quality_score IS '物品质量评分（1-5星）';
COMMENT ON COLUMN t_review.service_score IS '服务态度评分（1-5星）';
COMMENT ON COLUMN t_review.delivery_score IS '物流速度评分（1-5星）';
COMMENT ON COLUMN t_review.status IS '评价状态：NORMAL=正常, HIDDEN=隐藏, REPORTED=被举报';

-- =====================================================
-- 13. 评价媒体表 (t_review_media)
-- =====================================================
CREATE TABLE t_review_media (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL,
    media_url VARCHAR(500) NOT NULL,
    media_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_review_media_review FOREIGN KEY (review_id) REFERENCES t_review(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_review_media IS '评价媒体表（图片/视频）';
COMMENT ON COLUMN t_review_media.media_type IS '媒体类型：IMAGE=图片, VIDEO=视频';

-- =====================================================
-- 14. 评价点赞表 (t_review_like)
-- =====================================================
CREATE TABLE t_review_like (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_review_like_review FOREIGN KEY (review_id) REFERENCES t_review(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_like_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_review_like UNIQUE (review_id, user_id)
);

COMMENT ON TABLE t_review_like IS '评价点赞表';

-- =====================================================
-- 15. 评价回复表 (t_review_reply)
-- =====================================================
CREATE TABLE t_review_reply (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_review_reply_review FOREIGN KEY (review_id) REFERENCES t_review(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_reply_user FOREIGN KEY (user_id) REFERENCES t_user(id)
);

COMMENT ON TABLE t_review_reply IS '评价回复表（卖家/管理员回复）';

-- =====================================================
-- 16. 收藏表 (t_favorite)
-- =====================================================
CREATE TABLE t_favorite (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    goods_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_goods FOREIGN KEY (goods_id) REFERENCES t_goods(id) ON DELETE CASCADE,
    CONSTRAINT uk_favorite UNIQUE (user_id, goods_id)
);

COMMENT ON TABLE t_favorite IS '收藏表';

-- =====================================================
-- 17. 优惠券表 (t_coupon)
-- =====================================================
CREATE TABLE t_coupon (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10, 2) NOT NULL,
    min_amount DECIMAL(10, 2),
    max_discount DECIMAL(10, 2),
    total_count INTEGER NOT NULL,
    used_count INTEGER NOT NULL DEFAULT 0,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

COMMENT ON TABLE t_coupon IS '优惠券表';
COMMENT ON COLUMN t_coupon.discount_type IS '折扣类型：PERCENTAGE=百分比, FIXED=固定金额';

-- =====================================================
-- 18. 用户-优惠券关联表 (t_coupon_user_relation)
-- =====================================================
CREATE TABLE t_coupon_user_relation (
    id BIGSERIAL PRIMARY KEY,
    coupon_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UNUSED',
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_coupon_user_coupon FOREIGN KEY (coupon_id) REFERENCES t_coupon(id),
    CONSTRAINT fk_coupon_user_user FOREIGN KEY (user_id) REFERENCES t_user(id)
);

COMMENT ON TABLE t_coupon_user_relation IS '用户-优惠券关联表';
COMMENT ON COLUMN t_coupon_user_relation.status IS '使用状态：UNUSED=未使用, USED=已使用, EXPIRED=已过期';

-- =====================================================
-- 19. 通知表 (t_notification)
-- =====================================================
CREATE TABLE t_notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_notification IS '通知表';
COMMENT ON COLUMN t_notification.type IS '通知类型：SYSTEM=系统, ORDER=订单, REVIEW=评价';

-- =====================================================
-- 20. 举报表 (t_report)
-- =====================================================
CREATE TABLE t_report (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    handler_id BIGINT,
    handle_result VARCHAR(500),
    handled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES t_user(id),
    CONSTRAINT fk_report_handler FOREIGN KEY (handler_id) REFERENCES t_user(id)
);

COMMENT ON TABLE t_report IS '举报表';
COMMENT ON COLUMN t_report.target_type IS '举报对象类型：GOODS=商品, USER=用户, REVIEW=评价';
COMMENT ON COLUMN t_report.status IS '处理状态：PENDING=待处理, APPROVED=已通过, REJECTED=已拒绝';

-- =====================================================
-- 21. 审计日志表 (t_audit_log)
-- =====================================================
CREATE TABLE t_audit_log (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    action_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT,
    target_ids TEXT,
    result VARCHAR(20),
    old_value TEXT,
    new_value TEXT,
    entity_name VARCHAR(50) NOT NULL,
    entity_type VARCHAR(20) NOT NULL,
    entity_id BIGINT,
    details TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    is_reversible BOOLEAN NOT NULL DEFAULT FALSE,
    revert_deadline TIMESTAMP,
    reverted_by_log_id BIGINT,
    reverted_at TIMESTAMP,
    revert_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_audit_log_operator FOREIGN KEY (operator_id) REFERENCES t_user(id)
);

COMMENT ON TABLE t_audit_log IS '审计日志表';
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

-- =====================================================
-- 22. 积分日志表 (t_points_log)
-- =====================================================
CREATE TABLE t_points_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    points INTEGER NOT NULL,
    type VARCHAR(20) NOT NULL,
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_points_log_user FOREIGN KEY (user_id) REFERENCES t_user(id)
);

COMMENT ON TABLE t_points_log IS '积分日志表';
COMMENT ON COLUMN t_points_log.type IS '积分类型：EARN=获得, SPEND=消费, EXPIRE=过期';

-- =====================================================
-- 23. 搜索历史表 (t_search_history)
-- =====================================================
CREATE TABLE t_search_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    keyword VARCHAR(100) NOT NULL,
    result_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_search_history_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_search_history IS '搜索历史表';

-- =====================================================
-- 24. 浏览日志表 (t_view_log)
-- =====================================================
CREATE TABLE t_view_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    goods_id BIGINT NOT NULL,
    duration INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_view_log_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_view_log_goods FOREIGN KEY (goods_id) REFERENCES t_goods(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_view_log IS '浏览日志表';
COMMENT ON COLUMN t_view_log.duration IS '浏览时长（秒）';

-- =====================================================
-- 25. API性能日志表 (t_api_performance_log)
-- =====================================================
CREATE TABLE t_api_performance_log (
    id BIGSERIAL PRIMARY KEY,
    http_method VARCHAR(10) NOT NULL,
    api_path VARCHAR(200) NOT NULL,
    response_time INTEGER NOT NULL,
    status_code INTEGER NOT NULL,
    user_id BIGINT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

COMMENT ON TABLE t_api_performance_log IS 'API性能日志表';
COMMENT ON COLUMN t_api_performance_log.http_method IS 'HTTP方法 (GET/POST/PUT/DELETE)';
COMMENT ON COLUMN t_api_performance_log.api_path IS 'API路径';
COMMENT ON COLUMN t_api_performance_log.response_time IS '响应时间（毫秒）';

-- =====================================================
-- 完成！🎉
-- =====================================================
