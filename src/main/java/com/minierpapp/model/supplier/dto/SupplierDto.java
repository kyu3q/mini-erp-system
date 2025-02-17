package com.minierpapp.model.supplier.dto;

import java.time.LocalDateTime;

import com.minierpapp.model.common.Status;
import lombok.Data;

@Data
public class SupplierDto {
    private Long id;
    private String supplierCode;
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