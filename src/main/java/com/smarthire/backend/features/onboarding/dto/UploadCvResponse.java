package com.smarthire.backend.features.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadCvResponse {
    private Long cvFileId;
    private String status;
    private String message;
}
