package com.liochio.payment.controller;

import com.liochio.common.annotation.Idempotent;
import com.liochio.common.annotation.RequirePermission;
import com.liochio.common.constant.MessageConstants;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.i18n.MessageService;
import com.liochio.payment.dto.CreatePaymentRequest;
import com.liochio.payment.dto.PaymentOrderResponse;
import com.liochio.payment.dto.PaymentUrlResponse;
import com.liochio.payment.service.PaymentService;
import com.liochio.payment.service.SagaCompensationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * ==============================================================================
 * Controller Khởi Tạo Giao Dịch Thanh Toán (Payment Controller)
 * ==============================================================================
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Controller", description = "Các API khởi tạo thanh toán qua VNPay, MoMo, Stripe, quản lý tính bất biến và bù trừ Saga")
public class PaymentController {

    private final PaymentService paymentService;
    private final MessageService messageService;
    private final SagaCompensationService sagaCompensationService;

    @PostMapping("/create-url")
    @ResponseStatus(HttpStatus.CREATED)
    @Idempotent(timeoutSeconds = 60)
    @RequirePermission("portfolio:write")
    @Operation(summary = "Khởi tạo URL thanh toán chuyển hướng tới cổng (VNPay, MoMo, Stripe) với Idempotency Key")
    public ApiResponse<PaymentUrlResponse> createPaymentUrl(
            @Valid @RequestBody CreatePaymentRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            HttpServletRequest httpServletRequest
    ) {
        String clientIp = httpServletRequest.getRemoteAddr();
        PaymentUrlResponse response = paymentService.createPayment(request, clientIp, idempotencyKey);
        return ApiResponse.created(response, messageService.getMessage(MessageConstants.MSG_PAYMENT_URL_CREATED));
    }

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "Xem trạng thái đơn hàng thanh toán theo Order ID")
    public ApiResponse<PaymentOrderResponse> getOrder(@PathVariable String orderId) {
        PaymentOrderResponse response = paymentService.getOrderByOrderId(orderId);
        return ApiResponse.success(response);
    }

    @PostMapping("/orders/{orderId}/reconcile")
    @Operation(summary = "Chủ động đối soát trạng thái đơn hàng (Active Status Reconciliation)")
    public ApiResponse<PaymentOrderResponse> reconcileOrder(@PathVariable String orderId) {
        PaymentOrderResponse response = paymentService.reconcilePendingOrder(orderId);
        return ApiResponse.success(response, "Đối soát trạng thái đơn hàng thành công");
    }

    @PostMapping("/compensate")
    @Operation(summary = "Kích hoạt giao dịch bù trừ Saga (Saga Compensating Transaction)")
    public ApiResponse<com.liochio.common.saga.SagaEvent> compensate(
            @RequestParam String orderId,
            @RequestParam(defaultValue = "Giao dịch chặng sau bị lỗi") String reason,
            @RequestHeader(value = "X-Trace-ID", required = false) String traceId
    ) {
        return ApiResponse.success(sagaCompensationService.executeCompensation(orderId, reason, traceId != null ? traceId : "trace_manual"));
    }
}