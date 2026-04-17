# nexus-user-service

Handles user registration, authentication, and Keycloak identity provisioning for the Nexus Banking platform. Sits
behind the `api-gateway` and communicates with downstream services via Kafka events using the transactional outbox
pattern.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Endpoints](#endpoints)
- [Kafka Events](#kafka-events)
- [Configuration](#configuration)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Testing](#testing)

---

## Overview

`nexus-user-service` is responsible for:

- Registering new users in the Nexus database and provisioning their Keycloak identity simultaneously
- Exposing a login proxy endpoint that exchanges credentials for a Keycloak JWT
- Publishing `user.registered` events via the transactional outbox pattern so downstream services (e.g.
  `account-service`) can react to new registrations

---

## Architecture

```
nexus-web-portal
      │
      ▼
api-gateway  (routes /api/v1/auth/** publicly, /api/v1/users/** requires JWT)
      │
      ▼
user-service
      ├── UserRegistrationOrchestrator
      │       ├── KeycloakUserProvisioningAdapter  →  Keycloak Admin REST API
      │       ├── UserService                      →  PostgreSQL (nexus_user_db)
      │       └── OutboxService                    →  outbox_events table
      │
      └── OutboxProcessor (scheduled)
              └── KafkaUserEventPublisher          →  Kafka topic: user.events
```

**Compensating transactions:** If the database save fails after Keycloak provisioning succeeds,
`UserRegistrationOrchestrator` calls `KeycloakUserProvisioningAdapter.deleteUser()` to roll back the Keycloak user and
maintain consistency.

---

## Endpoints

All endpoints are routed through `api-gateway` on port `8080`.

### Public (no JWT required)

| Method | Path                    | Description                             |
|--------|-------------------------|-----------------------------------------|
| `POST` | `/api/v1/auth/register` | Register a new user                     |
| `POST` | `/api/v1/auth/login`    | Exchange credentials for a Keycloak JWT |

#### Register — Request Body

```json
{
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "password123"
}
```

#### Register — Response `201 Created`

```json
{
  "id": "ca13375d-70b5-4933-9bbf-27b82fb2dbd0",
  "email": "user@example.com"
}
```

#### Login — Request Body

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

#### Login — Response `200 OK`

```json
{
  "access_token": "eyJhbGci...",
  "refresh_token": "eyJhbGci...",
  "expires_in": 300,
  "token_type": "Bearer"
}
```

### Authenticated (JWT required)

| Method | Path                     | Description                                |
|--------|--------------------------|--------------------------------------------|
| `POST` | `/api/v1/users/register` | Internal user registration (authenticated) |

---

## Kafka Events

`user-service` publishes to the `user.events` topic using the transactional outbox pattern. Events are saved atomically
with the user record and processed by the scheduled `OutboxProcessor`.

### `user.registered`

Published when a new user is successfully registered.

```json
{
  "userId": "ca13375d-70b5-4933-9bbf-27b82fb2dbd0"
}
```

**Consumers:** `account-service` listens on this topic to automatically create a bank account for each new user.

---

## Configuration

`user-service` runs on port `9090` by default.

Key `application.yaml` settings:

```yaml
server:
  port: 9090

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/nexus_user_db

  kafka:
    bootstrap-servers: localhost:9092

app:
  kafka:
    topic: user.events

keycloak:
  admin:
    base-url: ${KEYCLOAK_BASE_URL:http://localhost:8180}
    realm: ${KEYCLOAK_REALM:nexus}
    client-id: ${KEYCLOAK_ADMIN_CLIENT_ID:nexus-user-service}
    client-secret: ${KEYCLOAK_ADMIN_CLIENT_SECRET}
```

---

## Getting Started

### Prerequisites

- Java 21
- Maven
- PostgreSQL (`nexus_user_db` database)
- Kafka (via `nexus-infra` Docker Compose)
- Keycloak (via `nexus-infra` Docker Compose)

### Run

```bash
./mvnw spring-boot:run
```

### Infrastructure

Start the required infrastructure from the `nexus-infra` repo:

```bash
docker compose up -d
```

This starts Keycloak (`localhost:8180`), Kafka (`localhost:9092`), Redis, and Mailpit (`localhost:8025` for local
email).

---

## Environment Variables

Create a `.env` file in the project root:

```env
KEYCLOAK_ADMIN_CLIENT_SECRET=<nexus-user-service client secret from Keycloak>
```

The `nexus-user-service` Keycloak client requires the `manage-users` role under `realm-management` to provision new
users via the Admin REST API. Configure this in:

**Keycloak Admin Console → Realm: nexus → Clients → nexus-user-service → Service accounts roles → realm-management →
manage-users**

---

## Email Verification (Local Development)

`user-service` triggers a Keycloak verification email after provisioning each new user via
`KeycloakUserProvisioningAdapter.sendVerificationEmail()`. In local development, emails are caught by **Mailpit**
instead of being sent to a real inbox.

### Accessing Mailpit

```
http://localhost:8025
```

### Flow

1. Register a new user via `POST /api/v1/auth/register`
2. Open `http://localhost:8025` — the verification email from Keycloak will appear in the inbox
3. Click the verification link inside the email
4. The user's email is now verified in Keycloak and they can log in

### Configuration

Mailpit's SMTP server runs on `localhost:1025`. Keycloak is configured to use it via the Admin API with these settings:

```
Host:     nexus-mailpit
Port:     1025
From:     noreply@nexus-banking.com
StartTLS: false
Auth:     false
```

> **Note:** Keycloak and Mailpit must be on the same Docker network (`nexus-network`) for the container hostname
`nexus-mailpit` to resolve. Both are configured in `nexus-infra/compose.yaml`.

### Production

Swap Keycloak's SMTP config to a real provider (e.g. Resend, Brevo) — no code changes required since Keycloak owns the
email sending flow entirely.

---

## Testing

```bash
./mvnw test
```

Tests use Mockito for unit tests across `UserService`, `UserRegistrationOrchestrator`, and `UserController`. H2 is not
used — tests mock the repository layer directly.

---

## Related Services

| Service            | Role                                                                         |
|--------------------|------------------------------------------------------------------------------|
| `api-gateway`      | Routes and secures all inbound requests                                      |
| `account-service`  | Consumes `user.registered` events to auto-create bank accounts               |
| `nexus-infra`      | Provides Keycloak, Kafka, Redis, and Mailpit                                 |
| `nexus-web-portal` | Angular frontend that calls `/api/v1/auth/register` and `/api/v1/auth/login` |

---