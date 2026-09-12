package com.liochio.payment.controller;

import com.liochio.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ==============================================================================
 * Controller Tiếp Nhận Callback IPN Webhook Từ Cổng Thanh Toán (IPN Controller)
 * ==============================================================================
 */
@Slf4j
@RestController
@RequestMapping("/api/payments/ipn")
@RequiredArgsConstructor
@Tag(name = "Payment IPN Webhook Controller", description = "Endpoint tiếp nhận thông báo kết quả giao dịch ngầm (Server-to-Server) từ cổng thanh toán")
public class IpnWebhookController {

    private final PaymentService paymentService;

    @GetMapping("/vnpay")
    @Operation(summary = "Tiếp nhận IPN callback từ cổng VNPay")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> allParams) {
        log.info("[IpnController] Nhận callback từ VNPay: {}", allParams);
        boolean success = paymentService.handleIpn("VNPAY", allParams);
        if (success) {
            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        } else {
            return ResponseEntity.ok(Map.of("RspCode", "97", "Message", "Invalid Checksum"));
        }
    }

    @PostMapping("/stripe")
    @Operation(summary = "Tiếp nhận Webhook từ Stripe")
    public ResponseEntity<String> stripeWebhook(@RequestBody Map<String, String> payload) {
        log.info("[IpnController] Nhận webhook từ Stripe");
        paymentService.handleIpn("STRIPE", payload);
        return ResponseEntity.ok("Received");
    }

    @PostMapping("/momo")
    @Operation(summary = "Tiếp nhận IPN từ MoMo")
    public ResponseEntity<Map<String, Object>> momoIpn(@RequestBody Map<String, String> payload) {
        log.info("[IpnController] Nhận IPN từ MoMo: {}", payload);
        paymentService.handleIpn("MOMO", payload);
        return ResponseEntity.ok(Map.of("status", 0, "message", "Success"));
    }
}
