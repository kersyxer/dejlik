package com.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyTotalDto {
    private LocalDate date;
    private BigDecimal cost;
    private Map<String, BigDecimal> revenues = new HashMap<>();
}
