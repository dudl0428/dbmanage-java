-- 创建保存的查询表
CREATE TABLE IF NOT EXISTS saved_queries (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sql TEXT NOT NULL,
    connection_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (connection_id) REFERENCES database_connections(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建查询历史表
CREATE TABLE IF NOT EXISTS query_history (
    id SERIAL PRIMARY KEY,
    sql TEXT NOT NULL,
    execution_time BIGINT,
    affected_rows INTEGER,
    is_success BOOLEAN DEFAULT TRUE,
    error_message TEXT,
    favorite BOOLEAN DEFAULT FALSE,
    connection_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    executed_at TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (connection_id) REFERENCES database_connections(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
