import os
import json
import logging
import time
from typing import Dict, Any, Optional
try:
    import requests
except ImportError:
    import urllib.request
    requests = None

logger = logging.getLogger("CoreApiClient")

class CoreApiClient:
    """
    Liochio Platform Core Client
    Giao tiep huong tam (Inward Calling) tu Python Vertical sang Spring Boot Core:
    - payment-service (:8085): Ghi nhan but toan ke toan kep so cai khi co coin drop IoT
    - notification-service (:8084): Gui SMS Telco, Email canh bao phan cung
    - auth-service (:8081): Kiem tra dinh danh va quyen han
    """
    def __init__(self):
        self.gateway_url = os.getenv("CORE_GATEWAY_URL", "http://localhost:8080")
        self.ledger_url = os.getenv("CORE_LEDGER_URL", "http://localhost:8085")
        self.payment_url = os.getenv("CORE_PAYMENT_URL", "http://localhost:8083")
        self.notif_url = os.getenv("CORE_NOTIF_URL", "http://localhost:8084")
        self.auth_url = os.getenv("CORE_AUTH_URL", "http://localhost:8081")
        self.service_token = os.getenv("CORE_S2S_TOKEN", "s2s_internal_python_key_2026")
        self.m2m_secret = os.getenv("M2M_SECRET_KEY", "liochio-fintech-m2m-secret-key-2026")

    def _headers(self, tenant_id: str = "default", auth_token: Optional[str] = None) -> Dict[str, str]:
        headers = {
            "Content-Type": "application/json",
            "X-Tenant-Id": tenant_id,
            "X-Service-Token": self.service_token
        }
        if auth_token:
            headers["Authorization"] = f"Bearer {auth_token}" if not auth_token.startswith("Bearer ") else auth_token
        return headers

    def sync_ledger_transaction(
        self,
        user_id: int,
        transaction_type: str,
        amount: float,
        idempotency_key: str,
        description: str,
        tenant_id: str = "default",
        reference_id: Optional[str] = None,
        target_user_id: Optional[int] = None
    ) -> Dict[str, Any]:
        """
        👑 MACHINE-TO-MACHINE (M2M) DUAL-ENTRY POSTING TO CORE LEDGER SERVICE (:8085)
        Có ký số HMAC-SHA256 chống giả mạo và bảo đảm tính bất biến tài chính.
        """
        import hmac
        import hashlib

        timestamp = str(int(time.time() * 1000))
        # Chuẩn payload ký số theo LedgerController.java: userId|transactionType|amount|idempotencyKey|timestamp
        amount_val = f"{amount:.2f}" if isinstance(amount, float) else str(amount)
        sign_payload = f"{user_id}|{transaction_type}|{amount_val}|{idempotency_key}|{timestamp}"
        signature = hmac.new(self.m2m_secret.encode('utf-8'), sign_payload.encode('utf-8'), hashlib.sha256).hexdigest()

        req_body = {
            "tenantId": tenant_id,
            "userId": user_id,
            "transactionType": transaction_type,
            "amount": amount,
            "idempotencyKey": idempotency_key,
            "description": description,
            "referenceId": reference_id,
            "targetUserId": target_user_id
        }

        headers = self._headers(tenant_id)
        headers["X-M2M-Signature"] = signature
        headers["X-M2M-Timestamp"] = timestamp

        url = f"{self.ledger_url}/api/v1/ledger/m2m/transaction"
        try:
            if requests:
                res = requests.post(url, json=req_body, headers=headers, timeout=5)
                if res.ok:
                    logger.info(f"[CoreApiClient] ✅ Đã hạch toán Sổ cái kép M2M thành công: {idempotency_key}")
                    return res.json()
                gw_url = f"{self.gateway_url}/api/v1/ledger/m2m/transaction"
                res_gw = requests.post(gw_url, json=req_body, headers=headers, timeout=5)
                if res_gw.ok:
                    logger.info(f"[CoreApiClient] ✅ Đã hạch toán qua Gateway M2M thành công: {idempotency_key}")
                    return res_gw.json()
                logger.warn(f"[CoreApiClient] ⚠️ Hạch toán Sổ cái M2M thất bại: HTTP {res.status_code} - {res.text}")
                return {"status": res.status_code, "message": res.text}
            else:
                import urllib.request
                req = urllib.request.Request(url, data=json.dumps(req_body).encode('utf-8'), headers=headers, method="POST")
                with urllib.request.urlopen(req, timeout=5) as resp:
                    return json.loads(resp.read().decode())
        except Exception as e:
            logger.error(f"[CoreApiClient] ❌ Lỗi kết nối tới ledger-service: {str(e)}")
            return {"status": 500, "error": str(e)}

    def record_iot_coin_drop(self, tenant_id: str, device_id: str, user_id: str, amount: float, auth_token: Optional[str] = None) -> Dict[str, Any]:
        """
        Ghi nhận tiền đút heo vào Sổ cái Kế toán kép (Double-entry Ledger) trên ledger-service (:8085)
        """
        numeric_user_id = 1
        try:
            numeric_user_id = int(''.join(filter(str.isdigit, str(user_id)))) if any(c.isdigit() for c in str(user_id)) else 1
        except Exception:
            numeric_user_id = 1

        idempotency_key = f"iot_{device_id}_{int(time.time() * 1000)}"
        return self.sync_ledger_transaction(
            user_id=numeric_user_id,
            transaction_type="TOPUP",
            amount=amount,
            idempotency_key=idempotency_key,
            description=f"Tiết kiệm tự động từ Heo Đất IoT (Device: {device_id})",
            tenant_id=tenant_id,
            reference_id=f"DEV_{device_id}"
        )

    def dispatch_security_alert(self, tenant_id: str, title: str, content: str, recipient: str) -> Dict[str, Any]:
        """
        Gui canh bao an ninh phan cung qua notification-service
        """
        url = f"{self.notif_url}/api/v1/notifications/send"
        payload = {
            "channel": "SMS",
            "recipient": recipient,
            "title": title,
            "content": content
        }
        try:
            res = requests.post(url, json=payload, headers=self._headers(tenant_id), timeout=5)
            return res.json() if res.ok else {"status": res.status_code, "message": res.text}
        except Exception as e:
            logger.error(f"[CoreApiClient] Loi ket noi Notification Service: {e}")
            return {"status": 500, "error": str(e)}

core_client = CoreApiClient()
