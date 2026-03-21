package com.smarthire.backend.features.interview.service;

import com.smarthire.backend.core.exception.BadRequestException;
import com.smarthire.backend.core.exception.ForbiddenException;
import com.smarthire.backend.core.exception.ResourceNotFoundException;
import com.smarthire.backend.core.security.SecurityUtils;
import com.smarthire.backend.features.application.entity.Application;
import com.smarthire.backend.features.application.repository.ApplicationRepository;
import com.smarthire.backend.features.auth.entity.User;
import com.smarthire.backend.features.interview.dto.CreateInterviewRequest;
import com.smarthire.backend.features.interview.dto.InterviewResponse;
import com.smarthire.backend.features.interview.dto.UpdateInterviewRequest;
import com.smarthire.backend.features.interview.entity.InterviewRoom;
import com.smarthire.backend.features.interview.repository.InterviewRoomRepository;
import com.smarthire.backend.shared.enums.InterviewStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRoomRepository interviewRoomRepository;
    private final ApplicationRepository applicationRepository;

    @Override
    @Transactional
    public InterviewResponse createInterview(CreateInterviewRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + request.getApplicationId()));

        String roomCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        InterviewRoom room = InterviewRoom.builder()
                .application(application)
                .createdBy(currentUser)
                .roomName(request.getRoomName())
                .roomCode(roomCode)
                .scheduledAt(request.getScheduledAt())
                .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 60)
                .meetingUrl(request.getMeetingUrl())
                .note(request.getNote())
                .status(InterviewStatus.SCHEDULED)
                .build();

        InterviewRoom saved = interviewRoomRepository.save(room);
        log.info("Interview room created: {} (code={}) by {}", saved.getRoomName(), roomCode, currentUser.getEmail());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewResponse getInterviewById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviewsByApplication(Long applicationId) {
        return interviewRoomRepository.findByApplicationIdOrderByScheduledAtDesc(applicationId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponse> getMyInterviews() {
        User currentUser = SecurityUtils.getCurrentUser();
        return interviewRoomRepository.findByCreatedByIdOrderByScheduledAtDesc(currentUser.getId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public InterviewResponse updateInterview(Long id, UpdateInterviewRequest request) {
        InterviewRoom room = findOrThrow(id);
        checkOwnership(room);

        if (request.getRoomName() != null) room.setRoomName(request.getRoomName());
        if (request.getScheduledAt() != null) room.setScheduledAt(request.getScheduledAt());
        if (request.getDurationMinutes() != null) room.setDurationMinutes(request.getDurationMinutes());
        if (request.getMeetingUrl() != null) room.setMeetingUrl(request.getMeetingUrl());
        if (request.getNote() != null) room.setNote(request.getNote());

        InterviewRoom saved = interviewRoomRepository.save(room);
        log.info("Interview room updated: {}", saved.getRoomName());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public InterviewResponse changeStatus(Long id, String status) {
        InterviewRoom room = findOrThrow(id);
        checkOwnership(room);

        InterviewStatus newStatus;
        try {
            newStatus = InterviewStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status. Must be: SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED");
        }

        room.setStatus(newStatus);
        InterviewRoom saved = interviewRoomRepository.save(room);
        log.info("Interview room {} status changed to {}", saved.getRoomName(), newStatus);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteInterview(Long id) {
        InterviewRoom room = findOrThrow(id);
        checkOwnership(room);
        interviewRoomRepository.delete(room);
        log.info("Interview room deleted: {}", room.getRoomName());
    }

    // ── Helpers ──

    private InterviewRoom findOrThrow(Long id) {
        return interviewRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview room not found with id: " + id));
    }

    private void checkOwnership(InterviewRoom room) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (!room.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You do not have permission to modify this interview room");
        }
    }

    private InterviewResponse toResponse(InterviewRoom room) {
        return InterviewResponse.builder()
                .id(room.getId())
                .applicationId(room.getApplication().getId())
                .createdBy(room.getCreatedBy().getId())
                .roomName(room.getRoomName())
                .roomCode(room.getRoomCode())
                .scheduledAt(room.getScheduledAt())
                .durationMinutes(room.getDurationMinutes())
                .meetingUrl(room.getMeetingUrl())
                .note(room.getNote())
                .status(room.getStatus())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
