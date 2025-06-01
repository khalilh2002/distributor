# Vending Machine API

A modular and testable API for a vending machine, built with Java and Spring Boot, adhering to modern software architecture and object-oriented design principles.

## Table of Contents

1.  [Project Overview](#project-overview)
2.  [Core Architectural Concepts](#core-architectural-concepts)
    *   [Usecases](#usecases)
    *   [Key Classes & Domain Model](#key-classes--domain-model)
    *   [Database Design](#database-design)
3.  [Technologies Used](#technologies-used)
4.  [API Endpoints & Demonstration](#api-endpoints--demonstration)
5.  [Setup and Running the Application](#setup-and-running-the-application)
6.  [Running Tests](#running-tests)
7.  [Assumptions & Design Choices](#assumptions--design-choices)

## 1. Project Overview

This API simulates the operations of a vending machine, allowing users to:
*   Insert valid coins (MAD currency).
*   View a catalog of available products with their prices and purchasability status.
*   Select and deselect one or more products.
*   Dispense selected products if sufficient funds are available.
*   Receive appropriate change.
*   Cancel the transaction and retrieve inserted coins.

The API is designed for clarity, testability, and adherence to good software engineering practices.

## 2. Core Architectural Concepts

The system is built upon a layered architecture (Controller, Service, Repository) to ensure a clear separation of concerns.

### Usecases

The primary interactions (usecases) supported by the API for a standard **User** are:
*   **Insert Coins:** Accepts valid MAD denominations (0.5, 1, 2, 5, 10) and rejects invalid ones.
*   **View Product Catalog:** Displays all products, their names, prices, and whether they can be purchased with the current balance.
*   **Select Product:** Adds a product to the current transaction's selection. Multiple instances of the same product can be selected.
*   **Deselect Product:** Removes one instance of a previously selected product.
*   **Dispense Products:** If funds are sufficient for all selected items, dispenses products and calculates optimized change.
*   **Cancel Transaction:** Refunds all inserted coins and clears the current selection.
*   **View Current State:** Allows checking the current inserted balance and selected items.

An **Administrator** can:
*   **Add New Product:** Introduce new products to the vending machine's catalog.

### Key Classes & Domain Model

The system revolves around a few core classes and data structures:

*   **`Product` (Entity):** Represents an item available in the vending machine.
    *   Attributes: `id` (Long), `name` (String), `price` (BigDecimal).
*   **`Coin` (Enum):** Defines valid coin denominations (e.g., `ONE_MAD`, `FIVE_MAD`) and their values.
*   **`VendingMachineService` (Service):** Contains the core business logic. It manages the transactional state (current balance, inserted coins, selected products) and orchestrates operations like coin insertion, product selection, dispensing, and change calculation. It is an application-scoped singleton, representing a single physical machine.
*   **`VendingMachineController` (Controller):** Exposes the RESTful API endpoints, handles incoming HTTP requests, validates input (using DTOs), and delegates business logic to `VendingMachineService`.
*   **`ProductRepository` (Repository):** An interface (extending Spring Data JPA's `JpaRepository`) responsible for data access operations related to `Product` entities (CRUD).
*   **DTOs (Data Transfer Objects):** Various DTOs (e.g., `ProductDTO`, `CoinInsertRequest`, `SelectionRequest`, `DispenseResponse`, `RefundResponse`) are used to transfer data between the controller and service layers, and for API request/response bodies. This decouples the API from the internal domain model.
*   **Custom Exceptions:** Specific exceptions (e.g., `InvalidCoinException`, `InsufficientFundsException`, `ProductNotFoundException`) are used for clear error handling.
*   **`GlobalExceptionHandler`:** A `@ControllerAdvice` component that intercepts exceptions thrown by controllers or services and formats them into consistent JSON error responses.

### Database Design

The database schema is simple and primarily driven by the `Product` entity.

*   **`PRODUCT` Table:**
    *   `ID` (Primary Key, Auto-Incremented): Unique identifier for the product.
    *   `NAME` (VARCHAR): Name of the product.
    *   `PRICE` (DECIMAL): Price of the product.

This structure is managed by JPA (Hibernate) based on the `Product` entity definition. The H2 database is used for simplicity during development and testing, configured for file-based persistence.

## 3. Technologies Used

*   **Java 17**
*   **Spring Boot 3.x** (e.g., 3.3.0 or as per `pom.xml`)
    *   Spring Web (for REST APIs)
    *   Spring Data JPA (for database interaction)
    *   Spring Validation (for request validation)
*   **Hibernate 6.x** (as the JPA provider)
*   **H2 Database Engine** (File-based persistent mode)
*   **Maven** (Build and dependency management)
*   **Lombok** (To reduce boilerplate code)
*   **JUnit 5 & Mockito** (For unit testing)

## 4. API Endpoints & Demonstration

All endpoints are prefixed with `/api/distributor`.

| Method | Endpoint            | Request Body Example                               | Description                                                                        |
| :----- | :------------------ | :------------------------------------------------- | :--------------------------------------------------------------------------------- |
| POST   | `/coin`             | `{"value": 5.00}`                                  | Inserts a coin. Returns current balance.                                           |
| GET    | `/products`         | _N/A_                                              | Lists all products with name, price, and purchasable status.                       |
| POST   | `/select`           | `{"productId": 1}`                                 | Adds one instance of the specified product to the selection.                       |
| POST   | `/deselect`         | `{"productId": 1}`                                 | Removes one instance of the specified product from the selection.                  |
| POST   | `/dispense`         | _N/A_                                              | Dispenses selected products if funds are sufficient and returns change.            |
| POST   | `/cancel`           | _N/A_                                              | Cancels the transaction, refunds inserted coins.                                   |
| GET    | `/state`            | _N/A_                                              | Shows current balance, selected items (with quantities), and total selected cost. |
| POST   | `/admin/product`    | `{"name": "New Snack", "price": "2.75"}`           | (Admin) Adds a new product to the catalog.                                         |

**Example cURL commands:**

*   **Insert 5 MAD coin:**
    ```bash
    curl -X POST -H "Content-Type: application/json" -d '{"value": "5.00"}' http://localhost:8080/api/distributor/coin
    ```
*   **List products:**
    ```bash
    curl -X GET http://localhost:8080/api/distributor/products
    ```
*   **Select product with ID 1:**
    ```bash
    curl -X POST -H "Content-Type: application/json" -d '{"productId": 1}' http://localhost:8080/api/distributor/select
    ```
*   **View current state:**
    ```bash
    curl -X GET http://localhost:8080/api/distributor/state
    ```
*   **Dispense:**
    ```bash
    curl -X POST http://localhost:8080/api/distributor/dispense
    ```

## 5. Setup and Running the Application

**Prerequisites:**
*   Java JDK 17 or higher
*   Apache Maven 3.6+ (or use the included Maven Wrapper)

**Steps:**

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    cd <repository-name> # Navigate into the backend project directory if in a monorepo
    ```
2.  **Build the project:**
    Using Maven Wrapper (recommended):
    ```bash
    ./mvnw clean package
    ```
    Or, if you have Maven installed globally:
    ```bash
    mvn clean package
    ```
3.  **Run the application:**
    ```bash
    java -jar target/distributor-0.0.1-SNAPSHOT.jar
    ```
    (The JAR filename might vary slightly based on the artifactId and version in `pom.xml`).

The API will typically start on `http://localhost:8080`.
An H2 database file (`vendingmachinedb.mv.db`) will be created in a `data/` subdirectory within the project root upon first run.
The H2 console can be accessed at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/vendingmachinedb`).

## 6. Running Tests

The project includes unit tests for the core service logic.

*   **To run all tests using Maven Wrapper:**
    ```bash
    ./mvnw test
    ```
*   **To run all tests using globally installed Maven:**
    ```bash
    mvn test
    ```
Test results will be displayed in the console. Detailed reports can often be found in the `target/surefire-reports` directory.
Code coverage reports (if JaCoCo is configured) are typically in `target/site/jacoco/index.html`.

## 7. Assumptions & Design Choices

*   **Unlimited Stock:** The vending machine is assumed to have an unlimited stock of products and an unlimited supply of coins for making change.
*   **Optimized Change:** Change is calculated using a greedy algorithm (largest denominations first).
*   **Single Machine State:** The `VendingMachineService` is application-scoped, simulating a single physical machine. Its state (balance, selections) is reset after a dispense or cancellation.
*   **MAD Currency:** All monetary values are assumed to be in Moroccan Dirham (MAD).
*   **H2 File Persistence:** Data (products, and transaction state during operation) is persisted to a local H2 file database for development simplicity.
