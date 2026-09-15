
from fastapi import APIRouter, WebSocket, WebSocketDisconnect, Query
from app.websocket.manager.connection_manager import ws_manager

router = APIRouter(prefix="/ws", tags=["Real-time WebSocket Live Stream"])


@router.websocket("/live/{user_id}")
async def websocket_user_live_stream(websocket: WebSocket, user_id: str):
    """
    📡 WEBSOCKET LIVE STREAM DÀNH CHO MOBILE APP / WEB CLIENT:
    - Lắng nghe sự kiện nạp tiền "Ting ting" tức thời khi đút tiền vào Heo đất.
    - Nhận cảnh báo an ninh rung lắc MPU6050 và thông báo biến động số dư.
    """
    await ws_manager.connect_user(user_id, websocket)
    try:
        # Gửi thông điệp chào mừng kết nối thành công
        await websocket.send_json({
            "event": "CONNECTED",
            "user_id": user_id,
            "message": "Kết nối WebSocket Live Stream thành công!"
        })
        while True:
            # Giữ kết nối và nhận Ping/Pong từ client
            data = await websocket.receive_text()
            if data == "ping":
                await websocket.send_text("pong")
    except WebSocketDisconnect:
        ws_manager.disconnect_user(user_id, websocket)
    except Exception:
        ws_manager.disconnect_user(user_id, websocket)


@router.websocket("/piggy-live/{device_id}")
async def websocket_piggy_device_stream(websocket: WebSocket, device_id: str):
    """
    📡 WEBSOCKET LIVE STREAM THEO DÕI HEO ĐẤT CỤ THỂ:
    - Đồng bộ hiệu ứng đèn LED RGB, phát loa buzzer và hiển thị tiến độ bỏ heo.
    """
    await ws_manager.connect_device(device_id, websocket)
    try:
        await websocket.send_json({
            "event": "DEVICE_STREAM_READY",
            "device_id": device_id,
            "message": f"Đang lắng nghe luồng dữ liệu thời gian thực Heo đất {device_id}"
        })
        while True:
            data = await websocket.receive_text()
            if data == "ping":
                await websocket.send_text("pong")
    except WebSocketDisconnect:
        ws_manager.disconnect_device(device_id, websocket)
    except Exception:
        ws_manager.disconnect_device(device_id, websocket)
