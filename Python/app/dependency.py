# 📄 Đường dẫn file: app/dependency.py
from typing import Generator
from fastapi import Request
from sqlalchemy.orm import Session
from app.db.session import SessionLocal


def get_db(request: Request = None) -> Generator[Session, None, None]:
    """
    👑 DATABASE SESSION DEPENDENCY CHUẨN MỰC:
    🎯 Mục đích:
       - Cung cấp Database Session độc lập, an toàn luồng (thread-safe) cho từng Request.
       - Tự động nạp context trace_id, client_ip, user_agent vào metadata của session.
       - Gắn session vào request.state.db_conn để tương thích ngược với các service cũ.
       - Tự động đóng kết nối (close session) thu hồi về MySQL Pool sau khi hoàn thành.
    """
    db = SessionLocal()
    if request is not None:
        trace_id = getattr(request.state, "trace_id", "UNKNOWN")
        client_ip = getattr(request.state, "client_ip", "127.0.0.1")
        user_agent = getattr(request.state, "user_agent", "UNKNOWN")

        setattr(db, "_ctx_trace_id", trace_id)
        setattr(db, "_ctx_ip_address", client_ip)
        setattr(db, "_ctx_user_agent", user_agent)
        request.state.db_conn = db

    try:
        yield db
    finally:
        db.close()


# Alias tương thích ngược
get_db_conn = get_db