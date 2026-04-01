package com.smarthire.backend.features.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyJobRequest {
    @NotNull(message = "Job ID is required")
    private Long jobId;

    @NotNull(message = "CV File ID is required")
    private Long cvFileId;
}
