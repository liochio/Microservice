package com.liochio.payment.service;

import com.liochio.common.enums.PaymentStatus;
import com.liochio.common.outbox.OutboxPublisher;
import com.liochio.common.saga.SagaEvent;
import com.liochio.common.saga.SagaStatus;
import com.liochio.payment.entity.PaymentOrderEntity;
import com.liochio.payment.repository.PaymentOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ==============================================================================
 * Dịch Vụ Quản Trị Giao Dịch Bù Trừ Saga (Saga Compensation Engine)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tiếp nhận các sự kiện lỗi chặng sau (ví dụ: Python AI lỗi, xuất hóa đơn VAT thất bại).
 * - Tự động phát động giao dịch bù trừ (Compensating Transaction) để đảo ngược trạng thái,
 *   hoàn tiền về tài khoản và ghi nhận bút toán đảo ngược Sổ cái kép.
 */
@Service
@RequiredArgsConstructor
public class SagaCompensationService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SagaCompensationService.class);
    private final PaymentOrderRepository paymentOrderRepository;
    private final OutboxPublisher outboxPublisher;

    /**
     * Thực thi giao dịch bù trừ hoàn tiền tự động khi luồng phân tán thất bại
     */
    @Transactional
    public SagaEvent executeCompensation(String orderId, String failureReason, String traceId) {
        log.warn("\n" +
                "================================================================================\n" +
                "🚨 [SAGA COMPENSATION ENGINE] KÍCH HOẠT GIAO DỊCH BÙ TRỪ PHÂN TÁN\n" +
                "   -> Mã Đơn Hàng       : {}\n" +
                "   -> Trace ID          : {}\n" +
                "   -> Nguyên Nhân Lỗi   : {}\n" +
                "   -> Hành Động Bù Trừ  : Hoàn tiền Ví & Ghi Có bút toán đảo ngược Sổ cái\n" +
                "================================================================================",
                orderId, traceId, failureReason);

        Optional<PaymentOrderEntity> orderOpt = paymentOrderRepository.findByOrderId(orderId);
        if (orderOpt.isEmpty()) {
            log.error("[SagaCompensation] Không tìm thấy đơn hàng '{}' để bù trừ", orderId);
            return SagaEvent.builder()
                    .sagaId("saga_" + orderId)
                    .traceId(traceId)
                    .status(SagaStatus.COMPENSATION_FAILED)
                    .failureReason("Order not found: " + orderId)
                    .build();
        }

        PaymentOrderEntity order = orderOpt.get();
        order.setStatus(PaymentStatus.REFUNDED);
        paymentOrderRepository.save(order);

        // 1. Chuẩn bị payload bù trừ
        Map<String, Object> compensationData = new HashMap<>();
        compensationData.put("orderId", order.getOrderId());
        compensationData.put("userId", order.getUserId());
        compensationData.put("refundAmount", order.getAmount());
        compensationData.put("currency", "VND");
        compensationData.put("failureReason", failureReason);
        compensationData.put("compensatedAt", Instant.now().toString());

        // 2. Phát tán sự kiện Outbox cho Core Banking và Worker để gửi thông báo hoàn tiền
        outboxPublisher.publish("SAGA_PAYMENT", order.getOrderId(), "REFUND_PROCESSED", compensationData);

        log.info("[SagaCompensation] ✅ Đã hoàn tất bù trừ cho đơn hàng '{}'. Trạng thái chuyển sang REFUNDED", orderId);

        return SagaEvent.builder()
                .sagaId("saga_" + orderId)
                .traceId(traceId)
                .sagaType("PAYMENT_COMPENSATION")
                .currentStep("REVERSE_LEDGER_REFUND")
                .status(SagaStatus.COMPENSATED_REVERSED)
                .payload(compensationData)
                .timestamp(Instant.now())
                .build();
    }
}
