# 📄 Đường dẫn file: app/schemas/requests/payment.py
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any


class VietQrCreateRequest(BaseModel):
    wallet_id: Optional[str] = Field(None, description="Ví đích nhận tiền (nếu bỏ trống nhận vào ví chính)")
    amount: float = Field(..., gt=1000, description="Số tiền nạp (tối thiểu 1,000 VND)")
    bank_code: str = Field("MB", description="Mã ngân hàng (MB, VCB, TCB, ACB, VPB, ICB...)")
    account_number: str = Field("0912345678", description="Số tài khoản thụ hưởng ngân hàng")
    account_name: str = Field("NGUYEN VAN ADMIN", description="Tên chủ tài khoản thụ hưởng")
    description: Optional[str] = Field(None, description="Nội dung chuyển khoản")


class PaymentWebhookPayload(BaseModel):
    gateway: str = Field("VIETQR_CASSO", description="Cổng thanh toán (CASSO, SEPAY, VNPAY, MOMO)")
    transaction_id: str = Field(..., description="Mã giao dịch từ phía cổng thanh toán")
    amount: float = Field(..., gt=0, description="Số tiền thực nhận")
    content: str = Field(..., description="Nội dung chuyển khoản chứa mã Order ID")
    bank_account: Optional[str] = None
    signature: Optional[str] = None
    raw_payload: Optional[Dict[str, Any]] = None
