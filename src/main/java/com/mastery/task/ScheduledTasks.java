package com.mastery.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class ScheduledTasks {
    
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredLogs() {
        log.info("Scheduled: clean expired logs - {}", LocalDateTime.now());
    }
    
    @Scheduled(fixedRate = 60000)
    public void heartbeat() {
        log.debug("Heartbeat - {}", LocalDateTime.now());
    }
}
