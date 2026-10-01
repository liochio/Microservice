import pymysql
import sys

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')

conn = pymysql.connect(host='localhost', port=3306, user='root', password='12345678')
cursor = conn.cursor()

# 1. Nạp liochio_app_db.system_settings
app_configs = [
    ('iot.allow_hardware_bypass', 'true', 'IOT', 'ACTIVE', 'Cho phép kiểm thử và mô phỏng API IoT khi chưa kết nối phần cứng ESP32 thật'),
    ('iot.virtual_device_auto_provision', 'true', 'IOT', 'ACTIVE', 'Tự động khởi tạo thiết bị Heo Đất ảo trong DB khi gọi API nếu chưa ghép đôi phần cứng'),
    ('otp.allow_dev_bypass', 'true', 'SECURITY', 'ACTIVE', 'Cho phép dùng OTP demo 123456 trong môi trường dev/staging'),
    ('mail.fallback_to_default_recipient', 'true', 'MAIL', 'ACTIVE', 'Tự động chuyển tiếp email ảo về email mặc định'),
    ('mail.default_recipient', 'voduylebt99@gmail.com', 'MAIL', 'ACTIVE', 'Hòm thư nhận email chuyển tiếp mặc định')
]

for key, val, c_type, status, desc in app_configs:
    cursor.execute("""
        INSERT INTO liochio_app_db.system_settings (`key`, `value`, `type`, `status`, `description`)
        VALUES (%s, %s, %s, %s, %s)
        ON DUPLICATE KEY UPDATE `value`=VALUES(`value`), `status`='ACTIVE', `description`=VALUES(`description`), updated_at=NOW()
    """, (key, val, c_type, status, desc))

# 2. Nạp liochio_core_db.system_configs
core_configs = [
    ('iot.allow_hardware_bypass', 'true', 'Cho phép kiểm thử và mô phỏng API IoT khi chưa kết nối phần cứng ESP32 thật'),
    ('iot.virtual_device_auto_provision', 'true', 'Tự động khởi tạo thiết bị Heo Đất ảo trong DB khi gọi API nếu chưa ghép đôi phần cứng'),
    ('otp.allow_dev_bypass', 'true', 'Cho phép dùng OTP demo 123456 trong môi trường dev/staging'),
    ('mail.fallback_to_default_recipient', 'true', 'Tự động chuyển tiếp email ảo về email mặc định'),
    ('mail.default_recipient', 'voduylebt99@gmail.com', 'Hòm thư nhận email chuyển tiếp mặc định')
]

for key, val, desc in core_configs:
    cursor.execute("""
        INSERT INTO liochio_core_db.system_configs 
        (config_key, config_value, created_at, description, env_profile, is_active, is_sensitive, service_name, updated_at)
        VALUES (%s, %s, NOW(6), %s, 'DEFAULT', b'1', b'0', 'core-platform', NOW(6))
        ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), updated_at = NOW(6), is_active = b'1'
    """, (key, val, desc))

conn.commit()
print("=" * 60)
print("SUCCESS: CẤU HÌNH IOT BYPASS & SYSTEM SETTINGS ĐÃ NẠP THÀNH CÔNG VÀO DB!")
print("=" * 60)

cursor.execute("SELECT `key`, `value`, `type`, `status` FROM liochio_app_db.system_settings WHERE `type` IN ('IOT', 'SECURITY', 'MAIL')")
print("\n[liochio_app_db.system_settings]:")
for r in cursor.fetchall():
    print(" ", r)

cursor.close()
conn.close()
