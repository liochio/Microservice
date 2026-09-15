import random
import bcrypt
import jwt
from datetime import datetime, timedelta, timezone
from app.core.security.jwt.jwt_config import JWTConfig


class CryptoService:
    """
    👑 TẦNG MÃ HÓA VÀ AN NINH MẬT MÃ LÕI
    🎯 Giữ nguyên vẹn thuật toán Bcrypt tiêu chuẩn và bảo mật cao thêm cơ chế sinh chuỗi token JWT bảo mật cao.
    """

    @staticmethod
    def hash_password(password: str, rounds: int = 12) -> str:
        """🔒 Mã hóa một chiều chuẩn Bcrypt Premium với cấu hình số vòng băm linh hoạt"""
        password_bytes = password.encode('utf-8')
        salt = bcrypt.gensalt(rounds=rounds)
        hashed_password = bcrypt.hashpw(password_bytes, salt)
        return hashed_password.decode('utf-8')

    @staticmethod
    def verify_password(plain_password: str, hashed_password: str) -> bool:
        """🔑 Đối chiếu mật khẩu thô với chuỗi băm dưới DB"""
        try:
            return bcrypt.checkpw(plain_password.encode('utf-8'), hashed_password.encode('utf-8'))
        except (AttributeError, ValueError, TypeError):
            return False

    @staticmethod
    def generate_secure_otp() -> str:
        """🧮 Sinh mã số OTP bảo mật gồm 6 chữ số ngẫu nhiên"""
        return f"{random.randint(100000, 999999)}"

    @staticmethod
    def create_access_token(data: dict) -> str:
        """🛡️ Sản sinh Access Token (JWT) ngắn hạn cấu hình trực tiếp từ biến môi trường hệ thống (.env)"""
        # 👑 ĐÁNH DẤU CHỈNH SỬA: Ép kiểu và làm sạch dữ liệu đầu vào để chống nuốt dữ liệu khi mã hóa
        to_encode = {str(k): v for k, v in data.items()}
        expire = datetime.now(timezone.utc) + timedelta(minutes=JWTConfig.ACCESS_TOKEN_EXPIRE_MINUTES)
        to_encode.update({"exp": expire, "type": "access"})

        encoded_jwt = jwt.encode(to_encode, JWTConfig.JWT_SECRET_KEY, algorithm=JWTConfig.ALGORITHM)
        return str(encoded_jwt)

    @staticmethod
    def create_refresh_token(data: dict) -> str:
        """🛡️ Sản sinh Refresh Token (JWT) dài hạn phục vụ cơ chế xoay vòng phiên phiên"""
        # 👑 ĐÁNH DẤU CHỈNH SỬA: Ép kiểu đồng bộ an toàn dữ liệu payload
        to_encode = {str(k): v for k, v in data.items()}
        expire = datetime.now(timezone.utc) + timedelta(days=JWTConfig.REFRESH_TOKEN_EXPIRE_DAYS)
        to_encode.update({"exp": expire, "type": "refresh"})

        encoded_jwt = jwt.encode(to_encode, JWTConfig.JWT_REFRESH_SECRET_KEY, algorithm=JWTConfig.ALGORITHM)
        return str(encoded_jwt)