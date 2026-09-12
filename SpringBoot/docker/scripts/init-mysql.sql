-- ==============================================================================
-- Portfolio Backend Engine - MySQL Initialization Script (Docker & Local)
-- Khởi tạo toàn bộ hệ sinh thái 8 Databases theo mô hình Database-per-Domain
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS `portfolio-engine` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `db_content_eav`   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `db_tour`          CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `db_music`         CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `db_film`          CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `db_gaming`        CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `db_blog`          CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `db_ai_vector`     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Cấp toàn quyền cho root user truy cập từ mọi host
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
