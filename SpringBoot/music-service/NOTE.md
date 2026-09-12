# Music Service - Dịch Vụ Âm Nhạc & Audio Streaming

## 1. Giới thiệu tổng quan
`music-service` là Microservice chuyên biệt phụ trách toàn bộ nghiệp vụ ngành Âm nhạc, Nghệ sĩ, Album, Bài hát và Audio Streaming theo mô hình **Database-per-Domain**.

- **Cổng chạy mặc định**: `8092` (`http://localhost:8092`)
- **Định tuyến qua Gateway**: `/api/v1/music/**`, `/api/music/**`
- **Cơ sở dữ liệu độc lập**: `db_music` (MySQL 8.0)

## 2. Danh sách bảng cơ sở dữ liệu (`db_music`)
1. `artists`: Thông tin nghệ sĩ (nghệ danh, tiểu sử, avatar, cover, số lượt nghe hàng tháng, liên kết MXH).
2. `music_genres`: Thể loại âm nhạc (Pop, Rock, Ballad, EDM, Jazz...).
3. `albums`: Album đĩa nhạc (tiêu đề, ngày phát hành, ảnh bìa, tổng số bài hát).
4. `songs`: Chi tiết bài hát (thời lượng, link stream audio, lời bài hát dạng LRC sync, chất lượng 320kbps/Lossless, lượt nghe, lượt thích).
5. `playlists`: Danh sách phát của người dùng hoặc công khai từ hệ thống.
6. `playlist_songs`: Bảng trung gian ánh xạ bài hát vào playlist theo thứ tự phát.
7. `track_reviews`: Đánh giá và bình luận về từng bài hát.

## 3. Kiến trúc giao tiếp liên Service
- **Multi-tenancy**: Gắn chặt với `tenant_id` từ Token/Context.
- **Audio Files**: File âm thanh MP3/FLAC được upload qua `media-service` và lưu đường dẫn CDN `audio_url` tại `db_music`.
