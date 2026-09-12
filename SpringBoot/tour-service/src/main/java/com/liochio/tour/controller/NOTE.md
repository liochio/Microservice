# Package com.liochio.tour.controller

## Mục đích
Chứa các REST Controller tiếp nhận request HTTP cho domain Du lịch & Tour:
- `TourController`: CRUD Tour, tìm kiếm theo bộ lọc (điểm đến, khoảng giá, ngày khởi hành), đánh giá và đặt tour.

## Quy chuẩn
- Định tuyến chuẩn RESTful: `/api/v1/tours`
- Bọc kết quả bằng `ApiResponse<T>`
- Validate dữ liệu đầu vào bằng `@Valid`
