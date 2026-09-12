package com.liochio.common.config;

import com.liochio.common.constant.AppConstants;
import com.liochio.common.context.UserContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * ==============================================================================
 * Cấu Hình Tự Động Ghi Vết Bản Ghi (JPA Auditing Configuration)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tự động kích hoạt cơ chế JPA Auditing để gán giá trị cho `@CreatedBy` và `@LastModifiedBy`
 *   trên các Entity kế thừa `BaseEntity`.
 * - Lấy thông tin tài khoản người dùng từ `UserContext` (ThreadLocal đã xác thực từ JWT).
 * 
 * Khi nào gọi:
 * - Tự động được Hibernate / Spring Data JPA kích hoạt khi gọi `repository.save()`.
 */
@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnBean(name = "entityManagerFactory")
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            String username = UserContext.getUsername();
            if (username != null && !username.isBlank() && !"ANONYMOUS".equalsIgnoreCase(username)) {
                return Optional.of(username);
            }
            return Optional.of(AppConstants.SYSTEM_USER);
        };
    }
}
