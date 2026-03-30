package com.smarthire.backend.features.application.repository;

import com.smarthire.backend.features.application.entity.AiFilterSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiFilterSessionRepository extends JpaRepository<AiFilterSession, Long> {

    List<AiFilterSession> findByJobIdOrderByCreatedAtDesc(Long jobId);

    Optional<AiFilterSession> findByIdAndCreatedById(Long id, Long userId);

    List<AiFilterSession> findByJobIdAndCreatedByIdOrderByCreatedAtDesc(Long jobId, Long userId);
}
