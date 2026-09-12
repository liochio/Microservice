
# ============================================
# AUTO GENERATED FILE
# ============================================

import os


def create_file(path, content):

    os.makedirs(
        os.path.dirname(path),
        exist_ok=True
    )

    with open(
        path,
        "w",
        encoding="utf-8"
    ) as writer:

        writer.write(content)

    print(
        f"[SUCCESS] Created: {path}"
    )


def main():

    print(
        "🚀 Building models structure..."
    )


    create_file(
        "app/models/__init__.py",
        """

"""
    )



    create_file(
        "app/models/ai/ai_behavior_analysis.py",
        """
from sqlalchemy import Column, String, ForeignKey, Text, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class AiBehaviorAnalysis(Base):
    __tablename__ = "ai_behavior_analyses"
    __table_args__ = {"comment": "Phân tích hành vi chi tiêu rủi ro bất thường"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    risk_level = Column(String(50), nullable=False)
    anomaly_details = Column(Text, nullable=False)
    recommended_action = Column(String(255), nullable=True)
    model_version = Column(String(50), nullable=False)
    status = Column(String(50), server_default=text("'PROCESSED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/ai/ai_financial_score.py",
        """
from sqlalchemy import Column, String, ForeignKey, Integer, Numeric, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class AiFinancialScore(Base):
    __tablename__ = "ai_financial_scores"
    __table_args__ = {"comment": "Bảng chấm điểm sức khỏe tài chính cá nhân"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    score = Column(Integer, nullable=False)
    rating_tier = Column(String(50), nullable=False)
    debt_to_income_ratio = Column(Numeric(5, 4), nullable=False)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/ai/ai_model_log.py",
        """
from sqlalchemy import Column, String, Numeric, Integer, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class AiModelLog(Base):
    __tablename__ = "ai_model_logs"
    __table_args__ = {"comment": "Giám sát hiệu năng và thời gian huấn luyện mô hình AI"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    model_name = Column(String(100), nullable=False, index=True)
    accuracy = Column(Numeric(5, 4), nullable=True)
    loss = Column(Numeric(7, 4), nullable=True)
    training_duration_sec = Column(Integer, nullable=False)
    status = Column(String(50), server_default=text("'SUCCESS'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/ai/ai_prediction.py",
        """
from sqlalchemy import Column, String, ForeignKey, Numeric, DateTime, JSON, text
from sqlalchemy.sql import func
from app.db.base import Base

class AiPrediction(Base):
    __tablename__ = "ai_predictions"
    __table_args__ = {"comment": "Dự đoán xu hướng dòng tiền chi tiêu tương lai"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    target_date = Column(DateTime, nullable=False)
    predicted_amount = Column(Numeric(18, 4), nullable=False)
    confidence_score = Column(Numeric(5, 4), nullable=False)
    features_used = Column(JSON, nullable=True)
    status = Column(String(50), server_default=text("'COMPLETED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/ai/ai_recommendation.py",
        """
from sqlalchemy import Column, String, ForeignKey, Text, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class AiRecommendation(Base):
    __tablename__ = "ai_recommendations"
    __table_args__ = {"comment": "Gợi ý tối ưu hóa tài chính tiết kiệm thông minh"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    title = Column(String(255), nullable=False)
    recommendation_text = Column(Text, nullable=False)
    impact_level = Column(String(50), nullable=False)
    status = Column(String(50), server_default=text("'UNREAD'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/ai/__init__.py",
        """

"""
    )



    create_file(
        "app/models/audit/api_request_log.py",
        """
from sqlalchemy import Column, String, JSON, Integer, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class ApiRequestLog(Base):
    __tablename__ = "api_request_logs"
    __table_args__ = {"comment": "Bảng lưu nhật ký thô toàn bộ Request Response HTTP"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), nullable=True, index=True)
    endpoint = Column(String(500), nullable=False)
    method = Column(String(10), nullable=False)
    request_payload = Column(JSON, nullable=True)
    response_payload = Column(JSON, nullable=True)
    status_code = Column(Integer, nullable=False)
    latency_ms = Column(Integer, nullable=False)
    status = Column(String(50), server_default=text("'SUCCESS'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/audit/audit_log.py",
        """
from sqlalchemy import Column, String, ForeignKey, JSON, Index, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class AuditLog(Base):
    __tablename__ = "audit_logs"
    __table_args__ = (Index("ix_audit_user_created", "user_id", "created_at"), {"comment": "Bảng lịch sử kiểm toán lưu vết chi tiết"})

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="NO ACTION"), nullable=True)
    action = Column(String(255), nullable=False)
    table_name = Column(String(255), nullable=False)
    old_data = Column(JSON, nullable=True)
    new_data = Column(JSON, nullable=True)
    ip_address = Column(String(100), nullable=True)
    user_agent = Column(String(500), nullable=True)
    status = Column(String(50), server_default=text("'SUCCESS'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/audit/request_flow_log.py",
        """
from sqlalchemy import Column, String, Text, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class RequestFlowLog(Base):
    \"\"\"
    👑 REQUEST FLOW LOGS ENTITY MODEL (CLASSIC STYLE)
    🎯 Viết giống hệt phong cách AuditLog của sếp để hệ thống tự sinh bảng mượt mà.
    🔒 Tuyệt đối không đụng chạm hay làm ảnh hưởng tính năng cũ.
    \"\"\"
    __tablename__ = "request_flow_logs"
    __table_args__ = {"comment": "Bảng kiểm toán lưu vết dòng chảy request tuần tự end-to-end"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    trace_id = Column(String(36), nullable=False, index=True, comment="Trace-ID liên kết end-to-end dòng chảy dữ liệu")
    node = Column(String(50), nullable=False, index=True, comment="Tên chặng xử lý trong vòng đời request")
    details = Column(Text, nullable=True, comment="Chi tiết dữ liệu hoặc vết xích xử lý tại node")
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/audit/security_log.py",
        """
from sqlalchemy import Column, String, ForeignKey, Text, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SecurityLog(Base):
    __tablename__ = "security_logs"
    __table_args__ = {"comment": "Bảng ghi nhận nhật ký cảnh báo an ninh bảo mật và tấn công WAF"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="NO ACTION"), nullable=True)
    event_type = Column(String(100), nullable=False, index=True)
    severity = Column(String(20), nullable=False)
    description = Column(Text, nullable=False)
    ip_address = Column(String(100), nullable=False)
    status = Column(String(50), server_default=text("'TRIGGERED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/audit/system_log.py",
        """
from sqlalchemy import Column, String, Text, DateTime, text, Enum
from sqlalchemy.sql import func
from app.db.base import Base

class SystemLog(Base):
    __tablename__ = "system_logs"
    __table_args__ = {"comment": "Bảng lưu vết log lỗi Exception crash thô sâu của lõi hệ thống"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    error_type = Column(String(255), nullable=False, index=True)
    stack_trace = Column(Text, nullable=False)
    component = Column(String(100), nullable=False)
    status = Column(Enum("UNRESOLVED", "RESOLVED", "IGNORED", name="sys_log_status_enum"), server_default=text("'UNRESOLVED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/audit/__init__.py",
        """

"""
    )



    create_file(
        "app/models/auth/refresh_token.py",
        """
from sqlalchemy import Column, String, DateTime, ForeignKey, Boolean, text
from sqlalchemy.sql import func
from app.db.base import Base

class RefreshToken(Base):
    __tablename__ = "refresh_tokens"
    __table_args__ = {"comment": "Bảng quản lý Refresh Token"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    token = Column(String(500), unique=True, nullable=False, index=True)
    expires_at = Column(DateTime, nullable=False)
    is_revoked = Column(Boolean, server_default=text("0"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/auth/user_session.py",
        """
import uuid
from datetime import datetime
from sqlalchemy import String, Integer, DateTime
from sqlalchemy.orm import Mapped, mapped_column
from app.db.base import Base


class UserSession(Base):
    \"\"\"
    👑 USER SESSIONS ENTITY MODEL
    🎯 ĐÁNH DẤU CHỈNH SỬA: Quản lý vòng đời Refresh Token, vân tay thiết bị và JTI độc bản.
    \"\"\"
    __tablename__ = "user_sessions"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id: Mapped[str] = mapped_column(String(36), nullable=False, index=True)
    jti: Mapped[str] = mapped_column(String(255), nullable=False, unique=True, index=True)
    refresh_token_hash: Mapped[str] = mapped_column(String(255), nullable=False)
    device_info: Mapped[str] = mapped_column(String(500), nullable=True)
    ip_address: Mapped[str] = mapped_column(String(45), nullable=True)
    is_revoked: Mapped[int] = mapped_column(Integer, default=0, server_default="0")
    expires_at: Mapped[datetime] = mapped_column(DateTime, nullable=False)
    login_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.now)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.now)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.now)
"""
    )



    create_file(
        "app/models/common/base_entity.py",
        """
from app.db.base import Base

class BaseEntity(Base):
    \"\"\"👑 Thực thể gốc trừu tượng cho metadata engine \"\"\"
    __abstract__ = True

# 👑 SIÊU BỌC THÉP METADATA: Ép hệ thống nạp toàn bộ các phân hệ để tạo các bảng vật lý tự động

"""
    )



    create_file(
        "app/models/common/error_message.py",
        """
from sqlalchemy import Column, String, Text, DateTime, text, Enum
from sqlalchemy.sql import func
from app.db.base import Base

class ErrorMessage(Base):
    __tablename__ = "error_messages"
    __table_args__ = {"comment": "Bảng từ điển định nghĩa toàn bộ mã lỗi, nhãn UI, thông báo tập trung đa ngôn ngữ"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    error_code = Column(String(100), unique=True, nullable=False, index=True, comment="Mã code định danh hệ thống (Key)")
    msg_type = Column(Enum("ERROR", "MESSAGE", "LABEL", name="i18n_msg_type_enum"), nullable=False, index=True, comment="Phân loại: ERROR, MESSAGE, LABEL")
    lang_vi = Column(Text, nullable=False, comment="Nội dung hiển thị tiếng Việt")
    lang_en = Column(Text, nullable=False, comment="Nội dung hiển thị tiếng Anh")
    lang_zh = Column(Text, nullable=False, comment="Nội dung hiển thị tiếng Trung")
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False, comment="Trạng thái cấu hình")
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/common/mixins.py",
        """
# D:\\UIT - HK2\\FinanceProject\\app\\models\\common\\mixins.py

from sqlalchemy import Column, DateTime
from sqlalchemy.sql import func
from sqlalchemy.orm import declarative_mixin

@declarative_mixin
class TimestampMixin:
    # Bỏ sort_order để Alembic và SQLAlchemy bản cũ không báo lỗi khi sinh schema
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/common/system_setting.py",
        """
from sqlalchemy import Column, String, Text, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SystemSetting(Base):
    __tablename__ = "system_settings"
    __table_args__ = {"comment": "Bảng cấu hình cài đặt dùng chung toàn bộ hệ thống phân chia phân hệ type"}

    key = Column(String(100), primary_key=True, index=True, comment="Từ khóa cấu hình")
    value = Column(String(255), nullable=False, comment="Giá trị cấu hình")
    type = Column(String(50), nullable=False, index=True, comment="Phân loại cấu hình hệ thống (AUTH, NOTI, FINANCE...)")
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False, comment="Trạng thái cấu hình")
    description = Column(Text, nullable=True, comment="Mô tả chi tiết tác dụng cài đặt")
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/common/__init__.py",
        """

"""
    )



    create_file(
        "app/models/finance/budget.py",
        """
from sqlalchemy import Column, String, Numeric, DateTime, ForeignKey, text
from sqlalchemy.sql import func
from app.db.base import Base

class Budget(Base):
    __tablename__ = "budgets"
    __table_args__ = {"comment": "Bảng thiết lập ngân sách cảnh báo chi tiêu"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    category_id = Column(String(36), ForeignKey("categories.id", ondelete="CASCADE"), nullable=False, index=True)
    amount_limit = Column(Numeric(18, 4), nullable=False)
    current_spent = Column(Numeric(18, 4), server_default=text("0.0000"), nullable=False)
    start_date = Column(DateTime, nullable=False)
    end_date = Column(DateTime, nullable=False)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/finance/category.py",
        """
from sqlalchemy import Column, String, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class Category(Base):
    __tablename__ = "categories"
    __table_args__ = {"comment": "Bảng phân loại danh mục thu chi"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=True, index=True)
    name = Column(String(100), nullable=False)
    type = Column(String(20), nullable=False)
    parent_id = Column(String(36), ForeignKey("categories.id", ondelete="SET NULL"), nullable=True)
    icon = Column(String(100), nullable=True)
    color = Column(String(50), nullable=True)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/finance/financial_goal.py",
        """
from sqlalchemy import Column, String, Numeric, DateTime, ForeignKey, text
from sqlalchemy.sql import func
from app.db.base import Base

class FinancialGoal(Base):
    __tablename__ = "financial_goals"
    __table_args__ = {"comment": "Bảng theo dõi mục tiêu tích lũy tài chính"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    name = Column(String(255), nullable=False)
    target_amount = Column(Numeric(18, 4), nullable=False)
    current_amount = Column(Numeric(18, 4), server_default=text("0.0000"), nullable=False)
    deadline = Column(DateTime, nullable=True)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/finance/mock_bank_account.py",
        """
# D:\\UIT - HK2\\FinanceProject\\app\\models\\finance\\mock_bank_account.py

from sqlalchemy import Column, String, Numeric, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base


class MockBankAccount(Base):
    __tablename__ = "mock_bank_accounts"
    __table_args__ = {"comment": "Tài khoản ngân hàng giả lập"}

    id = Column(String(36), primary_key=True, server_default=text("(UUID())"))

    bank_name = Column(String(100), nullable=False)

    account_number = Column(String(50), nullable=False, unique=True)

    account_name = Column(String(255), nullable=False)

    balance = Column(Numeric(18, 4), server_default=text("0.0000"), nullable=False)

    currency = Column(String(10), server_default=text("'VND'"), nullable=False)

    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)

    created_at = Column(DateTime, server_default=func.now(), nullable=False)
"""
    )



    create_file(
        "app/models/finance/recurring_transaction.py",
        """
from sqlalchemy import Column, String, Numeric, DateTime, ForeignKey, Integer, text
from sqlalchemy.sql import func
from app.db.base import Base

class RecurringTransaction(Base):
    __tablename__ = "recurring_transactions"
    __table_args__ = {"comment": "Bảng thiết lập lịch thu chi định kỳ tự động"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="CASCADE"), nullable=False, index=True)
    category_id = Column(String(36), ForeignKey("categories.id", ondelete="CASCADE"), nullable=False, index=True)
    amount = Column(Numeric(18, 4), nullable=False)
    transaction_type = Column(String(20), nullable=False)
    frequency = Column(String(50), nullable=False)
    interval_days = Column(Integer, server_default=text("1"), nullable=False)
    next_run_date = Column(DateTime, nullable=False)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/finance/transaction.py",
        """
from sqlalchemy import Column, String, Numeric, DateTime, ForeignKey, Index, text
from sqlalchemy.sql import func
from app.db.base import Base


class Transaction(Base):
    __tablename__ = "transactions"

    __table_args__ = (
        Index("ix_transaction_user_date", "user_id", "transaction_date"),
        {"comment": "Bảng quản lý biến động giao dịch thu chi"}
    )

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")

    user_id = Column(String(36), ForeignKey("users.id", ondelete="RESTRICT"), nullable=False)

    wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="RESTRICT"), nullable=False, index=True)

    category_id = Column(String(36), ForeignKey("categories.id", ondelete="RESTRICT"), nullable=False, index=True)

    proof_file_id = Column(String(36), nullable=True, comment="ID tệp đính kèm hóa đơn chứng từ")

    amount = Column(Numeric(18, 4), nullable=False)

    transaction_type = Column(String(20), nullable=False, comment="TOPUP, INCOME, EXPENSE, TRANSFER")

    transaction_date = Column(DateTime, nullable=False)

    description = Column(String(500), nullable=True)

    status = Column(String(50), server_default=text("'COMPLETED'"), nullable=False)

    # ===== BỔ SUNG =====

    transaction_code = Column(String(50), unique=True, nullable=True, comment="Mã giao dịch duy nhất")

    balance_before = Column(Numeric(18, 4), nullable=True, comment="Số dư trước giao dịch")

    balance_after = Column(Numeric(18, 4), nullable=True, comment="Số dư sau giao dịch")

    source_type = Column(String(50), nullable=True, comment="BANK, EWALLET, CASH")

    source_name = Column(String(100), nullable=True, comment="Tên nguồn tiền")

    destination_wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="SET NULL"), nullable=True, comment="Ví nhận khi transfer")

    reference_id = Column(String(36), nullable=True, comment="Reference payment/transfer")

    external_transaction_id = Column(String(100), nullable=True, comment="Mã giao dịch từ hệ thống ngoài")

    failure_reason = Column(String(255), nullable=True, comment="Lý do thất bại")

    created_by = Column(String(36), nullable=True, comment="User tạo giao dịch")

    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")

    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/finance/transfer.py",
        """
from sqlalchemy import Column, String, Numeric, DateTime, ForeignKey, text
from sqlalchemy.sql import func
from app.db.base import Base

class Transfer(Base):
    __tablename__ = "transfers"
    __table_args__ = {"comment": "Bảng ghi nhận luồng chuyển tiền nội bộ giữa các ví"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="RESTRICT"), nullable=False, index=True)
    source_wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="RESTRICT"), nullable=False, index=True)
    destination_wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="RESTRICT"), nullable=False, index=True)
    amount = Column(Numeric(18, 4), nullable=False)
    transfer_date = Column(DateTime, nullable=False)
    fee = Column(Numeric(18, 4), server_default=text("0.0000"), nullable=False)
    description = Column(String(500), nullable=True)
    status = Column(String(50), server_default=text("'COMPLETED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/finance/__init__.py",
        """

"""
    )



    create_file(
        "app/models/iot/iot_device.py",
        """
from sqlalchemy import Column, String, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class IotDevice(Base):
    __tablename__ = "iot_devices"
    __table_args__ = {"comment": "Bảng danh mục thiết bị hạ tầng IoT"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    mac_address = Column(String(100), unique=True, nullable=False, index=True)
    device_name = Column(String(150), nullable=False)
    device_type = Column(String(100), nullable=False)
    firmware_version = Column(String(50), nullable=True)
    status = Column(String(50), server_default=text("'OFFLINE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/iot/iot_device_log.py",
        """
from sqlalchemy import Column, String, ForeignKey, Text, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class IotDeviceLog(Base):
    __tablename__ = "iot_device_logs"
    __table_args__ = {"comment": "Nhật ký vận hành, báo lỗi kết nối phần cứng"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    iot_device_id = Column(String(36), ForeignKey("iot_devices.id", ondelete="CASCADE"), nullable=False, index=True)
    log_level = Column(String(20), nullable=False)
    message = Column(Text, nullable=False)
    status = Column(String(50), server_default=text("'RECORDED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/iot/iot_sensor_data.py",
        """
from sqlalchemy import Column, String, ForeignKey, JSON, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class IotSensorData(Base):
    __tablename__ = "iot_sensor_data"
    __table_args__ = {"comment": "Dữ liệu cảm biến thô từ hạ tầng IoT"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    iot_device_id = Column(String(36), ForeignKey("iot_devices.id", ondelete="CASCADE"), nullable=False, index=True)
    sensor_type = Column(String(100), nullable=False, index=True)
    reading_payload = Column(JSON, nullable=False)
    status = Column(String(50), server_default=text("'PROCESSED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/iot/iot_transaction.py",
        """
from sqlalchemy import Column, String, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class IotTransaction(Base):
    __tablename__ = "iot_transactions"
    __table_args__ = {"comment": "Giao dịch tài chính tự động kích hoạt bởi IoT"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    iot_device_id = Column(String(36), ForeignKey("iot_devices.id", ondelete="RESTRICT"), nullable=False, index=True)
    transaction_id = Column(String(36), ForeignKey("transactions.id", ondelete="CASCADE"), nullable=False, index=True)
    trigger_event = Column(String(255), nullable=False)
    status = Column(String(50), server_default=text("'COMPLETED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/iot/__init__.py",
        """

"""
    )



    create_file(
        "app/models/ledger/accounting_period.py",
        """
from sqlalchemy import Column, String, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class AccountingPeriod(Base):
    __tablename__ = "accounting_periods"
    __table_args__ = {"comment": "Kỳ kế toán hệ thống phục vụ chốt sổ đóng băng"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    period_name = Column(String(100), nullable=False)
    start_date = Column(DateTime, nullable=False)
    end_date = Column(DateTime, nullable=False)
    status = Column(String(50), server_default=text("'OPEN'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/ledger/journal_entry.py",
        """
from sqlalchemy import Column, String, DateTime, text
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.db.base import Base

class JournalEntry(Base):
    __tablename__ = "journal_entries"
    __table_args__ = {"comment": "Bảng bút toán tổng hợp đầu não cho toán bộ hệ thống kế toán"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    reference_id = Column(String(36), nullable=False, index=True)
    reference_type = Column(String(100), nullable=False)
    entry_date = Column(DateTime, nullable=False, index=True)
    description = Column(String(500), nullable=True)
    status = Column(String(50), server_default=text("'POSTED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

    details = relationship("JournalEntryDetail", back_populates="journal_entry", cascade="all, delete-orphan")
"""
    )



    create_file(
        "app/models/ledger/journal_entry_detail.py",
        """
from sqlalchemy import Column, String, ForeignKey, Numeric, DateTime, text
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.db.base import Base

class JournalEntryDetail(Base):
    __tablename__ = "journal_entry_details"
    __table_args__ = {"comment": "Chi tiết dòng hạch toán kế toán"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    journal_entry_id = Column(String(36), ForeignKey("journal_entries.id", ondelete="CASCADE"), nullable=False, index=True)
    ledger_account_id = Column(String(36), ForeignKey("ledger_accounts.id", ondelete="RESTRICT"), nullable=False, index=True)
    entry_side = Column(String(10), nullable=False)
    amount = Column(Numeric(18, 4), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

    journal_entry = relationship("JournalEntry", back_populates="details")
"""
    )



    create_file(
        "app/models/ledger/ledger_account.py",
        """
from sqlalchemy import Column, String, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class LedgerAccount(Base):
    __tablename__ = "ledger_accounts"
    __table_args__ = {"comment": "Hệ thống tài khoản kế toán xương sống"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    account_code = Column(String(50), unique=True, nullable=False, index=True)
    account_name = Column(String(255), nullable=False)
    account_type = Column(String(50), nullable=False)
    parent_id = Column(String(36), ForeignKey("ledger_accounts.id", ondelete="SET NULL"), nullable=True)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/ledger/models.py",
        """

"""
    )



    create_file(
        "app/models/ledger/reconciliation_log.py",
        """
from sqlalchemy import Column, String, DateTime, Numeric, Text, text
from sqlalchemy.sql import func
from app.db.base import Base

class ReconciliationLog(Base):
    __tablename__ = "reconciliation_logs"
    __table_args__ = {"comment": "Nhật ký đối soát định kỳ hệ thống kế toán"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    reconciliation_date = Column(DateTime, nullable=False, index=True)
    source_system = Column(String(100), nullable=False)
    target_system = Column(String(100), nullable=False)
    total_source_amount = Column(Numeric(18, 4), nullable=False)
    total_target_amount = Column(Numeric(18, 4), nullable=False)
    difference_amount = Column(Numeric(18, 4), nullable=False)
    discrepancy_details = Column(Text, nullable=True)
    status = Column(String(50), server_default=text("'MATCHED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/ledger/__init__.py",
        """

"""
    )



    create_file(
        "app/models/module/module.py",
        """
from sqlalchemy import Column, String, DateTime, Index, text, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.db.base import Base

class Module(Base):
    __tablename__ = "modules"
    __table_args__ = (Index("ux_modules_code", "code", unique=True), {"comment": "Bảng quản lý phân hệ chức năng menu động phân cấp cha-con"})

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    code = Column(String(100), nullable=False, comment="Mã định danh module (VD: FINANCE, WALLET_MGMT)")
    name = Column(String(150), nullable=False, comment="Tên hiển thị menu")
    icon = Column(String(100), nullable=True, comment="Icon hiển thị")
    parent_id = Column(String(36), ForeignKey("modules.id", ondelete="CASCADE"), nullable=True, comment="ID của module cha nếu phân cấp cây")
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

    # FIX DỨT ĐIỂM: Thêm single_parent=True để cho phép delete-orphan trên quan hệ cha-con
    sub_modules = relationship(
        "Module",
        back_populates="parent",
        cascade="all, delete-orphan",
        remote_side=[id],
        single_parent=True
    )
    parent = relationship("Module", back_populates="sub_modules", remote_side=[parent_id])
    permissions = relationship("Permission", back_populates="module", cascade="all, delete-orphan")
"""
    )



    create_file(
        "app/models/module/__init__.py",
        """

"""
    )



    create_file(
        "app/models/notification/notification.py",
        """
from sqlalchemy import Column, String, Text, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class Notification(Base):
    __tablename__ = "notifications"
    __table_args__ = {"comment": "Bảng lưu trữ thông báo phía người dùng"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    title = Column(String(255), nullable=False)
    content = Column(Text, nullable=False)
    notification_type = Column(String(50), nullable=False, index=True)
    is_read = Column(String(10), server_default=text("'UNREAD'"), nullable=False)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/notification/notification_log.py",
        """
from sqlalchemy import Column, String, ForeignKey, Text, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class NotificationLog(Base):
    __tablename__ = "notification_logs"
    __table_args__ = {"comment": "Lịch sử đẩy tin nhắn qua nhà mạng SMS, Firebase Push"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    notification_id = Column(String(36), ForeignKey("notifications.id", ondelete="CASCADE"), nullable=False, index=True)
    channel = Column(String(50), nullable=False, index=True)
    recipient_target = Column(String(255), nullable=False)
    gateway_response = Column(Text, nullable=True)
    status = Column(String(50), server_default=text("'SENT'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/notification/notification_template.py",
        """
from sqlalchemy import Column, String, Text, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class NotificationTemplate(Base):
    __tablename__ = "notification_templates"
    __table_args__ = {"comment": "Bảng lưu trữ mẫu tin nhắn thông báo đa ngôn ngữ"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    template_code = Column(String(100), unique=True, nullable=False, index=True)
    title_template_vi = Column(String(255), nullable=False)
    content_template_vi = Column(Text, nullable=False)
    title_template_en = Column(String(255), nullable=False)
    content_template_en = Column(Text, nullable=False)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/notification/push_device.py",
        """
from sqlalchemy import Column, String, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class PushDevice(Base):
    __tablename__ = "push_devices"
    __table_args__ = {"comment": "Bảng lưu Device Token quản lý thiết bị Firebase"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    device_token = Column(String(500), nullable=False, index=True)
    device_type = Column(String(50), nullable=False)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/notification/__init__.py",
        """

"""
    )



    create_file(
        "app/models/ocr/ocr_extracted_item.py",
        """
from sqlalchemy import Column, String, ForeignKey, Numeric, Integer, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class OcrExtractedItem(Base):
    __tablename__ = "ocr_extracted_items"
    __table_args__ = {"comment": "Chi tiết từng mặt hàng bốc tách được trong hóa đơn"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    ocr_result_id = Column(String(36), ForeignKey("ocr_results.id", ondelete="CASCADE"), nullable=False, index=True)
    item_name = Column(String(255), nullable=False)
    quantity = Column(Integer, nullable=False)
    unit_price = Column(Numeric(18, 4), nullable=False)
    total_price = Column(Numeric(18, 4), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/ocr/ocr_result.py",
        """
from sqlalchemy import Column, String, ForeignKey, Numeric, JSON, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class OcrResult(Base):
    __tablename__ = "ocr_results"
    __table_args__ = {"comment": "Bảng tổng hợp bốc tách dữ liệu hóa đơn thô"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    stored_file_id = Column(String(36), ForeignKey("stored_files.id", ondelete="CASCADE"), nullable=False, index=True)
    merchant_name = Column(String(255), nullable=True, index=True)
    total_amount = Column(Numeric(18, 4), nullable=True)
    raw_ocr_json = Column(JSON, nullable=False)
    status = Column(String(50), server_default=text("'PROCESSED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/ocr/__init__.py",
        """

"""
    )



    create_file(
        "app/models/payment/payment_method.py",
        """
from sqlalchemy import Column, String, JSON, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class PaymentMethod(Base):
    __tablename__ = "payment_methods"
    __table_args__ = {"comment": "Cấu hình tích hợp cổng thanh toán trực tuyến"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    code = Column(String(50), unique=True, nullable=False, index=True)
    name = Column(String(100), nullable=False)
    config_payload = Column(JSON, nullable=True)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/payment/payment_reconciliation.py",
        """
from sqlalchemy import Column, String, DateTime, Numeric, text
from sqlalchemy.sql import func
from app.db.base import Base

class PaymentReconciliation(Base):
    __tablename__ = "payment_reconciliations"
    __table_args__ = {"comment": "Bảng khớp lệnh đối soát chi tiết cổng thanh toán"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    reconciliation_date = Column(DateTime, nullable=False, index=True)
    payment_method_code = Column(String(50), nullable=False, index=True)
    total_gateway_transactions = Column(Numeric(18, 4), nullable=False)
    total_system_transactions = Column(Numeric(18, 4), nullable=False)
    status = Column(String(50), server_default=text("'SUCCESS'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/payment/payment_refund.py",
        """
from sqlalchemy import Column, String, Numeric, ForeignKey, Text, JSON, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class PaymentRefund(Base):
    __tablename__ = "payment_refunds"
    __table_args__ = {"comment": "Quản lý hoàn tiền giao dịch thanh toán trực tuyến"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    payment_transaction_id = Column(String(36), ForeignKey("payment_transactions.id", ondelete="RESTRICT"), nullable=False, index=True)
    refund_gateway_id = Column(String(255), unique=True, nullable=True, index=True)
    amount = Column(Numeric(18, 4), nullable=False)
    reason = Column(Text, nullable=False)
    gateway_response = Column(JSON, nullable=True)
    status = Column(String(50), server_default=text("'PENDING'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/payment/payment_transaction.py",
        """
from sqlalchemy import Column, String, Numeric, ForeignKey, JSON, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class PaymentTransaction(Base):
    __tablename__ = "payment_transactions"
    __table_args__ = {"comment": "Giao dịch thanh toán cổng trực tuyến trực tiếp"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="RESTRICT"), nullable=False, index=True)
    payment_method_id = Column(String(36), ForeignKey("payment_methods.id", ondelete="RESTRICT"), nullable=False, index=True)
    reference_order_id = Column(String(100), unique=True, nullable=False, index=True)
    gateway_transaction_id = Column(String(255), unique=True, nullable=True, index=True)
    amount = Column(Numeric(18, 4), nullable=False)
    currency = Column(String(10), server_default=text("'VND'"), nullable=False)
    gateway_response = Column(JSON, nullable=True)
    status = Column(String(50), server_default=text("'PENDING'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/payment/payment_webhook.py",
        """
from sqlalchemy import Column, String, ForeignKey, JSON, Integer, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class PaymentWebhook(Base):
    __tablename__ = "payment_webhooks"
    __table_args__ = {"comment": "Nhật ký tiếp nhận tín hiệu Webhook từ cổng ngân hàng"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    payment_method_id = Column(String(36), ForeignKey("payment_methods.id", ondelete="RESTRICT"), nullable=False, index=True)
    payload = Column(JSON, nullable=False)
    ip_address = Column(String(100), nullable=True)
    http_status_code = Column(Integer, nullable=True)
    status = Column(String(50), server_default=text("'RECEIVED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/permission/permission.py",
        """
from sqlalchemy import Column, String, DateTime, ForeignKey, Index, text
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.db.base import Base

class Permission(Base):
    __tablename__ = "permissions"
    __table_args__ = (Index("ux_permissions_code", "code", unique=True), {"comment": "Bảng danh mục quyền hạn hành động API chi tiết"})

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    module_id = Column(String(36), ForeignKey("modules.id", ondelete="CASCADE"), nullable=False, comment="Liên kết sang phân hệ modules")
    code = Column(String(100), nullable=False, comment="Mã quyền hạn (VD: WALLET_FREEZE)")
    name = Column(String(255), nullable=False, comment="Mô tả chi tiết quyền")
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

    module = relationship("Module", back_populates="permissions")
"""
    )



    create_file(
        "app/models/role/role.py",
        """
from sqlalchemy import Column, String, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class Role(Base):
    __tablename__ = "roles"
    __table_args__ = {"comment": "Bảng định nghĩa vai trò người dùng"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    name = Column(String(100), unique=True, nullable=False, index=True)
    description = Column(String(255), nullable=True)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/role/__init__.py",
        """

"""
    )



    create_file(
        "app/models/smart_piggy/smart_piggy_coin_log.py",
        """
from sqlalchemy import Column, String, ForeignKey, Numeric, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SmartPiggyCoinLog(Base):
    __tablename__ = "smart_piggy_coin_logs"
    __table_args__ = {"comment": "Nhật ký hành vi bỏ tiền xu vật lý"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    smart_piggy_device_id = Column(String(36), ForeignKey("smart_piggy_devices.id", ondelete="CASCADE"), nullable=False, index=True)
    coin_value = Column(Numeric(18, 4), nullable=False)
    status = Column(String(50), server_default=text("'SUCCESS'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/smart_piggy/smart_piggy_device.py",
        """
from sqlalchemy import Column, String, ForeignKey, Numeric, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SmartPiggyDevice(Base):
    __tablename__ = "smart_piggy_devices"
    __table_args__ = {"comment": "Bảng quản lý cốt lõi phần cứng Heo đất thông minh"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="RESTRICT"), nullable=False, index=True)
    wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="RESTRICT"), nullable=False, index=True)
    mac_address = Column(String(100), unique=True, nullable=False, index=True)
    device_name = Column(String(150), nullable=False)
    total_coins_dropped = Column(Numeric(18, 4), server_default=text("0.0000"), nullable=False)
    status = Column(String(50), server_default=text("'ONLINE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/smart_piggy/smart_piggy_firmware.py",
        """
from sqlalchemy import Column, String, Text, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SmartPiggyFirmware(Base):
    __tablename__ = "smart_piggy_firmwares"
    __table_args__ = {"comment": "Quản lý các bản build binary Firmware nâng cấp OTA"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    version = Column(String(50), unique=True, nullable=False, index=True)
    firmware_file_path = Column(String(500), nullable=False)
    changelog = Column(Text, nullable=True)
    status = Column(String(50), server_default=text("'RELEASED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/smart_piggy/smart_piggy_gamification.py",
        """
from sqlalchemy import Column, String, ForeignKey, Integer, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SmartPiggyGamification(Base):
    __tablename__ = "smart_piggy_gamifications"
    __table_args__ = {"comment": "Hệ thống tính điểm Level, Streak ngày nuôi heo"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    current_points = Column(Integer, server_default=text("0"), nullable=False)
    current_level = Column(Integer, server_default=text("1"), nullable=False)
    streak_days = Column(Integer, server_default=text("0"), nullable=False)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/smart_piggy/smart_piggy_goal.py",
        """
from sqlalchemy import Column, String, ForeignKey, Numeric, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SmartPiggyGoal(Base):
    __tablename__ = "smart_piggy_goals"
    __table_args__ = {"comment": "Mục tiêu nuôi heo đất tích lũy dành cho trẻ em"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    smart_piggy_device_id = Column(String(36), ForeignKey("smart_piggy_devices.id", ondelete="CASCADE"), nullable=False, index=True)
    goal_name = Column(String(255), nullable=False)
    target_amount = Column(Numeric(18, 4), nullable=False)
    current_amount = Column(Numeric(18, 4), server_default=text("0.0000"), nullable=False)
    deadline = Column(DateTime, nullable=True)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/smart_piggy/smart_piggy_led_log.py",
        """
from sqlalchemy import Column, String, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SmartPiggyLedLog(Base):
    __tablename__ = "smart_piggy_led_logs"
    __table_args__ = {"comment": "Nhật ký điều khiển hiệu ứng đèn LED báo trạng thái"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    smart_piggy_device_id = Column(String(36), ForeignKey("smart_piggy_devices.id", ondelete="CASCADE"), nullable=False, index=True)
    led_effect = Column(String(100), nullable=False)
    triggered_by = Column(String(255), nullable=False)
    status = Column(String(50), server_default=text("'EXECUTED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/smart_piggy/smart_piggy_reward.py",
        """
from sqlalchemy import Column, String, ForeignKey, Numeric, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SmartPiggyReward(Base):
    __tablename__ = "smart_piggy_rewards"
    __table_args__ = {"comment": "Quản lý phần thưởng động viên khích lệ"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    smart_piggy_goal_id = Column(String(36), ForeignKey("smart_piggy_goals.id", ondelete="CASCADE"), nullable=False, index=True)
    reward_name = Column(String(255), nullable=False)
    points_cost = Column(Numeric(10, 2), nullable=False)
    status = Column(String(50), server_default=text("'AVAILABLE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/smart_piggy/smart_piggy_sensor.py",
        """
from sqlalchemy import Column, String, ForeignKey, JSON, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class SmartPiggySensor(Base):
    __tablename__ = "smart_piggy_sensors"
    __table_args__ = {"comment": "Dữ liệu cảm biến môi trường trên vỏ heo đất"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    smart_piggy_device_id = Column(String(36), ForeignKey("smart_piggy_devices.id", ondelete="CASCADE"), nullable=False, index=True)
    sensor_type = Column(String(100), nullable=False, index=True)
    reading_payload = Column(JSON, nullable=False)
    status = Column(String(50), server_default=text("'PROCESSED'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/storage/stored_file.py",
        """
from sqlalchemy import Column, String, BigInteger, DateTime, text, Enum
from sqlalchemy.dialects.mysql import LONGTEXT
from sqlalchemy.sql import func
from app.db.base import Base

class StoredFile(Base):
    __tablename__ = "stored_files"
    __table_args__ = {"comment": "Bảng lưu trữ siêu dữ liệu file và nội dung text bốc tách base64/CLOB"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    file_name = Column(String(255), nullable=False)
    unique_name = Column(String(255), unique=True, nullable=False, index=True)
    file_path = Column(String(500), nullable=True)
    file_size = Column(BigInteger, nullable=False)
    mime_type = Column(String(100), nullable=False)
    file_type = Column(Enum("AVATAR", "TRANSACTION_PROOF", "OCR_BILL", "FIRMWARE_BINARY", "OTHER", name="file_type_enum"), nullable=False)
    content = Column(LONGTEXT, nullable=True)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/user/user.py",
        """
from sqlalchemy import Column, String, Boolean, DateTime, Index, text, ForeignKey, Date, Enum
from sqlalchemy.sql import func
from app.db.base import Base

class User(Base):
    __tablename__ = "users"
    __table_args__ = (Index("ix_users_email_phone", "email", "phone_number"), {"comment": "Bảng người dùng cốt lõi hệ thống"})

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    username = Column(String(100), unique=True, nullable=False, index=True)
    email = Column(String(255), unique=True, nullable=False, index=True)
    phone_number = Column(String(20), unique=True, nullable=True, index=True)
    password_hash = Column(String(255), nullable=False)
    full_name = Column(String(255), nullable=True)
    date_of_birth = Column(Date, nullable=True)
    gender = Column(Enum("MALE", "FEMALE", "OTHER", name="gender_enum"), nullable=True)
    avatar_file_id = Column(String(36), ForeignKey("stored_files.id", ondelete="SET NULL"), nullable=True)
    is_active = Column(Boolean, server_default=text("1"), nullable=False)
    is_verified = Column(Boolean, server_default=text("0"), nullable=False)
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/user_otp/user_otp.py",
        """
import uuid
from datetime import datetime
from sqlalchemy import String, Integer, DateTime, Text
from sqlalchemy.orm import Mapped, mapped_column
from app.db.base import Base


class UserOtp(Base):
    \"\"\"
    👑 USER OTP ENTITY MODEL
    🎯 ĐÁNH DẤU CHỈNH SỬA: Bổ sung trường retry_count chặn Brute-force.
    \"\"\"
    __tablename__ = "user_otps"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id: Mapped[str] = mapped_column(String(36), nullable=False, index=True)
    otp_code: Mapped[str] = mapped_column(String(255), nullable=False)  # Chứa chuỗi Hash Bcrypt
    type: Mapped[str] = mapped_column(String(50), nullable=False)
    content: Mapped[str] = mapped_column(Text, nullable=True)
    is_used: Mapped[int] = mapped_column(Integer, default=0, server_default="0")
    status: Mapped[str] = mapped_column(String(50), default="ACTIVE", server_default="'ACTIVE'")

    # 👑 ĐÁNH DẤU CHỈNH SỬA: Bộ đếm chống Spam/Brute-force
    retry_count: Mapped[int] = mapped_column(Integer, default=0, server_default="0")
    max_retries: Mapped[int] = mapped_column(Integer, default=3, server_default="3")

    expired_at: Mapped[datetime] = mapped_column(DateTime, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.now)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.now, onupdate=datetime.now)
"""
    )



    create_file(
        "app/models/user_role/user_role.py",
        """
from sqlalchemy import Column, String, ForeignKey, DateTime, Index, text
from sqlalchemy.sql import func
from app.db.base import Base

class UserRole(Base):
    __tablename__ = "user_roles"
    __table_args__ = (Index("ux_user_role", "user_id", "role_id", unique=True), {"comment": "Bảng trung gian liên kết Người dùng và Vai trò"})

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    role_id = Column(String(36), ForeignKey("roles.id", ondelete="CASCADE"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")

class RoleModule(Base):
    __tablename__ = "role_modules"
    __table_args__ = (Index("ux_role_module", "role_id", "module_id", unique=True), {"comment": "Bảng trung gian liên kết Vai trò và Module"})

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    role_id = Column(String(36), ForeignKey("roles.id", ondelete="CASCADE"), nullable=False)
    module_id = Column(String(36), ForeignKey("modules.id", ondelete="CASCADE"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/wallet/wallet.py",
        """
# D:\\UIT - HK2\\FinanceProject\\app\\models\\finance\\wallet.py

from sqlalchemy import Column, String, Numeric, ForeignKey, DateTime, Boolean, text
from sqlalchemy.sql import func
from app.db.base import Base


class Wallet(Base):
    __tablename__ = "wallets"
    __table_args__ = {"comment": "Bảng ví tài khoản lưu trữ số dư trực tiếp"}
    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    user_id = Column(String(36), ForeignKey("users.id", ondelete="RESTRICT"), nullable=False, index=True)
    wallet_code = Column(String(50), unique=True, nullable=False, comment="Mã ví duy nhất (vd: VPBANK_001)")
    name = Column(String(100), nullable=False)
    wallet_type = Column(String(50), nullable=False, index=True, comment="Phân loại: CASH, BANK, EWALLET, SAVINGS, SMART_PIGGY")
    wallet_account = Column(String(50), nullable=False, comment="Số tài khoản ví")
    balance = Column(Numeric(18, 4), server_default=text("0.0000"), nullable=False)
    currency = Column(String(10), server_default=text("'VND'"), nullable=False)
    color = Column(String(20), nullable=True, comment="Mã màu hiển thị UI")
    icon = Column(String(50), nullable=True, comment="Icon hiển thị UI")
    description = Column(String(255), nullable=True, comment="Mô tả mục đích sử dụng ví")
    status = Column(String(50), server_default=text("'ACTIVE'"), nullable=False, index=True, comment="ACTIVE, LOCKED, ARCHIVED")
    is_deleted = Column(Boolean, server_default=text("0"), nullable=False, comment="Cờ xóa mềm")
    deleted_at = Column(DateTime, nullable=True, comment="Thời điểm bị xóa mềm")
    is_default = Column(Boolean, server_default=text("0"), nullable=False, comment="Ví mặc định của user")
    locked_at = Column(DateTime, nullable=True, comment="Thời điểm khóa ví")
    locked_reason = Column(String(255), nullable=True, comment="Lý do khóa ví")
    archived_at = Column(DateTime, nullable=True, comment="Thời điểm archive ví")
    created_by = Column(String(36), nullable=True, comment="User tạo ví")
    updated_by = Column(String(36), nullable=True, comment="User cập nhật gần nhất")
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")
"""
    )



    create_file(
        "app/models/wallet/wallet_history.py",
        """
# D:\\UIT - HK2\\FinanceProject\\app\\models\\finance\\wallet_history.py

from sqlalchemy import Column, String, Numeric, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base


class WalletHistory(Base):
    __tablename__ = "wallet_histories"
    __table_args__ = {"comment": "Lưu lịch sử biến động số dư ví"}
    id = Column(String(36), primary_key=True, server_default=text("(UUID())"))
    wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="RESTRICT"), nullable=False, index=True)
    action_type = Column(String(50), nullable=False, comment="CREATE, UPDATE, TRANSFER, LOCK, DELETE")
    balance_before = Column(Numeric(18, 4), nullable=True)
    balance_after = Column(Numeric(18, 4), nullable=True)
    amount = Column(Numeric(18, 4), nullable=True)
    reference_id = Column(String(36), nullable=True, comment="Transaction/Transfer reference")
    note = Column(String(255), nullable=True)
    created_at = Column(DateTime, server_default=func.now(), nullable=False)
"""
    )



    create_file(
        "app/models/wallet/wallet_limit.py",
        """
# D:\\UIT - HK2\\FinanceProject\\app\\models\\finance\\wallet_limit.py

from sqlalchemy import Column, String, Numeric, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base


class WalletLimit(Base):
    __tablename__ = "wallet_limits"
    __table_args__ = {"comment": "Thiết lập hạn mức ví"}
    id = Column(String(36), primary_key=True, server_default=text("(UUID())"))
    wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="CASCADE"), nullable=False, unique=True)
    daily_limit = Column(Numeric(18, 4), nullable=True)
    monthly_limit = Column(Numeric(18, 4), nullable=True)
    transfer_limit = Column(Numeric(18, 4), nullable=True)
    created_at = Column(DateTime, server_default=func.now(), nullable=False)
"""
    )



    create_file(
        "app/models/wallet/wallet_notification_setting.py",
        """
# D:\\UIT - HK2\\FinanceProject\\app\\models\\finance\\wallet_notification_setting.py

from sqlalchemy import Column, String, Numeric, Boolean, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base


class WalletNotificationSetting(Base):
    __tablename__ = "wallet_notification_settings"
    __table_args__ = {"comment": "Cấu hình thông báo ví"}
    id = Column(String(36), primary_key=True, server_default=text("(UUID())"))
    wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="CASCADE"), nullable=False, unique=True)
    low_balance_enabled = Column(Boolean, server_default=text("1"), nullable=False)
    large_transaction_enabled = Column(Boolean, server_default=text("1"), nullable=False)
    low_balance_threshold = Column(Numeric(18, 4), nullable=True)
    created_at = Column(DateTime, server_default=func.now(), nullable=False)
"""
    )



    create_file(
        "app/models/wallet/wallet_tag.py",
        """
# D:\\UIT - HK2\\FinanceProject\\app\\models\\finance\\wallet_tag.py

from sqlalchemy import Column, String, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base


class WalletTag(Base):
    __tablename__ = "wallet_tags"
    __table_args__ = {"comment": "Tag phân loại ví"}
    id = Column(String(36), primary_key=True, server_default=text("(UUID())"))
    wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="CASCADE"), nullable=False, index=True)
    tag_name = Column(String(100), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), nullable=False)
"""
    )



    create_file(
        "app/models/wallet/wallet_transfer.py",
        """
# D:\\UIT - HK2\\FinanceProject\\app\\models\\finance\\wallet_transfer.py

from sqlalchemy import Column, String, Numeric, ForeignKey, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base


class WalletTransfer(Base):
    __tablename__ = "wallet_transfers"
    __table_args__ = {"comment": "Lưu giao dịch chuyển tiền giữa các ví"}
    id = Column(String(36), primary_key=True, server_default=text("(UUID())"))
    from_wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="RESTRICT"), nullable=False, index=True)
    to_wallet_id = Column(String(36), ForeignKey("wallets.id", ondelete="RESTRICT"), nullable=False, index=True)
    amount = Column(Numeric(18, 4), nullable=False)
    transfer_type = Column(String(50), nullable=True, comment="INTERNAL, EXTERNAL")
    status = Column(String(50), server_default=text("'SUCCESS'"), nullable=False, comment="PENDING, SUCCESS, FAILED")
    description = Column(String(255), nullable=True)
    created_by = Column(String(36), nullable=True)
    created_at = Column(DateTime, server_default=func.now(), nullable=False)
"""
    )



    create_file(
        "app/models/wallet/__init__.py",
        """

"""
    )



if __name__ == "__main__":
    main()
