from app.repositories.module.module_repository import ModuleRepository

class ModuleService:
    @staticmethod
    def get_modules_by_user(db_conn, user_id: str) -> list:
        try:
            return ModuleRepository.fetch_by_user_id(db_conn, user_id)
        except Exception as e:
            print(f"[SERVICE_ERROR] Lỗi tại ModuleService: {str(e)}")
            return []