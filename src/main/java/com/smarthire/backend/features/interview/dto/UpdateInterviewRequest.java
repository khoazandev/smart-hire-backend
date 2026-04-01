package com.smarthire.backend.features.interview.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateInterviewRequest {

    private String roomName;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String meetingUrl;
    private String note;
    private Boolean isPassed;
}
