package com.minierpapp.model.order.dto;

import com.minierpapp.model.order.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class OrderRequest {
    private Long id;
    private String orderNumber;
    private LocalDate orderDate;
    private Long customerId;
    private LocalDate deliveryDate;
    private String shippingAddress;
    private String shippingPostalCode;
    private String shippingPhone;
    private String shippingContactPerson;
    private OrderStatus status;
    private String notes;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private List<OrderDetailRequest> orderDetails;

    @Data
    public static class OrderDetailRequest {
        private Integer lineNumber;
        private String itemName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private Long warehouseId;
        private LocalDate deliveryDate;
        private String notes;
    }
}