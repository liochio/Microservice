USE `portfolio-engine`;

-- Helper procedure to add column safely
DROP PROCEDURE IF EXISTS AddColumnSafely;
DELIMITER $$
CREATE PROCEDURE AddColumnSafely(IN tableName VARCHAR(64), IN colName VARCHAR(64), IN colDef VARCHAR(255))
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'portfolio-engine' 
          AND TABLE_NAME = tableName 
          AND COLUMN_NAME = colName
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', tableName, '` ADD COLUMN `', colName, '` ', colDef);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- permissions
CALL AddColumnSafely('permissions', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL AddColumnSafely('permissions', 'deleted_at', 'TIMESTAMP NULL');
CALL AddColumnSafely('permissions', 'deleted_by', 'BIGINT NULL');

-- roles
CALL AddColumnSafely('roles', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL AddColumnSafely('roles', 'deleted_at', 'TIMESTAMP NULL');
CALL AddColumnSafely('roles', 'deleted_by', 'BIGINT NULL');

-- users
CALL AddColumnSafely('users', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL AddColumnSafely('users', 'deleted_at', 'TIMESTAMP NULL');
CALL AddColumnSafely('users', 'deleted_by', 'BIGINT NULL');

-- bookings
CALL AddColumnSafely('bookings', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL AddColumnSafely('bookings', 'deleted_at', 'TIMESTAMP NULL');
CALL AddColumnSafely('bookings', 'deleted_by', 'BIGINT NULL');

-- tenant_payment_configs
CALL AddColumnSafely('tenant_payment_configs', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL AddColumnSafely('tenant_payment_configs', 'deleted_at', 'TIMESTAMP NULL');
CALL AddColumnSafely('tenant_payment_configs', 'deleted_by', 'BIGINT NULL');

-- payment_transactions
CALL AddColumnSafely('payment_transactions', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL AddColumnSafely('payment_transactions', 'deleted_at', 'TIMESTAMP NULL');
CALL AddColumnSafely('payment_transactions', 'deleted_by', 'BIGINT NULL');

-- media_assets
CALL AddColumnSafely('media_assets', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL AddColumnSafely('media_assets', 'deleted_at', 'TIMESTAMP NULL');
CALL AddColumnSafely('media_assets', 'deleted_by', 'BIGINT NULL');

-- media_files
CALL AddColumnSafely('media_files', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL AddColumnSafely('media_files', 'deleted_at', 'TIMESTAMP NULL');
CALL AddColumnSafely('media_files', 'deleted_by', 'BIGINT NULL');

-- media_chunk_uploads
CALL AddColumnSafely('media_chunk_uploads', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL AddColumnSafely('media_chunk_uploads', 'deleted_at', 'TIMESTAMP NULL');
CALL AddColumnSafely('media_chunk_uploads', 'deleted_by', 'BIGINT NULL');

-- notifications
CALL AddColumnSafely('notifications', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL AddColumnSafely('notifications', 'deleted_at', 'TIMESTAMP NULL');
CALL AddColumnSafely('notifications', 'deleted_by', 'BIGINT NULL');

-- notification_templates
CALL AddColumnSafely('notification_templates', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL AddColumnSafely('notification_templates', 'deleted_at', 'TIMESTAMP NULL');
CALL AddColumnSafely('notification_templates', 'deleted_by', 'BIGINT NULL');

-- tenant_notification_configs
CALL AddColumnSafely('tenant_notification_configs', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL AddColumnSafely('tenant_notification_configs', 'deleted_at', 'TIMESTAMP NULL');
CALL AddColumnSafely('tenant_notification_configs', 'deleted_by', 'BIGINT NULL');

DROP PROCEDURE IF EXISTS AddColumnSafely;
