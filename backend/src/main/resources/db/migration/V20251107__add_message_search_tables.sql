-- 消息搜索功能相关表
-- 创建时间：2025-11-07
-- 作者：BaSui 😎

-- 消息搜索历史表
CREATE TABLE message_search_history (
    id VARCHAR(36) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    dispute_id BIGINT NOT NULL,
    keyword VARCHAR(200) NOT NULL,
    result_count INT DEFAULT 0,
    filters TEXT,
    searched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_dispute (user_id, dispute_id),
    INDEX idx_searched_at (searched_at),
    INDEX idx_keyword (keyword),
    INDEX idx_user_dispute_searched (user_id, dispute_id, searched_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 消息搜索统计表
CREATE TABLE message_search_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    dispute_id BIGINT NOT NULL,
    search_date DATE NOT NULL,
    total_searches INT DEFAULT 0,
    successful_searches INT DEFAULT 0,
    popular_keywords TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_dispute_date (user_id, dispute_id, search_date),
    INDEX idx_search_date (search_date),
    INDEX idx_user_dispute (user_id, dispute_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 添加索引优化查询性能
-- 为消息表添加全文搜索索引（如果支持的话）
-- ALTER TABLE message ADD FULLTEXT(content);

-- 为消息表添加复合索引
CREATE INDEX idx_message_dispute_timestamp ON message(dispute_id, timestamp);
CREATE INDEX idx_message_sender_timestamp ON message(sender_id, timestamp);
CREATE INDEX idx_message_type_timestamp ON message(message_type, timestamp);

-- 添加外键约束
ALTER TABLE message_search_history
ADD CONSTRAINT fk_search_history_user
FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE;

ALTER TABLE message_search_history
ADD CONSTRAINT fk_search_history_dispute
FOREIGN KEY (dispute_id) REFERENCES dispute(id) ON DELETE CASCADE;

ALTER TABLE message_search_statistics
ADD CONSTRAINT fk_search_stats_user
FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE;

ALTER TABLE message_search_statistics
ADD CONSTRAINT fk_search_stats_dispute
FOREIGN KEY (dispute_id) REFERENCES dispute(id) ON DELETE CASCADE;