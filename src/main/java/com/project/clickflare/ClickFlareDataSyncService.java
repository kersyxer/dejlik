package com.project.clickflare;

import com.google.common.util.concurrent.RateLimiter;
import com.project.dto.ClickFlareReportResponseDto;
import com.project.dto.ReportItemDto;
import com.project.dto.TotalsDto;
import com.project.entity.*;
import com.project.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickFlareDataSyncService {
    private final WebClient clickFlareClient;
    private final CampaignRepository campaignRepository;
    private final PartnerRepository partnerRepository;
    private final SourceRepository sourceRepository;
    private final DailyStatsRepository dailyStatsRepository;
    private final UserService userService;

    private final RateLimiter rateLimiter = RateLimiter.create(2.0);
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    @Async
    public void syncDailyStats(LocalDate startDate, LocalDate endDate) {
        System.out.println("Starting asynchronous sync from " + startDate + " to " + endDate);
        List<ReportItemDto> allItems = Collections.synchronizedList(new ArrayList<>());
        int pageSize = 1000;

        // Спочатку отримуємо загальну кількість
        ClickFlareReportResponseDto firstResponse = fetchReport(startDate, endDate, 1, pageSize);
        if (firstResponse == null || firstResponse.getItems() == null) {
            log.warn("❌ Empty or null response from ClickFlare");
            return;
        }
        int total = firstResponse.getTotals().getCounter();
        int totalPages = (int) Math.ceil((double) total / pageSize);

        // Додаємо першу сторінку одразу
        allItems.addAll(firstResponse.getItems());

        // Всі інші сторінки паралельно
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int page = 2; page <= totalPages; page++) {
            final int currentPage = page;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                rateLimiter.acquire();
                ClickFlareReportResponseDto response = fetchReport(startDate, endDate, currentPage, pageSize);
                if (response != null && response.getItems() != null) {
                    allItems.addAll(response.getItems());
                    log.info("✅ Parsed page {} with {} items", currentPage, response.getItems().size());
                }
            }, executor);
            futures.add(future);
        }

        // Очікуємо завершення всіх потоків
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        log.info("📊 Total collected items: {}", allItems.size());
        saveStats(allItems);
        System.out.println("Async sync finished.");
    }

    private ClickFlareReportResponseDto fetchReport(LocalDate start, LocalDate end, int page, int pageSize) {
        String body = """
            {
                "startDate": "%s 00:00:00",
                "endDate": "%s 23:59:59",
                "groupBy": ["campaignID", "affiliateNetworkID", "trafficSourceID", "date"],
                "metrics": ["campaignID", "affiliateNetworkName", "trafficSourceName","campaignTrafficSourceId", "visits", "conversions", "cvr",  "cost", "revenue", "profit", "roi", "cpa", "campaignName"],
                "timezone": "America/Los_Angeles",
                "currency": "USD",
                "page": %d,
                "pageSize": %d,
                "includeAll": false,
                "sortBy": "cost",
                "orderType": "desc"
            }
        """.formatted(start, end, page, pageSize);

        try {
            return clickFlareClient.post()
                    .uri("/api/report")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(ClickFlareReportResponseDto.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch report from ClickFlare: (page {}): {}", page, e.getMessage());
            return null;
        }
    }

    private void saveStats(List<ReportItemDto> items) {
        int savedCount = 0;
        Set<String> allowedBuyers = new HashSet<>(userService.findAllUsernamesByRole("USER"));
        for (ReportItemDto dto : items) {
            // 🎯 Фільтрація по allowed buyers
            String buyer = Optional.ofNullable(dto.getCampaignName())
                    .map(name -> name.split(" - ")[0].toLowerCase())
                    .orElse("");

            if (!allowedBuyers.contains(buyer)) continue;

            String tsName = Optional.ofNullable(dto.getTrafficSourceName()).orElse("").toLowerCase();
            if (tsName.contains("mg - dsp") || tsName.contains("adskeeper")) continue;
            // 1. Partner
            log.warn("🚨 Missing partner name for ID: {}, campaign: {}", dto.getAffiliateNetworkID(), dto.getCampaignName());


            Partner partner = partnerRepository.findByClickflareId(dto.getAffiliateNetworkID())
                    .orElseGet(() -> partnerRepository.save(Partner.builder()
                            .clickflareId(dto.getAffiliateNetworkID())
                            .name(dto.getAffiliateNetworkName())
                            .build()));

            // 2. Source
            Source source = sourceRepository.findByClickflareId(dto.getTrafficSourceId())
                    .orElseGet(() -> sourceRepository.save(Source.builder()
                            .clickflareId(dto.getTrafficSourceId())
                            .name(dto.getTrafficSourceName())
                            .build()));

            // 3. Campaign
            Campaign campaign = campaignRepository.findByClickflareId(dto.getCampaignID())
                    .orElseGet(() -> {
                        User user = userService.findByUsername(buyer);
                        return campaignRepository.save(Campaign.builder()
                            .clickflareId(dto.getCampaignID())
                            .name(dto.getCampaignName())
                            .partner(partner)
                            .source(source)
                            .user(user)
                            .build());
                    });

            // 4. DailyStats
            LocalDate reportDate = LocalDate.parse(dto.getDate());
            DailyStats stats = dailyStatsRepository.findByCampaignAndDate(campaign, reportDate)
                    .orElse(DailyStats.builder()
                            .campaign(campaign)
                            .date(reportDate)
                            .build());

            stats.setCost(BigDecimal.valueOf(dto.getCost()));
            stats.setRevenue(BigDecimal.valueOf(dto.getRevenue()));
            stats.setProfit(BigDecimal.valueOf(dto.getProfit()));
            stats.setRoi(BigDecimal.valueOf(dto.getRoi()));
            stats.setCpa(BigDecimal.valueOf(dto.getCpa()));
            stats.setCvr(BigDecimal.valueOf(dto.getCvr()));
            stats.setVisits(dto.getVisits());
            stats.setConversions(dto.getConversions());
            stats.setClicks(dto.getClicks());

            dailyStatsRepository.save(stats);
            savedCount++;
        }

        log.info("Saved or updated {} DailyStats entries", savedCount);
    }
}
