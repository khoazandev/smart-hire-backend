package com.smarthire.backend.features.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthire.backend.core.exception.ResourceNotFoundException;
import com.smarthire.backend.features.application.dto.employer.FilterResultResponse;
import com.smarthire.backend.features.application.dto.employer.FilterSessionResponse;
import com.smarthire.backend.features.application.dto.employer.RunFilterRequest;
import com.smarthire.backend.features.application.entity.AiFilterResult;
import com.smarthire.backend.features.application.entity.AiFilterSession;
import com.smarthire.backend.features.application.entity.Application;
import com.smarthire.backend.features.application.repository.AiFilterResultRepository;
import com.smarthire.backend.features.application.repository.AiFilterSessionRepository;
import com.smarthire.backend.features.application.repository.ApplicationRepository;
import com.smarthire.backend.features.auth.entity.User;
import com.smarthire.backend.features.auth.repository.UserRepository;
import com.smarthire.backend.features.candidate.entity.CandidateProfile;
import com.smarthire.backend.features.candidate.entity.CvFile;
import com.smarthire.backend.features.job.entity.Job;
import com.smarthire.backend.features.job.repository.JobRepository;
import com.smarthire.backend.infrastructure.ai.client.GeminiClient;
import com.smarthire.backend.infrastructure.ai.prompts.PromptTemplates;
import com.smarthire.backend.infrastructure.storage.FileStorageService;
import com.smarthire.backend.shared.enums.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiFilterServiceImpl implements AiFilterService {

    private final GeminiClient geminiClient;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final AiFilterSessionRepository filterSessionRepository;
    private final AiFilterResultRepository filterResultRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  PUBLIC API
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    @Override
    @Transactional
    public FilterSessionResponse runFilter(Long jobId, Long employerId, RunFilterRequest conditions) {
        log.info("🤖 Smart Filter — jobId={}, employerId={}", jobId, employerId);

        // 1. Validate
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));

        if (!job.getCreatedBy().getId().equals(employerId)) {
            throw new IllegalArgumentException("Bạn không có quyền lọc CV cho tin tuyển dụng này.");
        }

        if (job.getDescription() == null || job.getDescription().isBlank()) {
            throw new IllegalArgumentException("Tin tuyển dụng phải có mô tả công việc (JD) trước khi chạy AI filter.");
        }

        List<Application> applications = applicationRepository.findByJobIdOrderByAppliedAtDesc(jobId);
        if (applications.isEmpty()) {
            throw new IllegalArgumentException("Chưa có ứng viên nào ứng tuyển vào vị trí này.");
        }

        User employer = userRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", employerId));

        // 2. Create session
        String conditionsJson = serializeConditions(conditions);
        AiFilterSession session = AiFilterSession.builder()
                .job(job)
                .createdBy(employer)
                .status(FilterStatus.PENDING)
                .totalCandidates(applications.size())
                .filterConditions(conditionsJson)
                .build();
        session = filterSessionRepository.save(session);

        try {
            // 3. Phase 1 — Pre-filter
            session.setStatus(FilterStatus.PRE_FILTERING);
            filterSessionRepository.save(session);

            Map<Long, String> preFilterResults = runPreFilter(job, applications, conditionsJson);

            // Save pre-filter results
            List<Long> suitableAppIds = new ArrayList<>();
            for (Application app : applications) {
                String classification = preFilterResults.getOrDefault(app.getId(), "NEEDS_REVIEW");

                // Extract reason from the full pre-filter data (stored temporarily)
                AiFilterResult result = AiFilterResult.builder()
                        .filterSession(session)
                        .application(app)
                        .phase(FilterPhase.PRE_FILTER)
                        .classification(mapPreFilterClassification(classification))
                        .confidence(FilterConfidence.MEDIUM)
                        .preFilterReason(preFilterReasons.getOrDefault(app.getId(), ""))
                        .build();
                filterResultRepository.save(result);

                if ("SUITABLE".equals(classification) || "NEEDS_REVIEW".equals(classification)) {
                    suitableAppIds.add(app.getId());
                }
            }

            int notSuitableCount = applications.size() - suitableAppIds.size();
            session.setNotSuitableCount(notSuitableCount);
            filterSessionRepository.save(session);

            // 4. Phase 2 — Deep Evaluation (only for suitable candidates)
            session.setStatus(FilterStatus.DEEP_EVALUATING);
            filterSessionRepository.save(session);

            int suitableCount = 0;
            for (Application app : applications) {
                if (!suitableAppIds.contains(app.getId())) continue;

                try {
                    DeepEvalResult deepResult = runDeepEvaluation(job, app, conditionsJson);

                    // Update the existing pre-filter result with deep eval data
                    var existingResults = filterResultRepository
                            .findByFilterSessionIdAndPhase(session.getId(), FilterPhase.PRE_FILTER);
                    for (AiFilterResult existing : existingResults) {
                        if (existing.getApplication().getId().equals(app.getId())) {
                            existing.setPhase(FilterPhase.DEEP_EVAL);
                            existing.setMatchScore(deepResult.matchScore);
                            existing.setClassification(deepResult.classification);
                            existing.setConfidence(deepResult.confidence);
                            existing.setSummary(deepResult.summary);
                            existing.setStrengths(toJson(deepResult.strengths));
                            existing.setMissingRequirements(toJson(deepResult.missingRequirements));
                            existing.setRecommendation(deepResult.recommendation);
                            filterResultRepository.save(existing);
                            break;
                        }
                    }

                    if (deepResult.classification != FilterClassification.NOT_SUITABLE) {
                        suitableCount++;
                    }
                } catch (Exception e) {
                    log.error("Deep eval failed for applicationId={}: {}", app.getId(), e.getMessage());
                }
            }

            // 5. Complete
            session.setSuitableCount(suitableCount);
            session.setStatus(FilterStatus.COMPLETED);
            session.setCompletedAt(LocalDateTime.now());
            filterSessionRepository.save(session);

        } catch (Exception e) {
            log.error("Smart Filter failed: {}", e.getMessage(), e);
            session.setStatus(FilterStatus.FAILED);
            session.setErrorMessage(e.getMessage());
            filterSessionRepository.save(session);
        }

        return toSessionResponse(session);
    }

    @Override
    public FilterSessionResponse getFilterSession(Long sessionId, Long employerId) {
        AiFilterSession session = filterSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("FilterSession", sessionId));

        if (!session.getCreatedBy().getId().equals(employerId)) {
            throw new IllegalArgumentException("Bạn không có quyền xem kết quả này.");
        }

        return toSessionResponse(session);
    }

    @Override
    public List<FilterSessionResponse> getFilterHistory(Long jobId, Long employerId) {
        return filterSessionRepository.findByJobIdAndCreatedByIdOrderByCreatedAtDesc(jobId, employerId)
                .stream()
                .map(this::toSessionResponseBrief)
                .collect(Collectors.toList());
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  PHASE 1 — PRE-FILTER
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    // Temp storage for reasons across the method call
    private Map<Long, String> preFilterReasons = new HashMap<>();

    private Map<Long, String> runPreFilter(Job job, List<Application> applications, String conditionsJson) {
        log.info("📋 Phase 1: Pre-filtering {} candidates", applications.size());

        // Build candidate snippets JSON
        List<Map<String, Object>> snippets = new ArrayList<>();
        for (Application app : applications) {
            CandidateProfile profile = app.getCandidateProfile();
            Map<String, Object> snippet = new LinkedHashMap<>();
            snippet.put("candidateId", app.getId());
            snippet.put("name", profile.getUser().getFullName());
            snippet.put("currentTitle", profile.getHeadline() != null ? profile.getHeadline() : "");
            snippet.put("summary", profile.getSummary() != null ? profile.getSummary() : "");
            snippet.put("yearsOfExperience", profile.getYearsOfExperience() != null ? profile.getYearsOfExperience() : 0);
            snippets.add(snippet);
        }

        String snippetsJson;
        try {
            snippetsJson = objectMapper.writeValueAsString(snippets);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize candidate snippets", e);
        }

        String skills = extractJobSkills(job);

        String prompt = String.format(PromptTemplates.CV_BATCH_PRE_FILTER_PROMPT,
                job.getTitle(),
                safe(job.getDescription()),
                safe(job.getRequirements()),
                skills,
                job.getJobLevel() != null ? job.getJobLevel().name() : "N/A",
                conditionsJson,
                snippetsJson
        );

        String aiResponse = geminiClient.chat(prompt);
        return parsePreFilterResponse(aiResponse);
    }

    private Map<Long, String> parsePreFilterResponse(String aiResponse) {
        Map<Long, String> results = new HashMap<>();
        preFilterReasons = new HashMap<>();

        try {
            String json = cleanJsonResponse(aiResponse);
            JsonNode array = objectMapper.readTree(json);

            if (array.isArray()) {
                for (JsonNode node : array) {
                    long candidateId = node.get("candidateId").asLong();
                    String classification = node.has("classification") ? node.get("classification").asText() : "NEEDS_REVIEW";
                    String reason = node.has("reason") ? node.get("reason").asText() : "";
                    results.put(candidateId, classification);
                    preFilterReasons.put(candidateId, reason);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse pre-filter response: {}", e.getMessage());
        }
        return results;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  PHASE 2 — DEEP EVALUATION
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private DeepEvalResult runDeepEvaluation(Job job, Application app, String conditionsJson) {
        log.info("🔍 Phase 2: Deep evaluating applicationId={}", app.getId());

        // Extract CV content by uploading to Gemini
        CvFile cvFile = app.getCvFile();
        String cvContent;
        try {
            Path filePath = fileStorageService.getFilePath(cvFile.getFilePath());
            String mimeType = getMimeType(cvFile.getFileName());
            cvContent = geminiClient.chatWithFile(filePath, mimeType,
                    "Extract all text content from this CV/Resume document. Return only the plain text, no formatting or markdown.");
        } catch (Exception e) {
            log.warn("Could not extract CV content for appId={}: {}", app.getId(), e.getMessage());
            cvContent = "CV content could not be extracted.";
        }

        String skills = extractJobSkills(job);

        String prompt = String.format(PromptTemplates.CV_DEEP_EVALUATION_PROMPT,
                job.getTitle(),
                safe(job.getDescription()),
                safe(job.getRequirements()),
                skills,
                job.getJobLevel() != null ? job.getJobLevel().name() : "N/A",
                conditionsJson,
                cvContent
        );

        String aiResponse = geminiClient.chat(prompt);
        return parseDeepEvalResponse(aiResponse);
    }

    private DeepEvalResult parseDeepEvalResponse(String aiResponse) {
        DeepEvalResult result = new DeepEvalResult();
        try {
            String json = cleanJsonResponse(aiResponse);
            JsonNode node = objectMapper.readTree(json);

            result.matchScore = node.has("matchScore") ? node.get("matchScore").asInt() : 50;
            result.classification = mapClassification(node.has("classification") ? node.get("classification").asText() : "MODERATE_FIT");
            result.confidence = mapConfidence(node.has("confidence") ? node.get("confidence").asText() : "MEDIUM");
            result.summary = node.has("summary") ? node.get("summary").asText() : "";
            result.recommendation = node.has("recommendation") ? node.get("recommendation").asText() : "";

            result.strengths = new ArrayList<>();
            if (node.has("strengths") && node.get("strengths").isArray()) {
                for (JsonNode s : node.get("strengths")) {
                    result.strengths.add(s.asText());
                }
            }

            result.missingRequirements = new ArrayList<>();
            if (node.has("missingRequirements") && node.get("missingRequirements").isArray()) {
                for (JsonNode m : node.get("missingRequirements")) {
                    result.missingRequirements.add(m.asText());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse deep eval response: {}", e.getMessage());
            result.matchScore = 0;
            result.classification = FilterClassification.NEEDS_REVIEW;
        }
        return result;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  MAPPERS & HELPERS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private static class DeepEvalResult {
        int matchScore;
        FilterClassification classification = FilterClassification.MODERATE_FIT;
        FilterConfidence confidence = FilterConfidence.MEDIUM;
        String summary = "";
        List<String> strengths = new ArrayList<>();
        List<String> missingRequirements = new ArrayList<>();
        String recommendation = "";
    }

    private FilterClassification mapPreFilterClassification(String raw) {
        return switch (raw.toUpperCase()) {
            case "SUITABLE" -> FilterClassification.MODERATE_FIT; // will be refined in Phase 2
            case "NOT_SUITABLE" -> FilterClassification.NOT_SUITABLE;
            default -> FilterClassification.NEEDS_REVIEW;
        };
    }

    private FilterClassification mapClassification(String raw) {
        return switch (raw.toUpperCase().replace(" ", "_")) {
            case "STRONG_FIT" -> FilterClassification.STRONG_FIT;
            case "MODERATE_FIT" -> FilterClassification.MODERATE_FIT;
            case "WEAK_FIT" -> FilterClassification.WEAK_FIT;
            case "NOT_SUITABLE" -> FilterClassification.NOT_SUITABLE;
            default -> FilterClassification.NEEDS_REVIEW;
        };
    }

    private FilterConfidence mapConfidence(String raw) {
        return switch (raw.toUpperCase()) {
            case "HIGH" -> FilterConfidence.HIGH;
            case "LOW" -> FilterConfidence.LOW;
            default -> FilterConfidence.MEDIUM;
        };
    }

    private String extractJobSkills(Job job) {
        if (job.getSkills() == null || job.getSkills().isEmpty()) return "N/A";
        return job.getSkills().stream()
                .map(s -> s.getSkillName())
                .collect(Collectors.joining(", "));
    }

    private String safe(String s) {
        return s != null ? s : "N/A";
    }

    private String serializeConditions(RunFilterRequest conditions) {
        if (conditions == null) return "{}";
        try {
            return objectMapper.writeValueAsString(conditions);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private String getMimeType(String fileName) {
        if (fileName == null) return "application/pdf";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        return "application/pdf";
    }

    private String cleanJsonResponse(String response) {
        if (response == null) return "{}";
        String cleaned = response.trim();
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            int lastBacktick = cleaned.lastIndexOf("```");
            if (firstNewline > 0 && lastBacktick > firstNewline) {
                cleaned = cleaned.substring(firstNewline + 1, lastBacktick).trim();
            }
        }
        return cleaned;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    //  RESPONSE MAPPERS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private FilterSessionResponse toSessionResponse(AiFilterSession session) {
        List<AiFilterResult> results = filterResultRepository
                .findByFilterSessionIdOrderByMatchScoreDesc(session.getId());

        List<FilterResultResponse> resultResponses = results.stream()
                .map(this::toResultResponse)
                .collect(Collectors.toList());

        return FilterSessionResponse.builder()
                .id(session.getId())
                .jobId(session.getJob().getId())
                .jobTitle(session.getJob().getTitle())
                .status(session.getStatus())
                .totalCandidates(session.getTotalCandidates())
                .suitableCount(session.getSuitableCount())
                .notSuitableCount(session.getNotSuitableCount())
                .filterConditions(session.getFilterConditions())
                .createdAt(session.getCreatedAt())
                .completedAt(session.getCompletedAt())
                .errorMessage(session.getErrorMessage())
                .results(resultResponses)
                .build();
    }

    private FilterSessionResponse toSessionResponseBrief(AiFilterSession session) {
        return FilterSessionResponse.builder()
                .id(session.getId())
                .jobId(session.getJob().getId())
                .jobTitle(session.getJob().getTitle())
                .status(session.getStatus())
                .totalCandidates(session.getTotalCandidates())
                .suitableCount(session.getSuitableCount())
                .notSuitableCount(session.getNotSuitableCount())
                .createdAt(session.getCreatedAt())
                .completedAt(session.getCompletedAt())
                .build();
    }

    private FilterResultResponse toResultResponse(AiFilterResult result) {
        Application app = result.getApplication();
        CandidateProfile profile = app.getCandidateProfile();

        return FilterResultResponse.builder()
                .applicationId(app.getId())
                .candidateName(profile.getUser().getFullName())
                .candidateEmail(profile.getUser().getEmail())
                .avatarUrl(profile.getUser().getAvatarUrl())
                .currentTitle(profile.getHeadline())
                .experienceYears(profile.getYearsOfExperience())
                .skills(List.of()) // Could be populated from candidate_skills
                .classification(result.getClassification())
                .matchScore(result.getMatchScore())
                .confidence(result.getConfidence())
                .summary(result.getSummary())
                .strengths(fromJson(result.getStrengths()))
                .missingRequirements(fromJson(result.getMissingRequirements()))
                .recommendation(result.getRecommendation())
                .preFilterReason(result.getPreFilterReason())
                .build();
    }
}
