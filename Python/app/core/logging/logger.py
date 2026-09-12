import json
import logging
import os
import re
import sys
from datetime import datetime

from pathlib import Path

# 👑 ĐỊNH VỊ THƯ MỤC GHI LOG TỰ ĐỘNG (Tương thích mọi môi trường)
# Mục đích: Đảm bảo thư mục logs/ luôn nằm tại root project, tự động tạo nếu chưa có
BASE_DIR = Path(__file__).resolve().parent.parent.parent.parent
LOG_DIR = BASE_DIR / "logs"
os.makedirs(LOG_DIR, exist_ok=True)
LOG_FILE_PATH = str(LOG_DIR / "app.log")

# Thiết lập Logger hệ thống chuẩn Enterprise JSON
logger = logging.getLogger("fintech_core")
logger.setLevel(logging.DEBUG)

if not logger.handlers:
    # Handler ghi file app.log
    file_handler = logging.FileHandler(LOG_FILE_PATH, encoding="utf-8")
    file_handler.setLevel(logging.INFO)
    logger.addHandler(file_handler)

    # Handler đẩy ra Stream Terminal Console
    console_handler = logging.StreamHandler(sys.stdout)
    console_handler.setLevel(logging.DEBUG)
    logger.addHandler(console_handler)

# Ma trận các từ khóa nhạy cảm cần áp mặt nạ bảo mật
MASK_PATTERNS = {
    "password": r"'password':\s*'[^']+'|\"password\":\s*\"[^\"]+\"",
    "confirm_password": r"'confirm_password':\s*'[^']+'|\"confirm_password\":\s*\"[^\"]+\"",
    "access_token": r"'access_token':\s*'[^']+'|\"access_token\":\s*\"[^\"]+\"",
    "refresh_token": r"'refresh_token':\s*'[^']+'|\"refresh_token\":\s*\"[^\"]+\"",
    "otp_code": r"'otp_code':\s*'[^']+'|\"otp_code\":\s*\"[^\"]+\""
}


class DBLogger:
    """
    👑 ENGINE HỘP ĐEN VẠN NĂNG - SẢN XUẤT STRUCTURED JSON LINE
    🎯 Đạt chuẩn 100% mã lỗi i18n, liên thông ma trận forensic bọc thép.
    """

    @staticmethod
    def mask_sensitive_data(message: str) -> str:
        """🛡️ KÍNH LỌC AN NINH: Che giấu thông tin nhạy cảm (Mục 10)"""
        if not message:
            return ""
        masked_msg = message
        for key, pattern in MASK_PATTERNS.items():
            masked_msg = re.sub(pattern, f'"{key}": "******"', masked_msg)
        return masked_msg

    @classmethod
    def emit_json_log(cls, trace_id: str, event: str, user_id: str, status: str,
                      latency_ms: int = 0, module: str = "CORE", severity: str = "INFO",
                      details: any = None, exception: str = None) -> None:
        log_entry = {
            "timestamp": datetime.now().isoformat(),
            "trace_id": trace_id or "SYSTEM_PROCESS",
            "event": event.upper(),
            "user_id": user_id or "ANONYMOUS",
            "status": status.upper(),
            "latency_ms": latency_ms,
            "module": module.upper(),
            "severity": severity.upper(),
            "details": details if isinstance(details, (dict, list)) else str(details or ""),
            "exception": cls.mask_sensitive_data(exception) if exception else None
        }

        raw_line = json.dumps(log_entry, ensure_ascii=False)
        masked_line = cls.mask_sensitive_data(raw_line)

        if severity.upper() in ["ERROR", "CRITICAL"]:
            logger.error(masked_line)
        elif severity.upper() == "WARNING":
            logger.warning(masked_line)
        else:
            logger.info(masked_line)

    @classmethod
    def audit(cls, db_conn, user_id: str, action: str, details: str) -> None:
        """📝 Tương thích ngược logic cũ của sếp - Tự động đổ luồng ra JSON line"""
        from app.repositories.log.log_repository import LogRepository
        trace_id = getattr(db_conn, "_ctx_trace_id", "UNKNOWN")
        cls.emit_json_log(trace_id, action, user_id, "SUCCESS", module="AUDIT", details=details)
        LogRepository.insert_audit_log(db_conn, user_id, action, details)

    @classmethod
    def system(cls, db_conn, log_level: str, module: str, message: str) -> None:
        """📝 Tương thích ngược luồng Crash cũ của sếp"""
        from app.repositories.log.log_repository import LogRepository
        trace_id = getattr(db_conn, "_ctx_trace_id", "UNKNOWN")
        cls.emit_json_log(trace_id, "SYSTEM_EVENT", "SYSTEM", log_level, module=module, severity=log_level,
                          details=message)
        LogRepository.insert_system_log(db_conn, log_level, module, message)

    @classmethod
    def security(cls, db_conn, user_id: str, event_type: str, severity: str, ip_address: str, details: str) -> None:
        """📝 Tương thích ngược luồng an ninh cũ"""
        from app.repositories.log.log_repository import LogRepository
        trace_id = getattr(db_conn, "_ctx_trace_id", "UNKNOWN")
        cls.emit_json_log(trace_id, event_type, user_id, "TRIGGERED", module="SECURITY", severity=severity,
                          details=f"IP: {ip_address} | {details}")
        LogRepository.insert_security_log(db_conn, user_id, event_type, severity, details, ip_address)

    @classmethod
    def execute_repo_log(cls, db_conn, repo_class, method_name: str, console_msg: str, **kwargs) -> None:
        """🎯 Khớp signature hàm Reflection cũ trong API của sếp"""
        trace_id = getattr(db_conn, "_ctx_trace_id", "UNKNOWN")
        cls.emit_json_log(trace_id, "REPO_REFLECTION", kwargs.get("user_id", "UNKNOWN"), "EXECUTE",
                          module=str(repo_class.__name__), details=console_msg)
        try:
            if hasattr(repo_class, method_name):
                func = getattr(repo_class, method_name)
                func(db_conn, **kwargs)
        except Exception as e:
            cls.emit_json_log(trace_id, "REPO_REFLECTION_CRASH", "SYSTEM", "ERROR", severity="ERROR", exception=str(e))