from app.repositories.log.mapping.security_log_mapping import SecurityLogMappingFactory


class SecurityLogRepository:
    """
    👑 REPOSITORY SECURITY LOGS
    🎯 Ghi vết cảnh báo lỗ hổng an ninh bảo mật tài khoản.
    """

    @staticmethod
    def insert_security_log(db_conn, user_id: str, event_type: str, severity: str, ip_address: str,
                            details: str) -> None:
        try:
            query, params = SecurityLogMappingFactory.get_insert_security_log_stmt_and_params(
                db_conn=db_conn,
                user_id=user_id,
                event_type=event_type,
                severity=severity,
                ip_address=ip_address,
                details=details
            )

            db_conn.execute(query, params)

            if hasattr(db_conn, "commit"):
                db_conn.commit()

        except Exception as e:
            print(f"[DYNAMIC_LOG_ERROR] Thất bại insert bảng security_logs: {str(e)}")