package com.minierpapp.model.supplier;

import com.minierpapp.model.base.BaseEntity;
import com.minierpapp.model.common.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "suppliers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_suppliers_supplier_code_not_deleted",
                        columnNames = {"supplier_code", "deleted"})
        })
@Getter
@Setter
public class Supplier extends BaseEntity {
    @Column(name = "supplier_code", nullable = false, length = 50)
    private String supplierCode;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}