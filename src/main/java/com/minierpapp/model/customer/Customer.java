package com.minierpapp.model.customer;

import com.minierpapp.model.base.BaseEntity;
import com.minierpapp.model.common.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_customers_customer_code_not_deleted",
                        columnNames = {"customer_code", "deleted"})
        })
@Getter
@Setter
public class Customer extends BaseEntity {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer customer)) return false;
        if (!super.equals(o)) return false;
        return getId() != null && getId().equals(customer.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 31;
    }

    @NotBlank
    @Column(name = "customer_code", nullable = false, length = 50)
    private String customerCode;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "name_kana", length = 100)
    private String nameKana;

    @Column(name = "postal_code", length = 8)
    private String postalCode;

    @Column(length = 200)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String fax;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}