# 💸 Payment Service Application

This Spring Boot application is responsible for managing payments via SEPA or SWIFT. It supports creating, cancelling payments (with fee handling), and retrieving payment details.
Below is the requirements for this task. 

## 📋 Requirements

This application fulfills the following business and technical requirements:

### 1. ✅ Payment Creation

- Users can create two types of payments: **SEPA** and **SWIFT**
- Fields required:
  - `amount`: Positive decimal
  - `currency`: Only `EUR` or `USD`
  - `debtorIban`, `creditorIban`: Must be valid IBANs
  - `details`: Text, up to 200 characters
- Rules:
  - SEPA payments must be in **EUR**
  - SEPA payments must be sent to **Latvian, Lithuanian, or Estonian IBANs**
  - Each payment stores a **creation timestamp**

---

### 2. ❌ Payment Cancellation

- Payments are **not deleted**, only marked as cancelled
- Can only cancel a payment until **00:00 UTC on the day of creation**
- Cancellation fee rules:
  - **Less than 1 hour** since creation: 1% of the payment amount
  - **One hour or later**: 2% of the payment amount + fixed fee of **0.05 EUR**
- Stores a **cancellation timestamp** and **fee amount**

---

### 3. 🔍 Payments Querying

- Query all payments via an API
- Supports filtering by:
  - `amount`
  - `status` (accepted / cancelled)
- Each result includes:
  - All original fields
  - `payment ID`
  - `creation timestamp`
  - `cancellation fee` (if applicable)
  - `cancellation timestamp` (if applicable)

---

### 4. 🌍 Client Country Logging

- Resolves and logs the **API consumer's country** using an external IP geolocation service
- Logging is **non-blocking**: failures do not impact business logic

---

### 5. 🔔 Notifications

- Notifications are triggered for:
  - **Payment creation**
  - **SWIFT payment cancellation**
- Implemented using a **dummy interface** (mocked service, no real integration)

---

### 6. 🧪 Integration Tests

- Includes comprehensive integration tests for:
  - Payment creation
  - Cancellation rules
  - Query filtering
- Validates all business constraints and time-sensitive logic

---

---

## 📌 Features

- Create SEPA/SWIFT payments
- Detect client country and log the information
- Cancel payments and apply corresponding fees
- Retrieve payment details

---

## 🚀 Getting Started

### 🐳 Run with Docker Compose

Make sure [Docker](https://www.docker.com/products/docker-desktop) is installed on your machine.  
Then simply run:

```bash
docker compose up --build
```
## 🧪 Testing the Application

You can test the API using the HTTP scripts provided in the `scripts/` directory:

1. Start with `createPayment.http` to create a new payment  
2. Then run `cancelPayment.http` (use previously created payment id) and `getPayment.http` to test other operations  

Alternatively, check out the integration tests under:
```PaymentControllerIntegrationTest.java```
It covers most of the use cases in the app. for tests I've used in-memory DB.



