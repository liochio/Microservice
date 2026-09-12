-- ==============================================================================
-- Migration V3: Khởi tạo các bảng Web Templates, Entity Types, SDUI, AI RAG & Reviews
-- ==============================================================================

-- 4. Bảng Mẫu Website (Web Templates)
CREATE TABLE IF NOT EXISTS `web_templates` (
    `code` VARCHAR(50) PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT NULL,
    `default_features` JSON NOT NULL,
    `version` BIGINT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO `web_templates` (`code`, `name`, `description`, `default_features`) VALUES
('TEMPLATE_TOURISM', 'Mẫu Website Du Lịch & Khám Phá', 'Dành cho Blogger, Công ty lữ hành, Tour du lịch', '["FEATURE_PORTFOLIO", "FEATURE_DYNAMIC_ENTITY", "FEATURE_ECOMMERCE_BOOKING", "FEATURE_AI_CHATBOT"]'),
('TEMPLATE_ARTIST', 'Mẫu Portfolio Nghệ Sĩ / Nhiếp Ảnh Gia', 'Dành cho Nhiếp ảnh gia, Ca sĩ, Họa sĩ trưng bày tác phẩm', '["FEATURE_PORTFOLIO", "FEATURE_DYNAMIC_ENTITY", "FEATURE_CHUNK_UPLOAD"]'),
('TEMPLATE_FREELANCER', 'Mẫu Portfolio Cá Nhân Chuyên Nghiệp', 'Dành cho Kỹ sư phần mềm, Designer, Content Creator', '["FEATURE_PORTFOLIO", "FEATURE_DYNAMIC_ENTITY", "FEATURE_MULTI_LANGUAGE"]');

-- 10. Bảng Định nghĩa Loại Thực thể (Entity Types)
CREATE TABLE IF NOT EXISTS `entity_types` (
    `code` VARCHAR(50) PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(255) NULL,
    `is_hierarchical` BOOLEAN NOT NULL DEFAULT FALSE,
    `icon` VARCHAR(100) NULL,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO `entity_types` (`code`, `name`, `description`, `is_hierarchical`, `icon`) VALUES
('TOUR_PACKAGE', 'Gói Tour Du Lịch', 'Định nghĩa thông tin các tour du lịch', FALSE, 'map-pin'),
('HOTEL_ROOM', 'Phòng Khách Sạn / Homestay', 'Định nghĩa phòng nghỉ và dịch vụ lưu trú', FALSE, 'home'),
('MUSIC_TRACK', 'Bài Hát & Album', 'Định nghĩa bản nhạc số và podcast', FALSE, 'music'),
('ARTICLE_POST', 'Bài Viết Tin Tức / Blog', 'Định nghĩa bài viết tin tức và cẩm nang', TRUE, 'file-text');

-- 11. Bảng Định nghĩa Thuộc tính Động (Dynamic Field Definitions)
CREATE TABLE IF NOT EXISTS `dynamic_field_definitions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `entity_type_code` VARCHAR(50) NOT NULL,
    `field_key` VARCHAR(50) NOT NULL,
    `field_label` JSON NOT NULL,
    `data_type` VARCHAR(30) NOT NULL,
    `is_required` BOOLEAN NOT NULL DEFAULT FALSE,
    `is_searchable` BOOLEAN NOT NULL DEFAULT FALSE,
    `is_filterable` BOOLEAN NOT NULL DEFAULT FALSE,
    `validation_rules` JSON NULL,
    `ui_component` VARCHAR(50) NOT NULL,
    `default_value` VARCHAR(255) NULL,
    `display_order` INT NOT NULL DEFAULT 0,
    `version` BIGINT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_entity_type_field` (`entity_type_code`, `field_key`),
    FOREIGN KEY (`entity_type_code`) REFERENCES `entity_types`(`code`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. Bảng Từ điển Đa ngôn ngữ (i18n Dictionaries)
CREATE TABLE IF NOT EXISTS `i18n_dictionaries` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    `text_key` VARCHAR(150) NOT NULL,
    `locale` VARCHAR(10) NOT NULL,
    `content` TEXT NOT NULL,
    `version` BIGINT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_locale_key` (`tenant_id`, `locale`, `text_key`),
    INDEX `idx_i18n_lookup` (`tenant_id`, `locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 21. Bảng Đánh giá & Bình luận Đa năng (Entity Reviews)
CREATE TABLE IF NOT EXISTS `entity_reviews` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL,
    `entity_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `parent_id` BIGINT NULL,
    `rating_score` TINYINT NOT NULL DEFAULT 5,
    `comment` TEXT NOT NULL,
    `media_attachments` JSON NULL,
    `is_approved` BOOLEAN NOT NULL DEFAULT TRUE,
    `like_count` INT NOT NULL DEFAULT 0,
    `version` BIGINT NOT NULL DEFAULT 0,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `deleted_at` TIMESTAMP NULL,
    `deleted_by` BIGINT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_review_lookup` (`tenant_id`, `entity_id`, `is_approved`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 30. Bảng Tích hợp Dịch vụ & API Bên Ngoài
CREATE TABLE IF NOT EXISTS `external_api_integrations` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    `service_code` VARCHAR(50) NOT NULL,
    `provider_name` VARCHAR(100) NOT NULL,
    `base_url` VARCHAR(500) NOT NULL,
    `config_payload` JSON NOT NULL,
    `cache_ttl_seconds` INT NOT NULL DEFAULT 3600,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `version` BIGINT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_service` (`tenant_id`, `service_code`),
    INDEX `idx_service_lookup` (`tenant_id`, `service_code`, `is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
