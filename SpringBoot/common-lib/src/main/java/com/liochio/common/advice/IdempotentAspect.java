package com.liochio.common.advice;

import com.liochio.common.annotation.Idempotent;
import com.liochio.common.context.TenantContext;
import com.liochio.common.context.UserContext;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.entity.IdempotencyKeyEntity;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.common.repository.IdempotencyKeyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * ==============================================================================
 * AOP Aspect Chống Trùng Lặp Request Phân Tán (Idempotency Aspect - Bảng 37)
 * ==============================================================================
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private IdempotencyKeyRepository idempotencyKeyRepository;
    private final Optional<StringRedisTemplate> redisTemplate;
    private final ObjectMapper objectMapper;

    @Around("@annotation(idempotent)")
    public Object handleIdempotency(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String idempotencyKey = request.getHeader("Idempotency-Key");

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            if (idempotent.required()) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Thiếu header Idempotency-Key bắt buộc");
            }
            return joinPoint.proceed();
        }

        String tenantId = TenantContext.getTenantId();
        String redisLockKey = String.format("idempotent:%s:%s", tenantId, idempotencyKey);

        // 1. Kiểm tra L2 Cache Redis (Lock chống race condition)
        if (redisTemplate.isPresent()) {
            try {
                Boolean isNew = redisTemplate.get().opsForValue().setIfAbsent(redisLockKey, "PROCESSING", idempotent.timeoutSeconds(), TimeUnit.SECONDS);
                if (Boolean.FALSE.equals(isNew)) {
                    // Kiểm tra xem đã có kết quả trong DB chưa
                    if (idempotencyKeyRepository != null) {
                        Optional<IdempotencyKeyEntity> existingKey = idempotencyKeyRepository.findByIdempotencyKeyAndTenantId(idempotencyKey, tenantId);
                        if (existingKey.isPresent()) {
                            log.info("[IdempotentAspect] Trả về cached response cho Idempotency-Key: {}", idempotencyKey);
                            return objectMapper.readValue(existingKey.get().getResponseBody(), Object.class);
                        }
                    }
                    throw new AppException(ErrorCode.IDEMPOTENCY_CONFLICT, "Yêu cầu đang được xử lý, vui lòng không gửi trùng lặp");
                }
            } catch (AppException ae) {
                throw ae;
            } catch (Exception e) {
                log.warn("[IdempotentAspect] Redis không khả dụng, bỏ qua Redis lock: {}", e.getMessage());
            }
        }

        // 2. Thực thi nghiệp vụ
        Object result = joinPoint.proceed();

        // 3. Lưu kết quả vào cơ sở dữ liệu để phục vụ retry
        if (idempotencyKeyRepository != null) {
            try {
                String responseJson = objectMapper.writeValueAsString(result);
                IdempotencyKeyEntity entity = IdempotencyKeyEntity.builder()
                        .idempotencyKey(idempotencyKey)
                        .tenantId(tenantId)
                        .userId(UserContext.getUserId())
                        .requestHash(hashString(request.getRequestURI() + joinPoint.getArgs().toString()))
                        .responseBody(responseJson)
                        .statusCode(200)
                        .expiresAt(Instant.now().plus(idempotent.timeoutSeconds(), ChronoUnit.SECONDS))
                        .createdAt(Instant.now())
                        .build();

                idempotencyKeyRepository.save(entity);
            } catch (Exception e) {
                log.warn("[IdempotentAspect] Không thể lưu Idempotency Key vào DB: {}", e.getMessage());
            }
        }

        return result;
    }

    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
    }
}
