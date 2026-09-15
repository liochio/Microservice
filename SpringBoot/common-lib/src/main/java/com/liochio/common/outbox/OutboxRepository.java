package com.liochio.common.outbox;

import com.liochio.common.enums.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ==============================================================================
 * Repository Truy Vấn Bảng Sự Kiện Outbox (Outbox Event Repository)
 * ==============================================================================
 * 
 * Mục đích:
 * - Cung cấp hàm truy vấn các sự kiện đang ở trạng thái 'PENDING' hoặc 'FAILED'
 *   để Scheduler định kỳ quét và phát tán lên Message Broker (RabbitMQ/Kafka).
 */
@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = :status AND o.retryCount < :maxRetries ORDER BY o.createdAt ASC")
    List<OutboxEvent> findPendingEvents(
            @Param("status") OutboxStatus status,
            @Param("maxRetries") int maxRetries,
            Pageable pageable
    );
}
