-- V1__create_todos.sql
-- ============================
-- 초기 스키마 생성
-- todos 테이블 정의
-- ============================

-- 한번 바꾼 스키마는 절대 바꾸지말기 - 추가하는 형식으로 가야함
CREATE TABLE todos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(20) DEFAULT 'GENERAL',
    status VARCHAR(20) DEFAULT 'PLANNED',
    is_recurring BOOLEAN DEFAULT FALSE,
    repeat_days INT DEFAULT 0,
    parent_todo_id BIGINT,
    active_from DATETIME,
    active_until DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
       CONSTRAINT fk_todos_parent FOREIGN KEY (parent_todo_id)
       REFERENCES todos(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;