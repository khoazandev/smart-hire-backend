package com.smarthire.backend.features.application.repository;

import com.smarthire.backend.features.application.entity.AiFilterResult;
import com.smarthire.backend.shared.enums.FilterPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiFilterResultRepository extends JpaRepository<AiFilterResult, Long> {

    List<AiFilterResult> findByFilterSessionIdOrderByMatchScoreDesc(Long filterSessionId);

    List<AiFilterResult> findByFilterSessionIdAndPhase(Long filterSessionId, FilterPhase phase);
}
