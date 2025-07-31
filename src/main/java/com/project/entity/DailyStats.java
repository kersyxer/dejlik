package com.project.entity;

import io.swagger.v3.oas.models.responses.ApiResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_stats", uniqueConstraints = {@UniqueConstraint(columnNames = {"campaign_id", "date"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyStats implements Serializable {
    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "campaign_id", referencedColumnName = "id", nullable = false)
    private Campaign campaign;

    @Column(nullable = false)
    private LocalDate date;

    @Column(precision = 12, scale = 2)
    private BigDecimal cost;

    @Column(precision = 12, scale = 2)
    private BigDecimal revenue;

    @Column(precision = 12, scale = 2)
    private BigDecimal profit;

    @Column(precision = 6, scale = 2)
    private BigDecimal roi;

    @Column(precision = 10, scale = 4)
    private BigDecimal cpa;

    @Column(precision = 10, scale = 4)
    private BigDecimal cvr;

    @Column
    private int visits;

    @Column
    private int conversions;

    @Column
    private int clicks;
}
