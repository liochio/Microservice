package com.liochio.payment.entity;

import com.liochio.common.entity.BaseEntity;
import com.liochio.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

/**
 * ==============================================================================
 * Thực Thể Đơn Hàng Thanh Toán (Payment Order Entity)
 * ==============================================================================
 */
@Entity
@Table(name = "payment_orders")
@SQLDelete(sql = "UPDATE payment_orders SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderEntity extends BaseEntity {

    @Column(name = "order_id", length = 100, nullable = false, unique = true)
    private String orderId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "gateway", length = 50, nullable = false)
    private String gateway;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 10, nullable = false)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "order_info", length = 255)
    private String orderInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "transaction_no", length = 100)
    private String transactionNo;

    @Column(name = "response_code", length = 50)
    private String responseCode;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getOrderInfo() { return orderInfo; }
    public void setOrderInfo(String orderInfo) { this.orderInfo = orderInfo; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getTransactionNo() { return transactionNo; }
    public void setTransactionNo(String transactionNo) { this.transactionNo = transactionNo; }
    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }
}
