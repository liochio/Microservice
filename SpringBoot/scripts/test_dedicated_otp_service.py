import urllib.request
import json
import time
import sys

sys.stdout.reconfigure(encoding='utf-8')

BASE_URL = "http://localhost:8094"

def make_request(method, path, body=None, headers=None):
    url = f"{BASE_URL}{path}"
    req_headers = {"Content-Type": "application/json", "Accept-Language": "vi"}
    if headers:
        req_headers.update(headers)
    
    data = json.dumps(body).encode('utf-8') if body is not None else None
    req = urllib.request.Request(url, data=data, headers=req_headers, method=method)
    
    try:
        with urllib.request.urlopen(req) as resp:
            content = resp.read().decode('utf-8')
            return resp.status, json.loads(content) if content else {}
    except urllib.error.HTTPError as e:
        content = e.read().decode('utf-8')
        return e.code, json.loads(content) if content else {}
    except Exception as e:
        return 0, {"error": str(e)}

def run_tests():
    print("=" * 80)
    print("🚀 BẮT ĐẦU KIỂM THỬ TOÀN DIỆN DEDICATED OTP & SMARTOTP SERVICE (PORT 8094)")
    print("=" * 80)
    
    total_tests = 0
    passed_tests = 0

    # 1. Kiểm tra cấu hình mặc định ban đầu
    total_tests += 1
    status, res = make_request("GET", "/api/v1/otp/config?tenantId=default")
    print(f"\n[TEST 1] GET /api/v1/otp/config -> Status: {status}")
    if status == 200 and res.get("data", {}).get("isEnabled") is not None:
        print("  --> PASS: Đọc cấu hình thành công:", res.get("data"))
        passed_tests += 1
    else:
        print("  --> FAIL:", res)

    # 2. Sinh OTP ở chế độ Bypass (Dev Mode)
    total_tests += 1
    gen_body = {
        "tenantId": "default",
        "userId": 9999,
        "purpose": "REGISTRATION",
        "destination": "dev_test@portfolio.dev",
        "otpType": "EMAIL"
    }
    status, res = make_request("POST", "/api/v1/otp/generate", body=gen_body)
    print(f"\n[TEST 2] POST /api/v1/otp/generate (Bypass Mode) -> Status: {status}")
    if status == 201 and res.get("data", {}).get("isBypassed") is True:
        print("  --> PASS: Sinh OTP Bypass thành công:", res.get("data"))
        passed_tests += 1
    else:
        print("  --> FAIL:", res)

    # 3. Xác thực OTP ở chế độ Bypass (Chấp thuận tự động)
    total_tests += 1
    ver_body = {
        "tenantId": "default",
        "userId": 9999,
        "purpose": "REGISTRATION",
        "otpCode": "123456"
    }
    status, res = make_request("POST", "/api/v1/otp/verify", body=ver_body)
    print(f"\n[TEST 3] POST /api/v1/otp/verify (Bypass Mode) -> Status: {status}")
    if status == 200 and res.get("data", {}).get("success") is True and res.get("data", {}).get("isBypassed") is True:
        print("  --> PASS: Xác thực OTP Bypass thành công:", res)
        passed_tests += 1
    else:
        print("  --> FAIL:", res)

    # 4. Tắt OTP Service qua API / DB (isEnabled = False)
    total_tests += 1
    cfg_body = {
        "isEnabled": False,
        "bypassInDev": True,
        "devBypassCode": "888888",
        "environment": "DEV"
    }
    status, res = make_request("PUT", "/api/v1/otp/config?tenantId=default", body=cfg_body)
    print(f"\n[TEST 4] PUT /api/v1/otp/config (Tắt Service isEnabled = False) -> Status: {status}")
    if status == 200 and res.get("data", {}).get("isEnabled") is False:
        print("  --> PASS: Cập nhật tắt Service thành công:", res.get("data"))
        passed_tests += 1
    else:
        print("  --> FAIL:", res)

    # 5. Xác thực OTP khi Service bị TẮT (Tự động bỏ qua xác thực 100%)
    total_tests += 1
    ver_body_disabled = {
        "tenantId": "default",
        "userId": 9999,
        "purpose": "REGISTRATION",
        "otpCode": "arbitrary_wrong_code"
    }
    status, res = make_request("POST", "/api/v1/otp/verify", body=ver_body_disabled)
    print(f"\n[TEST 5] POST /api/v1/otp/verify (Khi Service bị Tắt) -> Status: {status}")
    if status == 200 and res.get("data", {}).get("success") is True:
        print("  --> PASS: Hệ thống bỏ qua xác thực đúng theo yêu cầu:", res)
        passed_tests += 1
    else:
        print("  --> FAIL:", res)

    # 6. Bật lại Chế độ Ngặt nghèo (Strict Production Mode: isEnabled = True, bypassInDev = False)
    total_tests += 1
    cfg_strict = {
        "isEnabled": True,
        "bypassInDev": False,
        "environment": "PROD"
    }
    status, res = make_request("PUT", "/api/v1/otp/config?tenantId=default", body=cfg_strict)
    print(f"\n[TEST 6] PUT /api/v1/otp/config (Bật Strict Mode) -> Status: {status}")
    if status == 200 and res.get("data", {}).get("isEnabled") is True and res.get("data", {}).get("bypassInDev") is False:
        print("  --> PASS: Chuyển sang Strict Mode thành công:", res.get("data"))
        passed_tests += 1
    else:
        print("  --> FAIL:", res)

    # 7. Khởi tạo SmartOTP TOTP RFC 6238
    total_tests += 1
    smart_setup_body = {
        "tenantId": "default",
        "userId": 9999,
        "username": "tester_smart_otp"
    }
    status, res = make_request("POST", "/api/v1/otp/smart-otp/setup", body=smart_setup_body)
    print(f"\n[TEST 7] POST /api/v1/otp/smart-otp/setup -> Status: {status}")
    if status == 200 and res.get("data", {}).get("secret") and res.get("data", {}).get("qrBarcodeUri"):
        print("  --> PASS: Khởi tạo TOTP Secret & QR Barcode thành công:", res.get("data", {}).get("secret"))
        passed_tests += 1
    else:
        print("  --> FAIL:", res)

    # 8. Kiểm tra Đa Ngôn ngữ i18n (vi, en, zh) trên OTP Service
    languages = [
        ("vi", "api.response.otp.config_updated", "Cập nhật cấu hình OTP Service thành công"),
        ("en", "api.response.otp.config_updated", "OTP Service configuration updated successfully"),
        ("zh", "api.response.otp.config_updated", "OTP服务配置更新成功")
    ]
    for lang, key, expected_snippet in languages:
        total_tests += 1
        st, r = make_request("PUT", "/api/v1/otp/config?tenantId=default", body={"isEnabled": True, "bypassInDev": True, "environment": "DEV"}, headers={"Accept-Language": lang})
        msg = r.get("message", "")
        print(f"\n[TEST 8 - i18n {lang}] PUT /api/v1/otp/config -> Message: '{msg}'")
        if st == 200 and expected_snippet in msg:
            print(f"  --> PASS i18n {lang} chuẩn xác!")
            passed_tests += 1
        else:
            print(f"  --> FAIL i18n {lang}:", r)

    print("\n" + "=" * 80)
    print(f"📊 KẾT QUẢ KIỂM THỬ: {passed_tests}/{total_tests} PASSED ({passed_tests/total_tests*100:.1f}%)")
    print("=" * 80)
    return passed_tests == total_tests

if __name__ == "__main__":
    run_tests()
