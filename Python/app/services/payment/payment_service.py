# 📄 Đường dẫn file: app/services/payment/payment_service.py
from sqlalchemy.orm import Session
from datetime import datetime
import uuid
import urllib.parse

from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException
from app.schemas.requests.payment import VietQrCreateRequest, PaymentWebhookPayload
from app.models.payment.payment_transaction import PaymentTransaction
from app.models.payment.payment_method import PaymentMethod
from app.models.payment.payment_webhook import PaymentWebhook
from app.models.wallet.wallet import Wallet
from app.repositories.finance.transaction_repository import TransactionRepository
from app.models.finance.category import Category


class PaymentService:
    """👑 VIETQR & PAYMENT GATEWAY WEBHOOK ENGINE (ACID TRANSACTION)"""

    @staticmethod
    def create_vietqr(db: Session, user_id: str, payload: VietQrCreateRequest) -> dict:
        """Sinh chuỗi mã VietQR chuẩn NAPAS 247"""
        order_id = f"ORDER-{datetime.now().strftime('%Y%m%d%H%M%S')}-{uuid.uuid4().hex[:4].upper()}"
        transfer_content = f"NAPTIEN {order_id}" if not payload.description else f"{order_id} {payload.description}"

        # URL VietQR chuẩn NAPAS QuickLink (VietQR.io open standard)
        encoded_content = urllib.parse.quote(transfer_content)
        encoded_name = urllib.parse.quote(payload.account_name)
        vietqr_url = f"https://img.vietqr.io/image/{payload.bank_code}-{payload.account_number}-compact2.png?amount={int(payload.amount)}&addInfo={encoded_content}&accountName={encoded_name}"
        qr_quicklink = f"https://api.vietqr.io/{payload.bank_code}/{payload.account_number}/{int(payload.amount)}/{encoded_content}/vietqr_net_2.jpg"

        # Đảm bảo có sẵn phương thức thanh toán VietQR
        pm = db.query(PaymentMethod).filter(PaymentMethod.code == "VIETQR").first()
        if not pm:
            pm = PaymentMethod(
                id=str(uuid.uuid4()),
                code="VIETQR",
                name="Cổng Nạp Tiền VietQR NAPAS 247",
                status="ACTIVE"
            )
            db.add(pm)
            db.flush()

        # Ghi transaction PENDING vào bảng payment_transactions
        pay_tx = PaymentTransaction(
            id=str(uuid.uuid4()),
            user_id=user_id,
            payment_method_id=pm.id,
            reference_order_id=order_id,
            amount=payload.amount,
            currency="VND",
            status="PENDING"
        )
        db.add(pay_tx)
        db.commit()

        return {
            "order_id": order_id,
            "amount": payload.amount,
            "currency": "VND",
            "bank_code": payload.bank_code,
            "account_number": payload.account_number,
            "account_name": payload.account_name,
            "transfer_content": transfer_content,
            "vietqr_url": vietqr_url,
            "qr_quicklink": qr_quicklink
        }

    @staticmethod
    def process_webhook(db: Session, payload: PaymentWebhookPayload) -> dict:
        """
        🛡️ XỬ LÝ WEBHOOK TỰ ĐỘNG CỘNG TIỀN ACID KHI NHẬN ĐƯỢC TIỀN TỪ NGÂN HÀNG:
        1. Tìm giao dịch theo reference_order_id trong nội dung chuyển khoản.
        2. Cộng số dư ví tài chính của User.
        3. Ghi log Transaction INCOME và đánh dấu Webhook SUCCESS.
        """
        # Lưu log raw webhook
        pm = db.query(PaymentMethod).filter(PaymentMethod.code == "VIETQR").first()
        pm_id = pm.id if pm else str(uuid.uuid4())

        wh = PaymentWebhook(
            id=str(uuid.uuid4()),
            payment_method_id=pm_id,
            payload=payload.model_dump(),
            status="PROCESSED"
        )
        db.add(wh)

        # Trích xuất mã order từ nội dung chuyển khoản (Ví dụ: "NAPTIEN ORDER-20260901...")
        content = payload.content.upper()
        matching_tx = None
        pending_txs = db.query(PaymentTransaction).filter(PaymentTransaction.status == "PENDING").all()
        for ptx in pending_txs:
            if ptx.reference_order_id.upper() in content:
                matching_tx = ptx
                break

        if matching_tx:
            matching_tx.status = "SUCCESS"
            matching_tx.gateway_transaction_id = payload.transaction_id
            matching_tx.updated_at = datetime.now()

            user_id = matching_tx.user_id
            amount = float(payload.amount)

            # Cộng tiền vào ví chính của User
            wallet = db.query(Wallet).filter(Wallet.user_id == user_id, Wallet.is_deleted == False).first()
            if wallet:
                bal_before = float(wallet.balance)
                wallet.balance = bal_before + amount
                bal_after = float(wallet.balance)
                wallet_id = wallet.id
            else:
                bal_before = 0.0
                bal_after = amount
                wallet_id = str(uuid.uuid4())

            income_cat = db.query(Category).filter(Category.type == "INCOME").first()
            cat_id = income_cat.id if income_cat else None

            TransactionRepository.insert_transaction(
                db=db,
                user_id=user_id,
                wallet_id=wallet_id,
                category_id=cat_id,
                amount=amount,
                tx_type="INCOME",
                tx_date=datetime.now(),
                description=f"Nạp tiền qua VietQR tự động: {matching_tx.reference_order_id}",
                balance_before=bal_before,
                balance_after=bal_after
            )

            db.commit()
            return {
                "matched": True,
                "order_id": matching_tx.reference_order_id,
                "amount_credited": amount,
                "new_wallet_balance": bal_after
            }

        db.commit()
        return {
            "matched": False,
            "message": "Không tìm thấy order khớp hoặc đã được xử lý trước đó."
        }

    @staticmethod
    def get_user_payment_history(db: Session, user_id: str) -> list:
        txs = db.query(PaymentTransaction).filter(PaymentTransaction.user_id == user_id).order_by(PaymentTransaction.created_at.desc()).all()
        return [
            {
                "id": t.id,
                "user_id": t.user_id,
                "reference_order_id": t.reference_order_id,
                "gateway_transaction_id": t.gateway_transaction_id,
                "amount": float(t.amount),
                "currency": t.currency,
                "status": t.status,
                "created_at": t.created_at
            }
            for t in txs
        ]
