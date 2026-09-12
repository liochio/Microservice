# 📄 Đường dẫn file: app/schemas/requests/transaction.py
from pydantic import BaseModel, model_validator
from typing import Optional, Any
from datetime import datetime
from decimal import Decimal
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException


class CreateTransactionRequest(BaseModel):
    """
    👑 DTO ĐẦU VÀO TẠO GIAO DỊCH THU / CHI
    🎯 Mục đích:
       - Validate ví tiền, danh mục, số tiền (> 0), loại giao dịch (INCOME / EXPENSE).
    """
    wallet_id: Optional[Any] = None
    category_id: Optional[Any] = None
    amount: Optional[Any] = None
    transaction_type: Optional[Any] = None
    transaction_date: Optional[Any] = None
    description: Optional[str] = None

    @model_validator(mode="after")
    def validate_transaction(self):
        if not self.wallet_id or str(self.wallet_id).strip() == "":
            raise FintechBaseException(error_code="MISSING_WALLET_ID", status_code=400)

        if not self.category_id or str(self.category_id).strip() == "":
            raise FintechBaseException(error_code="MISSING_CATEGORY_ID", status_code=400)

        if self.amount is None:
            raise FintechBaseException(error_code="MISSING_AMOUNT", status_code=400)

        try:
            val = Decimal(str(self.amount))
            if val <= Decimal("0"):
                raise FintechBaseException(error_code="INVALID_AMOUNT_POSITIVE", status_code=400)
            self.amount = float(val)
        except Exception:
            raise FintechBaseException(error_code="INVALID_AMOUNT_POSITIVE", status_code=400)

        if not self.transaction_type or str(self.transaction_type).upper() not in ["INCOME", "EXPENSE"]:
            raise FintechBaseException(error_code="INVALID_TRANSACTION_TYPE", status_code=400)

        self.transaction_type = str(self.transaction_type).upper()
        if not self.transaction_date:
            self.transaction_date = datetime.now()
        elif isinstance(self.transaction_date, str):
            try:
                self.transaction_date = datetime.fromisoformat(self.transaction_date)
            except Exception:
                self.transaction_date = datetime.now()

        return self
