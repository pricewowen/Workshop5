# Workshop 5 — Java Desktop Application

## Overview
This repository contains the **Workshop 5 Java Desktop Application** for a bakery e-commerce system. The application connects to a local MySQL database and provides a desktop-based interface for managing and interacting with project data.

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