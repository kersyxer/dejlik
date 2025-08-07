package com.project.service;

import com.project.dto.DailyOverallStatsDto;
import com.project.dto.DailyTotalDto;
import com.project.dto.FlattenedDailyStatsDto;
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
    private final PartnerRepository partnerRepository;

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

    /// /////////////////////////////////////////////
    private String toCamelCase(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        String[] parts = s.split(" ");
        StringBuilder camelCaseString = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            camelCaseString.append(parts[i].substring(0, 1).toUpperCase());
            camelCaseString.append(parts[i].substring(1).toLowerCase());
        }
        return camelCaseString.toString();
    }

    public Page<FlattenedDailyStatsDto> getAggregatedFlattenedStats(
            UUID userId,
            LocalDate start,
            LocalDate end,
            Optional<String> trafficSource,
            Pageable pageable) {

        String trafficSourceName = trafficSource.orElse(null);

        // Крок 1: Отримуємо всі унікальні назви партнерських мереж з БД
        List<String> allPartnerNames = partnerRepository.findAllPartnerNames();
        Map<String, String> partnerFieldMap = allPartnerNames.stream()
                .collect(Collectors.toMap(
                        partnerName -> partnerName,
                        // !!! ВИПРАВЛЕННЯ: Використовуємо toCamelCase для генерації назви поля
                        partnerName -> toCamelCase(partnerName) + "Revenue",
                        (existingValue, newValue) -> existingValue
                ));

        // Крок 2: Отримуємо необроблені агреговані дані з БД
        List<Object[]> rawData;
        if (userId == null) {
            rawData = dailyStatsRepository.findDailyAggregatedData(start, end, trafficSourceName);
        } else {
            rawData = dailyStatsRepository.findDailyAggregatedDataForUser(userId, start, end, trafficSourceName);
        }

        // Крок 3: Агрегуємо дані в Map
        Map<String, FlattenedDailyStatsDto> aggregatedMap = new HashMap<>();

        for (Object[] row : rawData) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            BigDecimal cost = (BigDecimal) row[1];
            String trafficSourceFromDb = (String) row[2];
            String partnerNetwork = (String) row[3];
            BigDecimal revenue = (BigDecimal) row[4];

            String key = date.toString() + "_" + trafficSourceFromDb;

            FlattenedDailyStatsDto dto = aggregatedMap.computeIfAbsent(key, k -> {
                FlattenedDailyStatsDto newDto = new FlattenedDailyStatsDto();
                newDto.setDate(date);
                newDto.setTrafficSource(trafficSourceFromDb);
                newDto.setCost(BigDecimal.ZERO);

                for (String fieldName : partnerFieldMap.values()) {
                    try {
                        java.lang.reflect.Field field = FlattenedDailyStatsDto.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(newDto, BigDecimal.ZERO);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        // Якщо поле не знайдено, ігноруємо, оскільки це може бути поле не для цього DTO
                    }
                }
                return newDto;
            });

            dto.setCost(dto.getCost().add(cost));

            String fieldName = partnerFieldMap.get(partnerNetwork);
            if (fieldName != null) {
                try {
                    java.lang.reflect.Field field = FlattenedDailyStatsDto.class.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    BigDecimal currentRevenue = (BigDecimal) field.get(dto);
                    field.set(dto, currentRevenue.add(revenue));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // Якщо поле не знайдено, ігноруємо
                }
            }
        }

        // Крок 4: Пагінація результату після агрегації
        List<FlattenedDailyStatsDto> resultList = new ArrayList<>(aggregatedMap.values());
        resultList.sort(Comparator.comparing(FlattenedDailyStatsDto::getDate).reversed()
                .thenComparing(FlattenedDailyStatsDto::getTrafficSource));
        int startItem = (int) pageable.getOffset();
        int endItem = Math.min(startItem + pageable.getPageSize(), resultList.size());
        List<FlattenedDailyStatsDto> pageContent = new ArrayList<>();
        if (startItem < resultList.size()) {
            pageContent = resultList.subList(startItem, endItem);
        }
        return new PageImpl<>(pageContent, pageable, resultList.size());
    }

}
