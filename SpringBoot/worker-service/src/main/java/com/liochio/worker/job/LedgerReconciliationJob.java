package com.liochio.worker.job;

import com.liochio.worker.service.LedgerReconciliationService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LedgerReconciliationJob {

    private final LedgerReconciliationService reconciliationService;

    // Chạy định kỳ lúc 00:00 mỗi ngày
    @Scheduled(cron = "0 0 0 * * ?")
    @SchedulerLock(name = "LedgerReconciliationJob_runDailyAudit", lockAtLeastFor = "10s", lockAtMostFor = "5m")
    public void runDailyAudit() {
        log.info("[LedgerReconciliationJob] [CRON] Bắt đầu phiên đối soát tự động cuối ngày...");
        reconciliationService.performAudit();
    }
}
