package com.liochio.auth.controller;

import com.liochio.common.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * ==============================================================================
 * Centralized Identity Provider JWKS Endpoint (RFC 7517)
 * ==============================================================================
 */
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "JWKS Key Discovery", description = "Endpoint công khai cung cấp JWKS RFC 7517")
public class JwksController {

    private final JwtUtils jwtUtils;

    @GetMapping(value = {"/.well-known/jwks.json", "/api/v1/auth/.well-known/jwks.json", "/api/auth/.well-known/jwks.json"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lấy danh sách khóa công khai JWKS", description = "Trả về RFC 7517 JWKS Public Key để Resource Server xác thực RS256 offline")
    public Map<String, Object> getJwks() {
        return jwtUtils.getJwks();
    }
}
