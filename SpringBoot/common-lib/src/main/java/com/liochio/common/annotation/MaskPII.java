package com.liochio.common.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.liochio.common.serializer.PiiDataSerializer;

import java.lang.annotation.*;

/**
 * ==============================================================================
 * Che Giấu Thông Tin Định Danh Nhạy Cảm (Data Masking & PII Protection)
 * ==============================================================================
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside
@JsonSerialize(using = PiiDataSerializer.class)
public @interface MaskPII {

    MaskType type() default MaskType.AUTO;

    char maskChar() default '*';

    int prefixLength() default 4;

    int suffixLength() default 4;

    enum MaskType {
        AUTO,
        ID_CARD,     // 079200001234 -> 07920000****
        PHONE,       // 0901234567 -> 090*****67
        BANK_CARD,   // 9704198526371234 -> 9704********1234
        EMAIL        // nguyen.van.a@gmail.com -> n***a@gmail.com
    }
}
