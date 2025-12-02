-- V1__create_tables.sql
-- ============================
-- 초기 스키마 생성
-- notification 관련 테이블 정의
-- ============================

-- notifications 테이블
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    todo_id BIGINT NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    remain_time VARCHAR(10) NOT NULL,
    title VARCHAR(20) NOT NULL,
    body TEXT NOT NULL,
    send_at DATETIME,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- notification_channels 테이블
CREATE TABLE notification_channels (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    notification_id BIGINT,
    channel_type VARCHAR(20),
    send_status VARCHAR(20) DEFAULT 'PENDING',
    send_at TIMESTAMP NOT NULL,
    error_message VARCHAR(100),

    CONSTRAINT fk_notification FOREIGN KEY (notification_id)
       REFERENCES notifications(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- notification_templates 테이블
CREATE TABLE notification_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(20) NOT NULL,
    channel_type VARCHAR(20) NOT NULL,
    lang VARCHAR(10) NOT NULL,
    title_template VARCHAR(50),
    body_template TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;