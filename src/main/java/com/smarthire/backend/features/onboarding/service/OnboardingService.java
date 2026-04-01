package com.smarthire.backend.features.onboarding.service;

import com.smarthire.backend.features.onboarding.dto.OnboardingCompleteRequest;
import com.smarthire.backend.features.onboarding.dto.ParseStatusResponse;
import com.smarthire.backend.features.onboarding.dto.UploadCvResponse;
import org.springframework.web.multipart.MultipartFile;

public interface OnboardingService {
    
    UploadCvResponse uploadCvForOnboarding(MultipartFile file);
    
    ParseStatusResponse getParseStatus(Long cvFileId);
    
    void completeOnboarding(OnboardingCompleteRequest request);
}
