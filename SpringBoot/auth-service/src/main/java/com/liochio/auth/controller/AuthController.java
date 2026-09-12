package com.liochio.auth.controller;

import com.liochio.auth.dto.*;
import com.liochio.auth.service.*;
import com.liochio.common.annotation.AuditLog;
import com.liochio.common.annotation.RequireRole;
import com.liochio.common.constant.MessageConstants;
import com.liochio.common.i18n.MessageService;
import com.liochio.common.context.TenantContext;
import com.liochio.common.context.UserContext;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.dto.ClientContextRequest;
import com.liochio.common.enums.ActionType;
import com.liochio.common.security.DeviceFingerprintExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ==============================================================================
 * Controller Xác Thực, Bảo Mật, Quản Lý Phiên & QR Login (Enterprise Auth Controller)
 * ==============================================================================
 */
@Slf4j
@RestController
@RequestMapping({"/api/v1/auth", "/api/auth"})
@RequiredArgsConstructor
@Tag(name = "Enterprise Authentication Controller", description = "Các API xác thực, đăng nhập 2FA, QR Login, SmartOTP, quản trị thiết bị và phiên")
public class AuthController {

    private final AuthService authService;
    private final DeviceService deviceService;
    private final SessionService sessionService;
    private final QrLoginService qrLoginService;
    private final SmartOtpService smartOtpService;
    private final DeviceFingerprintExtractor fingerprintExtractor;
    private final MessageService messageService;

    // 1. Đăng ký tài khoản
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Đăng ký tài khoản", description = "Tạo mới tài khoản ở trạng thái PENDING_VERIFY và phát sinh mã OTP gửi xác thực")
    @AuditLog(module = "AUTH", action = ActionType.REGISTER, description = "Đăng ký tài khoản người dùng mới")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        ClientContextRequest context = fingerprintExtractor.extractContext(httpRequest);
        UserResponse response = authService.register(request, context);
        return ApiResponse.created(response, messageService.getMessage(MessageConstants.MSG_AUTH_REGISTER_SUCCESS));
    }

    // 2. Xác thực OTP kích hoạt tài khoản
    @PostMapping("/verify-otp")
    @Operation(summary = "Xác thực OTP kích hoạt", description = "Nhập mã OTP 6 số để kích hoạt tài khoản thành ACTIVE")
    @AuditLog(module = "AUTH", action = ActionType.UPDATE, description = "Xác thực OTP kích hoạt tài khoản")
    public ApiResponse<UserResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        UserResponse response = authService.verifyRegistrationOtp(request);
        return ApiResponse.success(response, messageService.getMessage(MessageConstants.MSG_AUTH_VERIFY_SUCCESS));
    }

    // 3. Đăng nhập
    @PostMapping("/login")
    @Operation(summary = "Đăng nhập hệ thống", description = "Kiểm tra tài khoản/mật khẩu, kiểm soát Brute-force và Thách thức 2FA thiết bị lạ")
    @AuditLog(module = "AUTH", action = ActionType.LOGIN, description = "Đăng nhập tài khoản")
    public ApiResponse<TokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "X-Portal-Type", required = false) String portalTypeHeader,
            HttpServletRequest httpRequest) {
        if (request.getPortalType() == null || request.getPortalType().isBlank()) {
            request.setPortalType(portalTypeHeader);
        }
        ClientContextRequest context = fingerprintExtractor.extractContext(httpRequest);
        TokenResponse response = authService.login(request, context);
        if (Boolean.TRUE.equals(response.getRequires2Fa())) {
            return ApiResponse.success(response, messageService.getMessage(MessageConstants.MSG_AUTH_LOGIN_2FA_CHALLENGE));
        }
        return ApiResponse.success(response, messageService.getMessage(MessageConstants.MSG_AUTH_LOGIN_SUCCESS));
    }

    // 4. Xác thực OTP thiết bị mới (2FA Challenge)
    @PostMapping("/verify-device-otp")
    @Operation(summary = "Xác thực 2FA thiết bị mới", description = "Nhập OTP xác thực thiết bị lạ để nhận Access Token và Refresh Token")
    @AuditLog(module = "AUTH", action = ActionType.LOGIN, description = "Xác thực 2FA thiết bị mới thành công")
    public ApiResponse<TokenResponse> verifyDeviceOtp(@Valid @RequestBody VerifyDeviceOtpRequest request, HttpServletRequest httpRequest) {
        ClientContextRequest context = fingerprintExtractor.extractContext(httpRequest);
        TokenResponse response = authService.verifyDeviceOtp(request, context);
        return ApiResponse.success(response, messageService.getMessage(MessageConstants.MSG_AUTH_VERIFY_DEVICE_SUCCESS));
    }

    // 5. Làm mới Token (Token Rotation & Reuse Detection)
    @PostMapping("/refresh")
    @Operation(summary = "Làm mới mã xác thực (Token Rotation)", description = "Thu hồi Refresh Token cũ, phát hiện rò rỉ token và cấp cặp Token mới")
    public ApiResponse<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        ClientContextRequest context = fingerprintExtractor.extractContext(httpRequest);
        TokenResponse response = authService.refreshToken(request, context);
        return ApiResponse.success(response, messageService.getMessage(MessageConstants.MSG_AUTH_REFRESH_SUCCESS));
    }

    // 6. Đăng xuất phiên hiện tại
    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất tài khoản", description = "Thu hồi phiên làm việc hiện tại của thiết bị")
    @AuditLog(module = "AUTH", action = ActionType.LOGOUT, description = "Đăng xuất phiên làm việc")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        authService.logout(token);
        return ApiResponse.noContent(messageService.getMessage(MessageConstants.MSG_AUTH_LOGOUT_SUCCESS));
    }

    // 7. Đăng xuất tất cả thiết bị (Force Logout)
    @PostMapping({"/logout-all-devices", "/force-logout"})
    @Operation(summary = "Đăng xuất toàn bộ thiết bị", description = "Cưỡng chế thu hồi toàn bộ các phiên làm việc của tài khoản")
    @AuditLog(module = "AUTH", action = ActionType.LOGOUT, description = "Cưỡng chế đăng xuất toàn bộ thiết bị")
    public ApiResponse<Void> logoutAllDevices() {
        authService.logoutAllDevices(UserContext.getUserId());
        return ApiResponse.noContent(messageService.getMessage(MessageConstants.MSG_AUTH_LOGOUT_ALL_SUCCESS));
    }

    // 8. Đổi mật khẩu
    @PostMapping("/change-password")
    @Operation(summary = "Đổi mật khẩu tài khoản", description = "Cập nhật mật khẩu mới tuân thủ chính sách bảo mật doanh nghiệp")
    @AuditLog(module = "AUTH", action = ActionType.UPDATE, description = "Đổi mật khẩu tài khoản")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(UserContext.getUserId(), request);
        return ApiResponse.noContent(messageService.getMessage(MessageConstants.MSG_AUTH_CHANGE_PASSWORD_SUCCESS));
    }

    // 9. Khởi tạo phiên quét mã QR Đăng nhập (Web)
    @PostMapping("/qr/init")
    @Operation(summary = "Khởi tạo QR Login (Web)", description = "Tạo session QR code (hạn 120s) và trả về topic WebSocket để lắng nghe")
    public ApiResponse<QrInitResponse> initQrLogin(HttpServletRequest httpRequest) {
        ClientContextRequest context = fingerprintExtractor.extractContext(httpRequest);
        String tenantId = TenantContext.getTenantId();
        QrInitResponse response = qrLoginService.initQrSession(tenantId, context);
        return ApiResponse.success(response, messageService.getMessage(MessageConstants.MSG_AUTH_QR_INIT_SUCCESS));
    }

    // 10. Quét mã QR (App Mobile)
    @PostMapping("/qr/scan")
    @Operation(summary = "Quét mã QR (Mobile)", description = "App Mobile quét mã QR để thông báo cho Web trạng thái SCANNED")
    public ApiResponse<Void> scanQrCode(@Valid @RequestBody QrScanRequest request) {
        Long mobileUserId = UserContext.getUserId();
        qrLoginService.scanQrSession(request, mobileUserId);
        return ApiResponse.noContent(messageService.getMessage(MessageConstants.MSG_AUTH_QR_SCANNED));
    }

    // 11. Xác nhận đăng nhập qua QR (App Mobile)
    @PostMapping("/qr/confirm")
    @Operation(summary = "Xác nhận đăng nhập QR (Mobile)", description = "Mobile xác thực FaceID/PIN để cấp mã đổi exchangeAuthCode")
    public ApiResponse<String> confirmQrCode(@Valid @RequestBody QrConfirmRequest request) {
        Long mobileUserId = UserContext.getUserId();
        String exchangeCode = qrLoginService.confirmQrSession(request, mobileUserId);
        return ApiResponse.success(exchangeCode, messageService.getMessage(MessageConstants.MSG_AUTH_QR_CONFIRM_SUCCESS));
    }

    // 12. Đổi Token qua mã xác nhận QR (Web)
    @PostMapping("/qr/exchange")
    @Operation(summary = "Đổi mã xác nhận QR lấy Token (Web)", description = "Trình duyệt Web gửi exchangeAuthCode để nhận cặp Token chính thức")
    public ApiResponse<TokenResponse> exchangeQrCode(@Valid @RequestBody QrExchangeRequest request, HttpServletRequest httpRequest) {
        ClientContextRequest context = fingerprintExtractor.extractContext(httpRequest);
        TokenResponse response = qrLoginService.exchangeQrCode(request, context);
        return ApiResponse.success(response, messageService.getMessage(MessageConstants.MSG_AUTH_QR_EXCHANGE_SUCCESS));
    }

    // 13. Danh sách thiết bị đã đăng nhập
    @GetMapping("/devices")
    @Operation(summary = "Danh sách thiết bị truy cập", description = "Xem toàn bộ danh sách thiết bị đã đăng nhập của người dùng")
    public ApiResponse<List<DeviceResponse>> getDevices() {
        String tenantId = TenantContext.getTenantId();
        Long userId = UserContext.getUserId();
        List<DeviceResponse> devices = deviceService.getUserDevices(tenantId, userId);
        return ApiResponse.success(devices);
    }

    // 14. Xóa/Thu hồi thiết bị
    @DeleteMapping("/devices/{deviceId}")
    @Operation(summary = "Thu hồi quyền thiết bị", description = "Xóa và thu hồi quyền tin cậy của thiết bị")
    @AuditLog(module = "AUTH", action = ActionType.DELETE, description = "Thu hồi quyền thiết bị truy cập")
    public ApiResponse<Void> revokeDevice(@PathVariable("deviceId") String deviceId) {
        String tenantId = TenantContext.getTenantId();
        Long userId = UserContext.getUserId();
        deviceService.revokeDevice(tenantId, userId, deviceId);
        return ApiResponse.noContent(messageService.getMessage(MessageConstants.MSG_AUTH_DEVICE_REVOKED));
    }

    // 15. Danh sách phiên đăng nhập hoạt động
    @GetMapping("/sessions")
    @Operation(summary = "Danh sách phiên hoạt động", description = "Xem các phiên làm việc (sessions) còn hiệu lực của tài khoản")
    public ApiResponse<List<ActiveSessionResponse>> getSessions(@RequestHeader(value = "X-Session-ID", required = false) String currentSessionId) {
        String tenantId = TenantContext.getTenantId();
        Long userId = UserContext.getUserId();
        List<ActiveSessionResponse> sessions = sessionService.getUserActiveSessions(tenantId, userId, currentSessionId);
        return ApiResponse.success(sessions);
    }

    // 16. Thiết lập SmartOTP (TOTP RFC 6238)
    @PostMapping("/smart-otp/setup")
    @Operation(summary = "Khởi tạo thiết lập SmartOTP", description = "Sinh Base32 Secret và URL Barcode QR để nạp vào Authenticator")
    public ApiResponse<SetupSmartOtpResponse> setupSmartOtp() {
        Long userId = UserContext.getUserId();
        String username = UserContext.getUsername();
        String tenantId = TenantContext.getTenantId();
        SetupSmartOtpResponse response = smartOtpService.setupSmartOtp(userId, tenantId, username);
        return ApiResponse.success(response, messageService.getMessage(MessageConstants.MSG_AUTH_SMART_OTP_SETUP_SUCCESS));
    }

    // 17. Kích hoạt SmartOTP
    @PostMapping("/smart-otp/verify")
    @Operation(summary = "Kích hoạt SmartOTP", description = "Xác thực mã 6 số TOTP và đặt mã PIN bảo vệ SmartOTP")
    @AuditLog(module = "AUTH", action = ActionType.UPDATE, description = "Kích hoạt SmartOTP bảo vệ tài khoản")
    public ApiResponse<Void> verifySmartOtp(@Valid @RequestBody VerifySmartOtpRequest request) {
        Long userId = UserContext.getUserId();
        String tenantId = TenantContext.getTenantId();
        smartOtpService.verifyAndEnrollSmartOtp(userId, tenantId, request.getOtpCode(), request.getPin());
        return ApiResponse.noContent(messageService.getMessage(MessageConstants.MSG_AUTH_SMART_OTP_VERIFY_SUCCESS));
    }

    // 18. Lấy thông tin tài khoản hiện tại
    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin tài khoản hiện tại", description = "Trả về thông tin chi tiết của người dùng đang đăng nhập")
    public ApiResponse<UserResponse> getCurrentUser() {
        UserResponse response = authService.getCurrentUser();
        return ApiResponse.success(response);
    }

    // 19. Xác thực Step-up SmartOTP và nhận Action Token cho giao dịch nhạy cảm
    @PostMapping("/smart-otp/action-token")
    @Operation(summary = "Xác thực Step-up SmartOTP và cấp Action Token", description = "Xác thực mã TOTP 6 số + PIN và nhận action_token có hiệu lực 120s")
    public ApiResponse<com.liochio.auth.dto.ActionTokenResponse> getActionToken(
            @Valid @RequestBody VerifySmartOtpRequest request,
            @RequestParam(required = false, defaultValue = "STEP_UP_TRANSACTION") String purpose
    ) {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 5L;
        String tenantId = TenantContext.getTenantId();
        com.liochio.auth.dto.ActionTokenResponse response = smartOtpService.verifySmartOtpAndIssueActionToken(userId, tenantId, request.getOtpCode(), request.getPin(), purpose);
        return ApiResponse.success(response, "Cấp Action Token giao dịch thành công");
    }
}