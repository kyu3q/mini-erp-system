package com.minierpapp.model.order.dto;

import com.minierpapp.model.order.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private String orderNumber;
    private LocalDate orderDate;
    private Long customerId;
    private String customerName;
    private LocalDate deliveryDate;
    private String shippingAddress;
    private String shippingPostalCode;
    private String shippingPhone;
    private String shippingContactPerson;
    private OrderStatus status;
    private String notes;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private List<OrderDetailResponse> orderDetails;

    @Data
    public static class OrderDetailResponse {
        private Long id;
        private Integer lineNumber;
        private Long productId;
        private String productName;
        private String productCode;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private Long warehouseId;
        private String warehouseName;
        private LocalDate deliveryDate;
        private String notes;
    }
}