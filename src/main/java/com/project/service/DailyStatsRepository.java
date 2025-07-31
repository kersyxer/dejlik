package com.project.service;

import com.project.entity.Campaign;
import com.project.entity.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailyStatsRepository extends JpaRepository<DailyStats, UUID> {
    Optional<DailyStats> findByCampaignIdAndDate(UUID campaignId, LocalDate date);
    List<DailyStats> findAllByCampaign_User_IdAndDate(UUID userId, LocalDate date);
    List<DailyStats> findAllByDate(LocalDate date);

    Optional<DailyStats> findByCampaignAndDate(Campaign campaign, LocalDate date);
}
