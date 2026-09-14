package com.liochio.auth.service;

import com.liochio.auth.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ==============================================================================
 * Dịch Vụ Cấp Phát & Đồng Bộ Tài Khoản Người Dùng (IAM Pure Provisioning)
 * ==============================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProvisioningService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void provisionUserAccounts(UserEntity user) {
        if (user == null || user.getId() == null) return;

        Long userId = user.getId();
        log.info("[UserProvisioningService] Bắt đầu cấp phát & đồng bộ tài khoản cho User ID: {}, Username: '{}'", userId, user.getUsername());

        // Đồng bộ sang liochio_app_db (User + Wallet + Role)
        syncToFintechDb(user);

        log.info("[UserProvisioningService] Cấp phát và đồng bộ thành công cho User ID: {}", userId);
    }

    private void syncToFintechDb(UserEntity user) {
        try {
            String fintechUserId = "usr_" + user.getId();
            String walletId = "wal_" + user.getId() + "_vnd";
            String walletCode = "WAL-VND-" + String.format("%06d", user.getId());
            String walletAccount = "ACC_" + String.format("%08d", user.getId());

            // 1. Đồng bộ User sang liochio_app_db.users
            jdbcTemplate.update("""
                INSERT INTO `liochio_app_db`.`users` (
                    `id`, `username`, `email`, `phone_number`, `password_hash`, `full_name`, `status`, `is_active`, `is_verified`, `created_at`
                ) VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE', 1, 1, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                    `email` = VALUES(`email`),
                    `phone_number` = VALUES(`phone_number`),
                    `full_name` = VALUES(`full_name`),
                    `status` = 'ACTIVE',
                    `is_active` = 1,
                    `is_verified` = 1;
            """, fintechUserId, user.getUsername(), user.getEmail(), user.getPhone(), user.getPassword(), user.getFullName());

            // 2. Tạo Ví tiêu dùng mặc định trong liochio_app_db.wallets
            jdbcTemplate.update("""
                INSERT INTO `liochio_app_db`.`wallets` (
                    `id`, `user_id`, `wallet_code`, `name`, `wallet_type`, `wallet_account`, `balance`, `currency`, `color`, `icon`, `status`, `is_default`
                ) VALUES (?, ?, ?, 'Ví Tiêu Dùng Chính', 'DEFAULT', ?, 0.0000, 'VND', '#3498db', 'wallet', 'ACTIVE', 1)
                ON DUPLICATE KEY UPDATE `status` = 'ACTIVE';
            """, walletId, fintechUserId, walletCode, walletAccount);

            // 3. Gán vai trò mặc định trong liochio_app_db.user_roles
            jdbcTemplate.update("""
                INSERT IGNORE INTO `liochio_app_db`.`user_roles` (`id`, `user_id`, `role_id`)
                SELECT ?, ?, `id` FROM `liochio_app_db`.`roles` WHERE `name` IN ('ROLE_CUSTOMER', 'ROLE_USER') LIMIT 1;
            """, "ur_" + user.getId() + "_default", fintechUserId);

            log.info("[UserProvisioningService] -> Đồng bộ hoàn tất liochio_app_db cho User ID: {}", user.getId());
        } catch (Exception e) {
            log.error("[UserProvisioningService] Lỗi khi đồng bộ sang liochio_app_db: {}", e.getMessage(), e);
        }
    }
}
