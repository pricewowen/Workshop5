# Workshop 5 — Bakery Management System

## Overview
**Peelin' Good Bakery** is a staff-facing bakery business management desktop application built with Java, JavaFX, and MySQL. It allows admins and employees to manage products, customers, orders, employees, locations, rewards, messaging, and view analytics — all from a single dashboard.

**This is a staff-only application.** There is no customer login. Customers are data records managed by staff through the app.

## Tech Stack
- Java Development Kit (JDK) 23
- JavaFX 17.0.6 (UI framework)
- MySQL (database)
- XAMPP (local server environment)
- BCrypt (password hashing)
- Maven (build tool)

## Prerequisites
- JDK 23
- JavaFX 17.0.6
- XAMPP (or any MySQL server)
- IntelliJ IDEA (recommended) or any Java IDE
- Git

## Project Setup

### 1. Clone and Open
1. Clone the repository
2. Open the project in your IDE

### 2. Database Setup
1. Open XAMPP and start **Apache** and **MySQL**
   - If MySQL 80 is already running: press **Win + R**, type `services.msc`, stop **MySQL80** first
2. Click **Admin** for MySQL to open phpMyAdmin
3. Go to the **Import** tab
4. Select `BakeryEcommerceFull.sql` from the project root
5. Import the database

### 3. Database User Setup
Create a MySQL user in phpMyAdmin:
- **Username:** `baker`
- **Host:** `Local`
- **Password:** `Password1`

Grant all privileges on the `BakeryEcommerce` database.

### 4. Environment Configuration
Create a file named `.env.local` in the project root:

```properties
DB_URL=jdbc:mysql://localhost:3306/bakeryecommerce?useSSL=false&serverTimezone=UTC
DB_USER=baker
DB_PASSWORD=Password1
```

### 5. Generate Test Staff Accounts
The database needs staff user accounts with BCrypt-hashed passwords to log in.

1. Open `src/main/java/com/sait/workshop05/database/GenerateTestUserSQL.java`
2. Run it (right-click -> Run 'GenerateTestUserSQL.main()')
3. Copy the SQL output from the console
4. Paste and execute it in phpMyAdmin (select the `BakeryEcommerce` database first)

### 6. Run the Application
- In IntelliJ: right-click `MainApplication.java` -> Run
- Or via Maven: `mvn clean javafx:run`

---

## Test Credentials

| Role | Username | Password |
|------|----------|----------|
| **Admin** | `admin` | `admin123` |
| **Employee** | `employee1` | `emp123` |
| **Employee** | `manager` | `manager123` |

> **Note:** You must run `GenerateTestUserSQL.java` and execute the output SQL before these credentials will work. Each run generates different BCrypt hashes (this is normal).

---

## User Roles

### Admin
- Full access to all features
- CRUD employees, products, customers, locations, rewards
- View analytics and charts
- Manage orders
- Internal messaging

### Employee
- CRUD products and customers
- Manage customer loyalty points
- View customer order history
- Manage orders (POS-style new orders, update status)
- Internal messaging
- **Cannot access:** Employee management, Locations, Analytics

---

## Application Flow

```
Login Screen (Role dropdown: Admin / Employee)
    |
    v
Main Dashboard (sidebar navigation)
    |-- Dashboard        (overview + recent orders)
    |-- Orders           (POS-style order management)
    |-- Products         (CRUD)
    |-- Customers        (CRUD + order history + loyalty points)
    |-- Employees        (CRUD - Admin only)
    |-- Locations        (CRUD bakery branches - Admin only)
    |-- Rewards          (CRUD loyalty tiers)
    |-- Messages         (internal staff chat)
    |-- Analytics        (sales charts - Admin only)
    |-- Activity Log     (all DB changes + exceptions)
    |-- Logout
```

---

## Troubleshooting

### Database Connection Failed
- Verify MySQL is running (check XAMPP control panel)
- Ensure `.env.local` exists in the project root with correct credentials
- Check that the `BakeryEcommerce` database has been imported
- Verify the database URL, username, and password

### Invalid Username or Password
- Verify you ran `GenerateTestUserSQL.java` and executed the output SQL
- Make sure you selected the correct role in the dropdown (Admin vs Employee)
- Query the database to verify users exist: `SELECT * FROM User;`

### BCrypt Dependency Not Found
- Run `mvn clean install` to download dependencies
- Reload the Maven project in your IDE
- Check that `jbcrypt` is in `pom.xml`

### Module Errors
- Ensure `module-info.java` includes `requires jbcrypt;`
- Try "Invalidate Caches / Restart" in IntelliJ

---

## Activity Logging
All database changes and caught exceptions are logged to `Log.txt` in the project root. Each entry is a single line with a timestamp, as required by the project proposal.
