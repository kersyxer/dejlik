package com.project.service;

import com.project.dto.DailyOverallStatsDto;
import com.project.dto.DailyTotalDtoProjection;
import com.project.entity.Campaign;
import com.project.entity.DailyStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface DailyStatsRepository extends JpaRepository<DailyStats, UUID> {
    Optional<DailyStats> findByCampaignIdAndDate(UUID campaignId, LocalDate date);
    List<DailyStats> findAllByCampaign_User_IdAndDate(UUID userId, LocalDate date);
    List<DailyStats> findAllByDate(LocalDate date);

    Optional<DailyStats> findByCampaignAndDate(Campaign campaign, LocalDate date);
    ///////////////////////////////////////////////////////
    @Query(value = "SELECT ds.date, " +
            "    SUM(ds.cost) as totalCost, " +
            "    CASE WHEN position(' - ' in s.name) > 0 " +
            "         THEN substring(s.name, 1, position(' - ' in s.name) - 1) " +
            "         ELSE s.name " +
            "    END as trafficSource, " +
            "    CASE WHEN position(' [' in p.name) > 0 " +
            "         THEN substring(p.name, 1, position(' [' in p.name) - 1) " +
            "         ELSE p.name " +
            "    END as partnerNetwork, " +
            "    SUM(ds.revenue) as totalRevenue " +
            "FROM daily_stats ds " +
            "JOIN campaigns c ON c.id = ds.campaign_id " +
            "JOIN sources s ON s.id = c.source_id " +
            "JOIN partners p ON p.id = c.partner_id " +
            "WHERE (:trafficSourceNameBase IS NULL OR " +
            "       LOWER(CASE WHEN position(' - ' in s.name) > 0 " +
            "                  THEN substring(s.name, 1, position(' - ' in s.name) - 1) " +
            "                  ELSE s.name " +
            "             END) = LOWER(:trafficSourceNameBase)) " +
            "AND (CAST(:start AS DATE) IS NULL OR ds.date >= :start) " +
            "AND (CAST(:end AS DATE) IS NULL OR ds.date <= :end) " +
            "GROUP BY ds.date, s.name, p.name " +
            "ORDER BY ds.date",
            nativeQuery = true)
    List<Object[]> findDailyAggregatedData(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("trafficSourceNameBase") String trafficSourceNameBase
    );

    @Query(value = "SELECT ds.date, " +
            "    SUM(ds.cost) as totalCost, " +
            "    CASE WHEN position(' - ' in s.name) > 0 " +
            "         THEN substring(s.name, 1, position(' - ' in s.name) - 1) " +
            "         ELSE s.name " +
            "    END as trafficSource, " +
            "    CASE WHEN position(' [' in p.name) > 0 " +
            "         THEN substring(p.name, 1, position(' [' in p.name) - 1) " +
            "         ELSE p.name " +
            "    END as partnerNetwork, " +
            "    SUM(ds.revenue) as totalRevenue " +
            "FROM daily_stats ds " +
            "JOIN campaigns c ON c.id = ds.campaign_id " +
            "JOIN sources s ON s.id = c.source_id " +
            "JOIN partners p ON p.id = c.partner_id " + // Додали JOIN для таблиці партнерів
            "WHERE c.user_id = :userId " +
            "AND (:trafficSourceNameBase IS NULL OR " +
            "       LOWER(CASE WHEN position(' - ' in s.name) > 0 " +
            "                  THEN substring(s.name, 1, position(' - ' in s.name) - 1) " +
            "                  ELSE s.name " +
            "             END) = LOWER(:trafficSourceNameBase)) " +
            "AND (CAST(:start AS DATE) IS NULL OR ds.date >= :start) " + // Явно приводимо тип
            "AND (CAST(:end AS DATE) IS NULL OR ds.date <= :end) " + // Явно приводимо тип
            "GROUP BY ds.date, s.name, p.name " + // Оновлюємо GROUP BY
            "ORDER BY ds.date DESC",
            nativeQuery = true)
    List<Object[]> findDailyAggregatedDataForUser(
            @Param("userId") UUID userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("trafficSourceNameBase") String trafficSourceNameBase
    );
}
