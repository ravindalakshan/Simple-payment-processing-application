# 💸 Payment Service Application

This Spring Boot application is responsible for managing payments via SEPA or SWIFT. It supports creating, cancelling payments (with fee handling), and retrieving payment details.
Below is the requirements for this task. 

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



