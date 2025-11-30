package it.noxno.hbci.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO for requesting transactions for a specific period.
 */
public class TransactionRequestDTO {

    @NotNull(message = "Account ID is required")
    public Long accountId;

    @NotNull(message = "Start date is required")
    public LocalDate startDate;

    @NotNull(message = "End date is required")
    public LocalDate endDate;
}
