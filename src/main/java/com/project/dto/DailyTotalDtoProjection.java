package com.project.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyTotalDtoProjection(
        LocalDate date,
        BigDecimal totalCost,
        String trafficSource,
        BigDecimal totalRevenue
) {}