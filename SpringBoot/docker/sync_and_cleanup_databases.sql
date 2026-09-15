-- ==============================================================================
-- Liochio FinTech Platform - Đồng Bộ & Quy Hoạch Toàn Diện Cơ Sở Dữ Liệu
-- Database-per-Domain Pattern
-- ==============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ==============================================================================
-- 1. DATABASE CONTENT & EAV ENGINE: `db_content_eav` (Port 8082 - entity-service)
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS `db_content_eav` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_content_eav`;

-- 1.1 Bảng Entity Types
CREATE TABLE IF NOT EXISTS `entity_types` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `code` VARCHAR(50) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(255),
    `icon` VARCHAR(100),
    `is_hierarchical` BOOLEAN DEFAULT FALSE,
    `is_system` BOOLEAN DEFAULT FALSE,
    `status` VARCHAR(20) DEFAULT 'ACTIVE',
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_tenant_entity_code` (`tenant_id`, `code`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.2 Bảng Dynamic Field Definitions
CREATE TABLE IF NOT EXISTS `dynamic_field_definitions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `entity_type_id` BIGINT NOT NULL,
    `field_code` VARCHAR(50) NOT NULL,
    `field_label` VARCHAR(100) NOT NULL,
    `field_type` VARCHAR(30) NOT NULL,
    `is_required` BOOLEAN DEFAULT FALSE,
    `is_unique` BOOLEAN DEFAULT FALSE,
    `is_searchable` BOOLEAN DEFAULT TRUE,
    `is_filterable` BOOLEAN DEFAULT TRUE,
    `default_value` TEXT,
    `validation_rules` JSON,
    `ui_component_type` VARCHAR(50) DEFAULT 'INPUT_TEXT',
    `display_order` INT DEFAULT 0,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_entity_field_code` (`entity_type_id`, `field_code`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.3 Bảng Dynamic Entities
CREATE TABLE IF NOT EXISTS `dynamic_entities` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `entity_type_id` BIGINT NOT NULL,
    `parent_id` BIGINT NULL,
    `code` VARCHAR(100) NOT NULL,
    `slug` VARCHAR(150),
    `status` VARCHAR(20) DEFAULT 'PUBLISHED',
    `view_count` BIGINT DEFAULT 0,
    `like_count` BIGINT DEFAULT 0,
    `rating_avg` DECIMAL(3,2) DEFAULT 5.00,
    `rating_count` INT DEFAULT 0,
    `attributes` JSON,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    INDEX `idx_tenant_type_status` (`tenant_id`, `entity_type_id`, `status`),
    INDEX `idx_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.4 Bảng Form Definitions
CREATE TABLE IF NOT EXISTS `form_definitions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `form_code` VARCHAR(100) NOT NULL,
    `form_name` VARCHAR(150) NOT NULL,
    `entity_type_code` VARCHAR(50),
    `schema_json` JSON NOT NULL,
    `status` VARCHAR(20) DEFAULT 'ACTIVE',
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_tenant_form_code` (`tenant_id`, `form_code`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.5 Bảng UI Configurations (Server-Driven UI)
CREATE TABLE IF NOT EXISTS `ui_configurations` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `page_code` VARCHAR(100) NOT NULL,
    `page_title` VARCHAR(150),
    `device_target` VARCHAR(30) DEFAULT 'ALL',
    `layout_json` JSON NOT NULL,
    `theme_json` JSON,
    `is_published` BOOLEAN DEFAULT TRUE,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_tenant_page_device` (`tenant_id`, `page_code`, `device_target`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.6 Bảng Navigation Menus
CREATE TABLE IF NOT EXISTS `navigation_menus` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `menu_code` VARCHAR(50) NOT NULL,
    `parent_id` BIGINT NULL,
    `title` VARCHAR(100) NOT NULL,
    `url` VARCHAR(255),
    `icon` VARCHAR(100),
    `target` VARCHAR(20) DEFAULT '_self',
    `display_order` INT DEFAULT 0,
    `required_role` VARCHAR(50) NULL,
    `is_visible` BOOLEAN DEFAULT TRUE,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.7 Bảng i18n Dictionaries
CREATE TABLE IF NOT EXISTS `i18n_dictionaries` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `locale` VARCHAR(10) NOT NULL,
    `message_key` VARCHAR(150) NOT NULL,
    `message_value` TEXT NOT NULL,
    `module_name` VARCHAR(50) DEFAULT 'COMMON',
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_tenant_locale_key` (`tenant_id`, `locale`, `message_key`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.8 Bảng Web Templates
CREATE TABLE IF NOT EXISTS `web_templates` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `template_code` VARCHAR(50) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `category` VARCHAR(50) NOT NULL,
    `thumbnail_url` VARCHAR(500),
    `structure_json` JSON,
    `default_theme_json` JSON,
    `is_active` BOOLEAN DEFAULT TRUE,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_template_code` (`template_code`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.9 Bảng Portfolio Items
CREATE TABLE IF NOT EXISTS `portfolio_items` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `title` VARCHAR(255) NOT NULL,
    `slug` VARCHAR(255) NOT NULL,
    `category` VARCHAR(50) NOT NULL,
    `summary` VARCHAR(500),
    `content_html` LONGTEXT,
    `thumbnail_url` VARCHAR(500),
    `gallery_urls` JSON,
    `external_url` VARCHAR(500),
    `client_name` VARCHAR(100),
    `completion_date` DATE,
    `tags` JSON,
    `status` VARCHAR(20) DEFAULT 'PUBLISHED',
    `is_featured` BOOLEAN DEFAULT FALSE,
    `view_count` BIGINT DEFAULT 0,
    `display_order` INT DEFAULT 0,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_tenant_portfolio_slug` (`tenant_id`, `slug`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.10 Bảng Entity Reviews & Ratings
CREATE TABLE IF NOT EXISTS `entity_reviews` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `entity_type` VARCHAR(50) NOT NULL,
    `entity_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `user_name` VARCHAR(100) NOT NULL,
    `user_avatar` VARCHAR(500),
    `rating` INT NOT NULL,
    `title` VARCHAR(200),
    `comment` TEXT,
    `status` VARCHAR(20) DEFAULT 'APPROVED',
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    INDEX `idx_tenant_entity` (`tenant_id`, `entity_type`, `entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.11 Bảng External API Integrations
CREATE TABLE IF NOT EXISTS `external_api_integrations` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `provider_code` VARCHAR(50) NOT NULL,
    `provider_name` VARCHAR(100) NOT NULL,
    `api_key` VARCHAR(255),
    `api_secret` VARCHAR(255),
    `base_url` VARCHAR(255),
    `configuration` JSON,
    `is_active` BOOLEAN DEFAULT TRUE,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_tenant_provider` (`tenant_id`, `provider_code`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ==============================================================================
-- 2. DATABASE TOUR & TRAVEL: `db_tour` (Port 8091 - tour-service)
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS `db_tour` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_tour`;

CREATE TABLE IF NOT EXISTS `tours` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `title` VARCHAR(255) NOT NULL,
    `slug` VARCHAR(255) NOT NULL,
    `tour_code` VARCHAR(50) NOT NULL,
    `summary` VARCHAR(500),
    `description` LONGTEXT,
    `duration_days` INT NOT NULL DEFAULT 1,
    `duration_nights` INT NOT NULL DEFAULT 0,
    `price_from` DECIMAL(15,2) NOT NULL,
    `currency` VARCHAR(10) DEFAULT 'VND',
    `transportation` VARCHAR(100),
    `departure_city` VARCHAR(100),
    `thumbnail_url` VARCHAR(500),
    `gallery_urls` JSON,
    `status` VARCHAR(20) DEFAULT 'ACTIVE',
    `is_featured` BOOLEAN DEFAULT FALSE,
    `rating_avg` DECIMAL(3,2) DEFAULT 5.00,
    `rating_count` INT DEFAULT 0,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_tenant_tour_slug` (`tenant_id`, `slug`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tour_destinations` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tour_id` BIGINT NOT NULL,
    `destination_name` VARCHAR(150) NOT NULL,
    `country` VARCHAR(100) DEFAULT 'Vietnam',
    `province_city` VARCHAR(100),
    `display_order` INT DEFAULT 0,
    `thumbnail_url` VARCHAR(500),
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tour_itineraries` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tour_id` BIGINT NOT NULL,
    `day_number` INT NOT NULL,
    `day_title` VARCHAR(200) NOT NULL,
    `activities_description` LONGTEXT,
    `meals_included` VARCHAR(100),
    `accommodation` VARCHAR(150),
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tour_departures` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tour_id` BIGINT NOT NULL,
    `departure_date` DATE NOT NULL,
    `return_date` DATE NOT NULL,
    `available_slots` INT NOT NULL DEFAULT 20,
    `total_slots` INT NOT NULL DEFAULT 20,
    `price_adult` DECIMAL(15,2) NOT NULL,
    `price_child` DECIMAL(15,2) DEFAULT 0,
    `status` VARCHAR(20) DEFAULT 'OPEN',
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tour_bookings` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `booking_code` VARCHAR(50) NOT NULL,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `tour_id` BIGINT NOT NULL,
    `departure_id` BIGINT NULL,
    `user_id` BIGINT NULL,
    `customer_name` VARCHAR(150) NOT NULL,
    `customer_email` VARCHAR(150) NOT NULL,
    `customer_phone` VARCHAR(30) NOT NULL,
    `number_of_adults` INT NOT NULL DEFAULT 1,
    `number_of_children` INT NOT NULL DEFAULT 0,
    `total_amount` DECIMAL(15,2) NOT NULL,
    `currency` VARCHAR(10) DEFAULT 'VND',
    `booking_status` VARCHAR(30) DEFAULT 'PENDING',
    `payment_status` VARCHAR(30) DEFAULT 'UNPAID',
    `payment_order_id` VARCHAR(100) NULL,
    `special_requests` TEXT,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_tour_booking_code` (`booking_code`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tour_reviews` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tour_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `user_name` VARCHAR(100) NOT NULL,
    `user_avatar` VARCHAR(500),
    `rating` INT NOT NULL,
    `comment` TEXT,
    `status` VARCHAR(20) DEFAULT 'APPROVED',
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tour_guides` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `full_name` VARCHAR(150) NOT NULL,
    `phone` VARCHAR(30),
    `email` VARCHAR(100),
    `languages` JSON,
    `bio` TEXT,
    `avatar_url` VARCHAR(500),
    `rating` DECIMAL(3,2) DEFAULT 5.00,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tour_pricing_tiers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tour_id` BIGINT NOT NULL,
    `tier_name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(255),
    `price` DECIMAL(15,2) NOT NULL,
    `benefits_json` JSON,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ==============================================================================
-- 3. DATABASE MUSIC & STREAMING: `db_music` (Port 8092 - music-service)
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS `db_music` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_music`;

CREATE TABLE IF NOT EXISTS `artists` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `name` VARCHAR(150) NOT NULL,
    `stage_name` VARCHAR(150),
    `bio` LONGTEXT,
    `avatar_url` VARCHAR(500),
    `cover_url` VARCHAR(500),
    `monthly_listeners` BIGINT DEFAULT 0,
    `is_verified` BOOLEAN DEFAULT TRUE,
    `social_links_json` JSON,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `music_genres` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `genre_code` VARCHAR(50) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(255),
    `thumbnail_url` VARCHAR(500),
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_music_genre_code` (`genre_code`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `albums` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `artist_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `slug` VARCHAR(200) NOT NULL,
    `release_date` DATE,
    `cover_url` VARCHAR(500),
    `genre` VARCHAR(50),
    `total_tracks` INT DEFAULT 0,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `songs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `album_id` BIGINT NULL,
    `artist_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `slug` VARCHAR(200) NOT NULL,
    `duration_seconds` INT NOT NULL,
    `audio_url` VARCHAR(500) NOT NULL,
    `lyrics_lrc` LONGTEXT,
    `play_count` BIGINT DEFAULT 0,
    `like_count` BIGINT DEFAULT 0,
    `is_lossless` BOOLEAN DEFAULT TRUE,
    `bitrate_kbps` INT DEFAULT 320,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `playlists` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(150) NOT NULL,
    `description` VARCHAR(500),
    `cover_url` VARCHAR(500),
    `is_public` BOOLEAN DEFAULT TRUE,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `playlist_songs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `playlist_id` BIGINT NOT NULL,
    `song_id` BIGINT NOT NULL,
    `display_order` INT DEFAULT 0,
    `added_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_playlist_song` (`playlist_id`, `song_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `track_reviews` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `song_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `user_name` VARCHAR(100) NOT NULL,
    `user_avatar` VARCHAR(500),
    `comment` TEXT,
    `rating` INT DEFAULT 5,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ==============================================================================
-- 4. DATABASE FILM & CINEMA: `db_film` (Port 8093 - film-service)
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS `db_film` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_film`;

CREATE TABLE IF NOT EXISTS `movie_genres` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `genre_code` VARCHAR(50) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(255),
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_film_genre_code` (`genre_code`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `movies` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `title` VARCHAR(255) NOT NULL,
    `original_title` VARCHAR(255),
    `slug` VARCHAR(255) NOT NULL,
    `synopsis` LONGTEXT,
    `duration_minutes` INT,
    `release_year` INT,
    `country` VARCHAR(100) DEFAULT 'Vietnam',
    `director` VARCHAR(150),
    `cast_members` JSON,
    `poster_url` VARCHAR(500),
    `banner_url` VARCHAR(500),
    `trailer_url` VARCHAR(500),
    `age_rating` VARCHAR(10) DEFAULT 'P',
    `imdb_rating` DECIMAL(3,1),
    `status` VARCHAR(20) DEFAULT 'RELEASED',
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_tenant_movie_slug` (`tenant_id`, `slug`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `movie_genres_mapping` (
    `movie_id` BIGINT NOT NULL,
    `genre_id` BIGINT NOT NULL,
    PRIMARY KEY (`movie_id`, `genre_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `movie_episodes` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `movie_id` BIGINT NOT NULL,
    `episode_number` INT NOT NULL,
    `episode_title` VARCHAR(150),
    `video_stream_url` VARCHAR(500) NOT NULL,
    `video_quality` VARCHAR(20) DEFAULT '1080p',
    `subtitles_json` JSON,
    `duration_minutes` INT,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `streaming_servers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `server_name` VARCHAR(100) NOT NULL,
    `server_region` VARCHAR(50) DEFAULT 'VN',
    `base_url` VARCHAR(255) NOT NULL,
    `priority` INT DEFAULT 1,
    `is_active` BOOLEAN DEFAULT TRUE,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `movie_casts` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `movie_id` BIGINT NOT NULL,
    `actor_name` VARCHAR(150) NOT NULL,
    `character_name` VARCHAR(150),
    `avatar_url` VARCHAR(500),
    `display_order` INT DEFAULT 0,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `movie_reviews` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `movie_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `user_name` VARCHAR(100) NOT NULL,
    `rating` DECIMAL(2,1) NOT NULL,
    `comment` TEXT,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ==============================================================================
-- 5. DATABASE GAMING & ESPORTS: `db_gaming`
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS `db_gaming` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_gaming`;

CREATE TABLE IF NOT EXISTS `games` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `title` VARCHAR(150) NOT NULL,
    `slug` VARCHAR(150) NOT NULL,
    `genre` VARCHAR(50),
    `publisher` VARCHAR(100),
    `icon_url` VARCHAR(500),
    `cover_url` VARCHAR(500),
    `download_url` VARCHAR(500),
    `version_str` VARCHAR(20) DEFAULT '1.0.0',
    `is_active` BOOLEAN DEFAULT TRUE,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_tenant_game_slug` (`tenant_id`, `slug`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `game_heroes` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `game_id` BIGINT NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `role` VARCHAR(50) NOT NULL,
    `difficulty` VARCHAR(20) DEFAULT 'MEDIUM',
    `avatar_url` VARCHAR(500),
    `skills_json` JSON,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `game_servers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `game_id` BIGINT NOT NULL,
    `server_name` VARCHAR(100) NOT NULL,
    `region` VARCHAR(50) DEFAULT 'ASIA',
    `status` VARCHAR(20) DEFAULT 'ONLINE',
    `capacity_max` INT DEFAULT 10000,
    `online_players` INT DEFAULT 0,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `game_items_shop` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `game_id` BIGINT NOT NULL,
    `item_name` VARCHAR(150) NOT NULL,
    `item_type` VARCHAR(50) NOT NULL,
    `price_gem` INT DEFAULT 0,
    `price_gold` INT DEFAULT 0,
    `rarity` VARCHAR(30) DEFAULT 'COMMON',
    `icon_url` VARCHAR(500),
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `game_rankings` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `game_id` BIGINT NOT NULL,
    `season_name` VARCHAR(50) NOT NULL,
    `user_id` BIGINT NOT NULL,
    `player_name` VARCHAR(100) NOT NULL,
    `rank_tier` VARCHAR(50) NOT NULL,
    `points` INT DEFAULT 0,
    `rank_position` INT NOT NULL,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `guilds` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `game_id` BIGINT NOT NULL,
    `guild_name` VARCHAR(100) NOT NULL,
    `leader_user_id` BIGINT NOT NULL,
    `level` INT DEFAULT 1,
    `total_members` INT DEFAULT 1,
    `badge_url` VARCHAR(500),
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `guild_members` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `guild_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `member_role` VARCHAR(30) DEFAULT 'MEMBER',
    `contribution_points` INT DEFAULT 0,
    `joined_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_guild_member` (`guild_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ==============================================================================
-- 6. DATABASE BLOG & EDITORIAL: `db_blog`
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS `db_blog` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_blog`;

CREATE TABLE IF NOT EXISTS `blog_categories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `name` VARCHAR(100) NOT NULL,
    `slug` VARCHAR(100) NOT NULL,
    `description` VARCHAR(255),
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_tenant_category_slug` (`tenant_id`, `slug`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `blog_tags` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `tag_name` VARCHAR(50) NOT NULL,
    `slug` VARCHAR(50) NOT NULL,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_tenant_tag_slug` (`tenant_id`, `slug`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `blog_articles` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `category_id` BIGINT NULL,
    `title` VARCHAR(255) NOT NULL,
    `slug` VARCHAR(255) NOT NULL,
    `summary` VARCHAR(500),
    `content_html` LONGTEXT NOT NULL,
    `thumbnail_url` VARCHAR(500),
    `author_name` VARCHAR(100) NOT NULL,
    `author_avatar` VARCHAR(500),
    `status` VARCHAR(20) DEFAULT 'PUBLISHED',
    `published_at` TIMESTAMP NULL,
    `view_count` BIGINT DEFAULT 0,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_tenant_article_slug` (`tenant_id`, `slug`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `blog_article_tags` (
    `article_id` BIGINT NOT NULL,
    `tag_id` BIGINT NOT NULL,
    PRIMARY KEY (`article_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `blog_comments` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `article_id` BIGINT NOT NULL,
    `user_id` BIGINT NULL,
    `author_name` VARCHAR(100) NOT NULL,
    `author_email` VARCHAR(100),
    `content` TEXT NOT NULL,
    `parent_id` BIGINT NULL,
    `status` VARCHAR(20) DEFAULT 'APPROVED',
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ==============================================================================
-- 7. DATABASE AI & VECTOR: `db_ai_vector` (Port 8086 - ai-service)
-- ==============================================================================
CREATE DATABASE IF NOT EXISTS `db_ai_vector` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `db_ai_vector`;

CREATE TABLE IF NOT EXISTS `tenant_ai_configs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `ai_provider` VARCHAR(50) DEFAULT 'OPENAI',
    `api_key` VARCHAR(255),
    `model_name` VARCHAR(50) DEFAULT 'gpt-4o-mini',
    `system_prompt` TEXT,
    `temperature` DECIMAL(2,1) DEFAULT 0.7,
    `max_tokens` INT DEFAULT 1000,
    `is_enabled` BOOLEAN DEFAULT TRUE,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_tenant_ai_config` (`tenant_id`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ai_chat_sessions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `session_code` VARCHAR(50) NOT NULL,
    `user_id` BIGINT NULL,
    `visitor_id` VARCHAR(100) NULL,
    `title` VARCHAR(200) DEFAULT 'Cuộc trò chuyện mới',
    `context_metadata` JSON,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_ai_session_code` (`session_code`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ai_chat_messages` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `session_id` BIGINT NOT NULL,
    `role` VARCHAR(20) NOT NULL,
    `content` LONGTEXT NOT NULL,
    `tokens_used` INT DEFAULT 0,
    `model_used` VARCHAR(50),
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ai_knowledge_base` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `doc_title` VARCHAR(200) NOT NULL,
    `doc_category` VARCHAR(50) DEFAULT 'GENERAL',
    `raw_content` LONGTEXT NOT NULL,
    `chunk_index` INT DEFAULT 0,
    `source_url` VARCHAR(500),
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ai_vector_embeddings` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `knowledge_id` BIGINT NOT NULL,
    `embedding_vector` JSON NOT NULL,
    `dimensions` INT DEFAULT 1536,
    `model_name` VARCHAR(50) DEFAULT 'text-embedding-3-small',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ai_prompts` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `prompt_code` VARCHAR(50) NOT NULL,
    `prompt_title` VARCHAR(150) NOT NULL,
    `prompt_template` LONGTEXT NOT NULL,
    `variables_json` JSON,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    UNIQUE KEY `uk_tenant_prompt_code` (`tenant_id`, `prompt_code`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ==============================================================================
-- 8. DỌN DẸP BẢNG NẰM SAI TRONG CORE DATABASE `liochio_core_db`
-- ==============================================================================
USE `liochio_core_db`;

-- Dọn dẹp các bảng thuộc domain khác khỏi core database để đảm bảo nguyên tắc Database-Per-Service
DROP TABLE IF EXISTS `liochio_core_db`.`ai_chat_messages`;
DROP TABLE IF EXISTS `liochio_core_db`.`ai_chat_sessions`;
DROP TABLE IF EXISTS `liochio_core_db`.`ai_knowledge_base`;
DROP TABLE IF EXISTS `liochio_core_db`.`tenant_ai_configs`;

DROP TABLE IF EXISTS `liochio_core_db`.`dynamic_entities`;
DROP TABLE IF EXISTS `liochio_core_db`.`dynamic_field_definitions`;
DROP TABLE IF EXISTS `liochio_core_db`.`entity_types`;
DROP TABLE IF EXISTS `liochio_core_db`.`form_definitions`;
DROP TABLE IF EXISTS `liochio_core_db`.`i18n_dictionaries`;
DROP TABLE IF EXISTS `liochio_core_db`.`navigation_menus`;
DROP TABLE IF EXISTS `liochio_core_db`.`ui_configurations`;
DROP TABLE IF EXISTS `liochio_core_db`.`web_templates`;
DROP TABLE IF EXISTS `liochio_core_db`.`entity_reviews`;
DROP TABLE IF EXISTS `liochio_core_db`.`external_api_integrations`;
DROP TABLE IF EXISTS `liochio_core_db`.`bookings`;

SET FOREIGN_KEY_CHECKS = 1;
