package it.noxno.hbci.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for transaction data returned via API.
 */
public class TransactionDTO {

    public Long id;
    public Long bankAccountId;
    public LocalDate valueDate;
    public LocalDate bookingDate;
    public BigDecimal amount;
    public String currency;
    public String purpose;
    public String otherAccountNumber;
    public String otherBankCode;
    public String otherName;
    public String transactionCode;
}
