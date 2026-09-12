package com.liochio.otp.service;

import com.liochio.common.dto.otp.OtpServiceConfigDto;
import com.liochio.otp.entity.OtpConfigEntity;
import com.liochio.otp.repository.OtpConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OtpConfigService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OtpConfigService.class);

    private final OtpConfigRepository configRepository;

    @Transactional(readOnly = true)
    public OtpConfigEntity getEffectiveConfig(String tenantId) {
        String effectiveTenantId = (tenantId != null && !tenantId.isBlank()) ? tenantId : "SYSTEM";
        return configRepository.findByTenantId(effectiveTenantId)
                .or(() -> configRepository.findByTenantId("SYSTEM"))
                .orElseGet(() -> OtpConfigEntity.builder()
                        .tenantId(effectiveTenantId)
                        .isEnabled(true)
                        .bypassInDev(true)
                        .devBypassCode("123456")
                        .environment("DEV")
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build());
    }

    @Transactional(readOnly = true)
    public OtpServiceConfigDto getConfigDto(String tenantId) {
        OtpConfigEntity entity = getEffectiveConfig(tenantId);
        return mapToDto(entity);
    }

    @Transactional
    public OtpServiceConfigDto updateConfig(String tenantId, OtpServiceConfigDto dto) {
        String effectiveTenantId = (tenantId != null && !tenantId.isBlank()) ? tenantId : "SYSTEM";
        OtpConfigEntity entity = configRepository.findByTenantId(effectiveTenantId)
                .orElseGet(() -> OtpConfigEntity.builder().tenantId(effectiveTenantId).build());

        if (dto.getIsEnabled() != null) {
            entity.setIsEnabled(dto.getIsEnabled());
        }
        if (dto.getBypassInDev() != null) {
            entity.setBypassInDev(dto.getBypassInDev());
        }
        if (dto.getDevBypassCode() != null && !dto.getDevBypassCode().isBlank()) {
            entity.setDevBypassCode(dto.getDevBypassCode().trim());
        }
        if (dto.getEnvironment() != null && !dto.getEnvironment().isBlank()) {
            entity.setEnvironment(dto.getEnvironment().trim().toUpperCase());
        }
        entity.setUpdatedAt(Instant.now());

        OtpConfigEntity saved = configRepository.save(entity);
        log.info("[OtpConfigService] Cập nhật cấu hình OTP Service cho Tenant: '{}' -> isEnabled: {}, bypassInDev: {}, devBypassCode: '{}'",
                effectiveTenantId, saved.getIsEnabled(), saved.getBypassInDev(), saved.getDevBypassCode());

        return mapToDto(saved);
    }

    public boolean isVerificationBypassed(String tenantId) {
        OtpConfigEntity config = getEffectiveConfig(tenantId);
        // Nếu service bị tắt (isEnabled == false) hoặc cờ bypass_in_dev bật trong môi trường DEV
        return !Boolean.TRUE.equals(config.getIsEnabled()) || 
               (Boolean.TRUE.equals(config.getBypassInDev()) && "DEV".equalsIgnoreCase(config.getEnvironment()));
    }

    public String getDevBypassCode(String tenantId) {
        OtpConfigEntity config = getEffectiveConfig(tenantId);
        return (config.getDevBypassCode() != null && !config.getDevBypassCode().isBlank()) 
                ? config.getDevBypassCode() : "123456";
    }

    private OtpServiceConfigDto mapToDto(OtpConfigEntity entity) {
        return OtpServiceConfigDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .isEnabled(entity.getIsEnabled())
                .bypassInDev(entity.getBypassInDev())
                .devBypassCode(entity.getDevBypassCode())
                .environment(entity.getEnvironment())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
