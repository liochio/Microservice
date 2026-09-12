package com.liochio.common.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.liochio.common.constant.CacheConstants;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * ==============================================================================
 * Dịch Vụ Quản Lý Blacklist Token & Thu Hồi Phiên Phân Tán (Token Blacklist Service)
 * ==============================================================================
 * 
 * Mục đích:
 * 1. Thu hồi hiệu lực tức thời của JWT Access Token khi người dùng nhấn Logout.
 * 2. Thu hồi toàn bộ phiên khi Force Logout hoặc phát hiện bất thường (Token Reuse).
 * 3. Bảo vệ 2 tầng: L1 (Caffeine In-Memory) và L2 (Redis Distributed Cache).
 * 4. Tự động kiểm tra tính hợp lệ của Token dựa trên:
 *    - Mã băm SHA-256 của Token trong blacklist:token:<hash>
 *    - Session ID trong blacklist:session:<sessionId>
 *    - Mốc thời gian Force Logout trong blacklist:user:<userId>
 */
@Slf4j
@Service
public class TokenBlacklistService {

    private final Optional<StringRedisTemplate> redisTemplate;
    private final JwtUtils jwtUtils;

    // L1 In-Memory Cache: tối đa 50,000 mục, hết hạn sau 24 giờ để tăng tốc độ lọc
    private final Cache<String, String> localBlacklistCache = Caffeine.newBuilder()
            .maximumSize(50_000)
            .expireAfterWrite(24, TimeUnit.HOURS)
            .build();

    public TokenBlacklistService(
            @Autowired(required = false) StringRedisTemplate redisTemplate,
            JwtUtils jwtUtils
    ) {
        this.redisTemplate = Optional.ofNullable(redisTemplate);
        this.jwtUtils = jwtUtils;
    }

    /**
     * Đưa Access Token vào danh sách đen với TTL bằng thời gian sống còn lại của Token
     */
    public void blacklistToken(String token, Duration ttl, String reason) {
        if (token == null || token.isBlank()) return;
        String tokenHash = hashToken(token);
        String key = CacheConstants.REDIS_PREFIX_TOKEN_BLACKLIST + tokenHash;
        String val = (reason != null && !reason.isBlank()) ? reason : "LOGOUT";

        long seconds = (ttl != null && !ttl.isNegative() && !ttl.isZero()) ? ttl.getSeconds() : 7200;

        localBlacklistCache.put(key, val);

        redisTemplate.ifPresent(rt -> {
            try {
                rt.opsForValue().set(key, val, seconds, TimeUnit.SECONDS);
                log.info("[TokenBlacklist] Đã đưa Token vào Redis Blacklist: hash={}, TTL={}s, reason='{}'", tokenHash, seconds, val);
            } catch (Exception e) {
                log.warn("[TokenBlacklist] Không thể ghi Redis, sử dụng L1 Cache dự phòng: {}", e.getMessage());
            }
        });
    }

    /**
     * Thu hồi Session ID
     */
    public void blacklistSession(String sessionId, Duration ttl, String reason) {
        if (sessionId == null || sessionId.isBlank()) return;
        String key = CacheConstants.REDIS_PREFIX_SESSION_BLACKLIST + sessionId;
        String val = (reason != null && !reason.isBlank()) ? reason : "REVOKED";

        long seconds = (ttl != null && !ttl.isNegative() && !ttl.isZero()) ? ttl.getSeconds() : 604800; // 7 ngày

        localBlacklistCache.put(key, val);

        redisTemplate.ifPresent(rt -> {
            try {
                rt.opsForValue().set(key, val, seconds, TimeUnit.SECONDS);
                log.info("[TokenBlacklist] Đã thu hồi Session ID: {}, TTL={}s, reason='{}'", sessionId, seconds, val);
            } catch (Exception e) {
                log.warn("[TokenBlacklist] Không thể ghi Session vào Redis: {}", e.getMessage());
            }
        });
    }

    /**
     * Thu hồi toàn bộ Token phát hành trước thời điểm hiện tại của một User (Force Logout)
     */
    public void blacklistUser(Long userId, Duration ttl) {
        if (userId == null) return;
        String key = CacheConstants.REDIS_PREFIX_USER_REVOCATION + userId;
        String val = String.valueOf(System.currentTimeMillis());

        long seconds = (ttl != null && !ttl.isNegative() && !ttl.isZero()) ? ttl.getSeconds() : 604800;

        localBlacklistCache.put(key, val);

        redisTemplate.ifPresent(rt -> {
            try {
                rt.opsForValue().set(key, val, seconds, TimeUnit.SECONDS);
                log.info("[TokenBlacklist] Đã kích hoạt Force Logout toàn bộ Token của User ID: {} tại mốc {}", userId, val);
            } catch (Exception e) {
                log.warn("[TokenBlacklist] Không thể ghi User revocation vào Redis: {}", e.getMessage());
            }
        });
    }

    /**
     * Kiểm tra xem Token có bị Blacklist / Thu hồi hay không
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) return false;

        // 1. Kiểm tra mã băm Token trực tiếp
        String tokenHash = hashToken(token);
        String tokenKey = CacheConstants.REDIS_PREFIX_TOKEN_BLACKLIST + tokenHash;

        if (localBlacklistCache.getIfPresent(tokenKey) != null) {
            return true;
        }

        if (redisTemplate.isPresent()) {
            try {
                if (Boolean.TRUE.equals(redisTemplate.get().hasKey(tokenKey))) {
                    localBlacklistCache.put(tokenKey, "LOGOUT");
                    return true;
                }
            } catch (Exception e) {
                log.debug("[TokenBlacklist] Kiểm tra Redis lỗi, tiếp tục: {}", e.getMessage());
            }
        }

        // 2. Trích xuất claims để kiểm tra Session ID và User Revocation Timestamp
        try {
            Claims claims = jwtUtils.extractClaims(token);

            // 2a. Kiểm tra Session ID
            String sessionId = (String) claims.get("sessionId");
            if (sessionId != null && !sessionId.isBlank()) {
                String sessionKey = CacheConstants.REDIS_PREFIX_SESSION_BLACKLIST + sessionId;
                if (localBlacklistCache.getIfPresent(sessionKey) != null) {
                    return true;
                }
                if (redisTemplate.isPresent()) {
                    try {
                        if (Boolean.TRUE.equals(redisTemplate.get().hasKey(sessionKey))) {
                            localBlacklistCache.put(sessionKey, "REVOKED");
                            return true;
                        }
                    } catch (Exception ignored) {}
                }
            }

            // 2b. Kiểm tra User ID Force Logout
            Object userIdObj = claims.get("userId");
            if (userIdObj instanceof Number num) {
                Long userId = num.longValue();
                String userKey = CacheConstants.REDIS_PREFIX_USER_REVOCATION + userId;
                String revokedTimeStr = localBlacklistCache.getIfPresent(userKey);

                if (revokedTimeStr == null && redisTemplate.isPresent()) {
                    try {
                        revokedTimeStr = redisTemplate.get().opsForValue().get(userKey);
                        if (revokedTimeStr != null) {
                            localBlacklistCache.put(userKey, revokedTimeStr);
                        }
                    } catch (Exception ignored) {}
                }

                if (revokedTimeStr != null) {
                    try {
                        long revokedEpochMs = Long.parseLong(revokedTimeStr);
                        Date issuedAt = claims.getIssuedAt();
                        if (issuedAt == null || issuedAt.getTime() <= revokedEpochMs) {
                            log.warn("[TokenBlacklist] Token phát hành ({}) trước mốc Force Logout ({}) của User ID: {}",
                                    issuedAt, revokedEpochMs, userId);
                            return true;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception e) {
            // Token không phân tích được claims hợp lệ -> để jwtUtils xử lý
            return false;
        }

        return false;
    }

    public static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(token.hashCode());
        }
    }
}
