package com.smarthire.backend.features.application.entity;

import com.smarthire.backend.features.auth.entity.User;
import com.smarthire.backend.features.job.entity.Job;
import com.smarthire.backend.shared.enums.FilterStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ai_filter_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiFilterSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FilterStatus status = FilterStatus.PENDING;

    @Column(name = "total_candidates", nullable = false)
    @Builder.Default
    private Integer totalCandidates = 0;

    @Column(name = "suitable_count", nullable = false)
    @Builder.Default
    private Integer suitableCount = 0;

    @Column(name = "not_suitable_count", nullable = false)
    @Builder.Default
    private Integer notSuitableCount = 0;

    @Column(name = "filter_conditions", columnDefinition = "JSON")
    private String filterConditions;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder.Default
    @OneToMany(mappedBy = "filterSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiFilterResult> results = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
