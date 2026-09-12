from app.repositories.role_module.role_module_repository import RoleModuleRepository

class RoleModuleService:
    @staticmethod
    def get_role_modules_by_user(db_conn, user_id: str) -> list:
        try:
            return RoleModuleRepository.fetch_by_user_id(db_conn, user_id)
        except Exception as e:
            print(f"[SERVICE_ERROR] Lỗi tại RoleModuleService: {str(e)}")
            return []