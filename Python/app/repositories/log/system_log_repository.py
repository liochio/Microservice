from app.repositories.log.mapping.system_log_mapping import SystemLogMappingFactory


class SystemLogRepository:
    """
    👑 REPOSITORY SYSTEM LOGS
    🎯 Lưu vết log lỗi Exception crash thô sâu của lõi hệ thống chuẩn Model hệ thống.
    """

    @staticmethod
    def insert_system_log(db_conn, error_type: str, stack_trace: str, component: str) -> None:
        try:
            query, params = SystemLogMappingFactory.get_insert_system_log_stmt_and_params(
                error_type=error_type,
                stack_trace=stack_trace,
                component=component
            )

            db_conn.execute(query, params)

            if hasattr(db_conn, "commit"):
                db_conn.commit()

        except Exception as e:
            print(f"[REPO_LOG_ERROR] Thất bại insert bảng system_logs chuẩn: {str(e)}")