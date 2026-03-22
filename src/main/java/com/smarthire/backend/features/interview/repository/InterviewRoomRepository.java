package com.smarthire.backend.features.interview.repository;

import com.smarthire.backend.features.interview.entity.InterviewRoom;
import com.smarthire.backend.shared.enums.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRoomRepository extends JpaRepository<InterviewRoom, Long> {

    List<InterviewRoom> findByApplicationIdOrderByScheduledAtDesc(Long applicationId);

    List<InterviewRoom> findByCreatedByIdOrderByScheduledAtDesc(Long userId);

    List<InterviewRoom> findByStatusOrderByScheduledAtAsc(InterviewStatus status);

    Optional<InterviewRoom> findByRoomCode(String roomCode);
}
