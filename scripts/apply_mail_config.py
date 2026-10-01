import pymysql
import sys

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')
conn = pymysql.connect(host='localhost', port=3306, user='root', password='12345678')
cursor = conn.cursor()

# 1. Update liochio_app_db.system_settings
app_configs = [
    ('smtp.host', 'smtp.gmail.com', 'MAIL', 'ACTIVE', 'Máy chủ gửi mail SMTP (Gmail)'),
    ('smtp.port', '587', 'MAIL', 'ACTIVE', 'Cổng kết nối TLS SMTP'),
    ('smtp.username', 'voduylebt99@gmail.com', 'MAIL', 'ACTIVE', 'Tài khoản người gửi SMTP Gmail'),
    ('smtp.password', 'uaennreskbytgkjt', 'MAIL', 'ACTIVE', 'Mật khẩu ứng dụng 16 ký tự Gmail'),
    ('smtp.from_email', 'voduylebt99@gmail.com', 'MAIL', 'ACTIVE', 'Địa chỉ email người gửi hiển thị'),
    ('mail.fallback_to_default_recipient', 'true', 'MAIL', 'ACTIVE', 'Cho phép gửi mail mặc định khi email người nhận không có thật/test (mặc định có)'),
    ('mail.default_recipient', 'voduylebt99@gmail.com', 'MAIL', 'ACTIVE', 'Email mặc định tiếp nhận thông báo và mã OTP thay thế')
]

for key, val, c_type, status, desc in app_configs:
    cursor.execute("""
        INSERT INTO liochio_app_db.system_settings (`key`, `value`, `type`, `status`, `description`)
        VALUES (%s, %s, %s, %s, %s)
        ON DUPLICATE KEY UPDATE `value`=VALUES(`value`), `status`='ACTIVE', `description`=VALUES(`description`), updated_at=NOW()
    """, (key, val, c_type, status, desc))

# 2. Update liochio_core_db.system_configs
core_configs = [
    ('smtp.host', 'smtp.gmail.com', 'Máy chủ gửi mail SMTP (Gmail)'),
    ('smtp.port', '587', 'Cổng kết nối TLS SMTP'),
    ('smtp.username', 'voduylebt99@gmail.com', 'Tài khoản người gửi SMTP Gmail'),
    ('smtp.password', 'uaennreskbytgkjt', 'Mật khẩu ứng dụng 16 ký tự Gmail'),
    ('smtp.from_email', 'voduylebt99@gmail.com', 'Địa chỉ email người gửi hiển thị'),
    ('mail.fallback_to_default_recipient', 'true', 'Cho phép gửi mail mặc định khi email người nhận không có thật/test (mặc định có)'),
    ('mail.default_recipient', 'voduylebt99@gmail.com', 'Email mặc định tiếp nhận thông báo và mã OTP thay thế')
]

for key, val, desc in core_configs:
    cursor.execute("""
        INSERT INTO liochio_core_db.system_configs 
        (config_key, config_value, created_at, description, env_profile, is_active, is_sensitive, service_name, updated_at)
        VALUES (%s, %s, NOW(6), %s, 'DEFAULT', b'1', b'0', 'worker-service', NOW(6))
        ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), updated_at = NOW(6), is_active = b'1'
    """, (key, val, desc))

conn.commit()
print("=" * 60)
print("SUCCESS: CẬP NHẬT CẤU HÌNH MAIL & FALLBACK DATABASE HOÀN TẤT!")
print("=" * 60)

cursor.execute("SELECT `key`, `value`, `status` FROM liochio_app_db.system_settings WHERE `type` = 'MAIL'")
print("\n[liochio_app_db.system_settings]:")
for r in cursor.fetchall():
    print(" ", r)

cursor.execute("SELECT config_key, config_value, is_active FROM liochio_core_db.system_configs WHERE config_key LIKE 'smtp%' OR config_key LIKE 'mail%'")
print("\n[liochio_core_db.system_configs]:")
for r in cursor.fetchall():
    print(" ", r)

conn.close()
