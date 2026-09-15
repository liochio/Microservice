package com.liochio.common.outbox;

import com.liochio.common.context.TenantContext;
import com.liochio.common.enums.OutboxStatus;
import com.liochio.common.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * ==============================================================================
 * Bộ Phát Sự Kiện Vào Bảng Outbox (Outbox Event Publisher)
 * ==============================================================================
 * 
 * Mục đích:
 * - Đóng gói dữ liệu Payload thành JSON và lưu vào bảng 'outbox_events' trong
 *   cùng Transaction hiện tại (Propagation.MANDATORY hoặc REQUIRED).
 * 
 * Khi nào gọi:
 * - Được các Service gọi ngay trước khi hoàn tất Transaction lưu dữ liệu chính.
 */
@Slf4j
@Component
public class OutboxPublisher {

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private OutboxRepository outboxRepository;

    /**
     * Ghi nhận sự kiện vào Outbox Table
     *
     * @param aggregateType Tên đối tượng gốc (vd: "USER", "PAYMENT", "PORTFOLIO")
     * @param aggregateId   ID của đối tượng (vd: "101", "INV-2026-001")
     * @param eventType     Loại sự kiện (vd: "USER_REGISTERED", "PAYMENT_COMPLETED")
     * @param payload       Đối tượng dữ liệu chi tiết của sự kiện
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void publish(String aggregateType, String aggregateId, String eventType, Object payload) {
        if (outboxRepository == null) {
            log.debug("[OutboxPublisher] outboxRepository không có sẵn trên service này, bỏ qua lưu outbox");
            return;
        }
        String jsonPayload = JsonUtils.toJson(payload);
        String tenantId = TenantContext.getTenantId();

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .tenantId(tenantId)
                .payload(jsonPayload)
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build();

        outboxRepository.save(outboxEvent);
        log.info("[OutboxPublisher] Đã lưu sự kiện Outbox: Type='{}', AggregateId='{}'", eventType, aggregateId);
    }
}
