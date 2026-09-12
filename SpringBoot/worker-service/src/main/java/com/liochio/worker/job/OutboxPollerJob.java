package com.liochio.worker.job;

import com.liochio.worker.service.OutboxProcessingService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPollerJob {

    private final OutboxProcessingService outboxProcessingService;

    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(name = "OutboxPollerJob_processPendingEvents", lockAtLeastFor = "2s", lockAtMostFor = "30s")
    public void processPendingEvents() {
        int count = outboxProcessingService.processPendingOutboxEvents();
        if (count > 0) {
            log.info("[OutboxPollerJob] Đã phát tán thành công {} sự kiện Outbox", count);
        }
    }
}
