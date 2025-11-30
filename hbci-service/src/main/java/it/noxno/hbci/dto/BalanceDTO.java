package it.noxno.hbci.dto;

import java.math.BigDecimal;

/**
 * DTO for account balance information.
 */
public class BalanceDTO {

    public Long accountId;
    public BigDecimal bookedBalance;
    public BigDecimal pendingBalance;
    public String currency;
    public String timestamp;
}
