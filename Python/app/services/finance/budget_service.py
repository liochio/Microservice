# 📄 Đường dẫn file: app/services/finance/budget_service.py
from app.repositories.finance.budget_repository import BudgetRepository
from app.repositories.finance.category_repository import CategoryRepository
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException


class BudgetService:
    """
    👑 SERVICE: NGHIỆP VỤ QUẢN LÝ NGÂN SÁCH CHI TIÊU
    """

    @staticmethod
    def get_user_budgets(db, user_id: str) -> list:
        budgets = BudgetRepository.list_by_user(db, user_id)
        # Nạp thêm tên danh mục
        for b in budgets:
            cat = CategoryRepository.get_by_id(db, b["category_id"])
            b["category_name"] = cat["name"] if cat else "Danh mục"
        return budgets

    @staticmethod
    def create_budget(db, user_id: str, payload) -> dict:
        cat = CategoryRepository.get_by_id(db, payload.category_id)
        if not cat:
            raise FintechBaseException(error_code="BUDGET_CATEGORY_NOT_FOUND", status_code=404)

        new_b = BudgetRepository.insert_budget(
            db=db,
            user_id=user_id,
            category_id=payload.category_id,
            amount_limit=payload.amount_limit,
            start_date=payload.start_date,
            end_date=payload.end_date
        )
        new_b["category_name"] = cat["name"]
        return new_b

    @staticmethod
    def update_budget(db, user_id: str, budget_id: str, payload) -> dict:
        b = BudgetRepository.get_by_id(db, budget_id, user_id)
        if not b:
            raise FintechBaseException(error_code="BUDGET_NOT_FOUND", status_code=404)
        updated = BudgetRepository.update_budget(db, budget_id, user_id, payload.amount_limit)
        return updated

    @staticmethod
    def delete_budget(db, user_id: str, budget_id: str) -> bool:
        b = BudgetRepository.get_by_id(db, budget_id, user_id)
        if not b:
            raise FintechBaseException(error_code="BUDGET_NOT_FOUND", status_code=404)
        return BudgetRepository.delete_budget(db, budget_id, user_id)
