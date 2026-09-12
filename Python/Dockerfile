# 🐳 FinTech Backend Monolith Dockerfile
FROM python:3.11-slim

WORKDIR /code

# Cài đặt các gói phụ thuộc hệ thống cần thiết cho MySQL & Cryptography
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    default-libmysqlclient-dev \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

# Cài đặt dependencies Python
COPY ./requirements.txt /code/requirements.txt
RUN pip install --no-cache-dir --upgrade -r /code/requirements.txt

# Copy mã nguồn ứng dụng, cấu hình Alembic và thư mục đa ngôn ngữ i18n
COPY ./alembic.ini /code/alembic.ini
COPY ./alembic /code/alembic
COPY ./i18n /code/i18n
COPY ./app /code/app

# Mở cổng mạng 8000
EXPOSE 8000

# Lệnh khởi động Uvicorn ASGI Server (sử dụng cú pháp JSON kép chuẩn)
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]