
from app.constants import SystemConstants
from app.repositories.notification.notification_repository import NotificationRepository
from app.core.exceptions.base_exception import FintechBaseException


class NotificationService:

    @staticmethod
    def create_notification(db_conn, user_id: str, template_code: str, context_data: dict) -> str:
        """📦 KHỞI TẠO THÔNG BÁO VÀO HÀNG CHỜ QUA TẦNG REPOSITORY"""
        # Gọi xuống tầng Repo lấy cấu hình template mẫu
        template = NotificationRepository.get_template_by_code(db_conn, template_code)

        if not template:
            # Mã lỗi hệ thống 100% nếu thiếu cấu hình template mẫu
            raise FintechBaseException(error_code=SystemConstants.DATABASE_CONNECTION_FAILED, status_code=500)

        title, body_template, channels = template[0], template[1], template[2]

        # Thực hiện trộn dữ liệu ngữ cảnh Context động vào mẫu body_template
        final_body = body_template
        for key, value in context_data.items():
            final_body = final_body.replace(f"{{{key}}}", str(value))

        # Gọi xuống tầng Repo để thực thi insert hàng chờ dữ liệu vào bảng notifications
        NotificationRepository.insert_notification(
            db_conn=db_conn,
            user_id=user_id,
            template_code=template_code,
            title=title,
            body=final_body
        )

        return template_code