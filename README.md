# Workshop 5 — Bakery Management System

## Overview

**Peelin' Good Bakery** is a staff-facing bakery management desktop app built with **Java, JavaFX, and HTTP**. It talks to the **Workshop 7 Spring Boot API** for products, customers, orders, employees, bakeries, rewards, analytics, and messaging. There is **no local MySQL or JDBC** in this project.

**Staff-only:** there is no customer login in this app. Customer records are maintained by staff against the shared API.

## Tech stack

- JDK 23
- JavaFX 25 (Maven BOM)
- `java.net.http` + Jackson for REST calls
- Maven (`javafx-maven-plugin`)

## Prerequisites

- JDK 23
- A **Workshop 7 backend** reachable over HTTP (local `http://localhost:8080` or a deployed host)
- IntelliJ IDEA (recommended) or any Java IDE

## Setup

### 1. Clone and open

Clone the repository and open the folder that contains this `README.md` and `pom.xml` as a **Maven** project. In the course monorepo layout, that path is typically `Workshop-5/Workshop5` (IntelliJ: *File → Open* and select the directory with `pom.xml`).

### 2. API base URL

`ApiClient` resolves the base URL in this order:

1. **`API_URL`** in a `.env.local` file in the **current working directory** (same folder you run the app from), e.g. `API_URL=https://your-host/`
2. JVM or OS environment variable **`API_URL`**
3. Compile-time default: **`http://localhost:8080`**

Trailing slashes are trimmed automatically. No `.env.local` is required if the backend runs locally on port 8080. To use a hosted API instead, set `API_URL` (for example to your Workshop 7 deployment base URL) in `.env.local` or in the environment before starting the app.

### 3. Run the application

- **Maven (recommended):** from `Workshop5`, run `mvnw javafx:run` (Windows: `mvnw.cmd javafx:run`).
- **IDE:** use a run configuration that applies the **module path** for JavaFX (e.g. run the `javafx:run` goal from the Maven tool window), or open the included IntelliJ module metadata so `MainApplication` runs with the correct module settings.

On startup, the app checks that the API responds (`GET /api/v1/tags`). Start Workshop 7 before launching the desktop client.

## Test credentials

Use accounts from your **Workshop 7** seed data (see Workshop 7 README). Examples (adjust if your seed differs):

| Role       | Username   | Password   |
| ---------- | ---------- | ---------- |
| Admin      | `admin`    | `Admin123!` |
| Employee   | `employee2`| `Emp123!`  |

Log in with the **username** (or email if your login screen accepts it) and password validated by the API.

## Roles

- **Admin:** full CRUD, analytics, locations, employees, orders, messaging.
- **Employee:** products, customers, orders, messaging; no employee/locations/analytics admin areas as configured in the UI.

## Troubleshooting

### Login fails / connection errors

- Confirm the Workshop 7 API is running and matches your configured base URL (local vs deployed).
- If using HTTPS against a dev certificate, ensure trust settings match your environment.
- Use the same credentials as in the API database (BCrypt hashes are on the server only).

### Module / JavaFX errors

Use JDK 23 and a run configuration that includes **JavaFX on the module path** (the `javafx:run` goal does this for you). Plain “run main class” without module path often fails with missing JavaFX packages.

## Legacy note

Older MySQL scripts and JDBC DAOs were removed. Schema and data live in **Workshop 7** (Flyway migrations and seed SQL).
