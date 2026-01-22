CREATE DATABASE IF NOT EXISTS spring_ai_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE spring_ai_demo;
-- 对话记录表
CREATE TABLE IF NOT EXISTS chat_record (
                                           id BIGINT AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
                                           user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    user_prompt TEXT NOT NULL COMMENT '用户提问内容',
    ai_response TEXT NOT NULL COMMENT 'AI回答内容',
    model VARCHAR(32) NOT NULL COMMENT '使用的模型（qwen-turbo/qwen-plus等）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话记录表';