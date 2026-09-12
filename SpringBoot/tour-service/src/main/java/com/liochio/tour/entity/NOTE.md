# Package com.liochio.tour.entity

## Mục đích
Chứa các JPA Entity ánh xạ trực tiếp đến các bảng trong database `db_tour`:
- `TourEntity` (`tours`): Thông tin tour cơ bản và giá.
- `TourDestinationEntity` (`tour_destinations`): Điểm tham quan.
- `TourItineraryEntity` (`tour_itineraries`): Chi tiết từng ngày.
- `TourDepartureEntity` (`tour_departures`): Ngày khởi hành và slots.
- `TourBookingEntity` (`tour_bookings`): Đơn đặt tour.
- `TourReviewEntity` (`tour_reviews`): Đánh giá tour.

Tất cả Entity đều kế thừa `BaseEntity` (hỗ trợ auditing `created_at`, `updated_at`, `version` và soft delete `is_deleted`).
