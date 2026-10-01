import os
import sys
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

# Thêm thư mục gốc vào path để import app modules
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from app.db.session import SessionLocal
from app.jobs.notification_worker import NotificationWorker

db = SessionLocal()
try:
    target_email = sys.argv[1] if len(sys.argv) > 1 else 'abc@gmail.com'
    otp_code = sys.argv[2] if len(sys.argv) > 2 else '886699'
    username = sys.argv[3] if len(sys.argv) > 3 else 'LiochioUser'

    subject = f"[Liochio FinTech] Ma Kich Hoat Tai Khoan {username}"
    html = f"""
    <div style="font-family: Arial, sans-serif; padding: 20px; background: #0f172a; color: #e2e8f0; border-radius: 12px;">
        <h2 style="color: #10b981;">Chào mừng {username} đến với Liochio FinTech!</h2>
        <p>Tài khoản của bạn đã được khởi tạo thành công ở trạng thái chờ kích hoạt.</p>
        <div style="padding: 15px; background: #1e293b; border-radius: 8px; margin: 20px 0; text-align: center;">
            <span style="font-size: 14px; color: #94a3b8;">Mã OTP Kích Hoạt (Hiệu lực 5 phút):</span><br/>
            <strong style="font-size: 32px; color: #34d399; letter-spacing: 5px;">{otp_code}</strong>
        </div>
        <p>Bạn có thể nhập mã OTP trực tiếp tại: <a href="http://localhost:5173/verify-otp?username={username}" style="color: #38bdf8;">Kích Hoạt Ngay</a></p>
        <p style="font-size: 12px; color: #64748b;">Nếu email của bạn là email test (vd: abc@gmail.com), hệ thống tự động định tuyến đến email mặc định cấu hình trong DB.</p>
    </div>
    """

    res = NotificationWorker.send_email_via_smtp(
        to_email=target_email,
        subject=subject,
        html_content=html,
        db_conn=db
    )
    print(f"KẾT QUẢ GỬI EMAIL: {res}")
finally:
    db.close()
