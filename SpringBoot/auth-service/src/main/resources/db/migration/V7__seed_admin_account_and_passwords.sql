-- ==============================================================================
-- Migration V7: Seed Admin Account và Thiết lập Mật khẩu Mặc định cho db_auth
-- ==============================================================================

-- 1. Đảm bảo tài khoản admin tồn tại trong bảng users
INSERT IGNORE INTO `users` (`id`, `tenant_id`, `username`, `password`, `email`, `full_name`, `status`, `user_type`, `is_email_verified`)
VALUES (2, 'default', 'admin', '$2a$10$jyKx9jwJWRKV0CTyHmFGD.R/L.0vIvHdqTBjtM3Cp.nShSkfCNVf.', 'admin@portfolio.com', 'System Administrator', 'ACTIVE', 'SUPER_ADMIN', TRUE);

-- 2. Cập nhật mật khẩu chuẩn 'password123' cho cả superadmin và admin
UPDATE `users` 
SET `password` = '$2a$10$jyKx9jwJWRKV0CTyHmFGD.R/L.0vIvHdqTBjtM3Cp.nShSkfCNVf.',
    `status` = 'ACTIVE',
    `failed_login_attempts` = 0,
    `lockout_until` = NULL
WHERE `username` IN ('superadmin', 'admin');

-- 3. Gán Role Super Admin cho admin
INSERT IGNORE INTO `user_roles` (`user_id`, `role_id`)
SELECT u.`id`, r.`id`
FROM `users` u, `roles` r
WHERE u.`username` = 'admin' AND r.`role_name` = 'ROLE_SUPER_ADMIN';
