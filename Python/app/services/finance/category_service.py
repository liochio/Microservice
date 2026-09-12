# 📄 Đường dẫn file: app/services/finance/category_service.py
from app.repositories.finance.category_repository import CategoryRepository
from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException


class CategoryService:
    """
    👑 SERVICE: NGHIỆP VỤ DANH MỤC THU / CHI
    """

    @staticmethod
    def get_categories(db, user_id: str = None) -> list:
        return CategoryRepository.get_all_for_user(db, user_id)

    @staticmethod
    def create_category(db, user_id: str, payload) -> dict:
        # Tạo danh mục riêng cho người dùng
        return CategoryRepository.insert_category(
            db=db,
            user_id=user_id,
            name=payload.name,
            cat_type=payload.type,
            icon=payload.icon or "tag",
            color=payload.color or "#1976D2",
            parent_id=payload.parent_id
        )
