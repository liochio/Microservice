import traceback
from app.core.logging.logger import DBLogger
from app.core.exceptions.base_exception import FintechBaseException


class AuthLogInterceptor:
    """
    👑 CENTRALIZED LOG INTERCEPTOR (COMMON LAYER)
    🎯 Cục tổng hợp xử lý ghi trọn vẹn 4 bảng log vật lý vật lý cho toàn bộ phân hệ Auth/Verify.
    """

    @staticmethod
    def log_register(db_conn, user_id: str, username: str, email: str, exc: Exception = None):
        """🛡️ Trút 4 bảng log cho luồng Đăng ký (Thành công / Thất bại nghiệp vụ / Crash)"""
        if not exc:
            # 🟢 TRƯỜNG HỢP THÀNH CÔNG
            DBLogger.audit(db_conn, user_id, "USER_REGISTRATION_SUCCESS",
                           f"Khách hàng {username} khởi tạo tài khoản PENDING thành công.")
            DBLogger.security(db_conn, user_id, "NEW_ACCOUNT_CREATED", "LOW", "127.0.0.1",
                              "Tài khoản tạm găm trạng thái PENDING chờ kích hoạt liên hoàn link.")
            DBLogger.system(db_conn, "INFO", f"Khởi tạo user_id: {user_id} vào luồng xác thực.", "AUTH_MODULE")
        else:
            # 🟡 TRƯỜNG HỢP THẤT BẠI DO LỖI NGHIỆP VỤ (Trùng email/phone/username)
            if isinstance(exc, FintechBaseException):
                DBLogger.audit(db_conn, "UNKNOWN", "REGISTRATION_FAILED",
                               f"Đăng ký thất bại do trùng dữ liệu hệ thống. Mã lỗi: {exc.error_code}")
                DBLogger.security(db_conn, "UNKNOWN", "REGISTRATION_REJECTED", "MEDIUM", "127.0.0.1",
                                  f"Yêu cầu tạo tài khoản {username} bị từ chối do xung đột trường thông tin: {email}")
                DBLogger.system(db_conn, "WARNING", f"Từ chối khởi tạo user trùng lặp dữ liệu: {exc.error_code}",
                                "AUTH_MODULE")
                return

            # 🔴 TRƯỜNG HỢP CRASH HỆ THỐNG / DB LỖI VẬT LÝ
            DBLogger.audit(db_conn, "UNKNOWN", "REGISTRATION_CRASH", f"Sập luồng tạo User: {str(exc)}")
            DBLogger.security(db_conn, "UNKNOWN", "REGISTRATION_CRASH", "HIGH", "127.0.0.1",
                              f"Lỗi DB luồng đăng ký: {str(exc)}")
            DBLogger.system(db_conn, "ERROR", traceback.format_exc(), "AUTH_MODULE")

    @staticmethod
    def log_verify_link(db_conn, user_id: str, token: str, exc: Exception = None):
        """🛡️ Trút 4 bảng log cho luồng Click Link Mail Kích Hoạt"""
        if not exc:
            # 🟢 TRƯỜNG HỢP THÀNH CÔNG
            DBLogger.audit(db_conn, user_id, "LINK_CLICK_SUCCESS",
                           "User xác thực link email thành công, hệ thống sinh mã OTP số.")
            DBLogger.security(db_conn, user_id, "OTP_ISSUED", "LOW", "127.0.0.1",
                              "Hệ thống cấp OTP xác thực 2 lớp thành công.")
            DBLogger.system(db_conn, "INFO", f"User {user_id} click link xác thực sinh mã OTP hoàn tất.", "AUTH_MODULE")
        else:
            # 🟡 TRƯỜNG HỢP THẤT BẠI DO LỖI NGHIỆP VỤ LINK (Hết hạn link / Token fake)
            if isinstance(exc, FintechBaseException):
                DBLogger.audit(db_conn, "UNKNOWN", "LINK_EXPIRED_VERIFICATION_FAILED",
                               f"Xác thực link email vãng lai thất bại lỗi nghiệp vụ: {exc.error_code}")
                if exc.error_code == "LINK_TOKEN_EXPIRED":
                    DBLogger.security(db_conn, "UNKNOWN", "LINK_TOKEN_TIMEOUT", "MEDIUM", "127.0.0.1",
                                      "Link Token hết hiệu lực bốc từ system_settings.")
                else:
                    DBLogger.security(db_conn, "UNKNOWN", "INVALID_LINK_CLICK", "HIGH", "127.0.0.1",
                                      f"Cố ý click vào link chứa token giả mạo: {token}")
                DBLogger.system(db_conn, "WARNING", f"Xử lý link xác thực thất bại: {exc.error_code}", "AUTH_MODULE")
                return

            # 🔴 TRƯỜNG HỢP CRASH LUỒNG LINK
            DBLogger.audit(db_conn, "UNKNOWN", "LINK_VERIFY_CRASH", f"Sập luồng xử lý link email: {str(exc)}")
            DBLogger.security(db_conn, "UNKNOWN", "LINK_VERIFY_CRASH", "HIGH", "127.0.0.1",
                              f"Lỗi hệ thống crash luồng link: {str(exc)}")
            DBLogger.system(db_conn, "ERROR", traceback.format_exc(), "AUTH_MODULE")

    @staticmethod
    def log_confirm_otp(db_conn, user_id: str, email: str, otp_sent: str, exc: Exception = None):
        """🛡️ Trút 4 bảng log cho luồng Submit OTP Chốt Hạ"""
        if not exc:
            # 🟢 TRƯỜNG HỢP THÀNH CÔNG -> ACTIVE USER
            DBLogger.audit(db_conn, user_id, "OTP_ACTIVATION_SUCCESS",
                           "Kích hoạt tài khoản Fintech thành công. Thực thể chuyển dịch mượt mà sang trạng thái ACTIVE.")
            DBLogger.security(db_conn, user_id, "USER_VERIFIED_SUCCESSFULLY", "LOW", "127.0.0.1",
                              "Xác thực hai lớp thông suốt, tài khoản gỡ gác chặn PENDING.")
            DBLogger.system(db_conn, "INFO", f"User {user_id} chính thức kích nổ trạng thái ACTIVE.", "AUTH_MODULE")
        else:
            # 🟡 TRƯỜNG HỢP THẤT BẠI DO LỖI NGHIỆP VỤ OTP (Sai OTP / OTP hết hạn)
            if isinstance(exc, FintechBaseException):
                DBLogger.audit(db_conn, "UNKNOWN", "OTP_ACTIVATION_FAILED",
                               f"Kích hoạt tài khoản thất bại do mã OTP lỗi nghiệp vụ: {exc.error_code}")
                DBLogger.security(db_conn, "UNKNOWN", "OTP_ATTACK_WARNING", "HIGH", "127.0.0.1",
                                  f"Nhập sai hoặc hết hạn OTP cho tài khoản: {email}. Mã gửi lên: {otp_sent}")
                DBLogger.system(db_conn, "WARNING", f"Xác thực mã OTP thất bại: {exc.error_code}", "AUTH_MODULE")
                return

            # 🔴 TRƯỜNG HỢP CRASH LUỒNG OTP
            DBLogger.audit(db_conn, "UNKNOWN", "OTP_CONFIRM_CRASH", f"Sập luồng xác thực mã OTP: {str(exc)}")
            DBLogger.security(db_conn, "UNKNOWN", "OTP_CONFIRM_CRASH", "HIGH", "127.0.0.1",
                              f"Lỗi nghiêm trọng sập hệ thống luồng OTP: {str(exc)}")
            DBLogger.system(db_conn, "ERROR", traceback.format_exc(), "AUTH_MODULE")

    @staticmethod
    def log_login(db_conn, user_id: str, username: str, email: str, exc: Exception = None):
        """🛡️ Trút 4 bảng log cho luồng Đăng nhập (Thành công / Sai mật khẩu / Blocked)"""
        if not exc:
            # 🟢 TRƯỜNG HỢP THÀNH CÔNG ĐỔ MA TRẬN QUYỀN
            DBLogger.audit(db_conn, user_id, "LOGIN_SUCCESS",
                           f"Người dùng {username} đăng nhập thành công. Phân tách Service độc lập hoàn mỹ.")
            DBLogger.security(db_conn, user_id, "TOKEN_ISSUED", "LOW", "127.0.0.1",
                              "Cấp thành công cặp bài trùng Token chứa ma trận quyền.")
            DBLogger.system(db_conn, "INFO", f"Phiên làm việc của user_id: {user_id} đã được khởi tạo mượt mà.",
                            "AUTH_MODULE")
        else:
            # 🟡 TRƯỜNG HỢP THẤT BẠI NGHIỆP VỤ LOGIN
            if isinstance(exc, FintechBaseException):
                if exc.error_code == "104":
                    DBLogger.audit(db_conn, "UNKNOWN", "LOGIN_FAILED",
                                   f"Đăng nhập thất bại bằng email vô danh: {email}")
                    DBLogger.security(db_conn, "UNKNOWN", "LOGIN_FAILED", "HIGH", "127.0.0.1",
                                      f"Tài khoản không tồn tại: {email}")
                    DBLogger.system(db_conn, "WARNING", f"Cố gắng đăng nhập bất thành bằng email vô danh: {email}",
                                    "AUTH_MODULE")
                elif exc.error_code == "ACCOUNT_PENDING_ACTIVATION":
                    DBLogger.audit(db_conn, user_id, "LOGIN_FAILED",
                                   "Đăng nhập thất bại do tài khoản chưa xác thực kích hoạt mã OTP.")
                    DBLogger.security(db_conn, user_id, "PENDING_ACCOUNT_ACCESS_DENIED", "MEDIUM", "127.0.0.1",
                                      "Tài khoản đang găm PENDING cố ý truy cập luồng Login.")
                elif exc.error_code == "USER_ACCOUNT_BLOCKED":
                    DBLogger.audit(db_conn, user_id, "LOGIN_FAILED",
                                   "Đăng nhập bị từ chối do trạng thái tài khoản bị khóa.")
                    DBLogger.security(db_conn, user_id, "ACCOUNT_SUSPENDED_ACCESS", "CRITICAL", "127.0.0.1",
                                      "Tài khoản mang trạng thái BLOCKED cố ý truy cập.")
                elif exc.error_code == "105":
                    DBLogger.audit(db_conn, "UNKNOWN", "LOGIN_ATTEMPT", "Nhập sai mật khẩu hệ thống.")
                    wrong_username = email.split("@")[0] if email else "UNKNOWN"
                    DBLogger.security(db_conn, "UNKNOWN", "PASSWORD_INVALID", "MEDIUM", "127.0.0.1",
                                      f"Cảnh báo nhập sai mật khẩu tài khoản: {email}")
                    DBLogger.system(db_conn, "WARNING", f"Người dùng {wrong_username} nhập sai mật mã đăng nhập.",
                                    "AUTH_MODULE")
                return

            # 🔴 TRƯỜNG HỢP CRASH LUỒNG LOGIN
            DBLogger.audit(db_conn, "UNKNOWN", "LOGIN_CRASH", f"Sập luồng đăng nhập hệ thống: {str(exc)}")
            DBLogger.security(db_conn, "UNKNOWN", "LOGIN_CRASH", "HIGH", "127.0.0.1",
                              f"Lỗi crash hệ thống luồng login: {str(exc)}")
            DBLogger.system(db_conn, "ERROR", traceback.format_exc(), "AUTH_MODULE")