-- =====================================================
-- Flyway 数据库迁移脚本 V2 - 索引优化
-- =====================================================
-- 作者: BaSui 😎
-- 日期: 2025-11-05
-- 描述: 创建所有表的性能优化索引（查询加速、外键索引）
-- 数据库: PostgreSQL 14+
-- =====================================================

-- =====================================================
-- 1. 校区表索引
-- =====================================================
CREATE INDEX idx_campus_code ON t_campus(code);
CREATE INDEX idx_campus_status ON t_campus(status);
CREATE INDEX idx_campus_deleted ON t_campus(deleted) WHERE deleted = false;

-- =====================================================
-- 2. 用户表索引
-- =====================================================
CREATE INDEX idx_user_username ON t_user(username);
CREATE INDEX idx_user_email ON t_user(email);
CREATE INDEX idx_user_phone ON t_user(phone);
CREATE INDEX idx_user_student_id ON t_user(student_id);
CREATE INDEX idx_user_campus ON t_user(campus_id);
CREATE INDEX idx_user_status ON t_user(status);
CREATE INDEX idx_user_created_at ON t_user(created_at);

-- =====================================================
-- 3. 角色表索引
-- =====================================================
CREATE INDEX idx_role_name ON t_role(name);

-- =====================================================
-- 4. 权限表索引
-- =====================================================
CREATE INDEX idx_permission_name ON t_permission(name);

-- =====================================================
-- 5. 用户-角色关联表索引
-- =====================================================
CREATE INDEX idx_user_role_user ON t_user_role(user_id);
CREATE INDEX idx_user_role_role ON t_user_role(role_id);

-- =====================================================
-- 6. 角色-权限关联表索引
-- =====================================================
CREATE INDEX idx_role_permission_role ON t_role_permission(role_id);
CREATE INDEX idx_role_permission_permission ON t_role_permission(permission_id);

-- =====================================================
-- 7. 分类表索引
-- =====================================================
CREATE INDEX idx_category_name ON t_category(name);
CREATE INDEX idx_category_parent ON t_category(parent_id);
CREATE INDEX idx_category_sort ON t_category(sort_order DESC);
CREATE INDEX idx_category_deleted ON t_category(deleted) WHERE deleted = false;

-- =====================================================
-- 8. 商品表索引
-- =====================================================
CREATE INDEX idx_goods_seller ON t_goods(seller_id);
CREATE INDEX idx_goods_category ON t_goods(category_id);
CREATE INDEX idx_goods_campus ON t_goods(campus_id);
CREATE INDEX idx_goods_status ON t_goods(status);
CREATE INDEX idx_goods_price ON t_goods(price);
CREATE INDEX idx_goods_created_at ON t_goods(created_at DESC);
CREATE INDEX idx_goods_view_count ON t_goods(view_count DESC);
CREATE INDEX idx_goods_favorite_count ON t_goods(favorite_count DESC);
CREATE INDEX idx_goods_deleted ON t_goods(deleted) WHERE deleted = false;

-- 复合索引：按状态+创建时间查询（商品列表常用）
CREATE INDEX idx_goods_status_created ON t_goods(status, created_at DESC);

-- 复合索引：按分类+状态查询（分类筛选常用）
CREATE INDEX idx_goods_category_status ON t_goods(category_id, status);

-- JSONB 索引：扩展属性查询优化
CREATE INDEX idx_goods_extra_attrs ON t_goods USING GIN(extra_attrs);

-- =====================================================
-- 9. 标签表索引
-- =====================================================
CREATE INDEX idx_tag_name ON t_tag(name);
CREATE INDEX idx_tag_deleted ON t_tag(deleted) WHERE deleted = false;

-- =====================================================
-- 10. 商品-标签关联表索引
-- =====================================================
CREATE INDEX idx_goods_tag_goods ON t_goods_tag(goods_id);
CREATE INDEX idx_goods_tag_tag ON t_goods_tag(tag_id);
CREATE INDEX idx_goods_tag_deleted ON t_goods_tag(deleted) WHERE deleted = false;

-- =====================================================
-- 11. 订单表索引
-- =====================================================
CREATE INDEX idx_order_no ON t_order(order_no);
CREATE INDEX idx_order_goods ON t_order(goods_id);
CREATE INDEX idx_order_buyer ON t_order(buyer_id);
CREATE INDEX idx_order_seller ON t_order(seller_id);
CREATE INDEX idx_order_campus ON t_order(campus_id);
CREATE INDEX idx_order_status ON t_order(status);
CREATE INDEX idx_order_created_at ON t_order(created_at DESC);
CREATE INDEX idx_order_payment_time ON t_order(payment_time);

-- 复合索引：买家订单查询（我的订单常用）
CREATE INDEX idx_order_buyer_status ON t_order(buyer_id, status);

-- 复合索引：卖家订单查询（卖家中心常用）
CREATE INDEX idx_order_seller_status ON t_order(seller_id, status);

-- =====================================================
-- 12. 评价表索引
-- =====================================================
CREATE INDEX idx_review_order ON t_review(order_id);
CREATE INDEX idx_review_buyer ON t_review(buyer_id);
CREATE INDEX idx_review_seller ON t_review(seller_id);
CREATE INDEX idx_review_status ON t_review(status);
CREATE INDEX idx_review_created_at ON t_review(created_at DESC);
CREATE INDEX idx_review_like_count ON t_review(like_count DESC);

-- 复合索引：卖家评价查询（卖家信誉计算常用）
CREATE INDEX idx_review_seller_status ON t_review(seller_id, status);

-- =====================================================
-- 13. 评价媒体表索引
-- =====================================================
CREATE INDEX idx_review_media_review ON t_review_media(review_id);
CREATE INDEX idx_review_media_deleted ON t_review_media(deleted) WHERE deleted = false;

-- =====================================================
-- 14. 评价点赞表索引
-- =====================================================
CREATE INDEX idx_review_like_review ON t_review_like(review_id);
CREATE INDEX idx_review_like_user ON t_review_like(user_id);
CREATE INDEX idx_review_like_deleted ON t_review_like(deleted) WHERE deleted = false;

-- =====================================================
-- 15. 评价回复表索引
-- =====================================================
CREATE INDEX idx_review_reply_review ON t_review_reply(review_id);
CREATE INDEX idx_review_reply_user ON t_review_reply(user_id);
CREATE INDEX idx_review_reply_created_at ON t_review_reply(created_at DESC);
CREATE INDEX idx_review_reply_deleted ON t_review_reply(deleted) WHERE deleted = false;

-- =====================================================
-- 16. 收藏表索引
-- =====================================================
CREATE INDEX idx_favorite_user ON t_favorite(user_id);
CREATE INDEX idx_favorite_goods ON t_favorite(goods_id);
CREATE INDEX idx_favorite_created_at ON t_favorite(created_at DESC);
CREATE INDEX idx_favorite_deleted ON t_favorite(deleted) WHERE deleted = false;

-- =====================================================
-- 17. 优惠券表索引
-- =====================================================
CREATE INDEX idx_coupon_code ON t_coupon(code);
CREATE INDEX idx_coupon_status ON t_coupon(status);
CREATE INDEX idx_coupon_time ON t_coupon(start_time, end_time);
CREATE INDEX idx_coupon_deleted ON t_coupon(deleted) WHERE deleted = false;

-- =====================================================
-- 18. 用户-优惠券关联表索引
-- =====================================================
CREATE INDEX idx_coupon_user_coupon ON t_coupon_user_relation(coupon_id);
CREATE INDEX idx_coupon_user_user ON t_coupon_user_relation(user_id);
CREATE INDEX idx_coupon_user_status ON t_coupon_user_relation(status);
CREATE INDEX idx_coupon_user_deleted ON t_coupon_user_relation(deleted) WHERE deleted = false;

-- 复合索引：用户可用优惠券查询
CREATE INDEX idx_coupon_user_available ON t_coupon_user_relation(user_id, status);

-- =====================================================
-- 19. 通知表索引
-- =====================================================
CREATE INDEX idx_notification_user ON t_notification(user_id);
CREATE INDEX idx_notification_type ON t_notification(type);
CREATE INDEX idx_notification_read ON t_notification(is_read);
CREATE INDEX idx_notification_created_at ON t_notification(created_at DESC);
CREATE INDEX idx_notification_deleted ON t_notification(deleted) WHERE deleted = false;

-- 复合索引：用户未读通知查询
CREATE INDEX idx_notification_user_unread ON t_notification(user_id, is_read) WHERE is_read = false;

-- =====================================================
-- 20. 举报表索引
-- =====================================================
CREATE INDEX idx_report_reporter ON t_report(reporter_id);
CREATE INDEX idx_report_target ON t_report(target_type, target_id);
CREATE INDEX idx_report_status ON t_report(status);
CREATE INDEX idx_report_handler ON t_report(handler_id);
CREATE INDEX idx_report_created_at ON t_report(created_at DESC);
CREATE INDEX idx_report_deleted ON t_report(deleted) WHERE deleted = false;

-- 复合索引：待处理举报查询
CREATE INDEX idx_report_pending ON t_report(status, created_at DESC) WHERE status = 'PENDING';

-- =====================================================
-- 21. 审计日志表索引
-- =====================================================
CREATE INDEX idx_audit_operator ON t_audit_log(operator_id);
CREATE INDEX idx_audit_action ON t_audit_log(action_type);
CREATE INDEX idx_audit_target ON t_audit_log(target_type, target_id);
CREATE INDEX idx_audit_created_at ON t_audit_log(created_at);
CREATE INDEX idx_audit_entity ON t_audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_reversible ON t_audit_log(is_reversible);
CREATE INDEX idx_audit_log_deleted ON t_audit_log(deleted) WHERE deleted = false;

-- 复合索引：用户操作日志查询
CREATE INDEX idx_audit_log_operator_action ON t_audit_log(operator_id, action_type, created_at DESC);

-- =====================================================
-- 22. 积分日志表索引
-- =====================================================
CREATE INDEX idx_points_log_user ON t_points_log(user_id);
CREATE INDEX idx_points_log_type ON t_points_log(type);
CREATE INDEX idx_points_log_created_at ON t_points_log(created_at DESC);
CREATE INDEX idx_points_log_deleted ON t_points_log(deleted) WHERE deleted = false;

-- 复合索引：用户积分历史查询
CREATE INDEX idx_points_log_user_created ON t_points_log(user_id, created_at DESC);

-- =====================================================
-- 23. 搜索历史表索引
-- =====================================================
CREATE INDEX idx_search_history_user ON t_search_history(user_id);
CREATE INDEX idx_search_history_keyword ON t_search_history(keyword);
CREATE INDEX idx_search_history_created_at ON t_search_history(created_at DESC);
CREATE INDEX idx_search_history_deleted ON t_search_history(deleted) WHERE deleted = false;

-- 复合索引：用户搜索历史查询
CREATE INDEX idx_search_history_user_created ON t_search_history(user_id, created_at DESC);

-- =====================================================
-- 24. 浏览日志表索引
-- =====================================================
CREATE INDEX idx_view_log_user ON t_view_log(user_id);
CREATE INDEX idx_view_log_goods ON t_view_log(goods_id);
CREATE INDEX idx_view_log_created_at ON t_view_log(created_at DESC);
CREATE INDEX idx_view_log_deleted ON t_view_log(deleted) WHERE deleted = false;

-- 复合索引：用户浏览历史查询
CREATE INDEX idx_view_log_user_created ON t_view_log(user_id, created_at DESC);

-- 复合索引：商品浏览统计查询
CREATE INDEX idx_view_log_goods_created ON t_view_log(goods_id, created_at DESC);

-- =====================================================
-- 25. API性能日志表索引
-- =====================================================
CREATE INDEX idx_api_performance_api_path ON t_api_performance_log(api_path);
CREATE INDEX idx_api_performance_http_method ON t_api_performance_log(http_method);
CREATE INDEX idx_api_performance_response_time ON t_api_performance_log(response_time DESC);
CREATE INDEX idx_api_performance_status ON t_api_performance_log(status_code);
CREATE INDEX idx_api_performance_user ON t_api_performance_log(user_id);
CREATE INDEX idx_api_performance_created_at ON t_api_performance_log(created_at DESC);

-- 复合索引：慢查询分析
CREATE INDEX idx_api_performance_slow ON t_api_performance_log(api_path, response_time DESC) WHERE response_time > 1000;

-- =====================================================
-- 完成！🎉 所有索引创建完毕！
-- =====================================================
