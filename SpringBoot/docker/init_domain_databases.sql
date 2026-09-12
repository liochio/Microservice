-- ==============================================================================
-- KHỞI TẠO CÁC CƠ SỞ DỮ LIỆU CHUYÊN BIỆT THEO CHỦ ĐỀ (DATABASE-PER-DOMAIN)
-- ==============================================================================

-- ==============================================================================
-- 1. DATABASE CHỦ ĐỀ DU LỊCH & KHÁM PHÁ (db_tour)
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

-- Dữ liệu mẫu Tour
INSERT IGNORE INTO `tours` (`id`, `tenant_id`, `code`, `title`, `slug`, `summary`, `thumbnail_url`, `duration_days`, `duration_nights`, `departure_location`, `destination`, `base_price`, `transportation`, `included_services`, `status`) VALUES
(1, 'default', 'TOUR-HG-3N2D', 'Khám Phá Cao Nguyên Đá Hà Giang 3N2Đ', 'kham-pha-cao-nguyen-da-ha-giang-3n2d', 'Hành trình ngắm mùa hoa tam giác mạch và vượt đèo Mã Pí Lèng hùng vĩ.', 'https://images.unsplash.com/photo-1528127269322-539801943592', 3, 2, 'Hà Nội', 'Hà Giang', 3200000.00, 'Xe Limousine VIP', '["Khách sạn 3 sao", "Ăn sáng buffet", "Vé tham quan"]', 'PUBLISHED');

INSERT IGNORE INTO `tour_departures` (`id`, `tour_id`, `departure_date`, `return_date`, `max_slots`, `booked_slots`, `adult_price`, `child_price`, `status`) VALUES
(1, 1, '2026-09-15', '2026-09-17', 25, 6, 3200000.00, 2200000.00, 'OPEN');

-- ==============================================================================
-- 2. DATABASE CHỦ ĐỀ ÂM NHẠC & AUDIO (db_music)
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

-- Dữ liệu mẫu Music
INSERT IGNORE INTO `artists` (`id`, `tenant_id`, `name`, `avatar_url`, `bio`) VALUES
(1, 'default', 'Sơn Tùng M-TP', 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4', 'Nghệ sĩ, ca sĩ kiêm nhạc sĩ hàng đầu Việt Nam.');

INSERT IGNORE INTO `songs` (`id`, `tenant_id`, `artist_id`, `title`, `slug`, `audio_url`, `thumbnail_url`, `duration_seconds`, `composer`, `play_count`) VALUES
(1, 'default', 1, 'Đừng Làm Trái Tim Anh Đau', 'dung-lam-trai-tim-anh-dau', 'https://cdn.portfolio.dev/audio/dung-lam-trai-tim-anh-dau.mp3', 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745', 260, 'Sơn Tùng M-TP', 1250000);

-- ==============================================================================
-- 3. DATABASE CHỦ ĐỀ ĐIỆN ẢNH & VIDEO (db_film)
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

-- Dữ liệu mẫu Film
INSERT IGNORE INTO `movies` (`id`, `tenant_id`, `title`, `slug`, `movie_type`, `poster_url`, `duration_minutes`, `release_year`, `quality`, `country`, `director`, `genres`, `rating_avg`) VALUES
(1, 'default', 'Mai (2024)', 'mai-2024', 'SINGLE', 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba', 131, 2024, '4K', 'Việt Nam', 'Trấn Thành', '["Tâm Lý", "Tình Cảm"]', 8.6);

INSERT IGNORE INTO `movie_episodes` (`id`, `movie_id`, `episode_number`, `title`, `video_cdn_url`, `duration_seconds`) VALUES
(1, 1, 1, 'Bản Chiếu Rạp Full HD', 'https://cdn.portfolio.dev/video/mai-2024-full.m3u8', 7860);
