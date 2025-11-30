# HBCI Wrapper Client Onboarding Guide

This guide helps you integrate the HBCI Wrapper microservice into your Quarkus backend application.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Integration Steps](#integration-steps)
3. [Configuration](#configuration)
4. [Usage Examples](#usage-examples)
5. [Best Practices](#best-practices)
6. [License Considerations](#license-considerations)
7. [Troubleshooting](#troubleshooting)

## Prerequisites

Before integrating the HBCI Wrapper client, ensure you have:

- Java 17 or higher
- Maven 3.9+ or Gradle 8+
- A running instance of the HBCI Wrapper service
- Access credentials for the HBCI service (if authentication is enabled)

## Integration Steps

### Step 1: Add Maven Dependency

Add the HBCI client library to your `pom.xml`:

```xml
<dependency>
    <groupId>it.noxno</groupId>
    <artifactId>hbci-client</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**For Gradle**, add to `build.gradle`:

```gradle
implementation 'it.noxno:hbci-client:1.0.0-SNAPSHOT'
```

### Step 2: Configure the Client

Create a configuration class in your application:

```java
package com.yourcompany.config;

import it.noxno.hbci.client.ApiClient;
import it.noxno.hbci.client.api.BankAccountsApi;
import it.noxno.hbci.client.api.HbciOperationsApi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class HBCIClientConfig {

    @ConfigProperty(name = "hbci.service.base-url")
    String hbciServiceUrl;

    @Produces
    @ApplicationScoped
    public ApiClient apiClient() {
        ApiClient client = new ApiClient();
        client.setBasePath(hbciServiceUrl);
        // Optional: Set timeouts
        client.setConnectTimeout(30000);
        client.setReadTimeout(60000);
        return client;
    }

    @Produces
    @ApplicationScoped
    public BankAccountsApi bankAccountsApi(ApiClient client) {
        return new BankAccountsApi(client);
    }

    @Produces
    @ApplicationScoped
    public HbciOperationsApi hbciOperationsApi(ApiClient client) {
        return new HbciOperationsApi(client);
    }
}
```

### Step 3: Add Configuration Properties

Add to your `application.properties`:

```properties
# HBCI Service Configuration
hbci.service.base-url=http://localhost:8080
```

Or in `application.yml`:

```yaml
hbci:
  service:
    base-url: http://localhost:8080
```

## Configuration

### Environment-Specific Configuration

Use Quarkus profiles for different environments:

```yaml
# Default (dev)
"%dev":
  hbci:
    service:
      base-url: http://localhost:8080

# Production
"%prod":
  hbci:
    service:
      base-url: ${HBCI_SERVICE_URL:http://hbci-service:8080}

# Test
"%test":
  hbci:
    service:
      base-url: http://localhost:8080
```

### Connection Pool Configuration

For high-traffic applications, configure the HTTP client:

```java
@Produces
@ApplicationScoped
public ApiClient apiClient() {
    ApiClient client = new ApiClient();
    client.setBasePath(hbciServiceUrl);
    
    // Configure connection pool
    OkHttpClient okHttpClient = client.getHttpClient()
        .newBuilder()
        .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
        .retryOnConnectionFailure(true)
        .build();
    
    client.setHttpClient(okHttpClient);
    return client;
}
```

## Usage Examples

### Example 1: Managing Bank Accounts

```java
package com.yourcompany.service;

import it.noxno.hbci.client.api.BankAccountsApi;
import it.noxno.hbci.client.model.BankAccount;
import it.noxno.hbci.client.model.BankAccountDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class BankAccountService {

    @Inject
    BankAccountsApi bankAccountsApi;

    public BankAccount createAccount(String accountNumber, String bankCode, 
                                    String holderName, String hbciUrl) {
        try {
            BankAccountDTO dto = new BankAccountDTO();
            dto.setAccountNumber(accountNumber);
            dto.setBankCode(bankCode);
            dto.setAccountHolderName(holderName);
            dto.setHbciUrl(hbciUrl);
            dto.setHbciVersion("300");
            dto.setActive(true);
            
            return bankAccountsApi.createAccount(dto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bank account", e);
        }
    }

    public List<BankAccount> getAllAccounts() {
        try {
            return bankAccountsApi.listAccounts();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch accounts", e);
        }
    }

    public BankAccount getAccount(Long id) {
        try {
            return bankAccountsApi.getAccount(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch account", e);
        }
    }
}
```

### Example 2: Fetching Balance

```java
package com.yourcompany.service;

import it.noxno.hbci.client.api.HbciOperationsApi;
import it.noxno.hbci.client.model.BalanceDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class BalanceService {

    private static final Logger LOG = Logger.getLogger(BalanceService.class);

    @Inject
    HbciOperationsApi hbciOperationsApi;

    public BalanceDTO fetchBalance(Long accountId) {
        try {
            LOG.infof("Fetching balance for account %d", accountId);
            BalanceDTO balance = hbciOperationsApi.getBalance(accountId);
            LOG.infof("Balance retrieved: %s %s", 
                     balance.getBookedBalance(), balance.getCurrency());
            return balance;
        } catch (Exception e) {
            LOG.errorf(e, "Failed to fetch balance for account %d", accountId);
            throw new RuntimeException("Failed to fetch balance", e);
        }
    }
}
```

### Example 3: Fetching Transactions

```java
package com.yourcompany.service;

import it.noxno.hbci.client.api.HbciOperationsApi;
import it.noxno.hbci.client.model.TransactionDTO;
import it.noxno.hbci.client.model.TransactionRequestDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class TransactionService {

    @Inject
    HbciOperationsApi hbciOperationsApi;

    public List<TransactionDTO> fetchRecentTransactions(Long accountId, int days) {
        try {
            TransactionRequestDTO request = new TransactionRequestDTO();
            request.setAccountId(accountId);
            request.setStartDate(LocalDate.now().minusDays(days));
            request.setEndDate(LocalDate.now());
            
            return hbciOperationsApi.fetchTransactions(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch transactions", e);
        }
    }

    public List<TransactionDTO> fetchTransactionsInRange(Long accountId, 
                                                         LocalDate start, 
                                                         LocalDate end) {
        try {
            TransactionRequestDTO request = new TransactionRequestDTO();
            request.setAccountId(accountId);
            request.setStartDate(start);
            request.setEndDate(end);
            
            return hbciOperationsApi.fetchTransactions(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch transactions", e);
        }
    }
}
```

### Example 4: REST Endpoint Integration

Create a REST endpoint in your application that uses the HBCI client:

```java
package com.yourcompany.resource;

import com.yourcompany.service.BalanceService;
import com.yourcompany.service.TransactionService;
import it.noxno.hbci.client.model.BalanceDTO;
import it.noxno.hbci.client.model.TransactionDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;

@Path("/banking")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BankingResource {

    @Inject
    BalanceService balanceService;

    @Inject
    TransactionService transactionService;

    @GET
    @Path("/balance/{accountId}")
    public Response getBalance(@PathParam("accountId") Long accountId) {
        try {
            BalanceDTO balance = balanceService.fetchBalance(accountId);
            return Response.ok(balance).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(e.getMessage())
                          .build();
        }
    }

    @GET
    @Path("/transactions/{accountId}")
    public Response getTransactions(
            @PathParam("accountId") Long accountId,
            @QueryParam("days") @DefaultValue("30") int days) {
        try {
            List<TransactionDTO> transactions = 
                transactionService.fetchRecentTransactions(accountId, days);
            return Response.ok(transactions).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(e.getMessage())
                          .build();
        }
    }
}
```

## Best Practices

### 1. Error Handling

Always wrap API calls in try-catch blocks and provide meaningful error messages:

```java
try {
    return hbciOperationsApi.getBalance(accountId);
} catch (ApiException e) {
    LOG.errorf("API Error: %d - %s", e.getCode(), e.getResponseBody());
    throw new BusinessException("Failed to fetch balance", e);
} catch (Exception e) {
    LOG.error("Unexpected error", e);
    throw new TechnicalException("System error", e);
}
```

### 2. Caching

Consider caching balance and transaction data to reduce API calls:

```java
@ApplicationScoped
public class CachedBalanceService {

    @Inject
    HbciOperationsApi hbciOperationsApi;

    @CacheResult(cacheName = "balance-cache")
    public BalanceDTO getBalance(@CacheKey Long accountId) {
        return hbciOperationsApi.getBalance(accountId);
    }

    @CacheInvalidate(cacheName = "balance-cache")
    public void invalidateBalance(@CacheKey Long accountId) {
        // Cache will be invalidated
    }
}
```

### 3. Async Operations

For long-running operations, use async patterns:

```java
@ApplicationScoped
public class AsyncTransactionService {

    @Inject
    TransactionService transactionService;

    @Inject
    @Channel("transaction-results")
    Emitter<List<TransactionDTO>> emitter;

    public void fetchTransactionsAsync(Long accountId, int days) {
        CompletableFuture.runAsync(() -> {
            try {
                List<TransactionDTO> transactions = 
                    transactionService.fetchRecentTransactions(accountId, days);
                emitter.send(transactions);
            } catch (Exception e) {
                LOG.error("Async fetch failed", e);
            }
        });
    }
}
```

### 4. Health Checks

Add health checks to monitor the HBCI service:

```java
package com.yourcompany.health;

import it.noxno.hbci.client.ApiClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class HBCIServiceHealthCheck implements HealthCheck {

    @Inject
    ApiClient apiClient;

    @Override
    public HealthCheckResponse call() {
        try {
            // Simple connectivity check
            // You might want to call a lightweight endpoint
            return HealthCheckResponse.up("hbci-service");
        } catch (Exception e) {
            return HealthCheckResponse.down("hbci-service");
        }
    }
}
```

## License Considerations

### ✅ Your Application is NOT Subject to LGPL

The HBCI client library communicates with the HBCI service via REST API only. This means:

1. **No Code Linking**: Your application does not link against hbci4java
2. **Network Boundary**: Communication happens over HTTP/REST
3. **License Isolation**: The LGPL license is contained within the microservice
4. **Your Freedom**: Your application can use any license (proprietary, Apache, MIT, etc.)

### Architecture Diagram

```
┌─────────────────────────────────────┐
│  Your Quarkus Application           │
│  (Any License - Proprietary OK)     │
│  ┌─────────────────────────────┐    │
│  │  hbci-client library        │    │
│  │  (REST client, NO LGPL)     │    │
│  └─────────────────────────────┘    │
└──────────────┬──────────────────────┘
               │ HTTP/REST
               │ (Network Boundary)
               ▼
┌─────────────────────────────────────┐
│  HBCI Wrapper Microservice          │
│  (LGPL - Contains hbci4java)        │
│  ┌─────────────────────────────┐    │
│  │  hbci4java (LGPL)           │    │
│  └─────────────────────────────┘    │
└─────────────────────────────────────┘
```

### What You Need to Know

- ✅ You can use this in proprietary software
- ✅ You don't need to publish your source code
- ✅ You don't need to license your app under LGPL
- ✅ The microservice architecture provides license isolation
- ℹ️ The HBCI service itself remains LGPL (but that's isolated)

## Troubleshooting

### Connection Issues

**Problem**: Cannot connect to HBCI service

**Solution**:
```java
// Add connection logging
ApiClient client = new ApiClient();
client.setDebugging(true);
client.setBasePath(hbciServiceUrl);
```

### Timeout Issues

**Problem**: Requests timing out

**Solution**:
```java
// Increase timeouts
client.setConnectTimeout(60000); // 60 seconds
client.setReadTimeout(120000);   // 2 minutes
```

### SSL Certificate Issues

**Problem**: SSL certificate validation failures

**Solution** (DEV only - NOT for production):
```java
// Disable SSL verification (DEVELOPMENT ONLY!)
OkHttpClient okHttpClient = client.getHttpClient()
    .newBuilder()
    .hostnameVerifier((hostname, session) -> true)
    .build();
```

### API Version Mismatch

**Problem**: API responses don't match expected format

**Solution**: Ensure client and service versions match. Regenerate client:
```bash
mvn clean install -f hbci-client/pom.xml
```

## Support

For additional help:

1. Check the main [README.md](../README.md)
2. Review API documentation at http://localhost:8080/swagger-ui
3. Create an issue on GitHub
4. Contact: noxno.it

## Next Steps

1. ✅ Add the dependency to your project
2. ✅ Configure the client
3. ✅ Implement your first integration
4. ✅ Add error handling and monitoring
5. ✅ Deploy to production

Happy coding! 🚀
