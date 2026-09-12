# Tour Service - Dịch Vụ Quản Lý Du Lịch & Trải Nghiệm

## 1. Giới thiệu tổng quan
`tour-service` là Microservice chuyên biệt phụ trách toàn bộ nghiệp vụ ngành Du lịch & Trải nghiệm theo mô hình **Database-per-Domain**.

- **Cổng chạy mặc định**: `8091` (`http://localhost:8091`)
- **Định tuyến qua Gateway**: `/api/v1/tours/**`, `/api/tours/**`
- **Cơ sở dữ liệu độc lập**: `db_tour` (MySQL 8.0)

## 2. Danh sách bảng cơ sở dữ liệu (`db_tour`)
1. `tours`: Quản lý thông tin chính của Tour (mã tour, tiêu đề, lịch trình, giá cơ bản, ảnh bìa, gallery, trạng thái, đánh giá).
2. `tour_destinations`: Điểm đến trong hành trình tour (quốc gia, tỉnh thành, thứ tự hiển thị).
3. `tour_itineraries`: Lịch trình chi tiết theo từng ngày (ngày 1, ngày 2, hoạt động, ăn uống, khách sạn).
4. `tour_departures`: Lịch khởi hành cụ thể (ngày đi, ngày về, giá người lớn/trẻ em, số chỗ còn nhận).
5. `tour_bookings`: Đơn đặt tour của khách hàng (thông tin khách hàng, số lượng, tổng tiền, trạng thái booking/thanh toán).
6. `tour_reviews`: Đánh giá và bình luận về tour từ người dùng.
7. `tour_guides`: Thông tin hướng dẫn viên du lịch (ngôn ngữ, tiểu sử, số điện thoại, đánh giá sao).
8. `tour_pricing_tiers`: Các gói định giá nâng cao (VIP, Tiêu chuẩn, Tiết kiệm).

## 3. Kiến trúc giao tiếp liên Service (Inter-service Communication)
- **Multi-tenancy**: Mọi dữ liệu đều gắn với `tenant_id` lấy từ `TenantContext` hoặc JWT.
- **Thanh toán**: Khi khách hàng đặt tour qua `tour-service`, thông tin thanh toán sẽ được ủy quyền tới `payment-service` thông qua REST/OpenFeign hoặc Event Bus.
- **Media Assets**: Ảnh bìa và ảnh gallery được tải lên qua `media-service` và lưu trữ đường dẫn CDN tại `db_tour`.
