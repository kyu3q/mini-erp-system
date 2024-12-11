package com.minierpapp.model.order;

import com.minierpapp.model.common.BaseEntity;
import com.minierpapp.model.customer.Customer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order extends BaseEntity {

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "shipping_postal_code")
    private String shippingPostalCode;

    @Column(name = "shipping_phone")
    private String shippingPhone;

    @Column(name = "shipping_contact_person")
    private String shippingContactPerson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "notes")
    private String notes;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    public void addOrderDetail(OrderDetail detail) {
        orderDetails.add(detail);
        detail.setOrder(this);
    }

    public void removeOrderDetail(OrderDetail detail) {
        orderDetails.remove(detail);
        detail.setOrder(null);
    }
}