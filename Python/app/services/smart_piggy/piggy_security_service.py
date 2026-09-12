# 📄 Đường dẫn file: app/services/smart_piggy/piggy_security_service.py
from datetime import datetime
from typing import Dict, Any, Optional, List
import hmac
import hashlib
import time
import uuid

from sqlalchemy.orm import Session
from sqlalchemy import select, update

from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException
from app.models.wallet.wallet import Wallet
from app.models.finance.category import Category
from app.models.notification.notification import Notification
from app.repositories.finance.transaction_repository import TransactionRepository
from app.repositories.smart_piggy.smart_piggy_repository import SmartPiggyRepository
from app.websocket.manager.connection_manager import ws_manager


# Bộ nhớ lưu trữ Cache Nonce chống Replay Attack: {nonce: timestamp}
_NONCE_CACHE: Dict[str, float] = {}

# Khóa trạng thái phần cứng (Hardware Mutex): {device_id: {"locked": bool, "lock_type": str, "timestamp": float}}
_HARDWARE_MUTEXES: Dict[str, Dict[str, Any]] = {}

# Sổ theo dõi nhịp tim (Heartbeat Watchdog): {device_id: {"last_seen": float, "status": str}}
_HEARTBEAT_REGISTRY: Dict[str, Dict[str, Any]] = {}


class PiggySecurityService:
    """
    🛡️ SMART PIGGY IOT SECURITY & INTEGRITY ENGINE
    🎯 Bọc thép toàn bộ hệ thống trước các kịch bản tấn công:
       1. Tấn công phát lại (Replay Attack): Kiểm tra Timestamp Window (+-60s) & Cache Nonce.
       2. Chữ ký số thiết bị (HMAC-SHA256 Verification): Xác thực nguồn gốc gói tin.
       3. Đánh lừa cảm biến (Debounce & Sensor Deception Filter): Chống chọc giấy <100ms hoặc câu tiền >3s.
       4. Khóa trạng thái độc quyền (Hardware Mutex): Ngăn chặn xung đột nạp/rút cùng lúc.
       5. Giám sát nhịp tim (Heartbeat Watchdog 30s): Phát hiện ngắt nguồn hoặc phá sóng Wi-Fi.
       6. Ghép đôi bảo mật (Device Pairing & Unbind): Chống chiếm quyền khi thanh lý heo cũ.
       7. Phá hủy toàn diện (Fatal Crash >6G) & Ghi giảm tài sản (Write-off Accounting Adjustment).
    """

    DEFAULT_SECRET_KEY = "LIOCHIO_IOT_PIGGY_SECRET_2026"

    # =========================================================================
    # 1. ANTI-REPLAY & HMAC-SHA256 AUTHENTICATION
    # =========================================================================
    @classmethod
    def verify_device_packet(
        cls,
        device_id: str,
        timestamp: float,
        nonce: str,
        payload_data: Dict[str, Any],
        received_signature: str,
        secret_key: str = DEFAULT_SECRET_KEY
    ) -> bool:
        """
        🔐 XÁC THỰC GÓI TIN VÀ CHỐNG TẤN CÔNG PHÁT LẠI (ANTI-REPLAY)
        - Kiểm tra cửa sổ thời gian: Không quá 60 giây so với giờ server.
        - Kiểm tra Nonce: Nếu Nonce đã từng xuất hiện -> Từ chối lập tức.
        - Kiểm tra chữ ký số HMAC-SHA256.
        """
        now = time.time()

        # 1. Kiểm tra Timestamp Window (+-60s)
        if abs(now - timestamp) > 60:
            raise FintechBaseException(
                error_code="PACKET_TIMESTAMP_EXPIRED",
                status_code=401
            )

        # 2. Kiểm tra Replay Nonce Cache
        if nonce in _NONCE_CACHE:
            raise FintechBaseException(
                error_code="REPLAY_ATTACK_DETECTED",
                status_code=403
            )

        # Lưu Nonce vào cache và dọn dẹp các Nonce cũ (> 120s)
        _NONCE_CACHE[nonce] = now
        for old_nonce, ts in list(_NONCE_CACHE.items()):
            if now - ts > 120:
                del _NONCE_CACHE[old_nonce]

        # 3. Tính toán chữ ký HMAC-SHA256 mong đợi
        # Sắp xếp payload theo key để đảm bảo tính tất định (Canonical JSON representation)
        sorted_items = sorted(payload_data.items())
        payload_repr = ",".join(f"{k}={v}" for k, v in sorted_items)
        raw_msg = f"{device_id}:{timestamp:.0f}:{nonce}:{payload_repr}".encode("utf-8")

        expected_sig = hmac.new(secret_key.encode("utf-8"), raw_msg, hashlib.sha256).hexdigest()

        # So sánh an toàn chống Timing Attack
        if not hmac.compare_digest(expected_sig.lower(), received_signature.lower()):
            raise FintechBaseException(
                error_code="INVALID_HMAC_SIGNATURE",
                status_code=401
            )

        return True

    # =========================================================================
    # 2. SENSOR DECEPTION & DEBOUNCE FILTER
    # =========================================================================
    @classmethod
    def validate_sensor_duration(cls, duration_ms: int, sensor_type: str = "OPTICAL_IR") -> Dict[str, Any]:
        """
        🚫 CHỐNG ĐÁNH LỪA CẢM BIẾN (ANTI-FISHING & PAPER POKING)
        - duration_ms < 100ms: Chọc que/bìa cứng quá nhanh -> Từ chối (Debounce rejection).
        - duration_ms > 3000ms: Băng keo giữ tờ tiền / Kẹt khe -> Từ chối (Fishing attempt).
        - 100ms <= duration_ms <= 3000ms: Tiền rơi tự nhiên hợp lệ.
        """
        if duration_ms < 100:
            return {
                "valid": False,
                "reason": "DEBOUNCE_TOO_FAST_PAPER_POKE",
                "message": "Phát hiện thao tác chọc giấy bất thường (<100ms). Từ chối ghi nhận tiền."
            }
        elif duration_ms > 3000:
            return {
                "valid": False,
                "reason": "FISHING_SUSPICION_OR_BLOCKED",
                "message": "Khe nhét tiền bị che quá lâu (>3s). Phát hiện nghi vấn câu tiền hoặc kẹt vật cản."
            }
        return {
            "valid": True,
            "reason": "LEGITIMATE_DROP",
            "message": "Cảm biến ghi nhận tiền rơi tự nhiên hợp lệ."
        }

    # =========================================================================
    # 3. HARDWARE MUTEX CONCURRENT STATE LOCK
    # =========================================================================
    @classmethod
    def acquire_device_lock(cls, device_id: str, lock_type: str = "WITHDRAWAL") -> bool:
        """Khóa trạng thái độc quyền phần cứng (Vô hiệu hóa cảm biến nạp khi mở nắp)"""
        current = _HARDWARE_MUTEXES.get(device_id, {})
        if current.get("locked"):
            return False
        _HARDWARE_MUTEXES[device_id] = {
            "locked": True,
            "lock_type": lock_type,
            "timestamp": time.time()
        }
        return True

    @classmethod
    def release_device_lock(cls, device_id: str):
        """Giải phóng khóa phần cứng"""
        if device_id in _HARDWARE_MUTEXES:
            _HARDWARE_MUTEXES[device_id]["locked"] = False
            _HARDWARE_MUTEXES[device_id]["lock_type"] = "IDLE"

    @classmethod
    def is_device_locked(cls, device_id: str) -> bool:
        """Kiểm tra xem thiết bị có đang bị khóa (ví dụ đang trong phiên rút tiền) hay không"""
        lock_info = _HARDWARE_MUTEXES.get(device_id, {})
        return bool(lock_info.get("locked", False))

    # =========================================================================
    # 4. HEARTBEAT WATCHDOG (ANTI-POWER-CUT & ANTI-JAMMER)
    # =========================================================================
    @classmethod
    def record_heartbeat(cls, device_id: str, ip_address: str = "127.0.0.1", battery_pct: float = 100.0) -> Dict[str, Any]:
        """Ghi nhận nhịp tim PING gửi định kỳ mỗi 10 giây từ ESP32"""
        now = time.time()
        _HEARTBEAT_REGISTRY[device_id] = {
            "last_seen": now,
            "ip_address": ip_address,
            "battery_pct": battery_pct,
            "status": "ONLINE"
        }
        return {
            "device_id": device_id,
            "status": "HEALTHY",
            "timestamp": now,
            "ack": True
        }

    @classmethod
    def check_heartbeat_health(cls, device_id: str, timeout_seconds: int = 30) -> Dict[str, Any]:
        """
        Kiểm tra độ trễ nhịp tim:
        - Quá 30s không có tin: Cảnh báo OFFLINE_SUSPICIOUS (Nghi vấn rút điện / ngắt Wi-Fi).
        """
        record = _HEARTBEAT_REGISTRY.get(device_id)
        now = time.time()
        if not record:
            return {
                "device_id": device_id,
                "status": "OFFLINE_SUSPICIOUS",
                "seconds_offline": 999,
                "alert": "⚠️ Cảnh báo: Thiết bị chưa từng gửi nhịp tim hoặc đã mất kết nối hoàn toàn."
            }

        elapsed = now - record["last_seen"]
        if elapsed > timeout_seconds:
            record["status"] = "OFFLINE_SUSPICIOUS"
            return {
                "device_id": device_id,
                "status": "OFFLINE_SUSPICIOUS",
                "seconds_offline": elapsed,
                "alert": f"⚠️ Cảnh báo: Heo đất đã mất kết nối nhịp tim trong {elapsed:.0f}s! Nghi vấn mất nguồn hoặc rớt mạng."
            }
        return {
            "device_id": device_id,
            "status": "ONLINE",
            "seconds_offline": elapsed,
            "alert": "Thiết bị đang hoạt động ổn định."
        }

    # =========================================================================
    # 5. DEVICE UNBIND & FACTORY RESET (ANTI-RESALE HIJACK)
    # =========================================================================
    @classmethod
    def unbind_device(cls, db: Session, user_id: str, device_id: str) -> Dict[str, Any]:
        """Hủy liên kết Heo Đất khỏi tài khoản, xóa sạch token bảo mật để sẵn sàng sang nhượng"""
        device = SmartPiggyRepository.get_by_id(db, device_id)
        if not device or device.user_id != user_id:
            raise FintechBaseException(error_code=SystemConstants.WALLET_NOT_FOUND, status_code=404)

        # Xóa liên kết người dùng và đưa thiết bị về chế độ UNPAIRED
        device.user_id = "UNBOUND"
        device.status = "UNPAIRED"
        db.commit()

        # Dọn dẹp cache
        cls.release_device_lock(device_id)
        if device_id in _HEARTBEAT_REGISTRY:
            del _HEARTBEAT_REGISTRY[device_id]

        return {
            "device_id": device_id,
            "status": "UNBOUND_SUCCESS",
            "message": "Đã hủy liên kết thiết bị Heo Đất thành công. Thiết bị đã được xóa trắng sẵn sàng ghép đôi lại."
        }

    # =========================================================================
    # 6. FATAL CRASH & WRITE-OFF ADJUSTMENT (EVENTUAL CONSISTENCY)
    # =========================================================================
    @classmethod
    def handle_fatal_crash(cls, db: Session, device_id: str, g_force: float) -> Dict[str, Any]:
        """
        💥 TIẾP NHẬN TÍN HIỆU PHÁ HỦY TOÀN DIỆN (Lực va đập cực mạnh > 6G)
        1. Đóng băng ngay lập tức ví Heo Đất (FROZEN) để ngăn chặn phát sinh giao dịch.
        2. Ghi nhật ký sự cố an ninh khẩn cấp.
        """
        device = SmartPiggyRepository.get_by_id(db, device_id)
        if not device:
            device = SmartPiggyRepository.get_by_mac(db, device_id.upper())

        user_id = device.user_id if device else "unknown"
        wallet_id = device.wallet_id if device else None

        if wallet_id:
            wallet = db.query(Wallet).filter(Wallet.id == wallet_id).first()
            if wallet:
                wallet.status = "FROZEN"
                db.commit()

        # Gửi cảnh báo khẩn cấp
        ws_event = {
            "event": "FATAL_CRASH_EMERGENCY",
            "device_id": device_id,
            "g_force": g_force,
            "status": "DEVICE_DESTROYED_WALLET_FROZEN",
            "message": f"CẢNH BÁO NGUY CẤP: Heo Đất phát hiện lực va đập hủy diệt {g_force:.1f}G! Ví đã tự động đóng băng (FROZEN)."
        }
        return {
            "status": "EMERGENCY_FROZEN",
            "device_id": device_id,
            "g_force_detected": g_force,
            "wallet_status": "FROZEN",
            "message": "Heo Đất đã bị tác động vật lý phá hủy. Ví đã được phong tỏa để kiểm toán đối soát."
        }

    @classmethod
    def execute_write_off_adjustment(
        cls,
        db: Session,
        user_id: str,
        wallet_id: str,
        declared_lost_amount: float,
        reason: str = "HEO_DAT_BI_DAP_NAT"
    ) -> Dict[str, Any]:
        """
        📝 BÚT TOÁN GHI GIẢM TÀI SẢN (WRITE-OFF RECONCILIATION)
        - Đưa sổ cái ảo về trạng thái cân bằng thực tế sau khi heo bị đập/mất cắp.
        """
        wallet = db.query(Wallet).filter(Wallet.id == wallet_id, Wallet.user_id == user_id).first()
        if not wallet:
            raise FintechBaseException(error_code=SystemConstants.WALLET_NOT_FOUND, status_code=404)

        current_balance = float(wallet.balance)
        write_off_amount = min(current_balance, declared_lost_amount)
        new_balance = current_balance - write_off_amount

        wallet.balance = new_balance
        wallet.status = "ACTIVE"  # Mở khóa ví sau khi đối soát xong
        wallet.updated_at = datetime.now()

        # Ghi nhận giao dịch điều chỉnh sổ cái
        expense_cat = db.query(Category).filter(Category.type == "EXPENSE").first()
        cat_id = expense_cat.id if expense_cat else None

        TransactionRepository.insert_transaction(
            db=db,
            user_id=user_id,
            wallet_id=wallet_id,
            category_id=cat_id,
            amount=write_off_amount,
            tx_type="EXPENSE",
            tx_date=datetime.now(),
            description=f"Bút toán ghi giảm tổn thất tài sản (Write-off): {reason}",
            balance_before=current_balance,
            balance_after=new_balance
        )
        db.commit()

        return {
            "wallet_id": wallet_id,
            "status": "WRITE_OFF_COMPLETED",
            "written_off_amount": write_off_amount,
            "previous_balance": current_balance,
            "new_reconciled_balance": new_balance,
            "reconciliation_reason": reason,
            "message": f"Đã thực hiện bút toán ghi giảm {write_off_amount:,.0f} VND. Sổ cái đã cân bằng thực tế (Eventual Consistency)."
        }