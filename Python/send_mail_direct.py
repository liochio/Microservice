import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

user = 'voduylebt99@gmail.com'
pwd = 'uaennreskbytgkjt'
to_email = 'liochioj3@gmail.com'

msg = MIMEMultipart('alternative')
msg['Subject'] = '[Liochio FinTech] Ma Kich Hoat Tai Khoan LiochioJ3'
msg['From'] = user
msg['To'] = to_email

html = """
<div style="font-family: Arial, sans-serif; padding: 20px; background: #0f172a; color: #e2e8f0; border-radius: 12px;">
    <h2 style="color: #10b981;">Chao mung LiochioJ3 den voi Liochio FinTech!</h2>
    <p>Tai khoan cua ban da duoc khoi tao thanh cong o trang thai cho kich hoat.</p>
    <div style="padding: 15px; background: #1e293b; border-radius: 8px; margin: 20px 0; text-align: center;">
        <span style="font-size: 14px; color: #94a3b8;">Ma OTP Kich Hoat:</span><br/>
        <strong style="font-size: 32px; color: #34d399; letter-spacing: 5px;">886699</strong>
    </div>
    <p>Ban co the nhap ma OTP truc tiep tai: <a href="http://localhost:5173/verify-otp?username=LiochioJ3" style="color: #38bdf8;">Kich Hoat Ngay</a></p>
    <p style="font-size: 12px; color: #64748b;">Lien ket co hieu luc trong 15 phut.</p>
</div>
"""
msg.attach(MIMEText(html, 'html', 'utf-8'))

with smtplib.SMTP('smtp.gmail.com', 587, timeout=15) as server:
    server.starttls()
    server.login(user, pwd)
    server.sendmail(user, to_email, msg.as_string())
    print('SUCCESS: Email sent to ' + to_email)
