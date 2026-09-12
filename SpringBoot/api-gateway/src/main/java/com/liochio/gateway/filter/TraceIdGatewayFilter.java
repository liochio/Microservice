package com.liochio.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * ==============================================================================
 * Bộ Lọc Cloudflare, Real IP & Trace ID Toàn Cục (Cloudflare & Traceability Filter)
 * ==============================================================================
 * 
 * Mục đích:
 * 1. Đồng bộ mã Trace ID xuyên suốt: Trích xuất `CF-Ray` do Cloudflare sinh, làm `X-Trace-ID` và `x-correlation-id`.
 * 2. Nhận diện Real Client IP chính xác qua `CF-Connecting-IP` / `X-Forwarded-For`, chống lấy nhầm IP proxy.
 * 3. Strict Origin Protection: Kiểm tra dải IP Whitelist của Cloudflare, ngăn chặn bypass WAF khi chạy Production.
 * 4. Truy vết luồng IN/OUT kèm đo lường thời gian thực thi (Latency Tracking).
 */
@Slf4j
@Component
public class TraceIdGatewayFilter implements GlobalFilter, Ordered {

    public static final String CF_RAY = "CF-Ray";
    public static final String CF_CONNECTING_IP = "CF-Connecting-IP";
    public static final String X_TRACE_ID = "X-Trace-ID";
    public static final String X_CORRELATION_ID = "x-correlation-id";
    public static final String X_REQUEST_ID = "X-Request-ID";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String X_REAL_IP = "X-Real-IP";

    @Value("${security.cloudflare.strict-origin-enabled:false}")
    private boolean strictOriginEnabled;

    @Value("${security.cloudflare.whitelist-cidrs:127.0.0.1/32,::1/128}")
    private List<String> whitelistCidrs;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 1. Strict Origin Protection Check
        if (strictOriginEnabled && !isAllowedOrigin(request)) {
            log.warn("[STRICT_ORIGIN_BLOCK] Blocked direct origin access from: {}", getDirectRemoteAddress(request));
            return onOriginForbidden(exchange);
        }

        // 2. Trích xuất Trace ID (Ưu tiên CF-Ray -> X-Trace-ID -> X-Request-ID -> UUID mới)
        String traceId = request.getHeaders().getFirst(CF_RAY);
        if (traceId == null || traceId.isBlank()) {
            traceId = request.getHeaders().getFirst(X_TRACE_ID);
        }
        if (traceId == null || traceId.isBlank()) {
            traceId = request.getHeaders().getFirst(X_REQUEST_ID);
        }
        if (traceId == null || traceId.isBlank()) {
            traceId = request.getHeaders().getFirst(X_CORRELATION_ID);
        }
        if (traceId == null || traceId.isBlank()) {
            traceId = "tr_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }

        // 3. Nhận diện Real Client IP
        String realIp = extractRealClientIp(request);

        final String finalTraceId = traceId;
        final String finalRealIp = realIp;

        // 4. Đột biến Request chuyển tiếp xuống downstream
        ServerHttpRequest.Builder requestBuilder = request.mutate()
                .header(X_TRACE_ID, finalTraceId)
                .header(X_CORRELATION_ID, finalTraceId)
                .header(X_REAL_IP, finalRealIp)
                .header(X_FORWARDED_FOR, finalRealIp);

        if (request.getHeaders().getFirst(CF_RAY) != null) {
            requestBuilder.header(CF_RAY, request.getHeaders().getFirst(CF_RAY));
        }

        long startTime = System.currentTimeMillis();
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        String path = request.getURI().getPath();

        log.info("[GATEWAY_IN] traceId={} ip={} method={} path={}", finalTraceId, finalRealIp, method, path);

        // 5. Nạp Headers phản hồi cho Client
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add(X_TRACE_ID, finalTraceId);
        response.getHeaders().add(X_CORRELATION_ID, finalTraceId);
        if (request.getHeaders().getFirst(CF_RAY) != null) {
            response.getHeaders().add(CF_RAY, request.getHeaders().getFirst(CF_RAY));
        }

        return chain.filter(exchange.mutate().request(requestBuilder.build()).build())
                .doFinally(signalType -> {
                    long duration = System.currentTimeMillis() - startTime;
                    Integer statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : 0;
                    log.info("[GATEWAY_OUT] traceId={} ip={} status={} duration={}ms path={}",
                            finalTraceId, finalRealIp, statusCode, duration, path);
                });
    }

    private String extractRealClientIp(ServerHttpRequest request) {
        String cfIp = request.getHeaders().getFirst(CF_CONNECTING_IP);
        if (cfIp != null && !cfIp.isBlank() && !cfIp.equalsIgnoreCase("unknown")) {
            return cfIp.trim();
        }

        String xff = request.getHeaders().getFirst(X_FORWARDED_FOR);
        if (xff != null && !xff.isBlank() && !xff.equalsIgnoreCase("unknown")) {
            int commaIdx = xff.indexOf(',');
            return commaIdx > 0 ? xff.substring(0, commaIdx).trim() : xff.trim();
        }

        String realIp = request.getHeaders().getFirst(X_REAL_IP);
        if (realIp != null && !realIp.isBlank() && !realIp.equalsIgnoreCase("unknown")) {
            return realIp.trim();
        }

        return getDirectRemoteAddress(request);
    }

    private String getDirectRemoteAddress(ServerHttpRequest request) {
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "127.0.0.1";
    }

    private boolean isAllowedOrigin(ServerHttpRequest request) {
        String directIp = getDirectRemoteAddress(request);
        if ("127.0.0.1".equals(directIp) || "0:0:0:0:0:0:0:1".equals(directIp) || "::1".equals(directIp)) {
            return true;
        }

        for (String cidr : whitelistCidrs) {
            if (ipMatchesCidr(directIp, cidr)) {
                return true;
            }
        }
        return false;
    }

    private boolean ipMatchesCidr(String ip, String cidr) {
        try {
            if (!cidr.contains("/")) {
                return ip.equalsIgnoreCase(cidr);
            }
            String[] parts = cidr.split("/");
            String baseIp = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            InetAddress targetAddr = InetAddress.getByName(ip);
            InetAddress baseAddr = InetAddress.getByName(baseIp);

            byte[] targetBytes = targetAddr.getAddress();
            byte[] baseBytes = baseAddr.getAddress();

            if (targetBytes.length != baseBytes.length) {
                return false;
            }

            int fullBytes = prefixLength / 8;
            for (int i = 0; i < fullBytes; i++) {
                if (targetBytes[i] != baseBytes[i]) return false;
            }

            int remainingBits = prefixLength % 8;
            if (remainingBits > 0 && fullBytes < targetBytes.length) {
                int mask = (0xFF << (8 - remainingBits)) & 0xFF;
                return (targetBytes[fullBytes] & mask) == (baseBytes[fullBytes] & mask);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Mono<Void> onOriginForbidden(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.valueOf("application/json;charset=UTF-8"));
        String jsonError = String.format("{\"status\":403,\"message\":\"Direct Origin IP Access Denied: Request must be routed through Cloudflare WAF\",\"data\":null,\"timestamp\":\"%s\"}",
                java.time.Instant.now());
        return response.writeWith(Mono.just(response.bufferFactory().wrap(jsonError.getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public int getOrder() {
        return -30; // Chạy đầu tiên trước tất cả các Filter khác
    }
}

