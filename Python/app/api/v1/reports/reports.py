# 📄 Đường dẫn file: app/api/v1/reports/reports.py
from fastapi import APIRouter, Depends, Request, status, Query, Response
from sqlalchemy.orm import Session
from datetime import datetime

from app.dependency import get_db
from app.core.security.guard.guards import get_current_user
from app.constants import SystemConstants
from app.core.translator.translator_engine import i18n_translator
from app.schemas.responses.report import (
    CashFlowReportResponse,
    CategoryBreakdownResponse,
    FinancialSummaryResponse
)
from app.services.report.financial_report_service import FinancialReportService

router = APIRouter(prefix="/reports", tags=["Financial Reports & Analytics"])


@router.get("/cash-flow", response_model=CashFlowReportResponse, status_code=status.HTTP_200_OK)
async def get_cash_flow_report(
    request: Request,
    days: int = Query(30, ge=7, le=365),
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Báo cáo dòng tiền thu chi theo chuỗi thời gian (Biểu đồ đường / Area chart)"""
    user_id = current_user.get("user_id")
    data = FinancialReportService.get_cash_flow_report(db, user_id, days)
    return CashFlowReportResponse(
        success=True,
        error_code=SystemConstants.TRANSACTION_FETCH_SUCCESS,
        message="Lấy báo cáo dòng tiền thành công!",
        **data,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.get("/category-breakdown", response_model=CategoryBreakdownResponse, status_code=status.HTTP_200_OK)
async def get_category_breakdown(
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Báo cáo tỷ trọng chi tiêu theo danh mục (Biểu đồ tròn Pie / Donut chart)"""
    user_id = current_user.get("user_id")
    data = FinancialReportService.get_category_breakdown(db, user_id)
    return CategoryBreakdownResponse(
        success=True,
        error_code=SystemConstants.TRANSACTION_FETCH_SUCCESS,
        message="Lấy tỷ trọng chi tiêu theo danh mục thành công!",
        **data,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.get("/summary", response_model=FinancialSummaryResponse, status_code=status.HTTP_200_OK)
async def get_financial_summary(
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Báo cáo tổng quan tình hình tài chính, tỷ lệ tiết kiệm và điểm sức khỏe tài chính"""
    user_id = current_user.get("user_id")
    data = FinancialReportService.get_financial_summary(db, user_id)
    return FinancialSummaryResponse(
        success=True,
        error_code=SystemConstants.TRANSACTION_FETCH_SUCCESS,
        message="Lấy tổng quan tài chính thành công!",
        **data,
        trace_id=getattr(request.state, "trace_id", None)
    )


@router.get("/export-csv")
async def export_transactions_csv(
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    📊 XUẤT SAO KÊ GIAO DỊCH RA FILE EXCEL / CSV UTF-8:
    - Định dạng chuẩn UTF-8 BOM hiển thị tiếng Việt hoàn hảo trên Microsoft Excel.
    """
    user_id = current_user.get("user_id")
    csv_content = FinancialReportService.export_transactions_csv(db, user_id)
    filename = f"sao_ke_giao_dich_{datetime.now().strftime('%Y%m%d_%H%M%S')}.csv"
    return Response(
        content=csv_content.encode("utf-8-sig"),
        media_type="text/csv; charset=utf-8",
        headers={"Content-Disposition": f"attachment; filename={filename}"}
    )
