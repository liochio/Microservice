from sqlalchemy import text


class SystemLogMappingFactory:
    """
    👑 FACTORY ÁNH XẠ ORM MAPPING CHÍNH QUY
    """

    @staticmethod
    def get_insert_system_log_stmt_and_params(error_type: str, stack_trace: str, component: str):
        query = text("""
                     INSERT INTO system_logs (error_type, stack_trace, component, status, created_at, updated_at)
                     VALUES (:error_type, :stack_trace, :component, 'UNRESOLVED', NOW(), NOW())
                     """)

        params = {
            "error_type": str(error_type),
            "stack_trace": str(stack_trace),
            "component": str(component)
        }

        return query, params