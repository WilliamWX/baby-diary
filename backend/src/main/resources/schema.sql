-- 养娃宝数据库初始化脚本

CREATE DATABASE IF NOT EXISTS baby_diary DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE baby_diary;

-- 用户表
CREATE TABLE `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    avatar VARCHAR(500),
    bio VARCHAR(200),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 宝宝档案表
CREATE TABLE baby (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    gender TINYINT DEFAULT 0 COMMENT '0女 1男',
    birthday DATE,
    avatar VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_baby_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 日记表
CREATE TABLE diary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    baby_id BIGINT,
    content LONGTEXT,
    visibility TINYINT DEFAULT 1 COMMENT '0私密 1公开 2仅好友',
    view_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    record_date DATE NOT NULL COMMENT '记录事件日期',
    INDEX idx_diary_user (user_id),
    INDEX idx_diary_baby (baby_id),
    INDEX idx_diary_created (created_at),
    INDEX idx_diary_record (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 日记图片表
CREATE TABLE diary_image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    diary_id BIGINT NOT NULL,
    url VARCHAR(500) NOT NULL,
    sort INT DEFAULT 0,
    INDEX idx_image_diary (diary_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 成长记录表
CREATE TABLE growth_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    baby_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    height DECIMAL(5,2) COMMENT '身高cm',
    weight DECIMAL(5,2) COMMENT '体重kg',
    head_circumference DECIMAL(5,2) COMMENT '头围cm',
    note VARCHAR(500),
    INDEX idx_growth_baby (baby_id),
    INDEX idx_growth_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 喂养记录表
CREATE TABLE feeding_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    baby_id BIGINT NOT NULL,
    feed_time DATETIME NOT NULL,
    type TINYINT NOT NULL COMMENT '0母乳 1奶粉 2辅食',
    amount VARCHAR(50) COMMENT '量(ml/克)',
    food_name VARCHAR(100),
    note VARCHAR(200),
    INDEX idx_feeding_baby (baby_id),
    INDEX idx_feeding_time (feed_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 睡眠记录表
CREATE TABLE sleep_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    baby_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    quality TINYINT DEFAULT 3 COMMENT '1-5评分',
    note VARCHAR(200),
    INDEX idx_sleep_baby (baby_id),
    INDEX idx_sleep_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 里程碑表
CREATE TABLE milestone (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    baby_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT,
    event_date DATE NOT NULL,
    photos VARCHAR(1000) COMMENT '照片JSON数组',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_milestone_baby (baby_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 经验分享帖子表
CREATE TABLE post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT,
    category VARCHAR(50) COMMENT '喂养/睡眠/早教/健康/其他',
    view_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_post_user (user_id),
    INDEX idx_post_category (category),
    INDEX idx_post_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 点赞记录表
CREATE TABLE like_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL COMMENT 'diary/post',
    target_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_like (user_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评论表
CREATE TABLE comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL COMMENT 'diary/post',
    target_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID，支持回复',
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_comment_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 收藏表
CREATE TABLE bookmark (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL COMMENT 'diary/post',
    target_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_bookmark (user_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 关注表
CREATE TABLE follow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_follow (follower_id, following_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 通知表
CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '接收者',
    actor_id BIGINT NOT NULL COMMENT '触发者',
    type VARCHAR(20) NOT NULL COMMENT 'like/comment/follow',
    target_type VARCHAR(20) COMMENT 'diary/post',
    target_id BIGINT,
    content VARCHAR(500),
    is_read TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notify_user (user_id, is_read),
    INDEX idx_notify_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- AI医生问答历史表
CREATE TABLE ai_chat_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    anonymous TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_chat_user (user_id),
    INDEX idx_chat_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
