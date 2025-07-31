package com.project.clickflare;

import com.project.dto.ClickFlareReportResponseDto;
import com.project.dto.ReportItemDto;
import com.project.dto.TotalsDto;
import com.project.entity.Campaign;
import com.project.entity.DailyStats;
import com.project.entity.Partner;
import com.project.entity.Source;
import com.project.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

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

    public void syncDailyStats(LocalDate startDate, LocalDate endDate) {
        int page = 1;
        int pageSize = 1000;
        int totalItems = 0;
        List<ReportItemDto> allItems = new ArrayList<>();

        do {
            ClickFlareReportResponseDto response = fetchReport(startDate, endDate, page, pageSize);
            assert response != null;
            List<ReportItemDto> items = response.getItems();
            if (items == null || items.isEmpty()) {
                log.warn("❗️No items found in response");
                break;
            }
            log.info("✅ Parsed {} items", items.size());
            allItems.addAll(items);
            TotalsDto totals = response.getTotals();
            if (totals == null) {
                log.warn("❗️No totals in response");
                break;
            }

            totalItems = totals.getCounter();
            page++;

        } while (allItems.size() < totalItems);

        log.info("📊 Total items collected: {}", allItems.size());
        saveStats(allItems);
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
            log.error("Failed to fetch report from ClickFlare: {}", e.getMessage(), e);
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
                    .orElseGet(() -> campaignRepository.save(Campaign.builder()
                            .clickflareId(dto.getCampaignID())
                            .name(dto.getCampaignName())
                            .partner(partner)
                            .source(source)
                            .build()));

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
