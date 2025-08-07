package com.project.controller;

import com.project.dto.DailyOverallStatsDto;
import com.project.dto.DailyTotalDto;
import com.project.dto.FlattenedDailyStatsDto;
import com.project.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @GetMapping("/total")
    public Page<FlattenedDailyStatsDto> getDailyTotal(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso= DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(value = "trafficSource", required = false) String trafficSourceName,
            @PageableDefault(size=50, page=0)  Pageable pageable) {
             return statsService.getAggregatedFlattenedStats(null, start, end, Optional.ofNullable(trafficSourceName), pageable);
    }

    @GetMapping("/{userID}")
    public Page<FlattenedDailyStatsDto> getDailyTotalForUser(
            @PathVariable UUID userID,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(value = "trafficSource", required = false) String trafficSourceName,
            @PageableDefault(size=30, page=0)  Pageable pageable){
        return statsService.getAggregatedFlattenedStats(userID, start, end, Optional.ofNullable(trafficSourceName), pageable);
    }
}
