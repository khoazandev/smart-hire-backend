package com.smarthire.backend.features.onboarding.dto;

import com.smarthire.backend.shared.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifiedCvData {
    private Long cvFileId;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String linkedin;
    private String website;
    private String country;
    private String state;
    private String city;
    private Gender gender;
}
