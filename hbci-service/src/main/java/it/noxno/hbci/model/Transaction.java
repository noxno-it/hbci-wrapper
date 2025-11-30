package it.noxno.hbci.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a bank transaction retrieved via HBCI.
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_account_date", columnList = "bank_account_id,valueDate")
})
public class Transaction extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    public BankAccount bankAccount;

    @Column(nullable = false)
    public LocalDate valueDate;

    @Column(nullable = false)
    public LocalDate bookingDate;

    @Column(nullable = false, precision = 15, scale = 2)
    public BigDecimal amount;

    @Column(nullable = false, length = 3)
    public String currency = "EUR";

    @Column(length = 1000)
    public String purpose;

    @Column
    public String otherAccountNumber;

    @Column
    public String otherBankCode;

    @Column
    public String otherName;

    @Column
    public String transactionCode;

    @Column(unique = true)
    public String externalId;

    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt = LocalDateTime.now();
}
