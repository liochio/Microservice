package com.liochio.common.enums;

import lombok.Getter;

/**
 * ==============================================================================
 * Enum Trạng thái Sự kiện Outbox (Transactional Outbox Status)
 * ==============================================================================
 * 
 * Mục đích:
 * - Theo dõi vòng đời của sự kiện phân tán trong bảng 'outbox_events' nhằm
 *   đảm bảo tính toàn vẹn dữ liệu khi có lỗi xảy ra hoặc cơ sở dữ liệu rollback.
 * 
 * Khi nào gọi:
 * - Được OutboxEventPublisher và Scheduler ngầm quét gửi message lên Message Queue sử dụng.
 */
@Getter
public enum OutboxStatus {
    PENDING("Chờ gửi lên Message Broker"),
    PROCESSING("Đang trong quá trình đẩy message"),
    PUBLISHED("Đã gửi thành công"),
    FAILED("Gửi thất bại (chờ retry hoặc đưa vào Dead Letter Queue)");

    private final String description;

    OutboxStatus(String description) {
        this.description = description;
    }
}
