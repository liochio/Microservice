import os
import requests
import logging
import time
from typing import Dict, Any, Optional

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
        self.payment_url = os.getenv("CORE_PAYMENT_URL", "http://localhost:8085")
        self.notif_url = os.getenv("CORE_NOTIF_URL", "http://localhost:8084")
        self.auth_url = os.getenv("CORE_AUTH_URL", "http://localhost:8081")
        self.service_token = os.getenv("CORE_S2S_TOKEN", "s2s_internal_python_key_2026")

    def _headers(self, tenant_id: str = "default", auth_token: Optional[str] = None) -> Dict[str, str]:
        headers = {
            "Content-Type": "application/json",
            "X-Tenant-Id": tenant_id,
            "X-Service-Token": self.service_token
        }
        if auth_token:
            headers["Authorization"] = f"Bearer {auth_token}" if not auth_token.startswith("Bearer ") else auth_token
        return headers

    def record_iot_coin_drop(self, tenant_id: str, device_id: str, user_id: str, amount: float, auth_token: Optional[str] = None) -> Dict[str, Any]:
        """
        Ghi nhan tien dut heo vao So cai Ke toan kep (Double-entry Ledger) tren payment-service
        """
        url = f"{self.payment_url}/api/v1/ledger/entries"
        payload = {
            "entryType": "IOT_PIGGY_COIN_DROP",
            "referenceId": f"iot_{device_id}_{int(time.time() * 1000)}",
            "description": f"Tiet kiem tu dong tu Heo Dat IoT (Device: {device_id})",
            "amount": amount,
            "details": [
                {
                    "accountCode": "1111",
                    "debitAmount": amount,
                    "creditAmount": 0.0,
                    "note": f"Cong tien heo dat cho User {user_id}"
                },
                {
                    "accountCode": "3388",
                    "debitAmount": 0.0,
                    "creditAmount": amount,
                    "note": "Ghi nhan tang no vi khach hang"
                }
            ]
        }
        try:
            res = requests.post(url, json=payload, headers=self._headers(tenant_id, auth_token), timeout=5)
            logger.info(f"[CoreApiClient] Posted IoT Coin Drop to Core Ledger -> HTTP {res.status_code}")
            return res.json() if res.ok else {"status": res.status_code, "message": res.text}
        except Exception as e:
            logger.error(f"[CoreApiClient] Loi ket noi Core Payment Service: {e}")
            return {"status": 500, "error": str(e)}

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
