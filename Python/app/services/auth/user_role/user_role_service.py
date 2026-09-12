from app.repositories.user_role.user_role_repository import UserRoleRepository

class UserRoleService:
    @staticmethod
    def get_user_roles_by_user(db_conn, user_id: str) -> list:
        try:
            return UserRoleRepository.fetch_by_user_id(db_conn, user_id)
        except Exception as e:
            print(f"[SERVICE_ERROR] Lỗi tại UserRoleService: {str(e)}")
            return []