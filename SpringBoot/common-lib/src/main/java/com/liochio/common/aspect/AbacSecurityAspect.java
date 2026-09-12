package com.liochio.common.aspect;

import com.liochio.common.annotation.RequiresPolicy;
import com.liochio.common.context.UserContext;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;

/**
 * ==============================================================================
 * Khía Cạnh Thực Thi Phân Quyền ABAC (Attribute-Based Access Control Aspect)
 * ==============================================================================
 */
@Slf4j
@Aspect
@Component
@Order(15)
public class AbacSecurityAspect {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final LocalTime WORK_START = LocalTime.of(8, 0);
    private static final LocalTime WORK_END = LocalTime.of(18, 0);

    @Before("@annotation(policy)")
    public void evaluatePolicy(JoinPoint joinPoint, RequiresPolicy policy) {
        String username = UserContext.getUsername();

        // 1. Kiểm tra chính sách Giờ hành chính
        if (policy.workingHoursOnly()) {
            LocalTime now = LocalTime.now(VN_ZONE);
            if (now.isBefore(WORK_START) || now.isAfter(WORK_END)) {
                log.warn("[ABAC Aspect] Từ chối User '{}' do ngoài giờ hành chính (Hiện tại: {}, Yêu cầu: 08:00 - 18:00)", username, now);
                throw new AppException(ErrorCode.ABAC_POLICY_VIOLATION, "Hành động này chỉ được phép thực hiện trong giờ hành chính (08:00 - 18:00)");
            }
        }

        // 2. Kiểm tra chính sách Dải IP
        if (policy.allowedCidrRanges() != null && policy.allowedCidrRanges().length > 0) {
            String clientIp = resolveClientIp();
            boolean isAllowedIp = Arrays.stream(policy.allowedCidrRanges())
                    .anyMatch(allowed -> allowed.equals(clientIp) || "127.0.0.1".equals(clientIp) || "0:0:0:0:0:0:0:1".equals(clientIp) || allowed.equals("*"));

            if (!isAllowedIp) {
                log.warn("[ABAC Aspect] Từ chối User '{}' do IP '{}' không nằm trong dải IP cho phép: {}", username, clientIp, Arrays.toString(policy.allowedCidrRanges()));
                throw new AppException(ErrorCode.ABAC_POLICY_VIOLATION, "Địa chỉ IP (" + clientIp + ") không được phép thực hiện giao dịch nhạy cảm này");
            }
        }
    }

    private String resolveClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return "127.0.0.1";
        HttpServletRequest request = attributes.getRequest();

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return (ip != null) ? ip : "127.0.0.1";
    }
}
