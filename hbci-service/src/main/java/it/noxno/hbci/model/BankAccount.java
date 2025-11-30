package it.noxno.hbci.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Entity representing a bank account configured for HBCI access.
 * This stores the configuration needed to communicate with the bank via HBCI.
 */
@Entity
@Table(name = "bank_accounts")
public class BankAccount extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotBlank
    @Column(nullable = false)
    public String accountNumber;

    @NotBlank
    @Column(nullable = false)
    public String bankCode;

    @NotBlank
    @Column(nullable = false)
    public String accountHolderName;

    @NotBlank
    @Column(nullable = false)
    public String hbciUrl;

    @NotNull
    @Column(nullable = false)
    public String hbciVersion = "300";

    @Column
    public String userId;

    @Column
    public String customerId;

    @Column(nullable = false)
    public Boolean active = true;

    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    public LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
