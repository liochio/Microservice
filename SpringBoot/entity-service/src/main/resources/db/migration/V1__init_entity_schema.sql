-- ==============================================================================
-- Migration V1: Khởi tạo bảng dữ liệu cho Entity Service & Dynamic Engine
-- ==============================================================================

-- 2. Bảng Quản lý Thực thể Động (Hybrid EAV + JSONB: dynamic_entities)
CREATE TABLE IF NOT EXISTS `dynamic_entities` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `entity_type` VARCHAR(100) NOT NULL,
    `slug` VARCHAR(200) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `attributes` JSON NOT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED',
    `view_count` BIGINT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY `uk_dynamic_tenant_type_slug` (`tenant_id`, `entity_type`, `slug`),
    INDEX `idx_dynamic_type_status` (`tenant_id`, `entity_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng Cấu hình Giao diện Động Server-Driven UI (ui_configurations)
CREATE TABLE IF NOT EXISTS `ui_configurations` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `page_code` VARCHAR(100) NOT NULL,
    `theme_name` VARCHAR(50) NOT NULL DEFAULT 'default_theme',
    `layout_schema` JSON NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY `uk_ui_tenant_page` (`tenant_id`, `page_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng Định nghĩa Form Nhập liệu Động (form_definitions)
CREATE TABLE IF NOT EXISTS `form_definitions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `form_code` VARCHAR(100) NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `field_definitions` JSON NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY `uk_form_tenant_code` (`tenant_id`, `form_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bảng Cấu hình Menu & Navigation Động (navigation_menus)
CREATE TABLE IF NOT EXISTS `navigation_menus` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tenant_id` VARCHAR(50) NOT NULL DEFAULT 'default',
    `menu_code` VARCHAR(100) NOT NULL,
    `title` VARCHAR(150) NOT NULL,
    `items` JSON NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `last_modified_by` VARCHAR(100) DEFAULT 'SYSTEM',
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY `uk_menu_tenant_code` (`tenant_id`, `menu_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- DỮ LIỆU KHỞI TẠO MẶC ĐỊNH (SEED DATA)
-- ==============================================================================

-- Seed UI Layout Mẫu cho Trang chủ (Home Page)
INSERT IGNORE INTO `ui_configurations` (`id`, `tenant_id`, `page_code`, `theme_name`, `layout_schema`) VALUES
(1, 'default', 'home_page', 'modern_dark', JSON_OBJECT(
    'theme', 'modern_dark',
    'sections', JSON_ARRAY(
        JSON_OBJECT('type', 'HeroSection', 'order', 1, 'props', JSON_OBJECT('headline', 'Xin chào, Tôi là Lập trình viên Backend', 'subheadline', 'Chuyên gia xây dựng hệ thống phân tán hiệu năng cao')),
        JSON_OBJECT('type', 'ProjectsSection', 'order', 2, 'props', JSON_OBJECT('title', 'Dự án Tiêu biểu', 'displayMode', 'grid')),
        JSON_OBJECT('type', 'ContactSection', 'order', 3, 'props', JSON_OBJECT('title', 'Liên hệ Hợp tác', 'email', 'contact@portfolio-engine.dev'))
    )
));

-- Seed Navigation Menu Mẫu
INSERT IGNORE INTO `navigation_menus` (`id`, `tenant_id`, `menu_code`, `title`, `items`) VALUES
(1, 'default', 'main_header_menu', 'Main Header Navigation', JSON_ARRAY(
    JSON_OBJECT('label', 'Trang chủ', 'url', '/', 'order', 1),
    JSON_OBJECT('label', 'Dự án', 'url', '/projects', 'order', 2),
    JSON_OBJECT('label', 'Kinh nghiệm', 'url', '/experience', 'order', 3),
    JSON_OBJECT('label', 'Liên hệ', 'url', '/contact', 'order', 4)
));
