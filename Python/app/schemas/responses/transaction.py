
from pydantic import BaseModel
from typing import List, Optional, Any


class TransactionItem(BaseModel):
    id: str
    user_id: str
    wallet_id: str
    category_id: str
    transaction_code: Optional[str] = None
    amount: float
    transaction_type: str
    transaction_date: str
    description: Optional[str] = None
    balance_before: Optional[float] = None
    balance_after: Optional[float] = None
    status: str = "COMPLETED"


class TransactionListResponse(BaseModel):
    success: bool = True
    error_code: str = "TRANSACTION_FETCH_SUCCESS"
    message: str
    data: List[TransactionItem]
    total_count: int = 0
    trace_id: Optional[str] = None


class TransactionDetailResponse(BaseModel):
    success: bool = True
    error_code: str = "TRANSACTION_CREATE_SUCCESS"
    message: str
    data: TransactionItem
    trace_id: Optional[str] = None
