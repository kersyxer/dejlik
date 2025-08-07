package com.project.service;

import com.project.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, UUID> {
    Optional<Partner> findByName(String name);
    Optional<Partner> findByClickflareId(String clickflareId);

    @Query(value = "SELECT DISTINCT CASE WHEN position(' [' in p.name) > 0 " +
            "         THEN substring(p.name, 1, position(' [' in p.name) - 1) " +
            "         ELSE p.name " +
            "    END FROM partners p", nativeQuery = true)
    List<String> findAllPartnerNames();
}
