package com.project.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FlattenedDailyStatsDto {
    private LocalDate date;
    private String trafficSource;
    private BigDecimal cost;
    private BigDecimal tonicRsocRevenue = BigDecimal.ZERO;
    private BigDecimal impeaksRevenue = BigDecimal.ZERO;
    private BigDecimal inuvoRevenue = BigDecimal.ZERO;
}
