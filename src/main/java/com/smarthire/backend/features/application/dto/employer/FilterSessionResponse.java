package com.smarthire.backend.features.application.dto.employer;

import com.smarthire.backend.shared.enums.FilterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterSessionResponse {

    private Long id;
    private Long jobId;
    private String jobTitle;

    private FilterStatus status;
    private Integer totalCandidates;
    private Integer suitableCount;
    private Integer notSuitableCount;

    private String filterConditions;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String errorMessage;

    private List<FilterResultResponse> results;
}
