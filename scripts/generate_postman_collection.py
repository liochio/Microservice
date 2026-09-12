import sys, io
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')
import json
import os

def build_postman():
    collection = {
        "info": {
            "_postman_id": "liochio-microservices-ecosystem-2026",
            "name": "Liochio Ecosystem - Full API & Security Flow (100% Strict - No Bypass)",
            "description": "Bộ Collection API hoàn chỉnh 100% cho toàn bộ nền tảng Liochio Microservices Ecosystem (Spring Boot IAM Core + Python FinTech Engine + Domain Services). Đầy đủ các trường nghiệp vụ thực tế, kiểm tra Validation nghiêm ngặt, tự động chụp Token và luồng giao dịch Core Banking Sổ cái kép.",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        },
        "auth": {
            "type": "bearer",
            "bearer": [
                {
                    "key": "token",
                    "value": "{{token}}",
                    "type": "string"
                }
            ]
        },
        "variable": [
            {"key": "authUrl", "value": "http://localhost:8081", "type": "string"},
            {"key": "fintechUrl", "value": "http://localhost:8089", "type": "string"},
            {"key": "gatewayUrl", "value": "http://localhost:8080", "type": "string"},
            {"key": "coreAdminUrl", "value": "http://localhost:5170", "type": "string"},
            {"key": "appPortalUrl", "value": "http://localhost:5173", "type": "string"},
            {"key": "token", "value": "", "type": "string"},
            {"key": "refreshToken", "value": "", "type": "string"},
            {"key": "actionToken", "value": "", "type": "string"},
            {"key": "deviceId", "value": "fp_postman_client_01", "type": "string"},
            {"key": "userId", "value": "1", "type": "string"},
            {"key": "username", "value": "superadmin", "type": "string"},
            {"key": "password", "value": "Password123!", "type": "string"},
            {"key": "sessionId", "value": "", "type": "string"},
            {"key": "lastOtpCode", "value": "", "type": "string"}
        ],
        "item": []
    }

    def make_item(name, method, full_url, body=None, headers=None, auth=None, test_script=None, desc=""):
        req_headers = [
            {"key": "Accept-Language", "value": "vi-VN", "type": "text"},
            {"key": "Content-Type", "value": "application/json", "type": "text"},
            {"key": "X-Tenant-ID", "value": "default", "type": "text"},
            {"key": "X-Device-ID", "value": "{{deviceId}}", "type": "text"},
            {"key": "X-Platform", "value": "POSTMAN", "type": "text"}
        ]
        if headers:
            for k, v in headers.items():
                req_headers.append({"key": k, "value": v, "type": "text"})
        
        # Cú pháp Postman v2.1.0 chuẩn:
        # Nếu url là {{authUrl}}/api/v1/auth/login -> host: [{{authUrl}}], path: [api, v1, auth, login]
        url_clean = full_url
        if "://" in url_clean:
            url_clean = url_clean.split("://", 1)[1]
        parts = [p for p in url_clean.split("/") if p]
        host_arr = [parts[0]] if parts else []
        path_arr = parts[1:] if len(parts) > 1 else []

        item = {
            "name": name,
            "request": {
                "method": method,
                "header": req_headers,
                "url": {
                    "raw": full_url,
                    "host": host_arr,
                    "path": path_arr
                },
                "description": desc
            }
        }
        
        if auth is not None:
            item["request"]["auth"] = auth
            
        if body is not None:
            item["request"]["body"] = {
                "mode": "raw",
                "raw": json.dumps(body, indent=2, ensure_ascii=False) if isinstance(body, (dict, list)) else body,
                "options": {
                    "raw": {
                        "language": "json"
                    }
                }
            }
                
        if test_script:
            item["event"] = [
                {
                    "listen": "test",
                    "script": {
                        "exec": test_script if isinstance(test_script, list) else test_script.strip().split("\n"),
                        "type": "text/javascript"
                    }
                }
            ]
            
        return item

    # Scripts
    token_extract_script = """
var jsonData = pm.response.json();
if (jsonData && jsonData.data) {
    if (jsonData.data.accessToken) {
        pm.collectionVariables.set("token", jsonData.data.accessToken);
        console.log("Captured Token: " + jsonData.data.accessToken);
    }
    if (jsonData.data.refreshToken) {
        pm.collectionVariables.set("refreshToken", jsonData.data.refreshToken);
    }
    if (jsonData.data.userId) {
        pm.collectionVariables.set("userId", jsonData.data.userId.toString());
    }
    if (jsonData.data.sessionId) {
        pm.collectionVariables.set("sessionId", jsonData.data.sessionId);
    }
}
pm.test("Status code is 200/201", function () {
    pm.expect(pm.response.code).to.be.oneOf([200, 201]);
});
"""

    validation_400_script = """
pm.test("Status code is 400 Bad Request", function () {
    pm.response.to.have.status(400);
});
pm.test("Response has validation errors", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.status).to.be.oneOf([400, "INVALID_REQUEST", "VALIDATION_FAILED"]);
});
"""

    # 1. AUTH & SECURITY FOLDER
    auth_folder = {
        "name": "01. Authentication & Security (Full 100% Strict Flow)",
        "description": "Phân hệ Xác thực IAM, Quản lý Token RS256, Phiên làm việc, Thiết bị và Smart OTP (Java Spring Boot Core).",
        "item": [
            make_item(
                "1.1 Đăng ký tài khoản mới (Register)",
                "POST",
                "{{authUrl}}/api/v1/auth/register",
                body={
                    "username": "client_demo_2026",
                    "password": "Password123!",
                    "email": "client_demo_2026@liochio.vn",
                    "fullName": "Liochio Client Demo 2026",
                    "roles": ["ROLE_CUSTOMER"]
                },
                desc="Tạo tài khoản mới ở trạng thái PENDING_VERIFY và sinh mã OTP kích hoạt qua Email.",
                test_script="""
var res = pm.response.json();
pm.test("Đăng ký thành công", function () {
    pm.expect(pm.response.code).to.eql(201);
    pm.expect(res.data.status).to.eql("PENDING_VERIFY");
    pm.collectionVariables.set("userId", res.data.id.toString());
});
"""
            ),
            make_item(
                "1.2 [VALIDATION] Đăng ký - Thiếu Email / Sai định dạng (400 Bad Request)",
                "POST",
                "{{authUrl}}/api/v1/auth/register",
                body={
                    "username": "invalid_user",
                    "password": "Password123!",
                    "email": "invalid-email-format",
                    "fullName": "Test Invalid Email"
                },
                desc="Kiểm tra bộ lọc Validation: Bắt lỗi email không đúng cú pháp.",
                test_script=validation_400_script
            ),
            make_item(
                "1.3 [VALIDATION] Đăng ký - Mật khẩu yếu vi phạm chính sách (400 Bad Request)",
                "POST",
                "{{authUrl}}/api/v1/auth/register",
                body={
                    "username": "weak_pwd_user",
                    "password": "123",
                    "email": "weak_user@liochio.vn",
                    "fullName": "Test Weak Password"
                },
                desc="Kiểm tra bộ lọc Validation: Mật khẩu phải từ 6 ký tự trở lên.",
                test_script=validation_400_script
            ),
            make_item(
                "1.4 Xác thực OTP kích hoạt tài khoản (Verify Registration OTP)",
                "POST",
                "{{authUrl}}/api/v1/auth/verify-otp",
                body={
                    "userId": 1,
                    "email": "client_demo_2026@liochio.vn",
                    "username": "client_demo_2026",
                    "otpCode": "123456"
                },
                desc="Kích hoạt tài khoản bằng mã OTP 6 số thực tế. Hệ thống tự động khởi tạo Sổ cái kép và Ví FinTech."
            ),
            make_item(
                "1.5 [VALIDATION] Xác thực OTP - Nhập sai mã OTP (400 Bad Request)",
                "POST",
                "{{authUrl}}/api/v1/auth/verify-otp",
                body={
                    "userId": 1,
                    "username": "client_demo_2026",
                    "otpCode": "000000"
                },
                desc="Kiểm tra hệ thống từ chối mã OTP không chính xác.",
                test_script=validation_400_script
            ),
            make_item(
                "1.6 Đăng nhập SuperAdmin (Login SuperAdmin - Full Quyền)",
                "POST",
                "{{authUrl}}/api/v1/auth/login",
                body={
                    "username": "superadmin",
                    "password": "Password123!"
                },
                desc="Đăng nhập tài khoản SuperAdmin có 50/50 quyền hạn và số dư sổ cái 100 tỷ VNĐ.",
                test_script=token_extract_script
            ),
            make_item(
                "1.7 [VALIDATION] Đăng nhập - Sai mật khẩu (400/401 Password Incorrect)",
                "POST",
                "{{authUrl}}/api/v1/auth/login",
                body={
                    "username": "superadmin",
                    "password": "WrongPassword999@"
                },
                desc="Kiểm tra kiểm soát đăng nhập: Báo sai mật khẩu, đếm số lần thử và khóa 15 phút nếu sai 5 lần.",
                test_script="""
pm.test("Status code is 400 or 401", function () {
    pm.expect(pm.response.code).to.be.oneOf([400, 401]);
});
"""
            ),
            make_item(
                "1.8 Xác thực 2FA Thiết bị lạ (Verify Device OTP)",
                "POST",
                "{{authUrl}}/api/v1/auth/verify-device-otp",
                body={
                    "username": "superadmin",
                    "deviceId": "{{deviceId}}",
                    "otpCode": "123456",
                    "rememberDevice": True
                },
                desc="Xác thực OTP khi đăng nhập từ thiết bị lạ để nhận cặp Token và gắn nhãn Tin cậy (Trusted).",
                test_script=token_extract_script
            ),
            make_item(
                "1.9 Lấy thông tin tài khoản hiện tại (Get /me)",
                "GET",
                "{{authUrl}}/api/v1/auth/me",
                desc="Tra cứu thông tin chi tiết, quyền hạn RBAC và trạng thái eKYC của tài khoản đang đăng nhập."
            ),
            make_item(
                "1.10 Làm mới Token (Refresh Token Rotation)",
                "POST",
                "{{authUrl}}/api/v1/auth/refresh",
                body={
                    "refreshToken": "{{refreshToken}}"
                },
                desc="Xoay vòng Refresh Token, phát hiện rò rỉ và cấp Access Token mới.",
                test_script=token_extract_script
            ),
            make_item(
                "1.11 Đổi mật khẩu tài khoản (Change Password)",
                "POST",
                "{{authUrl}}/api/v1/auth/change-password",
                body={
                    "oldPassword": "Password123!",
                    "newPassword": "Password123!"
                },
                desc="Đổi mật khẩu tài khoản và thu hồi toàn bộ các phiên khác."
            ),
            make_item(
                "1.12 Danh sách thiết bị truy cập (Get Devices)",
                "GET",
                "{{authUrl}}/api/v1/auth/devices",
                desc="Xem danh sách các thiết bị (Web, Mobile, Desktop) đã từng đăng nhập tài khoản."
            ),
            make_item(
                "1.13 Danh sách phiên làm việc hoạt động (Get Sessions)",
                "GET",
                "{{authUrl}}/api/v1/auth/sessions",
                desc="Xem các phiên active, phát hiện đăng nhập bất thường để Kick-out."
            ),
            make_item(
                "1.14 Khởi tạo Smart OTP TOTP RFC 6238 (Smart OTP Setup)",
                "POST",
                "{{authUrl}}/api/v1/auth/smart-otp/setup",
                desc="Sinh Secret Key Base32 và URL QR Code để nạp vào Google Authenticator / Authy."
            ),
            make_item(
                "1.15 Cấp Action Token giao dịch nhạy cảm (Smart OTP Action Token)",
                "POST",
                "{{authUrl}}/api/v1/auth/smart-otp/action-token",
                body={
                    "otpCode": "123456",
                    "pin": "123456"
                },
                desc="Xác thực TOTP + PIN để cấp Action Token có hiệu lực 120s cho các giao dịch nhạy cảm."
            ),
            make_item(
                "1.16 Đăng xuất phiên làm việc (Logout)",
                "POST",
                "{{authUrl}}/api/v1/auth/logout",
                headers={"Authorization": "Bearer {{token}}"},
                desc="Thu hồi phiên và đưa Access Token vào Blacklist (Caffeine + Redis) tức thì."
            ),
            make_item(
                "1.17 Cưỡng chế đăng xuất tất cả thiết bị (Force Logout All)",
                "POST",
                "{{authUrl}}/api/v1/auth/logout-all-devices",
                headers={"Authorization": "Bearer {{token}}"},
                desc="Thu hồi toàn bộ phiên và token trên mọi thiết bị."
            )
        ]
    }
    collection["item"].append(auth_folder)

    # 2. CORE BANKING & LEDGER FOLDER
    ledger_folder = {
        "name": "02. Core Banking & Double-Entry Ledger (Sổ Cái Kép)",
        "description": "Hệ thống kế toán bút toán kép bảo toàn số dư (Available, Holding, Escrow) chuẩn Banking.",
        "item": [
            make_item(
                "2.1 Tra cứu số dư Sổ cái kế toán của tôi (Get My Balance)",
                "GET",
                "{{authUrl}}/api/v1/ledger/balance?tenantId=default",
                desc="Tra cứu 3 trạng thái số dư: Khả dụng (USER_AVAILABLE), Tạm giữ (USER_HOLDING), Két Heo đất (USER_ESCROW)."
            ),
            make_item(
                "2.2 Tra cứu số dư theo User ID (Get User Balance - Admin)",
                "GET",
                "{{authUrl}}/api/v1/ledger/balance/1?tenantId=default",
                desc="Admin tra cứu chi tiết số dư tài khoản của người dùng bất kỳ."
            ),
            make_item(
                "2.3 Tra cứu sao kê bút toán kép (Get Journal History)",
                "GET",
                "{{authUrl}}/api/v1/ledger/history/1?tenantId=default&page=0&size=20",
                desc="Xem toàn bộ lịch sử ghi Nợ (DEBIT) và Có (CREDIT) cân bằng tuyệt đối."
            ),
            make_item(
                "2.4 Hạch toán bút toán kép M2M API (Double-Entry Transfer)",
                "POST",
                "{{authUrl}}/api/v1/ledger/m2m/transaction",
                body={
                    "tenantId": "default",
                    "userId": 1,
                    "transactionType": "TOPUP",
                    "amount": 5000000.00,
                    "currency": "VND",
                    "sourceAccount": "SYSTEM_SETTLEMENT",
                    "targetAccount": "USER_AVAILABLE",
                    "description": "Nạp tiền vào tài khoản sổ cái từ Cổng thanh toán",
                    "idempotencyKey": "TX-POSTMAN-2026-0001"
                },
                desc="Giao dịch hạch toán ghi Nợ tài khoản Hệ thống và ghi Có tài khoản Người dùng."
            )
        ]
    }
    collection["item"].append(ledger_folder)

    # 3. EKYC & IDENTITY FOLDER
    ekyc_folder = {
        "name": "03. Identity & eKYC (Định Danh Sinh Trắc Học)",
        "description": "Luồng nộp hồ sơ CCCD/Passport, tự động phân hạng hạn mức giao dịch (TIER_1 -> TIER_3).",
        "item": [
            make_item(
                "3.1 Nộp hồ sơ định danh eKYC (Submit eKYC)",
                "POST",
                "{{authUrl}}/api/v1/ekyc/submit",
                body={
                    "idCardType": "CCCD",
                    "idCardNumber": "079090000001",
                    "frontImageUrl": "https://storage.liochio.dev/ekyc/front_cccd_sample.jpg",
                    "backImageUrl": "https://storage.liochio.dev/ekyc/back_cccd_sample.jpg",
                    "faceVideoUrl": "https://storage.liochio.dev/ekyc/face_liveness_sample.mp4"
                },
                desc="Người dùng gửi ảnh CCCD và video liveness để xét duyệt nâng hạn mức."
            ),
            make_item(
                "3.2 [VALIDATION] eKYC - Thiếu số CCCD (400 Bad Request)",
                "POST",
                "{{authUrl}}/api/v1/ekyc/submit",
                body={
                    "idCardType": "CCCD",
                    "idCardNumber": "",
                    "frontImageUrl": "https://storage.liochio.dev/ekyc/front.jpg"
                },
                desc="Kiểm tra bắt lỗi khi thiếu số giấy tờ tùy thân.",
                test_script=validation_400_script
            ),
            make_item(
                "3.3 Phê duyệt hồ sơ eKYC lên TIER_3 (Admin Approve eKYC)",
                "POST",
                "{{authUrl}}/api/v1/ekyc/approve/1?tier=TIER_3",
                desc="Admin phê duyệt nâng cấp hạn mức lên TIER_3 (Không giới hạn hạn mức giao dịch/ngày)."
            )
        ]
    }
    collection["item"].append(ekyc_folder)

    # 4. FINTECH PYTHON RESOURCE SERVER
    fintech_folder = {
        "name": "04. FinTech Resource Engine (Python FastAPI - Wallets & Trans)",
        "description": "Dịch vụ tài chính, quản lý Ví, Chuyển tiền, Hạn mức, Heo đất thông minh IoT và AI Cố vấn (Python Port 8000).",
        "item": [
            make_item(
                "4.1 Danh sách Ví của người dùng (Get Wallets)",
                "GET",
                "{{fintechUrl}}/api/v1/wallets",
                desc="Tra cứu danh sách ví tiêu dùng, ví tiết kiệm và ví Heo đất của người dùng."
            ),
            make_item(
                "4.2 Tạo Ví mới (Create Wallet)",
                "POST",
                "{{fintechUrl}}/api/v1/wallets",
                body={
                    "name": "Ví Tiết Kiệm Du Lịch",
                    "wallet_type": "SAVING",
                    "currency": "VND",
                    "color": "#2ecc71",
                    "icon": "plane",
                    "description": "Quỹ tiết kiệm cho các chuyến đi chơi cuối năm"
                },
                desc="Tạo ví tài chính phụ phân loại theo mục đích sử dụng."
            ),
            make_item(
                "4.3 Chuyển tiền nội bộ (Transfer Money)",
                "POST",
                "{{fintechUrl}}/api/v1/transfers",
                body={
                    "source_wallet_id": "wal_superadmin_01",
                    "target_account": "ACC_00000005",
                    "amount": 250000.0,
                    "currency": "VND",
                    "note": "Chuyển tiền ăn trưa nhóm",
                    "pin": "123456"
                },
                desc="Thực hiện chuyển tiền giữa 2 tài khoản ví FinTech."
            ),
            make_item(
                "4.4 Lịch sử giao dịch tài chính (Get Transactions)",
                "GET",
                "{{fintechUrl}}/api/v1/transactions?limit=20&offset=0",
                desc="Xem danh sách các giao dịch thu/chi, chuyển tiền đã hoàn thành."
            ),
            make_item(
                "4.5 Trợ lý Cố vấn tài chính AI Gemini (AI Financial Advisor)",
                "POST",
                "{{fintechUrl}}/api/v1/ai/chat",
                body={
                    "message": "Hãy phân tích chi tiêu tháng này của tôi và đề xuất kế hoạch tiết kiệm 20% thu nhập.",
                    "session_id": "ai_session_2026_01"
                },
                desc="Gửi câu hỏi tư vấn tài chính tới mô hình Google Gemini RAG phân tích chi tiêu thực tế."
            )
        ]
    }
    collection["item"].append(fintech_folder)

    # 5. DOMAIN MICROSERVICES
    domain_folder = {
        "name": "05. Multi-Domain Services (Tour, Music, Film, Payment)",
        "description": "Các dịch vụ nghiệp vụ chuyên biệt: Du lịch, Âm nhạc số, Phim ảnh, Cổng thanh toán VNPay/MoMo.",
        "item": [
            make_item(
                "5.1 Danh sách Tour Du lịch (Tour Service)",
                "GET",
                "{{authUrl}}/api/v1/tours?page=0&size=10",
                desc="Tra cứu danh sách tour du lịch trọn gói đang mở bán."
            ),
            make_item(
                "5.2 Danh sách Bài hát & Nhạc số (Music Service)",
                "GET",
                "{{authUrl}}/api/v1/music/songs?page=0&size=10",
                desc="Tra cứu kho nhạc số chất lượng cao 320kbps."
            ),
            make_item(
                "5.3 Danh sách Phim ảnh Streaming (Film Service)",
                "GET",
                "{{authUrl}}/api/v1/films/movies?page=0&size=10",
                desc="Tra cứu danh mục phim truyện và phim tài liệu bản quyền."
            ),
            make_item(
                "5.4 Tạo giao dịch Cổng thanh toán (Payment Gateway)",
                "POST",
                "{{authUrl}}/api/v1/payments/create",
                body={
                    "gateway": "VNPAY",
                    "orderId": "ORD_POSTMAN_2026_99",
                    "amount": 3850000.0,
                    "currency": "VND",
                    "orderDescription": "Thanh toan Tour Ha Long Bay 3N2D",
                    "returnUrl": "http://localhost:3000/payment/result"
                },
                desc="Tạo URL chuyển hướng thanh toán qua cổng VNPAY / MoMo / Stripe."
            )
        ]
    }
    collection["item"].append(domain_folder)

    # Save collection to postman folder
    out_path = r"D:\Github\Back-end\Microservice\postman\Liochio_Microservices_API.postman_collection.json"
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(collection, f, indent=2, ensure_ascii=False)
    
    print(f"✅ Đã tạo thành công Postman Collection tại: {out_path}")
    print(f"   - Tổng số danh mục: {len(collection['item'])}")
    total_reqs = sum(len(f['item']) for f in collection['item'])
    print(f"   - Tổng số Request API: {total_reqs}")

if __name__ == '__main__':
    build_postman()
