from sqlalchemy import select
from app.core.exceptions.base_exception import FintechBaseException

class GenericValidator:
    @staticmethod
    def check_duplicate(db_conn, model_class, field_name: str, value: any, error_code: str):
        """
        Validator tập trung: Truy vấn trực tiếp DB để check trùng.
        - model_class: class của Model.
        - field_name: tên thuộc tính cột trong Model.
        - value: giá trị cần kiểm tra.
        - error_code: mã lỗi trả về nếu trùng.
        """
        # Kiểm tra xem thuộc tính có tồn tại trong Model không
        field = getattr(model_class, field_name, None)
        if field is None:
            raise AttributeError(f"Cột {field_name} không tồn tại trong model {model_class.__name__}")

        # Thực hiện query kiểm tra trùng
        stmt = select(model_class).where(field == value).limit(1)
        result = db_conn.execute(stmt).scalar()

        # Nếu tìm thấy kết quả -> Có trùng lặp -> Báo lỗi 409
        if result:
            raise FintechBaseException(error_code=error_code, status_code=409)

        return True