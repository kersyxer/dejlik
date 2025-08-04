package com.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyOverallStatsDto {
    private LocalDate date;
    private BigDecimal cost;
    private String partnerName;
    private String trafficSourceName;
    private BigDecimal revenue;
}
