package com.minierpapp.model.supplier.dto;

import com.minierpapp.model.common.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierRequest {
    @NotBlank(message = "仕入先コードは必須です")
    @Size(max = 50, message = "仕入先コードは50文字以内で入力してください")
    private String supplierCode;

    @NotBlank(message = "仕入先名は必須です")
    @Size(max = 100, message = "仕入先名は100文字以内で入力してください")
    private String name;

    @Size(max = 100, message = "仕入先名（カナ）は100文字以内で入力してください")
    private String nameKana;

    @Pattern(regexp = "^[0-9]{3}-[0-9]{4}$", message = "郵便番号の形式が正しくありません")
    private String postalCode;

    @Size(max = 200, message = "住所は200文字以内で入力してください")
    private String address;

    @Pattern(regexp = "^[0-9-]{10,20}$", message = "電話番号の形式が正しくありません")
    private String phone;

    @Email(message = "メールアドレスの形式が正しくありません")
    @Size(max = 100, message = "メールアドレスは100文字以内で入力してください")
    private String email;

    @Pattern(regexp = "^[0-9-]{10,20}$", message = "FAX番号の形式が正しくありません")
    private String fax;

    @Size(max = 100, message = "担当者名は100文字以内で入力してください")
    private String contactPerson;

    @Size(max = 100, message = "支払条件は100文字以内で入力してください")
    private String paymentTerms;

    private Status status;
    private String notes;
}