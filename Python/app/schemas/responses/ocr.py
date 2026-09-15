
from pydantic import BaseModel
from typing import List, Optional, Any


class OcrItemExtracted(BaseModel):
    item_name: str
    quantity: int = 1
    unit_price: float
    total_price: float


class OcrResultData(BaseModel):
    merchant_name: str
    invoice_date: str
    total_amount: float
    suggested_category: str
    confidence_score: float
    items: List[OcrItemExtracted] = []


class OcrScanResponse(BaseModel):
    success: bool = True
    error_code: str = "OCR_SCAN_SUCCESS"
    message: str
    data: OcrResultData
    trace_id: Optional[str] = None
