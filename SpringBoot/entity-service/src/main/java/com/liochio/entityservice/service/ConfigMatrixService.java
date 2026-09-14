package com.liochio.entityservice.service;

import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.entityservice.dto.SystemConfigDto;
import com.liochio.entityservice.entity.GlobalSystemConfigEntity;
import com.liochio.entityservice.entity.TenantConfigOverrideEntity;
import com.liochio.entityservice.repository.GlobalSystemConfigRepository;
import com.liochio.entityservice.repository.TenantConfigOverrideRepository;
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

        return SystemConfigDto.builder()
                .configKey(global.getConfigKey())
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
                .updatedAt(override.getUpdatedAt())
                .build();
    }

    @Transactional
    public GlobalSystemConfigEntity updateGlobalConfig(SystemConfigDto.GlobalConfigRequest req, Long userId) {
        GlobalSystemConfigEntity entity = globalRepo.findById(req.getConfigKey())
                .orElseGet(() -> GlobalSystemConfigEntity.builder()
                        .configKey(req.getConfigKey())
                        .build());

        entity.setCategory(req.getCategory().toUpperCase());
        entity.setConfigName(req.getConfigName());
        entity.setValue(req.getValue());
        if (req.getDataType() != null) entity.setDataType(req.getDataType());
        entity.setDescription(req.getDescription());
        if (req.getIsTenantOverridable() != null) entity.setIsTenantOverridable(req.getIsTenantOverridable());
        entity.setMinValue(req.getMinValue());
        entity.setMaxValue(req.getMaxValue());
        entity.setUpdatedBy(userId);

        return globalRepo.save(entity);
    }
}
