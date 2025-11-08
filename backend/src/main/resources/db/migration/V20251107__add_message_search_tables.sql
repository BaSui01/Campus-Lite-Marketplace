-- 消息搜索功能相关表（PostgreSQL版本）
-- 创建时间：2025-11-07
-- 作者：BaSui 😎

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