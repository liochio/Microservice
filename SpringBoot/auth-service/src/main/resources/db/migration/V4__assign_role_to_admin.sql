-- ==============================================================================
-- Migration V4: Gán quyền ROLE_SUPER_ADMIN cho tài khoản admin
-- ==============================================================================

INSERT IGNORE INTO `user_roles` (`user_id`, `role_id`)
SELECT u.id, r.id 
FROM `users` u, `roles` r 
WHERE u.username = 'admin' AND r.role_name = 'ROLE_SUPER_ADMIN';
