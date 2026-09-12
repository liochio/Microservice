package com.liochio.auth.service;

import com.liochio.auth.dto.CustomerGateDto;
import com.liochio.auth.entity.CustomerOnboardingGateEntity;
import com.liochio.auth.entity.LedgerAccountEntity;
import com.liochio.auth.entity.UserEntity;
import com.liochio.auth.repository.CustomerOnboardingGateRepository;
import com.liochio.auth.repository.LedgerAccountRepository;
import com.liochio.auth.repository.UserRepository;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerGateService {

    private final CustomerOnboardingGateRepository gateRepository;
    private final UserRepository userRepository;
    private final LedgerAccountRepository ledgerAccountRepository;

    @Transactional
    public CustomerGateDto getOrCreateGate(Long userId, String tenantId) {
        return gateRepository.findByUserId(userId)
                .map(this::mapToDto)
                .orElseGet(() -> {
                    CustomerOnboardingGateEntity entity = CustomerOnboardingGateEntity.builder()
                            .tenantId(tenantId != null ? tenantId : "default")
                            .userId(userId)
                            .gate1EkycStatus("PENDING")
                            .gate2RoleTierStatus("NOT_STARTED")
                            .gate3WalletProvisionStatus("NOT_STARTED")
                            .gate4DeviceBindingStatus("NOT_STARTED")
                            .overallStatus("IN_PROGRESS")
                            .build();
                    return mapToDto(gateRepository.save(entity));
                });
    }

    @Transactional
    public CustomerGateDto reviewGate1Ekyc(Long userId, Long reviewerId, CustomerGateDto.Gate1ReviewRequest request) {
        CustomerOnboardingGateEntity entity = getEntity(userId);
        
        String action = request.getAction().toUpperCase();
        entity.setGate1EkycStatus(action);
        entity.setGate1ReviewerId(reviewerId);
        entity.setGate1ReviewedAt(Instant.now());
        entity.setGate1Notes(request.getNotes());

        if ("APPROVED".equals(action)) {
            // Tự động mở khóa Gate 2 sang PENDING
            if ("NOT_STARTED".equals(entity.getGate2RoleTierStatus())) {
                entity.setGate2RoleTierStatus("PENDING");
            }
            // Cập nhật UserEntity
            userRepository.findById(userId).ifPresent(u -> {
                u.setEkycStatus("VERIFIED");
                u.setEkycVerifiedAt(Instant.now());
                userRepository.save(u);
            });
        } else if ("REJECTED".equals(action)) {
            entity.setOverallStatus("BLOCKED");
            userRepository.findById(userId).ifPresent(u -> {
                u.setEkycStatus("REJECTED");
                userRepository.save(u);
            });
        }

        checkOverallCompletion(entity);
        return mapToDto(gateRepository.save(entity));
    }

    @Transactional
    public CustomerGateDto assignGate2Tier(Long userId, Long reviewerId, CustomerGateDto.Gate2TierAssignRequest request) {
        CustomerOnboardingGateEntity entity = getEntity(userId);

        if (!"APPROVED".equals(entity.getGate1EkycStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Không thể duyệt Gate 2 khi Gate 1 (eKYC) chưa được phê duyệt!");
        }

        String tier = request.getTier() != null ? request.getTier().toUpperCase() : "TIER_1";
        BigDecimal limit = request.getDailyLimit() != null ? request.getDailyLimit() : new BigDecimal("5000000.00");

        entity.setAssignedTier(tier);
        entity.setAssignedDailyLimit(limit);
        entity.setGate2RoleTierStatus("APPROVED");
        entity.setGate2ReviewerId(reviewerId);
        entity.setGate2ReviewedAt(Instant.now());

        // Tự động kích hoạt Gate 3 sang PENDING
        if ("NOT_STARTED".equals(entity.getGate3WalletProvisionStatus())) {
            entity.setGate3WalletProvisionStatus("PENDING");
        }

        userRepository.findById(userId).ifPresent(u -> {
            u.setEkycLevel(tier);
            u.setDailyTransferLimit(limit);
            userRepository.save(u);
        });

        checkOverallCompletion(entity);
        return mapToDto(gateRepository.save(entity));
    }

    @Transactional
    public CustomerGateDto provisionGate3Wallets(Long userId) {
        CustomerOnboardingGateEntity entity = getEntity(userId);

        if (!"APPROVED".equals(entity.getGate2RoleTierStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Không thể mở ví Gate 3 khi Gate 2 (Tier & Vai trò) chưa được duyệt!");
        }

        String availAccNo = "ACC_USR_" + userId + "_AVAIL";
        String savingsAccNo = "ACC_USR_" + userId + "_SAVINGS";

        // Mở ví Khả dụng nếu chưa có
        if (ledgerAccountRepository.findByTenantIdAndAccountNumber(entity.getTenantId(), availAccNo).isEmpty()) {
            LedgerAccountEntity availAcc = LedgerAccountEntity.builder()
                    .tenantId(entity.getTenantId())
                    .accountNumber(availAccNo)
                    .userId(userId)
                    .accountType("USER_AVAILABLE")
                    .currency("VND")
                    .balance(BigDecimal.ZERO)
                    .status("ACTIVE")
                    .build();
            ledgerAccountRepository.save(availAcc);
        }

        // Mở ví Tiết kiệm / Heo Đất nếu chưa có
        if (ledgerAccountRepository.findByTenantIdAndAccountNumber(entity.getTenantId(), savingsAccNo).isEmpty()) {
            LedgerAccountEntity savingsAcc = LedgerAccountEntity.builder()
                    .tenantId(entity.getTenantId())
                    .accountNumber(savingsAccNo)
                    .userId(userId)
                    .accountType("USER_ESCROW")
                    .currency("VND")
                    .balance(BigDecimal.ZERO)
                    .status("ACTIVE")
                    .build();
            ledgerAccountRepository.save(savingsAcc);
        }

        entity.setAvailableAccountNo(availAccNo);
        entity.setSavingsAccountNo(savingsAccNo);
        entity.setGate3WalletProvisionStatus("PROVISIONED");
        entity.setGate3ProvisionedAt(Instant.now());

        // Mở khóa Gate 4 sang PENDING
        if ("NOT_STARTED".equals(entity.getGate4DeviceBindingStatus())) {
            entity.setGate4DeviceBindingStatus("PENDING");
        }

        checkOverallCompletion(entity);
        return mapToDto(gateRepository.save(entity));
    }

    @Transactional
    public CustomerGateDto pairGate4Device(Long userId, CustomerGateDto.Gate4PairingRequest request) {
        CustomerOnboardingGateEntity entity = getEntity(userId);

        if (!"PROVISIONED".equals(entity.getGate3WalletProvisionStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Không thể ghép đôi thiết bị Gate 4 khi Gate 3 (Ví Sổ cái) chưa kích hoạt!");
        }

        entity.setBoundDeviceSerial(request.getDeviceSerial());
        entity.setBoundDeviceModel(request.getDeviceModel() != null ? request.getDeviceModel() : "LIOCHIO-PIGGY-V1");
        entity.setGate4DeviceBindingStatus("BOUND");
        entity.setBoundAt(Instant.now());

        checkOverallCompletion(entity);
        return mapToDto(gateRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<CustomerGateDto> listCustomerGates(String tenantId, String status, Pageable pageable) {
        String effectiveTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : "default";
        Page<CustomerOnboardingGateEntity> page;
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            page = gateRepository.findByTenantIdAndOverallStatusOrderByCreatedAtDesc(effectiveTenant, status.toUpperCase(), pageable);
        } else {
            page = gateRepository.findByTenantIdOrderByCreatedAtDesc(effectiveTenant, pageable);
        }
        return page.map(this::mapToDto);
    }

    private void checkOverallCompletion(CustomerOnboardingGateEntity entity) {
        if ("APPROVED".equals(entity.getGate1EkycStatus()) &&
            "APPROVED".equals(entity.getGate2RoleTierStatus()) &&
            "PROVISIONED".equals(entity.getGate3WalletProvisionStatus()) &&
            "BOUND".equals(entity.getGate4DeviceBindingStatus())) {
            entity.setOverallStatus("COMPLETED");
            entity.setCompletedAt(Instant.now());
        }
    }

    private CustomerOnboardingGateEntity getEntity(Long userId) {
        return gateRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy tiến trình 4 Cửa ải của User ID: " + userId));
    }

    private CustomerGateDto mapToDto(CustomerOnboardingGateEntity entity) {
        Optional<UserEntity> userOpt = userRepository.findById(entity.getUserId());
        String username = userOpt.map(UserEntity::getUsername).orElse("User#" + entity.getUserId());
        String fullName = userOpt.map(UserEntity::getFullName).orElse("");
        String phone = userOpt.map(UserEntity::getPhone).orElse("");
        String email = userOpt.map(UserEntity::getEmail).orElse("");

        return CustomerGateDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .userId(entity.getUserId())
                .username(username)
                .fullName(fullName)
                .phone(phone)
                .email(email)
                .gate1EkycStatus(entity.getGate1EkycStatus())
                .gate1ReviewerId(entity.getGate1ReviewerId())
                .gate1ReviewedAt(entity.getGate1ReviewedAt())
                .gate1Notes(entity.getGate1Notes())
                .gate2RoleTierStatus(entity.getGate2RoleTierStatus())
                .assignedTier(entity.getAssignedTier())
                .assignedDailyLimit(entity.getAssignedDailyLimit())
                .gate2ReviewerId(entity.getGate2ReviewerId())
                .gate2ReviewedAt(entity.getGate2ReviewedAt())
                .gate3WalletProvisionStatus(entity.getGate3WalletProvisionStatus())
                .availableAccountNo(entity.getAvailableAccountNo())
                .savingsAccountNo(entity.getSavingsAccountNo())
                .gate3ProvisionedAt(entity.getGate3ProvisionedAt())
                .gate4DeviceBindingStatus(entity.getGate4DeviceBindingStatus())
                .boundDeviceSerial(entity.getBoundDeviceSerial())
                .boundDeviceModel(entity.getBoundDeviceModel())
                .boundAt(entity.getBoundAt())
                .overallStatus(entity.getOverallStatus())
                .completedAt(entity.getCompletedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
