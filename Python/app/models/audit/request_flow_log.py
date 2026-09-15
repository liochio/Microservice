from sqlalchemy import Column, String, Text, DateTime, text
from sqlalchemy.sql import func
from app.db.base import Base

class RequestFlowLog(Base):
    """
    👑 REQUEST FLOW LOGS ENTITY MODEL (CLASSIC STYLE)
    🎯 Viết giống hệt phong cách AuditLog hệ thống để hệ thống tự sinh bảng mượt mà.
    🔒 Tuyệt đối không đụng chạm hay làm ảnh hưởng tính năng cũ.
    """
    __tablename__ = "request_flow_logs"
    __table_args__ = {"comment": "Bảng kiểm toán lưu vết dòng chảy request tuần tự end-to-end"}

    id = Column(String(36), primary_key=True, index=True, server_default=text("(UUID())"), comment="Khóa chính UUID duy nhất")
    trace_id = Column(String(36), nullable=False, index=True, comment="Trace-ID liên kết end-to-end dòng chảy dữ liệu")
    node = Column(String(50), nullable=False, index=True, comment="Tên chặng xử lý trong vòng đời request")
    details = Column(Text, nullable=True, comment="Chi tiết dữ liệu hoặc vết xích xử lý tại node")
    created_at = Column(DateTime, server_default=func.now(), nullable=False, comment="Thời điểm tạo bản ghi")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), nullable=False, comment="Thời điểm cập nhật")