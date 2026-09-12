from fastapi import APIRouter, Depends, Request, status
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException
from app.core.logging.logger import DBLogger
from app.core.security.guard.guards import RoleBasedGuard
from app.core.translator.translator_engine import i18n_translator
from app.repositories.log.log_repository import LogRepository
from app.repositories.log.system_log_repository import SystemLogRepository

router = APIRouter(prefix="/reports", tags=["Financial Reports"])


@router.get(
    "/revenue",
    status_code=status.HTTP_200_OK,
    dependencies=[Depends(RoleBasedGuard(required_module=SystemConstants.MODULE_REPORT_MGMT))],
)
async def get_revenue_report(request: Request):
    """
    👑 API LẤY BÁO CÁO DOANH THU ĐỘNG (PROTECTED BY REPORT_MGMT)
    🎯 Đầu ra tự động đồng bộ mã lỗi i18n và ghi log kiểm toán vật lý.
    """
    try:
        user_id = getattr(request.state, "user_id", SystemConstants.UNKNOWN)
        username = getattr(request.state, "username", SystemConstants.UNKNOWN)

        # Mock dữ liệu báo cáo tài chính thô sạch để trả về cho Client
        report_data = {
            "module_id": "MOD-UUID-REPORT",
            "module_code": SystemConstants.MODULE_REPORT_MGMT,
            "generated_at": "2026-05-21 21:00:00",
            "summary": {
                "total_revenue": 150000000.0,
                "total_transactions": 1420,
                "currency": "VND"
            },
            "details": [
                {"period": "Q1-2026", "amount": 45000000.0, "status": "AUDITED"},
                {"period": "Q2-2026", "amount": 105000000.0, "status": "PENDING"}
            ]
        }

        # GHI LOG KIỂM TOÁN: Ghi nhận vết user truy cập vào phân hệ báo cáo nhạy cảm
        DBLogger.execute_repo_log(
            db_conn=request.state.db_conn,
            repo_class=LogRepository,
            method_name="insert_audit_log",
            console_msg=f"[AUDIT] User {username} fetched revenue report.",
            user_id=str(user_id),
            action="FETCH_REVENUE_REPORT_SUCCESS",
            details=f"Tai khoan {username} truy xuat thanh cong bao cao doanh thu. Trace-ID: {getattr(request.state, 'trace_id', 'N/A')}"
        )

        # Dịch thông điệp thành công động qua i18n_translator
        i18n_message = i18n_translator.translate(
            request,
            error_code=SystemConstants.FETCH_REPORT_SUCCESS,
            msg_type=SystemConstants.MSG_TYPE_MESSAGE
        )

        return {
            "success": True,
            "error_code": SystemConstants.FETCH_REPORT_SUCCESS,
            "message": i18n_message,
            "data": report_data
        }

    except FintechBaseException:
        raise

    except Exception as exc:
        DBLogger.execute_repo_log(
            db_conn=request.state.db_conn,
            repo_class=SystemLogRepository,
            method_name="insert_system_log",
            console_msg="[SYSTEM_ERROR] Revenue report endpoint crashed",
            log_level="ERROR",
            module="API_REVENUE_REPORT",
            message=f"Failed to fetch report: {str(exc)}",
        )
        raise FintechBaseException(
            error_code=SystemConstants.REPORT_INTERNAL_CRASH,
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        )