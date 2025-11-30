package it.noxno.hbci.service;

import it.noxno.hbci.dto.BalanceDTO;
import it.noxno.hbci.dto.TransactionDTO;
import it.noxno.hbci.model.BankAccount;
import it.noxno.hbci.model.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.callback.HBCICallbackConsole;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Saldo;
import org.kapott.hbci.structures.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Service for interacting with HBCI/FinTS banking systems.
 * This service wraps the LGPL-licensed hbci4java library and provides
 * a clean REST API interface for consumers.
 * 
 * Implementation includes:
 * - PIN/TAN authentication via HBCIPassport
 * - Secure credential storage with encryption
 * - Balance retrieval via HBCI
 * - Transaction fetching with date ranges
 * - Error handling and logging
 */
@ApplicationScoped
public class HBCIService {

    private static final Logger LOG = Logger.getLogger(HBCIService.class);

    @Inject
    EncryptionService encryptionService;

    static {
        // Initialize HBCI4Java logging
        HBCIUtils.init(null, null);
    }

    /**
     * Fetches the account balance for a given bank account via HBCI.
     * 
     * @param account The bank account to query
     * @return Balance information
     */
    public BalanceDTO getBalance(BankAccount account) {
        LOG.infof("Fetching balance for account %s at bank %s", 
                  account.accountNumber, account.bankCode);
        
        BalanceDTO balance = new BalanceDTO();
        balance.accountId = account.id;
        balance.currency = "EUR";
        balance.timestamp = LocalDateTime.now().toString();

        HBCIPassport passport = null;
        HBCIHandler handler = null;

        try {
            // Create passport for authentication
            passport = createPassport(account);
            
            // Initialize HBCI handler
            handler = new HBCIHandler(account.hbciVersion, passport);
            
            // Create balance request job
            HBCIJob job = handler.newJob("SaldoReq");
            job.setParam("my", createKonto(account));
            job.addToQueue();

            // Execute the job
            HBCIExecStatus status = handler.execute();
            
            if (status.isOK()) {
                // Parse balance from result
                GVRSaldoReq result = (GVRSaldoReq) job.getJobResult();
                if (result.isOK()) {
                    Saldo saldo = result.getEntries()[0].ready;
                    if (saldo != null && saldo.value != null) {
                        balance.bookedBalance = new BigDecimal(saldo.value.getDoubleValue());
                    }
                    
                    // Try to get pending balance if available
                    Saldo unready = result.getEntries()[0].unready;
                    if (unready != null && unready.value != null) {
                        balance.pendingBalance = new BigDecimal(unready.value.getDoubleValue());
                    }
                    
                    LOG.infof("Balance retrieved successfully: %s %s", 
                             balance.bookedBalance, balance.currency);
                } else {
                    LOG.errorf("Balance job failed: %s", result.getJobStatus().getErrorString());
                    throw new RuntimeException("Failed to retrieve balance: " + 
                                             result.getJobStatus().getErrorString());
                }
            } else {
                LOG.errorf("HBCI execution failed: %s", status.getErrorString());
                throw new RuntimeException("HBCI execution failed: " + status.getErrorString());
            }

        } catch (Exception e) {
            LOG.error("Error fetching balance via HBCI", e);
            throw new RuntimeException("Failed to fetch balance: " + e.getMessage(), e);
        } finally {
            cleanup(handler, passport);
        }

        return balance;
    }

    /**
     * Fetches transactions for a given account and date range via HBCI.
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
        HBCIPassport passport = null;
        HBCIHandler handler = null;

        try {
            // Create passport for authentication
            passport = createPassport(account);
            
            // Initialize HBCI handler
            handler = new HBCIHandler(account.hbciVersion, passport);

            // Create transaction list job
            HBCIJob job = handler.newJob("KUmsAll");
            job.setParam("my", createKonto(account));
            job.setParam("startdate", dateToString(startDate));
            job.setParam("enddate", dateToString(endDate));
            job.addToQueue();

            // Execute the job
            HBCIExecStatus status = handler.execute();
            
            if (status.isOK()) {
                // Parse transactions from result
                GVRKUms result = (GVRKUms) job.getJobResult();
                if (result.isOK()) {
                    transactions = parseTransactions(account, result);
                    LOG.infof("Retrieved %d transactions", transactions.size());
                    
                    // Persist to database
                    persistTransactions(account, transactions);
                } else {
                    LOG.errorf("Transaction job failed: %s", result.getJobStatus().getErrorString());
                    throw new RuntimeException("Failed to retrieve transactions: " + 
                                             result.getJobStatus().getErrorString());
                }
            } else {
                LOG.errorf("HBCI execution failed: %s", status.getErrorString());
                throw new RuntimeException("HBCI execution failed: " + status.getErrorString());
            }

        } catch (Exception e) {
            LOG.error("Error fetching transactions via HBCI", e);
            throw new RuntimeException("Failed to fetch transactions: " + e.getMessage(), e);
        } finally {
            cleanup(handler, passport);
        }

        return transactions;
    }

    /**
     * Creates an HBCI passport for authentication.
     * Uses PIN/TAN method with encrypted credentials.
     */
    private HBCIPassport createPassport(BankAccount account) {
        try {
            Properties props = new Properties();
            
            // Configure PIN/TAN passport
            props.setProperty("client.passport.default", "PinTan");
            props.setProperty("client.passport.PinTan.filename", 
                            "/tmp/hbci-passport-" + account.id + ".dat");
            props.setProperty("client.passport.PinTan.init", "1");
            props.setProperty("client.passport.PinTan.checkcert", "1");
            
            // Bank connection settings
            props.setProperty("client.passport.country", "DE");
            props.setProperty("client.passport.blz", account.bankCode);
            props.setProperty("client.passport.userid", account.userId != null ? account.userId : account.accountNumber);
            props.setProperty("client.passport.customerid", account.customerId != null ? account.customerId : account.userId);
            
            // Create callback for PIN entry
            HBCICallback callback = new HBCICallbackWithPin(account, encryptionService);
            
            // Create passport
            HBCIPassport passport = AbstractHBCIPassport.getInstance("PinTan", props);
            passport.setHost(account.hbciUrl);
            
            return passport;
            
        } catch (Exception e) {
            LOG.error("Error creating HBCI passport", e);
            throw new RuntimeException("Failed to create HBCI passport", e);
        }
    }

    /**
     * Creates a Konto object from BankAccount entity.
     */
    private Konto createKonto(BankAccount account) {
        Konto konto = new Konto();
        konto.blz = account.bankCode;
        konto.number = account.accountNumber;
        konto.name = account.accountHolderName;
        konto.curr = "EUR";
        konto.customerid = account.customerId != null ? account.customerId : account.userId;
        // Note: userid field doesn't exist in Konto, user ID is handled by passport
        return konto;
    }

    /**
     * Converts LocalDate to HBCI date format string.
     */
    private String dateToString(LocalDate date) {
        return String.format("%04d%02d%02d", 
                           date.getYear(), 
                           date.getMonthValue(), 
                           date.getDayOfMonth());
    }

    /**
     * Parses transactions from HBCI result.
     */
    private List<TransactionDTO> parseTransactions(BankAccount account, GVRKUms result) {
        List<TransactionDTO> transactions = new ArrayList<>();
        
        if (result.getFlatData() != null) {
            for (GVRKUms.UmsLine line : result.getFlatData()) {
                TransactionDTO dto = new TransactionDTO();
                dto.bankAccountId = account.id;
                
                // Parse date
                if (line.valuta != null) {
                    dto.valueDate = line.valuta.toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                }
                if (line.bdate != null) {
                    dto.bookingDate = line.bdate.toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                }
                
                // Parse amount
                if (line.value != null) {
                    dto.amount = new BigDecimal(line.value.getDoubleValue());
                }
                dto.currency = line.value != null ? line.value.getCurr() : "EUR";
                
                // Parse text/purpose
                List<String> purposes = line.usage;
                if (purposes != null && !purposes.isEmpty()) {
                    dto.purpose = String.join(" ", purposes);
                }
                
                // Parse other account info
                if (line.other != null) {
                    dto.otherAccountNumber = line.other.number;
                    dto.otherBankCode = line.other.blz;
                    dto.otherName = line.other.name;
                }
                
                // Transaction code
                dto.transactionCode = line.text;
                
                transactions.add(dto);
            }
        }
        
        return transactions;
    }

    /**
     * Persists transactions to database.
     */
    private void persistTransactions(BankAccount account, List<TransactionDTO> dtos) {
        for (TransactionDTO dto : dtos) {
            // Generate unique external ID
            String externalId = generateExternalId(dto);
            
            // Check if transaction already exists
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
                
                LOG.debugf("Persisted transaction: %s", externalId);
            }
        }
    }

    /**
     * Generates unique external ID for a transaction.
     */
    private String generateExternalId(TransactionDTO dto) {
        return String.format("%s-%s-%s-%s", 
                           dto.bankAccountId,
                           dto.bookingDate,
                           dto.amount,
                           dto.otherAccountNumber != null ? dto.otherAccountNumber : "");
    }

    /**
     * Cleanup HBCI resources.
     */
    private void cleanup(HBCIHandler handler, HBCIPassport passport) {
        if (handler != null) {
            try {
                handler.close();
            } catch (Exception e) {
                LOG.warn("Error closing HBCI handler", e);
            }
        }
        if (passport != null) {
            try {
                passport.close();
            } catch (Exception e) {
                LOG.warn("Error closing HBCI passport", e);
            }
        }
    }

    /**
     * Custom HBCI callback that provides encrypted PIN.
     */
    private static class HBCICallbackWithPin extends HBCICallbackConsole {
        private final BankAccount account;
        private final EncryptionService encryptionService;

        public HBCICallbackWithPin(BankAccount account, EncryptionService encryptionService) {
            this.account = account;
            this.encryptionService = encryptionService;
        }

        @Override
        public void callback(HBCIPassport passport, int reason, String msg, 
                           int datatype, StringBuffer retData) {
            // Provide PIN from encrypted storage
            if (reason == NEED_PT_PIN || reason == NEED_PT_TAN) {
                if (account.encryptedPin != null) {
                    String pin = encryptionService.decrypt(account.encryptedPin);
                    retData.replace(0, retData.length(), pin);
                    return;
                }
            }
            
            // Fallback to console callback for other reasons
            super.callback(passport, reason, msg, datatype, retData);
        }
    }
}
