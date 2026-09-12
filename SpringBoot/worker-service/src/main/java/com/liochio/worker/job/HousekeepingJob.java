package com.liochio.worker.job;

import com.liochio.worker.service.HousekeepingService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HousekeepingJob {

    private final HousekeepingService housekeepingService;

    // Chạy mỗi giờ một lần
    @Scheduled(cron = "0 0 * * * ?")
    @SchedulerLock(name = "HousekeepingJob_cleanupStaleData", lockAtLeastFor = "5s", lockAtMostFor = "2m")
    public void cleanupStaleData() {
        log.info("[HousekeepingJob] [CRON] Thực thi dọn dẹp dữ liệu rác...");
        housekeepingService.runCleanup();
    }
}
