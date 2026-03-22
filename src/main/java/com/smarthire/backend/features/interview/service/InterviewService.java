package com.smarthire.backend.features.interview.service;

import com.smarthire.backend.features.interview.dto.CreateInterviewRequest;
import com.smarthire.backend.features.interview.dto.InterviewResponse;
import com.smarthire.backend.features.interview.dto.UpdateInterviewRequest;

import java.util.List;

public interface InterviewService {

    InterviewResponse createInterview(CreateInterviewRequest request);

    InterviewResponse getInterviewById(Long id);

    List<InterviewResponse> getInterviewsByApplication(Long applicationId);

    List<InterviewResponse> getMyInterviews();

    InterviewResponse updateInterview(Long id, UpdateInterviewRequest request);

    InterviewResponse changeStatus(Long id, String status);

    void deleteInterview(Long id);
}
