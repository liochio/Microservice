from sqlalchemy import text

class RoleModuleRepository:
    @staticmethod
    def fetch_by_user_id(db_conn, user_id: str) -> list:
        query = text("SELECT rm.id, rm.role_id, rm.module_id, rm.created_at, rm.updated_at FROM role_modules rm JOIN user_roles ur ON rm.role_id = ur.role_id WHERE ur.user_id = :u_id")
        rows = db_conn.execute(query, {"u_id": user_id}).fetchall()
        return [{"id": str(r[0]), "role_id": str(r[1]), "module_id": str(r[2]), "created_at": str(r[3]), "updated_at": str(r[4])} for r in rows]