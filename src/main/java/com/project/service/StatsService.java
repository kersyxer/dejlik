package com.project.service;

import com.project.dto.DailyOverallStatsDto;
import com.project.dto.DailyTotalDto;
import com.project.entity.DailyStats;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final DailyStatsRepository dailyStatsRepository;

    public Page<DailyOverallStatsDto> getDailyTotals(LocalDate start, LocalDate end, Optional<String> trafficSource, Pageable pageable) {
        if (trafficSource.isPresent()) {
            String trafficSourceNameBase = trafficSource.get();

            if (start != null && end != null) {
                return dailyStatsRepository.findCombinedDailyStatsByTrafficSourceWithDates(start, end, trafficSourceNameBase, pageable);
            } else {
                return dailyStatsRepository.findCombinedDailyStatsByTrafficSourceWithoutDates(trafficSourceNameBase, pageable);
            }
        } else {
            if (start != null && end != null) {
                return dailyStatsRepository.findAllCombinedDailyStatsWithDates(start, end, pageable);
            } else {
                return dailyStatsRepository.findAllCombinedDailyStatsWithoutDates(pageable);
            }
        }
    }
}
