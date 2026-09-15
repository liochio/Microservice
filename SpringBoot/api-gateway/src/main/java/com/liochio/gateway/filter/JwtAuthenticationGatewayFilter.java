package com.liochio.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * ==============================================================================
 * Bộ Lọc Xác Thực JWT & Blacklist Cốt Lõi Tại Gateway (Enterprise JWT & Security Filter)
 * ==============================================================================
 * 
 * Mục đích:
 * 1. Xác thực Asymmetric RS256 JWT qua RSA Public Key mà không phụ thuộc HTTP sang Auth Service.
 * 2. Kiểm tra tức thì Redis L2 Cache cho Token Blacklist / Session Revocation.
 * 3. Header Sanitization: Xóa 100% header nội bộ do Client giả mạo (Anti-Spoofing).
 * 4. Nạp lại downstream các header sạch đã kiểm chứng: 'X-User-ID', 'X-Username', 'X-Tenant-ID', 'X-User-Roles',...
 */
@Slf4j
@Component
public class JwtAuthenticationGatewayFilter implements GlobalFilter, Ordered {

    public static final String KEY_ID = "liochio-core-rsa-key-1";

    private final ResourceLoader resourceLoader;
    private final String secret;
    private final String publicKeyLocation;
    private final ReactiveStringRedisTemplate reactiveRedisTemplate;

    private SecretKey hmacKey;
    private PublicKey rsaPublicKey;

    private static final List<String> SENSITIVE_INTERNAL_HEADERS = List.of(
            "X-User-ID",
            "X-Username",
            "X-Tenant-ID",
            "X-User-Roles",
            "X-User-Permissions",
            "X-Device-Id",
            "X-Session-ID",
            "X-User-Type",
            "X-Internal-Call"
    );

    private static final List<String> WHITELIST_PATHS = List.of(
            "/.well-known/",
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/verify-otp",
            "/api/auth/verify-device-otp",
            "/api/auth/refresh",
            "/api/auth/qr/init",
            "/api/auth/qr/exchange",
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/verify-otp",
            "/api/v1/auth/verify-device-otp",
            "/api/v1/auth/refresh",
            "/api/v1/auth/qr/init",
            "/api/v1/auth/qr/exchange",
            "/api/v1/otp/",
            "/api/otp/",
            "/api/v1/system/",
            "/api/system/",
            "/api/public/",
            "/v3/api-docs",
            "/swagger-ui",
            "/actuator",
            "/ws/",
            "/api/payments/webhook",
            "/api/payments/ipn",
            "/api/v1/worker/alerts",
            "/api/v1/worker/partitions",
            "/api/v1/ledger/m2m",
            "/api/ledger/m2m",
            "/api/v1/smart-piggy/drop-money",
            "/api/v1/smart-piggy/sync",
            "/api/v1/smart-piggy/sync-offline-batch",
            "/api/v1/iot/"
    );

    public JwtAuthenticationGatewayFilter(
            ResourceLoader resourceLoader,
            @Value("${jwt.secret:liochio-super-secret-jwt-key-minimum-256-bits-for-security-2026}") String secret,
            @Value("${jwt.rsa.public-key-path:classpath:certs/rsa-public.pem}") String publicKeyLocation,
            @Autowired(required = false) ReactiveStringRedisTemplate reactiveRedisTemplate
    ) {
        this.resourceLoader = resourceLoader;
        this.secret = secret;
        this.publicKeyLocation = publicKeyLocation;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    @PostConstruct
    public void init() {
        this.hmacKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        loadRsaPublicKey();
    }

    private void loadRsaPublicKey() {
        try {
            Resource pubResource = resourceLoader.getResource(publicKeyLocation);
            if (pubResource.exists()) {
                try (InputStream is = pubResource.getInputStream()) {
                    String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    String clean = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                            .replace("-----END PUBLIC KEY-----", "")
                            .replaceAll("\\s+", "");
                    byte[] bytes = Base64.getDecoder().decode(clean);
                    X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
                    this.rsaPublicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
                    log.info("[GatewayAuthFilter] Loaded RSA Public Key successfully from {}", publicKeyLocation);
                }
            }
        } catch (Exception e) {
            log.warn("[GatewayAuthFilter] Could not load RSA Public Key from {}: {}", publicKeyLocation, e.getMessage());
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. Header Sanitization: Xóa bỏ mọi Header nội bộ do Client gửi lên
        ServerHttpRequest.Builder sanitizedBuilder = request.mutate();
        for (String headerName : SENSITIVE_INTERNAL_HEADERS) {
            sanitizedBuilder.headers(headers -> headers.remove(headerName));
        }

        // 2. Bỏ qua các URL thuộc Whitelist
        for (String whitelist : WHITELIST_PATHS) {
            if (path.startsWith(whitelist) || path.contains(whitelist)) {
                return chain.filter(exchange.mutate().request(sanitizedBuilder.build()).build());
            }
        }

        // 3. Kiểm tra Header Authorization
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[GatewayAuthFilter] Missing or invalid Authorization header at path: {}", path);
            return onError(exchange, "Chưa gửi Authorization Bearer Token", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7).trim();

        // 4. Giải mã Token (Hỗ trợ RS256 và HMAC Fallback)
        Claims claims;
        try {
            claims = extractClaims(token);
            if (claims.getExpiration() != null && claims.getExpiration().before(new Date())) {
                return onError(exchange, "Token xác thực đã hết hạn", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            log.warn("[GatewayAuthFilter] Token signature invalid or expired at path {}: {}", path, e.getMessage());
            return onError(exchange, "Token xác thực không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED);
        }

        Object userId = claims.get("userId");
        String username = claims.getSubject();
        String tokenTenantId = (String) claims.get("tenantId");
        Object roles = claims.get("roles");
        Object permissions = claims.get("permissions");
        Object deviceId = claims.get("deviceId");
        Object sessionId = claims.get("sessionId");

        // 5. Kiểm tra Token Blacklist trên Reactive Redis
        Mono<Boolean> isBlacklistedMono = checkTokenBlacklist(token, userId != null ? String.valueOf(userId) : null);

        return isBlacklistedMono.flatMap(blacklisted -> {
            if (Boolean.TRUE.equals(blacklisted)) {
                log.warn("[GatewayAuthFilter] Blocked blacklisted token/user (userId={}) at path: {}", userId, path);
                return onError(exchange, "Token đã bị thu hồi hoặc tài khoản đã đăng xuất (Token Revoked / Blacklisted)", HttpStatus.UNAUTHORIZED);
            }

            // 6. Nạp lại Header nội bộ sạch từ Token Claims
            ServerHttpRequest.Builder validatedBuilder = sanitizedBuilder;
            if (userId != null) validatedBuilder.header("X-User-ID", String.valueOf(userId));
            if (username != null) validatedBuilder.header("X-Username", username);
            if (tokenTenantId != null) validatedBuilder.header("X-Tenant-ID", tokenTenantId);
            if (roles != null) validatedBuilder.header("X-User-Roles", roles.toString());
            if (permissions != null) validatedBuilder.header("X-User-Permissions", permissions.toString());
            if (deviceId != null) validatedBuilder.header("X-Device-Id", String.valueOf(deviceId));
            if (sessionId != null) validatedBuilder.header("X-Session-ID", String.valueOf(sessionId));

            return chain.filter(exchange.mutate().request(validatedBuilder.build()).build());
        });
    }

    private Claims extractClaims(String token) {
        if (rsaPublicKey != null) {
            try {
                return Jwts.parser()
                        .verifyWith(rsaPublicKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            } catch (Exception e) {
                if (hmacKey != null) {
                    try {
                        return Jwts.parser()
                                .verifyWith(hmacKey)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload();
                    } catch (Exception ignored) {}
                }
                throw e;
            }
        }
        return Jwts.parser()
                .verifyWith(hmacKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Mono<Boolean> checkTokenBlacklist(String token, String userId) {
        if (reactiveRedisTemplate == null) {
            return Mono.just(false);
        }
        String tokenKey = "blacklist:token:" + token;
        Mono<Boolean> tokenCheck = reactiveRedisTemplate.hasKey(tokenKey).onErrorReturn(false);

        if (userId != null && !userId.isBlank()) {
            String userKey = "blacklist:user:" + userId;
            return tokenCheck.flatMap(tokenRevoked -> {
                if (Boolean.TRUE.equals(tokenRevoked)) return Mono.just(true);
                return reactiveRedisTemplate.hasKey(userKey).onErrorReturn(false);
            });
        }
        return tokenCheck;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.valueOf("application/json;charset=UTF-8"));
        String jsonError = String.format("{\"status\":%d,\"message\":\"%s\",\"data\":null,\"timestamp\":\"%s\"}",
                httpStatus.value(), err, java.time.Instant.now());
        return response.writeWith(Mono.just(response.bufferFactory().wrap(jsonError.getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public int getOrder() {
        return -10; // Chạy ngay sau TraceId & Origin Filter
    }
}

