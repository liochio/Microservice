import json
import urllib.request
import hashlib
from pathlib import Path
from typing import Optional, Dict, Any
from datetime import datetime, timezone, timedelta
import jwt
from jwt.algorithms import RSAAlgorithm
from cryptography.hazmat.primitives import serialization
import redis
from app.core.config.settings import settings

_redis_sync_client: Optional[Any] = None

def get_redis_sync_client():
    global _redis_sync_client
    if _redis_sync_client is None:
        try:
            _redis_sync_client = redis.from_url(settings.REDIS_URL, socket_timeout=1)
        except Exception:
            _redis_sync_client = False
    return _redis_sync_client if _redis_sync_client is not False else None


class JwtService:
    """
    👑 ENTERPRISE JWT VERIFIER & JWKS CLIENT
    Hỗ trợ linh hoạt cả token ký HS256 nội bộ lẫn token ký RS256 từ SpringBoot Auth Core JWKS.
    """

    _cached_public_key: Optional[Any] = None

    @classmethod
    def get_public_key(cls):
        if cls._cached_public_key is not None:
            return cls._cached_public_key

        # 1. Thử tải từ JWKS URL của liochio-core
        try:
            req = urllib.request.Request(
                settings.AUTH_CORE_JWKS_URL,
                headers={"User-Agent": "Liochio-FinTech-ResourceServer/1.0"}
            )
            with urllib.request.urlopen(req, timeout=2) as resp:
                jwks_data = json.loads(resp.read().decode("utf-8"))
                keys_list = jwks_data.get("keys")
                if not keys_list and "data" in jwks_data and isinstance(jwks_data["data"], dict):
                    keys_list = jwks_data["data"].get("keys")
                if keys_list and len(keys_list) > 0:
                    key_dict = keys_list[0]
                    cls._cached_public_key = RSAAlgorithm.from_jwk(json.dumps(key_dict))
                    return cls._cached_public_key
        except Exception:
            pass

        # 2. Fallback đọc file rsa-public.pem từ thư mục certs
        possible_pem_paths = [
            Path(__file__).resolve().parents[3] / "certs" / "rsa-public.pem",
            Path(__file__).resolve().parents[4] / "certs" / "rsa-public.pem",
            Path(__file__).resolve().parents[5] / "certs" / "rsa-public.pem",
        ]
        for p in possible_pem_paths:
            if p.exists():
                try:
                    cls._cached_public_key = serialization.load_pem_public_key(p.read_bytes())
                    return cls._cached_public_key
                except Exception:
                    pass

        return None

    @classmethod
    def decode_token(cls, token: str) -> Dict[str, Any]:
        """Giải mã, xác thực token linh hoạt RS256 / HS256 và đối soát Redis Blacklist"""
        if not token:
            raise jwt.PyJWTError("Token is empty")

        # 1. Kiểm tra Token Blacklist SHA-256
        token_hash = hashlib.sha256(token.encode("utf-8")).hexdigest()
        r = get_redis_sync_client()
        if r:
            try:
                if r.exists(f"blacklist:token:{token_hash}"):
                    raise jwt.PyJWTError("Token has been revoked or logged out (Blacklisted)")
            except jwt.PyJWTError:
                raise
            except Exception:
                pass

        # 2. Nhận diện thuật toán từ Header Token
        try:
            unverified_header = jwt.get_unverified_header(token)
            token_alg = unverified_header.get("alg", "HS256")
        except Exception:
            token_alg = "HS256"

        if token_alg == "RS256":
            public_key = cls.get_public_key()
            if public_key is not None:
                payload = jwt.decode(
                    token,
                    public_key,
                    algorithms=["RS256"],
                    options={"verify_exp": True, "verify_iat": False, "leeway": 60}
                )
            else:
                payload = jwt.decode(
                    token,
                    settings.SECRET_KEY,
                    algorithms=["HS256", "RS256"],
                    options={"verify_exp": True, "verify_iat": False, "leeway": 60}
                )
        else:
            payload = jwt.decode(
                token,
                settings.SECRET_KEY,
                algorithms=["HS256", "HS384", "HS512", "RS256"],
                options={"verify_exp": True, "verify_iat": False, "leeway": 60}
            )

        # 3. Kiểm tra Session ID và User Force Logout Revocation
        if r:
            try:
                session_id = payload.get("sessionId")
                if session_id and r.exists(f"blacklist:session:{session_id}"):
                    raise jwt.PyJWTError("Session has been revoked (Blacklisted)")

                user_id = payload.get("userId") or payload.get("sub")
                if user_id:
                    revoked_ts = r.get(f"blacklist:user:{user_id}")
                    if revoked_ts:
                        revoked_epoch_ms = int(revoked_ts.decode("utf-8") if isinstance(revoked_ts, bytes) else revoked_ts)
                        iat = payload.get("iat", 0) * 1000
                        if iat <= revoked_epoch_ms:
                            raise jwt.PyJWTError("All user sessions have been force-revoked")
            except jwt.PyJWTError:
                raise
            except Exception:
                pass

        return payload

    @classmethod
    def generate_token_pair(cls, user_id: str, username: str, permissions: list = None, modules: list = None) -> Dict[str, Any]:
        """Sinh cặp Access Token và Refresh Token chuẩn JWT có Timezone UTC chuẩn"""
        import uuid

        now = datetime.now(timezone.utc)
        access_exp = now + timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
        refresh_exp = now + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS)

        access_jti = str(uuid.uuid4())
        refresh_jti = str(uuid.uuid4())

        access_payload = {
            "sub": str(user_id),
            "userId": str(user_id),
            "username": username,
            "permissions": permissions or [],
            "modules": modules or [],
            "jti": access_jti,
            "type": "ACCESS",
            "iat": int(now.timestamp()),
            "exp": int(access_exp.timestamp()),
        }

        refresh_payload = {
            "sub": str(user_id),
            "userId": str(user_id),
            "username": username,
            "jti": refresh_jti,
            "type": "REFRESH",
            "iat": int(now.timestamp()),
            "exp": int(refresh_exp.timestamp()),
        }

        access_token = jwt.encode(access_payload, settings.SECRET_KEY, algorithm="HS256")
        refresh_token = jwt.encode(refresh_payload, settings.SECRET_KEY, algorithm="HS256")

        return {
            "access_token": access_token,
            "refresh_token": refresh_token,
            "access_jti": access_jti,
            "refresh_jti": refresh_jti,
            "expires_at_dt": refresh_exp,
            "expires_in": settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
        }
