package com.project.service;

import com.project.dto.DailyTotalDto;
import com.project.entity.DailyStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final DailyStatsRepository dailyStatsRepository;

    public List<DailyTotalDto> getDailyTotals(LocalDate start, LocalDate end, String trafficSourceName) {
        String trafficSourceNameBase = trafficSourceName;
        if (trafficSourceName.contains(" - ")) {
            trafficSourceNameBase = trafficSourceName.split(" - ")[0];
        }
        String trafficSourceNameBaseLower = trafficSourceNameBase.toLowerCase();
        List<Object[]> results = dailyStatsRepository.findCombinedDailyStats(start, end, trafficSourceNameBaseLower);
        Map<LocalDate, DailyTotalDto> dailyMap = new TreeMap<>();
        for (Object[] result : results) {
            LocalDate date = (LocalDate) result[0];
            BigDecimal cost = (BigDecimal) result[1];
            String partnerBaseName = (String) result[2];
            BigDecimal revenue = (BigDecimal) result[3];

            DailyTotalDto dto = dailyMap.computeIfAbsent(date, d -> new DailyTotalDto(d, BigDecimal.ZERO, new HashMap<>()));
            dto.setCost(dto.getCost().add(cost));

            dto.getRevenues().merge(partnerBaseName, revenue, BigDecimal::add);
        }
        List<DailyTotalDto> finalResult = new ArrayList<>();
        LocalDate currentDate = start;
        while (!currentDate.isAfter(end)) {
            DailyTotalDto dailyDto = dailyMap.getOrDefault(currentDate, new DailyTotalDto(currentDate, BigDecimal.ZERO, new HashMap<>()));
            finalResult.add(dailyDto);
            currentDate = currentDate.plusDays(1);
        }
        return finalResult;
    }
}
