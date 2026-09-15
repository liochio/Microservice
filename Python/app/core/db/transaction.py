import traceback
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException
from app.core.logging.logger import DBLogger


class FintechTransactionManager:
    """
    👑 CORE DB TRANSACTION MANAGER (MỤC THỨ 5)
    🎯 Kiểm soát tính toàn vẹn giao dịch bảo mật cao ACID, tự động quản lý phiên và xử lý Rollback lợi hại.
    """

    @staticmethod
    def execute_atomic_scope(db_session, business_func, *args, **kwargs):
        """
        🛡️ THỰC THI KHỐI NGHIỆP VỤ AN TOÀN TUÂN THỦ NGUYÊN TẮC TOÀN VẸN ACID
        """
        try:
            # Khởi động mạch bảo mật cao mở phiên session giao dịch vật lý dưới DB
            # Thực thi logic nghiệp vụ thô thông qua con trỏ hàm đóng gói truyền vào từ Service
            result = business_func(db_session, *args, **kwargs)

            # Nếu toàn bộ tiến trình chạy mượt, nện lệnh commit chốt hạ dữ liệu xuống đĩa cứng
            if hasattr(db_session, "commit"):
                db_session.commit()
            return result

        except FintechBaseException as exc:
            # Phát hiện lỗi nghiệp vụ chủ động ➡️ Rollback ngay, trả trạng thái cũ để bảo toàn ví tiền
            if hasattr(db_session, "rollback"):
                db_session.rollback()
            raise exc

        except Exception:
            # Hệ thống sụp nguồn hoặc nổ DB đột ngột ➡️ Kích hoạt Rollback toàn phần khẩn cấp
            if hasattr(db_session, "rollback"):
                db_session.rollback()
            DBLogger.system(db_session, "DATABASE_TRANSACTION_CRASH", traceback.format_exc(), "TRANSACTION_MANAGER")
            raise FintechBaseException(error_code=SystemConstants.CORE_TRANSACTION_EXECUTION_FAILED, status_code=500)