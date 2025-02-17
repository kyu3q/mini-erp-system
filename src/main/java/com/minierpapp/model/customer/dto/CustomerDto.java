package com.minierpapp.model.customer.dto;

import java.time.LocalDateTime;

import com.minierpapp.model.common.Status;
import lombok.Data;

@Data
public class CustomerDto {
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
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private boolean deleted;
}