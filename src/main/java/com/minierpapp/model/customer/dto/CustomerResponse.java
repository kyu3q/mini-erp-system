package com.minierpapp.model.customer.dto;

import com.minierpapp.model.common.Status;
import lombok.Data;

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
}