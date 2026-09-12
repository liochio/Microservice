package com.liochio.payment.controller;

import com.liochio.common.annotation.Idempotent;
import com.liochio.common.constant.MessageConstants;
import com.liochio.common.context.TenantContext;
import com.liochio.common.context.UserContext;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.dto.PageResponse;
import com.liochio.common.i18n.MessageService;
import com.liochio.payment.entity.BookingEntity;
import com.liochio.payment.repository.BookingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * ==============================================================================
 * Controller Đơn Hàng & Đặt Chỗ (Booking Controller)
 * ==============================================================================
 */
@RestController
@RequestMapping("/api/payments/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Controller", description = "Các API tạo đơn hàng, đặt chỗ tour/khách sạn/dịch vụ và thanh toán")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final MessageService messageService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Idempotent(timeoutSeconds = 60, required = false)
    @Operation(summary = "Tạo một đơn hàng / đặt chỗ mới (chống trùng lặp Idempotency)")
    public ApiResponse<BookingEntity> createBooking(@RequestBody BookingEntity request) {
        request.setTenantId(TenantContext.getTenantId());
        request.setCustomerId(UserContext.getUserId() != null ? UserContext.getUserId() : 1L);
        if (request.getBookingCode() == null || request.getBookingCode().isBlank()) {
            request.setBookingCode("BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        BookingEntity saved = bookingRepository.save(request);
        return ApiResponse.created(saved, messageService.getMessage(MessageConstants.MSG_BOOKING_CREATED));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách các đơn đặt chỗ của người dùng")
    public ApiResponse<PageResponse<BookingEntity>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String tenantId = TenantContext.getTenantId();
        Long userId = UserContext.getUserId() != null ? UserContext.getUserId() : 1L;
        Page<BookingEntity> pageResult = bookingRepository.findByTenantIdAndCustomerId(
                tenantId, userId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ApiResponse.success(PageResponse.of(pageResult));
    }
}
