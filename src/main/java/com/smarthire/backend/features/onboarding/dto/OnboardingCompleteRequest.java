package com.smarthire.backend.features.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingCompleteRequest {

    @NotBlank(message = "Role ID (Job level/Headline) is required")
    private String roleId;

    @NotBlank(message = "Experience level is required")
    private String experienceLevel;

    private VerifiedCvData verifiedCvData;
}
