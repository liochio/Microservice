import uuid
import secrets
from datetime import datetime
from decimal import Decimal
from typing import Optional, Dict, Any
from sqlalchemy import text


class WalletFactory:
    @staticmethod
    def get_insert_sql() -> str:
        return """
               -- noinspection SqlResolve
               INSERT INTO wallets (id, user_id, wallet_code, name, wallet_type, wallet_account, description, balance, \
                                    currency, \
                                    color, icon, status, is_deleted, deleted_at, created_at, updated_at)
               VALUES (:id, :user_id, :wallet_code, :name, :wallet_type, :wallet_account, :description, :balance, \
                       :currency, \
                       :color, :icon, :status, :is_deleted, :deleted_at, :created_at, :updated_at)
               """

    @staticmethod
    def get_update_sql() -> str:
        """👑 Chuỗi SQL thô cập nhật chi tiết cấu hình ví"""
        return """
               -- noinspection SqlResolve
               UPDATE wallets
               SET wallet_code = :wallet_code,
                   name        = :name,
                   wallet_type = :wallet_type,
                   description = :description,
                   currency    = :currency,
                   color       = :color,
                   icon        = :icon,
                   updated_at  = :updated_at
               WHERE id = :id
               """

    @staticmethod
    def get_delete_sql() -> str:
        """👑 Chuỗi SQL thô lật flag xóa mềm, đóng dấu deleted_at và chuyển trạng thái thành DELETED"""
        return """
               -- noinspection SqlResolve
               UPDATE wallets
               SET is_deleted = 1,
                   status     = 'DELETED',
                   deleted_at = :deleted_at,
                   updated_at = :updated_at
               WHERE id = :id
               """

    @staticmethod
    def generate_unique_wallet_account(db_conn: Any) -> str:
        """👑 THUẬT TOÁN BỌC THÉP CHỐNG TRÙNG: Quét đệ quy liên tục xuống DB thô, bao giờ độc bản mới nhả số"""
        while True:
            # 1. Sinh chuỗi ngẫu nhiên mã hóa an toàn gồm 10 chữ số
            account_num = "".join([str(secrets.randbelow(10)) for _ in range(10)])

            # 2. Nã lệnh SQL thô kiểm tra sự tồn tại trong bảng wallets
            check_stmt = text("SELECT 1 FROM wallets WHERE wallet_account = :acc LIMIT 1;")
            existing = db_conn.execute(check_stmt, {"acc": account_num}).fetchone()

            # 3. Nếu không trùng dòng nào, chính thức nhả số tài khoản này ra để sử dụng
            if not existing:
                return account_num

    @staticmethod
    def build_params(db_conn: Any, user_id: str, wallet_code: str, name: str, currency: str, wallet_type: str = "CASH",
                     description: Optional[str] = None, color: Optional[str] = None,
                     icon: Optional[str] = None) -> Dict[str, Any]:
        now = datetime.now()
        initial_balance = Decimal("0.0000")

        # 👑 ĐÁNH DẤU CHỈNH SỬA: Gọi luồng kiểm trùng đệ quy bọc thép bảo vệ hệ thống trước khi trả thông số
        unique_account = WalletFactory.generate_unique_wallet_account(db_conn)

        return {
            "id": str(uuid.uuid4()),
            "user_id": user_id,
            "wallet_code": wallet_code,
            "name": name,
            "wallet_type": wallet_type,
            "wallet_account": unique_account,
            "description": description,
            "balance": initial_balance,
            "currency": currency.upper() if currency else "VND",
            "color": color,
            "icon": icon,
            "status": "ACTIVE",
            "is_deleted": 0,
            "deleted_at": None,
            "created_at": now,
            "updated_at": now
        }