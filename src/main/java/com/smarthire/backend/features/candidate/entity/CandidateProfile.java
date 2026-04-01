package com.smarthire.backend.features.candidate.entity;

import com.smarthire.backend.features.auth.entity.User;
import com.smarthire.backend.shared.enums.Gender;
import com.smarthire.backend.shared.enums.JobLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 255)
    private String headline;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String city;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "personal_website", length = 500)
    private String personalWebsite;

    @Column(name = "job_titles", length = 500)
    private String jobTitles;

    @Column(name = "preferred_locations", length = 500)
    private String preferredLocations;

    @Column(name = "preferred_industry", length = 100)
    private String preferredIndustry;

    @Column(name = "employment_type", length = 100)
    private String employmentType;

    @Column(name = "preferred_experience_level", length = 100)
    private String preferredExperienceLevel;

    @Column(name = "company_size", length = 50)
    private String companySize;

    @Column(name = "work_preference", length = 50)
    private String workPreference;

    @Column(name = "willing_to_relocate")
    private Boolean willingToRelocate;

    @Column(name = "availability_date")
    private LocalDate availabilityDate;

    @Column(name = "expected_salary", length = 100)
    private String expectedSalary;

    @Column(name = "years_of_experience")
    @Builder.Default
    private Integer yearsOfExperience = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_level")
    private JobLevel jobLevel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
