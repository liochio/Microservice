import json
import urllib.request
import hashlib
import time
from pathlib import Path
from typing import Optional, Dict, Any
from datetime import datetime, timezone, timedelta
import jwt
from jwt.algorithms import RSAAlgorithm
from cryptography.hazmat.primitives import serialization
import redis
from app.core.config.settings import settings

_redis_sync_client: Optional[Any] = None

# ==============================================================================
# 👑 L1 IN-MEMORY BLACKLIST & REVOCATION STORAGE (THREAD-SAFE FAST CACHE)
# ==============================================================================
_local_revoked_tokens: Dict[str, float] = {}       # token_hash -> expiry_epoch_sec
_local_revoked_sessions: Dict[str, float] = {}     # session_id -> expiry_epoch_sec
_local_revoked_users: Dict[str, int] = {}          # user_id -> revoked_epoch_ms
_redis_last_failed_ts: float = 0.0


def mark_redis_failed():
    global _redis_sync_client, _redis_last_failed_ts
    _redis_last_failed_ts = time.time()
    _redis_sync_client = None


def get_redis_sync_client():
    global _redis_sync_client, _redis_last_failed_ts
    now = time.time()
    if now - _redis_last_failed_ts < 30:
        return None
    if _redis_sync_client is None:
        try:
            r = redis.from_url(settings.REDIS_URL, socket_connect_timeout=0.1, socket_timeout=0.1)
            r.ping()
            _redis_sync_client = r
        except Exception:
            _redis_last_failed_ts = now
            _redis_sync_client = None
            return None
    return _redis_sync_client


def is_locally_revoked(token_hash: str, session_id: Optional[str], user_id: Optional[str], iat_ms: Optional[int]) -> bool:
    now_ts = datetime.now(timezone.utc).timestamp()

    # 1. Kiểm tra mã băm Token
    if token_hash in _local_revoked_tokens:
        if _local_revoked_tokens[token_hash] > now_ts:
            return True
        else:
            del _local_revoked_tokens[token_hash]

    # 2. Kiểm tra Session ID
    if session_id and session_id in _local_revoked_sessions:
        if _local_revoked_sessions[session_id] > now_ts:
            return True
        else:
            del _local_revoked_sessions[session_id]

    # 3. Kiểm tra Force Logout User ID
    if user_id and str(user_id) in _local_revoked_users:
        if iat_ms and iat_ms <= _local_revoked_users[str(user_id)]:
            return True

    return False


def add_revoked_token(token_hash: str, ttl_seconds: int = 7200):
    now_ts = datetime.now(timezone.utc).timestamp()
    _local_revoked_tokens[token_hash] = now_ts + ttl_seconds


def add_revoked_session(session_id: str, ttl_seconds: int = 604800):
    now_ts = datetime.now(timezone.utc).timestamp()
    _local_revoked_sessions[session_id] = now_ts + ttl_seconds


def add_revoked_user(user_id: str):
    now_ms = int(datetime.now(timezone.utc).timestamp() * 1000)
    _local_revoked_users[str(user_id)] = now_ms


def check_session_revoked_in_db(session_id: str) -> bool:
    """
    👑 L3 DATABASE FALLBACK:
    Đối soát trạng thái thu hồi của Session trong bảng user_sessions.
    Tự động truy vấn CSDL (liochio_core_db hoặc liochio_app_db) khi Redis không khả dụng.
    """
    if not session_id:
        return False
    try:
        import pymysql
        conn = pymysql.connect(
            host="127.0.0.1",
            port=3306,
            user="root",
            password="12345678",
            connect_timeout=1,
            read_timeout=1
        )
        with conn.cursor() as cur:
            # 1. Kiểm tra trong liochio_core_db
            cur.execute(
                "SELECT is_revoked FROM liochio_core_db.user_sessions WHERE id = %s LIMIT 1",
                (session_id,)
            )
            row = cur.fetchone()
            if row:
                conn.close()
                is_rev = row[0] in (1, True, b"\x01", "1")
                if is_rev:
                    add_revoked_session(session_id, 3600)
                return is_rev

            # 2. Kiểm tra fallback trong liochio_app_db
            cur.execute(
                "SELECT is_revoked FROM liochio_app_db.user_sessions WHERE id = %s LIMIT 1",
                (session_id,)
            )
            row = cur.fetchone()
            conn.close()
            if row:
                is_rev = row[0] in (1, True, b"\x01", "1")
                if is_rev:
                    add_revoked_session(session_id, 3600)
                return is_rev
    except Exception:
        pass
    return False


class JwtService:
    """
    👑 ENTERPRISE JWT VERIFIER & JWKS CLIENT
    Hỗ trợ linh hoạt cả token ký HS256 nội bộ lẫn token ký RS256 từ SpringBoot Auth Core JWKS.
    Bảo vệ 3 tầng thu hồi: L1 (In-Memory RAM) + L2 (Redis Distributed Cache) + L3 (Database Session).
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
    def register_logout(cls, token: str, session_id: Optional[str] = None, user_id: Optional[str] = None):
        """Đăng ký thu hồi Token, Session vào cả L1 In-Memory và Redis"""
        if not token:
            return
        token_hash = hashlib.sha256(token.encode("utf-8")).hexdigest()
        add_revoked_token(token_hash, 7200)

        if session_id:
            add_revoked_session(session_id, 604800)

        if user_id:
            add_revoked_user(str(user_id))

        r = get_redis_sync_client()
        if r:
            try:
                r.set(f"blacklist:token:{token_hash}", "LOGOUT", ex=7200)
                if session_id:
                    r.set(f"blacklist:session:{session_id}", "LOGOUT", ex=604800)
                if user_id:
                    now_ms = str(int(datetime.now(timezone.utc).timestamp() * 1000))
                    r.set(f"blacklist:user:{user_id}", now_ms, ex=604800)
            except Exception:
                mark_redis_failed()

    @classmethod
    def decode_token(cls, token: str) -> Dict[str, Any]:
        """Giải mã, xác thực token linh hoạt RS256 / HS256 và đối soát Blacklist 3 tầng"""
        if not token:
            raise jwt.PyJWTError("Token is empty")

        token_hash = hashlib.sha256(token.encode("utf-8")).hexdigest()

        # 1. Kiểm tra L1 In-Memory Revocation Cache (Token Hash)
        if is_locally_revoked(token_hash, None, None, None):
            raise jwt.PyJWTError("Token has been revoked or logged out (Local Blacklisted)")

        # 2. Kiểm tra L2 Redis Blacklist (nếu Redis khả dụng)
        r = get_redis_sync_client()
        if r:
            try:
                if r.exists(f"blacklist:token:{token_hash}"):
                    add_revoked_token(token_hash)
                    raise jwt.PyJWTError("Token has been revoked or logged out (Redis Blacklisted)")
            except jwt.PyJWTError:
                raise
            except Exception:
                mark_redis_failed()

        # 3. Nhận diện thuật toán từ Header Token
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

        # 4. Kiểm tra Session ID và User Force Logout Revocation (L1 Cache -> L2 Redis -> L3 Database)
        session_id = payload.get("sessionId")
        user_id = payload.get("userId") or payload.get("sub")
        iat_ms = payload.get("iat", 0) * 1000

        # 4a. L1 In-Memory Cache
        if is_locally_revoked(token_hash, session_id, user_id, iat_ms):
            raise jwt.PyJWTError("Session or user has been revoked (Local Blacklisted)")

        # 4b. L2 Redis Check
        if r:
            try:
                if session_id and r.exists(f"blacklist:session:{session_id}"):
                    add_revoked_session(session_id)
                    raise jwt.PyJWTError("Session has been revoked (Redis Blacklisted)")

                if user_id:
                    revoked_ts = r.get(f"blacklist:user:{user_id}")
                    if revoked_ts:
                        revoked_epoch_ms = int(revoked_ts.decode("utf-8") if isinstance(revoked_ts, bytes) else revoked_ts)
                        if iat_ms <= revoked_epoch_ms:
                            add_revoked_user(str(user_id))
                            raise jwt.PyJWTError("All user sessions have been force-revoked")
            except jwt.PyJWTError:
                raise
            except Exception:
                mark_redis_failed()

        # 4c. L3 Database Session Fallback (Đối soát MySQL user_sessions khi Redis offline)
        if session_id and check_session_revoked_in_db(session_id):
            raise jwt.PyJWTError("Session has been revoked or logged out (Database Verified)")

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
