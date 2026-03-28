package com.smarthire.backend.features.onboarding.dto;

import com.smarthire.backend.shared.enums.ParseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseStatusResponse {
    private ParseStatus status;
    private String message;
    private VerifiedCvData data;
}
