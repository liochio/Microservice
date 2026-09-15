import mysql.connector
import sys

sys.stdout.reconfigure(encoding='utf-8')

DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "12345678"
}

EXPECTED_DATABASES = [
    "db_auth", "db_otp", "db_entity", "db_payment", "db_media",
    "db_notification", "db_tour", "db_music", "db_film", "db_ai", "db_worker"
]

def audit():
    print("=" * 80)
    print("🔍 BẮT ĐẦU RÀ SOÁT TẤT CẢ CƠ SỞ DỮ LIỆU CHUYÊN BIỆT (db_*) VÀ BẢNG")
    print("=" * 80)
    
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    cursor.execute("SHOW DATABASES LIKE 'db_%'")
    existing_dbs = [row[0] for row in cursor.fetchall()]
    print(f"\n[1] Danh sách các DB hiện có bắt đầu bằng 'db_': {existing_dbs}")
    
    for db in EXPECTED_DATABASES:
        if db in existing_dbs:
            cursor.execute(f"SHOW TABLES FROM '{db}'")
            tables = [r[0] for r in cursor.fetchall()]
            print(f"\n📂 Database: '{db}' -> {len(tables)} bảng:")
            for t in tables:
                print(f"   ├─ {t}")
        else:
            print(f"\n⚠️ Database: '{db}' -> Chưa tồn tại")
            
    print("\n" + "-" * 80)
    print("🛡️ KIỂM TRA PHÂN LẬP & TÁCH BẢNG RÁC:")
    
    # Kiểm tra db_auth không được chứa user_otp_verifications
    cursor.execute("SHOW TABLES FROM db_auth LIKE 'user_otp_verifications'")
    otp_in_auth = cursor.fetchall()
    if not otp_in_auth:
        print("  ✅ db_auth: Đã dọn dẹp sạch sẽ, không còn bảng 'user_otp_verifications'")
    else:
        print("  ❌ db_auth: Vẫn còn bảng 'user_otp_verifications'")
        
    # Kiểm tra db_otp có bảng otp_service_configs và user_otp_verifications
    cursor.execute("SHOW TABLES FROM db_otp")
    otp_tables = [r[0] for r in cursor.fetchall()]
    if "otp_service_configs" in otp_tables and "user_otp_verifications" in otp_tables:
        print("  ✅ db_otp: Đầy đủ các bảng 'otp_service_configs' và 'user_otp_verifications'")
    else:
        print(f"  ❌ db_otp: Thiếu bảng, hiện có: {otp_tables}")

    cursor.close()
    conn.close()
    print("\n" + "=" * 80)
    print("🎉 RÀ SOÁT HOÀN TẤT THÀNH CÔNG!")
    print("=" * 80)

if __name__ == "__main__":
    audit()
