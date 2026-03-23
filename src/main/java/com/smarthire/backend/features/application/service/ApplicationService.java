package com.smarthire.backend.features.application.service;

import com.smarthire.backend.features.application.dto.ApplicationResponse;
import com.smarthire.backend.features.application.dto.ApplyJobRequest;

public interface ApplicationService {
    ApplicationResponse applyToJob(Long userId, ApplyJobRequest request);
}
