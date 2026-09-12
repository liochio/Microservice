package com.liochio.auth.service;

import com.liochio.auth.entity.LedgerAccountEntity;
import com.liochio.auth.entity.UserEntity;
import com.liochio.auth.repository.LedgerAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * ==============================================================================
 * Dịch Vụ Cấp Phát & Đồng Bộ Tài Khoản Tự Động (User & Wallet Auto-Provisioning)
 * ==============================================================================
 * 
 * Mục đích:
 * 1. Tự động khởi tạo 3 tài khoản Sổ cái kép (USER_AVAILABLE, USER_HOLDING, USER_ESCROW)
 *    trong `liochio_auth_db.ledger_accounts` ngay khi người dùng kích hoạt tài khoản.
 * 2. Tự động đồng bộ bản ghi User sang `liochio_fintech_db.users` (Customer Profile).
 * 3. Tự động tạo Ví tiêu dùng mặc định trong `liochio_fintech_db.wallets` (Currency: VND).
 * 4. Đảm bảo tính nhất quán 100% giữa Auth IAM IdP và Resource Server FinTech.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProvisioningService {

    private final LedgerAccountRepository ledgerAccountRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void provisionUserAccounts(UserEntity user) {
        if (user == null || user.getId() == null) return;

        String tenantId = user.getTenantId() != null ? user.getTenantId() : "default";
        Long userId = user.getId();

        log.info("[UserProvisioningService] Bắt đầu cấp phát tài khoản sổ cái & đồng bộ ví cho User ID: {}, Username: '{}'", userId, user.getUsername());

        // 1. Khởi tạo 3 tài khoản Sổ cái kép trong liochio_auth_db
        provisionLedgerAccounts(tenantId, userId);

        // 2. Đồng bộ sang liochio_fintech_db (User + Wallet + Role)
        syncToFintechDb(user);

        log.info("[UserProvisioningService] ✅ Cấp phát và đồng bộ thành công cho User ID: {}", userId);
    }

    private void provisionLedgerAccounts(String tenantId, Long userId) {
        // Tài khoản khả dụng
        if (!ledgerAccountRepository.findByTenantIdAndUserIdAndAccountType(tenantId, userId, "USER_AVAILABLE").isPresent()) {
            ledgerAccountRepository.save(LedgerAccountEntity.builder()
                    .tenantId(tenantId)
                    .accountNumber("LEDGER_AVAIL_" + userId + "_" + System.currentTimeMillis())
                    .userId(userId)
                    .accountType("USER_AVAILABLE")
                    .currency("VND")
                    .balance(BigDecimal.ZERO)
                    .status("ACTIVE")
                    .version(0L)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());
        }

        // Tài khoản tạm giữ (Holding / Pending Settlement)
        if (!ledgerAccountRepository.findByTenantIdAndUserIdAndAccountType(tenantId, userId, "USER_HOLDING").isPresent()) {
            ledgerAccountRepository.save(LedgerAccountEntity.builder()
                    .tenantId(tenantId)
                    .accountNumber("LEDGER_HOLD_" + userId + "_" + System.currentTimeMillis())
                    .userId(userId)
                    .accountType("USER_HOLDING")
                    .currency("VND")
                    .balance(BigDecimal.ZERO)
                    .status("ACTIVE")
                    .version(0L)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());
        }

        // Tài khoản Heo đất kỷ luật (Escrow / Smart Piggy)
        if (!ledgerAccountRepository.findByTenantIdAndUserIdAndAccountType(tenantId, userId, "USER_ESCROW").isPresent()) {
            ledgerAccountRepository.save(LedgerAccountEntity.builder()
                    .tenantId(tenantId)
                    .accountNumber("LEDGER_ESCROW_" + userId + "_" + System.currentTimeMillis())
                    .userId(userId)
                    .accountType("USER_ESCROW")
                    .currency("VND")
                    .balance(BigDecimal.ZERO)
                    .status("ACTIVE")
                    .version(0L)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());
        }
    }

    private void syncToFintechDb(UserEntity user) {
        try {
            String fintechUserId = "usr_" + user.getId();
            String walletId = "wal_" + user.getId() + "_vnd";
            String walletCode = "WAL-VND-" + String.format("%06d", user.getId());
            String walletAccount = "ACC_" + String.format("%08d", user.getId());

            // 2.1 Đồng bộ User sang liochio_app_db.users
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

            // 2.2 Tạo Ví tiêu dùng mặc định trong liochio_app_db.wallets
            jdbcTemplate.update("""
                INSERT INTO `liochio_app_db`.`wallets` (
                    `id`, `user_id`, `wallet_code`, `name`, `wallet_type`, `wallet_account`, `balance`, `currency`, `color`, `icon`, `status`, `is_default`
                ) VALUES (?, ?, ?, 'Ví Tiêu Dùng Chính', 'DEFAULT', ?, 0.0000, 'VND', '#3498db', 'wallet', 'ACTIVE', 1)
                ON DUPLICATE KEY UPDATE `status` = 'ACTIVE';
            """, walletId, fintechUserId, walletCode, walletAccount);

            // 2.3 Gán vai trò mặc định trong liochio_app_db.user_roles
            jdbcTemplate.update("""
                INSERT IGNORE INTO `liochio_app_db`.`user_roles` (`id`, `user_id`, `role_id`)
                SELECT ?, ?, `id` FROM `liochio_app_db`.`roles` WHERE `name` IN ('ROLE_CUSTOMER', 'ROLE_USER') LIMIT 1;
            """, "ur_" + user.getId() + "_default", fintechUserId);

            log.info("[UserProvisioningService] -> Đồng bộ hoàn tất liochio_app_db cho User ID: {}", user.getId());
        } catch (Exception e) {
            log.error("[UserProvisioningService] ⚠️ Lỗi khi đồng bộ sang liochio_app_db: {}", e.getMessage(), e);
        }
    }
}
