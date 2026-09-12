# File: run_worker.py (FULL FILE CHẠY ĐỘC LẬP)
import time
import sys
import os
from apscheduler.schedulers.background import BackgroundScheduler
from app.jobs.notification_worker import execute_notification_cron_job

# Đảm bảo Python nhận diện đúng đường dẫn thư mục dự án
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

if __name__ == "__main__":
    # Khởi tạo Scheduler
    scheduler = BackgroundScheduler()

    # Thiết lập cấu hình: Cứ cách 15 giây gõ lệnh quét hàng chờ PENDING của sếp một lần
    scheduler.add_job(execute_notification_cron_job, 'interval', seconds=15, id='standalone_noti_job')

    # Bấm nút kích hoạt
    scheduler.start()
    print("=" * 60)
    print("[CRON] CỖ MÁY APSCHEDULER CHẠY ĐỘC LẬP ĐÃ LÊN ĐÈN!")
    print("[CRON] Tự động quét hàng chờ bảng notifications mỗi 15 giây...")
    print("=" * 60)

    # Giữ cho luồng chính (Main Thread) không bị tắt để Background Thread chạy vô hạn
    try:
        while True:
            time.sleep(1)
    except (KeyboardInterrupt, SystemExit):
        scheduler.shutdown()
        print("\n[CRON] Đã dừng cỗ máy quét ngầm an toàn.")