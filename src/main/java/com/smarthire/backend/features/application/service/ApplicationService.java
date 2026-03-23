package com.smarthire.backend.features.application.service;

import com.smarthire.backend.features.application.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ApplicationService {
    // ── Candidate features (BE021 & BE020) ──
    ApplicationResponse applyToJob(Long userId, ApplyJobRequest request);
    Page<ApplicationTrackingResponse> getCandidateApplications(Long userId, Pageable pageable);
    ApplicationDetailResponse getApplicationDetail(Long userId, Long applicationId);

    // ── Management features (develop) ──
    ApplicationResponse getApplicationById(Long id);
    List<ApplicationResponse> getApplicationsByJob(Long jobId, String stage);
    ApplicationResponse changeStage(Long applicationId, Long userId, ChangeStageRequest request);
}
