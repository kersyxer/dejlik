package com.project.dto;

import lombok.Data;

import java.util.List;

@Data
public class ClickFlareReportResponseDto {
    private List<ReportItemDto> items;
    private TotalsDto totals;
}
