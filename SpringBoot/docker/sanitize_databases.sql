-- ==============================================================================
-- SCRIPT DỌN DẸP BẢNG RÁC VÀ ĐỒNG BỘ KIẾN TRÚC TOÀN DIỆN (ZERO RESIDUAL DEBT)
-- ==============================================================================

-- 1. DỌN DẸP LIOCHIO_CORE_DB (Chỉ giữ IAM & Compliance)
USE `liochio_core_db`;

-- Xóa các bảng Sổ cái đã chuyển sang liochio_ledger_db
DROP TABLE IF EXISTS `journal_entry_details`;
DROP TABLE IF EXISTS `journal_entries`;
DROP TABLE IF EXISTS `ledger_accounts`;

-- Xóa các bảng Menu & Config đã chuyển sang liochio_entity_db
DROP TABLE IF EXISTS `tenant_menus`;
DROP TABLE IF EXISTS `menu_i18n`;
DROP TABLE IF EXISTS `master_menus`;
DROP TABLE IF EXISTS `master_permissions`;
DROP TABLE IF EXISTS `tenant_config_overrides`;
DROP TABLE IF EXISTS `global_system_configs`;

-- 2. DỌN DẸP LIOCHIO_APP_DB (Python App Database)
USE `liochio_app_db`;

-- Xóa các bảng Model Sổ cái trùng lặp / Zombie
DROP TABLE IF EXISTS `journal_entry_details`;
DROP TABLE IF EXISTS `journal_entries`;
DROP TABLE IF EXISTS `ledger_accounts`;
DROP TABLE IF EXISTS `accounting_periods`;
DROP TABLE IF EXISTS `reconciliation_logs`;
DROP TABLE IF EXISTS `ledger_entries`;

-- 3. XÓA CÁC SCHEMA CŨ
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

SELECT '✅ ĐÃ DỌN DẸP TOÀN BỘ CƠ SỞ DỮ LIỆU THÀNH CÔNG!' AS status;
