package com.smarthire.backend.features.application.dto.employer;

import com.smarthire.backend.shared.enums.FilterClassification;
import com.smarthire.backend.shared.enums.FilterConfidence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterResultResponse {

    private Long applicationId;
    private String candidateName;
    private String candidateEmail;
    private String avatarUrl;
    private String currentTitle;
    private Integer experienceYears;
    private List<String> skills;

    private FilterClassification classification;
    private Integer matchScore;
    private FilterConfidence confidence;
    private String summary;
    private List<String> strengths;
    private List<String> missingRequirements;
    private String recommendation;
    private String preFilterReason;
}
