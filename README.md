# Workshop 5 — Bakery Management System

## Overview

**Peelin' Good Bakery** is a staff-facing bakery management desktop app built with **Java, JavaFX, and HTTP**. It talks to the **Workshop 7 Spring Boot API** for products, customers, orders, employees, bakeries, rewards, analytics, and messaging. There is **no local MySQL or JDBC** in this project.

**Staff-only:** there is no customer login in this app. Customer records are maintained by staff against the shared API.

## Tech stack

- JDK 23
- JavaFX 17.0.6
- `java.net.http` + Jackson for REST calls
- Maven

## Prerequisites

- JDK 23
- **Workshop 7 backend** running (default `http://localhost:8080`) with database migrated and seeded
- IntelliJ IDEA (recommended) or any Java IDE

## Setup

### 1. Clone and open

Clone the repo and open the `Workshop5` Maven module.

### 2. API base URL

Create **`.env.local`** in the **project root** (`Workshop5/` — same directory as `pom.xml`):

```properties
API_URL=http://localhost:8080
```

Use a different host or port if your API listens elsewhere (no trailing slash required).

### 3. Run the application

- **IDE:** run `com.sait.workshop05.MainApplication`
- **Maven:** `mvn clean javafx:run`

On startup, the app checks that the API responds (`GET /api/v1/tags`). Ensure the backend is up first.

## Test credentials

Use accounts from your **Workshop 7** seed data (see Workshop 7 docs). Examples (adjust if your seed differs):

| Role       | Username   | Password   |
| ---------- | ---------- | ---------- |
| Admin      | `admin`    | `Admin123!` |
| Employee   | `employee2`| `Emp123!`  |

Log in with the **username** (or email if your login screen accepts it) and password validated by the API.

## Roles

- **Admin:** full CRUD, analytics, locations, employees, orders, messaging.
- **Employee:** products, customers, orders, messaging; no employee/locations/analytics admin areas as configured in the UI.

## Troubleshooting

### `API_URL is missing from .env.local`

Create `.env.local` in the project root with `API_URL=...` as above.

### Login fails / connection errors

- Confirm Workshop 7 is running and reachable at `API_URL`.
- Check firewall and port (`8080` by default).
- Use the same credentials as in the API database (BCrypt hashes are on the server only).

### Module / JavaFX errors

Ensure the project uses JDK 23 and that JavaFX run configuration matches `module-info` exports.

## Legacy note

Older MySQL scripts and JDBC DAOs were removed. Schema and data live in **Workshop 7** (Flyway migrations and seed SQL).
