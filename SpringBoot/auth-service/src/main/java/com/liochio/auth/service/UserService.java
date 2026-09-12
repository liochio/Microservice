package com.liochio.auth.service;

import com.liochio.auth.dto.UserResponse;
import com.liochio.auth.entity.RoleEntity;
import com.liochio.auth.entity.UserEntity;
import com.liochio.auth.repository.UserRepository;
import com.liochio.common.dto.PageResponse;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * ==============================================================================
 * Dịch Vụ Quản Lý Người Dùng (User Management Service)
 * ==============================================================================
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(Pageable pageable) {
        Page<UserResponse> page = userRepository.findAll(pageable).map(this::mapToUserResponse);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return mapToUserResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setIsDeleted(true);
        user.setDeletedAt(java.time.Instant.now());
        userRepository.save(user);
    }

    private UserResponse mapToUserResponse(UserEntity user) {
        Set<String> roles = user.getRoles().stream()
                .map(RoleEntity::getRoleName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getPermissionCode())
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .userType(user.getUserType())
                .phone(user.getPhone())
                .idCardNumber(user.getIdCardNumber())
                .ekycLevel(user.getEkycLevel())
                .ekycStatus(user.getEkycStatus())
                .roles(roles)
                .permissions(permissions)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
