# AI-ChainID: AI-Powered Blockchain-Based Decentralized Identity & Access Management Platform

[![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.1-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Security](https://img.shields.io/badge/Security-JWT_Stateless-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![Swagger](https://img.shields.io/badge/OpenAPI-Swagger_3.0-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](http://localhost:8080/swagger-ui/index.html)

---

## 📌 Executive Summary

**AI-ChainID** is an enterprise-grade, decentralized identity (DID), verifiable credential, AI-driven risk-assessed access control, asset lifecycle tracking, and blockchain-anchored audit platform.

For academic and research institute environments (such as universities, national laboratories, and research hubs), AI-ChainID provides cryptographic identity verification, multi-factor AI risk evaluation for sensitive compute resources (GPUs, research labs, genomic databases), and immutable blockchain audit trail records without exposing sensitive Personally Identifiable Information (PII) on-chain.

---

## 🏛 Core Architecture

```
Organization
    │
    ├── Users
    │     ├── Decentralized Identity (DID: did:chainid:<uuid>)
    │     ├── Verifiable Credentials (SHA-256 Hash Anchored)
    │     ├── Assigned Roles & Permissions (RBAC)
    │     ├── Access Requests (AI Risk Evaluated & Admin Approved)
    │     └── Asset Assignments (Laptops, NFC Badges, GPU Nodes)
    │
    ├── Resources (Research Labs, GPU Clusters, Databases)
    ├── Assets (Hardware & Lab Equipment)
    └── Roles & Permissions
```

### Access Control & Governance State Machine:
```
[User Submits Request]
         │
         ▼
[AI Risk Engine Evaluation] ──► (Score: 0-100, Level: LOW/MED/HIGH/CRITICAL, Recommendation)
         │
         ▼
[Admin / Approver Review]
    ├───► [APPROVED] ──► [Permission ACTIVE] ──► [Anchored on Blockchain] ──► [Audit Log]
    └───► [REJECTED] ──► [Permission DENIED] ──► [Anchored on Blockchain] ──► [Audit Log]
         │
         ▼
[Revocation Lifecycle] ──► [Permission REVOKED] ──► [Anchored on Blockchain] ──► [Audit Log]
```

---

## 🚀 Technology Stack

- **Runtime & Language**: Java 21 / 23
- **Framework**: Spring Boot 3.4.1
- **Security**: Spring Security 6, JJWT (io.jsonwebtoken 0.12.6), BCrypt Password Hashing
- **Persistence / ORM**: Spring Data JPA, Hibernate 6
- **Database**: MySQL 8.0+
- **Validation**: Jakarta Bean Validation (`@Valid`, `@NotNull`, `@NotBlank`, `@Email`, `@Future`)
- **API Documentation**: Springdoc OpenAPI 3.0 UI (Swagger)
- **Tooling**: Maven 3.9+, Lombok, Jackson JavaTimeModule
- **Testing**: JUnit 5, Mockito, Spring Boot Test, Spring Security Test, MockMvc, H2 Database

---

## 🗄 Database Setup & Schema

### Database Configuration:
- **Database Name**: `AIchinId`
- **Host**: `localhost:3306`
- **User**: `root`
- **Password**: `Om_Pawar_214`

### Automatic DDL Migration:
Hibernate `ddl-auto=update` is enabled for development and initial deployment. All tables, foreign keys, unique constraints, and indexes are automatically created on startup.

### Relational Tables (16 Entities):
1. `organizations`: Core institutions and research organizations
2. `users`: Platform users, accounts, and credentials
3. `identities`: Decentralized Identifiers (DID) and public keys
4. `roles`: Role definitions (ADMIN, FACULTY, LAB_MANAGER, RESEARCHER, STUDENT)
5. `permissions`: Granular resource permissions
6. `user_roles`: User-to-Role composite mappings
7. `role_permissions`: Role-to-Permission composite mappings
8. `credentials`: Verifiable credentials with cryptographic SHA-256 integrity hashes
9. `resources`: Physical and digital infrastructure (Labs, GPU Servers, Databases)
10. `access_requests`: Resource access request records with full lifecycle statuses
11. `risk_assessments`: AI risk engine evaluation results (Score, Risk Level, Recommendation, Risk Factors)
12. `approvals`: Administrator decisions, reasoning, and approval timestamps
13. `assets`: Hardware, laptops, NFC smart badges, and equipment
14. `asset_assignments`: Asset assignment and return lifecycle tracking
15. `audit_logs`: System-wide immutable security and audit events
16. `blockchain_records`: On-chain transaction ledger records (Transaction hash, Block number, Event hash)

---

## ⚙ Configuration & Environment Variables

| Variable | Description | Default Value |
|---|---|---|
| `DB_URL` | JDBC MySQL connection URL | `jdbc:mysql://localhost:3306/AIchinId?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `DB_USERNAME` | MySQL database username | `root` |
| `DB_PASSWORD` | MySQL database password | `Om_Pawar_214` |
| `JWT_SECRET` | Base64 HMAC-SHA key for JWT signing | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` |
| `CORS_ORIGINS` | Allowed frontend origins (comma-separated) | `http://localhost:5173,http://localhost:3000,http://localhost:8080` |

---

## 🛠 How to Build & Run Locally

### Prerequisites:
- JDK 21 or JDK 23 installed
- MySQL Server 8.0 running on `localhost:3306`

### Option 1: Run with Maven Wrapper / Command Line
```bash
# Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
mvn spring-boot:run
```

### Option 2: Build Executable JAR and Run
```bash
# Build package with test verification
mvn clean package

# Run the executable fat JAR
java -jar target/aichainid-backend-1.0.0.jar
```

---

## 📖 Swagger / OpenAPI Documentation

Once the backend is running, access the interactive Swagger UI at:
👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

OpenAPI 3.0 Raw Specification:
👉 **[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)**

### Authenticating in Swagger UI:
1. Call `POST /api/auth/login` to obtain an `accessToken`.
2. Click the **Authorize 🔓** button at the top right in Swagger UI.
3. Enter the token in the format: `Bearer <your_token>` or just `<your_token>`.
4. Click **Authorize** to unlock all protected endpoints.

---

## 🔑 Initial Seed Data

On startup, `DataInitializer` automatically boots the database with:

- **Organization**: `VIT Research Institute`
- **Default Administrator**:
  - **Email**: `admin@aichainid.org`
  - **Password**: `Admin@12345`
  - **Role**: `ROLE_ADMIN`
- **Standard Roles**: `ADMIN`, `FACULTY`, `LAB_MANAGER`, `RESEARCHER`, `STUDENT`
- **Standard Permissions**: `LAB_VIEW`, `LAB_ACCESS`, `EQUIPMENT_USE`, `DATABASE_READ`, `DATABASE_WRITE`, `ASSET_VIEW`, `ASSET_ASSIGN`, `ACCESS_APPROVE`, `CREDENTIAL_VERIFY`
- **Pre-configured Resources**: `AI Research Lab`, `GPU Server`, `Research Database`, `Computer Lab`, `3D Printer`
- **Pre-configured Assets**: `LAP-001`, `LAP-002`, `ID-001`, `GPU-001`

---

## 📡 REST API Reference

### 1. Authentication (`/api/auth`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register new user under an organization |
| `POST` | `/api/auth/login` | Public | Login with email/password and receive JWT Bearer token |

### 2. User Management (`/api/users`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/users` | Admin | Create user |
| `GET` | `/api/users` | Authenticated | List all users |
| `GET` | `/api/users/{id}` | Authenticated | Get user by ID |
| `PUT` | `/api/users/{id}` | Authenticated | Update user details |
| `DELETE` | `/api/users/{id}` | Admin | Deactivate user account |
| `GET` | `/api/users/{id}/roles` | Authenticated | Get roles for user |
| `GET` | `/api/users/{id}/credentials` | Authenticated | Get credentials for user |
| `GET` | `/api/users/{id}/assets` | Authenticated | Get assigned assets for user |
| `GET` | `/api/users/{id}/access-requests` | Authenticated | Get access requests submitted by user |

### 3. Organization Management (`/api/organizations`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/organizations` | Admin | Create new organization |
| `GET` | `/api/organizations` | Authenticated | List all organizations |
| `GET` | `/api/organizations/{id}` | Authenticated | Get organization by ID |
| `PUT` | `/api/organizations/{id}` | Admin | Update organization details |
| `DELETE` | `/api/organizations/{id}` | Admin | Deactivate organization |
| `GET` | `/api/organizations/{id}/users` | Authenticated | Get users in organization |
| `GET` | `/api/organizations/{id}/resources` | Authenticated | Get resources in organization |
| `GET` | `/api/organizations/{id}/assets` | Authenticated | Get assets in organization |

### 4. Decentralized Identity (`/api/identities`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/identities` | Authenticated | Generate DID & public key for user |
| `GET` | `/api/identities/{userId}` | Authenticated | Get identity by user ID |
| `GET` | `/api/identities/did/{did}` | Authenticated | Lookup identity by DID URI |
| `POST` | `/api/identities/{id}/verify` | Authenticated | Verify DID cryptographic validity & active status |
| `PUT` | `/api/identities/{id}/status` | Authenticated | Update DID status (`ACTIVE`, `SUSPENDED`, `REVOKED`) |

### 5. Verifiable Credentials (`/api/credentials`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/credentials` | Admin / Faculty | Issue verifiable credential with SHA-256 hash |
| `GET` | `/api/credentials` | Authenticated | List all credentials |
| `GET` | `/api/credentials/{id}` | Authenticated | Get credential details |
| `GET` | `/api/users/{userId}/credentials` | Authenticated | Get all credentials for user |
| `POST` | `/api/credentials/{id}/verify` | Authenticated | Cryptographically verify integrity & validity |
| `POST` | `/api/credentials/{id}/revoke` | Admin / Faculty | Revoke credential & record on blockchain |
| `PUT` | `/api/credentials/{id}` | Admin / Faculty | Update credential metadata |

### 6. Roles & Permissions (`/api/roles`, `/api/permissions`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/roles` | Admin | Create role |
| `GET` | `/api/roles` | Authenticated | List all roles |
| `GET` | `/api/roles/{id}` | Authenticated | Get role by ID |
| `PUT` | `/api/roles/{id}` | Admin | Update role |
| `DELETE` | `/api/roles/{id}` | Admin | Delete role |
| `POST` | `/api/roles/{roleId}/permissions/{permissionId}` | Admin | Assign permission to role |
| `DELETE` | `/api/roles/{roleId}/permissions/{permissionId}` | Admin | Remove permission from role |
| `POST` | `/api/users/{userId}/roles/{roleId}` | Admin | Assign role to user |
| `DELETE` | `/api/users/{userId}/roles/{roleId}` | Admin | Remove role from user |
| `POST` | `/api/permissions` | Admin | Create permission |
| `GET` | `/api/permissions` | Authenticated | List all permissions |

### 7. Resource Management (`/api/resources`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/resources` | Admin / Lab Manager | Register resource |
| `GET` | `/api/resources` | Authenticated | List resources |
| `GET` | `/api/resources/{id}` | Authenticated | Get resource by ID |
| `PUT` | `/api/resources/{id}` | Admin / Lab Manager | Update resource |
| `DELETE` | `/api/resources/{id}` | Admin | Mark resource offline |
| `GET` | `/api/resources/{id}/access-requests` | Authenticated | List all requests for resource |

### 8. Access Requests & AI Risk Engine (`/api/access-requests`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/access-requests` | Authenticated | Submit access request (user extracted from JWT) |
| `GET` | `/api/access-requests` | Authenticated | List all access requests |
| `GET` | `/api/access-requests/{id}` | Authenticated | Get request details with risk & approval info |
| `POST` | `/api/access-requests/{id}/cancel` | Requester | Cancel pending access request |
| `POST` | `/api/access-requests/{id}/evaluate-risk` | Authenticated | Trigger AI risk engine evaluation |
| `POST` | `/api/access-requests/{id}/approve` | Admin / Approver | Approve access request & anchor on blockchain |
| `POST` | `/api/access-requests/{id}/reject` | Admin / Approver | Reject access request & anchor on blockchain |
| `POST` | `/api/access-requests/{id}/revoke` | Admin / Approver | Revoke approved access & anchor on blockchain |

### 9. Asset Management (`/api/assets`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/assets` | Admin / Lab Manager | Register hardware asset |
| `GET` | `/api/assets` | Authenticated | List all assets |
| `GET` | `/api/assets/{id}` | Authenticated | Get asset details |
| `PUT` | `/api/assets/{id}` | Admin / Lab Manager | Update asset |
| `DELETE` | `/api/assets/{id}` | Admin | Retire asset |
| `POST` | `/api/assets/{assetId}/assign` | Admin / Lab Manager | Assign asset to user (stores assignedBy from JWT) |
| `POST` | `/api/assets/{assetId}/return` | Admin / Lab Manager | Return assigned asset and set status to AVAILABLE |
| `GET` | `/api/assets/{assetId}/history` | Authenticated | Get asset assignment history |

### 10. Audit Logs (`/api/audit-logs`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/audit-logs` | Admin / Faculty | List audit logs with pagination & sorting |
| `GET` | `/api/audit-logs/{id}` | Admin / Faculty | Get audit log by ID |
| `GET` | `/api/users/{userId}/audit-logs` | Authenticated | Get audit logs for user |
| `GET` | `/api/access-requests/{id}/audit-logs` | Authenticated | Get audit trail for access request |

### 11. Blockchain Ledger (`/api/blockchain`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/blockchain/records` | Authenticated | List all blockchain transaction records |
| `GET` | `/api/blockchain/records/{id}` | Authenticated | Get blockchain record by ID |
| `GET` | `/api/blockchain/records/tx/{txHash}` | Authenticated | Lookup record by transaction hash |
| `POST` | `/api/blockchain/verify-hash` | Authenticated | Cryptographically verify record hash integrity |

---

## 🧪 Comprehensive Testing Results

```
Results:
Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

- **`EndToEndIntegrationTest`**: Complete 26-step education lab workflow from student registration, login, DID creation, verifiable credential issuance, AI risk evaluation, admin approval, asset assignment, return, and revocation.
- **`AuthControllerTest`**: Registration validation, duplicate email conflict handling, valid JWT issuance, and bad password detection.
- **`IdentityServiceTest`**: DID generation (`did:chainid:...`), public key generation, duplicate prevention, and verification.
- **`CredentialServiceTest`**: Credential hash computation, validity checking, expired status detection, and revocation state anchoring.
- **`RiskEngineTest`**: Multi-factor AI risk evaluation under diverse scenario combinations (Sensitivity level, time of day, credentials, frequency).
- **`AssetServiceTest`**: Asset creation, assignment status constraints, return workflow, and blockchain event creation.
- **`BlockchainServiceTest`**: Deterministic transaction hash generation (`0x...`), block counter, and cryptographic hash verification.
