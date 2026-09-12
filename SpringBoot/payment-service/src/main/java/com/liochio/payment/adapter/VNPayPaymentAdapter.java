package com.liochio.payment.adapter;

import com.liochio.common.pattern.strategy.PaymentStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ==============================================================================
 * Bộ Chuyển Đổi Cổng Thanh Toán VNPay (VNPay Payment Adapter)
 * ==============================================================================
 */
@Slf4j
@Component
public class VNPayPaymentAdapter implements PaymentStrategy {

    @Value("${vnpay.tmn-code:DEMO_TMN}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret:SECRET_HASH_KEY}")
    private String vnpHashSecret;

    @Value("${vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpPayUrl;

    @Override
    public String getGatewayName() {
        return "VNPAY";
    }

    @Override
    public String createPaymentUrl(String orderId, BigDecimal amount, String orderInfo, String returnUrl, String ipAddress) {
        try {
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", vnpTmnCode);
            vnpParams.put("vnp_Amount", String.valueOf(amount.multiply(BigDecimal.valueOf(100)).longValue()));
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", orderId);
            vnpParams.put("vnp_OrderInfo", orderInfo != null ? orderInfo : "Thanh toán đơn hàng " + orderId);
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", returnUrl);
            vnpParams.put("vnp_IpAddr", ipAddress != null ? ipAddress : "127.0.0.1");

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            vnpParams.put("vnp_CreateDate", formatter.format(cld.getTime()));
            cld.add(Calendar.MINUTE, 15);
            vnpParams.put("vnp_ExpireDate", formatter.format(cld.getTime()));

            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext(); ) {
                String fieldName = itr.next();
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String vnpSecureHash = hmacSHA512(vnpHashSecret, hashData.toString());
            query.append("&vnp_SecureHash=").append(vnpSecureHash);

            String paymentUrl = vnpPayUrl + "?" + query;
            log.info("[VNPayAdapter] Khởi tạo VNPay URL thành công: {}", paymentUrl);
            return paymentUrl;
        } catch (Exception e) {
            log.error("[VNPayAdapter] Lỗi tạo URL thanh toán VNPay: ", e);
            throw new RuntimeException("Lỗi tạo VNPay URL", e);
        }
    }

    @Override
    public boolean verifyIpnSignature(Map<String, String> params) {
        try {
            String vnpSecureHash = params.get("vnp_SecureHash");
            if (vnpSecureHash == null) return false;

            Map<String, String> fields = new HashMap<>(params);
            fields.remove("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");

            List<String> fieldNames = new ArrayList<>(fields.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext(); ) {
                String fieldName = itr.next();
                String fieldValue = fields.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }

            String signValue = hmacSHA512(vnpHashSecret, hashData.toString());
            boolean isValid = signValue.equalsIgnoreCase(vnpSecureHash);
            log.info("[VNPayAdapter] Kết quả xác thực IPN Signature: {}", isValid);
            return isValid;
        } catch (Exception e) {
            log.error("[VNPayAdapter] Lỗi kiểm tra chữ ký IPN: ", e);
            return false;
        }
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512Hmac.init(secretKey);
            byte[] hash = sha512Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}
