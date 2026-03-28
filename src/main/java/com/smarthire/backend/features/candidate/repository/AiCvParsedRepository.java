package com.smarthire.backend.features.candidate.repository;

import com.smarthire.backend.features.candidate.entity.AiCvParsed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiCvParsedRepository extends JpaRepository<AiCvParsed, Long> {
    Optional<AiCvParsed> findByCvFileId(Long cvFileId);
}
