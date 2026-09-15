# 📘 Hướng Dẫn Chuẩn Phát Triển API (Standard API Development Guide)
> **Tài liệu nhập môn & cẩm nang quy chuẩn dành cho lập trình viên Backend (Onboarding & Coding Standards)**

---

## 🧭 1. Tổng Quan Kiến Trúc & Vòng Đời Request (Request Lifecycle)

Hệ thống FinTech Monolith tuân thủ nghiêm ngặt mô hình kiến trúc **Clean Layered Architecture (Kiến trúc phân tầng sạch)**. Mọi Request từ Client gửi lên bắt buộc phải đi qua tuần tự các tầng phòng thủ và xử lý theo luồng dưới đây:

'''mermaid
sequenceDiagram
    autonumber
    actor Client as Client (Web / App)
    participant MW as 3-Stage Middleware
    participant Guard as Security Guard
    participant Schema as Request Schema (DTO)
    participant Router as API Router Layer
    participant Service as Service / Processor
    participant Repo as Repository Layer
    participant DB as Database (MySQL/Redis)
    participant i18n as i18n Translator & Handler

    Client->>MW: Gửi HTTP Request (Header: Auth, Accept-Language, Idempotency-Key)
    Note over MW: Gắn Trace-ID, đếm Rate Limit, ghi nhật ký WAF Context
    MW->>Guard: Chuyển Request vào tầng Security Guard
    Note over Guard: Giải mã JWT token, soi quyền PermissionGuard / RoleBasedGuard
    Guard->>Schema: Validate dữ liệu đầu vào (DTO Pydantic Model)
    Note over Schema: Chặn dữ liệu rác, kiểm tra độ dài, XSS, Regex, Missing Fields
    Schema->>Router: Dữ liệu sạch được nạp vào Controller
    Router->>Service: Gọi hàm nghiệp vụ (Truyền DB Connection, Context, DTO)
    Note over Service: Xử lý logic nghiệp vụ, tính toán tài chính, kiểm soát Transaction ACID
    Service->>Repo: Triệu hồi truy vấn dữ liệu chuẩn hóa
    Repo->>DB: Thực thi truy vấn SQL / ORM / Row Locking
    DB-->>Repo: Trả về kết quả thô
    Repo-->>Service: Chuyển đổi dữ liệu sang Python Dictionary / Model Domain
    Service-->>Router: Trả về Dữ liệu kết quả nghiệp vụ thành công
    Router-->>Client: Trả về JSON Response chuẩn (Mã HTTP, Trace-ID, Message đa ngôn ngữ)

    Note over Schema,Service: Nếu có lỗi bất kỳ: Ném FintechBaseException
    Service-->>i18n: Ném FintechBaseException(error_code=...)
    i18n-->>Client: Tự động dịch mã lỗi theo ngôn ngữ Client và trả về Response 4xx/5xx
'''

---

## 🚫 2. Bốn Nguyên Tắc "Bất Di Bất Dịch" (Zero Tolerance Rules)

1. **KHÔNG BAO GIỜ code cứng chuỗi ký tự (Zero Magic Strings)**:
   - CẤM: 'error_code="WALLET_NOT_FOUND"', 'msg_type="MESSAGE"', 'PermissionGuard("WALLET_CREATE")'.
   - BẮT BUỘC: 'error_code=SystemConstants.WALLET_NOT_FOUND', 'msg_type=SystemConstants.MSG_TYPE_MESSAGE', 'PermissionGuard(SystemConstants.WALLET_CREATE)'.
2. **Đa ngôn ngữ là bắt buộc (i18n First)**:
   - Mọi mã lỗi ('error_code') hoặc mã thông điệp ('message_code') khi tạo mới **bắt buộc** phải được định nghĩa ngay trong 3 file từ điển: 'i18n/vi/', 'i18n/en/', 'i18n/zh/'.
3. **Tuyệt đối không viết truy vấn SQL trong Router hoặc Service**:
   - CẤM: 'db_conn.execute("SELECT * FROM users WHERE id = ...")' trong file Router hay Service.
   - BẮT BUỘC: Khai báo hàm trong 'app/repositories/' và gọi thông qua Repository.
4. **Tuyệt đối không bắt lỗi chung chung (No Bare Except)**:
   - CẤM: 'except:' hoặc 'except Exception: pass' mà không ghi log hay rollback.
   - BẮT BUỘC: Luôn Rollback Transaction khi gặp lỗi, ghi log kiểm toán và ném 'FintechBaseException'.

---

## 🛠️ 3. Quy Trình 7 Bước Chuẩn Để Xây Dựng Một API Mới

Khi nhận yêu cầu viết một API mới (Ví dụ: Tạo ví tài chính 'POST /api/v1/wallets'), lập trình viên phải thực hiện chuẩn chỉ **7 bước** theo thứ tự từ gốc lên ngọn:

'''
[Bước 1: SystemConstants] --> [Bước 2: Từ điển i18n] --> [Bước 3: Schema DTO]
                                                                  |
[Bước 6: Router API] <----- [Bước 5: Service Logic] <---- [Bước 4: Model & Repo]
        |
[Bước 7: Testcase & Kiểm thử]
'''

---

### 📍 Bước 1: Khai Báo Hằng Số Tại 'app/constants.py'

- **Tệp tin**: 'app/constants.py'
- **Cần làm gì?**: Khai báo tên Module, Quyền hạn (Permission), Mã thành công và các Mã lỗi nghiệp vụ có thể xảy ra.
- **Mã nguồn mẫu**:
'''python
class SystemConstants:
    # 1. Module Code & Permission Code
    MODULE_WALLET_MGMT = "WALLET_MGMT"
    WALLET_CREATE = "WALLET_CREATE"

    # 2. Mã thành công
    WALLET_CREATE_SUCCESS = "WALLET_CREATE_SUCCESS"

    # 3. Mã lỗi nghiệp vụ
    MISSING_WALLET_CODE = "MISSING_WALLET_CODE"
    MISSING_WALLET_NAME = "MISSING_WALLET_NAME"
    WALLET_ALREADY_EXISTS = "WALLET_ALREADY_EXISTS"
    INVALID_CURRENCY_ENUM = "INVALID_CURRENCY_ENUM"
'''
- **Vì sao phải làm?**: Đảm bảo tất cả các file trong dự án (Guard, Schema, Service, Testcase) dùng chung 1 nguồn chân lý (Single Source of Truth), tránh lỗi gõ sai chính tả (typo).
- **Có bỏ qua được không?**: **KHÔNG**. Nếu bỏ qua và gõ chuỗi cứng, bộ kiểm tra tự động (AST Scanner) sẽ chặn commit.

---

### 📍 Bước 2: Cập Nhật Từ Điển Đa Ngôn Ngữ i18n ('vi', 'en', 'zh')

- **Tệp tin**:
  - Tiếng Việt: 'i18n/vi/errors.json', 'i18n/vi/messages.json', 'i18n/vi/labels.json'
  - Tiếng Anh: 'i18n/en/errors.json', 'i18n/en/messages.json', 'i18n/en/labels.json'
  - Tiếng Trung: 'i18n/zh/errors.json', 'i18n/zh/messages.json', 'i18n/zh/labels.json'
- **Cần làm gì?**: Bổ sung bản dịch tương ứng cho các hằng số vừa tạo ở Bước 1.
- **Mã nguồn mẫu (i18n/vi/errors.json)**:
'''json
{
  "MISSING_WALLET_CODE": "Mã ví không được để trống.",
  "MISSING_WALLET_NAME": "Tên ví không được để trống.",
  "WALLET_ALREADY_EXISTS": "Mã ví này đã tồn tại trên hệ thống.",
  "INVALID_CURRENCY_ENUM": "Đơn vị tiền tệ không hợp lệ (chỉ hỗ trợ VND, USD, EUR, CNY)."
}
'''
- **Mã nguồn mẫu (i18n/vi/messages.json)**:
'''json
{
  "WALLET_CREATE_SUCCESS": "Khởi tạo ví tài chính thành công!"
}
'''
- **Vì sao phải làm?**: Đảm bảo khi Client truyền Header 'Accept-Language: en' hoặc 'vi', hệ thống tự động trả về câu thông báo thân thiện tương ứng thay vì trả về mã code vô nghĩa.
- **Có bỏ qua được không?**: **KHÔNG**. Nếu thiếu, hệ thống sẽ trả về mã code thô hoặc thông báo mặc định.

---

### 📍 Bước 3: Xây Dựng Request & Response Schemas (DTO)

- **Tệp tin**: 'app/schemas/requests/wallet.py'
- **Cần làm gì?**: Sử dụng Pydantic 'BaseModel' kết hợp '@model_validator(mode="after")' để tạo **Ma trận phòng thủ tuần tự (Sequential Defense Matrix)**.
- **Mã nguồn mẫu**:
'''python
from typing import Optional, Any
from pydantic import BaseModel, model_validator
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException


class CreateWalletRequest(BaseModel):
    wallet_code: Optional[Any] = None
    name: Optional[Any] = None
    currency: Optional[Any] = "VND"
    wallet_type: Optional[Any] = "CASH"
    color: Optional[Any] = None
    icon: Optional[Any] = None
    description: Optional[Any] = None

    @model_validator(mode="after")
    def sequential_wallet_validation_pipeline(self):
        # 1. Missing Checks
        if not self.wallet_code:
            raise FintechBaseException(error_code=SystemConstants.MISSING_WALLET_CODE, status_code=400)
        if not self.name:
            raise FintechBaseException(error_code=SystemConstants.MISSING_WALLET_NAME, status_code=400)

        # 2. Format Checks
        self.wallet_code = str(self.wallet_code).strip()
        if len(self.wallet_code) > 50:
            raise FintechBaseException(error_code=SystemConstants.INVALID_WALLET_CODE_LENGTH, status_code=400)

        self.name = str(self.name).strip()
        if len(self.name) > 100:
            raise FintechBaseException(error_code=SystemConstants.INVALID_WALLET_NAME_LENGTH, status_code=400)

        self.currency = str(self.currency).strip().upper()
        if self.currency not in ["VND", "USD", "EUR", "CNY"]:
            raise FintechBaseException(error_code=SystemConstants.INVALID_CURRENCY_ENUM, status_code=400)

        self.wallet_type = str(self.wallet_type).strip().upper()
        return self
'''
- **Vì sao phải làm?**: Chặn đứng 100% dữ liệu rác, tấn công XSS, SQLi, số âm... ngay tại cổng vào của ứng dụng trước khi lọt xuống Service hay Database.
- **Có bỏ qua được không?**: **KHÔNG**. Không bao giờ tin tưởng dữ liệu từ Client gửi lên.

---

### 📍 Bước 4: Định Nghĩa Model ORM & Repository Truy Vấn

- **Tệp tin**: 'app/models/wallet/wallet.py' & 'app/repositories/wallet/wallet_repository.py'
- **Cần làm gì?**: 
  1. Khai báo Model bảng CSDL trong 'app/models/'.
  2. Tạo hàm truy vấn CSDL thuần túy trong 'app/repositories/'.
- **Mã nguồn mẫu (Repository)**:
'''python
import uuid
from typing import Optional
from sqlalchemy import text
from app.models.wallet.wallet import Wallet


class WalletRepository:
    @staticmethod
    def create_wallet(db_conn, user_id: str, wallet_code: str, name: str,
                      currency: str, wallet_type: str = "CASH",
                      color: Optional[str] = None, icon: Optional[str] = None,
                      description: Optional[str] = None) -> Wallet:
        """Thực thi tạo bản ghi ví mới qua raw SQL an toàn"""
        wallet_id = str(uuid.uuid4())
        stmt = text("""
            INSERT INTO wallets (id, user_id, wallet_code, name, currency, wallet_type,
                                 balance, color, icon, description, status, is_deleted, created_at, updated_at)
            VALUES (:id, :u_id, :code, :name, :cur, :w_type, 0.0, :col, :icon, :desc, 'ACTIVE', 0, NOW(), NOW())
        """)
        db_conn.execute(stmt, {
            "id": wallet_id, "u_id": user_id, "code": wallet_code, "name": name,
            "cur": currency, "w_type": wallet_type, "col": color, "icon": icon, "desc": description
        })
        return Wallet(id=wallet_id, user_id=user_id, wallet_code=wallet_code, name=name, currency=currency)
'''
- **Vì sao phải làm?**: Đóng gói toàn bộ logic truy vấn SQL/ORM tại tầng Repository. Nếu sau này CSDL thay đổi cấu trúc bảng, chỉ cần sửa tại Repo mà không ảnh hưởng tới tầng Service hay API Router.
- **Có bỏ qua được không?**: **KHÔNG**. Tuyệt đối không gọi truy vấn SQL trực tiếp trong Service hoặc Router.

---

### 📍 Bước 5: Xây Dựng Tầng Nghiệp Vụ (Service / Processor Layer)

- **Tệp tin**: 'app/services/wallet/wallet_service.py'
- **Cần làm gì?**: Triển khai logic tính toán, kiểm tra trùng lặp qua 'GenericValidator', gọi Repository, quản lý Commit/Rollback và ghi Log kiểm toán.
- **Mã nguồn mẫu**:
'''python
from typing import Dict, Any, Optional
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException
from app.core.validators.generic_validator import GenericValidator
from app.models.wallet.wallet import Wallet
from app.repositories.wallet.wallet_repository import WalletRepository
from app.core.logging.logger import DBLogger


class WalletService:
    @staticmethod
    def create_user_wallet(
            db_conn, user_id: str, wallet_code: str, name: str, currency: str,
            wallet_type: str, color: Optional[str], icon: Optional[str],
            description: Optional[str]
    ) -> Dict[str, Any]:
        try:
            # 1. Validation Logic kiểm tra trùng mã ví
            GenericValidator.check_duplicate(
                db_conn=db_conn, model_class=Wallet, field_name="wallet_code",
                value=wallet_code, error_code=SystemConstants.WALLET_ALREADY_EXISTS
            )

            # 2. Gọi Repository thực thi
            WalletRepository.create_wallet(
                db_conn=db_conn, user_id=user_id, wallet_code=wallet_code,
                name=name, currency=currency, wallet_type=wallet_type,
                color=color, icon=icon, description=description
            )

            # 3. Commit dữ liệu an toàn
            if hasattr(db_conn, "commit"):
                db_conn.commit()

            return {"status": "SUCCESS", "wallet_code": wallet_code}

        except FintechBaseException as f_exc:
            if hasattr(db_conn, "rollback"):
                db_conn.rollback()
            raise f_exc
        except Exception as e:
            if hasattr(db_conn, "rollback"):
                db_conn.rollback()
            DBLogger.system(db_conn, "ERROR", "WalletService", f"Lỗi: {str(e)}")
            raise FintechBaseException(SystemConstants.INTERNAL_SERVER_ERROR, 500)
'''
- **Vì sao phải làm?**: Đảm bảo toàn vẹn dữ liệu (ACID). Nếu xảy ra lỗi giữa chừng, toàn bộ thay đổi sẽ được Rollback tự động, không để lại dữ liệu rác trong CSDL.
- **Có bỏ qua được không?**: **KHÔNG**. Tầng Service là trái tim của hệ thống.

---

### 📍 Bước 6: Tạo API Router & Gắn Security Guard

- **Tệp tin**: 'app/api/v1/wallets/wallet.py'
- **Cần làm gì?**: Khai báo endpoint FastAPI, tiêm 'db_conn' qua Dependency 'get_db', áp dụng 'PermissionGuard', và trả về JSON Response chuẩn đa ngôn ngữ.
- **Mã nguồn mẫu**:
'''python
from typing import Any
from fastapi import APIRouter, Request, Depends, status
from fastapi.responses import JSONResponse
from app.constants import SystemConstants
from app.dependency import get_db
from app.core.security.guard.guards import get_current_user, PermissionGuard
from app.core.translator.translator_engine import i18n_translator
from app.schemas.requests.wallet import CreateWalletRequest
from app.services.wallet.wallet_service import WalletService

router = APIRouter(prefix="/wallets", tags=["Wallet Management"], dependencies=[Depends(get_current_user)])


@router.post("", status_code=status.HTTP_201_CREATED, summary="Tạo ví tài chính mới")
async def create_wallet(
    payload: CreateWalletRequest,
    request: Request,
    db_conn: Any = Depends(get_db),
    current_user: dict = Depends(PermissionGuard(SystemConstants.WALLET_CREATE))
):
    user_id = current_user["user_id"]
    result = WalletService.create_user_wallet(
        db_conn=db_conn,
        user_id=user_id,
        wallet_code=payload.wallet_code,
        name=payload.name,
        currency=payload.currency,
        wallet_type=payload.wallet_type,
        color=payload.color,
        icon=payload.icon,
        description=payload.description
    )

    translated_msg = i18n_translator.translate(
        request,
        error_code=SystemConstants.WALLET_CREATE_SUCCESS,
        msg_type=SystemConstants.MSG_TYPE_MESSAGE
    )

    return JSONResponse(
        status_code=status.HTTP_201_CREATED,
        content={
            "success": True,
            "message": translated_msg,
            "error_code": None,
            "data": result,
            "trace_id": getattr(request.state, "trace_id", "UNKNOWN")
        }
    )
'''
- **Vì sao phải làm?**: Bảo vệ API trước các truy cập trái phép hoặc không đủ quyền, đồng thời chuẩn hóa định dạng JSON Response cho Frontend.
- **Có bỏ qua được không?**: **KHÔNG**. Không bao giờ mở API mà không có xác thực hoặc không trả về theo chuẩn Response.

---

### 📍 Bước 7: Kiểm Thử & Ghi Nhận Ma Trận Testcase

- **Tệp tin**: 'docs/testing/api_test_cases.md'
- **Cần làm gì?**: Bổ sung các kịch bản kiểm thử:
  1. **Happy Path (HP)**: Dữ liệu chuẩn -> 201 Created.
  2. **Negative Path (NE)**: Thiếu tên, số tiền âm -> 400 Bad Request.
  3. **Security (SEC)**: Không có Token -> 401 Unauthorized; Thiếu quyền -> 403 Forbidden.
- **Kiểm thử trực tiếp**:
  - Mở Swagger UI tại: [http://localhost:8000/docs](http://localhost:8000/docs) để test trực quan.

---

## 📊 4. Ma Trận Giải Đáp: "Làm Gì? Vì Sao? Bỏ Qua Được Không?"

| Tầng kiến trúc | Phải làm gì? | Vì sao phải làm? | Bỏ qua được không? | Hậu quả nếu cố tình bỏ qua |
| :--- | :--- | :--- | :---: | :--- |
| **1. SystemConstants** | Khai báo hằng số tập trung cho quyền, lỗi, module | Tránh magic string, tránh typo, quản lý tập trung | CẤM BỎ | Code vỡ cấu trúc, cảnh báo lint, không kiểm soát được mã lỗi |
| **2. Từ điển i18n** | Khai báo bản dịch vi, en, zh | Hiển thị thông báo thân thiện cho Client | CẤM BỎ | Client nhận mã code thô, trải nghiệm người dùng kém |
| **3. Request Schema** | Tạo Pydantic DTO với validation tuần tự | Chặn đứng dữ liệu rác, tấn công XSS/SQLi | CẤM BỎ | Lỗi 500 nổ tràn lan khi dữ liệu đầu vào bị null hoặc sai kiểu |
| **4. Security Guard** | Gắn PermissionGuard / RoleBasedGuard | Kiểm soát phân quyền và xác thực người dùng | CẤM BỎ | Lỗ hổng bảo mật nghiêm trọng (IDOR, leo thang đặc quyền) |
| **5. Service Layer** | Xử lý nghiệp vụ, bọc Transaction Rollback | Bảo vệ tính toàn vẹn dữ liệu tài chính (ACID) | CẤM BỎ | Dữ liệu bị rách (nửa chừng thành công, thất bại), kẹt tiền |
| **6. Repository Layer** | Viết các hàm thực thi truy vấn SQL/ORM | Phân tách logic lưu trữ và nghiệp vụ | CẤM BỎ | Phá vỡ Clean Architecture, khó bảo trì và không thể Unit Test |
| **7. JSON Response** | Trả về response JSON chuẩn đa ngôn ngữ | Chuẩn hóa cấu trúc JSON, đính kèm Trace-ID | CẤM BỎ | Frontend không thể bắt lỗi đồng bộ, mất khả năng truy vết log |

---

## 🎯 5. Checklist Kiểm Tra Trước Khi Gửi Pull Request (PR Review Checklist)

Trước khi hoàn thành một API, hãy tự đối chiếu danh sách kiểm tra sau:

- [ ] Đã khai báo toàn bộ mã lỗi và quyền hạn trong 'app/constants.py' (Không còn chuỗi '""' nào).
- [ ] Đã có đủ bản dịch trong cả 3 file: 'i18n/vi/*.json', 'i18n/en/*.json', 'i18n/zh/*.json'.
- [ ] Schema Request có Pydantic '@model_validator' chặn missing check và format check.
- [ ] Router API có gắn 'PermissionGuard' hoặc 'get_current_user'.
- [ ] Service có khối 'try...except FintechBaseException' kèm lệnh 'commit()' và 'rollback()'.
- [ ] Không có truy vấn SQL thô trong Router hoặc Service (Phải gọi qua Repository).
- [ ] Đã xóa sạch các import thừa (Unused Imports) - IDE không còn cảnh báo vàng.
- [ ] Đã chạy 'python -m compileall app/' và không có bất kỳ lỗi cú pháp nào.
- [ ] Đã bổ sung ma trận testcase vào file 'docs/testing/api_test_cases.md'.

---
© 2026 FinTech Monolith Core Engineering. All rights reserved.
