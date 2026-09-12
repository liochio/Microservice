# 📄 Đường dẫn file: app/repositories/smart_piggy/smart_piggy_repository.py
from sqlalchemy.orm import Session
from sqlalchemy import desc, func
from typing import List, Optional
from datetime import datetime
import uuid

from app.models.smart_piggy.smart_piggy_device import SmartPiggyDevice
from app.models.smart_piggy.smart_piggy_coin_log import SmartPiggyCoinLog
from app.models.smart_piggy.smart_piggy_sensor import SmartPiggySensor
from app.models.smart_piggy.smart_piggy_led_log import SmartPiggyLedLog
from app.models.smart_piggy.smart_piggy_gamification import SmartPiggyGamification


class SmartPiggyRepository:
    """
    👑 SMART PIGGY REPOSITORY (DATA ACCESS LAYER)
    🎯 Xử lý truy vấn và ghi nhận dữ liệu phần cứng Heo đất thông minh, cảm biến và Gamification.
    """

    @staticmethod
    def get_by_mac(db: Session, mac_address: str) -> Optional[SmartPiggyDevice]:
        return db.query(SmartPiggyDevice).filter(
            SmartPiggyDevice.mac_address == mac_address.upper()
        ).first()

    @staticmethod
    def get_by_id(db: Session, device_id: str) -> Optional[SmartPiggyDevice]:
        return db.query(SmartPiggyDevice).filter(
            SmartPiggyDevice.id == device_id
        ).first()

    @staticmethod
    def get_user_devices(db: Session, user_id: str) -> List[SmartPiggyDevice]:
        return db.query(SmartPiggyDevice).filter(
            SmartPiggyDevice.user_id == user_id
        ).order_by(desc(SmartPiggyDevice.created_at)).all()

    @staticmethod
    def create_device(db: Session, user_id: str, wallet_id: str, mac_address: str, device_name: str) -> SmartPiggyDevice:
        device = SmartPiggyDevice(
            id=str(uuid.uuid4()),
            user_id=user_id,
            wallet_id=wallet_id,
            mac_address=mac_address.upper(),
            device_name=device_name,
            total_coins_dropped=0.0,
            status="ONLINE"
        )
        db.add(device)
        db.flush()
        return device

    @staticmethod
    def record_coin_drop(db: Session, device_id: str, coin_value: float, status: str = "SUCCESS", created_at: Optional[datetime] = None) -> SmartPiggyCoinLog:
        coin_log = SmartPiggyCoinLog(
            id=str(uuid.uuid4()),
            smart_piggy_device_id=device_id,
            coin_value=coin_value,
            status=status,
            created_at=created_at or datetime.now()
        )
        db.add(coin_log)
        db.flush()
        return coin_log

    @staticmethod
    def get_coin_logs(db: Session, device_id: str, limit: int = 50) -> List[SmartPiggyCoinLog]:
        return db.query(SmartPiggyCoinLog).filter(
            SmartPiggyCoinLog.smart_piggy_device_id == device_id
        ).order_by(desc(SmartPiggyCoinLog.created_at)).limit(limit).all()

    @staticmethod
    def record_sensor_event(db: Session, device_id: str, sensor_type: str, reading_payload: dict) -> SmartPiggySensor:
        sensor_entry = SmartPiggySensor(
            id=str(uuid.uuid4()),
            smart_piggy_device_id=device_id,
            sensor_type=sensor_type,
            reading_payload=reading_payload,
            status="PROCESSED"
        )
        db.add(sensor_entry)
        db.flush()
        return sensor_entry

    @staticmethod
    def record_led_event(db: Session, device_id: str, led_effect: str, triggered_by: str) -> SmartPiggyLedLog:
        led_entry = SmartPiggyLedLog(
            id=str(uuid.uuid4()),
            smart_piggy_device_id=device_id,
            led_effect=led_effect,
            triggered_by=triggered_by,
            status="EXECUTED"
        )
        db.add(led_entry)
        db.flush()
        return led_entry

    @staticmethod
    def get_or_create_gamification(db: Session, user_id: str) -> SmartPiggyGamification:
        game = db.query(SmartPiggyGamification).filter(
            SmartPiggyGamification.user_id == user_id
        ).first()
        if not game:
            game = SmartPiggyGamification(
                id=str(uuid.uuid4()),
                user_id=user_id,
                current_points=0,
                current_level=1,
                streak_days=1,
                status="ACTIVE"
            )
            db.add(game)
            db.flush()
        return game

    @staticmethod
    def add_gamification_points(db: Session, user_id: str, points: int) -> SmartPiggyGamification:
        game = SmartPiggyRepository.get_or_create_gamification(db, user_id)
        game.current_points += points

        # Cấp độ Heo: Level 1 (0-500), Level 2 (501-2000), Level 3 (2001-5000), Level 4 (5001-10000), Level 5 (10000+)
        p = game.current_points
        if p >= 10000:
            game.current_level = 5
        elif p >= 5000:
            game.current_level = 4
        elif p >= 2000:
            game.current_level = 3
        elif p >= 500:
            game.current_level = 2
        else:
            game.current_level = 1

        db.flush()
        return game
