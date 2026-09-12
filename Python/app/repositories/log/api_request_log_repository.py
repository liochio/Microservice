import json
import traceback
from typing import Any, Optional

from sqlalchemy.engine import Connection
from sqlalchemy.exc import SQLAlchemyError

from app.core.logging.logger import logger
from app.repositories.log.mapping.api_request_log_mapping import ApiRequestLogMappingFactory


class ApiRequestLogRepository:
    """
    👑 REPOSITORY API REQUEST LOGS (FULL ORM)

    🎯 Chốt chặn cuối cùng:
    - Ghi nhật ký API chuẩn ORM
    - Tự động xử lý JSON an toàn
    - Tránh lỗi type mismatch
    - Hạn chế crash payload bất thường
    """

    MAX_PAYLOAD_LENGTH: int = 10000

    @staticmethod
    def _safe_json(data: Any) -> Optional[Any]:
        """
        🛡️ Chuẩn hóa dữ liệu JSON an toàn cho SQLAlchemy JSON column.
        """
        if data is None:
            return None

        if isinstance(data, (dict, list)):
            return data

        if isinstance(data, str):
            trimmed_data = data.strip()

            if not trimmed_data:
                return None

            try:
                parsed_data = json.loads(trimmed_data)

                if isinstance(parsed_data, (dict, list)):
                    return parsed_data

                return {
                    "raw": str(parsed_data)[
                        : ApiRequestLogRepository.MAX_PAYLOAD_LENGTH
                    ]
                }

            except json.JSONDecodeError:
                return {
                    "raw": trimmed_data[
                        : ApiRequestLogRepository.MAX_PAYLOAD_LENGTH
                    ]
                }

        try:
            serialized_data = json.dumps(
                data,
                default=str,
                ensure_ascii=False
            )

            return {
                "raw": serialized_data[
                    : ApiRequestLogRepository.MAX_PAYLOAD_LENGTH
                ]
            }

        except (TypeError, ValueError):
            return {
                "raw": str(data)[
                    : ApiRequestLogRepository.MAX_PAYLOAD_LENGTH
                ]
            }

    @staticmethod
    def insert_api_request_log(
        db_conn: Connection,
        user_id: Optional[str],
        endpoint: str,
        method: str,
        request_payload: Any,
        response_payload: Any,
        status_code: int,
        latency_ms: int,
        status: str
    ) -> None:
        """
        🛡️ Insert API request log bằng cách gọi Factory Mapping ORM.
        """
        print("=========user_id============", user_id)
        try:
            # Luồng làm sạch định dạng JSON được giữ nguyên vẹn logic cũ
            safe_request = ApiRequestLogRepository._safe_json(request_payload)
            safe_response = ApiRequestLogRepository._safe_json(response_payload)

            # 👑 ĐÁNH DẤU CHỈNH SỬA: Repo chỉ gọi duy nhất Statement từ Factory Mapping
            # Toàn bộ cấu trúc câu lệnh SQL, Model, Import bẩn đã được trục xuất khỏi tệp tin này.
            stmt = ApiRequestLogMappingFactory.get_insert_log_stmt(
                user_id=user_id,
                endpoint=endpoint,
                method=method,
                request_payload=safe_request,
                response_payload=safe_response,
                status_code=status_code,
                latency_ms=latency_ms,
                status=status
            )

            db_conn.execute(stmt)
            db_conn.commit()

        except SQLAlchemyError as ex:
            db_conn.rollback()

            logger.error(
                (
                    "[FATAL_LOG_REPO] "
                    "Insert api_request_logs thất bại | "
                    f"endpoint={endpoint} | "
                    f"method={method} | "
                    f"status_code={status_code} | "
                    f"error={str(ex)}\n"
                    f"{traceback.format_exc()}"
                )
            )