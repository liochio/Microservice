# 📄 Đường dẫn file: app/schemas/requests/transfer.py
from pydantic import BaseModel, model_validator
from typing import Optional, Any
from decimal import Decimal
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException


class CreateTransferRequest(BaseModel):
    """
    👑 DTO ĐẦU VÀO CHUYỂN TIỀN NỘI BỘ GIỮA CÁC VÍ
    🎯 Mục đích:
       - Validate ví nguồn, ví đích, số tiền chuyển (> 0), chống chuyển cùng 1 ví.
    """
    source_wallet_id: Optional[Any] = None
    destination_wallet_id: Optional[Any] = None
    amount: Optional[Any] = None
    description: Optional[str] = "Chuyển tiền nội bộ giữa các ví"

    @model_validator(mode="after")
    def validate_transfer(self):
        if not self.source_wallet_id:
            raise FintechBaseException(error_code="MISSING_SOURCE_WALLET_ID", status_code=400)
        if not self.destination_wallet_id:
            raise FintechBaseException(error_code="MISSING_DESTINATION_WALLET_ID", status_code=400)

        if str(self.source_wallet_id).strip() == str(self.destination_wallet_id).strip():
            raise FintechBaseException(error_code="SAME_SOURCE_DESTINATION_WALLET", status_code=400)

        if self.amount is None:
            raise FintechBaseException(error_code="MISSING_AMOUNT", status_code=400)

        try:
            val = Decimal(str(self.amount))
            if val <= Decimal("0"):
                raise FintechBaseException(error_code="INVALID_AMOUNT_POSITIVE", status_code=400)
            self.amount = float(val)
        except Exception:
            raise FintechBaseException(error_code="INVALID_AMOUNT_POSITIVE", status_code=400)

        return self
