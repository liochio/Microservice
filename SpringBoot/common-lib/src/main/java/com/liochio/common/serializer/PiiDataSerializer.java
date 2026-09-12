package com.liochio.common.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.liochio.common.annotation.MaskPII;
import com.liochio.common.utils.DataMaskingUtils;

import java.io.IOException;

public class PiiDataSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private MaskPII.MaskType maskType = MaskPII.MaskType.AUTO;

    public PiiDataSerializer() {}

    public PiiDataSerializer(MaskPII.MaskType maskType) {
        this.maskType = maskType;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        String masked;
        switch (maskType) {
            case ID_CARD -> masked = DataMaskingUtils.maskIdCard(value);
            case PHONE -> masked = DataMaskingUtils.maskPhone(value);
            case BANK_CARD -> masked = DataMaskingUtils.maskBankCard(value);
            case EMAIL -> masked = DataMaskingUtils.maskEmail(value);
            case AUTO -> {
                if (value.contains("@")) {
                    masked = DataMaskingUtils.maskEmail(value);
                } else if (value.length() == 12 || value.length() == 9) {
                    masked = DataMaskingUtils.maskIdCard(value);
                } else if (value.length() == 10 || value.length() == 11) {
                    masked = DataMaskingUtils.maskPhone(value);
                } else {
                    masked = DataMaskingUtils.maskBankCard(value);
                }
            }
            default -> masked = value;
        }

        gen.writeString(masked);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        if (property != null) {
            MaskPII annotation = property.getAnnotation(MaskPII.class);
            if (annotation == null) {
                annotation = property.getContextAnnotation(MaskPII.class);
            }
            if (annotation != null) {
                return new PiiDataSerializer(annotation.type());
            }
        }
        return this;
    }
}
