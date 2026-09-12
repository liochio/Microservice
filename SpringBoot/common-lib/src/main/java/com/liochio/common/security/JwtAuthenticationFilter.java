package com.liochio.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liochio.common.context.TenantContext;
import com.liochio.common.context.UserContext;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.exception.ErrorCode;
import com.liochio.common.utils.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ==============================================================================
 * Bộ Lọc Xác Thực JWT & Blacklist Toàn Cục (JWT Authentication Filter)
 * ==============================================================================
 * 
 * Mục đích:
 * - Chặn mỗi HTTP Request, trích xuất Bearer JWT token từ Authorization header.
 * - Kiểm tra tức thời trạng thái Blacklist / Revocation của Token, Session và User.
 * - Xác thực chữ ký token, nạp thông tin quyền hạn vào Spring SecurityContextHolder
 *   và UserContext (ThreadLocal).
 * 
 * Khi nào gọi:
 * - Thực thi 1 lần trên mỗi request (OncePerRequestFilter) trước UsernamePasswordAuthenticationFilter.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = SecurityUtils.extractBearerToken(request);

        if (token != null) {
            // 1. Kiểm tra Token Blacklist (đã đăng xuất / bị thu hồi phiên)
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("[JwtAuthenticationFilter] Phát hiện Token đã bị thu hồi / Blacklisted tại URI: {}", request.getRequestURI());
                SecurityContextHolder.clearContext();
                UserContext.clear();

                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                ApiResponse<Object> errorResponse = ApiResponse.error(
                        ErrorCode.TOKEN_REVOKED.getCode(),
                        "Token đã bị thu hồi hoặc tài khoản đã đăng xuất (Token Revoked / Blacklisted)"
                );
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                return;
            }

            // 2. Xác thực tính hợp lệ và thời hạn của Token
            if (jwtUtils.validateToken(token)) {
                try {
                    String username = jwtUtils.extractUsername(token);
                    Long userId = jwtUtils.extractUserId(token);
                    String tenantId = jwtUtils.extractTenantId(token);
                    List<String> roles = jwtUtils.extractRoles(token);
                    List<String> permissions = jwtUtils.extractPermissions(token);

                    Set<String> roleSet = (roles != null) ? new HashSet<>(roles) : new HashSet<>();
                    Set<String> permSet = (permissions != null) ? new HashSet<>(permissions) : new HashSet<>();

                    // Đồng bộ TenantContext nếu token có chứa tenantId
                    if (tenantId != null && !tenantId.isBlank()) {
                        TenantContext.setTenantId(tenantId);
                    }

                    // Nạp vào UserContext ThreadLocal
                    UserContext.setUser(UserContext.UserInfo.builder()
                            .userId(userId)
                            .username(username)
                            .tenantId(tenantId)
                            .roles(roleSet)
                            .permissions(permSet)
                            .build());

                    // Chuyển đổi sang GrantedAuthorities của Spring Security
                    Set<SimpleGrantedAuthority> authorities = new HashSet<>();
                    for (String role : roleSet) {
                        authorities.add(new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role));
                    }
                    for (String perm : permSet) {
                        authorities.add(new SimpleGrantedAuthority(perm));
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (Exception e) {
                    log.error("[JwtAuthenticationFilter] Không thể thiết lập Security Context: {}", e.getMessage());
                }
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Dọn dẹp UserContext để tránh rò rỉ bộ nhớ giữa các Thread Pool
            UserContext.clear();
        }
    }
}
