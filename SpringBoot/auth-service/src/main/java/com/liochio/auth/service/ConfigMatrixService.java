package com.liochio.auth.service;

import com.liochio.auth.dto.SystemConfigDto;
import com.liochio.auth.entity.GlobalSystemConfigEntity;
import com.liochio.auth.entity.TenantConfigOverrideEntity;
import com.liochio.auth.repository.GlobalSystemConfigRepository;
import com.liochio.auth.repository.TenantConfigOverrideRepository;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigMatrixService {

    private final GlobalSystemConfigRepository globalRepo;
    private final TenantConfigOverrideRepository overrideRepo;

    @Transactional(readOnly = true)
    public List<SystemConfigDto> getEffectiveConfigs(String tenantId, String category) {
        List<GlobalSystemConfigEntity> globals = (category != null && !category.isBlank())
                ? globalRepo.findByCategory(category.toUpperCase())
                : globalRepo.findAll();

        Map<String, TenantConfigOverrideEntity> overrideMap = Collections.emptyMap();
        if (tenantId != null && !tenantId.isBlank()) {
            List<TenantConfigOverrideEntity> overrides = overrideRepo.findByTenantIdAndIsActiveTrue(tenantId);
            overrideMap = overrides.stream().collect(Collectors.toMap(TenantConfigOverrideEntity::getConfigKey, o -> o, (k1, k2) -> k1));
        }

        List<SystemConfigDto> result = new ArrayList<>();
        for (GlobalSystemConfigEntity g : globals) {
            TenantConfigOverrideEntity o = overrideMap.get(g.getConfigKey());
            boolean isOverridden = (o != null && o.getOverrideValue() != null && !o.getOverrideValue().isBlank());
            String effectiveVal = isOverridden ? o.getOverrideValue() : g.getValue();

            result.add(SystemConfigDto.builder()
                    .configKey(g.getConfigKey())
                    .category(g.getCategory())
                    .configName(g.getConfigName())
                    .dataType(g.getDataType())
                    .effectiveValue(effectiveVal)
                    .globalValue(g.getValue())
                    .overrideValue(isOverridden ? o.getOverrideValue() : null)
                    .isOverridden(isOverridden)
                    .isTenantOverridable(g.getIsTenantOverridable())
                    .minValue(g.getMinValue())
                    .maxValue(g.getMaxValue())
                    .description(g.getDescription())
                    .updatedAt(g.getUpdatedAt())
                    .build());
        }

        return result;
    }

    @Transactional
    public SystemConfigDto setTenantOverride(String tenantId, String configKey, String overrideValue, Long makerId) {
        GlobalSystemConfigEntity global = globalRepo.findById(configKey)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy tham số cấu hình: " + configKey));

        if (!Boolean.TRUE.equals(global.getIsTenantOverridable())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Tham số " + configKey + " được khóa ở cấp nền tảng (SuperAdmin), không cho phép Tenant ghi đè!");
        }

        TenantConfigOverrideEntity override = overrideRepo.findByTenantIdAndConfigKey(tenantId, configKey)
                .orElseGet(() -> TenantConfigOverrideEntity.builder()
                        .tenantId(tenantId)
                        .configKey(configKey)
                        .build());

        override.setOverrideValue(overrideValue);
        override.setIsActive(true);
        override.setMakerId(makerId);

        overrideRepo.save(override);
        log.info("Tenant {} updated override for key {} to {}", tenantId, configKey, overrideValue);

        return SystemConfigDto.builder()
                .configKey(configKey)
                .category(global.getCategory())
                .configName(global.getConfigName())
                .dataType(global.getDataType())
                .effectiveValue(overrideValue)
                .globalValue(global.getValue())
                .overrideValue(overrideValue)
                .isOverridden(true)
                .isTenantOverridable(global.getIsTenantOverridable())
                .minValue(global.getMinValue())
                .maxValue(global.getMaxValue())
                .description(global.getDescription())
                .build();
    }

    @Transactional
    public GlobalSystemConfigEntity updateGlobalConfig(SystemConfigDto.GlobalConfigRequest request, Long updatedBy) {
        GlobalSystemConfigEntity entity = globalRepo.findById(request.getConfigKey())
                .orElseGet(() -> GlobalSystemConfigEntity.builder()
                        .configKey(request.getConfigKey())
                        .build());

        entity.setCategory(request.getCategory());
        entity.setConfigName(request.getConfigName());
        entity.setDataType(request.getDataType() != null ? request.getDataType() : "STRING");
        entity.setValue(request.getValue());
        entity.setMinValue(request.getMinValue());
        entity.setMaxValue(request.getMaxValue());
        entity.setIsTenantOverridable(request.getIsTenantOverridable() != null ? request.getIsTenantOverridable() : false);
        entity.setDescription(request.getDescription());
        entity.setUpdatedBy(updatedBy);

        return globalRepo.save(entity);
    }
}
