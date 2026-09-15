
from pydantic import BaseModel, model_validator
from typing import Optional, Any
from datetime import date
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException
import re


class UpdateProfileRequest(BaseModel):
    """👑 DTO CẬP NHẬT HỒ SƠ CÁ NHÂN"""
    full_name: Optional[Any] = None
    date_of_birth: Optional[Any] = None
    gender: Optional[Any] = None

    @model_validator(mode="after")
    def validate_profile(self):
        if self.full_name is not None:
            self.full_name = str(self.full_name).strip()
            if len(self.full_name) < 2 or len(self.full_name) > 100:
                raise FintechBaseException(error_code=SystemConstants.INVALID_FULL_NAME_LENGTH, status_code=400)
            if any(char in self.full_name for char in ["<", ">", "script", "javascript"]):
                raise FintechBaseException(error_code=SystemConstants.XSS_DETECTED_IN_NAME, status_code=400)

        if self.date_of_birth is not None and isinstance(self.date_of_birth, str):
            try:
                self.date_of_birth = date.fromisoformat(self.date_of_birth)
            except ValueError:
                raise FintechBaseException(error_code=SystemConstants.INVALID_DATE_FORMAT, status_code=400)

        if self.gender is not None and str(self.gender).upper() not in ["MALE", "FEMALE", "OTHER"]:
            raise FintechBaseException(error_code=SystemConstants.INVALID_GENDER_ENUM, status_code=400)
        return self


class ChangePasswordRequest(BaseModel):
    """👑 DTO ĐỔI MẬT KHẨU"""
    old_password: Optional[Any] = None
    new_password: Optional[Any] = None
    confirm_password: Optional[Any] = None

    @model_validator(mode="after")
    def validate_passwords(self):
        if not self.old_password:
            raise FintechBaseException(error_code=SystemConstants.MISSING_PASSWORD, status_code=400)
        if not self.new_password:
            raise FintechBaseException(error_code=SystemConstants.MISSING_PASSWORD, status_code=400)
        if not self.confirm_password:
            raise FintechBaseException(error_code=SystemConstants.MISSING_CONFIRM_PASSWORD, status_code=400)

        if len(self.new_password) < 8 or len(self.new_password) > 32:
            raise FintechBaseException(error_code=SystemConstants.INVALID_PASSWORD_LENGTH, status_code=400)

        if not re.match(r"^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$", self.new_password):
            raise FintechBaseException(error_code=SystemConstants.WEAK_PASSWORD, status_code=400)

        if self.new_password != self.confirm_password:
            raise FintechBaseException(error_code=SystemConstants.PASSWORD_MISMATCH, status_code=400)

        if self.old_password == self.new_password:
            raise FintechBaseException(error_code="NEW_PASSWORD_SAME_AS_OLD", status_code=400)
        return self


class ForgotPasswordRequest(BaseModel):
    """👑 DTO QUÊN MẬT KHẨU - YÊU CẦU OTP"""
    email: Optional[Any] = None

    @model_validator(mode="after")
    def validate_email(self):
        if not self.email or str(self.email).strip() == "":
            raise FintechBaseException(error_code=SystemConstants.MISSING_EMAIL, status_code=400)
        self.email = str(self.email).strip().lower()
        if not re.match(r"^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$", self.email):
            raise FintechBaseException(error_code=SystemConstants.INVALID_EMAIL_FORMAT, status_code=400)
        return self


class ResetPasswordRequest(BaseModel):
    """👑 DTO ĐẶT LẠI MẬT KHẨU BẰNG OTP"""
    email: Optional[Any] = None
    otp_code: Optional[Any] = None
    new_password: Optional[Any] = None
    confirm_password: Optional[Any] = None

    @model_validator(mode="after")
    def validate_reset(self):
        if not self.email or not self.otp_code or not self.new_password or not self.confirm_password:
            raise FintechBaseException(error_code=SystemConstants.INVALID_VERIFY_OTP_PAYLOAD, status_code=400)
        if self.new_password != self.confirm_password:
            raise FintechBaseException(error_code=SystemConstants.PASSWORD_MISMATCH, status_code=400)
        if len(self.new_password) < 8:
            raise FintechBaseException(error_code=SystemConstants.INVALID_PASSWORD_LENGTH, status_code=400)
        return self
