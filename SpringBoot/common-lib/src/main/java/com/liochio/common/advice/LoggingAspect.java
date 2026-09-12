package com.liochio.common.advice;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * ==============================================================================
 * Khía Cạnh Ghi Vết Hiệu Năng & Thực Thi (Performance & Execution Logging Aspect)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tự động đo đạc thời gian thực thi (execution time in ms) của tất cả các Controller
 *   và Service trong hệ thống.
 * - Cảnh báo trong log nếu thời gian xử lý của một API vượt quá ngưỡng cho phép (vd: > 1000ms).
 * 
 * Khi nào gọi:
 * - AOP Proxy tự động chặn trước và sau khi gọi các method trong Controller và Service.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Service *)")
    public void applicationPackagePointcut() {
        // Pointcut nhắm vào các Controller và Service
    }

    @Around("applicationPackagePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - start;

            if (elapsedTime > 1000) {
                log.warn("[SLOW_EXECUTION] {}.{}() thực thi mất {} ms", className, methodName, elapsedTime);
            } else {
                log.debug("[EXECUTION] {}.{}() hoàn tất trong {} ms", className, methodName, elapsedTime);
            }
            return result;
        } catch (Throwable e) {
            long elapsedTime = System.currentTimeMillis() - start;
            log.error("[EXCEPTION] {}.{}() ném ngoại lệ sau {} ms: {}", className, methodName, elapsedTime, e.getMessage());
            throw e;
        }
    }
}
