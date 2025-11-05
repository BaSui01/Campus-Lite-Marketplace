-- =====================================================
-- Flyway 数据库迁移脚本 V5 - 新增表索引优化
-- =====================================================
-- 作者: BaSui 😎
-- 日期: 2025-11-05
-- 描述: 为V4脚本创建的46个新表添加性能优化索引
-- 数据库: PostgreSQL 14+
-- =====================================================

-- =====================================================
-- 聊天模块索引
-- =====================================================

-- 会话表索引
CREATE INDEX idx_conversation_user1 ON t_conversation(user1_id);
CREATE INDEX idx_conversation_user2 ON t_conversation(user2_id);
CREATE INDEX idx_conversation_last_time ON t_conversation(last_message_time DESC);

-- 消息表索引
CREATE INDEX idx_message_conversation ON t_message(conversation_id);
CREATE INDEX idx_message_sender ON t_message(sender_id);
CREATE INDEX idx_message_receiver ON t_message(receiver_id);
CREATE INDEX idx_message_status ON t_message(status);
CREATE INDEX idx_message_created_at ON t_message(created_at DESC);

-- 复合索引：接收者未读消息查询
CREATE INDEX idx_message_receiver_unread ON t_message(receiver_id, status) WHERE status = 'UNREAD';

-- =====================================================
-- 论坛模块索引
-- =====================================================

-- 帖子表索引
CREATE INDEX idx_post_author ON t_post(author_id);
CREATE INDEX idx_post_status ON t_post(status);
CREATE INDEX idx_post_created_at ON t_post(created_at DESC);
CREATE INDEX idx_post_campus ON t_post(campus_id);
CREATE INDEX idx_post_view_count ON t_post(view_count DESC);
CREATE INDEX idx_post_reply_count ON t_post(reply_count DESC);
CREATE INDEX idx_post_deleted ON t_post(deleted) WHERE deleted = false;

-- 复合索引：按状态+创建时间查询
CREATE INDEX idx_post_status_created ON t_post(status, created_at DESC);

-- 帖子点赞表索引
CREATE INDEX idx_post_like_post ON t_post_like(post_id);
CREATE INDEX idx_post_like_user ON t_post_like(user_id);
CREATE INDEX idx_post_like_deleted ON t_post_like(deleted) WHERE deleted = false;

-- 帖子收藏表索引
CREATE INDEX idx_post_collect_post ON t_post_collect(post_id);
CREATE INDEX idx_post_collect_user ON t_post_collect(user_id);
CREATE INDEX idx_post_collect_created_at ON t_post_collect(created_at DESC);
CREATE INDEX idx_post_collect_deleted ON t_post_collect(deleted) WHERE deleted = false;

-- 帖子回复表索引
CREATE INDEX idx_reply_post ON t_reply(post_id);
CREATE INDEX idx_reply_author ON t_reply(author_id);
CREATE INDEX idx_reply_parent ON t_reply(parent_id);
CREATE INDEX idx_reply_created_at ON t_reply(created_at DESC);
CREATE INDEX idx_reply_deleted ON t_reply(deleted) WHERE deleted = false;

-- 话题表索引
CREATE INDEX idx_topic_name ON t_topic(name);
CREATE INDEX idx_topic_hotness ON t_topic(hotness DESC);
CREATE INDEX idx_topic_post_count ON t_topic(post_count DESC);
CREATE INDEX idx_topic_deleted ON t_topic(deleted) WHERE deleted = false;

-- 话题关注表索引
CREATE INDEX idx_topic_follow_topic ON t_topic_follow(topic_id);
CREATE INDEX idx_topic_follow_user ON t_topic_follow(user_id);
CREATE INDEX idx_topic_follow_deleted ON t_topic_follow(deleted) WHERE deleted = false;

-- 话题标签关联表索引
CREATE INDEX idx_topic_tag_post ON t_topic_tag(post_id);
CREATE INDEX idx_topic_tag_topic ON t_topic_tag(topic_id);
CREATE INDEX idx_topic_tag_deleted ON t_topic_tag(deleted) WHERE deleted = false;

-- =====================================================
-- 订单相关索引
-- =====================================================

-- 物流表索引
CREATE INDEX idx_logistics_order ON t_logistics(order_id);
CREATE INDEX idx_logistics_tracking ON t_logistics(tracking_number);
CREATE INDEX idx_logistics_company ON t_logistics(logistics_company);
CREATE INDEX idx_logistics_status ON t_logistics(status);
CREATE INDEX idx_logistics_sync_time ON t_logistics(last_sync_time DESC);
CREATE INDEX idx_logistics_deleted ON t_logistics(deleted) WHERE deleted = false;

-- 复合索引：按状态+同步时间查询
CREATE INDEX idx_logistics_status_sync ON t_logistics(status, last_sync_time DESC);

-- JSONB索引：物流轨迹查询优化
CREATE INDEX idx_logistics_track_records ON t_logistics USING GIN(track_records);

-- 支付日志表索引
CREATE INDEX idx_payment_log_order_no ON t_payment_log(order_no);
CREATE INDEX idx_payment_log_trade_no ON t_payment_log(trade_no);
CREATE INDEX idx_payment_log_channel ON t_payment_log(channel);
CREATE INDEX idx_payment_log_type ON t_payment_log(type);
CREATE INDEX idx_payment_log_success ON t_payment_log(success);
CREATE INDEX idx_payment_log_created_at ON t_payment_log(created_at DESC);

-- 退款申请表索引
CREATE INDEX idx_refund_request_order_no ON t_refund_request(order_no);
CREATE INDEX idx_refund_request_applicant ON t_refund_request(applicant_id);
CREATE INDEX idx_refund_request_status ON t_refund_request(status);
CREATE INDEX idx_refund_request_created_at ON t_refund_request(created_at DESC);
CREATE INDEX idx_refund_request_deleted ON t_refund_request(deleted) WHERE deleted = false;

-- 撤销申请表索引
CREATE INDEX idx_revert_request_refund_no ON t_revert_request(refund_no);
CREATE INDEX idx_revert_request_applicant ON t_revert_request(applicant_id);
CREATE INDEX idx_revert_request_status ON t_revert_request(status);
CREATE INDEX idx_revert_request_handler ON t_revert_request(handler_id);
CREATE INDEX idx_revert_request_created_at ON t_revert_request(created_at DESC);
CREATE INDEX idx_revert_request_deleted ON t_revert_request(deleted) WHERE deleted = false;

-- 纠纷表索引
CREATE INDEX idx_dispute_order ON t_dispute(order_id);
CREATE INDEX idx_dispute_initiator ON t_dispute(initiator_id);
CREATE INDEX idx_dispute_respondent ON t_dispute(respondent_id);
CREATE INDEX idx_dispute_type ON t_dispute(dispute_type);
CREATE INDEX idx_dispute_status ON t_dispute(status);
CREATE INDEX idx_dispute_arbitrator ON t_dispute(arbitrator_id);
CREATE INDEX idx_dispute_created_at ON t_dispute(created_at DESC);
CREATE INDEX idx_dispute_deleted ON t_dispute(deleted) WHERE deleted = false;

-- 复合索引：待处理纠纷查询
CREATE INDEX idx_dispute_pending ON t_dispute(status, created_at DESC) WHERE status IN ('SUBMITTED', 'NEGOTIATING', 'ARBITRATING');

-- 纠纷协商表索引
CREATE INDEX idx_negotiation_dispute ON t_dispute_negotiation(dispute_id);
CREATE INDEX idx_negotiation_sender ON t_dispute_negotiation(sender_id);
CREATE INDEX idx_negotiation_type ON t_dispute_negotiation(message_type);
CREATE INDEX idx_negotiation_status ON t_dispute_negotiation(proposal_status);
CREATE INDEX idx_negotiation_created_at ON t_dispute_negotiation(created_at DESC);
CREATE INDEX idx_negotiation_deleted ON t_dispute_negotiation(deleted) WHERE deleted = false;

-- 纠纷仲裁表索引
CREATE INDEX idx_arbitration_arbitrator ON t_dispute_arbitration(arbitrator_id);
CREATE INDEX idx_arbitration_result ON t_dispute_arbitration(result);
CREATE INDEX idx_arbitration_executed ON t_dispute_arbitration(executed);
CREATE INDEX idx_arbitration_time ON t_dispute_arbitration(arbitrated_at DESC);
CREATE INDEX idx_arbitration_deleted ON t_dispute_arbitration(deleted) WHERE deleted = false;

-- 纠纷证据表索引
CREATE INDEX idx_evidence_dispute ON t_dispute_evidence(dispute_id);
CREATE INDEX idx_evidence_uploader ON t_dispute_evidence(uploader_id);
CREATE INDEX idx_evidence_type ON t_dispute_evidence(evidence_type);
CREATE INDEX idx_evidence_validity ON t_dispute_evidence(validity);
CREATE INDEX idx_evidence_created_at ON t_dispute_evidence(created_at DESC);
CREATE INDEX idx_evidence_deleted ON t_dispute_evidence(deleted) WHERE deleted = false;

-- =====================================================
-- 评价扩展索引
-- =====================================================

-- 评价标签表索引
CREATE INDEX idx_review_tag_review ON t_review_tag(review_id);
CREATE INDEX idx_review_tag_type ON t_review_tag(tag_type);
CREATE INDEX idx_review_tag_source ON t_review_tag(tag_source);
CREATE INDEX idx_review_tag_weight ON t_review_tag(weight DESC);
CREATE INDEX idx_review_tag_deleted ON t_review_tag(deleted) WHERE deleted = false;

-- 评价情感分析表索引
CREATE INDEX idx_sentiment_type ON t_review_sentiment(sentiment_type);
CREATE INDEX idx_sentiment_score ON t_review_sentiment(sentiment_score DESC);
CREATE INDEX idx_sentiment_deleted ON t_review_sentiment(deleted) WHERE deleted = false;

-- =====================================================
-- 用户关系索引
-- =====================================================

-- 用户关注表索引
CREATE INDEX idx_user_follow_follower ON t_user_follow(follower_id);
CREATE INDEX idx_user_follow_following ON t_user_follow(following_id);
CREATE INDEX idx_user_follow_created_at ON t_user_follow(created_at DESC);
CREATE INDEX idx_user_follow_deleted ON t_user_follow(deleted) WHERE deleted = false;

-- =====================================================
-- 通知扩展索引
-- =====================================================

-- 通知模板表索引（继承BaseEntity）
CREATE INDEX idx_notification_template_code ON t_notification_template(code);
CREATE INDEX idx_notification_template_deleted ON t_notification_template(deleted) WHERE deleted = false;

-- 通知偏好设置表索引
CREATE INDEX idx_preference_user ON t_notification_preference(user_id);
CREATE INDEX idx_preference_channel ON t_notification_preference(channel);
CREATE INDEX idx_preference_enabled ON t_notification_preference(enabled);

-- 通知退订表索引（不继承BaseEntity）
CREATE INDEX idx_unsubscribe_user ON t_notification_unsubscribe(user_id);
CREATE INDEX idx_unsubscribe_template ON t_notification_unsubscribe(template_code);
CREATE INDEX idx_unsubscribe_channel ON t_notification_unsubscribe(channel);

-- =====================================================
-- 搜索扩展索引
-- =====================================================

-- 搜索日志表索引
CREATE INDEX idx_search_log_user ON t_search_log(user_id);
CREATE INDEX idx_search_log_keyword ON t_search_log(keyword);
CREATE INDEX idx_search_log_created_at ON t_search_log(created_at DESC);
CREATE INDEX idx_search_log_deleted ON t_search_log(deleted) WHERE deleted = false;

-- 复合索引：用户搜索历史
CREATE INDEX idx_search_log_user_created ON t_search_log(user_id, created_at DESC);

-- 热门搜索关键词表索引
CREATE INDEX idx_search_keyword_hotness ON t_search_keyword(hotness DESC);
CREATE INDEX idx_search_keyword_count ON t_search_keyword(search_count DESC);
CREATE INDEX idx_search_keyword_deleted ON t_search_keyword(deleted) WHERE deleted = false;

-- =====================================================
-- 推荐系统索引
-- =====================================================

-- 用户行为日志表索引
CREATE INDEX idx_behavior_user ON t_user_behavior_log(user_id);
CREATE INDEX idx_behavior_type ON t_user_behavior_log(behavior_type);
CREATE INDEX idx_behavior_goods ON t_user_behavior_log(goods_id);
CREATE INDEX idx_behavior_post ON t_user_behavior_log(post_id);
CREATE INDEX idx_behavior_created_at ON t_user_behavior_log(created_at DESC);
CREATE INDEX idx_behavior_deleted ON t_user_behavior_log(deleted) WHERE deleted = false;

-- 复合索引：用户行为分析
CREATE INDEX idx_behavior_user_type ON t_user_behavior_log(user_id, behavior_type, created_at DESC);

-- 用户Feed流表索引
CREATE INDEX idx_feed_user ON t_user_feed(user_id);
CREATE INDEX idx_feed_item_type ON t_user_feed(item_type);
CREATE INDEX idx_feed_score ON t_user_feed(score DESC);
CREATE INDEX idx_feed_created_at ON t_user_feed(created_at DESC);
CREATE INDEX idx_feed_deleted ON t_user_feed(deleted) WHERE deleted = false;

-- 复合索引：用户Feed流查询
CREATE INDEX idx_feed_user_score ON t_user_feed(user_id, score DESC, created_at DESC);

-- 用户画像表索引
CREATE INDEX idx_persona_campus ON t_user_persona(campus_id);
CREATE INDEX idx_persona_age_group ON t_user_persona(age_group);
CREATE INDEX idx_persona_gender ON t_user_persona(gender);
CREATE INDEX idx_persona_deleted ON t_user_persona(deleted) WHERE deleted = false;

-- JSONB索引：兴趣标签和偏好分类查询优化
CREATE INDEX idx_persona_interests ON t_user_persona USING GIN(interests);
CREATE INDEX idx_persona_categories ON t_user_persona USING GIN(favorite_categories);
CREATE INDEX idx_persona_time_slots ON t_user_persona USING GIN(active_time_slots);

-- 用户相似度表索引
CREATE INDEX idx_similarity_user ON t_user_similarity(user_id);
CREATE INDEX idx_similarity_similar_user ON t_user_similarity(similar_user_id);
CREATE INDEX idx_similarity_score ON t_user_similarity(similarity_score DESC);
CREATE INDEX idx_similarity_deleted ON t_user_similarity(deleted) WHERE deleted = false;

-- 复合索引：相似用户推荐
CREATE INDEX idx_similarity_user_score ON t_user_similarity(user_id, similarity_score DESC);

-- =====================================================
-- 系统管理索引
-- =====================================================

-- 封禁日志表索引
CREATE INDEX idx_ban_log_user ON t_ban_log(user_id);
CREATE INDEX idx_ban_log_type ON t_ban_log(ban_type);
CREATE INDEX idx_ban_log_operator ON t_ban_log(operator_id);
CREATE INDEX idx_ban_log_active ON t_ban_log(is_active);
CREATE INDEX idx_ban_log_time ON t_ban_log(start_time, end_time);
CREATE INDEX idx_ban_log_deleted ON t_ban_log(deleted) WHERE deleted = false;

-- 黑名单表索引（不继承BaseEntity）
CREATE INDEX idx_blacklist_user ON t_blacklist(user_id);
CREATE INDEX idx_blacklist_blocked_user ON t_blacklist(blocked_user_id);
CREATE INDEX idx_blacklist_created_at ON t_blacklist(created_at DESC);

-- 合规审计日志表索引
CREATE INDEX idx_compliance_user ON t_compliance_audit_log(user_id);
CREATE INDEX idx_compliance_action ON t_compliance_audit_log(action);
CREATE INDEX idx_compliance_resource ON t_compliance_audit_log(resource_type, resource_id);
CREATE INDEX idx_compliance_risk ON t_compliance_audit_log(risk_level);
CREATE INDEX idx_compliance_created_at ON t_compliance_audit_log(created_at DESC);
CREATE INDEX idx_compliance_deleted ON t_compliance_audit_log(deleted) WHERE deleted = false;

-- 复合索引：高风险操作查询
CREATE INDEX idx_compliance_high_risk ON t_compliance_audit_log(risk_level, created_at DESC) WHERE risk_level IN ('HIGH', 'CRITICAL');

-- 合规白名单表索引（不继承BaseEntity）
CREATE INDEX idx_whitelist_type ON t_compliance_whitelist(type);
CREATE INDEX idx_whitelist_target ON t_compliance_whitelist(target_id);

-- 申诉表索引
CREATE INDEX idx_appeal_user ON t_appeal(user_id);
CREATE INDEX idx_appeal_target ON t_appeal(target_type, target_id);
CREATE INDEX idx_appeal_status ON t_appeal(status);
CREATE INDEX idx_appeal_reviewer ON t_appeal(reviewer_id);
CREATE INDEX idx_appeal_created_at ON t_appeal(created_at DESC);
CREATE INDEX idx_appeal_deleted ON t_appeal(deleted) WHERE deleted = false;

-- 复合索引：待处理申诉查询
CREATE INDEX idx_appeal_pending ON t_appeal(status, created_at DESC) WHERE status = 'PENDING';

-- 申诉材料表索引
CREATE INDEX idx_material_appeal ON t_appeal_material(appeal_id);
CREATE INDEX idx_material_file_type ON t_appeal_material(file_type);
CREATE INDEX idx_material_deleted ON t_appeal_material(deleted) WHERE deleted = false;

-- 错误日志表索引
CREATE INDEX idx_error_log_type ON t_error_log(error_type);
CREATE INDEX idx_error_log_severity ON t_error_log(severity);
CREATE INDEX idx_error_log_user ON t_error_log(user_id);
CREATE INDEX idx_error_log_url ON t_error_log(request_url);
CREATE INDEX idx_error_log_resolved ON t_error_log(resolved);
CREATE INDEX idx_error_log_created_at ON t_error_log(created_at DESC);

-- 复合索引：未解决错误查询
CREATE INDEX idx_error_log_unresolved ON t_error_log(resolved, severity, created_at DESC) WHERE resolved = false;

-- 健康检查记录表索引
CREATE INDEX idx_health_check_service ON t_health_check_record(service_name);
CREATE INDEX idx_health_check_type ON t_health_check_record(check_type);
CREATE INDEX idx_health_check_status ON t_health_check_record(status);
CREATE INDEX idx_health_check_time ON t_health_check_record(checked_at DESC);
CREATE INDEX idx_health_check_response ON t_health_check_record(response_time DESC);

-- 复合索引：服务健康状态查询
CREATE INDEX idx_health_check_service_status ON t_health_check_record(service_name, status, checked_at DESC);

-- 功能开关表索引（继承BaseEntity）
CREATE INDEX idx_feature_flag_key ON t_feature_flag(feature_key);
CREATE INDEX idx_feature_flag_enabled ON t_feature_flag(enabled);
CREATE INDEX idx_feature_flag_updated ON t_feature_flag(updated_at DESC);
CREATE INDEX idx_feature_flag_deleted ON t_feature_flag(deleted) WHERE deleted = false;

-- 数据备份表索引
CREATE INDEX idx_backup_type ON t_data_backup(backup_type);
CREATE INDEX idx_backup_table ON t_data_backup(table_name);
CREATE INDEX idx_backup_status ON t_data_backup(status);
CREATE INDEX idx_backup_started ON t_data_backup(started_at DESC);
CREATE INDEX idx_backup_deleted ON t_data_backup(deleted) WHERE deleted = false;

-- 隐私请求表索引
CREATE INDEX idx_privacy_user ON t_privacy_request(user_id);
CREATE INDEX idx_privacy_type ON t_privacy_request(request_type);
CREATE INDEX idx_privacy_status ON t_privacy_request(status);
CREATE INDEX idx_privacy_handler ON t_privacy_request(handler_id);
CREATE INDEX idx_privacy_executed ON t_privacy_request(executed);
CREATE INDEX idx_privacy_scheduled_at ON t_privacy_request(scheduled_at);
CREATE INDEX idx_privacy_created_at ON t_privacy_request(created_at DESC);
CREATE INDEX idx_privacy_deleted ON t_privacy_request(deleted) WHERE deleted = false;

-- =====================================================
-- 任务管理索引
-- =====================================================

-- 批量任务表索引
CREATE INDEX idx_batch_task_type ON t_batch_task(task_type);
CREATE INDEX idx_batch_task_status ON t_batch_task(status);
CREATE INDEX idx_batch_task_creator ON t_batch_task(created_by);
CREATE INDEX idx_batch_task_created_at ON t_batch_task(created_at DESC);
CREATE INDEX idx_batch_task_deleted ON t_batch_task(deleted) WHERE deleted = false;

-- 批量任务明细表索引
CREATE INDEX idx_batch_item_task ON t_batch_task_item(task_id);
CREATE INDEX idx_batch_item_status ON t_batch_task_item(status);
CREATE INDEX idx_batch_item_deleted ON t_batch_task_item(deleted) WHERE deleted = false;

-- JSONB索引：任务数据查询优化
CREATE INDEX idx_batch_item_data ON t_batch_task_item USING GIN(item_data);

-- 定时任务表索引（继承BaseEntity）
CREATE INDEX idx_scheduled_task_name ON t_scheduled_task(name);
CREATE INDEX idx_scheduled_task_status ON t_scheduled_task(status);
CREATE INDEX idx_scheduled_task_deleted ON t_scheduled_task(deleted) WHERE deleted = false;

-- 任务执行记录表索引（不继承BaseEntity）
CREATE INDEX idx_task_exec_name ON t_task_execution(task_name);
CREATE INDEX idx_task_exec_status ON t_task_execution(status);
CREATE INDEX idx_task_exec_started ON t_task_execution(started_at DESC);
CREATE INDEX idx_task_exec_node ON t_task_execution(node);

-- 复合索引：任务执行历史查询
CREATE INDEX idx_task_exec_name_status ON t_task_execution(task_name, status, started_at DESC);

-- =====================================================
-- 运营工具索引
-- =====================================================

-- 营销活动表索引
CREATE INDEX idx_campaign_type ON t_marketing_campaign(campaign_type);
CREATE INDEX idx_campaign_status ON t_marketing_campaign(status);
CREATE INDEX idx_campaign_time ON t_marketing_campaign(start_time, end_time);
CREATE INDEX idx_campaign_creator ON t_marketing_campaign(created_by);
CREATE INDEX idx_campaign_created_at ON t_marketing_campaign(created_at DESC);
CREATE INDEX idx_campaign_deleted ON t_marketing_campaign(deleted) WHERE deleted = false;

-- JSONB索引：目标用户查询优化
CREATE INDEX idx_campaign_targets ON t_marketing_campaign USING GIN(target_users);

-- 复合索引：进行中的活动查询
CREATE INDEX idx_campaign_active ON t_marketing_campaign(status, start_time, end_time) WHERE status = 'ACTIVE';

-- 商家看板表索引
CREATE INDEX idx_merchant_total_revenue ON t_merchant_dashboard(total_revenue DESC);
CREATE INDEX idx_merchant_avg_rating ON t_merchant_dashboard(avg_rating DESC);
CREATE INDEX idx_merchant_follower_count ON t_merchant_dashboard(follower_count DESC);
CREATE INDEX idx_merchant_deleted ON t_merchant_dashboard(deleted) WHERE deleted = false;

-- 导出任务表索引
CREATE INDEX idx_export_type ON t_export_job(export_type);
CREATE INDEX idx_export_status ON t_export_job(status);
CREATE INDEX idx_export_creator ON t_export_job(created_by);
CREATE INDEX idx_export_created_at ON t_export_job(created_at DESC);
CREATE INDEX idx_export_deleted ON t_export_job(deleted) WHERE deleted = false;

-- JSONB索引：查询参数优化
CREATE INDEX idx_export_query_params ON t_export_job USING GIN(query_params);

-- 订阅表索引
CREATE INDEX idx_subscription_user ON t_subscription(user_id);
CREATE INDEX idx_subscription_type ON t_subscription(subscription_type);
CREATE INDEX idx_subscription_target ON t_subscription(target_type, target_id);
CREATE INDEX idx_subscription_active ON t_subscription(is_active);
CREATE INDEX idx_subscription_deleted ON t_subscription(deleted) WHERE deleted = false;

-- 复合索引：用户订阅查询
CREATE INDEX idx_subscription_user_type ON t_subscription(user_id, subscription_type, is_active);

-- =====================================================
-- 完成！🎉 所有新增表的索引已创建完毕！
-- =====================================================
