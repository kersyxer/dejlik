package com.project.service;

import com.project.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignRepository extends JpaRepository<Campaign, UUID> {
    Optional<Campaign> findByClickflareId(String clickflareId);
    List<Campaign> findAllByUserId(UUID userId);
}
