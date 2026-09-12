from sqlalchemy import text

class RoleRepository:
    @staticmethod
    def fetch_roles_by_user_id(db_conn, user_id: str) -> list:
        # Cột trong DB là 'name'
        query = text("""
            SELECT r.id, r.name, r.description, r.status 
            FROM roles r 
            JOIN user_roles ur ON r.id = ur.role_id 
            WHERE ur.user_id = :u_id
        """)
        rows = db_conn.execute(query, {"u_id": user_id}).fetchall()
        # Map 'name' -> 'code' để Service cũ không bị lỗi logic
        return [{"id": str(r[0]), "code": str(r[1]), "description": str(r[2]), "status": str(r[3])} for r in rows]