-- ==============================================================================
-- Migration V3: Cập nhật mật khẩu chuẩn BCrypt cho superadmin (12345678)
-- ==============================================================================

UPDATE `users` 
SET `password` = '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a' 
WHERE `username` = 'superadmin';
