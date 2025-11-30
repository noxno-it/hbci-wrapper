# Microsoft Entra ID (Azure AD) OAuth Integration Guide

This document explains how to configure Microsoft Entra ID (formerly Azure AD) OAuth authentication for the HBCI Wrapper microservice.

## Overview

The HBCI Wrapper service uses OpenID Connect (OIDC) to authenticate users via Microsoft Entra ID. This ensures secure access to banking operations.

## Prerequisites

1. An Azure subscription
2. Admin access to Azure Active Directory
3. The HBCI Wrapper service deployed

## Azure AD Configuration

### Step 1: Register Application in Azure AD

1. Log into the [Azure Portal](https://portal.azure.com)
2. Navigate to **Azure Active Directory**
3. Select **App registrations** > **New registration**
4. Configure the application:
   - **Name**: HBCI Wrapper Service
   - **Supported account types**: Single tenant (most common) or multi-tenant
   - **Redirect URI**: `https://your-domain.com/api/*` (or http://localhost:8080/api/* for dev)
5. Click **Register**

### Step 2: Configure Application

After registration, note the following values (you'll need them for configuration):

#### Application (client) ID
- Found on the **Overview** page
- Example: `12345678-1234-1234-1234-123456789abc`

#### Directory (tenant) ID  
- Found on the **Overview** page
- Example: `87654321-4321-4321-4321-cba987654321`

### Step 3: Create Client Secret

1. Navigate to **Certificates & secrets**
2. Click **New client secret**
3. Add a description (e.g., "HBCI Service Secret")
4. Select an expiration period
5. Click **Add**
6. **Important**: Copy the secret VALUE immediately (it won't be shown again)

### Step 4: Configure API Permissions

1. Navigate to **API permissions**
2. Click **Add a permission**
3. Select **Microsoft Graph**
4. Choose **Delegated permissions**
5. Add the following permissions:
   - `openid`
   - `profile`
   - `email`
   - `User.Read`
6. Click **Add permissions**
7. Click **Grant admin consent** (requires admin rights)

### Step 5: Define App Roles

1. Navigate to **App roles**
2. Click **Create app role**
3. Create two roles:

**User Role:**
- Display name: `User`
- Allowed member types: `Users/Groups`
- Value: `user`
- Description: `Regular user access to HBCI operations`

**Admin Role:**
- Display name: `Admin`
- Allowed member types: `Users/Groups`
- Value: `admin`
- Description: `Administrator access to HBCI operations`

### Step 6: Assign Users to Roles

1. Navigate to **Enterprise applications** in Azure AD
2. Find your application
3. Go to **Users and groups**
4. Click **Add user/group**
5. Select users and assign them to appropriate roles

## Service Configuration

### Environment Variables

Set the following environment variables in your deployment:

```bash
# Azure AD Configuration
AZURE_AD_AUTH_SERVER_URL=https://login.microsoftonline.com/<YOUR-TENANT-ID>/v2.0
AZURE_AD_CLIENT_ID=<YOUR-CLIENT-ID>
AZURE_AD_CLIENT_SECRET=<YOUR-CLIENT-SECRET>
AZURE_AD_ISSUER=https://login.microsoftonline.com/<YOUR-TENANT-ID>/v2.0

# HBCI Encryption Key (32 characters)
HBCI_ENCRYPTION_KEY=<GENERATE-A-32-CHARACTER-KEY>
```

### Kubernetes ConfigMap Example

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: hbci-config
data:
  AZURE_AD_AUTH_SERVER_URL: "https://login.microsoftonline.com/87654321-4321-4321-4321-cba987654321/v2.0"
  AZURE_AD_CLIENT_ID: "12345678-1234-1234-1234-123456789abc"
  AZURE_AD_ISSUER: "https://login.microsoftonline.com/87654321-4321-4321-4321-cba987654321/v2.0"
```

### Kubernetes Secret Example

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: hbci-secrets
type: Opaque
stringData:
  AZURE_AD_CLIENT_SECRET: "your-client-secret-here"
  HBCI_ENCRYPTION_KEY: "your-32-character-encryption-key"
```

### Docker Compose Example

```yaml
services:
  hbci-service:
    image: hbci-service:latest
    environment:
      - AZURE_AD_AUTH_SERVER_URL=https://login.microsoftonline.com/YOUR-TENANT-ID/v2.0
      - AZURE_AD_CLIENT_ID=YOUR-CLIENT-ID
      - AZURE_AD_CLIENT_SECRET=YOUR-CLIENT-SECRET
      - AZURE_AD_ISSUER=https://login.microsoftonline.com/YOUR-TENANT-ID/v2.0
      - HBCI_ENCRYPTION_KEY=your-32-character-key-here!!!!!!
```

## Client Integration

### Obtaining Access Token

Clients must obtain an OAuth2 access token from Microsoft Entra ID before calling the API.

#### Using OAuth2 Authorization Code Flow (Recommended)

```bash
# 1. Redirect user to authorization endpoint
https://login.microsoftonline.com/<TENANT-ID>/oauth2/v2.0/authorize?
  client_id=<CLIENT-ID>
  &response_type=code
  &redirect_uri=<YOUR-REDIRECT-URI>
  &scope=openid%20profile%20email

# 2. Exchange authorization code for token
curl -X POST https://login.microsoftonline.com/<TENANT-ID>/oauth2/v2.0/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=<CLIENT-ID>" \
  -d "client_secret=<CLIENT-SECRET>" \
  -d "code=<AUTHORIZATION-CODE>" \
  -d "redirect_uri=<YOUR-REDIRECT-URI>" \
  -d "grant_type=authorization_code"
```

#### Using the Access Token

```bash
curl -H "Authorization: Bearer <ACCESS-TOKEN>" \
  http://localhost:8080/api/accounts
```

### Java Client Example

```java
import okhttp3.*;
import com.google.gson.Gson;

public class HBCIClient {
    
    private static final String TOKEN_URL = 
        "https://login.microsoftonline.com/<TENANT-ID>/oauth2/v2.0/token";
    
    public String getAccessToken(String clientId, String clientSecret) throws Exception {
        OkHttpClient client = new OkHttpClient();
        
        RequestBody body = new FormBody.Builder()
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .add("scope", "openid profile email")
            .add("grant_type", "client_credentials")
            .build();
        
        Request request = new Request.Builder()
            .url(TOKEN_URL)
            .post(body)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();
            TokenResponse token = new Gson().fromJson(json, TokenResponse.class);
            return token.access_token;
        }
    }
    
    public void callAPI(String accessToken) throws Exception {
        OkHttpClient client = new OkHttpClient();
        
        Request request = new Request.Builder()
            .url("http://localhost:8080/api/accounts")
            .addHeader("Authorization", "Bearer " + accessToken)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.body().string());
        }
    }
    
    static class TokenResponse {
        String access_token;
        String token_type;
        int expires_in;
    }
}
```

## Testing

### Local Development

For local development, you can disable OAuth by setting:

```yaml
"%dev":
  quarkus:
    oidc:
      enabled: false
```

### Integration Tests

The test profile automatically disables OIDC and uses basic authentication:

```yaml
"%test":
  quarkus:
    oidc:
      enabled: false
    security:
      users:
        embedded:
          enabled: true
          users:
            testuser: testpassword
          roles:
            testuser: user,admin
```

## Security Best Practices

1. **Never commit secrets**: Use environment variables or secret management
2. **Rotate secrets regularly**: Change client secrets every 90 days
3. **Use HTTPS**: Always use HTTPS in production
4. **Limit token scope**: Request only required permissions
5. **Validate tokens**: The service automatically validates tokens
6. **Monitor access**: Enable Azure AD audit logs
7. **Implement rate limiting**: Protect against brute force attacks

## Troubleshooting

### "401 Unauthorized" Response

- Check token is included in Authorization header
- Verify token hasn't expired
- Confirm user has required role (user or admin)
- Check client ID and tenant ID are correct

### "Invalid Issuer" Error

- Verify AZURE_AD_ISSUER matches your tenant ID
- Ensure tenant ID doesn't include angle brackets (<>)

### "Token Signature Invalid"

- Check client secret is correct
- Verify token is from correct tenant
- Ensure clocks are synchronized (token validation is time-sensitive)

### Unable to Get Token

- Verify client ID and secret are correct
- Check application is properly registered in Azure AD
- Confirm redirect URI matches exactly
- Ensure API permissions are granted

## References

- [Microsoft Identity Platform Documentation](https://docs.microsoft.com/en-us/azure/active-directory/develop/)
- [Quarkus OIDC Documentation](https://quarkus.io/guides/security-oidc-bearer-token-authentication)
- [OAuth 2.0 Authorization Framework](https://oauth.net/2/)
- [OpenID Connect Specification](https://openid.net/connect/)

## Support

For issues related to:
- Azure AD configuration: Contact your Azure administrator
- HBCI service: Create an issue on GitHub
- Client integration: See ONBOARDING.md
