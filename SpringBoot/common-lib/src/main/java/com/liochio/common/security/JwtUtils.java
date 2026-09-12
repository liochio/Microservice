package com.liochio.common.security;

import com.liochio.common.constant.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

/**
 * ==============================================================================
 * Tiện Ích Tạo & Giải Mã Mã Xác Thực JWT (JSON Web Token Utility - JJWT 0.12.x)
 * Hỗ trợ Asymmetric RSA RS256 & Symmetric HMAC-SHA256, JWKS & Smart OTP Action Token
 * ==============================================================================
 */
@Slf4j
@Component
public class JwtUtils {

    public static final String KEY_ID = "liochio-core-rsa-key-1";

    private final ResourceLoader resourceLoader;
    private final String secret;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;
    private final String privateKeyLocation;
    private final String publicKeyLocation;

    private SecretKey hmacKey;
    private PrivateKey rsaPrivateKey;
    private PublicKey rsaPublicKey;

    public JwtUtils(
            ResourceLoader resourceLoader,
            @Value("${jwt.secret:" + SecurityConstants.DEFAULT_JWT_SECRET + "}") String secret,
            @Value("${jwt.access-token-validity-ms:" + SecurityConstants.ACCESS_TOKEN_VALIDITY_MS + "}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity-ms:" + SecurityConstants.REFRESH_TOKEN_VALIDITY_MS + "}") long refreshTokenValidity,
            @Value("${jwt.rsa.private-key-path:classpath:certs/rsa-private.pem}") String privateKeyLocation,
            @Value("${jwt.rsa.public-key-path:classpath:certs/rsa-public.pem}") String publicKeyLocation
    ) {
        this.resourceLoader = resourceLoader;
        this.secret = secret;
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
        this.privateKeyLocation = privateKeyLocation;
        this.publicKeyLocation = publicKeyLocation;
    }

    @PostConstruct
    public void init() {
        this.hmacKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        loadRsaKeys();
    }

    private void loadRsaKeys() {
        try {
            Resource privResource = resourceLoader.getResource(privateKeyLocation);
            if (privResource.exists()) {
                try (InputStream is = privResource.getInputStream()) {
                    String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    this.rsaPrivateKey = parsePrivateKey(pem);
                    log.info("[JwtUtils] Loaded RSA Private Key successfully from {}", privateKeyLocation);
                }
            }
        } catch (Exception e) {
            log.warn("[JwtUtils] Could not load RSA Private Key from {}: {}", privateKeyLocation, e.getMessage());
        }

        try {
            Resource pubResource = resourceLoader.getResource(publicKeyLocation);
            if (pubResource.exists()) {
                try (InputStream is = pubResource.getInputStream()) {
                    String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    this.rsaPublicKey = parsePublicKey(pem);
                    log.info("[JwtUtils] Loaded RSA Public Key successfully from {}", publicKeyLocation);
                }
            }
        } catch (Exception e) {
            log.warn("[JwtUtils] Could not load RSA Public Key from {}: {}", publicKeyLocation, e.getMessage());
        }
    }

    private PrivateKey parsePrivateKey(String pem) throws Exception {
        String clean = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] bytes = Base64.getDecoder().decode(clean);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey parsePublicKey(String pem) throws Exception {
        String clean = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] bytes = Base64.getDecoder().decode(clean);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    public boolean isRsaEnabled() {
        return rsaPrivateKey != null && rsaPublicKey != null;
    }

    public PublicKey getRsaPublicKey() {
        return rsaPublicKey;
    }

    /**
     * Xuất thông tin JWKS (JSON Web Key Set) định dạng RFC 7517
     */
    public Map<String, Object> getJwks() {
        if (rsaPublicKey instanceof RSAPublicKey rsa) {
            String n = encodeBase64UrlUnsigned(rsa.getModulus());
            String e = encodeBase64UrlUnsigned(rsa.getPublicExponent());

            Map<String, Object> jwk = new LinkedHashMap<>();
            jwk.put("kty", "RSA");
            jwk.put("use", "sig");
            jwk.put("alg", "RS256");
            jwk.put("kid", KEY_ID);
            jwk.put("n", n);
            jwk.put("e", e);

            return Map.of("keys", List.of(jwk));
        }
        return Map.of("keys", List.of());
    }

    private static String encodeBase64UrlUnsigned(BigInteger bigInt) {
        byte[] array = bigInt.toByteArray();
        if (array.length > 1 && array[0] == 0) {
            byte[] trimmed = new byte[array.length - 1];
            System.arraycopy(array, 1, trimmed, 0, trimmed.length);
            array = trimmed;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(array);
    }

    /**
     * Tạo Access Token mới
     */
    public String generateAccessToken(Long userId, String username, String tenantId, Set<String> roles, Set<String> permissions) {
        return generateAccessToken(userId, username, tenantId, roles, permissions, null, null);
    }

    public String generateAccessToken(Long userId, String username, String tenantId, Set<String> roles, Set<String> permissions, String deviceId, String sessionId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidity);

        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim(SecurityConstants.CLAIM_USER_ID, userId)
                .claim(SecurityConstants.CLAIM_USERNAME, username)
                .claim(SecurityConstants.CLAIM_TENANT_ID, tenantId)
                .claim(SecurityConstants.CLAIM_ROLES, roles)
                .claim(SecurityConstants.CLAIM_PERMISSIONS, permissions)
                .claim(SecurityConstants.CLAIM_TOKEN_TYPE, SecurityConstants.TOKEN_TYPE_ACCESS);

        if (deviceId != null) builder.claim("deviceId", deviceId);
        if (sessionId != null) builder.claim("sessionId", sessionId);

        builder.issuedAt(now).expiration(expiryDate);

        if (isRsaEnabled()) {
            return builder.header().keyId(KEY_ID).and().signWith(rsaPrivateKey, Jwts.SIG.RS256).compact();
        } else {
            return builder.signWith(hmacKey).compact();
        }
    }

    /**
     * Tạo Refresh Token mới
     */
    public String generateRefreshToken(Long userId, String username, String tenantId) {
        return generateRefreshToken(userId, username, tenantId, null, null);
    }

    public String generateRefreshToken(Long userId, String username, String tenantId, String deviceId, String sessionId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenValidity);

        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim(SecurityConstants.CLAIM_USER_ID, userId)
                .claim(SecurityConstants.CLAIM_TENANT_ID, tenantId)
                .claim(SecurityConstants.CLAIM_TOKEN_TYPE, SecurityConstants.TOKEN_TYPE_REFRESH);

        if (deviceId != null) builder.claim("deviceId", deviceId);
        if (sessionId != null) builder.claim("sessionId", sessionId);

        builder.issuedAt(now).expiration(expiryDate);

        if (isRsaEnabled()) {
            return builder.header().keyId(KEY_ID).and().signWith(rsaPrivateKey, Jwts.SIG.RS256).compact();
        } else {
            return builder.signWith(hmacKey).compact();
        }
    }

    /**
     * Tạo Action Token ngắn hạn (5 phút) cho các giao dịch nhạy cảm (Smart OTP)
     */
    public String generateActionToken(Long userId, String username, String tenantId, String actionType, String payloadHash, long ttlSeconds) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (ttlSeconds * 1000));

        var builder = Jwts.builder()
                .subject(username)
                .claim(SecurityConstants.CLAIM_USER_ID, userId)
                .claim(SecurityConstants.CLAIM_USERNAME, username)
                .claim(SecurityConstants.CLAIM_TENANT_ID, tenantId != null ? tenantId : "SYSTEM")
                .claim(SecurityConstants.CLAIM_TOKEN_TYPE, "ACTION")
                .claim("action_type", actionType)
                .claim("payload_hash", payloadHash)
                .issuedAt(now)
                .expiration(expiryDate);

        if (isRsaEnabled()) {
            return builder.header().keyId(KEY_ID).and().signWith(rsaPrivateKey, Jwts.SIG.RS256).compact();
        } else {
            return builder.signWith(hmacKey).compact();
        }
    }

    /**
     * Giải mã và lấy Claims từ Token (hỗ trợ cả RS256 và HMAC)
     */
    public Claims extractClaims(String token) {
        if (rsaPublicKey != null) {
            try {
                return Jwts.parser()
                        .verifyWith(rsaPublicKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            } catch (Exception e) {
                // Thử dự phòng HMAC nếu parse RS256 không thành công
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

    /**
     * Kiểm tra Token có hợp lệ và còn hạn sử dụng hay không
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JwtUtils] Token không hợp lệ hoặc đã hết hạn: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object userId = extractClaims(token).get(SecurityConstants.CLAIM_USER_ID);
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    public String extractTenantId(String token) {
        return (String) extractClaims(token).get(SecurityConstants.CLAIM_TENANT_ID);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return (List<String>) extractClaims(token).get(SecurityConstants.CLAIM_ROLES);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        return (List<String>) extractClaims(token).get(SecurityConstants.CLAIM_PERMISSIONS);
    }
}

