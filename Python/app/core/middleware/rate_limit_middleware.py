

import time
import redis.asyncio as aioredis
from fastapi import Request, status
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware
from app.core.config.settings import settings


class RedisRateLimitMiddleware(BaseHTTPMiddleware):
    """
    👑 HIGH-PERFORMANCE REDIS RATE LIMIT MIDDLEWARE
    🎯 Chặn đứng spam API Login/OTP trực tiếp tại cửa ngõ Middleware bằng RAM cache siêu tốc.
    """

    async def dispatch(self, request: Request, call_next):
        client_ip = getattr(request.state, "client_ip", "127.0.0.1")
        endpoint = str(request.url.path)

        if any(path in endpoint for path in ["/auth/login", "/auth/verify-otp", "/activate", "/auth/register"]):
            current_ts = int(time.time())
            redis_key = f"rate:{client_ip}:{endpoint}:{current_ts}"

            try:
                # Cải thiện: Dùng Redis Async (aioredis) và Async Context Manager chống đứng I/O Loop
                redis_client = aioredis.from_url(settings.REDIS_URL, socket_timeout=2)

                async with redis_client as r:
                    current_hits = await r.incr(redis_key)
                    if current_hits == 1:
                        await r.expire(redis_key, 2)

                    if current_hits > 5:
                        from app.constants import SystemConstants
                        from app.core.translator.translator_engine import i18n_translator
                        message = i18n_translator.translate(
                            request,
                            error_code=SystemConstants.GLOBAL_TOO_MANY_REQUESTS,
                            msg_type=SystemConstants.MSG_TYPE_ERROR
                        )
                        return JSONResponse(
                            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                            content={
                                "success": False,
                                "error_code": SystemConstants.GLOBAL_TOO_MANY_REQUESTS,
                                "message": message,
                                "context": {"ip": client_ip}
                            }
                        )
            except aioredis.RedisError as redis_err:
                print(f"[RATE_LIMIT_REDIS_ERROR] Bypass Rate Limit do sự cố Redis: {str(redis_err)}")

        return await call_next(request)