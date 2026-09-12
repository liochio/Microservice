# 📄 Đường dẫn file: app/websocket/manager/connection_manager.py
import json
import asyncio
from typing import Dict, List, Any
from fastapi import WebSocket


class ConnectionManager:
    """
    👑 WEBSOCKET CONNECTION POOLING MANAGER
    🎯 Quản lý danh sách kết nối WebSocket thời gian thực theo user_id và device_id.
    """
    def __init__(self):
        # user_id -> List[WebSocket]
        self.active_user_connections: Dict[str, List[WebSocket]] = {}
        # device_id -> List[WebSocket]
        self.active_device_connections: Dict[str, List[WebSocket]] = {}

    async def connect_user(self, user_id: str, websocket: WebSocket):
        await websocket.accept()
        if user_id not in self.active_user_connections:
            self.active_user_connections[user_id] = []
        self.active_user_connections[user_id].append(websocket)

    def disconnect_user(self, user_id: str, websocket: WebSocket):
        if user_id in self.active_user_connections:
            if websocket in self.active_user_connections[user_id]:
                self.active_user_connections[user_id].remove(websocket)
            if not self.active_user_connections[user_id]:
                del self.active_user_connections[user_id]

    async def connect_device(self, device_id: str, websocket: WebSocket):
        await websocket.accept()
        if device_id not in self.active_device_connections:
            self.active_device_connections[device_id] = []
        self.active_device_connections[device_id].append(websocket)

    def disconnect_device(self, device_id: str, websocket: WebSocket):
        if device_id in self.active_device_connections:
            if websocket in self.active_device_connections[device_id]:
                self.active_device_connections[device_id].remove(websocket)
            if not self.active_device_connections[device_id]:
                del self.active_device_connections[device_id]

    async def send_to_user(self, user_id: str, message: dict):
        """Bắn dữ liệu JSON thời gian thực tới tất cả thiết bị đang mở app của User"""
        if user_id in self.active_user_connections:
            dead_sockets = []
            for ws in self.active_user_connections[user_id]:
                try:
                    await ws.send_text(json.dumps(message, ensure_ascii=False, default=str))
                except Exception:
                    dead_sockets.append(ws)
            for ds in dead_sockets:
                self.disconnect_user(user_id, ds)

    async def send_to_device(self, device_id: str, message: dict):
        """Bắn dữ liệu JSON tới Heo đất hoặc app đang lắng nghe theo device_id"""
        if device_id in self.active_device_connections:
            dead_sockets = []
            for ws in self.active_device_connections[device_id]:
                try:
                    await ws.send_text(json.dumps(message, ensure_ascii=False, default=str))
                except Exception:
                    dead_sockets.append(ws)
    async def broadcast_all(self, message: dict):
        """Phát sóng sự kiện tới TOÀN BỘ các kết nối WebSocket đang online (Dashboard / Mobile Apps)"""
        msg_str = json.dumps(message, ensure_ascii=False, default=str)
        # Broadcast to users
        for uid in list(self.active_user_connections.keys()):
            for ws in list(self.active_user_connections.get(uid, [])):
                try:
                    await ws.send_text(msg_str)
                except Exception:
                    pass
        # Broadcast to devices
        for did in list(self.active_device_connections.keys()):
            for ws in list(self.active_device_connections.get(did, [])):
                try:
                    await ws.send_text(msg_str)
                except Exception:
                    pass


ws_manager = ConnectionManager()
