package com.liochio.common.annotation;

import java.lang.annotation.*;

/**
 * ==============================================================================
 * Phân Quyền Theo Ngữ Cảnh Động (Attribute-Based Access Control - ABAC)
 * ==============================================================================
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPolicy {

    boolean workingHoursOnly() default false;

    String[] allowedCidrRanges() default {};

    String minEkycLevel() default "TIER_1";

    boolean requireTrustedDevice() default false;

    String description() default "";
}
