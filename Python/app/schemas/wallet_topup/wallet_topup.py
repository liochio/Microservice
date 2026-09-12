# D:\UIT - HK2\FinanceProject\app\schemas\wallet_topup\wallet_topup.py

import re

from decimal import Decimal
from typing import Optional

from pydantic import BaseModel, Field, field_validator


class WalletTopupRequest(BaseModel):
    """👑 SCHEMA KIỂM ĐỊNH NẠP TIỀN QUA SỐ TÀI KHOẢN BỌC THÉP"""

    wallet_account: str = Field(
        ...,
        min_length=10,
        max_length=10,
        description="Số tài khoản ví nhận tiền (10 chữ số)"
    )

    bank_name: str = Field(
        ...,
        min_length=2,
        max_length=100,
        description="Tên ngân hàng định danh (vd: Vietcombank)"
    )

    account_number: str = Field(
        ...,
        min_length=5,
        max_length=50,
        description="Số tài khoản ngân hàng giả lập"
    )

    account_name: str = Field(
        ...,
        min_length=2,
        max_length=255,
        description="Tên chủ tài khoản ngân hàng viết hoa không dấu"
    )

    amount: Decimal = Field(
        ...,
        description="Số tiền cần nạp"
    )

    description: Optional[str] = Field(
        None,
        max_length=150,
        description="Nội dung giao dịch"
    )

    @field_validator("amount")
    @classmethod
    def validate_amount_rules(cls, value: Decimal) -> Decimal:
        if value <= 0:
            raise ValueError("INVALID_AMOUNT_POSITIVE")

        if value > Decimal("100000000"):
            raise ValueError("INVALID_AMOUNT_MAX_LIMIT")

        value_str = str(value)
        if "." in value_str:
            decimals = value_str.split(".")[1]
            if len(decimals) > 4:
                raise ValueError("INVALID_AMOUNT_DECIMALS")

        return value

    @field_validator("wallet_account")
    @classmethod
    def validate_wallet_account(cls, value: str) -> str:
        if not value.isdigit():
            raise ValueError("INVALID_WALLET_ACCOUNT_DIGITS")
        return value.strip()

    @field_validator("account_number")
    @classmethod
    def validate_account_number(cls, value: str) -> str:
        if not re.match(r"^[0-9A-Za-z]+$", value):
            raise ValueError("INVALID_ACCOUNT_NUMBER_FORMAT")
        return value.strip()

    @field_validator("bank_name")
    @classmethod
    def validate_bank_name(cls, value: str) -> str:
        clean = value.strip()
        if len(clean) < 2:
            raise ValueError("INVALID_BANK_NAME_FORMAT")
        return clean

    @field_validator("account_name")
    @classmethod
    def validate_account_name(cls, value: str) -> str:
        clean = value.strip()
        if len(clean) < 2:
            raise ValueError("INVALID_ACCOUNT_NAME_FORMAT")
        return clean.upper()

    @field_validator("description")
    @classmethod
    def sanitize_and_clean_xss(cls, value: Optional[str]) -> Optional[str]:
        if not value:
            return None

        clean = re.sub(r"<[^>]*>", "", value)
        clean = (
            clean
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace('"', "&quot;")
        )
        return clean.strip()


class WalletTopupResponse(BaseModel):
    """👑 SCHEMA ĐẦU RA PHẲNG LỲ KHÔNG CẢNH BÁO"""

    success: bool
    transaction_code: str
    amount: Decimal
    balance_before: Decimal
    balance_after: Decimal
    wallet_currency: str
    status: str
    message: str