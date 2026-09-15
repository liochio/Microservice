from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException

# 👑 IMPORT CỤC INTERCEPTOR TỔNG HỢP GHI LOG DÙNG CHUNG TỪ COMMON PACKAGE
from app.services.common.log_interceptor import AuthLogInterceptor

# 👑 FIX CHUẨN ĐƯỜNG DẪN: Import 2 bộ xử lý nghiệp vụ con theo chuẩn kiến trúc hệ thống
from app.services.auth.auth.processors.register_processor import UserRegisterProcessor
from app.services.auth.auth.processors.login_processor import UserLoginProcessor


class AuthService:
    """
    👑 SERVICE AUTHENTICATION TRUNG TÂM (ORCHESTRATOR LAYER)
    🎯 Điều phối luồng nghiệp vụ sạch và gọi Interceptor trung tâm để ép trút đủ 4 bảng log.
    """

    @staticmethod
    def register_new_user(db_conn, payload) -> dict:
        """👑 ENDPOINT: POST /register | ÉP TRÚT ĐỦ 4 LOG QUA INTERCEPTOR TRUNG TÂM"""
        user_id = SystemConstants.UNKNOWN
        username = getattr(payload, "username", SystemConstants.UNKNOWN)
        email = getattr(payload, "email", SystemConstants.UNKNOWN)

        try:
            # 1. Gọi processor con xử lý nghiệp vụ thô
            result = UserRegisterProcessor.process(db_conn, payload)
            user_id = result.get("user_id", SystemConstants.UNKNOWN)

            # 2. Gọi Interceptor tổng hợp để trút log THÀNH CÔNG
            AuthLogInterceptor.log_register(db_conn, user_id, username, email, exc=None)
            return result

        except Exception as e:
            # 3. Gọi Interceptor tổng hợp để trút log THẤT BẠI / CRASH
            AuthLogInterceptor.log_register(db_conn, user_id, username, email, exc=e)

            # Nếu là lỗi nghiệp vụ chủ động đã định nghĩa -> Ném thẳng ra ngoài cho Handler dịch đa ngôn ngữ
            if isinstance(e, FintechBaseException):
                raise e

            # Hệ thống vỡ trận nổ lỗi thô -> Bọc lại bằng mã lỗi 500 bảo mật cao chuẩn Enterprise
            raise FintechBaseException(error_code=SystemConstants.REGISTRATION_PROCESSOR_CRASH, status_code=500)

    @staticmethod
    def login_user(db_conn, payload) -> dict:
        """👑 ENDPOINT: POST /login | ÉP TRÚT ĐỦ 4 LOG QUA INTERCEPTOR TRUNG TÂM"""
        user_id = SystemConstants.UNKNOWN
        email = getattr(payload, "email", SystemConstants.UNKNOWN)

        try:
            # 1. Gọi processor con xử lý nghiệp vụ thô
            result = UserLoginProcessor.process(db_conn, payload)
            user_id = result.get("user_id", SystemConstants.UNKNOWN)
            username = result.get("username", SystemConstants.UNKNOWN)

            # 2. Gọi Interceptor tổng hợp để trút log THÀNH CÔNG
            AuthLogInterceptor.log_login(db_conn, user_id, username, email, exc=None)
            return result

        except Exception as e:
            # 3. Gọi Interceptor tổng hợp để trút log THẤT BẠI / CRASH
            AuthLogInterceptor.log_login(db_conn, user_id, SystemConstants.UNKNOWN, email, exc=e)

            if isinstance(e, FintechBaseException):
                raise e

            raise FintechBaseException(error_code=SystemConstants.LOGIN_PROCESSOR_CRASH, status_code=500)