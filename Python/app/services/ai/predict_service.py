import math
import statistics
from sqlalchemy import text
from app.core.logging.logger import DBLogger


class PredictService:
    """
    👑 REAL AI FINTECH SECURITY ENGINE (MỤC THỨ 13)
    🎯 Thuật toán toán học thống kê Z-Score đo độ lệch chuẩn lịch sử chi tiêu thực tế để bắt đứng gian lận tài chính.
    """

    @staticmethod
    def detect_transaction_anomaly(db_conn, user_id: str, current_amount: float) -> dict:
        """
        🧠 THUẬT TOÁN TOÁN HỌC RESEARCH QUALITY QUÉT GIAN LẬN CHI TIÊU ĐỘT BIẾN
        """
        try:
            # 1. Truy vấn bốc lịch sử 30 giao dịch gần nhất của User làm tập mẫu dataset huấn luyện động
            query = text("""
                         SELECT amount
                         FROM transactions t
                                  JOIN wallets w ON t.source_wallet_id = w.id
                         WHERE w.user_id = :u_id
                         ORDER BY t.created_at DESC
                         LIMIT 30
                         """)
            rows = db_conn.execute(query, {"u_id": user_id}).fetchall()

            # 2. Tài khoản mới tinh chưa tích lũy đủ tập mẫu dữ liệu ➡️ Bypass an toàn, tránh báo động giả (False Positive)
            if len(rows) < 5:
                return {
                    "is_anomaly": False,
                    "confidence_score": 1.0,
                    "reason": "New user profile signature initialization bypass."
                }

            # Ép kiểu dữ liệu về mảng float phẳng
            amounts = [float(row[0]) for row in rows]

            # 3. Tính toán các chỉ số lõi thống kê: Mean (Trung bình) và Standard Deviation (Độ lệch chuẩn)
            mean = statistics.mean(amounts)
            std_dev = statistics.stdev(amounts) if len(amounts) > 1 else 0.01

            if std_dev == 0:
                std_dev = 0.01  # Rào chắn vật lý tránh lỗi chia cho số 0 trong toán học

            # 4. Đo đạc khoảng cách điểm biến thiên Z-Score của giao dịch hiện tại
            z_score = abs(current_amount - mean) / std_dev

            # Tiêu chuẩn an ninh ngân hàng: Điểm Z-Score vượt ngưỡng 3.0 chứng minh giao dịch mang tính biến dị 99.7%
            is_anomaly = bool(z_score > 3.0)
            confidence_score = float(min(1.0, 1.0 / (z_score + 0.1)))

            if is_anomaly:
                # Ép trút ngay vết cảnh báo an ninh cấp độ nguy cấp vật lý vào bảng security_logs (Mục thứ 4)
                DBLogger.security(
                    db_conn=db_conn,
                    user_id=user_id,
                    event_type="AI_FRAUD_ALARM_TRIGGERED",
                    severity="CRITICAL",
                    ip_address="127.0.0.1",
                    details=f"Mô hình AI phát hiện giao dịch đột biến! Số tiền {current_amount} có điểm độ lệch Z-Score={z_score:.2f} cao nguy hiểm."
                )
                return {
                    "is_anomaly": True,
                    "confidence_score": confidence_score,
                    "reason": f"Hành vi chi tiêu bất thường vượt ngưỡng. Số tiền lớn gấp {current_amount / mean:.1f} lần mức chi tiêu trung bình lịch sử."
                }

            return {
                "is_anomaly": False,
                "confidence_score": confidence_score,
                "reason": "Transaction signature matches statistical distribution patterns."
            }

        except Exception as e:
            # Nguyên tắc core banking: Lỗi phân tích AI không được phép làm nghẽn/chặn đứng luồng xử lý giao dịch lõi chính của khách hàng
            print(f"[AI_MODEL_PREDICT_ERROR] Fallback safe execution activated: {str(e)}")
            return {
                "is_anomaly": False,
                "confidence_score": 0.0,
                "reason": "AI model analysis fallback execution."
            }