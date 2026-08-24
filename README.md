# BankFlow — Online Banking Web Application

A secure, full-stack online banking web application built with **Java 21, Spring Boot, Spring Security, JWT, Spring Data JPA (Hibernate), MySQL** and a hand-crafted **HTML/CSS/JavaScript** frontend.

BankFlow lets customers register, log in, manage their bank accounts, deposit, withdraw and transfer money, view transaction history and details, update their profile and change their password — while admins get a dedicated console to monitor customers, accounts, transactions and audit logs. The whole application is protected by JWT authentication, role-based authorization and strict ownership checks.

---

## Key Features

| Area | Features |
| --- | --- |
| **Authentication** | User registration, login, JWT token issuance |
| **Security** | BCrypt password hashing, stateless JWT sessions, role-based access control (`ROLE_CUSTOMER` / `ROLE_ADMIN`) |
| **Customer** | Dashboard, profile view/update, change password |
| **Banking** | Create account, account balances, **deposit**, **withdraw**, **fund transfer** |
| **Transactions** | Transaction history per account, transaction detail view |
| **Admin** | Dashboard statistics, customer management, account monitoring, transaction monitoring, audit logs |
| **Hardening** | Ownership validation, 401 for unauthenticated requests, 403 for forbidden access, secrets never returned by APIs |

---

## Technology Stack

| Layer | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Security | Spring Security, JSON Web Tokens (JJWT 0.12.6), BCrypt |
| Persistence | Spring Data JPA, Hibernate, MySQL 8 |
| Validation | Bean Validation (Jakarta Validation) |
| Build | Maven |
| Frontend | HTML5, CSS3, Vanilla JavaScript (no framework) |
| Version control | Git / GitHub |

---

## Architecture

The backend follows a classic **layered architecture**:

```
Controller → Service → Repository → Database
```

- **Controller layer** — thin REST endpoints that handle HTTP concerns, validation and response mapping. No business logic, no direct repository access.
- **Service layer** — contains all business rules, transactional logic (`@Transactional`), ownership checks and audit logging.
- **Repository layer** — Spring Data JPA interfaces providing database access and custom queries.
- **Database** — MySQL relational schema for customers, accounts, transactions and audit logs.

Supporting patterns used throughout:

- **DTOs** — entities are never serialised directly; dedicated request/response DTOs keep passwords, hashes and internal fields out of the API responses.
- **Global exception handling** — a `@RestControllerAdvice` converts exceptions into a consistent JSON error body with safe, user-friendly messages.
- **Security filter chain** — the `JwtAuthenticationFilter` validates the `Authorization: Bearer <token>` header on every request, loads the user details and populates the `SecurityContext`; `SecurityConfig` enforces public vs authenticated vs admin-only routes with **stateless** sessions.
- **Role-based authorization** — `/api/admin/**` is restricted to `ROLE_ADMIN`; all other `/api/**` endpoints require a valid JWT (a customer account).
## Security

- **JWT authentication** — on login the server returns a signed JWT; every subsequent authenticated call must present it in `Authorization: Bearer <token>`.
- **BCrypt password hashing** — passwords are never stored or returned in plain text; the `password` field is write-only at the entity level.
- **Role-based access control** — `ROLE_CUSTOMER` and `ROLE_ADMIN` roles with route-level enforcement.
- **Ownership validation** — a customer can only view/operate on their own accounts, transactions and profile. Requests that reference another customer's data using a valid token return **403**.
- **Error semantics** — unauthenticated requests → **401**; authenticated-but-forbidden requests → **403**; not found → **404**; validation/business-rule failures → **400**; optimistic conflicts → **409**.
- **Secret hygiene** — passwords, BCrypt hashes, the JWT secret and database credentials are never serialised into any API response.
- **Admin endpoints protected** — the admin API surface cannot be accessed by customers.

> **Note:** sensitive values (database password, JWT secret) live in `src/main/resources/application.properties` only for **local development** and must be overridden via environment variables / an untracked profile for any real deployment.

---

## API Overview

All endpoints are JSON, served under `/api`. Authenticated endpoints expect `Authorization: Bearer <token>`.

### Public

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/auth/register` | Register a new customer |
| POST | `/api/auth/login` | Authenticate and receive a JWT |

### Customer

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/dashboard` | Dashboard summary (identity, accounts, balances, recent transactions) |
| GET | `/api/customers/me` | Current customer profile |
| PUT | `/api/customers/me` | Update current customer profile |
| PUT | `/api/customers/me/password` | Change current customer's password |
| GET | `/api/customers/{customerId}` | View a specific customer (admins only for other users) |

### Accounts

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/accounts` | Create an account |
| GET | `/api/accounts/{accountNumber}` | Account details |
| GET | `/api/accounts/{accountNumber}/balance` | Account balance |
| POST | `/api/accounts/deposit` | Deposit money |
| POST | `/api/accounts/withdraw` | Withdraw money |
| POST | `/api/accounts/transfer` | Transfer money (query-parameter variant) |

### Transfers & Transactions

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/transfers` | Fund transfer (JSON body) |
| GET | `/api/transactions/{accountNumber}` | Transaction history for the authenticated owner's account |

### Admin (`ROLE_ADMIN` only)

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/admin/stats` | Platform statistics |
| GET | `/api/admin/customers` | List/search customers |
| GET | `/api/admin/customers/{id}` | Customer detail |
| PUT | `/api/admin/customers/{id}/status` | Activate / suspend a customer |
| GET | `/api/admin/accounts` | List/filter accounts |
| GET | `/api/admin/accounts/{accountNumber}` | Account detail |
| GET | `/api/admin/accounts/{accountNumber}/transactions` | Account transaction history |
| PUT | `/api/admin/accounts/{id}/status` | Activate / deactivate an account |
| GET | `/api/admin/audit-logs` | Audit log entries |
---

## How to Run Locally

### Prerequisites

- **Java 21+**
- **Maven 3.9+**
- **MySQL 8+** running locally

### 1. Create the database

```sql
CREATE DATABASE online_banking;
```

> The application uses `spring.jpa.hibernate.ddl-auto=update` so the schema is created/updated automatically on startup.

### 2. Configure the connection

Open `src/main/resources/application.properties` and adjust to your environment (or override via environment variables):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/online_banking
spring.datasource.username=<YOUR_DB_USER>
spring.datasource.password=<YOUR_DB_PASSWORD>
```

### 3. Build

```bash
mvn clean compile
```

### 4. Run

```bash
mvn spring-boot:run
```

The application starts on **http://localhost:8080**.

---

## Application URLs

| Page | URL |
| --- | --- |
| Login | `http://localhost:8080/login.html` |
| Register | `http://localhost:8080/register.html` |
| Customer dashboard | `http://localhost:8080/dashboard.html` |
| Admin console | `http://localhost:8080/admin.html` |

---

## Testing

The following flows were manually verified against a running instance backed by MySQL:

- **Authentication** — register, login (valid + invalid credentials), JWT issuance, unauthenticated → 401, invalid token → 401.
- **Customer operations** — profile GET/UPDATE, change password (wrong/valid), deposit, withdraw, transfer, transaction history.
- **Transfer edge cases** — zero/negative amounts, same-account transfer, unknown receiver, insufficient funds, foreign source account, missing JWT.
- **Ownership / authorization** — customer accessing another customer's profile/account/transactions → 403; customer accessing admin endpoints → 403; admin endpoints → 200 for admins.
- **Frontend pages** — `login.html`, `register.html`, `dashboard.html`, `admin.html` served with HTTP 200 and valid JavaScript syntax.
- **Secret hygiene** — password, BCrypt hash, JWT secret and database credentials never appear in API responses.

> These verifications were performed during development on a local test database with clearly identifiable test users. No production data was modified.

---

## Future Improvements

- Automated unit and integration test coverage (JUnit, Spring Boot Test).
- Dockerisation (Dockerfile + `docker-compose` for MySQL).
- Production-ready database configuration (external secrets, Flyway migrations).
- CI/CD pipeline (build, test, deploy).
- Stronger observability (structured logging, metrics, tracing).
- Pagination for transaction history on the customer side.
- Cloud deployment (e.g., AWS/Azure/GCP managed MySQL + container runtime).

> These are optional future directions and are **not** yet implemented in the project.

---

## License

This project was built as a portfolio/educational banking application.
