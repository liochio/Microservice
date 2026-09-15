
# 👑 IDENTITY & ACCESS MANAGEMENT ROUTER - PROXIED TO JAVA CORE IAM (:8081 / :8080)
# Bảo đảm 100% Zero Regression cho các client gọi trực tiếp qua Python port 8000

from fastapi import APIRouter, Request, Response, status
from fastapi.responses import JSONResponse
import httpx
import logging

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/auth", tags=["Authentication (Proxied to Core IAM)"])

GATEWAY_IAM_URL = "http://localhost:8080/api/v1/auth"
CORE_IAM_URL = "http://localhost:8081/api/v1/auth"


async def proxy_to_iam(request: Request, path: str):
    """Chuyển tiếp yêu cầu xác thực sang Java IAM Service để thống nhất Single Source of Truth."""
    url = f"{CORE_IAM_URL}/{path}"
    headers = {k: v for k, v in request.headers.items() if k.lower() not in ("host", "content-length")}
    params = dict(request.query_params)
    body = await request.body()

    try:
        async with httpx.AsyncClient(timeout=10.0) as client:
            resp = await client.request(
                method=request.method,
                url=url,
                headers=headers,
                params=params,
                content=body
            )
            return Response(
                content=resp.content,
                status_code=resp.status_code,
                headers=dict(resp.headers),
                media_type=resp.headers.get("content-type", "application/json")
            )
    except Exception as e:
        logger.error(f"[IAM_PROXY_ERROR] Không thể kết nối tới Core IAM Service: {str(e)}")
        # Fallback qua Gateway
        try:
            gw_url = f"{GATEWAY_IAM_URL}/{path}"
            async with httpx.AsyncClient(timeout=10.0) as client:
                resp = await client.request(
                    method=request.method,
                    url=gw_url,
                    headers=headers,
                    params=params,
                    content=body
                )
                return Response(
                    content=resp.content,
                    status_code=resp.status_code,
                    headers=dict(resp.headers),
                    media_type=resp.headers.get("content-type", "application/json")
                )
        except Exception as gw_err:
            logger.error(f"[GATEWAY_PROXY_ERROR] Fallback gateway thất bại: {str(gw_err)}")
            return JSONResponse(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                content={"code": "IAM_SERVICE_UNAVAILABLE", "message": "Dịch vụ xác thực Core IAM hiện không khả dụng"}
            )


@router.post("/register")
async def register_account(request: Request):
    return await proxy_to_iam(request, "register")


@router.post("/login")
async def login_account(request: Request):
    return await proxy_to_iam(request, "login")


@router.post("/refresh")
@router.post("/token/refresh")
async def refresh_token(request: Request):
    return await proxy_to_iam(request, "token/refresh")


@router.post("/logout")
async def logout_account(request: Request):
    return await proxy_to_iam(request, "logout")


@router.post("/otp/verify")
async def verify_otp(request: Request):
    return await proxy_to_iam(request, "otp/verify")


@router.post("/otp/resend")
async def resend_otp(request: Request):
    return await proxy_to_iam(request, "otp/resend")


@router.post("/password/forgot")
async def forgot_password(request: Request):
    return await proxy_to_iam(request, "password/forgot")


@router.post("/password/reset")
async def reset_password(request: Request):
    return await proxy_to_iam(request, "password/reset")


@router.get("/me")
async def get_current_user_profile(request: Request):
    return await proxy_to_iam(request, "me")
