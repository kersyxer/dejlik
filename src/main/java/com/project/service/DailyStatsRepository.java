package com.project.service;

import com.project.entity.Campaign;
import com.project.entity.DailyStats;
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

    @Query("SELECT ds.date as date, " +
            "    SUM(ds.cost) as cost, " +
            "    CASE WHEN LOCATE(' [', ds.campaign.partner.name) > 0 " +
            "         THEN SUBSTRING(ds.campaign.partner.name, 1, LOCATE(' [', ds.campaign.partner.name) - 1) " +
            "         ELSE ds.campaign.partner.name " +
            "    END as partnerBaseName, " +
            "    SUM(ds.revenue) as revenue " +
            "FROM DailyStats ds " +
            "WHERE ds.date BETWEEN :start AND :end " +
            "AND (CASE WHEN LOCATE(' - ', ds.campaign.source.name) > 0 " +
            "          THEN LOWER(SUBSTRING(ds.campaign.source.name, 1, LOCATE(' - ', ds.campaign.source.name) - 1)) " +
            "          ELSE LOWER(ds.campaign.source.name) " +
            "     END) = LOWER(:trafficSourceNameBase) " +
            "GROUP BY ds.date, " +
            "         CASE WHEN LOCATE(' [', ds.campaign.partner.name) > 0 " +
            "              THEN SUBSTRING(ds.campaign.partner.name, 1, LOCATE(' [', ds.campaign.partner.name) - 1) " +
            "              ELSE ds.campaign.partner.name " +
            "         END " +
            "ORDER BY ds.date")
    List<Object[]> findCombinedDailyStats(@Param("start") LocalDate start,
                                          @Param("end") LocalDate end,
                                          @Param("trafficSourceNameBase") String trafficSourceNameBase);
}
