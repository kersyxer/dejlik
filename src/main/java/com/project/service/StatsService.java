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

import java.sql.Date;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final DailyStatsRepository dailyStatsRepository;

    public Page<DailyTotalDto> getDailyTotals(LocalDate start, LocalDate end, Optional<String> trafficSource, Pageable pageable) {
        String trafficSourceName = trafficSource.orElse(null);

        List<Object[]> rawData = dailyStatsRepository.findDailyAggregatedData(start, end, trafficSourceName);

        return aggregateDataToDailyTotalDto(rawData, pageable);
    }

    public Page<DailyTotalDto> getDailyTotalsForUser(UUID userId, LocalDate start, LocalDate end, Optional<String> trafficSource, Pageable pageable) {
        String trafficSourceName = trafficSource.orElse(null);

        List<Object[]> rawData = dailyStatsRepository.findDailyAggregatedDataForUser(userId, start, end, trafficSourceName);

        return aggregateDataToDailyTotalDto(rawData, pageable);
    }

    private Page<DailyTotalDto> aggregateDataToDailyTotalDto(List<Object[]> rawData, Pageable pageable) {
        // Нова структура: агрегація по (date + trafficSource)
        Map<String, DailyTotalDto> aggregatedData = new HashMap<>();

        for (Object[] row : rawData) {
            java.sql.Date sqlDate = (java.sql.Date) row[0];
            LocalDate date = sqlDate.toLocalDate();
            BigDecimal cost = (BigDecimal) row[1];
            String trafficSource = (String) row[2];
            String affiliateNetwork = (String) row[3]; // Змінив назву змінної для більшої ясності
            BigDecimal revenue = (BigDecimal) row[4];

            // Ключ для мапи буде комбінацією дати та джерела трафіку
            String key = date.toString() + "-" + trafficSource;

            // Створюємо або отримуємо існуючий DailyTotalDto
            DailyTotalDto dto = aggregatedData.computeIfAbsent(key, k -> {
                DailyTotalDto newDto = new DailyTotalDto();
                newDto.setDate(date);
                newDto.setTrafficSource(trafficSource);
                newDto.setCost(BigDecimal.ZERO); // Початкові витрати 0
                newDto.setRevenues(new HashMap<>());
                return newDto;
            });

            // Додаємо поточні витрати до загальної суми
            dto.setCost(dto.getCost().add(cost));

            // Додаємо дохід до мапи revenues
            // Якщо дохід для цієї партнерської мережі вже є, додаємо до існуючого
            dto.getRevenues().merge(affiliateNetwork, revenue, BigDecimal::add);
        }

        // Тепер у нас є коректно агрегована мапа. Перетворюємо її в список.
        List<DailyTotalDto> resultList = new ArrayList<>(aggregatedData.values());

        // Сортуємо за датою в порядку спадання
        resultList.sort(Comparator.comparing(DailyTotalDto::getDate).reversed()
                .thenComparing(DailyTotalDto::getTrafficSource)); // Додаткове сортування за джерелом трафіку

        // Пагінація залишається без змін
        int startItem = (int) pageable.getOffset();
        int endItem = Math.min(startItem + pageable.getPageSize(), resultList.size());
        List<DailyTotalDto> pageContent = new ArrayList<>();
        if (startItem < resultList.size()) {
            pageContent = resultList.subList(startItem, endItem);
        }

        return new PageImpl<>(pageContent, pageable, resultList.size());
    }
}
