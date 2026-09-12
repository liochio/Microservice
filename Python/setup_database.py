# 📄 Đường dẫn file: setup_database.py
"""
👑 SCRIPT KHỞI TẠO CƠ SỞ DỮ LIỆU & NẠP DỮ LIỆU MẪU (FINTECH RESOURCE SERVER)
🎯 Mục đích:
   1. Kiểm tra kết nối và khởi tạo toàn bộ cấu trúc bảng (Schema) qua SQLAlchemy BaseEntity trên `liochio_fintech_db`.
   2. Nạp cấu hình hệ thống mặc định (System Settings).
   3. Nạp danh mục Phân hệ (Modules) & Ma trận quyền hạn (Permissions).
   4. Nạp các Vai trò cốt lõi (ROLE_ADMIN, ROLE_USER) và gán phân hệ tương ứng.
   5. Khởi tạo tài khoản Quản trị viên (usr_admin_00000001) và Người dùng thử nghiệm (usr_user_00000001) đồng bộ với Java IAM Core.
   6. Nạp Danh mục thu chi mặc định và khởi tạo ví tài chính mẫu.
   7. Đồng bộ toàn bộ từ điển đa ngôn ngữ i18n (errors, messages, labels) vào Database.
"""

import sys
import uuid
from pathlib import Path
from datetime import datetime, date
from sqlalchemy import select, insert, update

# Nạp đường dẫn project vào sys.path
PROJECT_ROOT = Path(__file__).resolve().parent
sys.path.append(str(PROJECT_ROOT))

from app.db.session import engine, SessionLocal
from app.models.common.base_entity import BaseEntity
from app.services.common.crypto_service import CryptoService
from app.core.translator.i18n_loader import auto_sync_i18n_json_to_db

# Import toàn bộ models để BaseEntity.metadata nhận diện đủ bảng
from app.models.user.user import User
from app.models.role.role import Role
from app.models.permission.permission import Permission
from app.models.module.module import Module
from app.models.user_role.user_role import UserRole, RoleModule
from app.models.common.system_setting import SystemSetting
from app.models.wallet.wallet import Wallet
from app.models.finance.category import Category


def init_schema():
    """Khởi tạo toàn bộ cấu trúc bảng trong CSDL nếu chưa tồn tại"""
    print("📦 [1/7] Khởi tạo cấu trúc Database Schema...")
    BaseEntity.metadata.create_all(bind=engine)
    print("✅ [1/7] Cấu trúc bảng đã sẵn sàng.")


def seed_system_settings(db):
    """Nạp các cài đặt hệ thống mặc định"""
    print("⚙️ [2/7] Nạp cấu hình hệ thống (System Settings)...")
    default_settings = [
        {"key": "LINK_TOKEN_EXPIRE_MINUTES", "value": "15", "desc": "Thời gian hết hạn của Link Token kích hoạt (phút)"},
        {"key": "OTP_EXPIRE_MINUTES", "value": "5", "desc": "Thời gian hết hạn của mã OTP (phút)"},
        {"key": "MAX_LOGIN_ATTEMPTS", "value": "5", "desc": "Số lần thử đăng nhập tối đa trước khi khóa tạm thời"},
        {"key": "PIGGY_SYNC_INTERVAL_SEC", "value": "30", "desc": "Chu kỳ đồng bộ số dư Heo đất IoT (giây)"},
        {"key": "DEFAULT_CURRENCY", "value": "VND", "desc": "Đơn vị tiền tệ mặc định của hệ thống"},
    ]

    now = datetime.now()
    for item in default_settings:
        existing = db.execute(select(SystemSetting).where(SystemSetting.key == item["key"])).scalars().first()
        if not existing:
            db.execute(insert(SystemSetting).values(
                key=item["key"],
                value=item["value"],
                type="SYSTEM",
                description=item["desc"],
                status="ACTIVE",
                created_at=now,
                updated_at=now
            ))
    db.commit()
    print("✅ [2/7] Hoàn tất nạp System Settings.")


def seed_modules_and_permissions(db):
    """Nạp danh mục phân hệ và ma trận quyền hạn chi tiết"""
    print("🔐 [3/7] Nạp Phân hệ (Modules) & Quyền hạn (Permissions)...")
    modules_data = [
        {
            "code": "AUTH_MGMT", "name": "Quản lý Xác thực & Phiên", "icon": "lock",
            "permissions": [
                ("AUTH_REGISTER", "Quyền đăng ký tài khoản mới"),
                ("AUTH_LOGIN", "Quyền đăng nhập hệ thống"),
                ("AUTH_REFRESH_TOKEN", "Quyền làm mới Access Token"),
                ("AUTH_LOGOUT", "Quyền đăng xuất và hủy phiên làm việc"),
            ]
        },
        {
            "code": "USER_MGMT", "name": "Quản lý Người dùng & Hồ sơ", "icon": "users",
            "permissions": [
                ("USER_VIEW", "Xem thông tin người dùng"),
                ("USER_UPDATE", "Cập nhật thông tin hồ sơ cá nhân"),
                ("USER_BLOCK", "Khóa / Mở khóa tài khoản người dùng"),
                ("USER_LIST", "Xem danh sách toàn bộ người dùng"),
            ]
        },
        {
            "code": "WALLET_MGMT", "name": "Quản lý Ví Tài chính", "icon": "wallet",
            "permissions": [
                ("WALLET_CREATE", "Khởi tạo ví tiền mới"),
                ("WALLET_DETAIL", "Xem chi tiết số dư và thông tin ví"),
                ("WALLET_LIST", "Xem danh sách các ví đang sở hữu"),
                ("WALLET_UPDATE", "Chỉnh sửa thông tin ví"),
                ("WALLET_LOCK", "Khóa tạm thời hoặc mở khóa ví"),
                ("WALLET_TOPUP", "Nạp tiền vào ví"),
                ("WALLET_WITHDRAW", "Rút tiền từ ví"),
                ("WALLET_DEFAULT", "Thiết lập ví mặc định"),
                ("WALLET_DELETE", "Xóa mềm ví tài khoản"),
            ]
        },
        {
            "code": "TRANSACTION_MGMT", "name": "Quản lý Giao dịch", "icon": "activity",
            "permissions": [
                ("TRANSACTION_CREATE", "Tạo giao dịch thu / chi mới"),
                ("TRANSACTION_LIST", "Xem lịch sử giao dịch"),
                ("TRANSACTION_DETAIL", "Xem chi tiết giao dịch"),
                ("TRANSACTION_UPDATE", "Chỉnh sửa giao dịch"),
                ("TRANSACTION_DELETE", "Xóa giao dịch"),
            ]
        },
        {
            "code": "GOAL_MGMT", "name": "Mục tiêu tài chính & Heo đất", "icon": "target",
            "permissions": [
                ("GOAL_CREATE", "Tạo mục tiêu tài chính"),
                ("GOAL_VIEW", "Xem tiến độ mục tiêu"),
                ("PIGGY_UNLOCK", "Mở khóa heo đất thông minh"),
            ]
        }
    ]

    now = datetime.now()
    for mod in modules_data:
        existing_mod = db.execute(select(Module).where(Module.code == mod["code"])).scalars().first()
        if not existing_mod:
            mod_id = str(uuid.uuid4())
            db.execute(insert(Module).values(
                id=mod_id,
                code=mod["code"],
                name=mod["name"],
                icon=mod.get("icon", "folder"),
                status="ACTIVE",
                created_at=now,
                updated_at=now
            ))
        else:
            mod_id = existing_mod.id

        for p_code, p_name in mod["permissions"]:
            existing_perm = db.execute(select(Permission).where(Permission.code == p_code)).scalars().first()
            if not existing_perm:
                db.execute(insert(Permission).values(
                    id=str(uuid.uuid4()),
                    module_id=mod_id,
                    code=p_code,
                    name=p_name,
                    status="ACTIVE",
                    created_at=now,
                    updated_at=now
                ))
            else:
                db.execute(
                    update(Permission)
                    .where(Permission.code == p_code)
                    .values(module_id=mod_id, status="ACTIVE", name=p_name)
                )
    db.commit()
    print("✅ [3/7] Hoàn tất nạp Modules & Permissions.")


def seed_roles_and_role_modules(db):
    """Nạp các vai trò cốt lõi và gán ma trận phân hệ tương ứng"""
    print("👑 [4/7] Nạp Vai trò (Roles) & Phân quyền Phân hệ (Role Modules)...")
    roles = [
        {"name": "ROLE_ADMIN", "desc": "Quản trị viên toàn hệ thống"},
        {"name": "ROLE_USER", "desc": "Người dùng thành viên tiêu chuẩn"},
    ]

    now = datetime.now()
    role_map = {}
    for r in roles:
        existing_role = db.execute(select(Role).where(Role.name == r["name"])).scalars().first()
        if not existing_role:
            r_id = str(uuid.uuid4())
            db.execute(insert(Role).values(
                id=r_id,
                name=r["name"],
                description=r["desc"],
                status="ACTIVE",
                created_at=now,
                updated_at=now
            ))
            role_map[r["name"]] = r_id
        else:
            role_map[r["name"]] = existing_role.id

    db.commit()

    all_modules = db.execute(select(Module)).scalars().all()
    for mod in all_modules:
        for r_name in ["ROLE_ADMIN"]:
            r_id = role_map[r_name]
            existing_rm = db.execute(
                select(RoleModule).where(RoleModule.role_id == r_id, RoleModule.module_id == mod.id)
            ).scalars().first()
            if not existing_rm:
                db.execute(insert(RoleModule).values(
                    id=str(uuid.uuid4()), role_id=r_id, module_id=mod.id, created_at=now, updated_at=now
                ))

        if mod.code in ["WALLET_MGMT", "TRANSACTION_MGMT", "GOAL_MGMT", "USER_MGMT"]:
            r_id = role_map["ROLE_USER"]
            existing_rm = db.execute(
                select(RoleModule).where(RoleModule.role_id == r_id, RoleModule.module_id == mod.id)
            ).scalars().first()
            if not existing_rm:
                db.execute(insert(RoleModule).values(
                    id=str(uuid.uuid4()), role_id=r_id, module_id=mod.id, created_at=now, updated_at=now
                ))

    db.commit()
    print("✅ [4/7] Hoàn tất gán phân hệ cho các Vai trò.")


def seed_default_accounts_and_wallets(db):
    """Khởi tạo tài khoản và ví tài chính mẫu (đồng bộ ID với Java IAM usr_xxxx)"""
    print("👤 [5/7] Khởi tạo tài khoản & Ví tài chính mẫu đồng bộ với IAM Core...")
    now = datetime.now()

    # 1. Admin ID: usr_admin_00000001
    admin_id = "usr_admin_00000001"
    existing_admin = db.execute(select(User).where(User.id == admin_id)).scalars().first()
    if not existing_admin:
        db.execute(insert(User).values(
            id=admin_id,
            username="admin",
            email="admin@liochio.com",
            phone_number="0900000001",
            password_hash=CryptoService.hash_password("Admin@123456"),
            full_name="Liochio System Administrator",
            date_of_birth=date(1990, 1, 1),
            gender="MALE",
            status="ACTIVE",
            is_active=True,
            is_verified=True,
            created_at=now,
            updated_at=now
        ))

    # 2. User ID: usr_user_00000001
    user_id = "usr_user_00000001"
    existing_user = db.execute(select(User).where(User.id == user_id)).scalars().first()
    if not existing_user:
        db.execute(insert(User).values(
            id=user_id,
            username="user",
            email="user@liochio.com",
            phone_number="0900000002",
            password_hash=CryptoService.hash_password("User@123456"),
            full_name="Liochio FinTech Client",
            date_of_birth=date(2000, 1, 1),
            gender="MALE",
            status="ACTIVE",
            is_active=True,
            is_verified=True,
            created_at=now,
            updated_at=now
        ))

    # 3. Tạo ví mẫu cho user_id: usr_user_00000001
    existing_wallet = db.execute(select(Wallet).where(Wallet.user_id == user_id)).scalars().first()
    if not existing_wallet:
        db.execute(insert(Wallet).values(
            id=str(uuid.uuid4()),
            user_id=user_id,
            wallet_code="CASH_VND_001",
            wallet_account="9998887771",
            name="Ví Tiền Mặt Chính",
            currency="VND",
            balance=5000000.0,
            wallet_type="CASH",
            status="ACTIVE",
            is_deleted=0,
            is_default=1,
            created_at=now,
            updated_at=now
        ))
        db.execute(insert(Wallet).values(
            id=str(uuid.uuid4()),
            user_id=user_id,
            wallet_code="PIGGY_SMART_001",
            wallet_account="8887776662",
            name="Heo Đất Thông Minh",
            currency="VND",
            balance=1250000.0,
            wallet_type="SMART_PIGGY",
            status="ACTIVE",
            is_deleted=0,
            is_default=0,
            created_at=now,
            updated_at=now
        ))
        print("   💰 Khởi tạo ví mẫu cho user: usr_user_00000001 (Ví tiền mặt: 5,000,000 VND, Heo đất: 1,250,000 VND)")

    db.commit()
    print("✅ [5/7] Hoàn tất khởi tạo tài khoản & ví mẫu.")


def seed_default_categories(db):
    """Nạp các danh mục thu/chi mặc định của hệ thống"""
    print("🏷️ [6/7] Nạp Danh mục Thu/Chi mặc định (Categories)...")
    default_categories = [
        {"name": "Ăn uống", "type": "EXPENSE", "icon": "utensils", "color": "#FF5722"},
        {"name": "Mua sắm", "type": "EXPENSE", "icon": "shopping-bag", "color": "#E91E63"},
        {"name": "Di chuyển", "type": "EXPENSE", "icon": "car", "color": "#2196F3"},
        {"name": "Nhà ở & Tiện ích", "type": "EXPENSE", "icon": "home", "color": "#9C27B0"},
        {"name": "Giải trí", "type": "EXPENSE", "icon": "film", "color": "#00BCD4"},
        {"name": "Y tế & Sức khỏe", "type": "EXPENSE", "icon": "heart", "color": "#4CAF50"},
        {"name": "Giáo dục", "type": "EXPENSE", "icon": "book", "color": "#FF9800"},
        {"name": "Tiền lương", "type": "INCOME", "icon": "dollar-sign", "color": "#4CAF50"},
        {"name": "Tiền thưởng", "type": "INCOME", "icon": "award", "color": "#FFEB3B"},
        {"name": "Đầu tư & Sinh lời", "type": "INCOME", "icon": "trending-up", "color": "#009688"},
        {"name": "Thu nhập khác", "type": "INCOME", "icon": "plus-circle", "color": "#607D8B"},
    ]
    now = datetime.now()
    for cat in default_categories:
        existing = db.execute(select(Category).where(Category.name == cat["name"], Category.user_id.is_(None))).scalars().first()
        if not existing:
            db.execute(insert(Category).values(
                id=str(uuid.uuid4()),
                user_id=None,
                name=cat["name"],
                type=cat["type"],
                icon=cat["icon"],
                color=cat["color"],
                status="ACTIVE",
                created_at=now,
                updated_at=now
            ))
    db.commit()
    print("✅ [6/7] Hoàn tất nạp Danh mục Thu/Chi.")


def sync_i18n_translations():
    """Đồng bộ từ điển đa ngôn ngữ vào bảng error_messages"""
    print("🌐 [7/7] Đồng bộ từ điển i18n JSON vào Database...")
    try:
        auto_sync_i18n_json_to_db()
        print("✅ [7/7] Đồng bộ i18n hoàn tất.")
    except Exception as e:
        print(f"⚠️ [7/7] Bỏ qua đồng bộ i18n: {str(e)}")


def main():
    print("=" * 70)
    print("🚀 BẮT ĐẦU KHỞI TẠO CƠ SỞ DỮ LIỆU RESOURCE SERVER (liochio_fintech_db)")
    print("=" * 70)

    # 1. Khởi tạo schema
    init_schema()

    # 2. Mở DB session để nạp dữ liệu
    db = SessionLocal()
    try:
        seed_system_settings(db)
        seed_modules_and_permissions(db)
        seed_roles_and_role_modules(db)
        seed_default_accounts_and_wallets(db)
        seed_default_categories(db)
        sync_i18n_translations()
        print("\n" + "=" * 70)
        print("🎉 QUÁ TRÌNH THIẾT LẬP CƠ SỞ DỮ LIỆU ĐÃ HOÀN TẤT THÀNH CÔNG 100%!")
        print("=" * 70)
    except Exception as e:
        db.rollback()
        print(f"\n❌ [CRITICAL_ERROR] Thiết lập thất bại: {str(e)}")
        raise e
    finally:
        db.close()


if __name__ == "__main__":
    main()