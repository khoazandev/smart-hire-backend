package com.smarthire.backend.features.candidate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPreferenceDto {
    private List<String> jobTitles;
    private List<String> preferredLocations;
    private String preferredIndustry;
    private String employmentType;
    private String preferredExperienceLevel;
    private String companySize;
    private String workPreference;
    private Boolean willingToRelocate;
    private LocalDate availabilityDate;
    private String salary;
}
