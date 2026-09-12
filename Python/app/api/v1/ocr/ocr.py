# 📄 Đường dẫn file: app/api/v1/ocr/ocr.py
from fastapi import APIRouter, Depends, Request, status
from sqlalchemy.orm import Session
from app.dependency import get_db
from app.constants import SystemConstants
from app.core.security.guard.guards import get_current_user
from app.core.translator.translator_engine import i18n_translator
from app.schemas.requests.ocr import OcrScanRequest
from app.schemas.responses.ocr import OcrScanResponse
from app.services.ocr.ocr_service import OcrService
from pydantic import BaseModel, Field
from typing import Dict, Any, Optional

router = APIRouter(prefix="/ocr", tags=["OCR Receipt Scanner & 1-Click Payment"])


class OcrCreateTransactionRequest(BaseModel):
    wallet_id: str = Field(..., description="ID ví thanh toán trừ tiền")
    ocr_data: Dict[str, Any] = Field(..., description="Dữ liệu trích xuất từ hóa đơn OCR")


@router.post("/scan", response_model=OcrScanResponse, status_code=status.HTTP_200_OK)
async def scan_receipt(
    request: Request,
    payload: Optional[OcrScanRequest] = None,
    current_user: dict = Depends(get_current_user)
):
    """
    🎯 Quét và trích xuất hóa đơn / biên lai thanh toán:
       - Tự động nhận diện Tên cửa hàng, Ngày hóa đơn, Tổng tiền và Danh mục chi tiêu gợi ý qua NLP.
    """
    payload_dict = payload.model_dump() if payload else {}
    result = OcrService.process_receipt_image(payload_dict)
    msg = i18n_translator.translate(request, SystemConstants.OCR_SCAN_SUCCESS, msg_type=SystemConstants.MSG_TYPE_MESSAGE)
    return OcrScanResponse(
        success=True,
        error_code=SystemConstants.OCR_SCAN_SUCCESS,
        message=msg,
        data=result,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.post("/create-transaction", status_code=status.HTTP_201_CREATED)
async def create_transaction_from_ocr(
    payload: OcrCreateTransactionRequest,
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    ⚡ TẠO GIAO DỊCH 1-CHẠM TỰ ĐỘNG TỪ KẾT QUẢ QUÉT HÓA ĐƠN OCR:
       - Tự động trừ tiền ví, tạo transaction chi tiêu và đồng bộ sổ cái kép.
    """
    user_id = current_user.get("user_id")
    result = OcrService.create_transaction_from_ocr(db, user_id, payload.wallet_id, payload.ocr_data)
    return {
        "success": True,
        "error_code": SystemConstants.SYSTEM_SUCCESS,
        "message": "Đã tự động tạo giao dịch chi tiêu thành công từ kết quả OCR",
        "data": result,
        "trace_id": getattr(request.state, "trace_id", None)
    }