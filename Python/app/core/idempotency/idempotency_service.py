from sqlalchemy import text
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException
from app.core.logging.logger import DBLogger


class IdempotencyService:
    """
    👑 CENTRALIZED IDEMPOTENCY SERVICE (APPLICATION LAYER)
    🎯 Chốt chặn bọc thép, khóa cứng tiến trình và trút log an ninh nếu phát hiện lặp lệnh.
    """

    @staticmethod
    def check_and_lock_key(db_conn, idempotency_key: str, user_id: str) -> None:
        """
        🛡️ KIỂM TRA TRÙNG LẶP VÀ KHÓA CỨNG GIAO DỊCH VẬT LÝ TRONG PHIÊN
        """
        if not idempotency_key or str(idempotency_key).strip() == "":
            raise FintechBaseException(error_code=SystemConstants.MISSING_REQUIRED_IDEMPOTENCY_KEY, status_code=400)

        # 1. QUÉT ĐỐI CHIẾU XUYÊN SUỐT: Soi trong bảng api_request_logs xem key này từng kết thúc thành công chưa
        query_check_logs = text("""
                                SELECT id
                                FROM api_request_logs
                                WHERE request_payload LIKE :key_pattern
                                  AND status_code BETWEEN 200 AND 299
                                LIMIT 1
                                """)
        existing_log = db_conn.execute(query_check_logs, {"key_pattern": f'%"{idempotency_key}"%'}).fetchone()

        if existing_log:
            # Phát hiện lặp lệnh đã thành công quá khứ ➡️ Bẻ gãy luồng ngay lập tức, bảo vệ số dư tài sản
            DBLogger.security(db_conn, user_id, "IDEMPOTENCY_REPLAY_REJECTED", "HIGH", "127.0.0.1",
                              f"Chặn đứng yêu cầu trùng lặp Idempotency Key đã hoàn tất trước đó: {idempotency_key}")
            raise FintechBaseException(error_code=SystemConstants.DUPLICATE_IDEMPOTENCY_TRANSACTION_REJECTED, status_code=409)

        # 2. TIẾN HÀNH ĐÓNG ĐINH KHÓA (Row Locking độc quyền tạm thời)
        try:
            # Insert trực tiếp vào bảng idempotency_keys ở trạng thái 'PROCESSING'
            query_insert_lock = text("""
                                     INSERT INTO idempotency_keys (id, user_id, status, created_at, updated_at)
                                     VALUES (:id_key, :u_id, 'PROCESSING', NOW(), NOW())
                                     """)
            db_conn.execute(query_insert_lock, {"id_key": idempotency_key, "u_id": user_id})

        except Exception:
            # Nếu 2 request trùng key bắn lên đồng thời cùng micro-giây, Unique Constraint của DB sẽ kích nổ văng lỗi lập tức
            DBLogger.security(db_conn, user_id, "IDEMPOTENCY_CONCURRENT_LOCK", "MEDIUM", "127.0.0.1",
                              f"Request song song cố tình ghi đè khóa đang xử lý: {idempotency_key}")
            raise FintechBaseException(error_code=SystemConstants.TRANSACTION_IN_PROGRESS_LOCK, status_code=409)

    @staticmethod
    def resolve_key_status(db_conn, idempotency_key: str, is_success: bool) -> None:
        """
        🔄 CẬP NHẬT TRẠNG THÁI CUỐI CÙNG CHO KHÓA SAU KHI LUỒNG CHÍNH KẾT THÚC
        """
        final_status = "SUCCESS" if is_success else "FAILED"
        query_update = text("""
                            UPDATE idempotency_keys
                            SET status     = :status,
                                updated_at = NOW()
                            WHERE id = :id_key
                            """)
        db_conn.execute(query_update, {"status": final_status, "id_key": idempotency_key})