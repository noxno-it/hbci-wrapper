package it.noxno.hbci.service;

import it.noxno.hbci.dto.BalanceDTO;
import it.noxno.hbci.dto.TransactionDTO;
import it.noxno.hbci.model.BankAccount;
import it.noxno.hbci.model.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for interacting with HBCI/FinTS banking systems.
 * This service wraps the LGPL-licensed hbci4java library and provides
 * a clean REST API interface for consumers.
 * 
 * Note: This is a simplified implementation for demonstration purposes.
 * In a production environment, you would implement the actual HBCI4Java integration.
 */
@ApplicationScoped
public class HBCIService {

    private static final Logger LOG = Logger.getLogger(HBCIService.class);

    /**
     * Fetches the account balance for a given bank account.
     * 
     * @param account The bank account to query
     * @return Balance information
     */
    public BalanceDTO getBalance(BankAccount account) {
        LOG.infof("Fetching balance for account %s", account.accountNumber);
        
        BalanceDTO balance = new BalanceDTO();
        balance.accountId = account.id;
        balance.currency = "EUR";
        balance.timestamp = LocalDateTime.now().toString();

        try {
            // TODO: Implement actual HBCI4Java integration here
            // This is a placeholder implementation
            // Real implementation would use:
            // - HBCIPassport for authentication
            // - HBCIHandler for communication
            // - HBCIJob for balance requests
            
            // For now, return demo data
            LOG.warn("Using demo data - HBCI4Java integration not yet implemented");
            balance.bookedBalance = new BigDecimal("1234.56");
            balance.pendingBalance = new BigDecimal("1234.56");
            
        } catch (Exception e) {
            LOG.error("Error fetching balance", e);
            throw new RuntimeException("Failed to fetch balance: " + e.getMessage(), e);
        }

        return balance;
    }

    /**
     * Fetches transactions for a given account and date range.
     * 
     * @param account The bank account
     * @param startDate Start date for transactions
     * @param endDate End date for transactions
     * @return List of transactions
     */
    @Transactional
    public List<TransactionDTO> getTransactions(BankAccount account, LocalDate startDate, LocalDate endDate) {
        LOG.infof("Fetching transactions for account %s from %s to %s", 
                  account.accountNumber, startDate, endDate);
        
        List<TransactionDTO> transactions = new ArrayList<>();

        try {
            // TODO: Implement actual HBCI4Java integration here
            // This is a placeholder implementation
            // Real implementation would use:
            // - HBCIPassport for authentication
            // - HBCIHandler for communication
            // - HBCIJob for transaction requests
            // - Parse SWIFT MT940 format responses
            
            // For now, return demo data
            LOG.warn("Using demo data - HBCI4Java integration not yet implemented");
            
            // Create a sample transaction
            TransactionDTO dto = new TransactionDTO();
            dto.bankAccountId = account.id;
            dto.valueDate = LocalDate.now().minusDays(1);
            dto.bookingDate = LocalDate.now().minusDays(1);
            dto.amount = new BigDecimal("-50.00");
            dto.currency = "EUR";
            dto.purpose = "Sample transaction";
            dto.otherName = "Demo Merchant";
            transactions.add(dto);
            
            // Persist demo transactions
            persistTransactions(account, transactions);
            
        } catch (Exception e) {
            LOG.error("Error fetching transactions", e);
            throw new RuntimeException("Failed to fetch transactions: " + e.getMessage(), e);
        }

        return transactions;
    }

    private void persistTransactions(BankAccount account, List<TransactionDTO> dtos) {
        for (TransactionDTO dto : dtos) {
            // Check if transaction already exists
            String externalId = generateExternalId(dto);
            Transaction existing = Transaction.find("externalId", externalId).firstResult();
            
            if (existing == null) {
                Transaction transaction = new Transaction();
                transaction.bankAccount = account;
                transaction.valueDate = dto.valueDate;
                transaction.bookingDate = dto.bookingDate;
                transaction.amount = dto.amount;
                transaction.currency = dto.currency;
                transaction.purpose = dto.purpose;
                transaction.otherAccountNumber = dto.otherAccountNumber;
                transaction.otherBankCode = dto.otherBankCode;
                transaction.otherName = dto.otherName;
                transaction.transactionCode = dto.transactionCode;
                transaction.externalId = externalId;
                transaction.persist();
                
                LOG.infof("Persisted transaction: %s", externalId);
            }
        }
    }

    private String generateExternalId(TransactionDTO dto) {
        return String.format("%s-%s-%s-%s", 
                            dto.bankAccountId,
                            dto.bookingDate,
                            dto.amount,
                            dto.otherAccountNumber != null ? dto.otherAccountNumber : "");
    }
}
