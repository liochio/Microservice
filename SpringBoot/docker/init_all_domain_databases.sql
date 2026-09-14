-- ==============================================================================
-- Liochio FinTech Platform - MySQL Initialization Script (Docker & Local)
-- Khởi tạo toàn bộ hệ sinh thái Database chuẩn Microservices (Database-per-Service)
-- ==============================================================================

-- 1. Database Quản trị Định danh & Xác thực (Pure IAM & Compliance)
CREATE DATABASE IF NOT EXISTS `liochio_core_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. Database Sổ cái Kế toán kép Bất biến (Core Banking General Ledger)
CREATE DATABASE IF NOT EXISTS `liochio_ledger_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3. Database Thực thể Động, Menu Phân quyền, Headless CMS & UI Config Matrix
CREATE DATABASE IF NOT EXISTS `liochio_entity_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 4. Database Cổng Thanh toán, Đặt cọc & Giao dịch Booking
CREATE DATABASE IF NOT EXISTS `liochio_payment_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 5. Database Quản lý Thông báo Đa kênh & Audit Log
CREATE DATABASE IF NOT EXISTS `liochio_notification_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 6. Database Cổng Ứng dụng Tiện ích, IoT Heo Đất & AI Biometrics (Python FastAPI)
CREATE DATABASE IF NOT EXISTS `liochio_app_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Dọn dẹp dứt điểm các Database cũ không còn sử dụng
DROP DATABASE IF EXISTS `db_tour`;
DROP DATABASE IF EXISTS `db_film`;
DROP DATABASE IF EXISTS `db_media`;
DROP DATABASE IF EXISTS `db_music`;
DROP DATABASE IF EXISTS `db_gaming`;
DROP DATABASE IF EXISTS `db_blog`;
DROP DATABASE IF EXISTS `db_content_eav`;
DROP DATABASE IF EXISTS `db_ai_vector`;
DROP DATABASE IF EXISTS `portfolio-engine`;
DROP DATABASE IF EXISTS `liochio_auth_db`;

-- Cấp toàn quyền cho root user truy cập từ mọi host
GRANT ALL PRIVILEGES ON `liochio_%`.* TO 'root'@'%';
FLUSH PRIVILEGES;
