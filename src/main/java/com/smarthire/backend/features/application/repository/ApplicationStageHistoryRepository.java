package com.smarthire.backend.features.application.repository;

import com.smarthire.backend.features.application.entity.ApplicationStageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationStageHistoryRepository extends JpaRepository<ApplicationStageHistory, Long> {
}
