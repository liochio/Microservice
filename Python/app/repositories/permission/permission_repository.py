from sqlalchemy import text

class PermissionRepository:
    @staticmethod
    def fetch_by_user_id(db_conn, user_id: str) -> list:
        # Cột trong DB là 'code' và 'name'
        query = text("""
            SELECT DISTINCT p.id, p.module_id, p.code, p.name, p.status 
            FROM permissions p 
            JOIN role_modules rm ON p.module_id = rm.module_id 
            JOIN user_roles ur ON rm.role_id = ur.role_id 
            WHERE ur.user_id = :u_id
        """)
        result = db_conn.execute(query, {"u_id": user_id})
        return [dict(row._mapping) for row in result]