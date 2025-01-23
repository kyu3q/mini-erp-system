package com.minierpapp.model.customer.dto;

import com.minierpapp.model.common.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerResponse {
    private Long id;
    private String customerCode;
    private String name;
    private String nameKana;
    private String postalCode;
    private String address;
    private String phone;
    private String email;
    private String fax;
    private String contactPerson;
    private String paymentTerms;
    private Status status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}