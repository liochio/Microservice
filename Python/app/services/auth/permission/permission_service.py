from app.repositories.permission.permission_repository import PermissionRepository


class PermissionService:
    @staticmethod
    def get_permissions_and_token_list(db_conn, user_id: str) -> tuple:
        permissions_data = []
        permissions_list_for_token = []
        try:
            # 👑 FIX DỨT ĐIỂM: Gọi đúng tên hàm fetch_by_user_id định nghĩa trong Repository tương ứng
            rows = PermissionRepository.fetch_by_user_id(db_conn, user_id)
            for row in rows:
                if not row:
                    continue
                # row.get("code") là mã quyền, row.get("name") là tên quyền
                p_item = {
                    "id": str(row.get("id") if row.get("id") is not None else ""),
                    "module_id": str(row.get("module_id") if row.get("module_id") is not None else ""),
                    "code": str(row.get("code") if row.get("code") is not None else ""),
                    "name": str(row.get("name") if row.get("name") is not None else ""),
                    "status": str(row.get("status") if row.get("status") is not None else "")
                }
                permissions_data.append(p_item)

                # Token cần cái mã (code) để gác cổng
                if str(p_item.get("status", "")).upper() == "ACTIVE":
                    permissions_list_for_token.append(p_item["code"])

        except Exception as e:
            print(f"[SERVICE_ERROR] Lỗi tại PermissionService: {str(e)}")
        return permissions_data, permissions_list_for_token