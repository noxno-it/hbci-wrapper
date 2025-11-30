# HBCI Wrapper Microservice

A comprehensive microservice for HBCI/FinTS banking operations, built with Quarkus, Java 20, PostgreSQL, and Angular 20.

## Overview

This microservice provides a REST API for German banking operations using the HBCI (Home Banking Computer Interface) / FinTS protocol. It wraps the LGPL-licensed [hbci4java](https://github.com/hbci4j/hbci4java) library while maintaining license isolation for consuming applications.

## Architecture

The project consists of two main modules:

1. **hbci-service**: The Quarkus-based microservice that integrates with hbci4java
2. **hbci-client**: A generated OpenAPI client library for consuming the service

### License Isolation

The LGPL license of hbci4java is contained within the `hbci-service` module. Consuming applications use the `hbci-client` module, which communicates via REST API and is NOT subject to LGPL requirements. This follows the "service boundary" pattern for license isolation.

## Technology Stack

- **Backend**: Quarkus 3.6.4 with Java 20
- **Database**: PostgreSQL 16
- **Banking Protocol**: HBCI4Java 3.2.3 (LGPL)
- **API Documentation**: OpenAPI 3.0 / Swagger UI
- **Frontend**: Angular 20 (planned)
- **Containerization**: Docker & Docker Compose

## Prerequisites

- Java 20 or higher
- Maven 3.9+
- Docker and Docker Compose (for containerized deployment)
- PostgreSQL 16 (if running locally without Docker)

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/noxno-it/hbci-wrapper.git
cd hbci-wrapper
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Start with Docker Compose

```bash
docker-compose up -d
```

The service will be available at:
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui
- OpenAPI Spec: http://localhost:8080/openapi

### 4. Alternative: Run Locally

Start PostgreSQL:
```bash
docker run -d --name hbci-postgres \
  -e POSTGRES_DB=hbci \
  -e POSTGRES_USER=hbci \
  -e POSTGRES_PASSWORD=hbci \
  -p 5432:5432 \
  postgres:16
```

Run the service:
```bash
cd hbci-service
mvn quarkus:dev
```

## API Endpoints

### Bank Account Management

- `GET /api/accounts` - List all bank accounts
- `GET /api/accounts/{id}` - Get specific account
- `POST /api/accounts` - Create new account
- `PUT /api/accounts/{id}` - Update account
- `DELETE /api/accounts/{id}` - Delete account

### HBCI Operations

- `GET /api/hbci/balance/{accountId}` - Fetch current balance
- `POST /api/hbci/transactions/fetch` - Fetch transactions from bank
- `GET /api/hbci/transactions/{accountId}` - Get stored transactions

### Health & Monitoring

- `GET /q/health` - Health check
- `GET /q/health/live` - Liveness probe
- `GET /q/health/ready` - Readiness probe

## Using the Client Library

### Maven Dependency

Add the client library to your Quarkus project:

```xml
<dependency>
    <groupId>it.noxno</groupId>
    <artifactId>hbci-client</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Example Usage

```java
import it.noxno.hbci.client.ApiClient;
import it.noxno.hbci.client.api.BankAccountsApi;
import it.noxno.hbci.client.api.HbciOperationsApi;
import it.noxno.hbci.client.model.*;

public class HBCIClientExample {
    
    public void example() throws Exception {
        // Configure API client
        ApiClient client = new ApiClient();
        client.setBasePath("http://localhost:8080");
        
        // Create API instances
        BankAccountsApi accountsApi = new BankAccountsApi(client);
        HbciOperationsApi hbciApi = new HbciOperationsApi(client);
        
        // Create a bank account
        BankAccountDTO accountDTO = new BankAccountDTO();
        accountDTO.setAccountNumber("1234567890");
        accountDTO.setBankCode("12345678");
        accountDTO.setAccountHolderName("John Doe");
        accountDTO.setHbciUrl("https://banking.example.com/fints");
        accountDTO.setHbciVersion("300");
        
        BankAccount account = accountsApi.createAccount(accountDTO);
        
        // Get balance
        BalanceDTO balance = hbciApi.getBalance(account.getId());
        System.out.println("Balance: " + balance.getBookedBalance());
        
        // Fetch transactions
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setAccountId(account.getId());
        request.setStartDate(LocalDate.now().minusDays(30));
        request.setEndDate(LocalDate.now());
        
        List<TransactionDTO> transactions = hbciApi.fetchTransactions(request);
    }
}
```

## Configuration

### Application Configuration (application.yml)

Key configuration properties:

```yaml
quarkus:
  datasource:
    db-kind: postgresql
    username: hbci
    password: hbci
    jdbc:
      url: jdbc:postgresql://localhost:5432/hbci
  
  http:
    port: 8080
    cors:
      origins: "*"
```

### Environment Variables

- `QUARKUS_DATASOURCE_JDBC_URL` - Database connection URL
- `QUARKUS_DATASOURCE_USERNAME` - Database username
- `QUARKUS_DATASOURCE_PASSWORD` - Database password
- `QUARKUS_HTTP_PORT` - HTTP port (default: 8080)

## Database Schema

The service automatically creates the following tables:

- `bank_accounts` - Configured bank accounts
- `transactions` - Retrieved transaction history

Hibernate will auto-create the schema on first run.

## Development

### Running Tests

```bash
mvn test
```

### Development Mode with Live Reload

```bash
cd hbci-service
mvn quarkus:dev
```

### Generating OpenAPI Client

After making API changes, regenerate the client:

```bash
# First, export the OpenAPI spec from running service
curl http://localhost:8080/openapi > hbci-client/src/main/resources/openapi.yaml

# Then rebuild the client
cd hbci-client
mvn clean install
```

## Deployment

### Building for Production

```bash
mvn clean package -Dquarkus.package.type=uber-jar
```

### Docker Build

```bash
cd hbci-service
mvn package
docker build -f src/main/docker/Dockerfile.jvm -t hbci-service:latest .
```

### Kubernetes Deployment

Kubernetes manifests can be generated with:

```bash
mvn package -Dquarkus.kubernetes.deploy=true
```

## License Compliance

### HBCI Service (LGPL)

The `hbci-service` module uses hbci4java which is licensed under LGPL-2.1. This means:
- The source code must be available
- Modifications to hbci4java must be shared under LGPL
- The hbci-service itself is subject to LGPL

### Client Library (No License Propagation)

The `hbci-client` module is a clean-room implementation that:
- Does NOT link with hbci4java
- Communicates via REST API only
- Is NOT subject to LGPL requirements

Consuming applications using the `hbci-client` are NOT required to be LGPL-licensed. This is because:
1. Communication happens over network (REST API)
2. No code from hbci4java is linked into client applications
3. The service acts as a license boundary

This approach is specifically designed to allow proprietary applications to use HBCI functionality without license concerns.

## Support

For issues, questions, or contributions:
- Create an issue on GitHub
- Contact: noxno.it

## Roadmap

- [ ] Angular 20 frontend UI
- [ ] Additional HBCI operations (transfers, standing orders)
- [ ] Multi-bank support
- [ ] Enhanced security (OAuth2, encryption)
- [ ] Transaction categorization
- [ ] Scheduled transaction fetching
- [ ] Notification system

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## Acknowledgments

- [hbci4java](https://github.com/hbci4j/hbci4java) - The HBCI implementation library
- [Quarkus](https://quarkus.io/) - Supersonic Subatomic Java Framework
- [OpenAPI Generator](https://openapi-generator.tech/) - Client generation tool