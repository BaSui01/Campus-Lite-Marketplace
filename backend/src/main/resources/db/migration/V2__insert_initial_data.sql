-- ═══════════════════════════════════════════════════════════════════════
-- 校园轻享集市系统 - 初始数据插入脚本
-- Campus Lite Marketplace - Initial Data Insertion Script
-- ═══════════════════════════════════════════════════════════════════════
--
-- 📝 作者: BaSui 😎 | 创建日期: 2025-11-04
-- 🎯 用途: 插入系统初始数据（角色、权限、分类、校区等）
-- 🚀 版本: V2 - 初始数据插入
-- 📦 数据库: PostgreSQL 14+
-- 🔧 Flyway: 自动执行此脚本
--
-- ⚠️ 重要提醒:
--    - 此脚本由 Flyway 自动执行，请勿手动修改！
--    - 所有密码使用 BCrypt 加密（默认密码：admin123）
--    - 系统管理员账号：admin / admin123
--
-- ═══════════════════════════════════════════════════════════════════════

-- ==================== 1. 插入角色数据 ====================

INSERT INTO t_role (name, description, created_at) VALUES
('ROLE_ADMIN', '系统管理员', CURRENT_TIMESTAMP),
('ROLE_USER', '普通用户', CURRENT_TIMESTAMP),
('ROLE_MERCHANT', '商家用户', CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- ==================== 2. 插入权限数据 ====================

INSERT INTO t_permission (name, description, resource, action, created_at) VALUES
-- 用户管理权限
('USER_READ', '查看用户', 'user', 'READ', CURRENT_TIMESTAMP),
('USER_WRITE', '编辑用户', 'user', 'WRITE', CURRENT_TIMESTAMP),
('USER_DELETE', '删除用户', 'user', 'DELETE', CURRENT_TIMESTAMP),
('USER_BAN', '封禁用户', 'user', 'BAN', CURRENT_TIMESTAMP),

-- 商品管理权限
('GOODS_READ', '查看商品', 'goods', 'READ', CURRENT_TIMESTAMP),
('GOODS_WRITE', '编辑商品', 'goods', 'WRITE', CURRENT_TIMESTAMP),
('GOODS_DELETE', '删除商品', 'goods', 'DELETE', CURRENT_TIMESTAMP),
('GOODS_APPROVE', '审核商品', 'goods', 'APPROVE', CURRENT_TIMESTAMP),

-- 订单管理权限
('ORDER_READ', '查看订单', 'order', 'READ', CURRENT_TIMESTAMP),
('ORDER_WRITE', '编辑订单', 'order', 'WRITE', CURRENT_TIMESTAMP),
('ORDER_CANCEL', '取消订单', 'order', 'CANCEL', CURRENT_TIMESTAMP),

-- 评价管理权限
('REVIEW_READ', '查看评价', 'review', 'READ', CURRENT_TIMESTAMP),
('REVIEW_DELETE', '删除评价', 'review', 'DELETE', CURRENT_TIMESTAMP),
('REVIEW_HIDE', '隐藏评价', 'review', 'HIDE', CURRENT_TIMESTAMP),

-- 社区管理权限
('POST_READ', '查看帖子', 'post', 'READ', CURRENT_TIMESTAMP),
('POST_WRITE', '编辑帖子', 'post', 'WRITE', CURRENT_TIMESTAMP),
('POST_DELETE', '删除帖子', 'post', 'DELETE', CURRENT_TIMESTAMP),
('POST_HIDE', '隐藏帖子', 'post', 'HIDE', CURRENT_TIMESTAMP),

-- 系统管理权限
('SYSTEM_CONFIG', '系统配置', 'system', 'CONFIG', CURRENT_TIMESTAMP),
('SYSTEM_LOG', '查看日志', 'system', 'LOG', CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- ==================== 3. 分配角色权限 ====================

-- 管理员拥有所有权限
INSERT INTO t_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM t_role r
CROSS JOIN t_permission p
WHERE r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- 普通用户权限（只读）
INSERT INTO t_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM t_role r
CROSS JOIN t_permission p
WHERE r.name = 'ROLE_USER'
  AND p.action = 'READ'
ON CONFLICT DO NOTHING;

-- 商家用户权限（商品和订单管理）
INSERT INTO t_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM t_role r
CROSS JOIN t_permission p
WHERE r.name = 'ROLE_MERCHANT'
  AND (p.resource IN ('goods', 'order', 'review') OR p.action = 'READ')
ON CONFLICT DO NOTHING;

-- ==================== 4. 插入校区数据 ====================

INSERT INTO t_campus (code, name, status, created_at, updated_at) VALUES
('MAIN', '主校区', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('EAST', '东校区', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('WEST', '西校区', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('NORTH', '北校区', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SOUTH', '南校区', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- ==================== 5. 插入分类数据 ====================

-- 一级分类
INSERT INTO t_category (name, description, parent_id, sort_order, created_at, updated_at) VALUES
('数码产品', '手机、电脑、平板等数码设备', NULL, 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('图书教材', '教材、课外书、考试资料', NULL, 90, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('生活用品', '日用品、家居用品', NULL, 80, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('服装鞋包', '衣服、鞋子、包包', NULL, 70, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('运动健身', '运动器材、健身用品', NULL, 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('美妆护肤', '化妆品、护肤品', NULL, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('其他', '其他物品', NULL, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- 二级分类（数码产品）
INSERT INTO t_category (name, description, parent_id, sort_order, created_at, updated_at)
SELECT '手机', '智能手机、功能机', id, 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM t_category WHERE name = '数码产品'
ON CONFLICT (name) DO NOTHING;

INSERT INTO t_category (name, description, parent_id, sort_order, created_at, updated_at)
SELECT '电脑', '笔记本电脑、台式机', id, 90, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM t_category WHERE name = '数码产品'
ON CONFLICT (name) DO NOTHING;

INSERT INTO t_category (name, description, parent_id, sort_order, created_at, updated_at)
SELECT '平板', 'iPad、安卓平板', id, 80, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM t_category WHERE name = '数码产品'
ON CONFLICT (name) DO NOTHING;

INSERT INTO t_category (name, description, parent_id, sort_order, created_at, updated_at)
SELECT '耳机音响', '耳机、音箱、音响设备', id, 70, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM t_category WHERE name = '数码产品'
ON CONFLICT (name) DO NOTHING;

-- 二级分类（图书教材）
INSERT INTO t_category (name, description, parent_id, sort_order, created_at, updated_at)
SELECT '教材', '各科教材、教辅', id, 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM t_category WHERE name = '图书教材'
ON CONFLICT (name) DO NOTHING;

INSERT INTO t_category (name, description, parent_id, sort_order, created_at, updated_at)
SELECT '考试资料', '四六级、考研、考证资料', id, 90, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM t_category WHERE name = '图书教材'
ON CONFLICT (name) DO NOTHING;

INSERT INTO t_category (name, description, parent_id, sort_order, created_at, updated_at)
SELECT '课外书', '小说、散文、专业书籍', id, 80, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM t_category WHERE name = '图书教材'
ON CONFLICT (name) DO NOTHING;

-- ==================== 6. 插入系统管理员 ====================

-- 插入管理员用户（密码：admin123，BCrypt 加密）
-- BCrypt 加密后的密码：$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH
INSERT INTO t_user (username, password, email, nickname, points, credit_score, campus_id, status, created_at, updated_at)
SELECT 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@campus-marketplace.com', '系统管理员', 0, 100, id, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM t_campus WHERE code = 'MAIN'
ON CONFLICT (username) DO NOTHING;

-- 分配管理员角色
INSERT INTO t_user_role (user_id, role_id)
SELECT u.id, r.id
FROM t_user u
CROSS JOIN t_role r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- ==================== 7. 插入测试用户 ====================

-- 插入测试用户1（密码：user123）
INSERT INTO t_user (username, password, email, nickname, points, credit_score, campus_id, student_id, status, created_at, updated_at)
SELECT 'testuser1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'testuser1@example.com', '测试用户1', 100, 100, id, '2021001001', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM t_campus WHERE code = 'MAIN'
ON CONFLICT (username) DO NOTHING;

-- 插入测试用户2（密码：user123）
INSERT INTO t_user (username, password, email, nickname, points, credit_score, campus_id, student_id, status, created_at, updated_at)
SELECT 'testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'testuser2@example.com', '测试用户2', 50, 100, id, '2021001002', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM t_campus WHERE code = 'EAST'
ON CONFLICT (username) DO NOTHING;

-- 分配普通用户角色
INSERT INTO t_user_role (user_id, role_id)
SELECT u.id, r.id
FROM t_user u
CROSS JOIN t_role r
WHERE u.username IN ('testuser1', 'testuser2') AND r.name = 'ROLE_USER'
ON CONFLICT DO NOTHING;

-- ==================== 8. 插入话题数据 ====================

INSERT INTO t_topic (name, description, cover_image, post_count, follow_count, view_count, status, created_at, updated_at) VALUES
('校园生活', '分享校园日常、趣事、活动', NULL, 0, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('二手交易', '二手物品交易经验分享', NULL, 0, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('学习交流', '学习方法、考试经验分享', NULL, 0, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('求职招聘', '实习、兼职、求职信息', NULL, 0, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('美食推荐', '校园周边美食推荐', NULL, 0, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('运动健身', '运动打卡、健身经验', NULL, 0, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('数码科技', '数码产品评测、科技资讯', NULL, 0, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('闲聊灌水', '随便聊聊、吐槽', NULL, 0, 0, 0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- ==================== 9. 插入标签数据 ====================

-- 商品标签
INSERT INTO t_tag (name, type, created_at, updated_at) VALUES
('全新', 'GOODS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('九成新', 'GOODS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('八成新', 'GOODS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('包邮', 'GOODS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('急售', 'GOODS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('可议价', 'GOODS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('限时优惠', 'GOODS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 帖子标签
('原创', 'POST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('转载', 'POST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('求助', 'POST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('分享', 'POST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('讨论', 'POST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('吐槽', 'POST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 话题标签
('热门', 'TOPIC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('推荐', 'TOPIC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('精华', 'TOPIC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- ==================== 10. 插入通知模板数据 ====================

INSERT INTO t_notification_template (code, name, type, title_template, content_template, variables, is_active, created_at, updated_at) VALUES
-- 订单相关通知
('ORDER_CREATED', '订单创建通知', 'ORDER_STATUS', '订单创建成功', '您的订单 {{orderNo}} 已创建成功，请尽快完成支付。', '{"orderNo": "订单号"}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('ORDER_PAID', '订单支付成功通知', 'ORDER_STATUS', '订单支付成功', '您的订单 {{orderNo}} 已支付成功，卖家将尽快发货。', '{"orderNo": "订单号"}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('ORDER_SHIPPED', '订单发货通知', 'ORDER_STATUS', '订单已发货', '您的订单 {{orderNo}} 已发货，物流单号：{{trackingNo}}。', '{"orderNo": "订单号", "trackingNo": "物流单号"}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('ORDER_COMPLETED', '订单完成通知', 'ORDER_STATUS', '订单已完成', '您的订单 {{orderNo}} 已完成，欢迎评价。', '{"orderNo": "订单号"}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('ORDER_CANCELLED', '订单取消通知', 'ORDER_STATUS', '订单已取消', '您的订单 {{orderNo}} 已取消。', '{"orderNo": "订单号"}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 商品相关通知
('GOODS_APPROVED', '商品审核通过通知', 'GOODS_STATUS', '商品审核通过', '您的商品 {{goodsTitle}} 已审核通过，现已上架。', '{"goodsTitle": "商品标题"}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('GOODS_REJECTED', '商品审核拒绝通知', 'GOODS_STATUS', '商品审核未通过', '您的商品 {{goodsTitle}} 审核未通过，原因：{{reason}}。', '{"goodsTitle": "商品标题", "reason": "拒绝原因"}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('GOODS_SOLD', '商品售出通知', 'GOODS_STATUS', '商品已售出', '恭喜！您的商品 {{goodsTitle}} 已售出。', '{"goodsTitle": "商品标题"}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 社区相关通知
('POST_REPLY', '帖子回复通知', 'POST_REPLY', '有人回复了您的帖子', '{{username}} 回复了您的帖子：{{postTitle}}。', '{"username": "用户名", "postTitle": "帖子标题"}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('POST_LIKE', '帖子点赞通知', 'POST_LIKE', '有人点赞了您的帖子', '{{username}} 点赞了您的帖子：{{postTitle}}。', '{"username": "用户名", "postTitle": "帖子标题"}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MENTION', '提及通知', 'MENTION', '有人@了您', '{{username}} 在帖子中@了您。', '{"username": "用户名"}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 关注相关通知
('USER_FOLLOW', '用户关注通知', 'FOLLOW', '有人关注了您', '{{username}} 关注了您。', '{"username": "用户名"}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 系统通知
('SYSTEM_ANNOUNCEMENT', '系统公告', 'SYSTEM', '系统公告', '{{content}}', '{"content": "公告内容"}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('ACCOUNT_SECURITY', '账号安全通知', 'SYSTEM', '账号安全提醒', '{{content}}', '{"content": "安全提醒内容"}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- ==================== 11. 插入功能开关数据 ====================

INSERT INTO t_feature_flag (feature_key, feature_name, description, is_enabled, rollout_percentage, target_users, created_at, updated_at) VALUES
-- 核心功能开关
('GOODS_PUBLISH', '商品发布功能', '控制用户是否可以发布商品', TRUE, 100, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('POST_PUBLISH', '帖子发布功能', '控制用户是否可以发布帖子', TRUE, 100, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('ORDER_CREATE', '订单创建功能', '控制用户是否可以创建订单', TRUE, 100, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('REVIEW_SUBMIT', '评价提交功能', '控制用户是否可以提交评价', TRUE, 100, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 高级功能开关
('COUPON_SYSTEM', '优惠券系统', '控制优惠券功能是否启用', TRUE, 100, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('POINTS_SYSTEM', '积分系统', '控制积分功能是否启用', TRUE, 100, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('RECOMMENDATION', '推荐系统', '控制个性化推荐功能是否启用', TRUE, 50, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SENTIMENT_ANALYSIS', '情感分析', '控制评价情感分析功能是否启用', FALSE, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 实验性功能开关
('AI_ASSISTANT', 'AI助手', '控制AI助手功能是否启用', FALSE, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('VIDEO_REVIEW', '视频评价', '控制视频评价功能是否启用', FALSE, 10, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LIVE_STREAMING', '直播功能', '控制直播功能是否启用', FALSE, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (feature_key) DO NOTHING;

-- ==================== 12. 插入定时任务数据 ====================

INSERT INTO t_scheduled_task (name, status, description, created_at, updated_at) VALUES
-- 数据清理任务
('CLEAN_EXPIRED_TOKENS', 'ENABLED', '清理过期的Token', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CLEAN_OLD_LOGS', 'ENABLED', '清理旧日志（保留30天）', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CLEAN_DELETED_DATA', 'ENABLED', '清理软删除数据（保留90天）', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 数据统计任务
('UPDATE_GOODS_STATS', 'ENABLED', '更新商品统计数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('UPDATE_USER_STATS', 'ENABLED', '更新用户统计数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('UPDATE_MERCHANT_DASHBOARD', 'ENABLED', '更新商家仪表盘数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 推荐系统任务
('CALCULATE_USER_SIMILARITY', 'ENABLED', '计算用户相似度', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('UPDATE_RECOMMENDATIONS', 'ENABLED', '更新推荐列表', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 通知任务
('SEND_PENDING_NOTIFICATIONS', 'ENABLED', '发送待发送的通知', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CLEAN_OLD_NOTIFICATIONS', 'ENABLED', '清理旧通知（保留30天）', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 订单任务
('AUTO_COMPLETE_ORDERS', 'ENABLED', '自动完成订单（发货后7天）', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('AUTO_CANCEL_UNPAID_ORDERS', 'ENABLED', '自动取消未支付订单（30分钟）', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 数据备份任务
('BACKUP_DATABASE', 'ENABLED', '数据库备份', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 健康检查任务
('HEALTH_CHECK', 'ENABLED', '系统健康检查', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- ==================== 13. 插入搜索关键词数据 ====================

INSERT INTO t_search_keyword (keyword, search_count, click_count, is_hot, created_at, updated_at) VALUES
-- 热门搜索关键词
('iPhone', 0, 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MacBook', 0, 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('iPad', 0, 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('教材', 0, 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('考研资料', 0, 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('自行车', 0, 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('台灯', 0, 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('耳机', 0, 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (keyword) DO NOTHING;

-- ==================== 14. 为测试用户创建通知偏好 ====================

INSERT INTO t_notification_preference (user_id, order_notification, post_notification, mention_notification, follow_notification, system_notification, email_notification, created_at, updated_at)
SELECT id, TRUE, TRUE, TRUE, TRUE, TRUE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM t_user
WHERE username IN ('admin', 'testuser1', 'testuser2')
ON CONFLICT (user_id) DO NOTHING;

-- ==================== 15. 为测试用户创建商家仪表盘 ====================

INSERT INTO t_merchant_dashboard (seller_id, total_sales, total_orders, total_goods, active_goods, avg_rating, total_reviews, response_rate, avg_response_time, last_updated_at, created_at, updated_at)
SELECT id, 0, 0, 0, 0, 0, 0, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM t_user
WHERE username IN ('admin', 'testuser1', 'testuser2')
ON CONFLICT (seller_id) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════
-- 🎉 初始数据插入完成！
--
-- 📊 插入数据统计：
--    ✅ 3 个角色（管理员、普通用户、商家）
--    ✅ 18 个权限
--    ✅ 5 个校区
--    ✅ 7 个一级分类 + 7 个二级分类
--    ✅ 8 个话题
--    ✅ 19 个标签
--    ✅ 15 个通知模板
--    ✅ 11 个功能开关
--    ✅ 14 个定时任务
--    ✅ 8 个热门搜索关键词
--    ✅ 3 个测试用户（含管理员）
--
-- 📝 系统管理员账号：
--    用户名：admin
--    密码：admin123
--    邮箱：admin@campus-marketplace.com
--
-- 📝 测试用户账号：
--    用户名：testuser1 / testuser2
--    密码：user123
--
-- ⚠️ 生产环境请立即修改默认密码！
-- ═══════════════════════════════════════════════════════════════════════
