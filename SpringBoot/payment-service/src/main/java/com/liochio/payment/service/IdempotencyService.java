package com.liochio.payment.service;

import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.payment.entity.IdempotencyKeyEntity;
import com.liochio.payment.repository.PaymentIdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * ==============================================================================
 * Dịch Vụ Kiểm Soát Bất Biến Giao Dịch Chuẩn Enterprise (Idempotency Engine)
 * ==============================================================================
 */
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdempotencyService.class);
    private final PaymentIdempotencyKeyRepository idempotencyRepository;
    private static final int LOCK_TIMEOUT_SECONDS = 120; // Khóa tối đa 2 phút

    @Transactional
    public Optional<IdempotencyKeyEntity> findValidRecord(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return idempotencyRepository.findByIdempotencyKey(key);
    }

    @Transactional
    public IdempotencyKeyEntity acquireLock(String key, String tenantId, Long userId, String requestPath, String requestHash) {
        if (key == null || key.isBlank()) {
            return null;
        }

        Instant now = Instant.now();
        Optional<IdempotencyKeyEntity> existingOpt = idempotencyRepository.findByIdempotencyKey(key);

        if (existingOpt.isPresent()) {
            IdempotencyKeyEntity existing = existingOpt.get();
            if ("COMPLETED".equals(existing.getStatus())) {
                log.info("[IdempotencyService] Giao dịch với key '{}' đã hoàn tất trước đó. Trả về kết quả lưu trữ.", key);
                return existing;
            }

            if ("PROCESSING".equals(existing.getStatus())) {
                if (existing.getLockedUntil().isAfter(now)) {
                    log.warn("[IdempotencyService] Giao dịch với key '{}' đang trong tiến trình xử lý", key);
                    throw new AppException(ErrorCode.IDEMPOTENCY_CONFLICT, "Yêu cầu giao dịch trùng lặp đang được xử lý, vui lòng chờ trong giây lát.");
                } else {
                    log.warn("[IdempotencyService] Lock trước đó cho key '{}' đã hết hạn, gia hạn lock mới", key);
                    existing.setLockedUntil(now.plus(LOCK_TIMEOUT_SECONDS, ChronoUnit.SECONDS));
                    existing.setUpdatedAt(now);
                    return idempotencyRepository.save(existing);
                }
            }
        }

        IdempotencyKeyEntity newRecord = IdempotencyKeyEntity.builder()
                .idempotencyKey(key)
                .tenantId(tenantId != null ? tenantId : "SYSTEM")
                .userId(userId)
                .requestPath(requestPath)
                .requestHash(requestHash)
                .status("PROCESSING")
                .lockedUntil(now.plus(LOCK_TIMEOUT_SECONDS, ChronoUnit.SECONDS))
                .createdAt(now)
                .updatedAt(now)
                .build();

        return idempotencyRepository.save(newRecord);
    }

    @Transactional
    public void recordSuccess(String key, String responseBody, int statusCode) {
        if (key == null || key.isBlank()) return;

        idempotencyRepository.findByIdempotencyKey(key).ifPresent(entity -> {
            entity.setStatus("COMPLETED");
            entity.setResponseBody(responseBody);
            entity.setStatusCode(statusCode);
            entity.setUpdatedAt(Instant.now());
            idempotencyRepository.save(entity);
            log.info("[IdempotencyService] Đã ghi nhận thành công cho key: '{}'", key);
        });
    }

    @Transactional
    public void recordFailure(String key) {
        if (key == null || key.isBlank()) return;

        idempotencyRepository.findByIdempotencyKey(key).ifPresent(entity -> {
            entity.setStatus("FAILED");
            entity.setUpdatedAt(Instant.now());
            idempotencyRepository.save(entity);
            log.warn("[IdempotencyService] Đã hủy lock do lỗi cho key: '{}'", key);
        });
    }
}