package com.smarthire.backend.features.application.service;

import com.smarthire.backend.features.application.dto.employer.FilterSessionResponse;
import com.smarthire.backend.features.application.dto.employer.RunFilterRequest;

import java.util.List;

/**
 * AI Smart Filtering Service for HR.
 * 2-phase approach: Pre-filter (title/summary) → Deep Evaluation (full CV).
 */
public interface AiFilterService {

    /**
     * Run AI filter on all applicants of a job.
     * Phase 1: Batch pre-filter by title/summary.
     * Phase 2: Deep evaluation for suitable candidates.
     */
    FilterSessionResponse runFilter(Long jobId, Long employerId, RunFilterRequest conditions);

    /**
     * Get a specific filter session result.
     */
    FilterSessionResponse getFilterSession(Long sessionId, Long employerId);

    /**
     * Get history of all filter runs for a job.
     */
    List<FilterSessionResponse> getFilterHistory(Long jobId, Long employerId);
}
