package com.liochio.entityservice.validator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.entityservice.entity.DynamicFieldDefinitionEntity;
import com.liochio.entityservice.repository.DynamicFieldDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * ==============================================================================
 * Bộ Kiểm Tra Schema & Thuộc Tính Động (Dynamic Schema Validator Engine)
 * ==============================================================================
 * 
 * Mục đích:
 * 1. Tự động đối chiếu payload JSON attributes của thực thể với dynamic_field_definitions.
 * 2. Kiểm tra ràng buộc bắt buộc (is_required), kiểu dữ liệu (STRING, NUMBER, BOOLEAN, DATE, ENUM, ARRAY).
 * 3. Kiểm tra các quy tắc mở rộng: Min, Max, Regex Pattern, Allowed Options.
 * 4. Ném AppException(INVALID_REQUEST) chi tiết nếu không thỏa mãn schema.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicSchemaValidator {

    private final DynamicFieldDefinitionRepository fieldDefinitionRepository;
    private final ObjectMapper objectMapper;

    public void validate(String entityTypeCode, Map<String, Object> attributes) {
        if (entityTypeCode == null || entityTypeCode.isBlank()) {
            return;
        }

        List<DynamicFieldDefinitionEntity> fieldDefs = fieldDefinitionRepository
                .findByEntityTypeCodeOrderByDisplayOrderAsc(entityTypeCode.trim());

        if (fieldDefs == null || fieldDefs.isEmpty()) {
            // Không có schema định nghĩa riêng cho entity type này -> Cho phép linh hoạt
            return;
        }

        Map<String, Object> safeAttributes = attributes != null ? attributes : Collections.emptyMap();
        List<String> validationErrors = new ArrayList<>();

        for (DynamicFieldDefinitionEntity fieldDef : fieldDefs) {
            String key = fieldDef.getFieldKey();
            Object value = safeAttributes.get(key);

            // 1. Kiểm tra trường bắt buộc (is_required)
            if (Boolean.TRUE.equals(fieldDef.getIsRequired())) {
                if (value == null || (value instanceof String s && s.trim().isEmpty())) {
                    validationErrors.add(String.format("Trường '%s' là bắt buộc đối với loại thực thể '%s'", key, entityTypeCode));
                    continue;
                }
            }

            if (value == null) {
                continue;
            }

            // 2. Kiểm tra kiểu dữ liệu (data_type)
            String dataType = fieldDef.getDataType() != null ? fieldDef.getDataType().toUpperCase() : "STRING";
            boolean typeValid = validateDataType(dataType, value);
            if (!typeValid) {
                validationErrors.add(String.format("Trường '%s' phải có kiểu dữ liệu là %s (giá trị hiện tại: %s)", key, dataType, value));
                continue;
            }

            // 3. Kiểm tra validation rules mở rộng (min, max, regex, options)
            if (fieldDef.getValidationRules() != null && !fieldDef.getValidationRules().isBlank()) {
                try {
                    Map<String, Object> rules = objectMapper.readValue(fieldDef.getValidationRules(), new TypeReference<Map<String, Object>>() {});
                    validateCustomRules(key, value, dataType, rules, validationErrors);
                } catch (Exception e) {
                    log.warn("[SchemaValidator] Không thể parse validation rules của field '{}': {}", key, e.getMessage());
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            String combinedError = String.join("; ", validationErrors);
            log.warn("[SchemaValidator] Xác thực thất bại cho entityType '{}': {}", entityTypeCode, combinedError);
            throw new AppException(ErrorCode.INVALID_REQUEST, combinedError);
        }
    }

    private boolean validateDataType(String dataType, Object value) {
        switch (dataType) {
            case "NUMBER":
            case "INTEGER":
            case "DECIMAL":
            case "DOUBLE":
                if (value instanceof Number) return true;
                if (value instanceof String s) {
                    try {
                        Double.parseDouble(s);
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                return false;

            case "BOOLEAN":
                if (value instanceof Boolean) return true;
                if (value instanceof String s) {
                    return "true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s);
                }
                return false;

            case "DATE":
            case "DATETIME":
                if (value instanceof String s) {
                    return s.matches("^\\d{4}-\\d{2}-\\d{2}.*");
                }
                return false;

            case "ARRAY":
            case "LIST":
                return value instanceof List;

            case "JSON":
            case "OBJECT":
                return value instanceof Map;

            case "STRING":
            case "TEXT":
            case "ENUM":
            case "FILE":
            default:
                return true;
        }
    }

    private void validateCustomRules(String fieldKey, Object value, String dataType, Map<String, Object> rules, List<String> errors) {
        // Options check (Enum)
        if (rules.containsKey("options") && rules.get("options") instanceof List<?> options) {
            String strVal = String.valueOf(value);
            boolean matched = options.stream().anyMatch(opt -> String.valueOf(opt).equalsIgnoreCase(strVal));
            if (!matched) {
                errors.add(String.format("Trường '%s' phải là một trong các giá trị: %s", fieldKey, options));
            }
        }

        // Regex check
        if (rules.containsKey("regex") && rules.get("regex") instanceof String regex) {
            String strVal = String.valueOf(value);
            if (!Pattern.compile(regex).matcher(strVal).matches()) {
                String message = rules.get("regex_message") instanceof String msg ? msg : String.format("Trường '%s' không đúng định dạng quy định", fieldKey);
                errors.add(message);
            }
        }

        // Min value / Min length
        if (rules.containsKey("min")) {
            double min = Double.parseDouble(String.valueOf(rules.get("min")));
            if (value instanceof Number n) {
                if (n.doubleValue() < min) {
                    errors.add(String.format("Trường '%s' giá trị tối thiểu là %s", fieldKey, min));
                }
            } else if (value instanceof String s) {
                if (s.length() < min) {
                    errors.add(String.format("Trường '%s' độ dài tối thiểu là %d ký tự", fieldKey, (int) min));
                }
            }
        }

        // Max value / Max length
        if (rules.containsKey("max")) {
            double max = Double.parseDouble(String.valueOf(rules.get("max")));
            if (value instanceof Number n) {
                if (n.doubleValue() > max) {
                    errors.add(String.format("Trường '%s' giá trị tối đa là %s", fieldKey, max));
                }
            } else if (value instanceof String s) {
                if (s.length() > max) {
                    errors.add(String.format("Trường '%s' độ dài tối đa là %d ký tự", fieldKey, (int) max));
                }
            }
        }
    }
}