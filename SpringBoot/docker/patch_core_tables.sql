-- Patch all core tables in portfolio-engine with BaseEntity columns
USE `portfolio-engine`;

-- permissions
ALTER TABLE `permissions`
    ADD COLUMN IF NOT EXISTS `version` BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS `deleted_at` TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS `deleted_by` BIGINT NULL;

-- roles
ALTER TABLE `roles`
    ADD COLUMN IF NOT EXISTS `version` BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS `deleted_at` TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS `deleted_by` BIGINT NULL;

-- users
ALTER TABLE `users`
    ADD COLUMN IF NOT EXISTS `version` BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS `deleted_at` TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS `deleted_by` BIGINT NULL;

-- bookings
ALTER TABLE `bookings`
    ADD COLUMN IF NOT EXISTS `version` BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS `deleted_at` TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS `deleted_by` BIGINT NULL;

-- tenant_payment_configs
ALTER TABLE `tenant_payment_configs`
    ADD COLUMN IF NOT EXISTS `version` BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS `deleted_at` TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS `deleted_by` BIGINT NULL;

-- payment_transactions
ALTER TABLE `payment_transactions`
    ADD COLUMN IF NOT EXISTS `version` BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS `deleted_at` TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS `deleted_by` BIGINT NULL;

-- media_assets
ALTER TABLE `media_assets`
    ADD COLUMN IF NOT EXISTS `version` BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS `deleted_at` TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS `deleted_by` BIGINT NULL;

-- media_files
ALTER TABLE `media_files`
    ADD COLUMN IF NOT EXISTS `version` BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS `deleted_at` TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS `deleted_by` BIGINT NULL;

-- media_chunk_uploads
ALTER TABLE `media_chunk_uploads`
    ADD COLUMN IF NOT EXISTS `version` BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS `deleted_at` TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS `deleted_by` BIGINT NULL;

-- notifications
ALTER TABLE `notifications`
    ADD COLUMN IF NOT EXISTS `version` BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS `deleted_at` TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS `deleted_by` BIGINT NULL;

-- notification_templates
ALTER TABLE `notification_templates`
    ADD COLUMN IF NOT EXISTS `version` BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS `deleted_at` TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS `deleted_by` BIGINT NULL;

-- tenant_notification_configs
ALTER TABLE `tenant_notification_configs`
    ADD COLUMN IF NOT EXISTS `version` BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS `deleted_at` TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS `deleted_by` BIGINT NULL;
