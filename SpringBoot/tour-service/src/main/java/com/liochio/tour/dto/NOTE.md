# Package com.liochio.tour.dto

## Mục đích
Định nghĩa các Data Transfer Objects (DTO) để nhận payload từ Client và trả response ra ngoài:
- `TourCreateRequest`, `TourUpdateRequest`: Payload tạo/cập nhật Tour kèm Validation Annotations.
- `TourResponse`, `TourDetailResponse`: Thông tin Tour công khai kèm danh sách điểm đến và lịch trình.
- `TourSearchFilter`: Bộ lọc tìm kiếm động (điểm đến, ngân sách, số ngày).
