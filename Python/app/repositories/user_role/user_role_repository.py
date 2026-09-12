from sqlalchemy import text

class UserRoleRepository:
    @staticmethod
    def fetch_by_user_id(db_conn, user_id: str) -> list:
        query = text("SELECT id, user_id, role_id, created_at, updated_at FROM user_roles WHERE user_id = :u_id")
        rows = db_conn.execute(query, {"u_id": user_id}).fetchall()
        return [{"id": str(r[0]), "user_id": str(r[1]), "role_id": str(r[2]), "created_at": str(r[3]), "updated_at": str(r[4])} for r in rows]