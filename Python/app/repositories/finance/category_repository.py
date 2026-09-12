# 📄 Đường dẫn file: app/repositories/finance/category_repository.py
import uuid
from datetime import datetime
from sqlalchemy import select, or_, text
from app.models.finance.category import Category


class CategoryRepository:
    """
    👑 REPOSITORY: QUẢN LÝ TRUY VẤN DANH MỤC THU / CHI
    🎯 Nguyên tắc Bọc Thép: Chỉ truy vấn bảng `categories`, không JOIN chéo bảng khác.
    """

    @staticmethod
    def get_all_for_user(db, user_id: str = None) -> list:
        """Lấy tất cả danh mục hệ thống (user_id IS NULL) + danh mục riêng của User"""
        if user_id:
            stmt = select(Category).where(
                or_(Category.user_id.is_(None), Category.user_id == user_id),
                Category.status == "ACTIVE"
            ).order_by(Category.type, Category.name)
        else:
            stmt = select(Category).where(
                Category.user_id.is_(None),
                Category.status == "ACTIVE"
            ).order_by(Category.type, Category.name)
        
        records = db.execute(stmt).scalars().all()
        return [
            {
                "id": c.id,
                "user_id": c.user_id,
                "name": c.name,
                "type": c.type,
                "icon": c.icon,
                "color": c.color,
                "parent_id": c.parent_id,
                "status": c.status
            }
            for c in records
        ]

    @staticmethod
    def get_by_id(db, category_id: str) -> dict:
        stmt = select(Category).where(Category.id == category_id, Category.status == "ACTIVE")
        c = db.execute(stmt).scalars().first()
        if not c:
            return None
        return {
            "id": c.id,
            "user_id": c.user_id,
            "name": c.name,
            "type": c.type,
            "icon": c.icon,
            "color": c.color,
            "parent_id": c.parent_id,
            "status": c.status
        }

    @staticmethod
    def insert_category(db, user_id: str, name: str, cat_type: str, icon: str, color: str, parent_id: str = None) -> dict:
        now = datetime.now()
        cat_id = str(uuid.uuid4())
        new_cat = Category(
            id=cat_id,
            user_id=user_id,
            name=name,
            type=cat_type,
            icon=icon,
            color=color,
            parent_id=parent_id,
            status="ACTIVE",
            created_at=now,
            updated_at=now
        )
        db.add(new_cat)
        db.commit()
        db.refresh(new_cat)
        return {
            "id": new_cat.id,
            "user_id": new_cat.user_id,
            "name": new_cat.name,
            "type": new_cat.type,
            "icon": new_cat.icon,
            "color": new_cat.color,
            "parent_id": new_cat.parent_id,
            "status": new_cat.status
        }
