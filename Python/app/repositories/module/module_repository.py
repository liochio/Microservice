from sqlalchemy import text

class ModuleRepository:
    @staticmethod
    def fetch_by_user_id(db_conn, user_id: str) -> list:
        try:
            q = text("SELECT DISTINCT m.id, m.code, m.name, m.status, m.created_at FROM modules m JOIN role_modules rm ON m.id = rm.module_id JOIN user_roles ur ON rm.role_id = ur.role_id WHERE ur.user_id = :u_id")
            return [{"id": str(r[0]), "code": str(r[1]), "name": str(r[2]), "status": str(r[3]), "created_at": str(r[4])} for r in db_conn.execute(q, {"u_id": user_id}).fetchall()]
        except Exception:
            q = text("SELECT DISTINCT m.id, m.code FROM modules m JOIN role_modules rm ON m.id = rm.module_id JOIN user_roles ur ON rm.role_id = ur.role_id WHERE ur.user_id = :u_id")
            return [{"id": str(r[0]), "code": str(r[1])} for r in db_conn.execute(q, {"u_id": user_id}).fetchall()]

    @staticmethod
    def fetch_all_modules_recursive(db_conn, user_id: str) -> list:
        try:
            query = text("""
                         WITH RECURSIVE module_tree AS (SELECT m.id, m.code, m.name, m.status, m.parent_id
                                                        FROM modules m
                                                                 INNER JOIN role_modules rm ON m.id = rm.module_id
                                                                 INNER JOIN user_roles ur ON rm.role_id = ur.role_id
                                                        WHERE ur.user_id = :user_id
                                                          AND m.status = 'ACTIVE'

                                                        UNION ALL

                                                        SELECT c.id, c.code, c.name, c.status, c.parent_id
                                                        FROM modules c
                                                                 INNER JOIN module_tree p ON c.parent_id = p.id
                                                        WHERE c.status = 'ACTIVE')
                         SELECT DISTINCT id, code, name, status, parent_id
                         FROM module_tree;
                         """)
            result = db_conn.execute(query, {"user_id": user_id})
            return [
                {
                    "id": str(row[0]),
                    "code": str(row[1]),
                    "name": str(row[2]),
                    "status": str(row[3]),
                    "parent_id": str(row[4]) if row[4] else None
                }
                for row in result.fetchall()
            ]
        except Exception:
            return []