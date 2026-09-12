# TỔNG QUAN KIẾN TRÚC ENTERPRISE PORTFOLIO BACKEND ENGINE (META-ENGINE PLATFORM)

> **Phiên bản:** 2.0.0-ENTERPRISE  
> **Kiến trúc:** Spring Cloud Microservices & Hierarchical Multi-Tenancy Meta-Engine  
> **Mô hình Dữ liệu:** Database Core (Hạ tầng dùng chung) + Database-per-Domain (Chủ đề chuyên biệt: Tour, Music, Film, Gaming, Blog, AI, EAV)  
> **Nền tảng Kỹ thuật:** Java 17 LTS, Spring Boot 3.4.3, Spring Cloud 2024.0.0, MySQL 8.0, Redis 7, Flyway Migration  

---

## 1. QUY HOẠCH TOÀN BỘ HỆ THỐNG 12 MICROSERVICES

Hệ sinh thái gồm **12 Microservices** độc lập (16 Maven Modules) được chia làm 3 nhóm dịch vụ:

```
                                 ┌─────────────────────────────────┐
                                 │   Config Server (Port 8888)     │
                                 └───────────────┬─────────────────┘
                                                 │
┌────────────────────────────────────────────────┼────────────────────────────────────────────────┐
│                                                ▼                                                │
│                                ┌─────────────────────────────────┐                              │
│                                │ Service Registry (Eureka: 8761) │                              │
│                                └───────────────▲─────────────────┘                              │
│                                                │                                                │
│ ┌──────────────────────────────────────────────┴──────────────────────────────────────────────┐ │
│ │                                  API Gateway (Port 8080)                                    │ │
│ └──────┬────────────┬────────────┬─────────────┬─────────────┬────────────┬─────────────┬─────┘ │
└────────┼────────────┼────────────┼─────────────┼─────────────┼────────────┼─────────────┼───────┘
         │            │            │             │             │            │             │
   [DỊCH VỤ CORE DÙNG CHUNG]       │             │   [DỊCH VỤ CHỦ ĐỀ CHUYÊN BIỆT]         │
         │            │            │             │             │            │             │
         ▼            ▼            ▼             ▼             ▼            ▼             ▼
   ┌──────────┐ ┌──────────┐ ┌──────────┐  ┌──────────┐  ┌──────────┐ ┌──────────┐ ┌──────────┐
   │   auth   │ │ payment  │ │  media   │  │  entity  │  │   tour   │ │  music   │ │   film   │
   │ service  │ │ service  │ │ service  │  │ service  │  │ service  │ │ service  │ │ service  │
   │  (8081)  │ │  (8085)  │ │  (8083)  │  │  (8082)  │  │  (8091)  │ │  (8092)  │ │  (8093)  │
   └────┬─────┘ └────┬─────┘ └────┬─────┘  └────┬─────┘  └────┬─────┘ └────┬─────┘ └────┬─────┘
        │            │            │             │             │            │             │
        └────────────┴─────┬──────┴─────────────┴─────────────┴────────────┴─────────────┘
                           │
                           ▼
          ┌───────────────────────────────────┐
          │     DỊCH VỤ BỔ TRỢ & VẬN HÀNH      │
          │ ├─ notification-service (8084)    │
          │ ├─ ai-service (8086)              │
          │ ├─ realtime-service (8087)        │
          │ └─ worker-service (Non-Web Worker)│
          └───────────────────────────────────┘
```

---

## 2. BẢNG PHÂN BỔ SERVICE VÀ CƠ SỞ DỮ LIỆU TƯƠNG ỨNG

| Nhóm Dịch Vụ | Tên Service | Port | Database Phụ Trách | Nhiệm Vụ & Bảng Nghiệp Vụ Chính |
| :--- | :--- | :---: | :--- | :--- |
| **Hạ tầng cốt lõi** | `service-registry` | 8761 | *Không có* | Eureka Service Discovery Server |
| | `config-server` | 8888 | Git / Local Config | Quản lý cấu hình tập trung |
| | `api-gateway` | 8080 | Redis Cache/Limiter | Định tuyến, Rate Limiting, Check Blacklist IP, Dynamic CORS |
| **Core dùng chung** | `auth-service` | 8081 | `portfolio-engine` (`db_core`) | Quản trị `tenants`, `users`, `roles`, `permissions`, `user_devices`, `user_sessions`, `user_otp_verifications`, `security_login_histories` |
| | `payment-service` | 8085 | `portfolio-engine` (`db_core`) | Giao dịch: `bookings`, `tenant_payment_configs`, `payment_transactions`, `idempotency_keys` |
| | `media-service` | 8083 | `portfolio-engine` (`db_core`) + CDN | Quản lý tải tệp: `media_assets`, `media_chunk_uploads` |
| | `notification-service` | 8084 | `portfolio-engine` (`db_core`) | Đa kênh: `tenant_notification_configs`, `notification_templates`, `notifications` |
| **Chủ đề Động (EAV)** | `entity-service` | 8082 | `db_content_eav` (MySQL) | Động cơ EAV đa năng: `dynamic_entities`, `entity_types`, `dynamic_field_definitions`, `ui_configurations`, `navigation_menus`, `form_definitions`, `i18n_dictionaries` |
| **Chủ đề Chuyên biệt** | `tour-service` | 8091 | `db_tour` (MySQL) | Du lịch: `tours`, `tour_itineraries`, `tour_departures`, `tour_destinations`, `tour_reviews` |
| | `music-service` | 8092 | `db_music` (MySQL) | Âm nhạc: `artists`, `albums`, `songs`, `playlists`, `track_reviews` |
| | `film-service` | 8093 | `db_film` (MySQL) | Điện ảnh: `movies`, `movie_episodes`, `movie_genres`, `streaming_servers` |
| **AI & Realtime** | `ai-service` | 8086 | `db_ai_vector` (MySQL) | Trí tuệ nhân tạo: `tenant_ai_configs`, `ai_knowledge_base`, `ai_chat_sessions`, `ai_chat_messages` |
| | `realtime-service` | 8087 | Redis Pub/Sub | WebSocket STOMP Hub, Token Streaming Chatbot, In-App Push |
| | `worker-service` | Non-Web | `portfolio-engine` (`db_core`) | Outbox Polling Worker, ShedLock Distributed Lock, DB Backup Scheduler, Dọn rác Media |

---

## 3. CẤU HÌNH ĐỊNH TUYẾN TOÀN DIỆN TRÊN API GATEWAY

```yaml
spring:
  cloud:
    gateway:
      routes:
        # 1. Auth Service Routes
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/v1/auth/**, /api/v1/users/**, /api/v1/tenants/**, /api/auth/**, /api/users/**, /api/roles/**, /api/tenants/**

        # 2. Payment Service Routes
        - id: payment-service
          uri: lb://payment-service
          predicates:
            - Path=/api/v1/payments/**, /api/v1/bookings/**, /api/payments/**, /api/bookings/**

        # 3. Media Service Routes
        - id: media-service
          uri: lb://media-service
          predicates:
            - Path=/api/v1/media/**, /api/media/**

        # 4. Notification Service Routes
        - id: notification-service
          uri: lb://notification-service
          predicates:
            - Path=/api/v1/notifications/**, /api/notifications/**

        # 5. Entity / Dynamic Engine Routes
        - id: entity-service
          uri: lb://entity-service
          predicates:
            - Path=/api/v1/entities/**, /api/v1/portfolios/**, /api/v1/ui-configs/**, /api/v1/forms/**, /api/v1/menus/**, /api/entities/**, /api/portfolios/**, /api/ui-configs/**, /api/forms/**, /api/menus/**

        # 6. Tour Service
        - id: tour-service
          uri: lb://tour-service
          predicates:
            - Path=/api/v1/tours/**, /api/tours/**

        # 7. Music Service
        - id: music-service
          uri: lb://music-service
          predicates:
            - Path=/api/v1/music/**, /api/music/**

        # 8. Film Service
        - id: film-service
          uri: lb://film-service
          predicates:
            - Path=/api/v1/films/**, /api/films/**

        # 9. AI Service
        - id: ai-service
          uri: lb://ai-service
          predicates:
            - Path=/api/v1/ai/**, /api/ai/**

        # 10. Realtime WebSocket Gateway
        - id: realtime-service
          uri: lb:ws://realtime-service
          predicates:
            - Path=/ws/**, /ws-notification/**
```
