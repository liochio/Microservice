import sys
import urllib.request
import urllib.error
import json
import time

# Enforce UTF-8 stdout
sys.stdout.reconfigure(encoding='utf-8')

BASE_URL = "http://localhost:8081"
HEADERS_BASE = {
    "Content-Type": "application/json;charset=UTF-8",
    "X-Tenant-Id": "tenant-test-i18n",
    "X-Forwarded-For": "192.168.1.100",
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0"
}

results = []

def run_test(title, method, endpoint, headers, body, expected_status, expected_message_substr):
    url = f"{BASE_URL}{endpoint}"
    req_headers = {**HEADERS_BASE, **headers}
    
    data_bytes = None
    if body is not None:
        data_bytes = json.dumps(body).encode("utf-8")
        
    req = urllib.request.Request(url, data=data_bytes, headers=req_headers, method=method)
    
    try:
        try:
            with urllib.request.urlopen(req, timeout=5) as response:
                status_code = response.getcode()
                resp_data = json.loads(response.read().decode("utf-8"))
        except urllib.error.HTTPError as e:
            status_code = e.code
            try:
                resp_data = json.loads(e.read().decode("utf-8"))
            except Exception:
                resp_data = {"message": e.reason}
                
        status_ok = (status_code == expected_status)
        msg = resp_data.get("message", "")
        msg_ok = expected_message_substr in msg
        
        passed = status_ok and msg_ok
        res_entry = {
            "title": title,
            "status_code": status_code,
            "expected_status": expected_status,
            "message": msg,
            "expected_msg_substr": expected_message_substr,
            "passed": passed
        }
        results.append(res_entry)
        status_str = "PASS" if passed else "FAIL"
        print(f"[{status_str}] {title}")
        if not passed:
            print(f"   -> Got status {status_code} (expected {expected_status}), msg: '{msg}' (expected substring: '{expected_message_substr}')")
    except Exception as e:
        results.append({
            "title": title,
            "status_code": 0,
            "expected_status": expected_status,
            "message": str(e),
            "expected_msg_substr": expected_message_substr,
            "passed": False
        })
        print(f"[FAIL] {title} -> Exception: {e}")

print("=== STARTING I18N & ENCODING MULTI-LANGUAGE TEST SUITE ===")

# Test 1: Unauthenticated Error in Vietnamese
run_test(
    title="1. Unauthenticated Error - Vietnamese (vi)",
    method="GET",
    endpoint="/api/v1/auth/me",
    headers={"Accept-Language": "vi"},
    body=None,
    expected_status=401,
    expected_message_substr="Bạn chưa đăng nhập hoặc phiên làm việc đã hết hạn"
)

# Test 2: Unauthenticated Error in English
run_test(
    title="2. Unauthenticated Error - English (en)",
    method="GET",
    endpoint="/api/v1/auth/me",
    headers={"Accept-Language": "en"},
    body=None,
    expected_status=401,
    expected_message_substr="Unauthenticated or session has expired"
)

# Test 3: Unauthenticated Error in Chinese
run_test(
    title="3. Unauthenticated Error - Chinese (zh)",
    method="GET",
    endpoint="/api/v1/auth/me",
    headers={"Accept-Language": "zh"},
    body=None,
    expected_status=401,
    expected_message_substr="用户未登录或登录已过期"
)

# Test 4: Register in Vietnamese
user_vi = f"user_vi_{int(time.time())}"
run_test(
    title="4. Register User - Vietnamese (vi)",
    method="POST",
    endpoint="/api/v1/auth/register",
    headers={"Accept-Language": "vi"},
    body={
        "username": user_vi,
        "email": f"{user_vi}@enterprise.com",
        "password": "Password@123456",
        "fullName": "Nguyễn Văn A"
    },
    expected_status=201,
    expected_message_substr="Đăng ký tài khoản thành công"
)

# Test 5: Register in English
user_en = f"user_en_{int(time.time())}"
run_test(
    title="5. Register User - English (en)",
    method="POST",
    endpoint="/api/v1/auth/register",
    headers={"Accept-Language": "en"},
    body={
        "username": user_en,
        "email": f"{user_en}@enterprise.com",
        "password": "Password@123456",
        "fullName": "John Doe"
    },
    expected_status=201,
    expected_message_substr="Account registered successfully"
)

# Test 6: Register in Chinese
user_zh = f"user_zh_{int(time.time())}"
run_test(
    title="6. Register User - Chinese (zh)",
    method="POST",
    endpoint="/api/v1/auth/register",
    headers={"Accept-Language": "zh"},
    body={
        "username": user_zh,
        "email": f"{user_zh}@enterprise.com",
        "password": "Password@123456",
        "fullName": "张三"
    },
    expected_status=201,
    expected_message_substr="账号注册成功"
)

# Test 7: Duplicate Username Error in Vietnamese
run_test(
    title="7. Duplicate Username Error - Vietnamese (vi)",
    method="POST",
    endpoint="/api/v1/auth/register",
    headers={"Accept-Language": "vi"},
    body={
        "username": user_vi,
        "email": f"diff_{user_vi}@enterprise.com",
        "password": "Password@123456",
        "fullName": "Nguyễn Văn B"
    },
    expected_status=400,
    expected_message_substr="Tài khoản hoặc email đã tồn tại"
)

# Test 8: Duplicate Username Error in English
run_test(
    title="8. Duplicate Username Error - English (en)",
    method="POST",
    endpoint="/api/v1/auth/register",
    headers={"Accept-Language": "en"},
    body={
        "username": user_vi,
        "email": f"diff2_{user_vi}@enterprise.com",
        "password": "Password@123456",
        "fullName": "John Smith"
    },
    expected_status=400,
    expected_message_substr="Username or email already exists"
)

# Test 9: Duplicate Username Error in Chinese
run_test(
    title="9. Duplicate Username Error - Chinese (zh)",
    method="POST",
    endpoint="/api/v1/auth/register",
    headers={"Accept-Language": "zh"},
    body={
        "username": user_vi,
        "email": f"diff3_{user_vi}@enterprise.com",
        "password": "Password@123456",
        "fullName": "李四"
    },
    expected_status=400,
    expected_message_substr="用户名或电子邮箱已存在"
)

# Test 10: QR Code Init in Vietnamese
run_test(
    title="10. QR Code Init - Vietnamese (vi)",
    method="POST",
    endpoint="/api/v1/auth/qr/init",
    headers={"Accept-Language": "vi"},
    body={},
    expected_status=200,
    expected_message_substr="Khởi tạo mã QR thành công"
)

# Test 11: QR Code Init in English
run_test(
    title="11. QR Code Init - English (en)",
    method="POST",
    endpoint="/api/v1/auth/qr/init",
    headers={"Accept-Language": "en"},
    body={},
    expected_status=200,
    expected_message_substr="QR login session initialized successfully"
)

# Test 12: QR Code Init in Chinese
run_test(
    title="12. QR Code Init - Chinese (zh)",
    method="POST",
    endpoint="/api/v1/auth/qr/init",
    headers={"Accept-Language": "zh"},
    body={},
    expected_status=200,
    expected_message_substr="二维码登录会话初始化成功"
)

# Summary
total = len(results)
passed = sum(1 for r in results if r["passed"])
failed = total - passed

print(f"\n=======================================================")
print(f"I18N TEST SUMMARY: Total: {total} | Passed: {passed} | Failed: {failed}")
print(f"Pass Rate: {(passed / total) * 100:.1f}%")
print(f"=======================================================")

with open("I18N_VERIFICATION_RESULT.json", "w", encoding="utf-8") as f:
    json.dump(results, f, ensure_ascii=False, indent=2)
