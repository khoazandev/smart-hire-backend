package com.smarthire.backend.features.onboarding.controller;

import com.smarthire.backend.features.onboarding.dto.OnboardingCompleteRequest;
import com.smarthire.backend.features.onboarding.dto.ParseStatusResponse;
import com.smarthire.backend.features.onboarding.dto.UploadCvResponse;
import com.smarthire.backend.features.onboarding.service.OnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/upload-cv")
    public ResponseEntity<UploadCvResponse> uploadCv(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(onboardingService.uploadCvForOnboarding(file));
    }

    @GetMapping("/parse-status/{cv_file_id}")
    public ResponseEntity<ParseStatusResponse> getParseStatus(@PathVariable("cv_file_id") Long cvFileId) {
        return ResponseEntity.ok(onboardingService.getParseStatus(cvFileId));
    }

    @PostMapping("/complete")
    public ResponseEntity<Void> completeOnboarding(@RequestBody @Valid OnboardingCompleteRequest request) {
        onboardingService.completeOnboarding(request);
        return ResponseEntity.ok().build();
    }
}
