package com.minierpapp.model.customer.dto;

import com.minierpapp.model.common.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerRequest {
    private Long id;

    @NotBlank(message = "得意先コードは必須です")
    @Size(max = 50, message = "得意先コードは50文字以内で入力してください")
    private String customerCode;

    @NotBlank(message = "得意先名は必須です")
    @Size(max = 100, message = "得意先名は100文字以内で入力してください")
    private String name;

    @Size(max = 100, message = "得意先名（カナ）は100文字以内で入力してください")
    private String nameKana;

    @Pattern(regexp = "^$|^\\d{3}-\\d{4}$", message = "郵便番号の形式が正しくありません")
    private String postalCode;

    @Size(max = 200, message = "住所は200文字以内で入力してください")
    private String address;

    @Pattern(regexp = "^$|^\\d{2,4}-\\d{2,4}-\\d{4}$", message = "電話番号の形式が正しくありません")
    private String phone;

    @Email(message = "メールアドレスの形式が正しくありません")
    @Size(max = 100, message = "メールアドレスは100文字以内で入力してください")
    private String email;

    @Pattern(regexp = "^$|^\\d{2,4}-\\d{2,4}-\\d{4}$", message = "FAX番号の形式が正しくありません")
    private String fax;

    @Size(max = 100, message = "担当者名は100文字以内で入力してください")
    private String contactPerson;

    @Size(max = 100, message = "支払条件は100文字以内で入力してください")
    private String paymentTerms;

    private Status status;
    private String notes;
}