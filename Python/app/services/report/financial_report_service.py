
from sqlalchemy.orm import Session
from sqlalchemy import func
from datetime import datetime, timedelta
import io
import csv

from app.models.finance.transaction import Transaction
from app.models.finance.category import Category
from app.models.wallet.wallet import Wallet
from app.models.smart_piggy.smart_piggy_device import SmartPiggyDevice


class FinancialReportService:
    """👑 FINANCIAL ANALYTICS & STATEMENT EXPORT ENGINE"""

    @staticmethod
    def get_cash_flow_report(db: Session, user_id: str, days: int = 30) -> dict:
        start_date = datetime.now() - timedelta(days=days)
        txs = db.query(Transaction).filter(
            Transaction.user_id == user_id,
            Transaction.transaction_date >= start_date,
            Transaction.status == "COMPLETED"
        ).order_by(Transaction.transaction_date.asc()).all()

        daily_map = {}
        total_income = 0.0
        total_expense = 0.0

        for t in txs:
            d_str = t.transaction_date.strftime("%Y-%m-%d")
            if d_str not in daily_map:
                daily_map[d_str] = {"income": 0.0, "expense": 0.0}
            amt = float(t.amount)
            if t.transaction_type == "INCOME":
                daily_map[d_str]["income"] += amt
                total_income += amt
            elif t.transaction_type == "EXPENSE":
                daily_map[d_str]["expense"] += amt
                total_expense += amt

        points = [
            {
                "date": k,
                "total_income": v["income"],
                "total_expense": v["expense"],
                "net_savings": v["income"] - v["expense"]
            }
            for k, v in sorted(daily_map.items())
        ]

        return {
            "period": f"{days}_DAYS",
            "total_income": total_income,
            "total_expense": total_expense,
            "net_savings": total_income - total_expense,
            "daily_cash_flows": points
        }

    @staticmethod
    def get_category_breakdown(db: Session, user_id: str) -> dict:
        start_date = datetime.now() - timedelta(days=30)
        txs = db.query(Transaction).filter(
            Transaction.user_id == user_id,
            Transaction.transaction_type == "EXPENSE",
            Transaction.transaction_date >= start_date,
            Transaction.status == "COMPLETED"
        ).all()

        total_expense = sum(float(t.amount) for t in txs)
        cat_map = {}
        for t in txs:
            cid = t.category_id or "UNCATEGORIZED"
            cat_map[cid] = cat_map.get(cid, 0.0) + float(t.amount)

        categories = db.query(Category).all()
        cat_name_map = {c.id: c.name for c in categories}
        cat_color_map = {c.id: c.color for c in categories}

        items = []
        for cid, amt in cat_map.items():
            name = cat_name_map.get(cid, "Chi tiêu khác")
            pct = round((amt / total_expense * 100), 1) if total_expense > 0 else 0.0
            items.append({
                "category_id": cid if cid != "UNCATEGORIZED" else None,
                "category_name": name,
                "category_type": "EXPENSE",
                "total_amount": amt,
                "percentage": pct,
                "color": cat_color_map.get(cid, "#FF5722")
            })

        return {
            "total_expense": total_expense,
            "categories": sorted(items, key=lambda x: x["total_amount"], reverse=True)
        }

    @staticmethod
    def get_financial_summary(db: Session, user_id: str) -> dict:
        wallets = db.query(Wallet).filter(Wallet.user_id == user_id, Wallet.is_deleted == False).all()
        total_balance = sum(float(w.balance) for w in wallets)

        piggies = db.query(SmartPiggyDevice).filter(SmartPiggyDevice.user_id == user_id).all()
        total_piggy = sum(float(p.total_coins_dropped) for p in piggies)

        start_date = datetime.now() - timedelta(days=30)
        txs = db.query(Transaction).filter(
            Transaction.user_id == user_id,
            Transaction.transaction_date >= start_date,
            Transaction.status == "COMPLETED"
        ).all()

        m_income = sum(float(t.amount) for t in txs if t.transaction_type == "INCOME")
        m_expense = sum(float(t.amount) for t in txs if t.transaction_type == "EXPENSE")
        savings_rate = round(((m_income - m_expense) / m_income * 100), 1) if m_income > 0 else 0.0

        return {
            "total_wallets_balance": total_balance,
            "total_piggy_savings": total_piggy,
            "monthly_income": m_income,
            "monthly_expense": m_expense,
            "savings_rate_percentage": max(0.0, savings_rate),
            "financial_health_score": min(100, max(50, int(70 + savings_rate * 0.3)))
        }

    @staticmethod
    def export_transactions_csv(db: Session, user_id: str) -> str:
        """Xuất danh sách giao dịch ra CSV định dạng UTF-8 có BOM chống lỗi font tiếng Việt trong Excel"""
        txs = db.query(Transaction).filter(
            Transaction.user_id == user_id
        ).order_by(Transaction.transaction_date.desc()).all()

        output = io.StringIO()
        # UTF-8 BOM
        output.write('\ufeff')
        writer = csv.writer(output)
        writer.writerow(["Mã Giao Dịch", "Thời Gian", "Loại Giao Dịch", "Số Tiền (VND)", "Số Dư Trước (VND)", "Số Dư Sau (VND)", "Mô Tả", "Trạng Thái"])

        for t in txs:
            writer.writerow([
                t.transaction_code,
                t.transaction_date.strftime("%Y-%m-%d %H:%M:%S") if t.transaction_date else "",
                "Thu nhập (INCOME)" if t.transaction_type == "INCOME" else "Chi tiêu (EXPENSE)",
                f"{float(t.amount):,.0f}",
                f"{float(t.balance_before or 0):,.0f}",
                f"{float(t.balance_after or 0):,.0f}",
                t.description or "",
                t.status
            ])

        return output.getvalue()
