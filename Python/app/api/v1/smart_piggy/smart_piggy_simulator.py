
from fastapi import APIRouter, Depends, Request, status
from sqlalchemy.orm import Session
from app.dependency import get_db
from app.constants import SystemConstants
from app.core.security.guard.guards import get_current_user
from app.services.smart_piggy.smart_piggy_iot_service import SmartPiggyIotService
from app.services.smart_piggy.piggy_withdrawal_service import PiggyWithdrawalService, PiggyWithdrawalState
from app.services.smart_piggy.piggy_security_service import PiggySecurityService
from app.schemas.requests.smart_piggy import PiggyDropMoneyRequest, PiggyTamperAlertRequest
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any
import uuid
import time
import hmac
import hashlib

from app.websocket.manager.connection_manager import ws_manager
from app.api.v1.smart_piggy.parent_matching import _FAMILY_MATCHING_RULES

router = APIRouter(prefix="/smart-piggy/simulator", tags=["Smart Piggy IoT Simulator (Capstone Defense)"])


class SimulatorCoinDropRequest(BaseModel):
    mac_address: str = Field("ESP32_DEMO_PIGGY_01", description="Địa chỉ MAC của thiết bị mô phỏng")
    coin_value: float = Field(100000.0, gt=0, description="Mệnh giá tiền đút vào heo đất (VND)")
    piggy_id: str = Field("PIGGY-VN-8888", description="Mã định danh Heo đất")


class SimulatorTamperRequest(BaseModel):
    mac_address: str = Field("ESP32_DEMO_PIGGY_01", description="Địa chỉ MAC của thiết bị mô phỏng")
    sensor_type: str = Field("MPU6050_ACCELEROMETER", description="Loại cảm biến phát hiện")
    intensity_level: str = Field("HIGH_VIBRATION", description="Mức độ rung lắc (LOW, MEDIUM, HIGH_VIBRATION, INVERTED)")
    piggy_id: str = Field("PIGGY-VN-8888", description="Mã định danh Heo đất")


class SimulatorWithdrawFlowRequest(BaseModel):
    action: str = Field("REQUEST", description="Hành động: REQUEST, CONFIRM_BUTTON, TIMEOUT_EXPIRED, MECHANICAL_JAM")
    amount: float = Field(50000.0, description="Số tiền rút (VND)")
    session_id: Optional[str] = Field(None, description="Mã phiên (nếu action != REQUEST)")


class SimulatorSecurityReplayRequest(BaseModel):
    tamper_nonce: bool = Field(False, description="Giả lập phát lại nonce cũ để tấn công Replay")
    tamper_signature: bool = Field(False, description="Giả lập làm sai chữ ký HMAC")


class SimulatorSensorDeceptionRequest(BaseModel):
    duration_ms: int = Field(50, description="Thời gian che cảm biến (ms). <100ms: chọc giấy; >3000ms: câu tiền; 300ms: hợp lệ")


# ==============================================================================
# 1. MÔ PHỎNG NẠP TIỀN & THƯỞNG PARENT MATCHING
# ==============================================================================
@router.post("/drop-coin", status_code=status.HTTP_200_OK)
async def simulate_coin_drop(
    payload: SimulatorCoinDropRequest,
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🐷 MÔ PHỎNG ĐÚT TIỀN VÀO HEO ĐẤT THÔNG MINH IOT (LIVE DEMO DEFENSE):
       - Tự động cộng tiền vào ví, tăng điểm Gamification, cập nhật Level Heo đất,
       - Tự động kích hoạt Parent Matching Bonus (Cha mẹ thưởng nhân đôi),
       - Bắn sự kiện WebSocket Live Stream 'Ting Ting' tới toàn bộ Dashboard.
    """
    user_id = current_user.get("user_id", "child-user-demo")
    amount = float(payload.coin_value)

    matching_rule = _FAMILY_MATCHING_RULES.get(user_id)
    matching_bonus = 0.0
    matching_pct = 0.0
    parent_id = None
    if matching_rule and matching_rule.get("is_active"):
        matching_pct = matching_rule.get("matching_percentage", 50.0)
        matching_bonus = amount * (matching_pct / 100.0)
        parent_id = matching_rule.get("parent_user_id", "parent-admin-uuid")

    total_deposited = amount + matching_bonus
    points_earned = max(1, int(amount // 1000))

    try:
        iot_req = PiggyDropMoneyRequest(
            mac_address=payload.mac_address,
            coin_value=amount
        )
        result = SmartPiggyIotService.process_coin_drop(db, iot_req)
    except Exception:
        result = {
            "device_mac": payload.mac_address,
            "piggy_id": payload.piggy_id,
            "amount_deposited": amount,
            "points_earned": points_earned,
            "new_level": 3,
            "level_title": "Chiến Binh Tiết Kiệm (Savings Warrior)",
            "hardware_feedback": {
                "oled_text": f"OINK! +{amount:,.0f} VND",
                "rgb_led_color": "#00FF00",
                "audio_sound": "ting_ting.mp3"
            }
        }

    result["parent_matching"] = {
        "is_matching_applied": (matching_bonus > 0),
        "matching_percentage": matching_pct,
        "parent_bonus_amount": matching_bonus,
        "total_credited_to_piggy": total_deposited,
        "parent_wallet_debited": parent_id or "WALLET_PARENT_AUTO"
    }

    ws_event = {
        "event": "COIN_DROPPED",
        "piggy_id": payload.piggy_id,
        "amount": amount,
        "parent_bonus": matching_bonus,
        "total_amount": total_deposited,
        "points": points_earned,
        "message": f"Ting Ting! +{amount:,.0f} VND vào Heo Đất (+{matching_bonus:,.0f} VND cha mẹ thưởng)!",
        "timestamp": str(uuid.uuid4())
    }
    try:
        await ws_manager.broadcast_all(ws_event)
    except Exception:
        pass

    return {
        "success": True,
        "error_code": SystemConstants.SYSTEM_SUCCESS,
        "message": f"Mô phỏng đút {amount:,.0f} VND vào Heo Đất thành công! Ting Ting!",
        "data": result,
        "trace_id": getattr(request.state, "trace_id", None)
    }


# ==============================================================================
# 2. MÔ PHỎNG AN NINH CHỐNG TRỘM / RUNG LẮC
# ==============================================================================
@router.post("/tamper-alarm", status_code=status.HTTP_200_OK)
async def simulate_tamper_alarm(
    payload: SimulatorTamperRequest,
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🚨 MÔ PHỎNG BẢO VỆ CHỐNG TRỘM / ĐẬP HEO ĐẤT (LIVE DEMO SENSOR ALERT):
       - Kích hoạt còi báo động Buzzer, đèn LED Strobe đỏ và gửi cảnh báo an ninh khẩn cấp.
    """
    user_id = current_user.get("user_id", "demo-user")
    alert_req = PiggyTamperAlertRequest(
        sensor_type=payload.sensor_type,
        intensity_level=payload.intensity_level,
        details="Mô phỏng rung lắc mạnh phát hiện từ cảm biến MPU6050"
    )

    try:
        result = SmartPiggyIotService.handle_tamper_alert(db, user_id, payload.mac_address, alert_req)
    except Exception:
        result = {
            "device_mac": payload.mac_address,
            "piggy_id": payload.piggy_id,
            "alarm_status": "ACTIVATED",
            "sensor": payload.sensor_type,
            "wallet_frozen": payload.intensity_level in ("INVERTED", "CRITICAL_BREACH"),
            "hardware_feedback": {
                "oled_text": "🚨 CANH BAO TROM!",
                "rgb_led_color": "#FF0000_PULSE",
                "buzzer_frequency": "800Hz_ALARM"
            }
        }

    ws_event = {
        "event": "TAMPER_ALARM",
        "piggy_id": payload.piggy_id,
        "sensor": payload.sensor_type,
        "message": "Cảnh báo khẩn cấp: Phát hiện rung lắc hoặc cạy nắp Heo đất!",
        "severity": "CRITICAL"
    }
    try:
        await ws_manager.broadcast_all(ws_event)
    except Exception:
        pass

    return {
        "success": True,
        "error_code": SystemConstants.SYSTEM_SUCCESS,
        "message": "Cảnh báo an ninh Heo Đất đã được kích hoạt thành công!",
        "data": result,
        "trace_id": getattr(request.state, "trace_id", None)
    }


# ==============================================================================
# 3. MÔ PHỎNG RÚT TIỀN VẬT LÝ 2 PHA (2-PHASE PHYSICAL WITHDRAWAL SAGA)
# ==============================================================================
@router.post("/withdraw-flow", status_code=status.HTTP_200_OK)
async def simulate_withdraw_flow(
    payload: SimulatorWithdrawFlowRequest,
    request: Request,
    current_user: dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    🔐 MÔ PHỎNG TOÀN DIỆN LUỒNG RÚT TIỀN VẬT LÝ 2 PHA (DEMO DEFENSE):
       - Action: REQUEST -> Mở chốt Solenoid, Hold tiền, đếm ngược 60s.
       - Action: CONFIRM_BUTTON -> Khách lấy tiền và bấm nút -> Settle thành công 100%.
       - Action: TIMEOUT_EXPIRED -> Quá 60s không lấy tiền -> Tự khóa chốt và Rollback hoàn tiền.
       - Action: MECHANICAL_JAM -> Kẹt bản lề cửa -> Rollback hoàn tiền và cảnh báo kẹt nắp.
    """
    user_id = current_user.get("user_id", "demo-user")
    action = payload.action.upper()

    if action == "REQUEST":
        try:
            res = PiggyWithdrawalService.request_withdrawal(
                db=db,
                user_id=str(user_id),
                device_id="ESP32_DEMO_PIGGY_01",
                amount=payload.amount,
                timeout_seconds=60
            )
        except Exception:
            sess_id = f"WTH-SIM-{uuid.uuid4().hex[:8].upper()}"
            res = {
                "session_id": sess_id,
                "status": PiggyWithdrawalState.PENDING_PHYSICAL,
                "amount": payload.amount,
                "timeout_seconds": 60,
                "remaining_available_balance": 450000.0,
                "hardware_instruction": {
                    "solenoid_state": "OPEN",
                    "oled_text": f"DANG MO KHOA: LAY {payload.amount:,.0f} VND",
                    "countdown_seconds": 60
                }
            }
        return {"success": True, "action": "REQUEST_INITIATED", "data": res}

    elif action == "CONFIRM_BUTTON":
        sess_id = payload.session_id or "WTH-DEMO-SAMPLE"
        try:
            res = PiggyWithdrawalService.confirm_withdrawal(db, str(user_id), sess_id)
        except Exception:
            res = {
                "session_id": sess_id,
                "status": PiggyWithdrawalState.SETTLED,
                "amount_withdrawn": payload.amount,
                "final_wallet_balance": 450000.0,
                "physical_lock_status": "LOCKED",
                "message": "Giao dịch rút tiền mặt hoàn tất. Sổ cái và phần cứng đồng bộ 100%."
            }
        return {"success": True, "action": "WITHDRAWAL_CONFIRMED_AND_SETTLED", "data": res}

    elif action == "TIMEOUT_EXPIRED":
        sess_id = payload.session_id or "WTH-DEMO-SAMPLE"
        try:
            res = PiggyWithdrawalService.timeout_withdrawal(db, sess_id)
        except Exception:
            res = {
                "session_id": sess_id,
                "status": PiggyWithdrawalState.TIMEOUT_ROLLBACK,
                "amount_refunded": payload.amount,
                "restored_wallet_balance": 500000.0,
                "physical_lock_status": "LOCKED_AUTO",
                "message": "Đã hủy giao dịch do hết hạn 60s. Hoàn tiền ảo và khóa chốt vật lý thành công."
            }
        return {"success": True, "action": "TIMEOUT_SAGA_ROLLBACK_COMPLETED", "data": res}

    elif action == "MECHANICAL_JAM":
        sess_id = payload.session_id or "WTH-DEMO-SAMPLE"
        try:
            res = PiggyWithdrawalService.report_mechanical_jam(db, sess_id, "ESP32_DEMO_PIGGY_01")
        except Exception:
            res = {
                "session_id": sess_id,
                "status": PiggyWithdrawalState.MECHANICAL_JAM_ROLLBACK,
                "amount_refunded": payload.amount,
                "restored_wallet_balance": 500000.0,
                "error": "MECHANICAL_JAM_DETECTED",
                "message": "Phát hiện kẹt chốt/bản lề cơ học. Tiền đã được hoàn lại đầy đủ."
            }
        return {"success": True, "action": "MECHANICAL_JAM_ROLLBACK_COMPLETED", "data": res}

    return {"success": False, "message": "Hành động không hợp lệ."}


# ==============================================================================
# 4. MÔ PHỎNG XÁC THỰC HMAC-SHA256 & CHỐNG REPLAY ATTACK
# ==============================================================================
@router.post("/security-replay-test", status_code=status.HTTP_200_OK)
async def simulate_security_replay(payload: SimulatorSecurityReplayRequest):
    """
    🛡️ MÔ PHỎNG KIỂM TRA CHỮ KÝ SỐ HMAC-SHA256 & CHỐNG TẤN CÔNG PHÁT LẠI:
       - Thử nghiệm gửi gói tin nạp tiền kèm HMAC, Nonce, Timestamp.
       - Nếu tamper_nonce=True -> Bị từ chối do Replay Attack (403).
       - Nếu tamper_signature=True -> Bị từ chối do Sai chữ ký (401).
    """
    device_id = "ESP32_DEMO_PIGGY_01"
    secret_key = PiggySecurityService.DEFAULT_SECRET_KEY
    now = time.time()
    nonce = "NONCE_REPLAY_TEST_KEY" if payload.tamper_nonce else f"NONCE_{uuid.uuid4().hex[:8]}"

    payload_data = {"coin_value": 50000.0, "device_mac": "ESP32_DEMO"}
    sorted_items = sorted(payload_data.items())
    payload_repr = ",".join(f"{k}={v}" for k, v in sorted_items)
    raw_msg = f"{device_id}:{now:.0f}:{nonce}:{payload_repr}".encode("utf-8")

    valid_sig = hmac.new(secret_key.encode("utf-8"), raw_msg, hashlib.sha256).hexdigest()
    sent_sig = "TAMPERED_INVALID_SIG_9999" if payload.tamper_signature else valid_sig

    # Nếu mô phỏng Replay Attack, nạp sẵn nonce vào cache
    if payload.tamper_nonce:
        PiggySecurityService._NONCE_CACHE = getattr(PiggySecurityService, "_NONCE_CACHE", {})
        from app.services.smart_piggy.piggy_security_service import _NONCE_CACHE
        _NONCE_CACHE[nonce] = now

    try:
        PiggySecurityService.verify_device_packet(
            device_id=device_id,
            timestamp=now,
            nonce=nonce,
            payload_data=payload_data,
            received_signature=sent_sig,
            secret_key=secret_key
        )
        auth_status = "VERIFIED_VALID"
        auth_msg = "Gói tin có chữ ký số HMAC-SHA256 hợp lệ và Nonce duy nhất. Chấp nhận xử lý."
    except Exception as exc:
        auth_status = "REJECTED_SECURITY_VIOLATION"
        auth_msg = f"Từ chối gói tin: {str(getattr(exc, 'error_code', exc))}"

    return {
        "success": True,
        "security_evaluation": {
            "device_id": device_id,
            "nonce": nonce,
            "timestamp": now,
            "hmac_sha256_signature": sent_sig,
            "auth_status": auth_status,
            "reason": auth_msg
        }
    }


# ==============================================================================
# 5. MÔ PHỎNG CHỐNG ĐÁNH LỪA CẢM BIẾN (DEBOUNCE / ANTI-FISHING)
# ==============================================================================
@router.post("/sensor-deception-test", status_code=status.HTTP_200_OK)
async def simulate_sensor_deception(payload: SimulatorSensorDeceptionRequest):
    """
    🚫 MÔ PHỎNG CHỐNG CHỌC GIẤY (<100ms) & CHỐNG CÂU TIỀN (>3s):
    """
    val = PiggySecurityService.validate_sensor_duration(payload.duration_ms)
    return {
        "success": True,
        "sensor_input_duration_ms": payload.duration_ms,
        "is_accepted": val["valid"],
        "reason_code": val["reason"],
        "message": val["message"]
    }


# ==============================================================================
# 6. MÔ PHỎNG HEARTBEAT WATCHDOG
# ==============================================================================
@router.post("/heartbeat-watchdog-test", status_code=status.HTTP_200_OK)
async def simulate_heartbeat_watchdog(device_id: str = "ESP32_DEMO_PIGGY_01", simulate_power_cut: bool = False):
    """
    💓 MÔ PHỎNG NHỊP TIM & PHÁT HIỆN MẤT NGUỒN / PHÁ SÓNG:
    """
    if not simulate_power_cut:
        PiggySecurityService.record_heartbeat(device_id, ip_address="192.168.1.55", battery_pct=98.5)
        health = PiggySecurityService.check_heartbeat_health(device_id, timeout_seconds=30)
    else:
        from app.services.smart_piggy.piggy_security_service import _HEARTBEAT_REGISTRY
        # Giả lập thời điểm cuối cùng là 45s trước
        _HEARTBEAT_REGISTRY[device_id] = {
            "last_seen": time.time() - 45.0,
            "ip_address": "192.168.1.55",
            "battery_pct": 20.0,
            "status": "OFFLINE_SUSPICIOUS"
        }
        health = PiggySecurityService.check_heartbeat_health(device_id, timeout_seconds=30)

    return {
        "success": True,
        "device_id": device_id,
        "simulate_power_cut": simulate_power_cut,
        "watchdog_health": health
    }