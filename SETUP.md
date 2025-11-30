# HBCI Wrapper - Quick Setup Guide

This guide will help you get the HBCI Wrapper microservice up and running quickly.

## 🚀 Quick Start (Docker)

The fastest way to get started is using Docker Compose:

```bash
# Clone the repository
git clone https://github.com/noxno-it/hbci-wrapper.git
cd hbci-wrapper

# Build the services
mvn clean package -DskipTests

# Start with Docker Compose
docker-compose up -d

# Check if services are running
docker ps

# Access the application
# API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui
# Health: http://localhost:8080/q/health
```

## 📦 Manual Setup

### Prerequisites

- Java 17 or higher
- Maven 3.9+
- PostgreSQL 16 (or Docker)
- Node.js 18+ (for frontend)

### 1. Database Setup

Start PostgreSQL (using Docker):

```bash
docker run -d --name hbci-postgres \
  -e POSTGRES_DB=hbci \
  -e POSTGRES_USER=hbci \
  -e POSTGRES_PASSWORD=hbci \
  -p 5432:5432 \
  postgres:16
```

Or configure your existing PostgreSQL instance and update `hbci-service/src/main/resources/application.yml`.

### 2. Build the Project

```bash
# Build all modules
mvn clean install

# Or build specific modules
mvn clean install -pl hbci-service
mvn clean install -pl hbci-client
```

### 3. Run the Backend

```bash
cd hbci-service
mvn quarkus:dev
```

The service will start on `http://localhost:8080`.

### 4. Run the Frontend (Optional)

```bash
cd hbci-frontend
npm install
npm start
```

The frontend will start on `http://localhost:4200`.

## 🧪 Testing

### Run Backend Tests

```bash
cd hbci-service
mvn test
```

### Run Integration Tests

```bash
cd hbci-service
mvn verify
```

### Manual API Testing

Use the Swagger UI at `http://localhost:8080/swagger-ui` or use curl:

```bash
# List accounts
curl http://localhost:8080/api/accounts

# Create an account
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "1234567890",
    "bankCode": "12345678",
    "accountHolderName": "John Doe",
    "hbciUrl": "https://banking.example.com/fints",
    "hbciVersion": "300",
    "active": true
  }'

# Get balance (replace {id} with actual account ID)
curl http://localhost:8080/api/hbci/balance/{id}
```

## 📝 Using the Client Library

### Add Dependency

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

public class Example {
    public void run() throws Exception {
        ApiClient client = new ApiClient();
        client.setBasePath("http://localhost:8080");
        
        BankAccountsApi api = new BankAccountsApi(client);
        List<BankAccount> accounts = api.listAccounts();
        
        for (BankAccount account : accounts) {
            System.out.println(account.getAccountNumber());
        }
    }
}
```

See [ONBOARDING.md](ONBOARDING.md) for complete integration guide.

## 🛠️ Configuration

### Environment Variables

Common configuration options:

```bash
# Database
QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/hbci
QUARKUS_DATASOURCE_USERNAME=hbci
QUARKUS_DATASOURCE_PASSWORD=hbci

# HTTP
QUARKUS_HTTP_PORT=8080

# Logging
QUARKUS_LOG_LEVEL=INFO
```

### Production Configuration

For production deployment, create `application-prod.properties`:

```properties
quarkus.datasource.jdbc.url=${DB_URL}
quarkus.datasource.username=${DB_USER}
quarkus.datasource.password=${DB_PASSWORD}
quarkus.hibernate-orm.database.generation=validate
quarkus.log.level=INFO
```

## 🐛 Troubleshooting

### Port Already in Use

If port 8080 is already in use, change it:

```bash
mvn quarkus:dev -Dquarkus.http.port=8081
```

### Database Connection Issues

Check PostgreSQL is running:

```bash
docker ps | grep postgres
# or
psql -h localhost -U hbci -d hbci
```

### Build Failures

Clean and rebuild:

```bash
mvn clean
rm -rf ~/.m2/repository/it/noxno
mvn install
```

### OpenAPI Client Issues

If client generation fails, ensure the OpenAPI spec is valid:

```bash
# Export current spec
curl http://localhost:8080/openapi > hbci-client/src/main/resources/openapi.yaml

# Rebuild client
cd hbci-client
mvn clean install
```

## 📚 Next Steps

1. Read the [complete README](README.md) for architecture details
2. Check [ONBOARDING.md](ONBOARDING.md) for client integration
3. Review [LICENSE-COMPLIANCE.md](LICENSE-COMPLIANCE.md) for licensing
4. Explore the [Swagger UI](http://localhost:8080/swagger-ui) for API documentation

## 🆘 Getting Help

- GitHub Issues: https://github.com/noxno-it/hbci-wrapper/issues
- Swagger API Docs: http://localhost:8080/swagger-ui
- Health Check: http://localhost:8080/q/health

## 📄 License

This project uses LGPL-2.1 for the service module. See [LICENSE](LICENSE) and [LICENSE-COMPLIANCE.md](LICENSE-COMPLIANCE.md) for details on how the license is isolated for consuming applications.
