# Package com.liochio.tour.repository & com.liochio.tour.service

## Repository
- `TourRepository`: Spring Data JPA interface cho bảng `tours`.
- `TourDepartureRepository`: Quản lý ngày khởi hành và tính khả dụng của số chỗ trống.

## Service
- `TourService` & `TourServiceImpl`: Xử lý toàn bộ logic nghiệp vụ (Tạo tour, cập nhật lịch trình, kiểm tra số chỗ, quản lý đánh giá, tính giá động theo ngày).
