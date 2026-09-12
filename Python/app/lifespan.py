from contextlib import asynccontextmanager
from fastapi import FastAPI

@asynccontextmanager
async def app_lifespan(app: FastAPI):
    # STARTUP: Khởi động toàn bộ kết nối tài nguyên lớn
    # Connect MySQL pool, Redis cluster, Kafka broker, MQTT client, Preload AI model
    yield
    # SHUTDOWN: Thu hồi và ngắt sạch tài nguyên an toàn