package it.noxno.hbci.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating or updating a bank account.
 */
public class BankAccountDTO {

    @NotBlank(message = "Account number is required")
    public String accountNumber;

    @NotBlank(message = "Bank code is required")
    public String bankCode;

    @NotBlank(message = "Account holder name is required")
    public String accountHolderName;

    @NotBlank(message = "HBCI URL is required")
    public String hbciUrl;

    @NotNull(message = "HBCI version is required")
    public String hbciVersion = "300";

    public String userId;

    public String customerId;

    @NotNull
    public Boolean active = true;
}
