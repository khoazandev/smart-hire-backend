package com.smarthire.backend.features.application.entity;

import com.smarthire.backend.shared.enums.FilterClassification;
import com.smarthire.backend.shared.enums.FilterConfidence;
import com.smarthire.backend.shared.enums.FilterPhase;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_filter_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiFilterResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_session_id", nullable = false)
    private AiFilterSession filterSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FilterPhase phase = FilterPhase.PRE_FILTER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FilterClassification classification = FilterClassification.NEEDS_REVIEW;

    @Column(name = "match_score")
    private Integer matchScore;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FilterConfidence confidence = FilterConfidence.MEDIUM;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "JSON")
    private String strengths;

    @Column(name = "missing_requirements", columnDefinition = "JSON")
    private String missingRequirements;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "pre_filter_reason", columnDefinition = "TEXT")
    private String preFilterReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
