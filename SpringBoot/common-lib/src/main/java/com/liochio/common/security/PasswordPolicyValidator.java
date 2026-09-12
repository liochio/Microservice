package com.liochio.common.security;

import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * ==============================================================================
 * Trình Kiểm Soát Chính Sách Độ Mạnh Mật Khẩu Doanh Nghiệp (Password Policy Validator)
 * ==============================================================================
 * 
 * Mục đích:
 * - Đảm bảo mật khẩu người dùng đạt tiêu chuẩn an toàn bảo mật:
 *   + Tối thiểu 8 ký tự.
 *   + Có ít nhất 1 chữ cái in hoa (A-Z).
 *   + Có ít nhất 1 chữ cái in thường (a-z).
 *   + Có ít nhất 1 chữ số (0-9).
 *   + Có ít nhất 1 ký tự đặc biệt (!@#$%^&*...).
 */
@Component
public class PasswordPolicyValidator {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");

    public boolean isValid(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return false;
        }
        return UPPERCASE_PATTERN.matcher(password).find()
                && LOWERCASE_PATTERN.matcher(password).find()
                && DIGIT_PATTERN.matcher(password).find()
                && SPECIAL_CHAR_PATTERN.matcher(password).find();
    }

    public void validate(String password) {
        if (!isValid(password)) {
            throw new AppException(
                    ErrorCode.PASSWORD_POLICY_VIOLATION,
                    "Mật khẩu phải chứa ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, chữ số và ký tự đặc biệt"
            );
        }
    }
}
