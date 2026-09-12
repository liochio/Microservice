package com.liochio.auth.service;

import com.liochio.auth.dto.DeviceResponse;
import com.liochio.auth.entity.UserDeviceEntity;
import com.liochio.auth.repository.UserDeviceRepository;
import com.liochio.common.dto.ClientContextRequest;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ==============================================================================
 * Dịch Vụ Quản Lý Thiết Bị Người Dùng (User Device Management Service)
 * ==============================================================================
 * 
 * Mục đích:
 * - Theo dõi danh sách thiết bị truy cập, gắn nhãn thiết bị tin cậy (is_trusted).
 * - Kích hoạt thách thức 2FA khi phát hiện đăng nhập từ thiết bị mới.
 */
@Service
@RequiredArgsConstructor
public class DeviceService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeviceService.class);

    private final UserDeviceRepository deviceRepository;

    @Transactional
    public UserDeviceEntity getOrCreateDevice(String tenantId, Long userId, ClientContextRequest context) {
        String deviceId = (context.getDeviceId() != null && !context.getDeviceId().isBlank())
                ? context.getDeviceId()
                : "dev_default_" + userId;

        Optional<UserDeviceEntity> existingOpt = deviceRepository.findByTenantIdAndUserIdAndDeviceId(tenantId, userId, deviceId);

        if (existingOpt.isPresent()) {
            UserDeviceEntity device = existingOpt.get();
            device.setLastActiveAt(Instant.now());
            if (context.getClientIp() != null) device.setLastIpAddress(context.getClientIp());
            if (context.getBrowserName() != null) device.setBrowserName(context.getBrowserName());
            if (context.getOsVersion() != null) device.setOsVersion(context.getOsVersion());
            return deviceRepository.save(device);
        }

        // Kiểm tra xem đây có phải thiết bị đầu tiên của người dùng không
        List<UserDeviceEntity> existingDevices = deviceRepository.findByTenantIdAndUserId(tenantId, userId);
        boolean isFirstDevice = existingDevices.isEmpty();

        UserDeviceEntity newDevice = UserDeviceEntity.builder()
                .tenantId(tenantId != null ? tenantId : "SYSTEM")
                .userId(userId)
                .deviceId(deviceId)
                .deviceName(context.getDeviceName() != null ? context.getDeviceName() : "Unknown Device")
                .platform(context.getPlatform() != null ? context.getPlatform() : "WEB")
                .osVersion(context.getOsVersion())
                .appVersion(context.getAppVersion())
                .browserName(context.getBrowserName())
                .isTrusted(isFirstDevice) // Thiết bị đầu tiên lúc đăng ký được mặc định tin cậy
                .isSmartOtpEnrolled(false)
                .lastIpAddress(context.getClientIp())
                .status("ACTIVE")
                .lastActiveAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        log.info("[DeviceService] Tạo mới thiết bị: User={}, DeviceId={}, Trusted={}", userId, deviceId, isFirstDevice);
        return deviceRepository.save(newDevice);
    }

    @Transactional
    public void trustDevice(String tenantId, Long userId, String deviceId) {
        UserDeviceEntity device = deviceRepository.findByTenantIdAndUserIdAndDeviceId(tenantId, userId, deviceId)
            .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        device.setIsTrusted(true);
        device.setStatus("ACTIVE");
        device.setLastActiveAt(Instant.now());
        deviceRepository.save(device);
        log.info("[DeviceService] Đã gắn nhãn Tin Cậy (Trusted) cho thiết bị: User={}, DeviceId={}", userId, deviceId);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> getUserDevices(String tenantId, Long userId) {
        return deviceRepository.findByTenantIdAndUserId(tenantId, userId).stream()
                .map(this::mapToDeviceResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokeDevice(String tenantId, Long userId, String deviceId) {
        UserDeviceEntity device = deviceRepository.findByTenantIdAndUserIdAndDeviceId(tenantId, userId, deviceId)
            .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        device.setStatus("REVOKED");
        device.setIsTrusted(false);
        deviceRepository.save(device);
        log.info("[DeviceService] Đã thu hồi quyền thiết bị: User={}, DeviceId={}", userId, deviceId);
    }

    private DeviceResponse mapToDeviceResponse(UserDeviceEntity entity) {
        return DeviceResponse.builder()
                .id(entity.getId())
                .deviceId(entity.getDeviceId())
                .deviceName(entity.getDeviceName())
                .platform(entity.getPlatform())
                .osVersion(entity.getOsVersion())
                .appVersion(entity.getAppVersion())
                .browserName(entity.getBrowserName())
                .isTrusted(entity.getIsTrusted())
                .isSmartOtpEnrolled(entity.getIsSmartOtpEnrolled())
                .lastIpAddress(entity.getLastIpAddress())
                .lastLocation(entity.getLastLocation())
                .lastActiveAt(entity.getLastActiveAt())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
