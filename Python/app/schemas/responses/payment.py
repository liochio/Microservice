# 📄 Đường dẫn file: app/schemas/responses/payment.py
from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime
from app.schemas.responses.base_response import BaseResponse


class VietQrData(BaseModel):
    order_id: str
    amount: float
    currency: str = "VND"
    bank_code: str
    account_number: str
    account_name: str
    transfer_content: str
    vietqr_url: str
    qr_quicklink: str


class VietQrResponse(BaseResponse):
    data: Optional[VietQrData] = None


class PaymentTransactionItem(BaseModel):
    id: str
    user_id: str
    reference_order_id: str
    gateway_transaction_id: Optional[str] = None
    amount: float
    currency: str = "VND"
    status: str
    created_at: Optional[datetime] = None


class PaymentHistoryResponse(BaseResponse):
    data: List[PaymentTransactionItem] = []
    total_count: int = 0
