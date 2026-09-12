# D:\UIT - HK2\FinanceProject\app\schemas\responses\wallet.py

from pydantic import BaseModel, Field

class WalletResponse(BaseModel):
    success: bool = Field(..., description="Trạng thái phản hồi giao dịch")
    error_code: str = Field(..., description="Mã số lỗi quy chuẩn hệ thống (Key để Middleware tra DB)")
    message: str = Field(default="", description="Thông báo hệ thống (Sẽ bị Middleware ghi đè)")
    data: dict = Field(..., description="Dữ liệu nghiệm thu sạch trả về")

class WalletListResponse(BaseModel):
    success: bool = Field(..., description="Trạng thái phản hồi")
    error_code: str = Field(..., description="Mã số lỗi quy chuẩn hệ thống")
    message: str = Field(default="", description="Thông báo hệ thống (Sẽ bị Middleware ghi đè)")
    data: list = Field(..., description="Danh sách dữ liệu ví trả về")