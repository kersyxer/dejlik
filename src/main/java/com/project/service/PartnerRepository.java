package com.project.service;

import com.project.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

public interface PartnerRepository extends JpaRepository<Partner, UUID> {
    Optional<Partner> findByName(String name);
    Optional<Partner> findByClickflareId(String clickflareId);
}
