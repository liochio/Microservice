from app.repositories.role.role_repository import RoleRepository

class RoleService:
    @staticmethod
    def get_roles_by_user(db_conn, user_id: str) -> list:
        try:
            return RoleRepository.fetch_roles_by_user_id(db_conn, user_id)
        except Exception as e:
            print(f"[SERVICE_ERROR] Lỗi tại RoleService: {str(e)}")
            return []