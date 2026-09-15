
from pydantic import BaseModel, Field, field_validator
from typing import Optional, List
from datetime import datetime
import re


class PiggyPairRequest(BaseModel):
    mac_address: str = Field(..., example="AA:BB:CC:DD:EE:FF", description="Địa chỉ MAC của chip ESP32")
    device_name: str = Field("Heo Đất Thông Minh", example="Heo Đất Phòng Ngủ", max_length=150)
    wallet_id: Optional[str] = Field(None, description="Ví liên kết (nếu để trống sẽ tự tạo ví SMART_PIGGY)")
    pairing_code: Optional[str] = Field("123456", example="123456", description="Mã PIN 6 số trên màn hình Heo")

    @field_validator("mac_address")
    @classmethod
    def validate_mac(cls, v: str) -> str:
        v = v.strip().upper()
        if not re.match(r"^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$", v):
            raise ValueError("Địa chỉ MAC không hợp lệ (Định dạng chuẩn: AA:BB:CC:DD:EE:FF)")
        return v


class PiggyDropMoneyRequest(BaseModel):
    mac_address: Optional[str] = Field(None, example="AA:BB:CC:DD:EE:FF", description="Địa chỉ MAC phần cứng")
    device_id: Optional[str] = Field(None, example="device-uuid-123", description="ID định danh thiết bị Heo Đất")
    bucket_id: Optional[str] = Field(None, example="bucket-uuid-123", description="ID Hũ mục tiêu con (tùy chọn)")
    coin_value: float = Field(..., gt=0, example=50000.0, description="Mệnh giá tiền vừa đút (VND)")
    weight_delta_grams: Optional[float] = Field(None, example=1.2, description="Độ tăng khối lượng từ cảm biến Load Cell")
    sensor_confidence: Optional[float] = Field(0.98, ge=0.0, le=1.0, example=0.98, description="Độ tin cậy cảm biến quang (0.0 - 1.0)")
    drop_duration_ms: Optional[int] = Field(None, example=250, description="Thời gian vật thể che cảm biến quang (ms)")
    nonce: Optional[str] = Field(None, example="a1b2c3d4-nonce", description="Mã chống tấn công lặp gói tin (Replay Attack)")
    timestamp: Optional[int] = Field(None, example=1771934400, description="Unix timestamp từ đồng hồ RTC của ESP32")
    signature: Optional[str] = Field(None, description="Chữ ký HMAC-SHA256 bảo vệ gói tin vật lý")


class OfflineDropItem(BaseModel):
    offline_tx_id: str = Field(..., example="off-tx-uuid-001")
    coin_value: float = Field(..., gt=0, example=20000.0)
    weight_delta_grams: Optional[float] = Field(None, example=1.0)
    dropped_at: datetime = Field(default_factory=datetime.now)
    nonce: Optional[str] = None
    signature: Optional[str] = None


class PiggySyncOfflineBatchRequest(BaseModel):
    mac_address: str = Field(..., example="AA:BB:CC:DD:EE:FF")
    batch_items: List[OfflineDropItem] = Field(..., min_items=1)


class PiggyTamperAlertRequest(BaseModel):
    sensor_type: str = Field("ACCELEROMETER_TILT", example="MPU6050_SHAKE", description="Loại cảm biến phát hiện")
    intensity_level: str = Field("HIGH", example="CRITICAL", description="Mức độ rung lắc: LOW, MEDIUM, HIGH, CRITICAL")
    details: Optional[str] = Field("Phát hiện rung lắc mạnh hoặc heo bị dốc ngược > 90 độ.")


class PiggyLedControlRequest(BaseModel):
    color_hex: str = Field("#00FF00", example="#FF9800", description="Mã màu HEX (Ví dụ: #00FF00 xanh lá)")
    effect_mode: str = Field("SOLID", example="RAINBOW", description="Hiệu ứng: SOLID, BLINK, PULSE, RAINBOW")
    duration_seconds: int = Field(5, ge=1, le=3600, example=10)
