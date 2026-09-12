-- ==============================================================================
-- TỔNG HỢP SETUP TOÀN BỘ CƠ SỞ DỮ LIỆU CHUẨN MICROSERVICES (DATABASE-PER-DOMAIN)
-- ==============================================================================

-- ==============================================================================
-- 1. DATABASE CORE (portfolio-engine / db_core)
-- ==============================================================================
USE `portfolio-engine`;

-- Dọn dẹp các bảng nghiệp vụ chuyên biệt đã tách sang Database riêng
DROP TABLE IF EXISTS `ai_chat_messages`;
DROP TABLE IF EXISTS `ai_chat_sessions`;
DROP TABLE IF EXISTS `ai_knowledge_base`;
DROP TABLE IF EXISTS `tenant_ai_configs`;
DROP TABLE IF EXISTS `entity_reviews`;

-- ==============================================================================
-- 2. DATABASE DU LỊCH & KHÁM PHÁ (db_tour)
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS `db_tour` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_tour`;

CREATE TABLE IF NOT EXISTS `tours` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `code` VARCHAR(50) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `slug` VARCHAR(255) NOT NULL,
    `summary` TEXT,
    `thumbnail_url` VARCHAR(500),
    `duration_days` INT NOT NULL,
    `duration_nights` INT NOT NULL,
    `departure_location` VARCHAR(150),
    `destination` VARCHAR(150),
    `base_price` DECIMAL(15, 2) NOT NULL,
    `transportation` VARCHAR(100),
    `included_services` JSON,
    `excluded_services` JSON,
    `policies_refund` TEXT,
    `i18n_content` JSON,
    `status` ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'PUBLISHED',
    `view_count` BIGINT DEFAULT 0,
    `version` BIGINT DEFAULT 0,
    `is_deleted` BOOLEAN DEFAULT FALSE,
    `deleted_at` TIMESTAMP NULL,
    `created_by` BIGINT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_tour_slug` (`tenant_id`, `slug`),
    INDEX `idx_tour_lookup` (`tenant_id`, `status`, `is_deleted`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `tour_itineraries` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tour_id` BIGINT NOT NULL,
    `day_number` INT NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `description` TEXT,
    `meals` VARCHAR(100),
    `media_gallery` JSON,
    FOREIGN KEY (`tour_id`) REFERENCES `tours`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `tour_departures` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tour_id` BIGINT NOT NULL,
    `departure_date` DATE NOT NULL,
    `return_date` DATE NOT NULL,
    `max_slots` INT NOT NULL,
    `booked_slots` INT DEFAULT 0,
    `adult_price` DECIMAL(15, 2) NOT NULL,
    `child_price` DECIMAL(15, 2) NOT NULL,
    `status` ENUM('OPEN', 'FULL', 'CANCELLED') DEFAULT 'OPEN',
    FOREIGN KEY (`tour_id`) REFERENCES `tours`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `tour_destinations` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `name` VARCHAR(150) NOT NULL,
    `slug` VARCHAR(150) NOT NULL,
    `country` VARCHAR(100) DEFAULT 'Việt Nam',
    `region` VARCHAR(50),
    `thumbnail_url` VARCHAR(500),
    `description` TEXT,
    UNIQUE KEY `uk_tenant_dest_slug` (`tenant_id`, `slug`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `tour_reviews` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `tour_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `rating_score` TINYINT NOT NULL DEFAULT 5,
    `comment` TEXT NOT NULL,
    `media_attachments` JSON,
    `is_approved` BOOLEAN DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_review_tour` (`tenant_id`, `tour_id`),
    FOREIGN KEY (`tour_id`) REFERENCES `tours`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ==============================================================================
-- 3. DATABASE ÂM NHẠC & BẢN NHẠC SỐ (db_music)
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS `db_music` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_music`;

CREATE TABLE IF NOT EXISTS `artists` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `name` VARCHAR(150) NOT NULL,
    `avatar_url` VARCHAR(500),
    `bio` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `albums` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `artist_id` BIGINT,
    `title` VARCHAR(255) NOT NULL,
    `cover_url` VARCHAR(500),
    `release_year` INT,
    FOREIGN KEY (`artist_id`) REFERENCES `artists`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `songs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `album_id` BIGINT,
    `artist_id` BIGINT,
    `title` VARCHAR(255) NOT NULL,
    `slug` VARCHAR(255) NOT NULL,
    `audio_url` VARCHAR(500) NOT NULL,
    `thumbnail_url` VARCHAR(500),
    `bitrate` VARCHAR(20) DEFAULT '320kbps',
    `duration_seconds` INT NOT NULL,
    `composer` VARCHAR(150),
    `lyrics_lrc` TEXT,
    `play_count` BIGINT DEFAULT 0,
    `like_count` BIGINT DEFAULT 0,
    `is_premium` BOOLEAN DEFAULT FALSE,
    `version` BIGINT DEFAULT 0,
    `is_deleted` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_song_lookup` (`tenant_id`, `slug`),
    FOREIGN KEY (`album_id`) REFERENCES `albums`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`artist_id`) REFERENCES `artists`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `playlists` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `cover_url` VARCHAR(500),
    `is_public` BOOLEAN DEFAULT TRUE,
    `song_ids` JSON,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `track_reviews` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `song_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `rating_score` TINYINT NOT NULL DEFAULT 5,
    `comment` TEXT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_review_song` (`tenant_id`, `song_id`)
) ENGINE=InnoDB;

-- ==============================================================================
-- 4. DATABASE ĐIỆN ẢNH & VIDEO STREAMING (db_film)
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS `db_film` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_film`;

CREATE TABLE IF NOT EXISTS `movies` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `original_title` VARCHAR(255),
    `slug` VARCHAR(255) NOT NULL,
    `movie_type` ENUM('SINGLE', 'SERIES') NOT NULL,
    `poster_url` VARCHAR(500),
    `banner_url` VARCHAR(500),
    `trailer_url` VARCHAR(500),
    `duration_minutes` INT,
    `release_year` INT,
    `quality` VARCHAR(20) DEFAULT 'HD',
    `age_rating` VARCHAR(10) DEFAULT '16+',
    `country` VARCHAR(100),
    `director` VARCHAR(150),
    `cast_members` JSON,
    `genres` JSON,
    `view_count` BIGINT DEFAULT 0,
    `rating_avg` DECIMAL(3, 1) DEFAULT 0.0,
    `status` ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'PUBLISHED',
    `version` BIGINT DEFAULT 0,
    `is_deleted` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_movie` (`tenant_id`, `slug`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `movie_episodes` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `movie_id` BIGINT NOT NULL,
    `episode_number` INT NOT NULL,
    `title` VARCHAR(255),
    `video_cdn_url` VARCHAR(500) NOT NULL,
    `subtitles` JSON,
    `duration_seconds` INT,
    FOREIGN KEY (`movie_id`) REFERENCES `movies`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `movie_genres` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `slug` VARCHAR(100) NOT NULL,
    `description` VARCHAR(255),
    UNIQUE KEY `uk_genre_slug` (`slug`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `streaming_servers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `server_name` VARCHAR(100) NOT NULL,
    `base_url` VARCHAR(500) NOT NULL,
    `is_active` BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

-- ==============================================================================
-- 5. DATABASE GAME & ESPORTS (db_gaming)
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS `db_gaming` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_gaming`;

CREATE TABLE IF NOT EXISTS `games` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `slug` VARCHAR(255) NOT NULL,
    `icon_url` VARCHAR(500),
    `banner_url` VARCHAR(500),
    `game_engine` VARCHAR(50),
    `download_url` VARCHAR(500),
    `game_size_mb` BIGINT,
    `version` VARCHAR(30) DEFAULT '1.0.0',
    `system_requirements` JSON,
    `is_deleted` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_game` (`tenant_id`, `slug`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `game_heroes` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `game_id` BIGINT NOT NULL,
    `name` VARCHAR(150) NOT NULL,
    `role` VARCHAR(50),
    `stats` JSON,
    `avatar_url` VARCHAR(500),
    FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `game_items_shop` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `game_id` BIGINT NOT NULL,
    `name` VARCHAR(150) NOT NULL,
    `item_type` VARCHAR(50),
    `price_coins` BIGINT NOT NULL,
    `thumbnail_url` VARCHAR(500),
    `attributes` JSON,
    FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `game_servers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `game_id` BIGINT NOT NULL,
    `server_name` VARCHAR(100) NOT NULL,
    `ip_address` VARCHAR(100),
    `region` VARCHAR(50),
    `max_players` INT DEFAULT 1000,
    `current_players` INT DEFAULT 0,
    `status` ENUM('ONLINE', 'MAINTENANCE', 'OFFLINE') DEFAULT 'ONLINE',
    FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `game_rankings` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `game_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `rank_tier` VARCHAR(50) DEFAULT 'BRONZE',
    `elo_score` INT DEFAULT 1000,
    `wins` INT DEFAULT 0,
    `losses` INT DEFAULT 0,
    INDEX `idx_rank_elo` (`game_id`, `elo_score` DESC)
) ENGINE=InnoDB;

-- ==============================================================================
-- 6. DATABASE TIN TỨC & BLOG (db_blog)
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS `db_blog` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_blog`;

CREATE TABLE IF NOT EXISTS `blog_categories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `name` VARCHAR(150) NOT NULL,
    `slug` VARCHAR(150) NOT NULL,
    UNIQUE KEY `uk_cat` (`tenant_id`, `slug`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `blog_articles` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `category_id` BIGINT,
    `title` VARCHAR(255) NOT NULL,
    `slug` VARCHAR(255) NOT NULL,
    `thumbnail_url` VARCHAR(500),
    `content_html` MEDIUMTEXT NOT NULL,
    `author_id` BIGINT,
    `tags` JSON,
    `view_count` BIGINT DEFAULT 0,
    `status` ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'PUBLISHED',
    `published_at` TIMESTAMP NULL,
    `is_deleted` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`category_id`) REFERENCES `blog_categories`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ==============================================================================
-- 7. DATABASE TRÍ TUỆ NHÂN TẠO & RAG (db_ai_vector)
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS `db_ai_vector` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_ai_vector`;

CREATE TABLE IF NOT EXISTS `tenant_ai_configs` (
    `tenant_id` VARCHAR(50) PRIMARY KEY,
    `ai_provider` VARCHAR(50) NOT NULL DEFAULT 'OPENAI',
    `api_key_encrypted` VARCHAR(500),
    `model_name` VARCHAR(50) DEFAULT 'gpt-4o-mini',
    `system_prompt` TEXT,
    `temperature` DECIMAL(3, 2) DEFAULT 0.70,
    `max_tokens` INT DEFAULT 2000,
    `is_active` BOOLEAN DEFAULT TRUE,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `ai_knowledge_base` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `doc_title` VARCHAR(255) NOT NULL,
    `content_chunk` LONGTEXT NOT NULL,
    `metadata` JSON,
    `is_active` BOOLEAN DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_ai_kb_tenant` (`tenant_id`, `is_active`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `ai_chat_sessions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `session_token` VARCHAR(100) NOT NULL,
    `user_id` BIGINT,
    `guest_ip` VARCHAR(50),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `last_activity_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_ai_session_lookup` (`tenant_id`, `session_token`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `ai_chat_messages` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `session_id` BIGINT NOT NULL,
    `sender_type` VARCHAR(30) NOT NULL DEFAULT 'USER',
    `message_text` LONGTEXT NOT NULL,
    `tokens_used` INT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_ai_msg_session` (`session_id`, `created_at`),
    FOREIGN KEY (`session_id`) REFERENCES `ai_chat_sessions`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ==============================================================================
-- 8. DATABASE ĐỘNG CƠ EAV & METADATA (db_content_eav)
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS `db_content_eav` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_content_eav`;

CREATE TABLE IF NOT EXISTS `entity_types` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `code` VARCHAR(100) NOT NULL,
    `name` VARCHAR(150) NOT NULL,
    `description` VARCHAR(255),
    `icon` VARCHAR(100),
    `is_hierarchical` BOOLEAN DEFAULT FALSE,
    `is_system` BOOLEAN DEFAULT FALSE,
    `version` BIGINT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_type_code` (`tenant_id`, `code`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `dynamic_field_definitions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `entity_type_id` BIGINT NOT NULL,
    `field_key` VARCHAR(100) NOT NULL,
    `data_type` VARCHAR(50) NOT NULL,
    `ui_component` VARCHAR(50) NOT NULL,
    `label` JSON NOT NULL,
    `placeholder` JSON,
    `is_required` BOOLEAN DEFAULT FALSE,
    `is_searchable` BOOLEAN DEFAULT FALSE,
    `validation_rules` JSON,
    `default_value` JSON,
    `options` JSON,
    `display_order` INT DEFAULT 0,
    `version` BIGINT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_type_field_key` (`entity_type_id`, `field_key`),
    FOREIGN KEY (`entity_type_id`) REFERENCES `entity_types`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `dynamic_entities` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `entity_type_id` BIGINT NOT NULL,
    `parent_id` BIGINT NULL,
    `title` VARCHAR(255) NOT NULL,
    `slug` VARCHAR(255) NOT NULL,
    `attributes` JSON NOT NULL,
    `i18n_content` JSON,
    `status` VARCHAR(30) DEFAULT 'PUBLISHED',
    `view_count` BIGINT DEFAULT 0,
    `display_order` INT DEFAULT 0,
    `version` BIGINT DEFAULT 0,
    `is_deleted` BOOLEAN DEFAULT FALSE,
    `deleted_at` TIMESTAMP NULL,
    `created_by` BIGINT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_entity_slug` (`tenant_id`, `entity_type_id`, `slug`),
    INDEX `idx_entity_lookup` (`tenant_id`, `entity_type_id`, `status`, `is_deleted`),
    FOREIGN KEY (`entity_type_id`) REFERENCES `entity_types`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `ui_configurations` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `page_route` VARCHAR(150) NOT NULL,
    `layout_blocks` JSON NOT NULL,
    `seo_meta` JSON,
    `version` BIGINT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_page` (`tenant_id`, `page_route`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `navigation_menus` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `parent_id` BIGINT NULL,
    `title` JSON NOT NULL,
    `url` VARCHAR(255) NOT NULL,
    `icon` VARCHAR(100),
    `target` VARCHAR(20) DEFAULT '_self',
    `display_order` INT DEFAULT 0,
    `is_active` BOOLEAN DEFAULT TRUE,
    `required_permission` VARCHAR(100),
    `version` BIGINT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `form_definitions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `form_code` VARCHAR(100) NOT NULL,
    `title` JSON NOT NULL,
    `form_schema` JSON NOT NULL,
    `submit_action_url` VARCHAR(255),
    `success_message` JSON,
    `is_active` BOOLEAN DEFAULT TRUE,
    `version` BIGINT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_form` (`tenant_id`, `form_code`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `i18n_dictionaries` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `translation_key` VARCHAR(150) NOT NULL,
    `translations` JSON NOT NULL,
    `version` BIGINT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_i18n_key` (`tenant_id`, `translation_key`)
) ENGINE=InnoDB;
