## Description

A full-stack web application for simulating and analyzing personal investment portfolios.

Users can create a profile, search for real stocks using the Alpha Vantage API, build a portfolio, and view quantitative risk analytics backed by custom data structures.

The project demonstrates object-oriented design, REST API development, data structures (AVL tree, binary heap), and front-end/back-end integration in the context of financial portfolio management.

## Tech Stack

- **Backend:** Java 17, Spring Boot, Spring JDBC, MySQL
- **Frontend:** React, Vite
- **API:** Alpha Vantage (stock data)

## Features

- Create and save user profiles
- Search for real stocks via Alpha Vantage API
- Build a portfolio by adding and removing stocks
- Persistent storage with MySQL
- Portfolio value and allocation breakdown
- Annualised volatility, Sharpe ratio, and 1-day 95% VaR
- Top-k gainers/losers using a custom MinHeap (O(n log k))
- Alphabetical range query on holdings via AVL tree (O(log n + k))

## Data Structures & Algorithms

| Structure | Operations | Complexity |
|-----------|-----------|------------|
| AVLTree | insert, remove, find | O(log n) |
| AVLTree | findRange(from, to) | O(log n + k) |
| AVLTree | getItemsSorted | O(n) |
| MinHeap\<T\> | insert | O(log n) |
| MinHeap\<T\> | extractMin | O(log n) |
| MinHeap\<T\> | peekMin | O(1) |
| Top-k selection | getTopMovers(k) | O(n log k) |

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /users/{id}/portfolio | All holdings, sorted |
| POST | /users/{id}/portfolio | Save holdings |
| DELETE | /users/{id}/portfolio/items/{symbol} | Remove holding |
| GET | /users/{id}/portfolio/value | Market value + allocation |
| GET | /users/{id}/portfolio/analytics | Volatility, Sharpe, VaR |
| GET | /users/{id}/portfolio/items/{symbol} | Single holding (AVL find) |
| GET | /users/{id}/portfolio/range?from=A&to=M | Range query |
| GET | /users/{id}/portfolio/top-movers?k=5 | Top gainers & losers |

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

### Running Tests

```
cd portfolio-backend
mvn test
```

Tests cover:
- `MinHeapTest` — insert/extract ordering, top-k algorithm, reversed comparator, edge cases
- `VolatilityServiceTest` — volatility calculation, Sharpe ratio, VaR, error handling
- `AnalyticsServiceTest` — gainers/losers ranking, k > portfolio size, missing price skipping

