package com.smarthire.backend.features.auth.dto;

import lombok.Builder;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserProfileResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String role;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("isOnboarded")
    private Boolean isOnboarded;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
