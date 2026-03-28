package com.smarthire.backend.features.onboarding.service;

import com.smarthire.backend.core.exception.BadRequestException;
import com.smarthire.backend.core.exception.ResourceNotFoundException;
import com.smarthire.backend.core.security.SecurityUtils;
import com.smarthire.backend.features.auth.entity.User;
import com.smarthire.backend.features.auth.repository.UserRepository;
import com.smarthire.backend.features.candidate.dto.CvFileResponse;
import com.smarthire.backend.features.candidate.entity.AiCvParsed;
import com.smarthire.backend.features.candidate.entity.CandidateProfile;
import com.smarthire.backend.features.candidate.entity.CvFile;
import com.smarthire.backend.features.candidate.repository.AiCvParsedRepository;
import com.smarthire.backend.features.candidate.repository.CandidateProfileRepository;
import com.smarthire.backend.features.candidate.repository.CvFileRepository;
import com.smarthire.backend.features.candidate.service.CvFileService;
import com.smarthire.backend.features.onboarding.dto.OnboardingCompleteRequest;
import com.smarthire.backend.features.onboarding.dto.ParseStatusResponse;
import com.smarthire.backend.features.onboarding.dto.UploadCvResponse;
import com.smarthire.backend.features.onboarding.dto.VerifiedCvData;
import com.smarthire.backend.shared.enums.Gender;
import com.smarthire.backend.shared.enums.JobLevel;
import com.smarthire.backend.shared.enums.ParseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingServiceImpl implements OnboardingService {

    private final CandidateProfileRepository profileRepository;
    private final CvFileService cvFileService;
    private final CvFileRepository cvFileRepository;
    private final AiCvParsedRepository aiCvParsedRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UploadCvResponse uploadCvForOnboarding(MultipartFile file) {
        User currentUser = SecurityUtils.getCurrentUser();

        // Check and create empty candidate profile if it doesn't exist yet
        if (!profileRepository.existsByUserId(currentUser.getId())) {
            CandidateProfile newProfile = CandidateProfile.builder()
                    .user(currentUser)
                    .build();
            profileRepository.save(newProfile);
        }

        // Delegate to existing CvFileService to store the file
        CvFileResponse cvFileResponse = cvFileService.uploadCv(file);

        CvFile cvFile = cvFileRepository.findById(cvFileResponse.getId())
                .orElseThrow(() -> new ResourceNotFoundException("CvFile", cvFileResponse.getId()));

        // Create AI Parsing entry
        AiCvParsed aiCvParsed = AiCvParsed.builder()
                .cvFile(cvFile)
                .status(ParseStatus.PROCESSING)
                .build();
        aiCvParsedRepository.save(aiCvParsed);

        return UploadCvResponse.builder()
                .cvFileId(cvFile.getId())
                .status(ParseStatus.PROCESSING.name())
                .message("Đang tiến hành trích xuất dữ liệu CV...")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ParseStatusResponse getParseStatus(Long cvFileId) {
        AiCvParsed aiCvParsed = aiCvParsedRepository.findByCvFileId(cvFileId)
                .orElseThrow(() -> new ResourceNotFoundException("AiCvParsed for CV", cvFileId));

        // For demonstration/mocking purposes: if it's PROCESSING, we just return
        // COMPLETED with mock data
        // In a real scenario, this status is updated by a background job or external AI
        // service callback

        // Mocking completion here
        if (aiCvParsed.getStatus() == ParseStatus.PROCESSING || aiCvParsed.getStatus() == ParseStatus.PENDING) {
            // Note: In real life you'd update the entity to COMPLETED. Since this is
            // readOnly,
            // we will just return mock data if it was processing, simulating rapid
            // processing.

            VerifiedCvData mockData = VerifiedCvData.builder()
                    .cvFileId(cvFileId)
                    .firstName("Tuấn Anh")
                    .lastName("Trần")
                    .phone("0987654321")
                    .email(SecurityUtils.getCurrentUserEmail()) // populate from user
                    .linkedin("https://linkedin.com/in/tuananh")
                    .website("https://github.com/tuananh")
                    .country("VN")
                    .state("Đồng Nai")
                    .city("Biên Hòa")
                    .gender(Gender.MALE)
                    .build();

            return ParseStatusResponse.builder()
                    .status(ParseStatus.COMPLETED)
                    .message("Trích xuất thành công")
                    .data(mockData)
                    .build();
        }

        return ParseStatusResponse.builder()
                .status(aiCvParsed.getStatus())
                .message(aiCvParsed.getErrorMessage())
                .build();
    }

    @Override
    @Transactional
    public void completeOnboarding(OnboardingCompleteRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

        CandidateProfile profile = profileRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> {
                    CandidateProfile newProfile = CandidateProfile.builder()
                            .user(currentUser)
                            .build();
                    return profileRepository.save(newProfile);
                });

        // 1. Update User
        if (request.getVerifiedCvData() != null) {
            VerifiedCvData cvData = request.getVerifiedCvData();
            String firstName = cvData.getFirstName() != null ? cvData.getFirstName().trim() : "";
            String lastName = cvData.getLastName() != null ? cvData.getLastName().trim() : "";
            String fullName = (firstName + " " + lastName).trim();

            if (!fullName.isEmpty()) {
                currentUser.setFullName(fullName);
            }
            if (cvData.getPhone() != null && !cvData.getPhone().isEmpty()) {
                currentUser.setPhone(cvData.getPhone());
            }
            userRepository.save(currentUser);

            // 2. Update CandidateProfile with detailed info
            profile.setGender(cvData.getGender());
            profile.setCountry(cvData.getCountry());
            profile.setState(cvData.getState());
            profile.setCity(cvData.getCity());
            profile.setLinkedinUrl(cvData.getLinkedin());
            profile.setPersonalWebsite(cvData.getWebsite());
        }

        // 3. Update job level and role
        try {
            JobLevel jobLevel = JobLevel.valueOf(request.getExperienceLevel().toUpperCase());
            profile.setJobLevel(jobLevel);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid experience level provided: {}", request.getExperienceLevel());
        }

        profile.setHeadline(request.getRoleId());

        profileRepository.save(profile);

        // Mark user as onboarded
        currentUser.setIsOnboarded(true);
        userRepository.save(currentUser);

        log.info("Onboarding completed for user: {}", currentUser.getEmail());
    }
}