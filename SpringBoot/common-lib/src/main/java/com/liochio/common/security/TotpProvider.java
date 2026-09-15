package com.liochio.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * ==============================================================================
 * Trình Xử Lý Mã Xác Thực TOTP / SmartOTP (RFC 6238 Standard)
 * ==============================================================================
 * 
 * Mục đích:
 * - Sinh khóa bí mật Base32 ngẫu nhiên (160-bit).
 * - Sinh chuỗi URI Barcode QR Code chuẩn ('otpauth://totp/...').
 * - Xác thực mã 6 số TOTP với cửa sổ thời gian trôi (Time window $\pm 1$ step = 30s).
 */
@Slf4j
@Component
public class TotpProvider {

    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final int TIME_STEP_SECONDS = 30;
    private static final int DIGITS = 6;
    private static final int DIGITS_MODULO = (int) Math.pow(10, DIGITS);
    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Sinh khóa bí mật Base32 ngẫu nhiên 20 bytes (160 bits)
     */
    public String generateBase32Secret() {
        byte[] buffer = new byte[20];
        secureRandom.nextBytes(buffer);
        return encodeBase32(buffer);
    }

    /**
     * Sinh URI chuẩn phục vụ tạo QR code quét trên Google Authenticator / SmartOTP
     */
    public String generateQrBarcodeUri(String secret, String accountName, String issuer) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
                issuer, accountName, secret, issuer, DIGITS, TIME_STEP_SECONDS);
    }

    /**
     * Xác thực mã TOTP 6 số với cửa sổ sai lệch thời gian cho phép (+- 1 bước thời gian)
     */
    public boolean verifyTotpCode(String base32Secret, String totpCode) {
        if (base32Secret == null || totpCode == null || totpCode.trim().length() != DIGITS) {
            return false;
        }

        try {
            int code = Integer.parseInt(totpCode.trim());
            long currentStep = System.currentTimeMillis() / 1000L / TIME_STEP_SECONDS;
            byte[] keyBytes = decodeBase32(base32Secret);

            // Kiểm tra trong khoảng [currentStep - 1, currentStep, currentStep + 1]
            for (long step = currentStep - 1; step <= currentStep + 1; step++) {
                if (generateCodeForStep(keyBytes, step) == code) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.warn("[TotpProvider] Lỗi xác thực TOTP: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Sinh mã TOTP 6 số cho 1 bước thời gian cụ thể
     */
    private int generateCodeForStep(byte[] keyBytes, long step) throws Exception {
        byte[] data = ByteBuffer.allocate(8).putLong(step).array();
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(keyBytes, HMAC_ALGORITHM));
        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);

        return binary % DIGITS_MODULO;
    }

    private String encodeBase32(byte[] data) {
        StringBuilder result = new StringBuilder();
        int buffer = 0;
        int next = 0;
        int bitsLeft = 0;

        while (next < data.length || bitsLeft > 0) {
            if (bitsLeft < 5) {
                if (next < data.length) {
                    buffer <<= 8;
                    buffer |= (data[next++] & 0xFF);
                    bitsLeft += 8;
                } else {
                    int pad = 5 - bitsLeft;
                    buffer <<= pad;
                    bitsLeft += pad;
                }
            }
            int index = 0x1F & (buffer >> (bitsLeft - 5));
            bitsLeft -= 5;
            result.append(BASE32_CHARS.charAt(index));
        }
        return result.toString();
    }

    private byte[] decodeBase32(String base32) {
        String clean = base32.toUpperCase().replaceAll("[^A-Z2-7]", "");
        byte[] result = new byte[clean.length() * 5 / 8];
        int buffer = 0;
        int bitsLeft = 0;
        int count = 0;

        for (int i = 0; i < clean.length(); i++) {
            int val = BASE32_CHARS.indexOf(clean.charAt(i));
            if (val < 0) continue;
            buffer <<= 5;
            buffer |= val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                result[count++] = (byte) (buffer >> (bitsLeft - 8));
                bitsLeft -= 8;
            }
        }
        return Arrays.copyOf(result, count);
    }
}
