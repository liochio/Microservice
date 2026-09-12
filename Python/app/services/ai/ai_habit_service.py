# 📄 Đường dẫn file: app/services/ai/ai_habit_service.py
import math
import statistics
from datetime import datetime, timedelta
from typing import List, Dict, Any, Optional
import logging

logger = logging.getLogger(__name__)


class AIHabitTrackerService:
    """
    🧠 AI ROBO-ADVISOR: HỌC THÓI QUEN NẠP TIỀN & NHẮC NHỞ LINH HOẠT
    🎯 Thuật toán: Gaussian Kernel Density Estimation (KDE - Ước lượng mật độ hạt nhân).
       Học phân phối xác suất khoảng cách giữa các lần nạp tiền của từng người dùng,
       tự động tính ngưỡng phân vị 90% để đưa ra cảnh báo cá nhân hóa mà không dùng Cron cứng nhắc.
    """

    def __init__(self, bandwidth: float = 2.0):
        self.bandwidth = bandwidth  # Độ rộng dải (Bandwidth) h = 2.0 ngày

    def _gaussian_kernel(self, u: float) -> float:
        """Hàm nhân Gaussian chuẩn: K(u) = (1 / sqrt(2*pi)) * exp(-0.5 * u^2)"""
        return (1.0 / math.sqrt(2.0 * math.pi)) * math.exp(-0.5 * (u ** 2))

    def _kde_density(self, x: float, data_points: List[float]) -> float:
        """Hàm ước lượng mật độ xác suất f(x) tại điểm x"""
        n = len(data_points)
        if n == 0:
            return 0.0
        h = self.bandwidth
        total_sum = sum(self._gaussian_kernel((x - xi) / h) for xi in data_points)
        return total_sum / (n * h)

    def extract_features(self, transactions: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """
        Tiền xử lý dữ liệu: Tính số ngày cách biệt (Interval) giữa các lần nạp tiền liên tiếp.
        """
        if len(transactions) < 3:
            return []

        # Sắp xếp theo ngày tăng dần
        sorted_txs = sorted(
            transactions,
            key=lambda t: t.get("created_at") if isinstance(t.get("created_at"), datetime) else datetime.fromisoformat(str(t.get("created_at")))
        )

        intervals = []
        for i in range(1, len(sorted_txs)):
            prev_time = sorted_txs[i - 1].get("created_at")
            if not isinstance(prev_time, datetime):
                prev_time = datetime.fromisoformat(str(prev_time))

            curr_time = sorted_txs[i].get("created_at")
            if not isinstance(curr_time, datetime):
                curr_time = datetime.fromisoformat(str(curr_time))

            delta_days = max(1, (curr_time - prev_time).days)
            intervals.append({
                "interval_days": float(delta_days),
                "amount": float(sorted_txs[i].get("amount", 0.0)),
                "date": curr_time
            })

        return intervals

    def train_user_habit(self, user_id: str, transactions: List[Dict[str, Any]]) -> Optional[Dict[str, Any]]:
        """
        Huấn luyện mô hình tìm thói quen nạp tiền của một user.
        """
        features = self.extract_features(transactions)
        
        # Nếu chưa đủ 3 giao dịch thực tế, tạo mẫu phân phối điển hình
        if not features:
            data_points = [3.0, 4.0, 5.0, 7.0, 4.0, 6.0, 5.0]
            avg_amount = 100000.0
            median_interval = 5.0
        else:
            data_points = [f["interval_days"] for f in features]
            amounts = [f["amount"] for f in features]
            avg_amount = sum(amounts) / len(amounts) if amounts else 100000.0
            median_interval = statistics.median(data_points)

        # Tính mật độ xác suất và tích phân lũy kế từ ngày 1 đến 30
        grid_days = [float(d) for d in range(1, 31)]
        densities = [self._kde_density(d, data_points) for d in grid_days]
        
        total_density = sum(densities) if sum(densities) > 0 else 1.0
        normalized_densities = [round(d / total_density, 4) for d in densities]
        
        # Tìm ngưỡng phân vị 90% (90th percentile)
        cumulative = 0.0
        anomaly_threshold_days = 10.0
        for idx, p in enumerate(normalized_densities):
            cumulative += p
            if cumulative >= 0.90:
                anomaly_threshold_days = grid_days[idx]
                break

        return {
            "user_id": user_id,
            "sample_size": len(data_points),
            "average_deposit_amount": round(avg_amount, 2),
            "median_interval_days": round(median_interval, 1),
            "anomaly_threshold_days": round(anomaly_threshold_days, 1),
            "bandwidth_param": self.bandwidth,
            "kde_probability_curve": [
                {"day": d, "probability": p} for d, p in zip(grid_days[:15], normalized_densities[:15])
            ]
        }

    def check_and_alert(self, user_id: str, last_deposit_date: datetime, habit_profile: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        """
        Kiểm tra xem hôm nay có cần gửi thông báo nhắc nhở nạp tiền linh hoạt hay không.
        """
        days_since_last_deposit = (datetime.now() - last_deposit_date).days
        threshold = habit_profile.get("anomaly_threshold_days", 10.0)

        if days_since_last_deposit >= threshold:
            avg_amt = habit_profile.get("average_deposit_amount", 100000.0)
            median_days = habit_profile.get("median_interval_days", 5.0)
            return {
                "user_id": user_id,
                "should_alert": True,
                "alert_type": "MISSED_HABIT_ADVISOR",
                "days_since_last_deposit": days_since_last_deposit,
                "anomaly_threshold_days": threshold,
                "message": (
                    f"Đã {days_since_last_deposit} ngày bạn chưa nạp tiền vào Heo đất. "
                    f"Dựa trên AI thói quen, bạn thường nạp sau mỗi {median_days} ngày. "
                    f"Hãy tích lũy ~{avg_amt:,.0f} VND để duy trì kỷ luật nhé!"
                ),
                "suggested_amount": avg_amt,
                "timestamp": datetime.now().isoformat()
            }

        return {
            "user_id": user_id,
            "should_alert": False,
            "days_since_last_deposit": days_since_last_deposit,
            "anomaly_threshold_days": threshold,
            "message": "Thói quen nạp tiền vẫn đang ở trong vùng an toàn.",
            "timestamp": datetime.now().isoformat()
        }


ai_habit_service = AIHabitTrackerService()
