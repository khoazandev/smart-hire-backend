package com.smarthire.backend.features.auth.dto;

import lombok.Builder;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long userId;
    private String email;
    private String fullName;
    private String role;
    
    @JsonProperty("isOnboarded")
    private Boolean isOnboarded;

}
