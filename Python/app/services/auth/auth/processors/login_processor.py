import re
from app.repositories.auth.auth_repository import AuthRepository
from app.repositories.module.module_repository import ModuleRepository
from app.services.common.crypto_service import CryptoService
from app.core.exceptions.base_exception import FintechBaseException
from app.services.auth.user_role.user_role_service import UserRoleService
from app.services.auth.role.role_service import RoleService
from app.services.auth.role_module.role_module_service import RoleModuleService
from app.services.auth.permission.permission_service import PermissionService
from app.services.auth.wallet.wallet_service import WalletService
from app.constants import SystemConstants
from app.core.security.jwt.jwt_service import JwtService
from app.services.auth.auth.token_service import TokenService
from app.repositories.log.log_repository import LogRepository


class UserLoginProcessor:
    @staticmethod
    def _fetch_all_modules_recursive(db_conn, user_id: str) -> list:
        return ModuleRepository.fetch_all_modules_recursive(db_conn, user_id)

    @staticmethod
    def _build_module_tree(flat_modules: list) -> list:
        mapping = {m["id"]: {**m, "children": []} for m in flat_modules}
        tree = []
        for m_id, node in mapping.items():
            p_id = node.get("parent_id")
            if p_id and p_id in mapping:
                mapping[p_id]["children"].append(node)
            else:
                tree.append(node)
        return tree

    @staticmethod
    def process(db_conn, payload) -> dict:
        trace_id = getattr(db_conn, "_ctx_trace_id", "UNKNOWN")
        client_ip = getattr(db_conn, "_ctx_ip_address", "127.0.0.1")

        # STEP 1: AUTH TIMELINE LINE-UP (Mục 8)
        LogRepository.insert_request_flow_log(trace_id, "VALIDATE_INPUT", f"Đang làm sạch email xử lý đầu vào.")

        clean_email = re.sub(r'[\s\x00-\x1f\x7f-\x9f]', '', str(payload.email).strip().lower())

        # STEP 2: CHECK USER DISCOVERY (Mục 8)
        LogRepository.insert_request_flow_log(trace_id, "CHECK_USER", f"Truy vết sự tồn tại thực thể: {clean_email}")
        user_data = AuthRepository.get_user_by_email(db_conn, clean_email)

        if not user_data:
            # 🚨 ANTI FORENSIC ALERT: Ghi nhận cố tình rò quét email hệ thống (Mục 4)
            LogRepository.insert_security_log(db_conn, "ANONYMOUS", "BRUTE_FORCE_RECON", "HIGH",
                                              f"Cố ý dò quét đăng nhập bằng email không tồn tại: {clean_email}",
                                              client_ip)
            raise FintechBaseException(error_code=SystemConstants.USER_NOT_FOUND, status_code=401)

        hashed_password = getattr(user_data, "password_hash", None) or user_data.get("password_hash")
        user_id = str(getattr(user_data, "id", None) or user_data.get("id"))
        username = getattr(user_data, "username", None) or user_data.get("username")
        status_user = getattr(user_data, "status", None) or user_data.get("status")

        # STEP 3: VERIFY PASSWORD CRYPTO (Mục 8)
        LogRepository.insert_request_flow_log(trace_id, "VERIFY_PASSWORD", f"Tiến hành so khớp chuỗi Bcrypt mật mã.")
        if not CryptoService.verify_password(payload.password, hashed_password):
            # 🚨 FORENSIC SECURITY EVENT: Nhập sai mật khẩu (Mục 4)
            LogRepository.insert_security_log(db_conn, user_id, "SUSPICIOUS_PASSWORD_RETRY", "MEDIUM",
                                              f"Tài khoản {username} nhập sai mật mã xác thực.", client_ip)
            raise FintechBaseException(error_code=SystemConstants.INVALID_CREDENTIALS, status_code=401)

        if str(status_user).upper() == SystemConstants.BLOCKED:
            LogRepository.insert_security_log(db_conn, user_id, "BLOCKED_ACCOUNT_ACCESS", "HIGH",
                                              f"Tài khoản bị khóa cố tình thâm nhập.", client_ip)
            raise FintechBaseException(error_code=SystemConstants.USER_ACCOUNT_BLOCKED, status_code=403)

        if str(status_user).upper() in ["PENDING", "OTP_PENDING", "INACTIVE"]:
            LogRepository.insert_security_log(db_conn, user_id, "UNVERIFIED_ACCOUNT_LOGIN_ATTEMPT", "LOW",
                                              f"Tài khoản chưa kích hoạt ({status_user}) cố gắng đăng nhập.", client_ip)
            raise FintechBaseException(error_code=SystemConstants.ACCOUNT_PENDING_ACTIVATION, status_code=403)

        # STEP 4: LOAD ROLE & PERMISSION MA TRẬN (Mục 8)
        LogRepository.insert_request_flow_log(trace_id, "LOAD_ROLE",
                                              f"Triệu hồi ma trận quyền hành của user_id: {user_id}")
        flat_modules_data = UserLoginProcessor._fetch_all_modules_recursive(db_conn, user_id)
        permissions_data, perms_token = PermissionService.get_permissions_and_token_list(db_conn, user_id)
        user_module_codes = [str(m["code"]) for m in flat_modules_data]

        # STEP 5: GENERATE TOKEN PAIR (Mục 8)
        LogRepository.insert_request_flow_log(trace_id, "GENERATE_TOKEN", f"Đúc cặp bài trùng Token JTI bảo mật cao.")
        token_pair = JwtService.generate_token_pair(user_id, username, perms_token, user_module_codes)

        # STEP 6: SAVE SESSION LOCK TO SQL (Mục 8, 14)
        LogRepository.insert_request_flow_log(trace_id, "SAVE_SESSION", f"Khóa xích JTI Session xuống Database cứng.")
        device = getattr(db_conn, "_ctx_user_agent", "UNKNOWN")
        TokenService.register_session(db_conn, user_id, token_pair["refresh_token"], token_pair["refresh_jti"], device,
                                      client_ip, token_pair["expires_at_dt"])

        return {
            "user_id": user_id, "username": username,
            "access_token": token_pair["access_token"], "refresh_token": token_pair["refresh_token"],
            "token_type": "Bearer",
            "user": {"user_id": user_id, "username": username, "email": clean_email},
            "user_roles": UserRoleService.get_user_roles_by_user(db_conn, user_id),
            "roles": RoleService.get_roles_by_user(db_conn, user_id),
            "role_modules": RoleModuleService.get_role_modules_by_user(db_conn, user_id),
            "modules": UserLoginProcessor._build_module_tree(flat_modules_data),
            "permissions": permissions_data, "wallets": WalletService.get_wallets_by_user(db_conn, user_id)
        }