# Database Engine & Session Management Package

## Chức năng
Gói pp/db cấu hình và quản lý kết nối cơ sở dữ liệu MySQL liochio_fintech_db:
1. session.py: Khởi tạo SQLAlchemy Engine với HikariCP-like connection pool (pool_size=20, max_overflow=10, pool_recycle=3600).
2. ase.py: Khai báo Base Declarative Meta model cho toàn bộ Entity trong pp/models.
3. Hỗ trợ Session Scoped Dependency Injection cho FastAPI routes qua get_db().