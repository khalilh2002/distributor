# Vending Machine API

A modular and testable API for a vending machine, built with **Java and Spring Boot and H2 database**, following modern software architecture and object-oriented design principles. The project also includes a **React TypeScript frontend** for demonstration.

![French UI](./screenshots/ui_fr.png "Webapp - French Interface")
![English UI](./screenshots/ui_en.png "Webapp - English Interface")

## Table of Contents

1.  [Project Overview](#project-overview)
2.  [Core Architectural Concepts](#core-architectural-concepts)
    *   [Usecases](#usecases)
    *   [Key Classes & Domain Model](#key-classes--domain-model)
    *   [Database Design](#database-design)
3.  [Technologies Used](#technologies-used)
4.  [API Endpoints & Demonstration](#api-endpoints--demonstration)
5.  [Setup and Running the Application](#setup-and-running-the-application)
    *   [Backend Spring Boot API](#backend-spring-boot-api)
    *   [Frontend React UI](#frontend-react-ui)
6.  [Running Tests Backend](#running-tests-backend)
7.  [Assumptions & Design Choices](#assumptions--design-choices)

## 1. Project Overview

This project implements a vending machine system with a RESTful API backend and a React frontend. It allows users to perform standard vending machine operations such as inserting coins, selecting products, and receiving items with change.

## 2. Core Architectural Concepts

The system follows a layered architecture for clear separation of concerns.

### Usecases

The primary user interactions with the vending machine are depicted below:

![Usecase Diagram](./screenshots/usecase.png "Usecase Diagram")

Key usecases include inserting coins, viewing and selecting/deselecting products, dispensing items, and canceling transactions. Administrator functions include adding new products.

### Key Classes & Domain Model

The core components of the backend system and their relationships are illustrated in the class diagram:

![Class Diagram](./screenshots/class.png "Class Diagram")

*   **`Product` (Entity):** Represents items in the machine (ID, name, price).
*   **`Coin` (Enum):** Defines valid MAD coin denominations.
*   **`VendingMachineService` (Service):** Manages business logic and transactional state (balance, selected items).
*   **`VendingMachineController` (Controller):** Exposes REST API endpoints and delegates to the service.
*   **`ProductRepository` (Repository):** Handles data access for `Product` entities via Spring Data JPA.
*   **DTOs:** Used for API request/response bodies and data transfer (e.g., `ProductDTO`, `SelectionRequest`).
*   **Custom Exceptions & `GlobalExceptionHandler`:** Provide structured error handling and JSON error responses.

### Database Design

The database schema is straightforward, centered around the `Product` entity:

![Database Diagram](./screenshots/db.png "Database Schema")

*   **`PRODUCT` Table:** Stores product details (ID, name, price). Managed by JPA/Hibernate.

## 3. Technologies Used

**Backend (Spring Boot API):**
*   Java 17
*   Spring Boot 3.x (as per `pom.xml`)
*   Spring Web, Spring Data JPA, Spring Validation
*   Hibernate 6.x
*   H2 Database Engine (File-based persistent mode)
*   Maven
*   Lombok
*   JUnit 5 & Mockito

**Frontend (React UI):**
*   React
*   TypeScript
*   Axios (for API calls)
*   Bootstrap 5 (for styling)
*   i18next (for internationalization - English & French)

## 4. API Endpoints & Demonstration

All backend API endpoints are prefixed with `/api/distributor`. The frontend UI interacts with these endpoints.

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

**Example cURL (Backend API Testing):**
*   Insert 5 MAD coin:
    ```bash
    curl -X POST -H "Content-Type: application/json" -d '{"value": "5.00"}' http://localhost:8080/api/distributor/coin
    ```
*   List products:
    ```bash
    curl -X GET http://localhost:8080/api/distributor/products
    ```

## 5. Setup and Running the Application

**Prerequisites:**
*   Java JDK 17 or higher
*   Apache Maven 3.6+ (or use the included Maven Wrapper for the backend)
*   Node.js and npm (or yarn) for the frontend

### Backend (Spring Boot API)

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/khalilh2002/distributor
    cd distributor/backend  
    ```
2.  **Build the project:**
    ```bash
    ./mvnw clean package
    ```
3.  **Run the application:**
    ```bash
    java -jar target/distributor-0.0.1-SNAPSHOT.jar
    ```
    The API will start on `http://localhost:8080`. An H2 database file will be created in `backend/data/`.
    Access H2 console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/vendingmachinedb`).

### Frontend (React UI)

1.  **Navigate to the frontend directory:**
    ```bash
    cd <repository-name>/frontend # Or your UI project directory name
    ```
2.  **Install dependencies:**
    ```bash
    npm install
   
    ```
3.  **Start the development server:**
    ```bash
    npm start
  
    ```
    The React application will typically open on `http://localhost:3000` and connect to the backend API.

## 6. Running Tests (Backend)

The backend project includes unit tests for the core service logic.

*   Navigate to the backend project directory (`<repository-name>/backend`).
*   Run tests using Maven Wrapper:
    ```bash
    ./mvnw test
    ```
Test results are displayed in the console. Reports are in `target/surefire-reports`.
Code coverage (if JaCoCo is configured): `target/site/jacoco/index.html`.

## 7. Assumptions & Design Choices

*   **Unlimited Stock:** Products and change are assumed to be unlimited.
*   **Optimized Change:** A greedy algorithm is used for change.
*   **Single Machine State:** The backend service simulates a single machine state.
*   **MAD Currency:** All monetary values are in Moroccan Dirham.
*   **H2 File Persistence:** The backend uses a local H2 file database for data persistence.
*   **CORS:** Configured to allow requests from `http://localhost:3000` (the default React dev server).
