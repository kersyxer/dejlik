package com.project.clickflare;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AutoStatsUpdateService {
    private final ClickFlareDataSyncService clickFlareDataSyncService;

    @Value("${sync.stats.interval}")
    private long syncInterval;  // Зберігаємо interval як String для подальшого перетворення

    @Scheduled(fixedRateString = "${sync.stats.interval}")
    public void updateStats() {
        System.out.println("Sync interval: " + syncInterval); // Для перевірки

        LocalDate today = LocalDate.now();
        LocalDate twoDaysAgo = today.minusDays(2);
        clickFlareDataSyncService.syncDailyStats(twoDaysAgo, today);
    }
}