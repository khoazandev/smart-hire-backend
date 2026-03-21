package com.smarthire.backend.features.application.controller;

import com.smarthire.backend.core.security.SecurityUtils;
import com.smarthire.backend.features.application.dto.ApplicationResponse;
import com.smarthire.backend.features.application.dto.ApplyJobRequest;
import com.smarthire.backend.features.application.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "APIs for Job Applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/apply")
    @Operation(summary = "Apply to a Job", description = "Candidate submits an application utilizing a specific CV")
    public ResponseEntity<ApplicationResponse> applyToJob(@Valid @RequestBody ApplyJobRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(applicationService.applyToJob(userId, request));
    }
}
