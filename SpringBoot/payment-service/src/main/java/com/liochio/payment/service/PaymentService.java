package com.liochio.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liochio.common.context.TenantContext;
import com.liochio.common.context.UserContext;
import com.liochio.common.enums.PaymentStatus;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.common.outbox.OutboxPublisher;
import com.liochio.common.pattern.factory.PaymentFactory;
import com.liochio.common.pattern.strategy.PaymentStrategy;
import com.liochio.payment.dto.CreatePaymentRequest;
import com.liochio.payment.dto.PaymentOrderResponse;
import com.liochio.payment.dto.PaymentUrlResponse;
import com.liochio.payment.entity.IdempotencyKeyEntity;
import com.liochio.payment.entity.PaymentOrderEntity;
import com.liochio.payment.repository.PaymentOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * ==============================================================================
 * Dịch Vụ Xử Lý Giao Dịch Thanh Toán Chuẩn Enterprise (Payment Hub Service)
 * ==============================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentFactory paymentFactory;
    private final OutboxPublisher outboxPublisher;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "paymentGateway", fallbackMethod = "createPaymentFallback")
    @io.github.resilience4j.bulkhead.annotation.Bulkhead(name = "paymentGateway", fallbackMethod = "createPaymentBulkheadFallback")
    public PaymentUrlResponse createPayment(CreatePaymentRequest request, String clientIp, String idempotencyKey) {
        String tenantId = TenantContext.getTenantId();
        Long userId = UserContext.getUserId();

        // 1. Kiểm tra Tính Bất Biến (Idempotency Check)
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<IdempotencyKeyEntity> existingKeyOpt = idempotencyService.findValidRecord(idempotencyKey);
            if (existingKeyOpt.isPresent() && "COMPLETED".equals(existingKeyOpt.get().getStatus())) {
                try {
                    log.info("[PaymentService] IdempotencyKey='{}' đã được xử lý thành công. Trả về kết quả từ bộ đệm.", idempotencyKey);
                    return objectMapper.readValue(existingKeyOpt.get().getResponseBody(), PaymentUrlResponse.class);
                } catch (Exception e) {
                    log.warn("[PaymentService] Không thể giải mã cached response cho key '{}': {}", idempotencyKey, e.getMessage());
                }
            }
            idempotencyService.acquireLock(idempotencyKey, tenantId, userId, "/api/v1/payments/create", request.getOrderId());
        }

        try {
            PaymentStrategy strategy = paymentFactory.getStrategy(request.getGateway());

            String paymentUrl = strategy.createPaymentUrl(
                    request.getOrderId(),
                    request.getAmount(),
                    request.getOrderInfo(),
                    request.getReturnUrl(),
                    clientIp
            );

            PaymentOrderEntity order = PaymentOrderEntity.builder()
                    .orderId(request.getOrderId())
                    .userId(userId)
                    .gateway(request.getGateway().toUpperCase())
                    .amount(request.getAmount())
                    .orderInfo(request.getOrderInfo())
                    .status(PaymentStatus.PENDING)
                    .build();
            order.setTenantId(tenantId);

            paymentOrderRepository.save(order);
            log.info("[PaymentService] Đã khởi tạo đơn thanh toán: OrderId='{}', Gateway='{}', IdempotencyKey='{}'",
                    order.getOrderId(), order.getGateway(), idempotencyKey);

            PaymentUrlResponse response = PaymentUrlResponse.builder()
                    .orderId(order.getOrderId())
                    .gateway(order.getGateway())
                    .amount(order.getAmount())
                    .paymentUrl(paymentUrl)
                    .createdAt(Instant.now())
                    .build();

            // 2. Ghi nhận thành công vào Idempotency Engine
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                try {
                    String json = objectMapper.writeValueAsString(response);
                    idempotencyService.recordSuccess(idempotencyKey, json, 200);
                } catch (Exception ignored) {}
            }

            return response;
        } catch (Exception ex) {
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                idempotencyService.recordFailure(idempotencyKey);
            }
            throw ex;
        }
    }

    public PaymentUrlResponse createPaymentFallback(CreatePaymentRequest request, String clientIp, String idempotencyKey, Throwable t) {
        log.warn("[CircuitBreaker Fallback] Cổng thanh toán '{}' tạm thời gián đoạn: {}. Trả về fallback URL nội bộ.",
                request.getGateway(), t.getMessage());
        return PaymentUrlResponse.builder()
                .orderId(request.getOrderId())
                .gateway(request.getGateway() != null ? request.getGateway().toUpperCase() : "FALLBACK")
                .amount(request.getAmount())
                .paymentUrl("/api/v1/payments/fallback-queue?orderId=" + request.getOrderId())
                .createdAt(Instant.now())
                .build();
    }

    public PaymentUrlResponse createPaymentBulkheadFallback(CreatePaymentRequest request, String clientIp, String idempotencyKey, Throwable t) {
        log.warn("[Bulkhead Fallback] Hệ thống thanh toán đang chịu tải cao (Hết Thread Pool): {}. Đưa vào hàng đợi xử lý.",
                t.getMessage());
        return PaymentUrlResponse.builder()
                .orderId(request.getOrderId())
                .gateway("QUEUED")
                .amount(request.getAmount())
                .paymentUrl("/api/v1/payments/queue?orderId=" + request.getOrderId())
                .createdAt(Instant.now())
                .build();
    }

    @Transactional
    public boolean handleIpn(String gatewayName, Map<String, String> params) {
        PaymentStrategy strategy = paymentFactory.getStrategy(gatewayName);
        boolean isValidSignature = strategy.verifyIpnSignature(params);

        if (!isValidSignature) {
            log.warn("[PaymentService] Xác thực IPN Signature thất bại cho gateway: {}", gatewayName);
            throw new AppException(ErrorCode.PAYMENT_SIGNATURE_INVALID);
        }

        String orderId = params.getOrDefault("vnp_TxnRef", params.get("orderId"));
        if (orderId == null) {
            log.warn("[PaymentService] Không tìm thấy OrderId trong callback params");
            return false;
        }

        PaymentOrderEntity order = paymentOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy đơn hàng: " + orderId));

        if (order.getStatus() == PaymentStatus.SUCCESS) {
            log.info("[PaymentService] Đơn hàng '{}' đã xử lý thành công trước đó (Idempotent)", orderId);
            return true;
        }

        String responseCode = params.getOrDefault("vnp_ResponseCode", "00");
        if ("00".equals(responseCode) || "0".equals(responseCode)) {
            order.setStatus(PaymentStatus.SUCCESS);
            order.setTransactionNo(params.getOrDefault("vnp_TransactionNo", params.get("transId")));
            order.setResponseCode(responseCode);
            paymentOrderRepository.save(order);

            // Bắn sự kiện PAYMENT_SUCCESS vào Outbox Table
            outboxPublisher.publish("PAYMENT", order.getOrderId(), "PAYMENT_SUCCESS", order);
            log.info("[PaymentService] Thanh toán thành công cho đơn hàng: {}", orderId);
            return true;
        } else {
            order.setStatus(PaymentStatus.FAILED);
            order.setResponseCode(responseCode);
            paymentOrderRepository.save(order);
            log.warn("[PaymentService] Thanh toán thất bại cho đơn hàng: {}, ResponseCode: {}", orderId, responseCode);
            return false;
        }
    }

    @Transactional
    public PaymentOrderResponse reconcilePendingOrder(String orderId) {
        PaymentOrderEntity order = paymentOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy đơn hàng: " + orderId));

        log.info("[PaymentService] Đối soát trạng thái chủ động cho đơn hàng: OrderId='{}', Status='{}'", orderId, order.getStatus());
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public PaymentOrderResponse getOrderByOrderId(String orderId) {
        PaymentOrderEntity order = paymentOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy đơn hàng: " + orderId));
        return mapToResponse(order);
    }

    private PaymentOrderResponse mapToResponse(PaymentOrderEntity entity) {
        return PaymentOrderResponse.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .orderId(entity.getOrderId())
                .userId(entity.getUserId())
                .gateway(entity.getGateway())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .orderInfo(entity.getOrderInfo())
                .status(entity.getStatus().name())
                .transactionNo(entity.getTransactionNo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}