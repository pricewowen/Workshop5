# Workshop 5 — Java Desktop Application

## Overview
This repository contains the **Workshop 5 Java Desktop Application** for a bakery e-commerce system called **Peelin' Good Bakery**. The application connects to a local MySQL database and provides a desktop-based interface for managing and interacting with project data.

## Latest Update: Phase 1 Complete

**Phase 1: Authentication System** has been implemented with:
- Dual-role authentication (Employee/Admin and Customer)
- User registration with validation
- Secure BCrypt password hashing
- Modern role selection and login UI
- Session management
- Comprehensive documentation

## Tech Stack
- Java Development Kit (JDK) 23
- JavaFX 17.0.6 (UI framework)
- MySQL (database)
- XAMPP (local server environment)
- IntelliJ IDEA (development environment)

## Prerequisites
Before running the application, ensure you have the following installed:

- JDK 23
- JavaFX 17.0.6
- XAMPP
- IntelliJ IDEA (recommended)
- Git (for cloning the repository)

## Project Setup
1. Clone the repository.
2. Open the project in IntelliJ IDEA.
3. Configure your database (see **XAMPP Instructions** below).
4. Create your local environment file (`.env.local`) and set your database credentials.
5. Build and run the application.

## XAMPP Instructions
If you already have MySQL running on your machine, press **Windows + R** and type `services.msc`.  
Find **MySQL80** and stop the service.

1. Open XAMPP and start **Apache** and **MySQL**.
2. Once both are running, click **Admin** for MySQL to open phpMyAdmin in your browser.
3. Go to the **Import** tab.
4. Select the `BakeryEcommerceFull` file from the main branch of the GitHub repository.
5. Import the database.

### Database User Setup
Create a MySQL user with the following (recommended for consistency):

- **Username:** `baker`
- **Host:** `Local`
- **Password:** `Password1`

You may use different credentials if preferred, but be sure to update `.env.local` accordingly.

## Environment Configuration (.env.local)
Create a file named `.env.local` in the project root directory and add:

```properties
DB_URL=jdbc:mysql://localhost:3306/bakeryecommerce?useSSL=false&serverTimezone=UTC
DB_USER=baker
DB_PASSWORD=Password1
```

## Quick Start

There are two ways to get started with the application:

### Method 1: Self-Registration (Recommended)

This method requires no SQL scripts or pre-generated users.

1. **Set up the database**
   - Follow the XAMPP Instructions above to create the BakeryEcommerce database
   - Create the `.env.local` file with your database credentials

2. **Run the application**
   - In IntelliJ IDEA, right-click on `MainApplication.java`
   - Select "Run 'MainApplication.main()'"
   - Alternatively, use Maven: `mvn clean javafx:run`

3. **Create your account**
   - Click "Continue as Employee" or "Continue as Customer"
   - Click "Create New Account"
   - Fill in the registration form with your details
   - Click "Create Account"
   - You will be automatically logged in

### Method 2: Pre-Generated Test Users

This method creates test accounts with predefined credentials.

1. **Generate test user SQL**
   - Open `src/main/java/com/sait/workshop05/database/GenerateTestUserSQL.java`
   - Right-click in the editor and select "Run 'GenerateTestUserSQL.main()'"
   - Copy the SQL output from the console

2. **Run the SQL in your database**
   - Open phpMyAdmin or your MySQL client
   - Select the BakeryEcommerce database
   - Paste and execute the SQL commands

3. **Run the application and login**
   - Run `MainApplication.java`
   - Use the test credentials:
     - Admin: username `admin`, password `admin123`
     - Employee: username `employee1`, password `emp123`
     - Customer: username `customer1`, password `cust123`

### Verification

After logging in, you should see:
- Employee/Admin users: Management dashboard with navigation sidebar
- Customer users: Placeholder message (customer interface coming in Phase 3)

All authentication attempts are logged to `Log.txt` in the project root directory.

## Troubleshooting

### Database Connection Failed
- Verify MySQL is running (check XAMPP control panel)
- Ensure `.env.local` file exists in project root with correct credentials
- Check that the BakeryEcommerce database has been created
- Verify the database URL, username, and password are correct

### Invalid Username or Password
- If using Method 1: Ensure you completed registration successfully
- If using Method 2: Verify you ran the generated SQL to insert test users
- Check you selected the correct role (Employee vs Customer) before logging in
- Query the database to verify users exist: `SELECT * FROM User;`

### BCrypt Dependency Not Found
- Run `mvn clean install` to download all dependencies
- Reload the Maven project in IntelliJ IDEA
- Check that `jbcrypt` is listed in `pom.xml` dependencies

### Module Errors
- Ensure `module-info.java` includes `requires jbcrypt;`
- Reload the project structure in IntelliJ IDEA
- Try "Invalidate Caches / Restart" in IntelliJ



