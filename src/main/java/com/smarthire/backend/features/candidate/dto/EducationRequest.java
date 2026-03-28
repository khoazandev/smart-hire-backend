package com.smarthire.backend.features.candidate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationRequest {

    @NotBlank(message = "Institution is required")
    @Size(max = 255, message = "Institution must be less than 255 characters")
    private String institution;

    @Size(max = 100, message = "Degree must be less than 100 characters")
    private String degree;

    @Size(max = 255, message = "Field of study must be less than 255 characters")
    private String fieldOfStudy;

    private String startDate;

    private String endDate;

    private BigDecimal gpa;

    private String description;
}
