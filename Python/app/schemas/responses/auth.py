from pydantic import BaseModel, Field

class UserRegisterResponse(BaseModel):
    success: bool = Field(..., description="Trạng thái phản hồi giao dịch thành công hay thất bại")
    error_code: str = Field(..., description="Mã số lỗi quy chuẩn hệ thống")
    message: str = Field(..., description="Thông báo hệ thống (Sẽ bị Middleware ghi đè sau khi dịch từ DB)")
    data: dict = Field(..., description="Dữ liệu nghiệm thu sạch trả về sau khi đăng ký thành công")


class UserLoginResponse(BaseModel):
    """👑 RESPONSE ĐẦU RA ĐĂNG NHẬP CHUẨN DOANH NGHIỆP"""
    success: bool = Field(..., description="Trạng thái phản hồi giao dịch đăng nhập thành công hay thất bại")
    error_code: str = Field(..., description="Mã số lỗi quy chuẩn của phiên đăng nhập")
    message: str = Field(..., description="Thông báo hệ thống đã dịch đa ngôn ngữ")
    data: dict = Field(..., description="Dữ liệu cặp bài trùng Token phát hành (access_token, refresh_token...)")