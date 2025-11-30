# License Compliance Documentation

## Overview

This document explains the licensing structure of the HBCI Wrapper microservice and how it maintains compliance with the LGPL license while allowing consuming applications to use any license.

## License Architecture

### Three-Tier License Model

```
┌─────────────────────────────────────────────────────────┐
│  Tier 1: Consumer Applications                         │
│  License: ANY (Proprietary, Apache, MIT, etc.)         │
│  ✓ No LGPL requirements                                 │
│  ✓ No source code disclosure required                   │
│  ✓ No license propagation                               │
├─────────────────────────────────────────────────────────┤
│  Communication: HTTP/REST API (License Boundary)        │
├─────────────────────────────────────────────────────────┤
│  Tier 2: Client Library (hbci-client)                  │
│  License: Apache 2.0 (or similar permissive)           │
│  ✓ Generated from OpenAPI specification                 │
│  ✓ No hbci4java code or linking                         │
│  ✓ Pure REST client implementation                      │
├─────────────────────────────────────────────────────────┤
│  Communication: HTTP/REST API (License Boundary)        │
├─────────────────────────────────────────────────────────┤
│  Tier 3: HBCI Service (hbci-service)                   │
│  License: LGPL 2.1                                      │
│  ✓ Contains hbci4java dependency                        │
│  ✓ Source code available                                │
│  ✓ LGPL requirements apply here                         │
└─────────────────────────────────────────────────────────┘
```

## Key Licensing Points

### 1. HBCI Service (LGPL)

The `hbci-service` module:
- **License**: LGPL 2.1
- **Reason**: Contains and links with hbci4java (LGPL library)
- **Requirements**:
  - Source code must be available
  - Modifications must be shared under LGPL
  - Users must be able to relink with modified versions

**Compliance Measures**:
- ✓ Source code published on GitHub
- ✓ LGPL license file included
- ✓ All modifications tracked in version control
- ✓ Build instructions provided

### 2. Client Library (hbci-client)

The `hbci-client` module:
- **License**: Apache 2.0 (recommended) or similar permissive license
- **Key Characteristics**:
  - Does NOT link with hbci4java
  - Contains NO code from hbci4java
  - Generated from OpenAPI specification
  - Pure HTTP client implementation
  - Only communicates via REST API

**Why No LGPL Propagation**:
1. **No Linking**: Client never links with LGPL code
2. **Network Boundary**: Communication is via HTTP only
3. **Clean Room**: Generated independently from service spec
4. **No Derivation**: Not derived from hbci4java

### 3. Consumer Applications

Applications using hbci-client:
- **License**: ANY - Your choice!
- **Freedom**:
  - ✓ Can be proprietary/closed source
  - ✓ Can use any license (Apache, MIT, BSD, proprietary, etc.)
  - ✓ No source code disclosure required
  - ✓ No LGPL obligations

## Legal Basis for License Isolation

### LGPL Section 6 Analysis

The LGPL 2.1 Section 6 states:

> "However, linking a 'work that uses the Library' with the Library creates an executable that is a derivative of the Library..."

**Key Point**: Our architecture does NOT link the consumer application with the library. Communication happens over a network protocol (HTTP/REST).

### Precedent: SaaS and Microservices

This pattern is well-established:
1. **SaaS Exception**: LGPL does not propagate across network boundaries
2. **Microservice Architecture**: Each service maintains its own license
3. **API Communication**: REST APIs are license boundaries
4. **Industry Practice**: Common pattern used by major companies

### Supporting Cases

Similar patterns used successfully:
- MongoDB (SSPL) with network protocol
- MySQL with client libraries over TCP
- Qt with commercial/LGPL dual licensing
- Many SaaS companies using LGPL libraries

## Technical Implementation

### Service Boundary

The license boundary is enforced through:

1. **Physical Separation**
   - Service runs in separate process
   - Client runs in consumer's process
   - No shared memory or direct linking

2. **Network Protocol**
   - Communication via HTTP/REST
   - JSON payloads
   - Standard web protocols

3. **API Contract**
   - OpenAPI specification
   - Version-controlled interface
   - No implementation details leaked

### No Code Sharing

Verification that client shares no code with service:

```bash
# Client contains only:
- Generated API client code
- HTTP/REST communication logic
- DTOs from OpenAPI spec
- No hbci4java classes or logic

# Service contains:
- hbci4java integration
- Business logic
- Database persistence
- REST endpoints
```

## Usage Guidelines for Consumers

### ✅ Allowed Usage

You CAN:
- Use the client library in proprietary applications
- Use any license for your application
- Deploy in closed-source environments
- Sell your application commercially
- Keep your source code private
- Modify the client library without sharing changes

### ❌ Not Allowed

You CANNOT:
- Copy code from hbci-service into your application
- Link directly with hbci4java
- Distribute modified hbci-service without sharing source
- Remove license notices from the service

### Best Practices

1. **Use the Client Library**
   - Always use hbci-client for integration
   - Never directly depend on hbci4java
   - Communicate only via REST API

2. **Document Your Usage**
   - Note that you're using the HBCI Wrapper Service
   - Document the API endpoints you consume
   - Keep track of service version compatibility

3. **Service Deployment**
   - Deploy hbci-service separately
   - Treat it as an external service
   - Use standard service discovery/config

## Compliance Checklist

### For Service Operators

- [ ] Source code for hbci-service is available
- [ ] LGPL license file is included
- [ ] Build instructions are documented
- [ ] Service runs independently
- [ ] API is documented (OpenAPI)

### For Service Consumers

- [ ] Using hbci-client library only
- [ ] No direct hbci4java dependency
- [ ] Communication via REST API only
- [ ] Your application license is documented
- [ ] Service URL is configurable

## Questions and Answers

### Q: Can I use this in my proprietary application?
**A**: Yes! The client library and REST API are designed specifically to allow proprietary applications.

### Q: Do I need to share my application's source code?
**A**: No. The LGPL requirements do not extend to applications using the REST API.

### Q: Can I modify the client library?
**A**: Yes, and you don't need to share those modifications (though we'd appreciate contributions!).

### Q: What if I want to modify the service?
**A**: Modifications to hbci-service must be shared under LGPL, per the license terms.

### Q: Can I sell my application?
**A**: Yes, you can sell applications that use the HBCI Wrapper service.

### Q: Do I need to mention LGPL in my application?
**A**: You don't need to, but it's good practice to document your dependencies.

### Q: Can the license terms change in the future?
**A**: The service will remain LGPL as it uses hbci4java. The client library license is independent.

## Additional Resources

- [LGPL 2.1 Full Text](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html)
- [LGPL FAQ](https://www.gnu.org/licenses/gpl-faq.html)
- [Microservice Architecture and Licensing](https://opensource.stackexchange.com/questions/4447)
- [SaaS and LGPL](https://www.fsf.org/blogs/rms/lgpl-for-java)

## Contact

For licensing questions:
- GitHub Issues: [github.com/noxno-it/hbci-wrapper/issues](https://github.com/noxno-it/hbci-wrapper/issues)
- Email: noxno.it

## Disclaimer

This document provides information about the licensing structure of this project. It is not legal advice. For legal questions about your specific use case, consult with a qualified attorney.

---

**Last Updated**: 2025-11-30
**Document Version**: 1.0
