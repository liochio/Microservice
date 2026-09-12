# Film Service - Dịch Vụ Điện Ảnh, Phim & Video Streaming

## 1. Giới thiệu tổng quan
`film-service` là Microservice chuyên biệt phụ trách toàn bộ nghiệp vụ ngành Điện ảnh, Phim, Lịch chiếu, Diễn viên và Streaming Server theo mô hình **Database-per-Domain**.

- **Cổng chạy mặc định**: `8093` (`http://localhost:8093`)
- **Định tuyến qua Gateway**: `/api/v1/films/**`, `/api/films/**`
- **Cơ sở dữ liệu độc lập**: `db_film` (MySQL 8.0)

## 2. Danh sách bảng cơ sở dữ liệu (`db_film`)
1. `movies`: Thông tin phim (tên gốc, thời lượng, năm phát hành, đạo diễn, dàn cast JSON, poster, banner, trailer, đánh giá IMDb, độ tuổi).
2. `movie_genres`: Thể loại phim (Hành động, Hài hước, Tình cảm, Viễn tưởng...).
3. `movie_genres_mapping`: Ánh xạ nhiều - nhiều giữa phim và thể loại.
4. `movie_episodes`: Danh sách tập phim (cho phim bộ hoặc video HD, phụ đề JSON, chất lượng stream).
5. `streaming_servers`: Danh sách máy chủ CDN phát video phục vụ cân bằng tải playback.
6. `movie_casts`: Thông tin diễn viên và nhân vật đóng trong phim.
7. `movie_reviews`: Bình luận và chấm điểm phim từ người xem.

## 3. Kiến trúc phát video
- **Multi-tenancy**: Gắn chặt với `tenant_id` từ Token/Context.
- **Video Storage**: File video dung lượng lớn được phân mảnh upload qua `media-service` (Chunk Upload) và phát qua HLS/MP4 CDN link lưu tại `db_film`.
