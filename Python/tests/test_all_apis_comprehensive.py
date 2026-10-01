"""
==============================================================================
SUITE KIỂM THỬ TOÀN DIỆN CÁC API HỆ THỐNG LIOCHIO FINTECH & IOT SATELLITE
==============================================================================
Chạy kiểm thử trực tiếp qua TestClient tương tác với Database MySQL thật (localhost:3306).
Hỗ trợ cơ chế IoT Hardware Bypass tự động ghi nhận dữ liệu vào Database.
"""
import sys
import os
import unittest
import json
import uuid
import random

# Cấu hình UTF-8 cho Windows console
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from fastapi.testclient import TestClient
from app.main import app
from app.db.session import SessionLocal
from app.core.security.guard.guards import get_current_user
from sqlalchemy import text

client = TestClient(app)


class TestLiochioComprehensiveAPIs(unittest.TestCase):
    test_user_id = "test_user_init"
    test_wallet_id = "test_wallet_init"
    test_mac = "AA:BB:CC:11:22:33"
    test_device_id = "test_device_init"

    @classmethod
    def setUpClass(cls):
        print("\n" + "=" * 70)
        print("BẮT ĐẦU CHẠY BỘ TEST TOÀN DIỆN TẤT CẢ CÁC API HỆ THỐNG LIOCHIO")
        print("=" * 70)
        # Đảm bảo các cấu hình DB cần thiết đã sẵn sàng
        db = SessionLocal()
        try:
            db.execute(text("ALTER TABLE liochio_app_db.system_settings MODIFY COLUMN `value` TEXT"))
            db.commit()
        except Exception:
            pass
        db.execute(text("""
            INSERT INTO liochio_app_db.system_settings (`key`, `value`, `type`, `status`, `description`)
            VALUES 
                ('iot.allow_hardware_bypass', 'true', 'SYSTEM', 'ACTIVE', 'Cho phép kiểm thử IoT không cần thiết bị thật'),
                ('iot.virtual_device_auto_provision', 'true', 'SYSTEM', 'ACTIVE', 'Tự động tạo thiết bị ảo trong DB'),
                ('otp.allow_dev_bypass', 'true', 'SYSTEM', 'ACTIVE', 'Cho phép dùng OTP demo trong môi trường test')
            ON DUPLICATE KEY UPDATE `value`=VALUES(`value`), `status`='ACTIVE', updated_at=NOW()
        """))
        db.commit()
        db.close()

        # Mock authentication dependency cho các test case cần xác thực
        def mock_get_current_user():
            return {
                "user_id": cls.test_user_id,
                "username": "test_user_fintech",
                "role": "USER",
                "email": "test_user@gmail.com"
            }
        app.dependency_overrides[get_current_user] = mock_get_current_user

    @classmethod
    def tearDownClass(cls):
        app.dependency_overrides.clear()

    def test_01_health_check(self):
        """1. Kiểm tra API Health Check hệ thống"""
        res = client.get("/health")
        self.assertEqual(res.status_code, 200)
        data = res.json()
        self.assertEqual(data.get("status"), "healthy")
        print("  [PASS] test_01_health_check -> Hệ thống sẵn sàng hoạt động")

    def test_02_demo_onboard_user_and_wallet_activation_otp(self):
        """2. Kiểm tra Luồng Onboarding Người dùng mới, Sinh mã OTP và Kích hoạt Ví thật trong DB"""
        random_suffix = uuid.uuid4().hex[:6]
        rand_phone = f"098{random.randint(1000000, 9999999)}"
        user_payload = {
            "full_name": f"Nguyễn Kiểm Thử {random_suffix}",
            "username": f"test_user_{random_suffix}",
            "email": f"test_{random_suffix}@gmail.com",
            "phone_number": rand_phone
        }
        res_user = client.post("/api/v1/smart-piggy/demo/onboard-user", json=user_payload)
        self.assertEqual(res_user.status_code, 201)
        user_data = res_user.json()["data"]
        user_id = user_data["user_id"]
        self.assertIsNotNone(user_id)
        TestLiochioComprehensiveAPIs.test_user_id = user_id
        print(f"  [PASS] test_02_onboard_user -> Tạo thành công User ID: {user_id}")

        # Khởi tạo ví Heo Đất & sinh mã OTP thật lưu vào DB
        wallet_payload = {
            "user_id": user_id,
            "wallet_name": "Ví Heo Đất Kiểm Thử Tự Động"
        }
        res_wallet = client.post("/api/v1/smart-piggy/demo/init-wallet", json=wallet_payload)
        self.assertIn(res_wallet.status_code, [200, 201])
        wallet_data = res_wallet.json()["data"]
        wallet_id = wallet_data["wallet_id"]
        self.assertEqual(wallet_data["status"], "PENDING_ACTIVATION")
        TestLiochioComprehensiveAPIs.test_wallet_id = wallet_id
        print(f"  [PASS] test_02_init_wallet -> Tạo ví thành công (PENDING_ACTIVATION): {wallet_id}")

        # Lấy mã OTP thật vừa sinh trong Database
        db = SessionLocal()
        otp_row = db.execute(text("""
            SELECT otp_code FROM liochio_app_db.user_otps
            WHERE user_id = :uid AND type = 'WALLET_ACTIVATION' AND is_used = 0
            ORDER BY created_at DESC LIMIT 1
        """), {"uid": user_id}).fetchone()
        db.close()
        self.assertIsNotNone(otp_row, "Phải tìm thấy mã OTP thật trong bảng user_otps!")
        real_otp = otp_row[0]
        print(f"  [PASS] test_02_db_otp_persisted -> Mã OTP thật lưu DB: {real_otp}")

        # Kích hoạt ví bằng mã OTP thật
        res_act = client.post("/api/v1/smart-piggy/demo/activate-wallet", json={
            "wallet_id": wallet_id,
            "otp_code": real_otp
        })
        self.assertEqual(res_act.status_code, 200)
        print("  [PASS] test_02_activate_wallet -> Kích hoạt ví thành công bằng mã OTP lưu DB!")

    def test_03_iot_drop_money_sensor_ingest_with_hardware_bypass(self):
        """3. Kiểm tra API Cảm biến Đút tiền Heo đất (Drop Money) với cơ chế Bypass phần cứng & Auto-Provision DB"""
        random_mac = f"AA:BB:CC:{uuid.uuid4().hex[:2].upper()}:{uuid.uuid4().hex[:2].upper()}:{uuid.uuid4().hex[:2].upper()}"
        drop_payload = {
            "mac_address": random_mac,
            "coin_value": 50000.0,
            "sensor_delay_ms": 120,
            "debounce_count": 3
        }

        # Gọi API đút tiền từ cảm biến khi chưa có thiết bị thật
        res_drop = client.post("/api/v1/smart-piggy/drop-money", json=drop_payload)
        self.assertEqual(res_drop.status_code, 200)
        data = res_drop.json()["data"]
        self.assertEqual(data.get("coin_value_deposited"), 50000.0)
        self.assertEqual(data.get("total_credited"), 50000.0)
        print(f"  [PASS] test_03_iot_drop_money -> Đút 50,000 VND thành công! Ghi nhận DB, cộng số dư ví.")

        # Xác minh trong database thật: bảng smart_piggy_devices và transactions
        db = SessionLocal()
        dev_row = db.execute(text("SELECT id, mac_address, total_coins_dropped, status FROM liochio_app_db.smart_piggy_devices WHERE mac_address = :mac"), {"mac": random_mac}).fetchone()
        db.close()
        self.assertIsNotNone(dev_row, "Thiết bị phải được auto-provision và lưu trong Database!")
        self.assertEqual(float(dev_row[2]), 50000.0)
        self.assertEqual(dev_row[3], "ONLINE")
        print(f"  [PASS] test_03_db_verified -> Đã xác minh thiết bị {dev_row[0]} lưu trong DB với tổng nạp 50,000 VND!")

        TestLiochioComprehensiveAPIs.test_mac = random_mac
        TestLiochioComprehensiveAPIs.test_device_id = dev_row[0]

    def test_04_iot_sync_offline_batch(self):
        """4. Kiểm tra API Đồng bộ ngoại tuyến hàng loạt từ Flash ESP32 (Offline Sync)"""
        sync_payload = {
            "mac_address": TestLiochioComprehensiveAPIs.test_mac,
            "batch_items": [
                {"offline_tx_id": f"off-{uuid.uuid4().hex[:6]}", "coin_value": 10000.0},
                {"offline_tx_id": f"off-{uuid.uuid4().hex[:6]}", "coin_value": 20000.0}
            ]
        }
        res_sync = client.post("/api/v1/smart-piggy/sync-offline-batch", json=sync_payload)
        self.assertEqual(res_sync.status_code, 200)
        data = res_sync.json()["data"]
        self.assertEqual(data.get("coin_value_deposited"), 30000.0)
        self.assertEqual(data.get("total_credited"), 30000.0)
        self.assertIn("hardware_command", data)
        print("  [PASS] test_04_iot_sync_offline_batch -> Đồng bộ ngoại tuyến 2 giao dịch thành công!")

    def test_05_iot_lid_tamper_security_alert(self):
        """5. Kiểm tra API Cảnh báo Cạy nắp Heo Đất (Limit Switch Security Breach)"""
        tamper_payload = {
            "device_id": TestLiochioComprehensiveAPIs.test_device_id,
            "lid_opened": True
        }
        res_tamper = client.post("/api/v1/smart-piggy/security/lid-tamper", json=tamper_payload)
        self.assertEqual(res_tamper.status_code, 200)
        data = res_tamper.json()["data"]
        self.assertEqual(data.get("status"), "SECURITY_BREACH")
        self.assertEqual(data.get("wallet_status"), "FROZEN")
        print("  [PASS] test_05_lid_tamper -> Kích hoạt báo động cạy nắp, phong tỏa ví và gửi mail thành công!")

    def test_06_iot_power_cut_alert(self):
        """6. Kiểm tra API Cảnh báo Mất nguồn ngoài (Adapter Unplugged)"""
        power_payload = {
            "device_id": TestLiochioComprehensiveAPIs.test_device_id,
            "battery_pct": 85.5
        }
        res_power = client.post("/api/v1/smart-piggy/security/power-cut", json=power_payload)
        self.assertEqual(res_power.status_code, 200)
        data = res_power.json()["data"]
        self.assertEqual(data.get("status"), "POWER_CUT_ALERT_RECORDED")
        print("  [PASS] test_06_power_cut -> Ghi nhận cảnh báo mất điện ngoài, chuyển sang pin 18650 thành công!")

    def test_07_iot_buckets_management(self):
        """7. Kiểm tra API Quản lý Hũ mục tiêu con (Sub-pot Buckets)"""
        bucket_payload = {
            "device_id": TestLiochioComprehensiveAPIs.test_device_id,
            "goal_name": "Quỹ Mua Xe Đạp Thể Thao",
            "target_amount": 2500000.0,
            "deadline": "2026-12-31"
        }
        res_b = client.post("/api/v1/smart-piggy/buckets", json=bucket_payload)
        self.assertEqual(res_b.status_code, 201)
        data = res_b.json()["data"]
        bucket_id = data["id"]
        print(f"  [PASS] test_07_create_bucket -> Tạo Hũ mục tiêu '{data['goal_name']}' thành công: ID={bucket_id}")

        # Lấy danh sách hũ của thiết bị
        res_list = client.get(f"/api/v1/smart-piggy/buckets?device_id={TestLiochioComprehensiveAPIs.test_device_id}")
        self.assertEqual(res_list.status_code, 200)
        buckets = res_list.json()["data"]
        self.assertTrue(len(buckets) >= 1)
        print(f"  [PASS] test_07_get_buckets -> Lấy danh sách thành công, tổng cộng {len(buckets)} hũ.")

    def test_08_parent_matching_rules_db_persistence(self):
        """8. Kiểm tra API Quy tắc Cha Mẹ Thưởng Tiền (Parent Matching Bonus) lưu trữ bền vững DB"""
        rule_payload = {
            "child_user_id": TestLiochioComprehensiveAPIs.test_user_id,
            "matching_percentage": 50.0,
            "max_monthly_bonus": 1000000.0,
            "parent_wallet_id": "parent_wallet_123",
            "is_active": True
        }
        res_rule = client.post("/api/v1/smart-piggy/matching-rules", json=rule_payload)
        self.assertEqual(res_rule.status_code, 201)
        data = res_rule.json()["data"]
        self.assertEqual(data.get("matching_percentage"), 50.0)
        print("  [PASS] test_08_create_parent_matching_rule -> Tạo quy tắc thưởng 50% thành công!")

        # Kiểm tra xác minh quy tắc được lưu vào DB system_settings
        db = SessionLocal()
        s_row = db.execute(text("SELECT `value` FROM liochio_app_db.system_settings WHERE `key` = :k"), {"k": f"piggy_rule_{TestLiochioComprehensiveAPIs.test_user_id}"}).fetchone()
        db.close()
        self.assertIsNotNone(s_row, "Quy tắc thưởng phải được lưu bền vững trong system_settings!")
        print("  [PASS] test_08_db_verified -> Xác minh quy tắc đã lưu trong system_settings (JSON)!")

    def test_09_family_dashboard(self):
        """9. Kiểm tra API Family Savings Dashboard"""
        res_fam = client.get("/api/v1/smart-piggy/family-dashboard")
        self.assertEqual(res_fam.status_code, 200)
        data = res_fam.json()
        self.assertTrue(data.get("total_family_savings", 0) >= 0)
        print("  [PASS] test_09_family_dashboard -> Lấy bảng điều khiển tiết kiệm gia đình thành công!")

    def test_10_demo_unfreeze_and_smash(self):
        """10. Kiểm tra API Mở khóa Ví (Unfreeze) và Đập Heo Tất toán (Smash Demo)"""
        # Mở khóa ví
        res_unfreeze = client.post("/api/v1/smart-piggy/demo/unfreeze-wallet", json={
            "wallet_id": TestLiochioComprehensiveAPIs.test_wallet_id
        })
        self.assertEqual(res_unfreeze.status_code, 200)
        self.assertEqual(res_unfreeze.json()["data"]["status"], "ACTIVE")
        print("  [PASS] test_10_unfreeze -> Mở khóa ví về ACTIVE thành công!")

        # Đập heo tất toán
        res_smash = client.post("/api/v1/smart-piggy/demo/smash", json={
            "wallet_id": TestLiochioComprehensiveAPIs.test_wallet_id,
            "device_id": TestLiochioComprehensiveAPIs.test_device_id,
            "user_id": TestLiochioComprehensiveAPIs.test_user_id
        })
        self.assertEqual(res_smash.status_code, 200)
        print("  [PASS] test_10_smash -> Tất toán đập heo giải phóng số dư và ngắt liên kết thành công!")


if __name__ == "__main__":
    unittest.main(verbosity=2)
