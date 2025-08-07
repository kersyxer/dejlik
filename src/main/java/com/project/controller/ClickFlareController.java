package com.project.controller;

import com.project.clickflare.ClickFlareDataSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/clickflare")
@RequiredArgsConstructor
public class ClickFlareController {
    private final ClickFlareDataSyncService clickFlareService;

    @PostMapping("/sync")
    public ResponseEntity<Boolean> sync(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        clickFlareService.syncDailyStats(start, end);
        return ResponseEntity.accepted().body(true);
    }
}
