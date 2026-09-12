-- ==============================================================================
-- Liochio Microservices Engine - Khởi tạo toàn bộ Database Chuyên Biệt (liochio_*_db)
-- Kiến trúc: Microservices / Database-per-Service
-- ==============================================================================

-- 1. Database Quản trị Hệ thống, IAM, Xác thực & Phân quyền Core (Auth + OTP + Entity)
CREATE DATABASE IF NOT EXISTS `liochio_core_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. Database Cổng Ứng Dụng Hợp Nhất (FinTech, B2B, Retail, Payments, Workers, Notifications)
CREATE DATABASE IF NOT EXISTS `liochio_app_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Cấp quyền truy cập cho user MySQL
GRANT ALL PRIVILEGES ON `liochio_%`.* TO 'root'@'%';
FLUSH PRIVILEGES;
