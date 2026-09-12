import os


class JWTConfig:
    """
    👑 MA TRẬN CẤU HÌNH BẢO MẬT JWT ENTERPRISE (ĐỒNG BỘ ĐÉT THEO .ENV CỦA SẾP)
    🎯 Nạp nghiêm ngặt các tham số mật mã lõi từ biến môi trường hệ thống của sếp.
    """
    # 🛡️ Ánh xạ chính xác theo key sếp đặt trong .env: SECRET_KEY
    JWT_SECRET_KEY = os.getenv("SECRET_KEY", "09d25e094faa6ca2556c818166b7a9563b93f7099f6f0f4caa6cf63b88e8d3e7")

    # FinTech bọc thép: Nếu sếp không cấu hình key refresh riêng, hệ thống tự động fallback dùng chung SECRET_KEY để ký mã hóa
    JWT_REFRESH_SECRET_KEY = os.getenv("JWT_REFRESH_SECRET_KEY", JWT_SECRET_KEY)

    # Ánh xạ thuật toán mã hóa từ .env
    ALGORITHM = os.getenv("ALGORITHM", "HS256")

    # Ép kiểu dữ liệu về số nguyên (int) để tính toán thời gian hết hạn chính xác trên RAM
    ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", 30))
    REFRESH_TOKEN_EXPIRE_DAYS = int(os.getenv("REFRESH_TOKEN_EXPIRE_DAYS", 7))