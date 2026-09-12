import sys
import unittest
import jwt
from pathlib import Path
from datetime import datetime, timezone, timedelta

# Add Python root to path
PROJECT_ROOT = Path(__file__).resolve().parent.parent
sys.path.append(str(PROJECT_ROOT))

from app.core.security.jwt.jwt_service import JwtService


class TestRS256TokenVerify(unittest.TestCase):

    def test_local_jwt_verify_with_rsa_key(self):
        # 1. Load private key from certs or SpringBoot resources for test signing
        private_key_path = PROJECT_ROOT.parent / "certs" / "rsa-private.pem"
        if not private_key_path.exists():
            private_key_path = PROJECT_ROOT.parent / "SpringBoot" / "auth-service" / "src" / "main" / "resources" / "certs" / "rsa-private.pem"
        self.assertTrue(private_key_path.exists(), "File rsa-private.pem must exist")

        private_key_pem = private_key_path.read_text(encoding="utf-8")

        # 2. Simulate Java IAM signing an RS256 token
        now_utc = datetime.now(timezone.utc)
        payload = {
            "sub": "usr_test_00000001",
            "username": "fintech_user",
            "email": "user@liochio.com",
            "roles": ["ROLE_USER"],
            "permissions": ["WALLET:READ", "WALLET:TOPUP", "GOAL:READ"],
            "modules": ["WALLET_MGMT", "GOAL_MGMT"],
            "token_type": "ACCESS",
            "iat": int(now_utc.timestamp()),
            "exp": int((now_utc + timedelta(minutes=30)).timestamp())
        }

        token = jwt.encode(payload, private_key_pem, algorithm="RS256", headers={"kid": "liochio-core-rsa-key-1"})
        self.assertIsNotNone(token)

        # 3. Use Python JwtService to decode and verify token
        decoded = JwtService.decode_token(token)
        self.assertEqual(decoded["sub"], "usr_test_00000001")
        self.assertEqual(decoded["username"], "fintech_user")
        self.assertIn("ROLE_USER", decoded["roles"])
        self.assertIn("WALLET:READ", decoded["permissions"])
        print("SUCCESS: Python Local RS256 Verification: PASSED (Verified claims without calling Java service)")


if __name__ == "__main__":
    unittest.main()
