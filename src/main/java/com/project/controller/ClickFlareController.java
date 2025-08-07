package com.project.controller;

import com.project.clickflare.ClickFlareDataSyncService;
import com.project.dto.SyncRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/clickflare")
@RequiredArgsConstructor
public class ClickFlareController {
    private final ClickFlareDataSyncService clickFlareService;

    @PostMapping("/sync")
    public ResponseEntity<Boolean> sync(@RequestBody SyncRequestDto syncRequestDto) {
        clickFlareService.syncDailyStats(syncRequestDto.getStart(), syncRequestDto.getEnd());
        return ResponseEntity.accepted().body(true);
    }
}
