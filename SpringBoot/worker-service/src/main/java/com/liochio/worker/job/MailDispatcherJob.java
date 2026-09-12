package com.liochio.worker.job;

import com.liochio.worker.service.MailProcessingService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailDispatcherJob {

    private final MailProcessingService mailProcessingService;

    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(name = "MailDispatcherJob_processPendingMails", lockAtLeastFor = "2s", lockAtMostFor = "30s")
    public void processPendingMails() {
        int count = mailProcessingService.processPendingBatch();
        if (count > 0) {
            log.info("[MailDispatcherJob] Đã xử lý và gửi thành công {} bản ghi thư tín", count);
        }
    }
}
