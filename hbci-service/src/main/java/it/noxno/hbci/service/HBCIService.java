package it.noxno.hbci.service;

import it.noxno.hbci.dto.BalanceDTO;
import it.noxno.hbci.dto.TransactionDTO;
import it.noxno.hbci.model.BankAccount;
import it.noxno.hbci.model.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.callback.HBCICallbackConsole;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Service for interacting with HBCI/FinTS banking systems.
 * This service wraps the LGPL-licensed hbci4java library and provides
 * a clean REST API interface for consumers.
 */
@ApplicationScoped
public class HBCIService {

    private static final Logger LOG = Logger.getLogger(HBCIService.class);

    static {
        // Initialize HBCI4Java
        HBCIUtils.init(null, null);
    }

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
            HBCIPassport passport = createPassport(account);
            HBCIHandler handler = new HBCIHandler(account.hbciVersion, passport);

            // Create balance job
            HBCIJob job = handler.newJob("SaldoReq");
            job.setParam("my", getKonto(account));
            job.addToQueue();

            HBCIExecStatus status = handler.execute();
            
            if (status.isOK()) {
                // Extract balance from response
                Properties result = job.getJobResult().getJobStatus().getData();
                String bookedBalanceStr = result.getProperty("booked.value");
                String pendingBalanceStr = result.getProperty("pending.value");
                
                if (bookedBalanceStr != null) {
                    balance.bookedBalance = new BigDecimal(bookedBalanceStr);
                }
                if (pendingBalanceStr != null) {
                    balance.pendingBalance = new BigDecimal(pendingBalanceStr);
                }
            } else {
                LOG.errorf("Failed to fetch balance: %s", status.getErrorString());
            }

            handler.close();
            passport.close();

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
            HBCIPassport passport = createPassport(account);
            HBCIHandler handler = new HBCIHandler(account.hbciVersion, passport);

            // Create transaction list job
            HBCIJob job = handler.newJob("KUmsAll");
            job.setParam("my", getKonto(account));
            job.setParam("startdate", dateToHBCIDate(startDate));
            job.setParam("enddate", dateToHBCIDate(endDate));
            job.addToQueue();

            HBCIExecStatus status = handler.execute();
            
            if (status.isOK()) {
                // Parse transactions from response
                Properties result = job.getJobResult().getJobStatus().getData();
                transactions = parseTransactions(account, result);
                
                // Persist to database
                persistTransactions(account, transactions);
            } else {
                LOG.errorf("Failed to fetch transactions: %s", status.getErrorString());
            }

            handler.close();
            passport.close();

        } catch (Exception e) {
            LOG.error("Error fetching transactions", e);
            throw new RuntimeException("Failed to fetch transactions: " + e.getMessage(), e);
        }

        return transactions;
    }

    private HBCIPassport createPassport(BankAccount account) {
        Properties props = new Properties();
        props.setProperty("client.passport.default", "PinTan");
        props.setProperty("client.passport.PinTan.checkcert", "1");
        props.setProperty("client.passport.PinTan.init", "1");
        
        HBCICallback callback = new HBCICallbackConsole();
        HBCIPassport passport = AbstractHBCIPassport.getInstance("PinTan", props, callback);
        
        return passport;
    }

    private Konto getKonto(BankAccount account) {
        Konto konto = new Konto();
        konto.blz = account.bankCode;
        konto.number = account.accountNumber;
        konto.name = account.accountHolderName;
        konto.curr = "EUR";
        return konto;
    }

    private Date dateToHBCIDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private List<TransactionDTO> parseTransactions(BankAccount account, Properties result) {
        List<TransactionDTO> transactions = new ArrayList<>();
        // Simplified parsing - in real implementation would parse HBCI response format
        // This is a placeholder for the actual HBCI response parsing logic
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
