@echo off
chcp 65001 >nul
echo ========================================================
echo [1/3] TAO CAY THU MUC VA NOTE.MD CHO TUNG MODULE...
echo ========================================================

mkdir .github\workflows 2>nul
mkdir docker\scripts 2>nul
mkdir docs\architecture 2>nul

mkdir common-lib\src\main\java\com\liochio\common\annotation 2>nul
mkdir common-lib\src\main\java\com\liochio\common\config 2>nul
mkdir common-lib\src\main\java\com\liochio\common\constant 2>nul
mkdir common-lib\src\main\java\com\liochio\common\context 2>nul
mkdir common-lib\src\main\java\com\liochio\common\dto 2>nul
mkdir common-lib\src\main\java\com\liochio\common\entity 2>nul
mkdir common-lib\src\main\java\com\liochio\common\enums 2>nul
mkdir common-lib\src\main\java\com\liochio\common\exception 2>nul
mkdir common-lib\src\main\java\com\liochio\common\filter 2>nul
mkdir common-lib\src\main\java\com\liochio\common\i18n 2>nul
mkdir common-lib\src\main\java\com\liochio\common\middleware 2>nul
mkdir common-lib\src\main\java\com\liochio\common\outbox 2>nul
mkdir common-lib\src\main\java\com\liochio\common\pattern\template 2>nul
mkdir common-lib\src\main\java\com\liochio\common\pattern\factory 2>nul
mkdir common-lib\src\main\java\com\liochio\common\pattern\strategy 2>nul
mkdir common-lib\src\main\java\com\liochio\common\pattern\builder 2>nul
mkdir common-lib\src\main\java\com\liochio\common\repository 2>nul
mkdir common-lib\src\main\java\com\liochio\common\sanitization 2>nul
mkdir common-lib\src\main\java\com\liochio\common\scheduler 2>nul
mkdir common-lib\src\main\java\com\liochio\common\service 2>nul
mkdir common-lib\src\main\resources 2>nul

mkdir api-gateway\src\main\java\com\liochio\gateway\config 2>nul
mkdir api-gateway\src\main\java\com\liochio\gateway\filter 2>nul
mkdir api-gateway\src\main\resources 2>nul

mkdir service-registry\src\main\java\com\liochio\registry 2>nul
mkdir service-registry\src\main\resources 2>nul

mkdir config-server\src\main\java\com\liochio\configserver 2>nul
mkdir config-server\src\main\resources 2>nul

mkdir auth-service\src\main\java\com\liochio\auth\annotation 2>nul
mkdir auth-service\src\main\java\com\liochio\auth\aspect 2>nul
mkdir auth-service\src\main\java\com\liochio\auth\controller 2>nul
mkdir auth-service\src\main\java\com\liochio\auth\entity 2>nul
mkdir auth-service\src\main\java\com\liochio\auth\repository 2>nul
mkdir auth-service\src\main\java\com\liochio\auth\service 2>nul
mkdir auth-service\src\main\resources\db\migration 2>nul

mkdir entity-service\src\main\java\com\liochio\entityservice\client\fallback 2>nul
mkdir entity-service\src\main\java\com\liochio\entityservice\controller 2>nul
mkdir entity-service\src\main\java\com\liochio\entityservice\entity 2>nul
mkdir entity-service\src\main\java\com\liochio\entityservice\repository 2>nul
mkdir entity-service\src\main\java\com\liochio\entityservice\service 2>nul
mkdir entity-service\src\main\resources\db\migration 2>nul
mkdir entity-service\src\test\java\com\liochio\entityservice\architecture 2>nul
mkdir entity-service\src\test\java\com\liochio\entityservice\integration 2>nul

mkdir media-service\src\main\java\com\liochio\media\controller 2>nul
mkdir media-service\src\main\java\com\liochio\media\dto 2>nul
mkdir media-service\src\main\java\com\liochio\media\service 2>nul
mkdir media-service\src\main\resources 2>nul

mkdir notification-service\src\main\java\com\liochio\notification\adapter 2>nul
mkdir notification-service\src\main\java\com\liochio\notification\consumer 2>nul
mkdir notification-service\src\main\java\com\liochio\notification\service 2>nul
mkdir notification-service\src\main\java\com\liochio\notification\websocket 2>nul
mkdir notification-service\src\main\resources 2>nul

mkdir payment-service\src\main\java\com\liochio\payment\adapter 2>nul
mkdir payment-service\src\main\java\com\liochio\payment\controller 2>nul
mkdir payment-service\src\main\java\com\liochio\payment\service 2>nul
mkdir payment-service\src\main\resources 2>nul

(
echo # Module common-lib
echo ## 1. Vai trò cốt lõi
echo - Thư viện dùng chung cho toàn bộ hệ thống Microservices.
echo - DTO chuẩn hóa, Global Exception Handler, Data Sanitizer, Audit Log Filter, BaseEntity Auditing, ShedLock.
) > "common-lib\NOTE.md"

(
echo # Service api-gateway ^(Port 8080^)
echo ## 1. Vai trò cốt lõi
echo - Cổng Gateway định tuyến API, Auth Filter, Rate Limiting, CORS.
) > "api-gateway\NOTE.md"

(
echo # Service service-registry ^(Port 8761^)
echo ## 1. Vai trò cốt lõi
echo - Eureka Discovery Server quản lý service cluster.
) > "service-registry\NOTE.md"

(
echo # Service config-server ^(Port 8888^)
echo ## 1. Vai trò cốt lõi
echo - Quản lý tập trung toàn bộ file cấu hình hệ thống.
) > "config-server\NOTE.md"

(
echo # Service auth-service ^(Port 8081^)
echo ## 1. Vai trò cốt lõi
echo - Quản lý User, JWT, Dynamic RBAC, Multi-tenancy context.
) > "auth-service\NOTE.md"

(
echo # Service entity-service ^(Port 8082^)
echo ## 1. Vai trò cốt lõi
echo - Dynamic Engine, PostgreSQL JSONB, Server-Driven UI layout.
) > "entity-service\NOTE.md"

(
echo # Service media-service ^(Port 8083^)
echo ## 1. Vai trò cốt lõi
echo - Chunk Upload, gộp byte stream, Storage Adapter CDN/S3.
) > "media-service\NOTE.md"

(
echo # Service notification-service ^(Port 8084^)
echo ## 1. Vai trò cốt lõi
echo - Hub email, SMS, Telegram Webhook, WebSocket STOMP.
) > "notification-service\NOTE.md"

(
echo # Service payment-service ^(Port 8085^)
echo ## 1. Vai trò cốt lõi
echo - Strategy cổng thanh toán VNPay, Stripe, MoMo và IPN Webhook.
) > "payment-service\NOTE.md"

echo ========================================================
echo [2/3] TAO POM.XML CHO TUNG MODULE CON (4.1.0)...
echo ========================================================

(
echo ^<?xml version="1.0" encoding="UTF-8"?^>
echo ^<project xmlns="http://maven.apache.org/POM/4.0.0"
echo          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
echo          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"^>
echo     ^<modelVersion^>4.0.0^</modelVersion^>
echo     ^<parent^>
echo         ^<groupId^>com.liochio^</groupId^>
echo         ^<artifactId^>liochio-microservices^</artifactId^>
echo         ^<version^>1.0.0-SNAPSHOT^</version^>
echo     ^</parent^>
echo     ^<artifactId^>common-lib^</artifactId^>
echo     ^<packaging^>jar^</packaging^>
echo     ^<dependencies^>
echo         ^<dependency^>
echo             ^<groupId^>org.springframework.boot^</groupId^>
echo             ^<artifactId^>spring-boot-starter-web^</artifactId^>
echo         ^</dependency^>
echo         ^<dependency^>
echo             ^<groupId^>org.springframework.boot^</groupId^>
echo             ^<artifactId^>spring-boot-starter-validation^</artifactId^>
echo         ^</dependency^>
echo         ^<dependency^>
echo             ^<groupId^>org.springframework.boot^</groupId^>
echo             ^<artifactId^>spring-boot-starter-data-jpa^</artifactId^>
echo         ^</dependency^>
echo         ^<dependency^>
echo             ^<groupId^>org.springframework.boot^</groupId^>
echo             ^<artifactId^>spring-boot-starter-data-redis^</artifactId^>
echo         ^</dependency^>
echo         ^<dependency^>
echo             ^<groupId^>org.springframework.boot^</groupId^>
echo             ^<artifactId^>spring-boot-starter-security^</artifactId^>
echo         ^</dependency^>
echo         ^<dependency^>
echo             ^<groupId^>org.springframework.boot^</groupId^>
echo             ^<artifactId^>spring-boot-starter-aop^</artifactId^>
echo         ^</dependency^>
echo         ^<dependency^>
echo             ^<groupId^>com.github.ben-manes.caffeine^</groupId^>
echo             ^<artifactId^>caffeine^</artifactId^>
echo         ^</dependency^>
echo         ^<dependency^>
echo             ^<groupId^>org.projectlombok^</groupId^>
echo             ^<artifactId^>lombok^</artifactId^>
echo             ^<optional^>true^</optional^>
echo         ^</dependency^>
echo     ^</dependencies^>
echo ^</project^>
) > "common-lib\pom.xml"

(
echo ^<?xml version="1.0" encoding="UTF-8"?^>
echo ^<project xmlns="http://maven.apache.org/POM/4.0.0"
echo          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
echo          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"^>
echo     ^<modelVersion^>4.0.0^</modelVersion^>
echo     ^<parent^>
echo         ^<groupId^>com.liochio^</groupId^>
echo         ^<artifactId^>liochio-microservices^</artifactId^>
echo         ^<version^>1.0.0-SNAPSHOT^</version^>
echo     ^</parent^>
echo     ^<artifactId^>service-registry^</artifactId^>
echo     ^<packaging^>jar^</packaging^>
echo     ^<dependencies^>
echo         ^<dependency^>
echo             ^<groupId^>org.springframework.cloud^</groupId^>
echo             ^<artifactId^>spring-cloud-starter-netflix-eureka-server^</artifactId^>
echo         ^</dependency^>
echo     ^</dependencies^>
echo     ^<build^>
echo         ^<plugins^>
echo             ^<plugin^>
echo                 ^<groupId^>org.springframework.boot^</groupId^>
echo                 ^<artifactId^>spring-boot-maven-plugin^</artifactId^>
echo             ^</plugin^>
echo         ^</plugins^>
echo     ^</build^>
echo ^</project^>
) > "service-registry\pom.xml"

(
echo ^<?xml version="1.0" encoding="UTF-8"?^>
echo ^<project xmlns="http://maven.apache.org/POM/4.0.0"
echo          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
echo          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"^>
echo     ^<modelVersion^>4.0.0^</modelVersion^>
echo     ^<parent^>
echo         ^<groupId^>com.liochio^</groupId^>
echo         ^<artifactId^>liochio-microservices^</artifactId^>
echo         ^<version^>1.0.0-SNAPSHOT^</version^>
echo     ^</parent^>
echo     ^<artifactId^>config-server^</artifactId^>
echo     ^<packaging^>jar^</packaging^>
echo     ^<dependencies^>
echo         ^<dependency^>
echo             ^<groupId^>org.springframework.cloud^</groupId^>
echo             ^<artifactId^>spring-cloud-config-server^</artifactId^>
echo         ^</dependency^>
echo     ^</dependencies^>
echo     ^<build^>
echo         ^<plugins^>
echo             ^<plugin^>
echo                 ^<groupId^>org.springframework.boot^</groupId^>
echo                 ^<artifactId^>spring-boot-maven-plugin^</artifactId^>
echo             ^</plugin^>
echo         ^</plugins^>
echo     ^</build^>
echo ^</project^>
) > "config-server\pom.xml"

(
echo ^<?xml version="1.0" encoding="UTF-8"?^>
echo ^<project xmlns="http://maven.apache.org/POM/4.0.0"
echo          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
echo          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"^>
echo     ^<modelVersion^>4.0.0^</modelVersion^>
echo     ^<parent^>
echo         ^<groupId^>com.liochio^</groupId^>
echo         ^<artifactId^>liochio-microservices^</artifactId^>
echo         ^<version^>1.0.0-SNAPSHOT^</version^>
echo     ^</parent^>
echo     ^<artifactId^>api-gateway^</artifactId^>
echo     ^<packaging^>jar^</packaging^>
echo     ^<dependencies^>
echo         ^<dependency^>
echo             ^<groupId^>org.springframework.cloud^</groupId^>
echo             ^<artifactId^>spring-cloud-starter-gateway^</artifactId^>
echo         ^</dependency^>
echo         ^<dependency^>
echo             ^<groupId^>org.springframework.cloud^</groupId^>
echo             ^<artifactId^>spring-cloud-starter-netflix-eureka-client^</artifactId^>
echo         ^</dependency^>
echo     ^</dependencies^>
echo     ^<build^>
echo         ^<plugins^>
echo             ^<plugin^>
echo                 ^<groupId^>org.springframework.boot^</groupId^>
echo                 ^<artifactId^>spring-boot-maven-plugin^</artifactId^>
echo             ^</plugin^>
echo         ^</plugins^>
echo     ^</build^>
echo ^</project^>
) > "api-gateway\pom.xml"

for %%M in (auth-service entity-service media-service notification-service payment-service) do (
(
echo ^<?xml version="1.0" encoding="UTF-8"?^>
echo ^<project xmlns="http://maven.apache.org/POM/4.0.0"
echo          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
echo          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"^>
echo     ^<modelVersion^>4.0.0^</modelVersion^>
echo     ^<parent^>
echo         ^<groupId^>com.liochio^</groupId^>
echo         ^<artifactId^>liochio-microservices^</artifactId^>
echo         ^<version^>1.0.0-SNAPSHOT^</version^>
echo     ^</parent^>
echo     ^<artifactId^>%%M^</artifactId^>
echo     ^<packaging^>jar^</packaging^>
echo     ^<dependencies^>
echo         ^<dependency^>
echo             ^<groupId^>com.liochio^</groupId^>
echo             ^<artifactId^>common-lib^</artifactId^>
echo         ^</dependency^>
echo         ^<dependency^>
echo             ^<groupId^>org.springframework.cloud^</groupId^>
echo             ^<artifactId^>spring-cloud-starter-netflix-eureka-client^</artifactId^>
echo         ^</dependency^>
echo         ^<dependency^>
echo             ^<groupId^>org.postgresql^</groupId^>
echo             ^<artifactId^>postgresql^</artifactId^>
echo             ^<scope^>runtime^</scope^>
echo         ^</dependency^>
echo         ^<dependency^>
echo             ^<groupId^>org.springframework.boot^</groupId^>
echo             ^<artifactId^>spring-boot-starter-test^</artifactId^>
echo             ^<scope^>test^</scope^>
echo         ^</dependency^>
echo     ^</dependencies^>
echo     ^<build^>
echo         ^<plugins^>
echo             ^<plugin^>
echo                 ^<groupId^>org.springframework.boot^</groupId^>
echo                 ^<artifactId^>spring-boot-maven-plugin^</artifactId^>
echo             ^</plugin^>
echo         ^</plugins^>
echo     ^</build^>
echo ^</project^>
) > "%%M\pom.xml"
)

echo ========================================================
echo [3/3] TAO POM.XML GOC (SPRING BOOT 4.1.0 + JAVA 21)...
echo ========================================================

(
echo ^<?xml version="1.0" encoding="UTF-8"?^>
echo ^<project xmlns="http://maven.apache.org/POM/4.0.0"
echo          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
echo          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"^>
echo     ^<modelVersion^>4.0.0^</modelVersion^>
echo.
echo     ^<groupId^>com.liochio^</groupId^>
echo     ^<artifactId^>liochio-microservices^</artifactId^>
echo     ^<version^>1.0.0-SNAPSHOT^</version^>
echo     ^<packaging^>pom^</packaging^>
echo     ^<name^>Liochio Microservices Ecosystem^</name^>
echo.
echo     ^<modules^>
echo         ^<module^>common-lib^</module^>
echo         ^<module^>service-registry^</module^>
echo         ^<module^>config-server^</module^>
echo         ^<module^>api-gateway^</module^>
echo         ^<module^>auth-service^</module^>
echo         ^<module^>entity-service^</module^>
echo         ^<module^>notification-service^</module^>
echo         ^<module^>payment-service^</module^>
echo         ^<module^>ai-service^</module^>
echo         ^<module^>realtime-service^</module^>
echo         ^<module^>otp-service^</module^>
echo         ^<module^>worker-service^</module^>
echo     ^</modules^>
echo.
echo     ^<properties^>
echo         ^<java.version^>21^</java.version^>
echo         ^<maven.compiler.source^>21^</maven.compiler.source^>
echo         ^<maven.compiler.target^>21^</maven.compiler.target^>
echo         ^<project.build.sourceEncoding^>UTF-8^</project.build.sourceEncoding^>
echo         ^<project.reporting.outputEncoding^>UTF-8^</project.reporting.outputEncoding^>
echo.
echo         ^<spring-boot.version^>4.1.0^</spring-boot.version^>
echo         ^<spring-cloud.version^>2024.0.0^</spring-cloud.version^>
echo         ^<spring-ai.version^>1.0.0-M5^</spring-ai.version^>
echo         ^<shedlock.version^>6.2.0^</shedlock.version^>
echo         ^<resilience4j.version^>2.2.0^</resilience4j.version^>
echo         ^<springdoc.version^>2.8.0^</springdoc.version^>
echo         ^<mapstruct.version^>1.6.3^</mapstruct.version^>
echo         ^<lombok.version^>1.18.36^</lombok.version^>
echo         ^<lombok-mapstruct-binding.version^>0.2.0^</lombok-mapstruct-binding.version^>
echo         ^<testcontainers.version^>1.20.4^</testcontainers.version^>
echo         ^<jjwt.version^>0.12.6^</jjwt.version^>
echo     ^</properties^>
echo.
echo     ^<dependencyManagement^>
echo         ^<dependencies^>
echo             ^<dependency^>
echo                 ^<groupId^>org.springframework.boot^</groupId^>
echo                 ^<artifactId^>spring-boot-dependencies^</artifactId^>
echo                 ^<version^>${spring-boot.version}^</version^>
echo                 ^<type^>pom^</type^>
echo                 ^<scope^>import^</scope^>
echo             ^</dependency^>
echo             ^<dependency^>
echo                 ^<groupId^>org.springframework.cloud^</groupId^>
echo                 ^<artifactId^>spring-cloud-dependencies^</artifactId^>
echo                 ^<version^>${spring-cloud.version}^</version^>
echo                 ^<type^>pom^</type^>
echo                 ^<scope^>import^</scope^>
echo             ^</dependency^>
echo             ^<dependency^>
echo                 ^<groupId^>org.springframework.ai^</groupId^>
echo                 ^<artifactId^>spring-ai-bom^</artifactId^>
echo                 ^<version^>${spring-ai.version}^</version^>
echo                 ^<type^>pom^</type^>
echo                 ^<scope^>import^</scope^>
echo             ^</dependency^>
echo             ^<dependency^>
echo                 ^<groupId^>com.liochio^</groupId^>
echo                 ^<artifactId^>common-lib^</artifactId^>
echo                 ^<version^>${project.version}^</version^>
echo             ^</dependency^>
echo         ^</dependencies^>
echo     ^</dependencyManagement^>
echo.
echo     ^<repositories^>
echo         ^<repository^>
echo             ^<id^>spring-milestones^</id^>
echo             ^<name^>Spring Milestones^</name^>
echo             ^<url^>https://repo.spring.io/milestone^</url^>
echo             ^<snapshots^>
echo                 ^<enabled^>false^</enabled^>
echo             ^</snapshots^>
echo         ^</repository^>
echo     ^</repositories^>
echo.
echo     ^<build^>
echo         ^<pluginManagement^>
echo             ^<plugins^>
echo                 ^<plugin^>
echo                     ^<groupId^>org.springframework.boot^</groupId^>
echo                     ^<artifactId^>spring-boot-maven-plugin^</artifactId^>
echo                     ^<version^>${spring-boot.version}^</version^>
echo                 ^</plugin^>
echo             ^</plugins^>
echo         ^</pluginManagement^>
echo     ^</build^>
echo ^</project^>
) > "pom.xml"

echo ========================================================
echo [HOAN TAT] DA DONG BO HOAN TOAN SPRING BOOT 4.1.0!
echo ========================================================
pause