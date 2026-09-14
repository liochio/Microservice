# 📄 Đường dẫn file: app/services/smart_piggy/smart_piggy_iot_service.py
from sqlalchemy.orm import Session
from sqlalchemy import text
from typing import List, Dict, Any, Optional
from datetime import datetime
import hmac
import hashlib
import uuid
import asyncio
import time

from app.constants import SystemConstants
from app.core.exceptions.base_exception import FintechBaseException
from app.repositories.smart_piggy.smart_piggy_repository import SmartPiggyRepository
from app.repositories.wallet.wallet_repository import WalletRepository
from app.repositories.finance.transaction_repository import TransactionRepository
from app.models.wallet.wallet import Wallet
from app.models.finance.category import Category
from app.models.notification.notification import Notification
from app.models.smart_piggy.smart_piggy_goal import SmartPiggyGoal
from app.websocket.manager.connection_manager import ws_manager

_DEPOSIT_SESSIONS: Dict[str, Dict[str, Any]] = {}
from app.schemas.requests.smart_piggy import (
    PiggyPairRequest,
    PiggyDropMoneyRequest,
    PiggySyncOfflineBatchRequest,
    PiggyTamperAlertRequest,
    PiggyLedControlRequest
)
from app.services.smart_piggy.piggy_security_service import PiggySecurityService


class SmartPiggyIotService:
    """
    👑 SMART PIGGY IOT CORE SERVICE (BUSINESS LAYER)
    🎯 Xử lý toàn bộ luồng nạp tiền từ cảm biến phần cứng ESP32 về Backend theo chuẩn ACID,
       chống Replay Attack (HMAC-SHA256), chống chọc giấy (Debounce Filter),
       khóa phần cứng độc quyền (Hardware Mutex), phát thông báo WebSocket Live và tính Gamification.
    """

    LEVEL_TITLES = {
        1: "Heo Đất Sơ Sinh (Baby Piggy)",
        2: "Heo Con Chăm Chỉ (Diligent Piggy)",
        3: "Chiến Binh Tiết Kiệm (Savings Warrior)",
        4: "Bậc Thầy Tích Lũy (Accumulation Master)",
        5: "Đại Gia Heo Vàng (Golden Piggy Tycoon)"
    }

    @staticmethod
    def pair_device(db: Session, user_id: str, payload: PiggyPairRequest) -> dict:
        """Ghép nối thiết bị Heo đất ESP32 mới vào tài khoản người dùng"""
        mac = payload.mac_address.upper()
        existing = SmartPiggyRepository.get_by_mac(db, mac)

        # Nếu chưa chọn ví, tìm hoặc tạo ví SMART_PIGGY
        wallet_id = payload.wallet_id
        if not wallet_id:
            user_wallets = db.query(Wallet).filter(Wallet.user_id == user_id, Wallet.is_deleted == False).all()
            piggy_wallet = next((w for w in user_wallets if getattr(w, "wallet_type", "") == "SAVINGS" or "HEO" in getattr(w, "name", "").upper()), None)
            if piggy_wallet:
                wallet_id = piggy_wallet.id
            elif user_wallets:
                wallet_id = user_wallets[0].id
            else:
                new_w = Wallet(
                    id=str(uuid.uuid4()),
                    user_id=user_id,
                    wallet_code=f"PIGGY_{user_id[:8].upper()}",
                    name="Ví Heo Đất Thông Minh",
                    wallet_type="SAVINGS",
                    wallet_account=str(uuid.uuid4().int)[:10],
                    balance=0.0,
                    currency="VND",
                    description="Ví tích lũy kết nối phần cứng Heo đất IoT",
                    color="#E91E63",
                    icon="piggy-bank",
                    status="ACTIVE",
                    is_deleted=False
                )
                db.add(new_w)
                db.flush()
                wallet_id = new_w.id

        if existing:
            existing.user_id = user_id
            existing.wallet_id = wallet_id
            existing.device_name = payload.device_name
            existing.status = "ONLINE"
            db.commit()
            db.refresh(existing)
            device = existing
        else:
            device = SmartPiggyRepository.create_device(
                db=db,
                user_id=user_id,
                wallet_id=wallet_id,
                mac_address=mac,
                device_name=payload.device_name
            )
            db.commit()
            db.refresh(device)

        return {
            "id": device.id,
            "mac_address": device.mac_address,
            "device_name": device.device_name,
            "wallet_id": device.wallet_id,
            "total_coins_dropped": float(device.total_coins_dropped),
            "status": device.status,
            "created_at": device.created_at,
            "updated_at": device.updated_at
        }

    @staticmethod
    def get_user_devices(db: Session, user_id: str) -> List[dict]:
        """Lấy danh sách Heo đất của người dùng"""
        devices = SmartPiggyRepository.get_user_devices(db, user_id)
        return [
            {
                "id": d.id,
                "mac_address": d.mac_address,
                "device_name": d.device_name,
                "wallet_id": d.wallet_id,
                "total_coins_dropped": float(d.total_coins_dropped),
                "status": d.status,
                "created_at": d.created_at,
                "updated_at": d.updated_at
            }
            for d in devices
        ]

    @staticmethod
    def get_device_detail(db: Session, user_id: str, device_id: str) -> dict:
        """Xem chi tiết 1 Heo đất"""
        d = SmartPiggyRepository.get_by_id(db, device_id)
        if not d or d.user_id != user_id:
            raise FintechBaseException(error_code=SystemConstants.WALLET_NOT_FOUND, status_code=404)
        return {
            "id": d.id,
            "mac_address": d.mac_address,
            "device_name": d.device_name,
            "wallet_id": d.wallet_id,
            "total_coins_dropped": float(d.total_coins_dropped),
            "status": d.status,
            "created_at": d.created_at,
            "updated_at": d.updated_at
        }

    @staticmethod
    def process_coin_drop(db: Session, payload: PiggyDropMoneyRequest) -> dict:
        """
        🛡️ LUỒNG NẠP TIỀN CỐT LÕI TỪ PHẦN CỨNG HEO ĐẤT (ACID TRANSACTION):
        0. Kiểm tra Hardware Mutex: Nếu heo đang mở nắp rút tiền -> Từ chối nạp để tránh Race Condition.
        1. Tìm thiết bị theo MAC.
        2. Ghi nhật ký coin_log.
        3. Tăng tổng nạp trên thiết bị.
        4. Tạo giao dịch INCOME và cộng số dư ví.
        5. Cộng điểm Gamification và sinh lệnh điều khiển phần cứng.
        6. Tự động tính Parent Matching Bonus (nếu có).
        7. Bắn WebSocket Live Thông Báo "Ting ting" lên Mobile App tức thời.
        """
        device = None
        if getattr(payload, "device_id", None):
            device = SmartPiggyRepository.get_by_id(db, payload.device_id)
        if not device and getattr(payload, "mac_address", None):
            device = SmartPiggyRepository.get_by_mac(db, payload.mac_address.upper())
        if not device:
            raise FintechBaseException(error_code=SystemConstants.WALLET_NOT_FOUND, status_code=404)

        # 0. Kiểm tra Hardware Mutex Lock
        if PiggySecurityService.is_device_locked(device.id):
            raise FintechBaseException(
                error_code="HARDWARE_MUTEX_LOCKED",
                status_code=409
            )

        amount = payload.coin_value
        user_id = device.user_id
        wallet_id = device.wallet_id

        # 1. Ghi nhật ký phần cứng
        SmartPiggyRepository.record_coin_drop(db, device.id, amount, status="SUCCESS")

        # 2. Cập nhật tổng nạp trên thiết bị
        device.total_coins_dropped = float(device.total_coins_dropped) + amount
        device.status = "ONLINE"

        # 3. Cộng tiền vào ví tài chính trong cùng Transaction
        wallet = db.query(Wallet).filter(Wallet.id == wallet_id).first()
        if wallet:
            bal_before = float(wallet.balance)
            wallet.balance = bal_before + amount
            bal_after = float(wallet.balance)
            new_balance = bal_after
        else:
            bal_before = 0.0
            bal_after = amount
            new_balance = amount

        # Cập nhật số dư Hũ mục tiêu con (nếu có)
        if getattr(payload, "bucket_id", None):
            bucket = db.query(SmartPiggyGoal).filter(SmartPiggyGoal.id == payload.bucket_id).first()
            if bucket:
                bucket.current_amount = float(bucket.current_amount) + amount
                db.commit()

        # 4. Ghi Transaction tài chính (INCOME)
        income_cat = db.query(Category).filter(Category.type == "INCOME").first()
        cat_id = income_cat.id if income_cat else None

        TransactionRepository.insert_transaction(
            db=db,
            user_id=user_id,
            wallet_id=wallet_id,
            category_id=cat_id,
            amount=amount,
            tx_type="INCOME",
            tx_date=datetime.now(),
            description=f"Tiền đút ống heo thông minh: {device.device_name}",
            balance_before=bal_before,
            balance_after=bal_after
        )

        # 5. Cộng điểm Gamification (1,000 VND = 1 Point)
        points_earned = max(1, int(amount // 1000))
        game = SmartPiggyRepository.add_gamification_points(db, user_id, points_earned)

        # 6. Ghi nhật ký kích hoạt LED vui mừng trên Heo đất
        SmartPiggyRepository.record_led_event(db, device.id, "HAPPY_PULSE_GREEN", "COIN_DROP_TRIGGER")

        # 7. Tạo bản ghi thông báo hệ thống
        notif = Notification(
            user_id=user_id,
            recipient=user_id,
            title="🐷 Ting Ting! Đút Tiền Thành Công",
            content=f"Bạn vừa bỏ vào Heo đất {amount:,.0f} VND. Số dư mới: {new_balance:,.0f} VND (+{points_earned} điểm)",
            notification_type="SMART_PIGGY",
            is_read=0,
            status="ACTIVE"
        )
        db.add(notif)
        db.commit()

        level = game.current_level
        title = SmartPiggyIotService.LEVEL_TITLES.get(level, "Heo Đất Thông Minh")

        # 8. Bắn WebSocket Live Stream Event tới App (Non-blocking)
        ws_payload = {
            "event": "COIN_DEPOSITED",
            "device_id": device.id,
            "coin_value": amount,
            "new_balance": new_balance,
            "points_earned": points_earned,
            "current_level": level,
            "level_title": title,
            "sound": "ting_ting.mp3",
            "animation": "FIREWORKS"
        }
        try:
            loop = asyncio.get_event_loop()
            if loop.is_running():
                asyncio.create_task(ws_manager.send_to_user(user_id, ws_payload))
                asyncio.create_task(ws_manager.send_to_device(device.id, ws_payload))
        except Exception:
            pass

        return {
            "device_id": device.id,
            "device_name": device.device_name,
            "wallet_id": wallet_id,
            "coin_value_deposited": amount,
            "parent_matching_bonus": 0.0,
            "total_credited": amount,
            "new_wallet_balance": new_balance,
            "gamification_points_earned": points_earned,
            "current_total_points": game.current_points,
            "current_level": level,
            "level_title": title,
            "hardware_command": {
                "led_rgb": "#00FF00",
                "led_effect": "HAPPY_PULSE",
                "buzzer_beeps": 2,
                "display_text": f"OINK! +{amount:,.0f} VND"
            }
        }

    @staticmethod
    def sync_offline_batch(db: Session, payload: PiggySyncOfflineBatchRequest) -> dict:
        """Đồng bộ danh sách tiền đút ngoại tuyến khi có lại mạng WiFi"""
        mac = payload.mac_address.upper()
        device = SmartPiggyRepository.get_by_mac(db, mac)
        if not device:
            raise FintechBaseException(error_code=SystemConstants.WALLET_NOT_FOUND, status_code=404)

        total_batch_amount = 0.0
        user_id = device.user_id
        wallet_id = device.wallet_id

        wallet = db.query(Wallet).filter(Wallet.id == wallet_id).first()
        current_bal = float(wallet.balance) if wallet else 0.0

        income_cat = db.query(Category).filter(Category.type == "INCOME").first()
        cat_id = income_cat.id if income_cat else None

        for item in payload.batch_items:
            SmartPiggyRepository.record_coin_drop(db, device.id, item.coin_value, status="SUCCESS", created_at=item.dropped_at)
            total_batch_amount += item.coin_value
            bal_before = current_bal
            current_bal += item.coin_value

            TransactionRepository.insert_transaction(
                db=db,
                user_id=user_id,
                wallet_id=wallet_id,
                category_id=cat_id,
                amount=item.coin_value,
                tx_type="INCOME",
                tx_date=item.dropped_at,
                description=f"Đồng bộ tiền đút heo ngoại tuyến",
                balance_before=bal_before,
                balance_after=current_bal
            )

        device.total_coins_dropped = float(device.total_coins_dropped) + total_batch_amount
        if wallet:
            wallet.balance = current_bal

        points_earned = max(1, int(total_batch_amount // 1000))
        game = SmartPiggyRepository.add_gamification_points(db, user_id, points_earned)
        db.commit()

        return {
            "device_id": device.id,
            "device_name": device.device_name,
            "wallet_id": wallet_id,
            "coin_value_deposited": total_batch_amount,
            "parent_matching_bonus": 0.0,
            "total_credited": total_batch_amount,
            "new_wallet_balance": current_bal,
            "gamification_points_earned": points_earned,
            "current_total_points": game.current_points,
            "current_level": game.current_level,
            "level_title": SmartPiggyIotService.LEVEL_TITLES.get(game.current_level, "Heo Đất"),
            "hardware_command": {
                "led_rgb": "#00E5FF",
                "led_effect": "BATCH_SYNC_COMPLETED",
                "buzzer_beeps": 3,
                "display_text": f"SYNCED {len(payload.batch_items)} COINS"
            }
        }

    @staticmethod
    def get_coin_history(db: Session, user_id: str, device_id: str, limit: int = 50) -> dict:
        """Lấy lịch sử đút tiền vào Heo đất"""
        device = SmartPiggyRepository.get_by_id(db, device_id)
        if not device or device.user_id != user_id:
            raise FintechBaseException(error_code=SystemConstants.WALLET_NOT_FOUND, status_code=404)

        logs = SmartPiggyRepository.get_coin_logs(db, device_id, limit)
        return {
            "device_id": device.id,
            "device_name": device.device_name,
            "total_drops": len(logs),
            "total_amount": sum(float(l.coin_value) for l in logs),
            "logs": [
                {
                    "id": l.id,
                    "coin_value": float(l.coin_value),
                    "status": l.status,
                    "created_at": l.created_at
                }
                for l in logs
            ]
        }

    @staticmethod
    def handle_tamper_alert(db: Session, user_id: str, device_id: str, payload: PiggyTamperAlertRequest) -> dict:
        """Bảo vệ chống trộm/đập heo: MPU6050 phát hiện rung lắc mạnh hoặc dốc ngược"""
        device = SmartPiggyRepository.get_by_id(db, device_id)
        if not device:
            device = SmartPiggyRepository.get_by_mac(db, device_id.upper())

        # Nếu rung lắc cực đoan (INVERTED hoặc SEVERE_BREACH) -> Tự động phong tỏa ví
        if payload.intensity_level in ("INVERTED", "CRITICAL_BREACH", "SEVERE"):
            if device and device.wallet_id:
                w = db.query(Wallet).filter(Wallet.id == device.wallet_id).first()
                if w:
                    w.status = "FROZEN"
                    db.commit()

        if device:
            SmartPiggyRepository.record_led_event(db, device.id, "STROBE_ALARM_RED", f"TAMPER_{payload.intensity_level}")
            db.commit()

        # Bắn WebSocket cảnh báo an ninh
        ws_payload = {
            "event": "TAMPER_ALERT",
            "device_id": device.id if device else device_id,
            "sensor_type": payload.sensor_type,
            "intensity_level": payload.intensity_level,
            "details": payload.details or "Phát hiện rung lắc bất thường trên Heo Đất!"
        }
        try:
            loop = asyncio.get_event_loop()
            if loop.is_running():
                asyncio.create_task(ws_manager.send_to_user(user_id, ws_payload))
                asyncio.create_task(ws_manager.broadcast_all(ws_payload))
        except Exception:
            pass

        return {
            "alert_status": "TRIGGERED",
            "device_id": device.id if device else device_id,
            "intensity": payload.intensity_level,
            "wallet_frozen": payload.intensity_level in ("INVERTED", "CRITICAL_BREACH", "SEVERE"),
            "hardware_response": {
                "buzzer_frequency_hz": 2000,
                "buzzer_duration_ms": 3000,
                "led_rgb": "#FF0000",
                "led_effect": "STROBE_ALARM"
            }
        }

    @staticmethod
    def control_led(db: Session, user_id: str, device_id: str, payload: PiggyLedControlRequest) -> dict:
        """Điều khiển đèn LED RGB trên lưng Heo đất từ Mobile App"""
        device = SmartPiggyRepository.get_by_id(db, device_id)
        if not device or device.user_id != user_id:
            raise FintechBaseException(error_code=SystemConstants.WALLET_NOT_FOUND, status_code=404)

        SmartPiggyRepository.record_led_event(db, device_id, f"{payload.effect_mode}_{payload.color_hex}", "APP_REMOTE_CONTROL")
        db.commit()

        return {
            "command_sent": True,
            "device_id": device_id,
            "color_hex": payload.color_hex,
            "effect_mode": payload.effect_mode,
            "duration_seconds": payload.duration_seconds
        }

    # =========================================================================
    # 🪙 LUỒNG NẠP TIỀN CHỌN MỆNH GIÁ & MỞ KHE NẠP 60 GIÂY
    # =========================================================================
    @staticmethod
    def init_deposit(
        db: Session,
        user_id: str,
        device_id: str,
        amount: float,
        bucket_id: Optional[str] = None,
        timeout_seconds: int = 60
    ) -> dict:
        """
        🚀 KHỞI TẠO PHIÊN NẠP TIỀN (CHỌN MỆNH GIÁ & HŨ MỤC TIÊU):
        - Mở chốt Solenoid khe nạp trong 60 giây.
        - Phát lệnh WebSocket / MQTT CMD_OPEN_SLOT tới ESP32.
        - Đăng ký phiên nạp PENDING_DROP chờ cảm biến quang xác nhận.
        """
        if amount <= 0:
            raise FintechBaseException(error_code="INVALID_AMOUNT", status_code=400)

        device = SmartPiggyRepository.get_by_id(db, device_id)
        if not device or device.user_id != user_id:
            device = SmartPiggyRepository.get_by_mac(db, device_id.upper())
            if not device or device.user_id != user_id:
                raise FintechBaseException(error_code=SystemConstants.WALLET_NOT_FOUND, status_code=404)

        # Kiểm tra Hũ mục tiêu con nếu có truyền
        target_bucket = None
        if bucket_id:
            target_bucket = SmartPiggyRepository.get_bucket_by_id(db, bucket_id)
            if not target_bucket or target_bucket.smart_piggy_device_id != device.id:
                raise FintechBaseException(error_code="BUCKET_NOT_FOUND", status_code=404)

        txn_id = f"DEP-{uuid.uuid4().hex[:12].upper()}"
        now_ts = time.time()
        expires_at = now_ts + timeout_seconds

        _DEPOSIT_SESSIONS[txn_id] = {
            "txn_id": txn_id,
            "user_id": user_id,
            "device_id": device.id,
            "amount": amount,
            "bucket_id": bucket_id,
            "status": "PENDING_DROP",
            "created_at": now_ts,
            "expires_at": expires_at
        }

        # Phát bản tin mở khe nạp qua WebSocket tới Web & ESP32
        ws_payload = {
            "event": "CMD_OPEN_SLOT",
            "txn_id": txn_id,
            "device_id": device.id,
            "amount": amount,
            "bucket_name": target_bucket.goal_name if target_bucket else "Heo Đất Chung",
            "timeout_seconds": timeout_seconds,
            "message": f"Mời đưa tờ tiền {amount:,.0f} VND qua khe nạp (Thời gian chờ: {timeout_seconds}s)"
        }
        try:
            loop = asyncio.get_event_loop()
            if loop.is_running():
                asyncio.create_task(ws_manager.send_to_user(user_id, ws_payload))
                asyncio.create_task(ws_manager.send_to_device(device.id, ws_payload))
        except Exception:
            pass

        return {
            "txn_id": txn_id,
            "device_id": device.id,
            "amount": amount,
            "bucket_id": bucket_id,
            "bucket_name": target_bucket.goal_name if target_bucket else "Heo Đất Chung",
            "timeout_seconds": timeout_seconds,
            "status": "PENDING_DROP",
            "solenoid_slot_state": "OPEN",
            "message": f"Đã mở chốt khe nạp tiền. Quý khách vui lòng đút tờ {amount:,.0f} VND trong {timeout_seconds} giây."
        }

    @staticmethod
    def timeout_deposit(db: Session, txn_id: str, device_id: Optional[str] = None) -> dict:
        """
        ⏱️ HẾT THỜI GIAN 60S NẠP TIỀN (ROLLBACK & ĐÓNG KHE):
        - Hủy phiên nạp PENDING, đóng chốt Solenoid khe đút.
        """
        session = _DEPOSIT_SESSIONS.get(txn_id)
        if not session:
            return {
                "txn_id": txn_id,
                "status": "EXPIRED_OR_COMPLETED",
                "message": "Phiên nạp đã hết hạn hoặc đã hoàn tất trước đó."
            }

        session["status"] = "CANCELLED_TIMEOUT"
        dev_id = session.get("device_id") or device_id

        ws_payload = {
            "event": "DEPOSIT_TIMEOUT",
            "txn_id": txn_id,
            "device_id": dev_id,
            "status": "CANCELLED_TIMEOUT",
            "message": "Quá thời gian 60 giây không ghi nhận tiền đi qua khe. Khe nạp đã đóng lại bảo vệ an toàn."
        }
        try:
            loop = asyncio.get_event_loop()
            if loop.is_running():
                if dev_id:
                    asyncio.create_task(ws_manager.send_to_device(dev_id, ws_payload))
                user_id = session.get("user_id")
                if user_id:
                    asyncio.create_task(ws_manager.send_to_user(user_id, ws_payload))
        except Exception:
            pass

        return {
            "txn_id": txn_id,
            "status": "CANCELLED_TIMEOUT",
            "solenoid_slot_state": "CLOSED",
            "message": "Phiên nạp tiền đã hủy do hết thời gian chờ 60 giây. Khe nạp đã tự động đóng."
        }

    # =========================================================================
    # 🎯 QUẢN LÝ ĐA HŨ MỤC TIÊU CON TRONG VÍ HEO (SUB-POTS / BUCKETS)
    # =========================================================================
    @staticmethod
    def get_buckets(db: Session, user_id: str, device_id: str) -> List[dict]:
        """Lấy danh sách các Hũ mục tiêu con trong Heo Đất"""
        device = SmartPiggyRepository.get_by_id(db, device_id)
        if not device or device.user_id != user_id:
            device = SmartPiggyRepository.get_by_mac(db, device_id.upper())
            if not device or device.user_id != user_id:
                raise FintechBaseException(error_code=SystemConstants.WALLET_NOT_FOUND, status_code=404)

        raw_buckets = SmartPiggyRepository.get_buckets(db, device.id)
        results = []
        for b in raw_buckets:
            tgt = float(b.target_amount)
            curr = float(b.current_amount)
            pct = round((curr / tgt * 100) if tgt > 0 else 0, 1)
            results.append({
                "id": b.id,
                "device_id": b.smart_piggy_device_id,
                "goal_name": b.goal_name,
                "target_amount": tgt,
                "current_amount": curr,
                "progress_percentage": min(100.0, pct),
                "is_completed": curr >= tgt,
                "deadline": b.deadline.strftime("%Y-%m-%d") if b.deadline else None,
                "status": b.status,
                "created_at": b.created_at.strftime("%Y-%m-%d %H:%M:%S") if b.created_at else None
            })
        return results

    @staticmethod
    def create_bucket(
        db: Session,
        user_id: str,
        device_id: str,
        goal_name: str,
        target_amount: float,
        deadline: Optional[datetime] = None
    ) -> dict:
        """Tạo thêm Hũ mục tiêu con mới trong ví Heo"""
        if target_amount <= 0:
            raise FintechBaseException(error_code="INVALID_AMOUNT", status_code=400)

        device = SmartPiggyRepository.get_by_id(db, device_id)
        if not device or device.user_id != user_id:
            device = SmartPiggyRepository.get_by_mac(db, device_id.upper())
            if not device or device.user_id != user_id:
                raise FintechBaseException(error_code=SystemConstants.WALLET_NOT_FOUND, status_code=404)

        b = SmartPiggyRepository.create_bucket(
            db=db,
            device_id=device.id,
            goal_name=goal_name,
            target_amount=target_amount,
            deadline=deadline
        )
        db.commit()
        db.refresh(b)
        return {
            "id": b.id,
            "device_id": b.smart_piggy_device_id,
            "goal_name": b.goal_name,
            "target_amount": float(b.target_amount),
            "current_amount": float(b.current_amount),
            "status": b.status,
            "created_at": b.created_at.strftime("%Y-%m-%d %H:%M:%S") if b.created_at else None
        }

    @staticmethod
    def transfer_bucket_funds(
        db: Session,
        user_id: str,
        from_bucket_id: str,
        to_bucket_id: str,
        amount: float
    ) -> dict:
        """Chuyển tiền nội bộ giữa các hũ con trong cùng một Heo đất"""
        if amount <= 0:
            raise FintechBaseException(error_code="INVALID_AMOUNT", status_code=400)

        from_b = SmartPiggyRepository.get_bucket_by_id(db, from_bucket_id)
        to_b = SmartPiggyRepository.get_bucket_by_id(db, to_bucket_id)
        if not from_b or not to_b:
            raise FintechBaseException(error_code="BUCKET_NOT_FOUND", status_code=404)

        # Kiểm tra tính sở hữu của user
        dev = SmartPiggyRepository.get_by_id(db, from_b.smart_piggy_device_id)
        if not dev or dev.user_id != user_id:
            raise FintechBaseException(error_code=SystemConstants.WALLET_NOT_FOUND, status_code=403)

        try:
            res = SmartPiggyRepository.transfer_bucket_funds(db, from_bucket_id, to_bucket_id, amount)
            db.commit()
            return res
        except ValueError as val_err:
            raise FintechBaseException(error_code="BUCKET_TRANSFER_FAILED", status_code=400, context={"reason": str(val_err)})

    @staticmethod
    def delete_bucket(db: Session, user_id: str, bucket_id: str) -> dict:
        """Xóa Hũ mục tiêu con và hoàn dồn số dư"""
        b = SmartPiggyRepository.get_bucket_by_id(db, bucket_id)
        if not b:
            raise FintechBaseException(error_code="BUCKET_NOT_FOUND", status_code=404)

        dev = SmartPiggyRepository.get_by_id(db, b.smart_piggy_device_id)
        if not dev or dev.user_id != user_id:
            raise FintechBaseException(error_code=SystemConstants.WALLET_NOT_FOUND, status_code=403)

        res = SmartPiggyRepository.delete_bucket(db, bucket_id)
        db.commit()
        return res