package com.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ReportItemDto {
    private double cost;
    private double revenue;
    private double profit;
    private double roi;
    private double cpa;
    private double cvr;
    private int visits;
    private int conversions;
    private int clicks;

    @JsonProperty("campaignID")
    private String campaignID;

    @JsonProperty("campaignName")
    private String campaignName;

    @JsonProperty("campaignTrafficSourceId")
    private String trafficSourceId;

    @JsonProperty("affiliateNetworkID")
    private String affiliateNetworkID;

    @JsonProperty("affiliateNetworkName")
    private String affiliateNetworkName;

    @JsonProperty("trafficSourceName")
    private String trafficSourceName;

    @JsonProperty("date")
    private String date;
}
