package com.project.service;

import com.project.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SourceRepository extends JpaRepository<Source, UUID> {
    Optional<Source> findByName(String name);
    Optional<Source> findByClickflareId(String clickflareId);
}
