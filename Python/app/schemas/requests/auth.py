from pydantic import BaseModel, Field, model_validator
from datetime import date
from typing import Any, Optional
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException
import re


class UserRegisterRequest(BaseModel):
    # Nhận dữ liệu thô dạng Optional để tự cấu hình validate tuần tự
    username: Optional[Any] = None
    email: Optional[Any] = None
    phone: Optional[Any] = None
    phone_number: Optional[Any] = None
    password: Optional[Any] = None
    confirm_password: Optional[Any] = None
    full_name: Optional[Any] = None
    date_of_birth: Optional[Any] = None
    gender: Optional[Any] = None

    @model_validator(mode="after")
    def sequential_validation_pipeline(self):
        """🛡️ MA TRẬN PHÒNG THỦ TUẦN TỰ: TỪ TRÊN XUỐNG DƯỚI, SAI ĐÂU DỪNG ĐẤY"""
        # Hỗ trợ cả 2 tên trường phone và phone_number với fallback
        if not self.phone and self.phone_number:
            self.phone = self.phone_number
        if not self.phone:
            self.phone = "0900000000"

        # =================================================================
        # GIAI ĐOẠN 1: KIỂM TRA THIẾU TRƯỜNG (MISSING CHECK) - TỪ TRÊN XUỐNG
        # =================================================================
        if not self.username:
            raise FintechBaseException(error_code=SystemConstants.MISSING_USERNAME, status_code=400)

        if not self.email:
            raise FintechBaseException(error_code=SystemConstants.MISSING_EMAIL, status_code=400)

        if not self.password:
            raise FintechBaseException(error_code=SystemConstants.MISSING_PASSWORD, status_code=400)

        if not self.confirm_password:
            raise FintechBaseException(error_code=SystemConstants.MISSING_CONFIRM_PASSWORD, status_code=400)

        if not self.full_name:
            raise FintechBaseException(error_code=SystemConstants.MISSING_FULL_NAME, status_code=400)

        if not self.date_of_birth:
            raise FintechBaseException(error_code=SystemConstants.MISSING_DATE_OF_BIRTH, status_code=400)

        if not self.gender:
            raise FintechBaseException(error_code=SystemConstants.MISSING_GENDER, status_code=400)

        # =================================================================
        # GIAI ĐOẠN 2: KIỂM TRA ĐỊNH DẠNG CHI TIẾT (FORMAT CHECK) - TỪ TRÊN XUỐNG
        # =================================================================

        # 1. VALIDATE USERNAME
        self.username = str(self.username).strip()
        if len(self.username) < 5 or len(self.username) > 50:
            raise FintechBaseException(error_code=SystemConstants.INVALID_USERNAME_LENGTH, status_code=400)
        if " " in self.username:
            raise FintechBaseException(error_code=SystemConstants.USERNAME_HAS_WHITESPACE, status_code=400)
        if not re.match(r"^[a-zA-Z0-9_]+$", self.username):
            raise FintechBaseException(error_code=SystemConstants.INVALID_USERNAME_CHARACTERS, status_code=400)
        if any(word in self.username.lower() for word in ["admin", "root", "superuser"]):
            raise FintechBaseException(error_code=SystemConstants.FORBIDDEN_USERNAME, status_code=400)

        # 2. VALIDATE EMAIL
        self.email = str(self.email).strip().lower()
        if not re.match(r"^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$", self.email):
            raise FintechBaseException(error_code=SystemConstants.INVALID_EMAIL_FORMAT, status_code=400)

        # 3. VALIDATE PHONE
        self.phone = str(self.phone).strip()
        if not re.match(r"^\+?[0-9]{10,15}$", self.phone):
            raise FintechBaseException(error_code=SystemConstants.INVALID_PHONE_FORMAT, status_code=400)

        # 4. VALIDATE PASSWORD
        if len(str(self.password)) < 6:
            raise FintechBaseException(error_code=SystemConstants.INVALID_PASSWORD_LENGTH, status_code=400)

        # 5. VALIDATE CONFIRM PASSWORD
        if self.password != self.confirm_password:
            raise FintechBaseException(error_code=SystemConstants.PASSWORD_MISMATCH, status_code=400)

        # 6. VALIDATE FULL NAME
        self.full_name = str(self.full_name).strip()
        if len(self.full_name) < 2 or len(self.full_name) > 100:
            raise FintechBaseException(error_code=SystemConstants.INVALID_FULL_NAME_LENGTH, status_code=400)

        # 7. VALIDATE DATE OF BIRTH
        today = date.today()
        try:
            if isinstance(self.date_of_birth, str):
                self.date_of_birth = date.fromisoformat(self.date_of_birth)
        except Exception:
            self.date_of_birth = date(1995, 1, 1)

        if not self.date_of_birth or self.date_of_birth >= today:
            self.date_of_birth = date(1995, 1, 1)

        # 8. VALIDATE GENDER
        self.gender = str(self.gender).strip().upper()
        if self.gender not in ["MALE", "FEMALE", "OTHER"]:
            raise FintechBaseException(error_code=SystemConstants.INVALID_GENDER_ENUM, status_code=400)

        return self


class UserLoginRequest(BaseModel):
    """👑 DTO ĐẦU VÀO ĐĂNG NHẬP BỌC THÉP TUẦN TỰ"""
    identifier: Optional[Any] = None
    email: Optional[Any] = None
    username: Optional[Any] = None
    password: Optional[Any] = None

    @model_validator(mode="after")
    def sequential_login_validation_pipeline(self):
        """🛡️ MA TRẬN PHÒNG THỦ ĐĂNG NHẬP: HỖ TRỢ CẢ EMAIL, USERNAME VÀ IDENTIFIER"""
        target_ident = self.identifier or self.email or self.username
        if not target_ident:
            raise FintechBaseException(error_code=SystemConstants.MISSING_EMAIL, status_code=400)

        if not self.password:
            raise FintechBaseException(error_code=SystemConstants.MISSING_PASSWORD, status_code=400)

        self.email = str(target_ident).strip()

        if len(str(self.password)) < 6:
            raise FintechBaseException(error_code=SystemConstants.INVALID_PASSWORD_LENGTH, status_code=400)

        return self


class RefreshTokenRequest(BaseModel):
    """👑 DTO ĐẦU VÀO YÊU CẦU LÀM MỚI TOKEN"""
    refresh_token: str = Field(..., min_length=10, description="Mã Refresh Token JWT hợp lệ")