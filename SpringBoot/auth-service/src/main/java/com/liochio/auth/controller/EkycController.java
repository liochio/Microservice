package com.liochio.auth.controller;

import com.liochio.auth.dto.EkycSubmitRequest;
import com.liochio.auth.entity.UserEntity;
import com.liochio.auth.repository.UserRepository;
import com.liochio.common.annotation.RequireRole;
import com.liochio.common.context.UserContext;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@RestController
@RequestMapping({"/api/v1/ekyc", "/api/ekyc"})
@RequiredArgsConstructor
@Tag(name = "eKYC & Tier Limits Controller", description = "Các API định danh danh tính eKYC và nâng hạn mức giao dịch theo Tier")
public class EkycController {

    private final UserRepository userRepository;

    @GetMapping("/status")
    @Operation(summary = "Xem trạng thái định danh eKYC và hạn mức giao dịch hiện tại")
    public ApiResponse<EkycSubmitRequest.EkycResponseDto> getMyEkycStatus() {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 5L;

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return ApiResponse.success(mapToDto(user));
    }

    @PostMapping("/submit")
    @Operation(summary = "Gửi hồ sơ định danh CCCD/Hộ chiếu để nâng cấp Tier")
    public ApiResponse<EkycSubmitRequest.EkycResponseDto> submitEkyc(@Valid @RequestBody EkycSubmitRequest.SubmitDto request) {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 5L;

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setIdCardNumber(request.getIdCardNumber());
        if (request.getIdCardType() != null) {
            user.setIdCardType(request.getIdCardType());
        }
        user.setFullName(request.getFullName());
        user.setEkycStatus("PENDING_REVIEW");
        userRepository.save(user);

        log.info("[EkycController] User ID: {} đã nộp hồ sơ eKYC: {}", userId, request.getIdCardNumber());
        return ApiResponse.success(mapToDto(user), "Hồ sơ eKYC đã được tiếp nhận và đang chờ duyệt");
    }

    @PostMapping("/approve/{userId}")
    @RequireRole({"SUPER_ADMIN", "ADMIN"})
    @Operation(summary = "Duyệt hồ sơ eKYC và nâng cấp Tier hạn mức (Dành cho Admin)")
    public ApiResponse<EkycSubmitRequest.EkycResponseDto> approveEkyc(
            @PathVariable Long userId,
            @RequestBody(required = false) EkycSubmitRequest.ApproveDto request
    ) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String targetTier = (request != null && request.getTargetEkycLevel() != null)
                ? request.getTargetEkycLevel() : "TIER_2";

        BigDecimal newLimit = switch (targetTier) {
            case "TIER_3" -> new BigDecimal("999999999999.00");
            case "TIER_2" -> new BigDecimal("500000000.00");
            default -> new BigDecimal("5000000.00");
        };

        if (request != null && request.getCustomDailyLimit() != null) {
            newLimit = request.getCustomDailyLimit();
        }

        user.setEkycLevel(targetTier);
        user.setEkycStatus("VERIFIED");
        user.setEkycVerifiedAt(Instant.now());
        user.setDailyTransferLimit(newLimit);
        userRepository.save(user);

        log.info("[EkycController] Admin đã duyệt eKYC cho User ID: {}, Level mới: {}, Hạn mức: {} VND",
                userId, targetTier, newLimit);

        return ApiResponse.success(mapToDto(user), "Phê duyệt eKYC thành công. Hạn mức đã được nâng lên " + targetTier);
    }

    private EkycSubmitRequest.EkycResponseDto mapToDto(UserEntity user) {
        return EkycSubmitRequest.EkycResponseDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .idCardNumber(user.getIdCardNumber())
                .idCardType(user.getIdCardType())
                .ekycLevel(user.getEkycLevel())
                .ekycStatus(user.getEkycStatus())
                .dailyTransferLimit(user.getDailyTransferLimit())
                .ekycVerifiedAt(user.getEkycVerifiedAt())
                .build();
    }
}
