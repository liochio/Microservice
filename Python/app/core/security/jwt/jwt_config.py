import os


class JWTConfig:
    """
    Cấu hình bảo mật JWT (JSON Web Token) cho phân hệ Satellite Gateway.
    Nạp các tham số mật mã từ biến môi trường hệ thống (.env).
    """
    # Khóa bí mật ký mã hóa JWT
    JWT_SECRET_KEY = os.getenv("SECRET_KEY", "09d25e094faa6ca2556c818166b7a9563b93f7099f6f0f4caa6cf63b88e8d3e7")

    # Khóa bí mật ký mã hóa Refresh Token (nếu không cấu hình riêng sẽ fallback về JWT_SECRET_KEY)
    JWT_REFRESH_SECRET_KEY = os.getenv("JWT_REFRESH_SECRET_KEY", JWT_SECRET_KEY)

    # Ánh xạ thuật toán mã hóa từ .env
    ALGORITHM = os.getenv("ALGORITHM", "HS256")

    # Ép kiểu dữ liệu về số nguyên (int) để tính toán thời gian hết hạn chính xác trên RAM
    ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", 30))
    REFRESH_TOKEN_EXPIRE_DAYS = int(os.getenv("REFRESH_TOKEN_EXPIRE_DAYS", 7))