package com.smarthire.backend.features.candidate.dto;

import lombok.*;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceResponse {
    private Long id;
    private String companyName;
    private String title;
    private String startDate;
    private String endDate;
    private Boolean isCurrent;
    private String description;
}
