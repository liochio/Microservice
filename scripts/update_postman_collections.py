#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script cập nhật tự động toàn bộ Postman Collections và Environment của Liochio:
- 01_Liochio_Core_Platform_API.postman_collection.json
- 02_Liochio_Python_Resource_API.postman_collection.json
- Liochio_Microservices_API.postman_collection.json
- Liochio_Local_Environment.postman_environment.json
"""
import json
import os

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
POSTMAN_DIR = os.path.join(BASE_DIR, "postman")

def create_headers(auth_type="bearer", is_core=False):
    headers = [
        {"key": "Accept-Language", "value": "vi-VN", "type": "text"},
        {"key": "Content-Type", "value": "application/json", "type": "text"},
        {"key": "X-Tenant-ID", "value": "SYSTEM", "type": "text"},
        {"key": "X-Device-ID", "value": "{{deviceId}}", "type": "text"},
        {"key": "X-Platform", "value": "POSTMAN", "type": "text"}
    ]
    if auth_type == "bearer":
        token_var = "{{coreToken}}" if is_core else "{{pythonToken}}"
        headers.append({"key": "Authorization", "value": f"Bearer {token_var}", "type": "text"})
    return headers

def create_request_item(name, method, url_raw, body_str=None, description="", is_core=False, auth_type="bearer", test_script=None):
    url_parts = url_raw.replace("{{coreUrl}}/", "").replace("{{pythonUrl}}/", "").split("?")[0].split("/")
    host_var = "{{coreUrl}}" if is_core else "{{pythonUrl}}"
    
    url_obj = {
        "raw": url_raw,
        "host": [host_var],
        "path": url_parts
    }
    if "?" in url_raw:
        query_part = url_raw.split("?")[1]
        url_obj["query"] = []
        for q in query_part.split("&"):
            if "=" in q:
                k, v = q.split("=", 1)
                url_obj["query"].append({"key": k, "value": v})
            else:
                url_obj["query"].append({"key": q, "value": ""})
                
    item = {
        "name": name,
        "request": {
            "method": method,
            "header": create_headers(auth_type=auth_type, is_core=is_core),
            "url": url_obj,
            "description": description,
            "auth": {"type": "noauth"}
        }
    }
    if body_str is not None:
        item["request"]["body"] = {
            "mode": "raw",
            "raw": body_str,
            "options": {"raw": {"language": "json"}}
        }
    
    test_lines = test_script if test_script else [
        "pm.test(\"Status code hợp lệ (200, 201, 204)\", function () {",
        "    pm.expect(pm.response.code).to.be.oneOf([200, 201, 204]);",
        "});"
    ]
    item["event"] = [
        {
            "listen": "test",
            "script": {
                "exec": test_lines,
                "type": "text/javascript"
            }
        }
    ]
    return item

def update_python_collection():
    filepath = os.path.join(POSTMAN_DIR, "02_Liochio_Python_Resource_API.postman_collection.json")
    with open(filepath, "r", encoding="utf-8") as f:
        data = json.load(f)
        
    piggy_folder = None
    for folder in data["item"]:
        if "Heo" in folder["name"]:
            piggy_folder = folder
            break
            
    if not piggy_folder:
        print("Không tìm thấy folder Heo Đất!")
        return

    # Danh sách các request mới cần cập nhật
    new_requests = [
        create_request_item(
            "2.12 [SECURITY] Cảnh báo cạy nắp Heo Đất Limit Switch (Lid Tamper Security Alert)",
            "POST",
            "{{pythonUrl}}/api/v1/smart-piggy/security/lid-tamper",
            body_str=json.dumps({"device_id": "{{piggyDeviceId}}", "lid_opened": True}, indent=2),
            description="Cảm biến công tắc hành trình phát hiện nắp Heo bị cạy mở -> Báo động, phong tỏa ví tạm thời và gửi email cảnh báo bảo mật."
        ),
        create_request_item(
            "2.13 [SECURITY] Cảnh báo rút nguồn điện Adapter (Power Cut Alert)",
            "POST",
            "{{pythonUrl}}/api/v1/smart-piggy/security/power-cut",
            body_str=json.dumps({"device_id": "{{piggyDeviceId}}", "battery_pct": 85.5}, indent=2),
            description="Cảm biến điện áp phát hiện mất nguồn Adapter ngoài -> Chuyển sang nguồn Pin 18650 dự phòng và ghi nhật ký."
        ),
        create_request_item(
            "2.14 [BUCKETS] Tạo Hũ mục tiêu con (Create Sub-pot Bucket)",
            "POST",
            "{{pythonUrl}}/api/v1/smart-piggy/buckets",
            body_str=json.dumps({
                "device_id": "{{piggyDeviceId}}",
                "goal_name": "Quỹ Mua Xe Đạp Thể Thao",
                "target_amount": 2500000.0,
                "deadline": "2026-12-31"
            }, indent=2, ensure_ascii=False),
            description="Tạo Hũ mục tiêu tiết kiệm phân bổ theo mô hình hũ con gắn với Heo Đất."
        ),
        create_request_item(
            "2.15 [BUCKETS] Lấy danh sách Hũ mục tiêu con (Get Buckets)",
            "GET",
            "{{pythonUrl}}/api/v1/smart-piggy/buckets?device_id={{piggyDeviceId}}",
            description="Truy vấn danh sách tất cả các Hũ mục tiêu tiết kiệm đang hoạt động của Heo Đất."
        ),
        create_request_item(
            "2.16 [PARENT MATCHING] Thiết lập quy tắc Cha Mẹ Thưởng Tiền (Create Matching Rule)",
            "POST",
            "{{pythonUrl}}/api/v1/smart-piggy/matching-rules",
            body_str=json.dumps({
                "child_user_id": "{{userId}}",
                "matching_percentage": 50.0,
                "max_monthly_bonus": 1000000.0,
                "parent_wallet_id": "parent_wallet_demo",
                "is_active": True
            }, indent=2),
            description="Cha mẹ thiết lập quy tắc thưởng tiền tỷ lệ % khi con bỏ ống heo (lưu trữ bền vững trong DB system_settings)."
        ),
        create_request_item(
            "2.17 [PARENT MATCHING] Xem quy tắc thưởng đang áp dụng (Get Matching Rules)",
            "GET",
            "{{pythonUrl}}/api/v1/smart-piggy/matching-rules",
            description="Truy vấn quy tắc thưởng tiền Parent Matching Bonus hiện đang kích hoạt."
        ),
        create_request_item(
            "2.18 [FAMILY] Bảng điều khiển Tiết kiệm Gia đình (Family Dashboard)",
            "GET",
            "{{pythonUrl}}/api/v1/smart-piggy/family-dashboard",
            description="Tổng hợp số dư tiết kiệm của cả gia đình, bảng xếp hạng thi đua và tiến độ tích lũy."
        ),
        create_request_item(
            "2.19 [DEMO ONBOARDING] Onboard Người dùng Mới & Sinh mã OTP",
            "POST",
            "{{pythonUrl}}/api/v1/smart-piggy/demo/onboard-user",
            body_str=json.dumps({
                "full_name": "Nguyễn Kiểm Thử Postman",
                "username": "postman_user_{{$randomInt}}",
                "email": "voduylebt99@gmail.com",
                "phone_number": "098{{$randomInt}}"
            }, indent=2, ensure_ascii=False),
            description="Đăng ký tài khoản người dùng mới, tự động kiểm tra và ghi nhận vào bảng users.",
            test_script=[
                "pm.test(\"Tạo user thành công\", function () {",
                "    pm.expect(pm.response.code).to.be.oneOf([200, 201]);",
                "    var json = pm.response.json();",
                "    if (json.data && json.data.user_id) {",
                "        pm.environment.set(\"userId\", json.data.user_id);",
                "        pm.environment.set(\"pythonUserId\", json.data.user_id);",
                "    }",
                "});"
            ]
        ),
        create_request_item(
            "2.20 [DEMO ONBOARDING] Khởi tạo Ví Heo Đất & Gửi OTP Kích hoạt",
            "POST",
            "{{pythonUrl}}/api/v1/smart-piggy/demo/init-wallet",
            body_str=json.dumps({
                "user_id": "{{userId}}",
                "wallet_name": "Ví Heo Đất Tiết Kiệm Postman"
            }, indent=2, ensure_ascii=False),
            description="Khởi tạo bản ghi ví Heo Đất ở trạng thái PENDING_ACTIVATION và sinh mã OTP lưu DB + gửi mail.",
            test_script=[
                "pm.test(\"Khởi tạo ví thành công\", function () {",
                "    pm.expect(pm.response.code).to.be.oneOf([200, 201]);",
                "    var json = pm.response.json();",
                "    if (json.data && json.data.wallet_id) {",
                "        pm.environment.set(\"walletId\", json.data.wallet_id);",
                "    }",
                "});"
            ]
        ),
        create_request_item(
            "2.21 [DEMO ONBOARDING] Kích hoạt Ví thật bằng mã OTP",
            "POST",
            "{{pythonUrl}}/api/v1/smart-piggy/demo/activate-wallet",
            body_str=json.dumps({
                "wallet_id": "{{walletId}}",
                "otp_code": "123456"
            }, indent=2),
            description="Xác thực mã OTP từ Database / Mail để chuyển trạng thái ví sang ACTIVE."
        ),
        create_request_item(
            "2.22 [DEMO ONBOARDING] Mở khóa Ví Heo Đất (Unfreeze Wallet)",
            "POST",
            "{{pythonUrl}}/api/v1/smart-piggy/demo/unfreeze-wallet",
            body_str=json.dumps({
                "wallet_id": "{{walletId}}"
            }, indent=2),
            description="Phục hồi trạng thái ví từ FROZEN về ACTIVE sau khi đã xử lý xong sự cố an ninh."
        ),
        create_request_item(
            "2.23 [DEMO ONBOARDING] Đập Heo Tất toán & Đóng liên kết (Smash Demo)",
            "POST",
            "{{pythonUrl}}/api/v1/smart-piggy/demo/smash",
            body_str=json.dumps({
                "wallet_id": "{{walletId}}",
                "device_id": "{{piggyDeviceId}}",
                "user_id": "{{userId}}"
            }, indent=2),
            description="Tất toán toàn bộ số dư heo đất, hoàn trả tài chính và ngắt liên kết thiết bị."
        )
    ]

    # Kiểm tra tránh trùng lặp
    existing_names = {item["name"] for item in piggy_folder["item"]}
    added_count = 0
    for req in new_requests:
        if req["name"] not in existing_names:
            piggy_folder["item"].append(req)
            added_count += 1
            
    with open(filepath, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print(f"-> 02_Liochio_Python_Resource_API: Đã thêm {added_count} request mới.")
    return new_requests

def update_core_collection():
    filepath = os.path.join(POSTMAN_DIR, "01_Liochio_Core_Platform_API.postman_collection.json")
    with open(filepath, "r", encoding="utf-8") as f:
        data = json.load(f)

    # Thêm VietQR và Reconcile vào Payment Folder
    pay_folder = None
    for folder in data["item"]:
        if "Thanh Toán" in folder["name"]:
            pay_folder = folder
            break
            
    if pay_folder:
        pay_requests = [
            create_request_item(
                "6.3 Khởi tạo thanh toán VietQR Napas 247 (Create VietQR URL)",
                "POST",
                "{{coreUrl}}/api/payments/create-url",
                body_str=json.dumps({
                    "orderId": "ORD_VIETQR_{{$randomInt}}",
                    "amount": 100000,
                    "orderInfo": "Nap tien vi Heo Dat qua VietQR Napas 247",
                    "paymentMethod": "VIETQR"
                }, indent=2),
                description="Khởi tạo liên kết thanh toán chuẩn VietQR chuyển khoản nhanh Napas 247.",
                is_core=True
            ),
            create_request_item(
                "6.4 Đối soát chủ động đơn hàng (Active Status Reconciliation)",
                "POST",
                "{{coreUrl}}/api/payments/orders/ORD_DEMO_001/reconcile",
                description="Truy vấn đối soát trực tiếp với Gateway ngân hàng để cập nhật trạng thái đơn hàng.",
                is_core=True
            )
        ]
        existing_pay_names = {item["name"] for item in pay_folder["item"]}
        for r in pay_requests:
            if r["name"] not in existing_pay_names:
                pay_folder["item"].append(r)

    # Thêm folder Maker-Checker & 4 Cửa Ải Onboarding
    corp_folder_name = "09. Quản Trị Phê Duyệt Maker-Checker & 4 Cửa Ải Khách Hàng (Corporate Governance)"
    corp_folder = None
    for folder in data["item"]:
        if "Maker-Checker" in folder["name"] or "Corporate" in folder["name"]:
            corp_folder = folder
            break

    corp_requests = [
        create_request_item(
            "9.1 Lấy danh sách tiến trình 4 Cửa ải Onboarding (List Customer Gates)",
            "GET",
            "{{coreUrl}}/api/v1/corp/customer-gates?page=0&size=10",
            description="Xem danh sách tiến trình vượt 4 Cửa ải Onboarding của toàn bộ khách hàng trong hệ thống.",
            is_core=True
        ),
        create_request_item(
            "9.2 Xem chi tiết 4 Cửa ải của User (Get Customer Gate)",
            "GET",
            "{{coreUrl}}/api/v1/corp/customer-gates/{{coreUserId}}",
            description="Xem hồ sơ tiến trình và trạng thái các Gate của một User cụ thể.",
            is_core=True
        ),
        create_request_item(
            "9.3 Thẩm định Gate 1 eKYC (Review Gate 1 eKYC)",
            "POST",
            "{{coreUrl}}/api/v1/corp/customer-gates/{{coreUserId}}/gate1-review",
            body_str=json.dumps({"approved": True, "note": "Hồ sơ CCCD chip đối chiếu hợp lệ với cơ sở dữ liệu quốc gia."}, indent=2, ensure_ascii=False),
            description="Cán bộ Checker thẩm định và phê duyệt bước Định danh eKYC Gate 1.",
            is_core=True
        ),
        create_request_item(
            "9.4 Cấp phát Vai trò & Xếp hạng Gate 2 (Assign Gate 2 Tier)",
            "POST",
            "{{coreUrl}}/api/v1/corp/customer-gates/{{coreUserId}}/gate2-tier",
            body_str=json.dumps({
                "assignedTier": "GOLD",
                "assignedRole": "ROLE_CUSTOMER",
                "dailyLimit": 100000000
            }, indent=2),
            description="Cán bộ Checker chỉ định phân hạng khách hàng (BRONZE/SILVER/GOLD) và cấp hạn mức ngày.",
            is_core=True
        ),
        create_request_item(
            "9.5 Kích hoạt Cặp Tài khoản Sổ cái Gate 3 (Provision Gate 3 Wallets)",
            "POST",
            "{{coreUrl}}/api/v1/corp/customer-gates/{{coreUserId}}/gate3-wallets",
            description="Kích hoạt cặp tài khoản sổ cái kép (Khả dụng + Phong tỏa) trên Ledger Service.",
            is_core=True
        ),
        create_request_item(
            "9.6 Phê chuẩn Ghép đôi Heo Đất Gate 4 (Pair Gate 4 Device)",
            "POST",
            "{{coreUrl}}/api/v1/corp/customer-gates/{{coreUserId}}/gate4-pair",
            body_str=json.dumps({
                "macAddress": "AA:BB:CC:11:22:33",
                "deviceName": "Heo Đất Phê Duyệt Gate 4"
            }, indent=2, ensure_ascii=False),
            description="Phê chuẩn gán thiết bị Heo đất thông minh vào tài khoản sau khi hoàn thành 3 cửa ải trước.",
            is_core=True
        ),
        create_request_item(
            "9.7 Khách hàng tra cứu tiến trình Onboarding 4 Cửa ải (My Onboarding Status)",
            "GET",
            "{{coreUrl}}/api/v1/app/onboarding-status",
            description="Khách hàng tự tra cứu tiến trình 4 Cửa ải định danh của bản thân qua Mobile App / Web Portal.",
            is_core=True
        ),
        create_request_item(
            "9.8 Maker tạo yêu cầu phê duyệt mới (Submit Maker Request)",
            "POST",
            "{{coreUrl}}/api/v1/corp/approvals/submit",
            body_str=json.dumps({
                "requestType": "LIMIT_CHANGE",
                "entityType": "USER_LIMIT",
                "entityId": "1",
                "proposedData": "{\"daily_limit\": 200000000}",
                "reason": "Yêu cầu nâng hạn mức giao dịch cho khách hàng doanh nghiệp"
            }, indent=2, ensure_ascii=False),
            description="Maker khởi tạo hồ sơ yêu cầu phê duyệt chuyển sang trạng thái PENDING.",
            is_core=True
        ),
        create_request_item(
            "9.9 Checker phê duyệt hoặc từ chối yêu cầu (Action Maker-Checker Request)",
            "POST",
            "{{coreUrl}}/api/v1/corp/approvals/1/action",
            body_str=json.dumps({
                "action": "APPROVE",
                "checkerComment": "Đã đối chiếu hồ sơ và đồng ý phê duyệt."
            }, indent=2, ensure_ascii=False),
            description="Checker độc lập xem xét và ra quyết định APPROVE hoặc REJECT.",
            is_core=True
        ),
        create_request_item(
            "9.10 Danh sách yêu cầu phê duyệt Maker-Checker (List Approval Requests)",
            "GET",
            "{{coreUrl}}/api/v1/corp/approvals?status=PENDING&page=0&size=10",
            description="Tra cứu danh sách hồ sơ cần phê duyệt theo trạng thái.",
            is_core=True
        ),
        create_request_item(
            "9.11 Chi tiết yêu cầu phê duyệt (Get Approval Request)",
            "GET",
            "{{coreUrl}}/api/v1/corp/approvals/1",
            description="Xem chi tiết một hồ sơ phê duyệt Maker-Checker.",
            is_core=True
        )
    ]

    if not corp_folder:
        # Chèn trước thư mục Teardown (Cleanup)
        cleanup_idx = len(data["item"]) - 1
        for i, folder in enumerate(data["item"]):
            if "Đăng Xuất" in folder["name"] or "Cleanup" in folder["name"]:
                cleanup_idx = i
                break
        corp_folder = {
            "name": corp_folder_name,
            "item": corp_requests
        }
        data["item"].insert(cleanup_idx, corp_folder)
        print("-> 01_Liochio_Core_Platform_API: Đã tạo mới thư mục Quản trị Phê duyệt Maker-Checker & 4 Cửa ải.")
    else:
        existing_corp_names = {item["name"] for item in corp_folder["item"]}
        for r in corp_requests:
            if r["name"] not in existing_corp_names:
                corp_folder["item"].append(r)

    with open(filepath, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print("-> 01_Liochio_Core_Platform_API: Đã cập nhật thành công.")
    return corp_folder

def update_master_collection():
    filepath = os.path.join(POSTMAN_DIR, "Liochio_Microservices_API.postman_collection.json")
    with open(filepath, "r", encoding="utf-8") as f:
        data = json.load(f)

    # Đọc lại từ 2 collection con đã cập nhật
    with open(os.path.join(POSTMAN_DIR, "01_Liochio_Core_Platform_API.postman_collection.json"), "r", encoding="utf-8") as f:
        core_data = json.load(f)
    with open(os.path.join(POSTMAN_DIR, "02_Liochio_Python_Resource_API.postman_collection.json"), "r", encoding="utf-8") as f:
        py_data = json.load(f)

    # Cập nhật master items
    new_master_items = []
    # Thêm toàn bộ thư mục từ core (trừ cleanup)
    cleanup_folder = None
    for folder in core_data["item"]:
        if "Đăng Xuất" in folder["name"] or "Cleanup" in folder["name"]:
            cleanup_folder = folder
        else:
            new_master_items.append(folder)
            
    # Thêm toàn bộ thư mục từ python
    for folder in py_data["item"]:
        new_master_items.append(folder)
        
    # Thêm cleanup folder ở cuối cùng
    if cleanup_folder:
        new_master_items.append(cleanup_folder)
        
    data["item"] = new_master_items
    with open(filepath, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print("-> Liochio_Microservices_API: Đã hợp nhất và đồng bộ toàn bộ thư mục thành công.")

def update_environment():
    filepath = os.path.join(POSTMAN_DIR, "Liochio_Local_Environment.postman_environment.json")
    with open(filepath, "r", encoding="utf-8") as f:
        data = json.load(f)
        
    existing_keys = {v["key"] for v in data["values"]}
    new_vars = [
        {"key": "allowHardwareBypass", "value": "true", "type": "default", "enabled": True},
        {"key": "virtualDeviceAutoProvision", "value": "true", "type": "default", "enabled": True},
        {"key": "allowDevBypass", "value": "true", "type": "default", "enabled": True}
    ]
    for nv in new_vars:
        if nv["key"] not in existing_keys:
            data["values"].append(nv)
            
    with open(filepath, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print("-> Liochio_Local_Environment: Đã bổ sung các biến cấu hình bypass môi trường kiểm thử.")

if __name__ == "__main__":
    print("Bắt đầu cập nhật toàn bộ Postman Collections...")
    update_python_collection()
    update_core_collection()
    update_master_collection()
    update_environment()
    print("Hoàn tất cập nhật 100% Postman Collections!")
