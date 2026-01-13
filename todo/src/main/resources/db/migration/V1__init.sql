-- V1__init.sql
-- ============================
-- 초기 스키마 생성
-- recur_tasks 테이블 정의
-- todos 테이블 정의
-- ============================

-- RECUR_TASKS (반복 규칙)
CREATE TABLE recur_tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    todo_type ENUM('TODO', 'FOCUS', 'WORKOUT') NOT NULL,
    is_all_day TINYINT(1) NOT NULL DEFAULT 0,

    repeat_type ENUM('DAILY', 'WEEKLY', 'MONTHLY') NOT NULL,
    repeat_interval SMALLINT UNSIGNED NOT NULL DEFAULT 1,
    weekly_mask TINYINT UNSIGNED NULL,
    monthly_day TINYINT UNSIGNED NULL,
    at_time TIME NULL,

    reminder_mask TINYINT UNSIGNED NOT NULL DEFAULT 0,

    start_date DATE NOT NULL,
    end_date DATE NULL,
    active_from DATE NULL,
    is_calendar TINYINT(1) NOT NULL DEFAULT 0,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY idx_recur_tasks_user (user_id),
    CHECK (repeat_interval >= 1),
    CHECK (reminder_mask BETWEEN 0 AND 31)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- TODOS (반복으로 생성된 인스턴스 + 사용자 입력으로 생성된 인스턴스)
CREATE TABLE todos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    recur_task_id BIGINT NULL,

    todo_type ENUM('TODO', 'FOCUS', 'WORKOUT') NOT NULL DEFAULT 'TODO',
    todo_status ENUM('PENDING', 'DONE', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    title VARCHAR(255) NOT NULL,
    memo TEXT NULL,

    is_all_day TINYINT(1) NOT NULL DEFAULT 0,
    occurrence_date DATE NOT NULL,
    at_time TIME NULL,
    completed_at DATETIME NULL,
    duration_sec INT NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY idx_todos_user (user_id),
    KEY idx_todos_user_occurrence (user_id, occurrence_date),
    KEY idx_todos_user_status (user_id, todo_status),
    KEY idx_todos_recur (recur_task_id),
    UNIQUE KEY uk_todos_recur_occurrence (recur_task_id, occurrence_date),

    CONSTRAINT fk_todos_recur_task
       FOREIGN KEY (recur_task_id) REFERENCES recur_tasks(id)
           ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;