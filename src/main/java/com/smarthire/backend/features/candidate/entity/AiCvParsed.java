package com.smarthire.backend.features.candidate.entity;

import com.smarthire.backend.shared.enums.ParseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_cv_parsed")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiCvParsed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_file_id", nullable = false)
    private CvFile cvFile;

    @Column(name = "parsed_data", columnDefinition = "JSON")
    private String parsedData;

    @Enumerated(EnumType.STRING)
    private ParseStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ParseStatus.PENDING;
        }
    }
}
