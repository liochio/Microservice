# 📄 Đường dẫn file: app/services/smart_piggy/smart_piggy_service.py
from datetime import datetime
from decimal import Decimal
from sqlalchemy import select, text
from app.models.wallet.wallet import Wallet
from app.models.finance.transaction import Transaction
from app.models.finance.category import Category
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException


class SmartPiggyService:
    """
    👑 SERVICE: ĐỒNG BỘ PHẦN CỨNG HEO ĐẤT IOT (ESP32/MQTT) VỚI HỆ THỐNG VÍ
    """

    @staticmethod
    def get_piggy_status(db, user_id: str) -> dict:
        # Tìm ví heo đất hoặc ví tiết kiệm của user
        piggy_wallet = db.execute(
            select(Wallet).where(Wallet.user_id == user_id, Wallet.wallet_type.in_(["SAVINGS", "SMART_PIGGY"]), Wallet.is_deleted == 0)
        ).scalars().first()

        current_saved = float(piggy_wallet.balance) if piggy_wallet else 0.0
        target = 10000000.0  # Mục tiêu mặc định: 10,000,000đ
        pct = round((current_saved / target * 100), 2) if target > 0 else 0.0

        return {
            "device_id": f"PIGGY-IOT-{user_id[:8].upper()}",
            "device_code": "SMART_PIGGY_ESP32_V1",
            "name": "Heo Đất Tiết Kiệm Thông Minh",
            "total_saved": current_saved,
            "target_goal": target,
            "goal_percentage": pct,
            "status": "ONLINE",
            "last_synced_at": datetime.now().isoformat()
        }

    @staticmethod
    def sync_coin_deposit(db, user_id: str, payload) -> dict:
        # 1. Tìm ví để nạp tiền vào (nếu không truyền wallet_id, nạp vào ví mặc định)
        if payload.wallet_id:
            wallet = db.execute(
                select(Wallet).where(Wallet.id == payload.wallet_id, Wallet.user_id == user_id, Wallet.is_deleted == 0)
            ).scalars().first()
        else:
            wallet = db.execute(
                select(Wallet).where(Wallet.user_id == user_id, Wallet.is_deleted == 0)
            ).scalars().first()

        if not wallet:
            raise FintechBaseException(error_code=SystemConstants.WALLET_NOT_FOUND, status_code=404)

        # 2. Cộng số dư ví tương ứng với số tiền xu/tiền lẻ đút heo
        current_bal = Decimal(str(wallet.balance))
        coin_amt = Decimal(str(payload.coin_amount))
        new_bal = current_bal + coin_amt
        wallet.balance = new_bal
        wallet.updated_at = datetime.now()

        # 3. Lấy hoặc tạo danh mục Heo đất tiết kiệm
        cat = db.execute(select(Category).where(Category.name == "Heo đất tiết kiệm")).scalars().first()
        cat_id = cat.id if cat else None
        if not cat_id:
            # Lấy danh mục Thu nhập khác
            cat_other = db.execute(select(Category).where(Category.type == "INCOME")).scalars().first()
            cat_id = cat_other.id if cat_other else None

        # 4. Ghi giao dịch nạp tiền từ Heo đất
        import uuid
        tx = Transaction(
            id=str(uuid.uuid4()),
            user_id=user_id,
            wallet_id=wallet.id,
            category_id=cat_id,
            transaction_code=f"TX-PIGGY-{datetime.now().strftime('%Y%m%d%H%M%S')}",
            amount=float(coin_amt),
            transaction_type="INCOME",
            transaction_date=datetime.now(),
            description=f"Đút heo đất IoT ({payload.device_code})",
            balance_before=float(current_bal),
            balance_after=float(new_bal),
            status="COMPLETED"
        )
        db.add(tx)
        db.commit()

        return {
            "device_code": payload.device_code,
            "deposited_amount": float(coin_amt),
            "new_wallet_balance": float(new_bal),
            "wallet_id": wallet.id,
            "transaction_code": tx.transaction_code,
            "timestamp": datetime.now().isoformat()
        }
