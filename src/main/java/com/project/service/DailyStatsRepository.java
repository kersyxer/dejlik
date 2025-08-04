package com.project.service;

import com.project.dto.DailyOverallStatsDto;
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

    @Query(value = "SELECT new com.project.dto.DailyOverallStatsDto(ds.date, " +
            "    SUM(ds.cost), " +
            "    CASE WHEN LOCATE(' [', ds.campaign.partner.name) > 0 " +
            "         THEN SUBSTRING(ds.campaign.partner.name, 1, LOCATE(' [', ds.campaign.partner.name) - 1) " +
            "         ELSE ds.campaign.partner.name " +
            "    END, " +
            "    CASE WHEN LOCATE(' - ', ds.campaign.source.name) > 0 " +
            "         THEN SUBSTRING(ds.campaign.source.name, 1, LOCATE(' - ', ds.campaign.source.name) - 1) " +
            "         ELSE ds.campaign.source.name " +
            "    END, " +
            "    SUM(ds.revenue)) " +
            "FROM DailyStats ds " +
            "WHERE ds.date >= :start AND ds.date <= :end " +
            "AND (CASE WHEN LOCATE(' - ', ds.campaign.source.name) > 0 " +
            "          THEN LOWER(SUBSTRING(ds.campaign.source.name, 1, LOCATE(' - ', ds.campaign.source.name) - 1)) " +
            "          ELSE LOWER(ds.campaign.source.name) " +
            "     END) = LOWER(:trafficSourceNameBase) " +
            "GROUP BY ds.date, ds.campaign.partner.name, ds.campaign.source.name " +
            "ORDER BY ds.date",
            countQuery = "SELECT count(ds.date) FROM DailyStats ds " +
                    "WHERE ds.date >= :start AND ds.date <= :end " +
                    "AND (CASE WHEN LOCATE(' - ', ds.campaign.source.name) > 0 THEN LOWER(SUBSTRING(ds.campaign.source.name, 1, LOCATE(' - ', ds.campaign.source.name) - 1)) ELSE LOWER(ds.campaign.source.name) END) = LOWER(:trafficSourceNameBase) " +
                    "GROUP BY ds.date, CASE WHEN LOCATE(' [', ds.campaign.partner.name) > 0 THEN SUBSTRING(ds.campaign.partner.name, 1, LOCATE(' [', ds.campaign.partner.name) - 1) ELSE ds.campaign.partner.name END, CASE WHEN LOCATE(' - ', ds.campaign.source.name) > 0 THEN SUBSTRING(ds.campaign.source.name, 1, LOCATE(' - ', ds.campaign.source.name) - 1) ELSE ds.campaign.source.name END",
            nativeQuery = false)
    Page<DailyOverallStatsDto> findCombinedDailyStatsByTrafficSourceWithDates(@Param("start") LocalDate start,
                                                                              @Param("end") LocalDate end,
                                                                              @Param("trafficSourceNameBase") String trafficSourceNameBase,
                                                                              Pageable pageable);

    // Метод 2: З trafficSource, але БЕЗ ДАТ
    @Query(value = "SELECT new com.project.dto.DailyOverallStatsDto(ds.date, " +
            "    SUM(ds.cost), " +
            "    CASE WHEN LOCATE(' [', ds.campaign.partner.name) > 0 " +
            "         THEN SUBSTRING(ds.campaign.partner.name, 1, LOCATE(' [', ds.campaign.partner.name) - 1) " +
            "         ELSE ds.campaign.partner.name " +
            "    END, " +
            "    CASE WHEN LOCATE(' - ', ds.campaign.source.name) > 0 " +
            "         THEN SUBSTRING(ds.campaign.source.name, 1, LOCATE(' - ', ds.campaign.source.name) - 1) " +
            "         ELSE ds.campaign.source.name " +
            "    END, " +
            "    SUM(ds.revenue)) " +
            "FROM DailyStats ds " +
            "WHERE (CASE WHEN LOCATE(' - ', ds.campaign.source.name) > 0 " +
            "          THEN LOWER(SUBSTRING(ds.campaign.source.name, 1, LOCATE(' - ', ds.campaign.source.name) - 1)) " +
            "          ELSE LOWER(ds.campaign.source.name) " +
            "     END) = LOWER(:trafficSourceNameBase) " +
            "GROUP BY ds.date, ds.campaign.partner.name, ds.campaign.source.name " +
            "ORDER BY ds.date",
            countQuery = "SELECT count(ds.date) FROM DailyStats ds " +
                    "WHERE (CASE WHEN LOCATE(' - ', ds.campaign.source.name) > 0 THEN LOWER(SUBSTRING(ds.campaign.source.name, 1, LOCATE(' - ', ds.campaign.source.name) - 1)) ELSE LOWER(ds.campaign.source.name) END) = LOWER(:trafficSourceNameBase) " +
                    "GROUP BY ds.date, CASE WHEN LOCATE(' [', ds.campaign.partner.name) > 0 THEN SUBSTRING(ds.campaign.partner.name, 1, LOCATE(' [', ds.campaign.partner.name) - 1) ELSE ds.campaign.partner.name END, CASE WHEN LOCATE(' - ', ds.campaign.source.name) > 0 THEN SUBSTRING(ds.campaign.source.name, 1, LOCATE(' - ', ds.campaign.source.name) - 1) ELSE ds.campaign.source.name END",
            nativeQuery = false)
    Page<DailyOverallStatsDto> findCombinedDailyStatsByTrafficSourceWithoutDates(@Param("trafficSourceNameBase") String trafficSourceNameBase,
                                                                                 Pageable pageable);

    // Метод 3: БЕЗ trafficSource, але з ДАТАМИ
    @Query(value = "SELECT new com.project.dto.DailyOverallStatsDto(ds.date, " +
            "    SUM(ds.cost), " +
            "    CASE WHEN LOCATE(' [', ds.campaign.partner.name) > 0 " +
            "         THEN SUBSTRING(ds.campaign.partner.name, 1, LOCATE(' [', ds.campaign.partner.name) - 1) " +
            "         ELSE ds.campaign.partner.name " +
            "    END, " +
            "    CASE WHEN LOCATE(' - ', ds.campaign.source.name) > 0 " +
            "         THEN SUBSTRING(ds.campaign.source.name, 1, LOCATE(' - ', ds.campaign.source.name) - 1) " +
            "         ELSE ds.campaign.source.name " +
            "    END, " +
            "    SUM(ds.revenue)) " +
            "FROM DailyStats ds " +
            "WHERE ds.date >= :start AND ds.date <= :end " +
            "GROUP BY ds.date, ds.campaign.partner.name, ds.campaign.source.name " +
            "ORDER BY ds.date",
            countQuery = "SELECT count(ds.date) FROM DailyStats ds " +
                    "WHERE ds.date >= :start AND ds.date <= :end " +
                    "GROUP BY ds.date, CASE WHEN LOCATE(' [', ds.campaign.partner.name) > 0 THEN SUBSTRING(ds.campaign.partner.name, 1, LOCATE(' [', ds.campaign.partner.name) - 1) ELSE ds.campaign.partner.name END, CASE WHEN LOCATE(' - ', ds.campaign.source.name) > 0 THEN SUBSTRING(ds.campaign.source.name, 1, LOCATE(' - ', ds.campaign.source.name) - 1) ELSE ds.campaign.source.name END",
            nativeQuery = false)
    Page<DailyOverallStatsDto> findAllCombinedDailyStatsWithDates(@Param("start") LocalDate start,
                                                                  @Param("end") LocalDate end,
                                                                  Pageable pageable);

    // Метод 4: БЕЗ trafficSource і БЕЗ ДАТ
    @Query(value = "SELECT new com.project.dto.DailyOverallStatsDto(ds.date, " +
            "    SUM(ds.cost), " +
            "    CASE WHEN LOCATE(' [', ds.campaign.partner.name) > 0 " +
            "         THEN SUBSTRING(ds.campaign.partner.name, 1, LOCATE(' [', ds.campaign.partner.name) - 1) " +
            "         ELSE ds.campaign.partner.name " +
            "    END, " +
            "    CASE WHEN LOCATE(' - ', ds.campaign.source.name) > 0 " +
            "         THEN SUBSTRING(ds.campaign.source.name, 1, LOCATE(' - ', ds.campaign.source.name) - 1) " +
            "         ELSE ds.campaign.source.name " +
            "    END, " +
            "    SUM(ds.revenue)) " +
            "FROM DailyStats ds " +
            "GROUP BY ds.date, ds.campaign.partner.name, ds.campaign.source.name " +
            "ORDER BY ds.date",
            countQuery = "SELECT count(ds.date) FROM DailyStats ds " +
                    "GROUP BY ds.date, CASE WHEN LOCATE(' [', ds.campaign.partner.name) > 0 THEN SUBSTRING(ds.campaign.partner.name, 1, LOCATE(' [', ds.campaign.partner.name) - 1) ELSE ds.campaign.partner.name END, CASE WHEN LOCATE(' - ', ds.campaign.source.name) > 0 THEN SUBSTRING(ds.campaign.source.name, 1, LOCATE(' - ', ds.campaign.source.name) - 1) ELSE ds.campaign.source.name END",
            nativeQuery = false)
    Page<DailyOverallStatsDto> findAllCombinedDailyStatsWithoutDates(Pageable pageable);
}
