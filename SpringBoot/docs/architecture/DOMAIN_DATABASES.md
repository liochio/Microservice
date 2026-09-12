# QUY HOẠCH CƠ SỞ DỮ LIỆU CHUYÊN BIỆT THEO CHỦ ĐỀ (DATABASE-PER-SERVICE)

> **Mô hình kiến trúc:** Microservices / Database-per-Service  
> **Hệ quản trị CSDL:** MySQL 8.0 / 8.4 LTS  
> **Quy chuẩn đặt tên:** 100% tên Database bắt đầu bằng tiền tố `liochio_*_db`  
> **Nguyên tắc cốt lõi:** Phân tách độc lập hoàn toàn giữa các microservices, tuyệt đối không chia sẻ bảng hay cross-database joins; giao tiếp liên dịch vụ qua OpenFeign / REST / Event Bus.

---

## 1. BẢNG QUY HOẠCH CHUẨN TOÀN BỘ CƠ SỞ DỮ LIỆU `liochio_*_db`

| STT | Tên Database Chuẩn | Microservice / Nền tảng Phụ Trách | Danh Sách Bảng Chính Thức |
| :--- | :--- | :--- | :--- |
| 1 | **`liochio_auth_db`** | `auth-service` (Port 8081 - Java IAM) | `users`, `roles`, `permissions`, `role_permissions`, `user_roles`, `user_devices`, `user_sessions`, `security_login_histories`, `qr_login_sessions`, `tenants`, `system_features`, `tenant_features`, `outbox_events` |
| 2 | **`liochio_fintech_db`** | `liochio-fintech` (Port 8000 - Python Resource Server) | `wallets`, `transactions`, `transfers`, `categories`, `financial_goals`, `budgets`, `smart_piggy_devices`, `parent_matching` |
| 3 | **`liochio_otp_db`** | `otp-service` (Port 8094 - Java) | `otp_service_configs`, `user_otp_verifications` |
| 4 | **`liochio_entity_db`** | `entity-service` (Port 8082 - Java) | `entity_types`, `dynamic_field_definitions`, `dynamic_entities`, `navigation_menus`, `ui_configurations`, `form_definitions`, `i18n_dictionaries`, `entity_reviews`, `external_api_integrations`, `web_templates`, `outbox_events` |
| 5 | **`liochio_media_db`** | `media-service` (Port 8083 - Java) | `media_assets`, `media_chunk_uploads`, `media_files`, `outbox_events` |
| 6 | **`liochio_notification_db`** | `notification-service` (Port 8084 - Java) | `notifications`, `notification_templates`, `tenant_notification_configs`, `outbox_events` |
| 7 | **`liochio_payment_db`** | `payment-service` (Port 8085 - Java) | `payment_orders`, `payment_transactions`, `tenant_payment_configs`, `bookings`, `outbox_events` |
| 8 | **`liochio_tour_db`** | `tour-service` (Port 8086 - Java) | `tours`, `tour_itineraries`, `tour_departures`, `tour_destinations`, `tour_pricing_tiers`, `tour_guides`, `tour_reviews`, `tour_bookings` |
| 9 | **`liochio_music_db`** | `music-service` (Port 8087 - Java) | `artists`, `albums`, `songs`, `playlists`, `playlist_songs`, `music_genres`, `track_reviews` |
| 10 | **`liochio_film_db`** | `film-service` (Port 8088 - Java) | `movies`, `movie_episodes`, `movie_casts`, `movie_genres`, `movie_genres_mapping`, `streaming_servers`, `movie_reviews` |
| 11 | **`liochio_ai_db`** | `ai-service` (Port 8089 - Java) | `tenant_ai_configs`, `ai_prompts`, `ai_knowledge_base`, `ai_vector_embeddings`, `ai_chat_sessions`, `ai_chat_messages` |
| 12 | **`liochio_worker_db`** | `worker-service` (Port 8092 - Java) | `shedlock` |
| 13 | **`liochio_blog_db`** | `blog-service` | `blog_articles`, `blog_categories`, `blog_tags`, `blog_article_tags`, `blog_comments` |
| 14 | **`liochio_gaming_db`** | `gaming-service` | `games`, `game_heroes`, `game_items_shop`, `game_rankings`, `game_servers`, `guilds`, `guild_members` |

---

## 2. KỊCH BẢN DỌN DẸP SCHEMA VÀ BẢNG RÁC

```sql
-- 1. Xóa các database trùng lặp và rác
DROP DATABASE IF EXISTS `portfolio_otp`;
DROP DATABASE IF EXISTS `portfolio-engine`;
DROP DATABASE IF EXISTS `db_ai`;
DROP DATABASE IF EXISTS `db_entity`;

-- 2. Dọn các bảng AI và portfolio cũ trong db_content_eav
USE `db_content_eav`;
DROP TABLE IF EXISTS `ai_chat_messages`;
DROP TABLE IF EXISTS `ai_chat_sessions`;
DROP TABLE IF EXISTS `ai_knowledge_base`;
DROP TABLE IF EXISTS `tenant_ai_configs`;
DROP TABLE IF EXISTS `portfolio_items`;

-- 3. Dọn các bảng rác trong db_auth / db_core
USE `db_auth`;
DROP TABLE IF EXISTS `user_tokens`; -- Đã thống nhất dùng bảng user_sessions
DROP TABLE IF EXISTS `user_otp_verifications`; -- Đã chuyển sang db_otp
```
