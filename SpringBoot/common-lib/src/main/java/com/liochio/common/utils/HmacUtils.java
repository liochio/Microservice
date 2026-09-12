package com.liochio.common.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * ==============================================================================
 * Tiện Ích Chữ Ký Số HMAC-SHA256 Cho Kênh Truyền Machine-to-Machine (M2M)
 * ==============================================================================
 */
public final class HmacUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private HmacUtils() {}

    public static String sign(String payload, String secretKey) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hmacBytes);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tính toán chữ ký HMAC-SHA256: " + e.getMessage(), e);
        }
    }

    public static boolean verify(String payload, String secretKey, String expectedSignature) {
        if (payload == null || secretKey == null || expectedSignature == null) return false;
        String actualSignature = sign(payload, secretKey);
        return MessageDigest.isEqual(
                actualSignature.toLowerCase().getBytes(StandardCharsets.UTF_8),
                expectedSignature.toLowerCase().getBytes(StandardCharsets.UTF_8)
        );
    }
}
