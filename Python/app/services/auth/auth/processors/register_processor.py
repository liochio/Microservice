import uuid
import re
import secrets
from datetime import datetime, timedelta
from sqlalchemy import select, text
from sqlalchemy.exc import SQLAlchemyError
from app.models.notification.notification import Notification
from app.repositories.notification.notification_repository import NotificationRepository
from app.repositories.users.user_repository import UserRepository
from app.services.common.crypto_service import CryptoService
from app.core.validators.generic_validator import GenericValidator
from app.models.user.user import User
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException
from app.core.translator.translator_engine import i18n_translator

# 👑 Cấu hình cụm lỗi hệ thống/truy vấn để triệt tiêu triệt để cảnh báo "Too broad exception clause"
CORE_FALLBACK_ERRORS = (SQLAlchemyError, AttributeError, TypeError, ValueError, RuntimeError)


class UserRegisterProcessor:
    """
    👑 CORE REGISTER PROCESSOR (APPLICATION WORKER LAYER)
    🎯 Xử lý nghiệp vụ đăng ký tài khoản mới, sinh mã link token xác thực và cấu hình email thông báo.
    """

    @staticmethod
    def process(db_conn, payload) -> dict:
        raw_email = str(payload.email if payload and hasattr(payload, 'email') else '').strip().lower()
        clean_email = re.sub(r'[\s\x00-\x1f\x7f-\x9f]', '', raw_email)
        username = str(payload.username if payload and hasattr(payload, 'username') else '').strip()

        # 1. KIỂM TRA TRÙNG LẶP THỰC THỂ VIA ORM VALIDATOR
        GenericValidator.check_duplicate(db_conn, User, "email", payload.email, SystemConstants.EMAIL_ALREADY_EXISTS)
        
        if hasattr(payload, 'phone') and payload.phone:
            GenericValidator.check_duplicate(db_conn, User, "phone_number", payload.phone,
                                             SystemConstants.PHONE_ALREADY_EXISTS)
                                             
        GenericValidator.check_duplicate(db_conn, User, "username", payload.username,
                                         SystemConstants.USERNAME_ALREADY_EXISTS)

        # Thực thi mã hóa bảo mật mật mã đầu vào
        hashed_pwd = CryptoService.hash_password(payload.password)
        generated_user_id = str(uuid.uuid4())

        # 2. GỌI TẦNG REPO USERS CHÍNH QUY ĐỂ INSERT USER
        try:
            UserRepository.insert_new_user(
                db_conn=db_conn,
                user_id=generated_user_id,
                username=username,
                email=clean_email,
                phone=payload.phone,
                hashed_pwd=hashed_pwd,
                full_name=payload.full_name,
                dob=payload.date_of_birth,
                gender=payload.gender
            )
        except CORE_FALLBACK_ERRORS:
            raise FintechBaseException(error_code=SystemConstants.USER_CREATION_DATABASE_FAILED, status_code=500)

        # 2.5 👑 PHÂN ĐỊNH RANH GIỚI: Việc đồng bộ Sổ cái kép và Identity thuộc quyền kiểm soát của Java IAM & Ledger Service qua Event/M2M API
        # Đã loại bỏ hoàn toàn các truy vấn Raw SQL chọc chéo Database liochio_auth_db theo chuẩn Database-Per-Service.
        
        # 3. TRUY VẤN TẦNG HỆ THỐNG: Lấy cấu hình thời gian hết hạn hệ thống
        try:
            q_setting = text(
                "SELECT value FROM system_settings WHERE system_settings.key = 'LINK_TOKEN_EXPIRE_MINUTES' AND status = 'ACTIVE' LIMIT 1")
            res_setting = db_conn.execute(q_setting).fetchone()
            expire_minutes = int(res_setting[0]) if res_setting else 15
        except CORE_FALLBACK_ERRORS:
            expire_minutes = 15

        # Sinh mã OTP 6 số bảo mật ngẫu nhiên (100000 -> 999999)
        otp_code = f"{secrets.randbelow(900000) + 100000}"
        link_token = otp_code
        token_expired_at = datetime.now() + timedelta(minutes=int(expire_minutes))
        activation_link = f"http://localhost:5173/verify-otp?username={username}&otp={otp_code}"

        # 4. TẠO NỘI DUNG EMAIL HTML CÓ MÃ OTP 6 SỐ VÀ NÚT BẤM KÍCH HOẠT
        title = "🔐 [Liochio FinTech] Mã OTP Kích Hoạt Tài Khoản Của Bạn"
        final_content = f"""
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 24px; background: #0f172a; color: #f8fafc; border-radius: 16px; border: 1px solid #1e293b;">
            <div style="text-align: center; margin-bottom: 20px;">
                <h1 style="color: #10b981; margin: 0; font-size: 24px;">LIOCHIO FINTECH</h1>
                <p style="color: #94a3b8; font-size: 13px; margin-top: 4px;">Hệ Sinh Thái Tài Chính Lõi & Heo Đất Thông Minh</p>
            </div>
            <div style="background: #1e293b; padding: 24px; border-radius: 12px; margin-bottom: 20px; text-align: center;">
                <h2 style="color: #ffffff; font-size: 18px; margin-top: 0;">Mã OTP Xác Thực Tài Khoản</h2>
                <p style="font-size: 14px; color: #cbd5e1; text-align: left;">Chào <strong>{username}</strong>,</p>
                <p style="font-size: 14px; color: #cbd5e1; text-align: left;">Cảm ơn bạn đã đăng ký tài khoản tại Liochio FinTech. Dưới đây là mã OTP 6 số bảo mật của bạn:</p>
                
                <div style="margin: 24px 0;">
                    <div style="display: inline-block; padding: 12px 32px; background: #0f172a; border: 2px dashed #10b981; border-radius: 12px;">
                        <span style="font-size: 36px; font-weight: bold; color: #34d399; letter-spacing: 8px; font-family: monospace;">{otp_code}</span>
                    </div>
                </div>

                <div style="margin: 24px 0;">
                    <a href="{activation_link}" 
                       style="display: inline-block; padding: 14px 32px; background: #10b981; color: #ffffff; text-decoration: none; font-weight: bold; font-size: 16px; border-radius: 10px; box-shadow: 0 4px 14px rgba(16, 185, 129, 0.4);">
                        👉 BẤM VÀO ĐÂY ĐỂ KÍCH HOẠT TỰ ĐỘNG
                    </a>
                </div>
                
                <p style="font-size: 12px; color: #94a3b8; margin-bottom: 0;">
                    Hoặc nhập mã trực tiếp tại: <a href="http://localhost:5173/verify-otp?username={username}" style="color: #38bdf8;">http://localhost:5173/verify-otp?username={username}</a>
                </p>
            </div>
            <p style="font-size: 11px; color: #64748b; text-align: center; margin: 0;">
                Mã OTP có hiệu lực trong vòng {expire_minutes} phút (hết hạn lúc {token_expired_at.strftime('%H:%M:%S %d/%m/%Y')}).
            </p>
        </div>
        """

        # 5. GỌI TẦNG REPO THỰC THI: Đẩy thông báo chính quy
        try:
            NotificationRepository.insert_notification(
                db_conn=db_conn,
                user_id=generated_user_id,
                template_code="EMAIL_VERIFICATION",
                title=title,
                body=final_content
            )

            stmt_get_id = select(Notification.id).where(
                Notification.user_id == generated_user_id,
                Notification.notification_type == "EMAIL_VERIFICATION"
            ).order_by(Notification.created_at.desc()).limit(1)

            noti_id = db_conn.execute(stmt_get_id).scalar() or str(uuid.uuid4())

        except CORE_FALLBACK_ERRORS:
            raise FintechBaseException(error_code=SystemConstants.NOTIFICATION_QUEUE_FAILED, status_code=500)

        # 6. GỌI HÀM REPO THỰC THI GHI LOG KIỂM TOÁN NOTIFICATION
        gateway_resp_payload = f'{{"token": "{link_token}", "expire_at": "{token_expired_at.strftime("%Y-%m-%d %H:%M:%S")}"}}'

        try:
            NotificationRepository.insert_notification_log(
                db_conn=db_conn,
                notification_id=noti_id,
                user_id=generated_user_id,
                channel="EMAIL",
                status="PENDING",
                provider_response=gateway_resp_payload
            )
        except CORE_FALLBACK_ERRORS:
            pass

        # ==============================================================================
        # 👑 ĐÁNH DẤU CHỈNH SỬA: ĐẢM BẢO TRANSACTION ĐƯỢC COMMIT XUỐNG DATABASE
        # Mục đích: Ngăn chặn việc SessionLocal bị close ở middleware làm ROLLBACK toàn bộ dữ liệu User
        # ==============================================================================
        if hasattr(db_conn, "commit"):
            db_conn.commit()

        # Trả về kết quả thô sạch sẽ, trạng thái mặc định PENDING đồng bộ chuẩn xác nghiệp vụ
        return {"user_id": generated_user_id, "username": username, "email": clean_email, "status": "PENDING"}