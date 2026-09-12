# 📄 Đường dẫn file: app/schemas/responses/transfer.py
from pydantic import BaseModel
from typing import List, Optional, Any


class TransferItem(BaseModel):
    id: str
    user_id: str
    source_wallet_id: str
    destination_wallet_id: str
    amount: float
    fee: float = 0.0
    transfer_date: str
    description: Optional[str] = None
    status: str = "COMPLETED"


class TransferListResponse(BaseModel):
    success: bool = True
    error_code: str = "TRANSFER_FETCH_SUCCESS"
    message: str
    data: List[TransferItem]
    trace_id: Optional[str] = None


class TransferDetailResponse(BaseModel):
    success: bool = True
    error_code: str = "TRANSFER_EXECUTE_SUCCESS"
    message: str
    data: TransferItem
    trace_id: Optional[str] = None
