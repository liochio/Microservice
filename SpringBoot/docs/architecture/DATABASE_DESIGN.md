# THIẾT KẾ CƠ SỞ DỮ LIỆU TỔNG THỂ (ENTERPRISE DATABASE ARCHITECTURE)

> **Hệ Quản Trị CSDL:** MySQL 8.0 / MySQL 8.4 LTS  
> **Bộ mã & Đối chiếu:** 'utf8mb4_unicode_ci' / 'utf8mb4_0900_ai_ci'  
> **Kiến trúc dữ liệu:** Database-per-Domain Pattern (1 Database Core + 7 Database Chuyên Biệt)  
> **Tổng số bảng:** 85 Bảng nghiệp vụ phân bổ qua 8 Database độc lập  

---

## 1. SƠ ĐỒ THỰC THỂ LIÊN KẾT TỔNG THỂ (ERD MERMAID DIAGRAM)

'''mermaid
erDiagram
    %% Core Infrastructure Database
    tenants ||--o{ users : "owns"
    tenants ||--o{ tenant_features : "subscribes"
    system_features ||--o{ tenant_features : "catalog"
    users ||--o{ user_roles : "assigned"
    roles ||--o{ user_roles : "maps"
    roles ||--o{ role_permissions : "contains"
    permissions ||--o{ role_permissions : "defines"
    
    users ||--o{ user_devices : "trusts"
    users ||--o{ user_sessions : "maintains"
    users ||--o{ user_otp_verifications : "verifies"
    users ||--o{ security_login_histories : "logs"

    tenants ||--o{ payment_orders : "receives"
    payment_orders ||--o{ payment_transactions : "generates"
    tenants ||--o{ tenant_payment_configs : "integrates"

    tenants ||--o{ media_assets : "manages"
    tenants ||--o{ media_chunk_uploads : "tracks"

    tenants ||--o{ notifications : "sends"
    users ||--o{ notifications : "receives"
    tenants ||--o{ tenant_notification_configs : "configures"

    %% Dynamic Content & EAV Database
    tenants ||--o{ dynamic_entities : "stores"
    entity_types ||--o{ dynamic_entities : "categorizes"
    entity_types ||--o{ dynamic_field_definitions : "defines fields"
    dynamic_entities ||--o{ dynamic_entities : "parent/child"

    tenants ||--o{ ui_configurations : "configures"
    tenants ||--o{ navigation_menus : "renders"
    tenants ||--o{ form_definitions : "renders"

    %% Domain DBs: Tour, Music, Film, AI
    tenants ||--o{ tours : "offers"
    tours ||--o{ tour_departures : "schedules"
    tours ||--o{ tour_itineraries : "plans"
    tours ||--o{ tour_bookings : "books"

    tenants ||--o{ artists : "features"
    artists ||--o{ albums : "releases"
    artists ||--o{ songs : "performs"
    albums ||--o{ songs : "contains"

    tenants ||--o{ movies : "streams"
    movies ||--o{ movie_episodes : "has"

    tenants ||--o{ tenant_ai_configs : "configures"
    tenants ||--o{ ai_knowledge_base : "indexes"
    tenants ||--o{ ai_chat_sessions : "hosts"
    ai_chat_sessions ||--o{ ai_chat_messages : "contains"
'''

---

## 2. DANH MỤC 85 BẢNG THEO 8 DATABASE ĐỘC LẬP

### 🏢 1. Database Core IAM: 'liochio_core_db'
1. 'tenants': Khách thuê cá nhân / doanh nghiệp.
2. 'system_features': Danh mục tính năng hệ sinh thái.
3. 'tenant_features': Gói tính năng đăng ký theo Tenant.
4. 'users': Tài khoản người dùng đa cấp.
5. 'permissions': Danh mục quyền hạt nhân ('module:action').
6. 'roles': Nhóm vai trò quản trị.
7. 'role_permissions': Ánh xạ Role - Permission.
8. 'user_roles': Gán vai trò cho User.
9. 'user_otp_verifications': Mã OTP / SmartOTP 2FA.
10. 'user_devices': Thiết bị tin cậy.
11. 'user_sessions': Phiên đăng nhập hoạt động.
12. 'security_login_histories': Lịch sử đăng nhập và bảo mật.
13. 'user_tokens': Token xác thực và thu hồi.
14. 'payment_orders': Đơn hàng thanh toán tổng quát.
15. 'payment_transactions': Nhật ký giao dịch IPN (VNPay, MoMo).
16. 'tenant_payment_configs': Khóa API cổng thanh toán.
17. 'media_assets': Metadata tệp lưu trữ CDN/R2.
18. 'media_chunk_uploads': Theo dõi upload phân mảnh file lớn.
19. 'media_files': Quản lý tập tin chi tiết.
20. 'tenant_notification_configs': Cấu hình Email, Telegram, SMS.
21. 'notification_templates': Mẫu thông báo đa ngữ.
22. 'notifications': Hàng đợi phát tán thông báo.
23. 'audit_logs': Ghi vết 100% Request/Response kèm TraceId.
24. 'outbox_events': Transactional Outbox Pattern.
25. 'idempotency_keys': Khóa chống trùng lặp request.
26. 'shedlock': Khóa phân tán điều phối Scheduler đa node.
27. 'input_sanitization_rules': Regex lọc mã độc XSS/HTML.
28. 'system_error_codes': Danh mục mã lỗi nghiệp vụ số nguyên.
29. 'flyway_schema_history': Nhật ký migration Core.
30. 'flyway_schema_history_auth': Nhật ký migration Auth.
31. 'flyway_schema_history_media': Nhật ký migration Media.
32. 'flyway_schema_history_notification': Nhật ký migration Notification.
33. 'flyway_schema_history_payment': Nhật ký migration Payment.
34. 'flyway_schema_history_entity': Nhật ký migration Entity.

---

### 🌐 2. Database Động Cơ EAV & SDUI: 'db_content_eav' (11 bảng)
1. 'entity_types': Định nghĩa loại thực thể động.
2. 'dynamic_field_definitions': Thuộc tính động của thực thể.
3. 'dynamic_entities': Lưu trữ thực thể dạng Hybrid EAV + JSON.
4. 'form_definitions': Biểu mẫu động JSON Schema.
5. 'ui_configurations': Server-Driven UI layout blocks.
6. 'navigation_menus': Cây menu điều hướng đa tầng.
7. 'i18n_dictionaries': Từ điển bản dịch động.
8. 'web_templates': Mẫu website giao diện động.
9. 'portfolio_items': Danh mục dự án trình diễn.
10. 'entity_reviews': Đánh giá và bình luận thực thể.
11. 'external_api_integrations': Tích hợp API bên thứ 3.

---

### ✈️ 3. Database Du Lịch: 'db_tour' (8 bảng)
1. 'tours': Thông tin tour, giá gốc, hình ảnh.
2. 'tour_destinations': Điểm đến trong tour.
3. 'tour_itineraries': Lịch trình chi tiết từng ngày.
4. 'tour_departures': Lịch khởi hành và số chỗ.
5. 'tour_bookings': Đơn đặt tour của khách hàng.
6. 'tour_reviews': Đánh giá chấm điểm tour.
7. 'tour_guides': Hồ sơ hướng dẫn viên.
8. 'tour_pricing_tiers': Gói giá nâng cao (VIP, Standard).

---

### 🎵 4. Database Âm Nhạc: 'db_music' (7 bảng)
1. 'artists': Hồ sơ nghệ sĩ/ca sĩ.
2. 'music_genres': Thể loại âm nhạc.
3. 'albums': Album đĩa nhạc phát hành.
4. 'songs': Bài hát, link stream, lời bài hát LRC.
5. 'playlists': Danh sách phát người dùng.
6. 'playlist_songs': Bài hát trong playlist.
7. 'track_reviews': Đánh giá bài hát.

---

### 🎬 5. Database Điện Ảnh: 'db_film' (7 bảng)
1. 'movies': Danh mục phim và thông số.
2. 'movie_genres': Thể loại phim.
3. 'movie_genres_mapping': Ánh xạ phim - thể loại.
4. 'movie_episodes': Danh sách tập phim và link stream.
5. 'streaming_servers': Máy chủ phát video CDN.
6. 'movie_casts': Diễn viên và nhân vật.
7. 'movie_reviews': Đánh giá phim.

---

### 🎮 6. Database Gaming: 'db_gaming' (7 bảng)
1. 'games': Danh mục tựa game.
2. 'game_heroes': Nhân vật và tướng trong game.
3. 'game_servers': Máy chủ game và số người online.
4. 'game_items_shop': Cửa hàng vật phẩm game.
5. 'game_rankings': Bảng xếp hạng người chơi.
6. 'guilds': Bang hội / Clan.
7. 'guild_members': Thành viên bang hội.

---

### 📰 7. Database Blog: 'db_blog' (5 bảng)
1. 'blog_categories': Chuyên mục bài viết.
2. 'blog_tags': Thẻ gắn bài viết.
3. 'blog_articles': Chi tiết bài viết và tác giả.
4. 'blog_article_tags': Ánh xạ bài viết - thẻ.
5. 'blog_comments': Bình luận dưới bài viết.

---

### 🤖 8. Database Trợ Lý AI & Vector: 'db_ai_vector' (6 bảng)
1. 'tenant_ai_configs': Cấu hình LLM theo Tenant.
2. 'ai_chat_sessions': Phiên hội thoại với AI bot.
3. 'ai_chat_messages': Tin nhắn hội thoại và token.
4. 'ai_knowledge_base': Cơ sở tri thức tài liệu (RAG).
5. 'ai_vector_embeddings': Vector embedding 1536 chiều.
6. 'ai_prompts': Thư viện mẫu câu nhắc (Prompt Templates).

---

## 3. CHIẾN LƯỢC ĐÁNH INDEX VÀ TỐI ƯU TRUY VẤN
- **Multi-Tenant Indexing**: 100% bảng nghiệp vụ được đánh Index kép '(tenant_id, ...)' để đảm bảo truy vấn phạm vi hẹp tốc độ cao.
- **Soft Delete Filter**: Đánh Index '(tenant_id, is_deleted, status)' tương thích với Hibernate 6 '@SQLRestriction("is_deleted = false")'.
- **Loosely Coupled References**: Sử dụng 'tenant_id' và 'created_by' làm khóa liên kết logic giữa các Database độc lập.
