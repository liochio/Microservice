from sqlalchemy import text


class SecurityLogMappingFactory:
    """
    👑 FACTORY ÁNH XẠ ORM MAPPING CHÍNH QUY
    """

    @staticmethod
    def get_insert_security_log_stmt_and_params(db_conn, user_id: str, event_type: str, severity: str, ip_address: str,
                                                details: str):
        # 👑 GIỮ NGUYÊN HOÀN TOÀN LOGIC CŨ
        safe_user_id = None if (not user_id or str(user_id).strip() in ["UNKNOWN", "None"]) else user_id

        # Quét động cấu trúc cột của bảng bảo mật hệ thống dưới DB
        check_col = db_conn.execute(text("SHOW COLUMNS FROM security_logs LIKE 'details'")).fetchone()
        col_name = "details" if check_col else "description"

        query = text(f"""
            INSERT INTO security_logs (user_id, event_type, severity, ip_address, {col_name}, created_at) 
            VALUES (:user_id, :event_type, :severity, :ip_address, :details, NOW())
        """)

        params = {
            "user_id": safe_user_id,
            "event_type": str(event_type),
            "severity": str(severity).upper(),
            "ip_address": str(ip_address),
            "details": str(details)
        }

        return query, params