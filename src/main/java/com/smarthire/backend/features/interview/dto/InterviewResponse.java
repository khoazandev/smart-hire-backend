package com.smarthire.backend.features.interview.dto;

import com.smarthire.backend.shared.enums.InterviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InterviewResponse {

    private Long id;
    private Long applicationId;
    private Long createdBy;
    private String roomName;
    private String roomCode;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String meetingUrl;
    private String note;
    private InterviewStatus status;
    private Integer round;
    private Boolean isPassed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
