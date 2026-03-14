## Description

A full-stack web application for simulating and analyzing personal investment portfolios.

Users can create a profile, search for real stocks using the Alpha Vantage API, build a portfolio, and save their data to a MySQL database.

The project demonstrates object-oriented design, REST API development, and front-end/back-end integration in the context of financial portfolio management.

## Tech Stack

- **Backend:** Java 17, Spring Boot, Spring JDBC, MySQL
- **Frontend:** React, Vite
- **API:** Alpha Vantage (stock search)

## Features

- Create and save user profiles (name, age, sex, employment status, income range, deposit amount)
- Load existing users from the database
- Search for real stocks via Alpha Vantage API
- Build a portfolio by adding and removing stocks
- Persistent storage with MySQL

## Getting Started

### Prerequisites

- Java 17+
- Maven
- Node.js + npm
- MySQL

### Setup

1. Create the MySQL database:
   ```sql
   CREATE DATABASE portfolio_db;
   ```

2. Update `portfolio-backend/src/main/resources/application.properties` with your MySQL credentials and Alpha Vantage API key.

3. Start the backend:
   ```
   cd portfolio-backend
   mvn spring-boot:run
   ```

4. Start the frontend:
   ```
   cd portfolio-frontend
   npm install
   npm run dev
   ```

5. Open http://localhost:5173

## Project Structure

```
portfolio-backend/
  src/main/java/com/portfolio/
    controller/       # REST endpoints (UserController, StockController)
    entity/           # Data models (User, Portfolio, PortfolioItem)
    repository/       # Database access (UserRepository)
    service/          # Business logic (AlphaVantageService)
  src/main/resources/
    schema.sql        # Database table definitions
    application.properties  # Config (gitignored)

portfolio-frontend/
  src/
    App.jsx           # Main UI (landing page, user form, portfolio builder)
    main.jsx          # React entry point
  vite.config.js      # Dev server + API proxy config
```
