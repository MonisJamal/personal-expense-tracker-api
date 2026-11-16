# Personal Expense Tracker REST API

A secure, production-grade financial tracking REST API built with **Spring Boot 3**, **Spring Security 6**, **JSON Web Tokens (JWT)**, **Spring Data JPA**, and **MySQL/H2**. Engineered with strict user-level data isolation, stateless authentication, declarative validation, and analytical aggregation queries for monthly spending breakdowns.

---

## Tech Stack & Architecture

- **Language:** Java 21 (LTS)
- **Framework:** Spring Boot 3.3.5
- **Security:** Spring Security 6, BCrypt Password Hashing, JJWT (io.jsonwebtoken 0.12.6)
- **Persistence:** Spring Data JPA, Hibernate ORM
- **Database:** MySQL 8+ (Production) / H2 In-Memory (Dev & Automated Tests)
- **Validation:** Jakarta Validation (`@Valid`, Bean Validation)
- **Build Tool:** Apache Maven
- **Testing:** JUnit 5, Mockito, Spring Security Test, Spring Boot MockMvc, AssertJ

### Security Architecture & Request Flow
```
Client (Postman / Web App)
           │
           │ 1. HTTP Request + Header: 'Authorization: Bearer <jwt>'
           ▼
JwtAuthenticationFilter (OncePerRequestFilter)
  ├── Extracts Bearer token
  ├── Validates token signature & expiration
  └── Loads UserDetails & sets SecurityContextHolder
           │
           ▼
Spring Security Filter Chain
  ├── /api/v1/auth/** (Permitted)
  └── /api/v1/expenses/** (Requires Authenticated User)
           │
           ▼
ExpenseController & ExpenseService
  └── Scopes every query strictly to authenticated userId
           │
           ▼
Database (MySQL / H2)
```

---

## Database Design & Isolation

### Entities & Relationships
- **User (1) ───< (M) Expense**
- Every expense record has a mandatory foreign key `user_id` pointing to `users(id)`.
- **Strict Data Isolation:** All read, update, and delete operations filter by `WHERE id = :id AND user.id = :userId`. Even if an attacker guesses another user's expense ID, the system returns `404 Not Found` to prevent entity enumeration.

### Composite Indexes
- `expenses(user_id, expense_date)`: Fast range scanning for monthly summaries and date filters.
- `expenses(user_id, category)`: Fast category-based queries per user.

---

## API Endpoints

### Authentication Endpoints (`/api/v1/auth`)

| Method | Endpoint | Description | Status | Auth Required |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Register new user account | `201 Created` | No |
| `POST` | `/api/v1/auth/login` | Authenticate and obtain JWT token | `200 OK` | No |

### Expense Endpoints (`/api/v1/expenses`)

| Method | Endpoint | Description | Status | Auth Required |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/expenses` | Log new expense | `201 Created` | Yes (Bearer Token) |
| `GET` | `/api/v1/expenses` | List expenses (filterable, paginated, sorted) | `200 OK` | Yes (Bearer Token) |
| `GET` | `/api/v1/expenses/{id}` | Get expense by ID | `200 OK` | Yes (Bearer Token) |
| `PUT` | `/api/v1/expenses/{id}` | Update existing expense | `200 OK` | Yes (Bearer Token) |
| `DELETE` | `/api/v1/expenses/{id}` | Delete expense record | `204 No Content` | Yes (Bearer Token) |
| `GET` | `/api/v1/expenses/summary`| Monthly spending summary & category metrics | `200 OK` | Yes (Bearer Token) |
| `GET` | `/api/v1/categories` | List all supported expense categories | `200 OK` | Yes (Bearer Token) |

---

## Sample Requests & Responses

### 1. User Registration
`POST /api/v1/auth/register`
```json
{
  "name": "Alex Mercer",
  "email": "alex.mercer@example.com",
  "password": "StrongPassword123!"
}
```
**Response (`201 Created`):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGV4Lm1lcmNlckBleGFtcGxlLmNvbSIsImlhdCI6MTczMTc2...xyz",
  "tokenType": "Bearer",
  "userId": 1,
  "name": "Alex Mercer",
  "email": "alex.mercer@example.com",
  "roles": ["ROLE_USER"]
}
```

### 2. User Login
`POST /api/v1/auth/login`
```json
{
  "email": "alex.mercer@example.com",
  "password": "StrongPassword123!"
}
```
**Response (`200 OK`):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "name": "Alex Mercer",
  "email": "alex.mercer@example.com",
  "roles": ["ROLE_USER"]
}
```

### 3. Create an Expense
`POST /api/v1/expenses`  
*Header:* `Authorization: Bearer <your_jwt_token>`
```json
{
  "title": "Weekly Organic Produce",
  "description": "Vegetables, oats, and dairy",
  "amount": 2450.00,
  "expenseDate": "2026-11-16",
  "category": "FOOD"
}
```
**Response (`201 Created`):**
```json
{
  "id": 1,
  "title": "Weekly Organic Produce",
  "description": "Vegetables, oats, and dairy",
  "amount": 2450.00,
  "expenseDate": "2026-11-16",
  "category": "FOOD",
  "createdAt": "2026-11-16T17:20:00",
  "updatedAt": "2026-11-16T17:20:00"
}
```

### 4. Monthly Spending Analytics
`GET /api/v1/expenses/summary?year=2026&month=11`  
*Header:* `Authorization: Bearer <your_jwt_token>`

**Response (`200 OK`):**
```json
{
  "year": 2026,
  "month": 11,
  "totalSpent": 26850.50,
  "totalTransactions": 6,
  "categories": [
    {
      "category": "HOUSING",
      "totalAmount": 18000.00,
      "percentage": 67.04,
      "transactionCount": 1
    },
    {
      "category": "FOOD",
      "totalAmount": 3250.50,
      "percentage": 12.11,
      "transactionCount": 1
    },
    {
      "category": "HEALTHCARE",
      "totalAmount": 2100.00,
      "percentage": 7.82,
      "transactionCount": 1
    },
    {
      "category": "UTILITIES",
      "totalAmount": 1450.00,
      "percentage": 5.40,
      "transactionCount": 1
    }
  ]
}
```

---

## Setup & Local Execution

### Prerequisites
- Java 21 LTS installed (`java -version`)
- Maven 3.8+ installed (`mvn -version`)

### Run in Development Mode (Zero-setup with H2)
By default, the application runs on the `dev` profile with an in-memory database and pre-loaded test data:
```bash
mvn spring-boot:run
```
- API Base URL: `http://localhost:8080`
- **Pre-seeded Demo Credentials:**
  - Email: `demo@expensetracker.com`
  - Password: `Password123!`

### Run with MySQL Profile
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=expense_tracker_db
export DB_USERNAME=root
export DB_PASSWORD=your_password
export JWT_SECRET=your_base64_encoded_256_bit_secret

mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

### Run Automated Tests
```bash
mvn clean test
```

---

## Postman Collection

A complete Postman collection is included in the root directory: [`postman_collection.json`](postman_collection.json).
- Import it into Postman (`File -> Import -> postman_collection.json`).
- Automatically captures and saves your JWT token upon successful login into the `token` collection variable.
- Includes pre-configured endpoints for User Registration, Login, Category Listing, Expense CRUD, Pagination, Filtering, and Monthly Summaries.

---

## Key Interview Talking Points

1. **How Does JWT Authentication Work Here?**
   - The API is completely stateless (`SessionCreationPolicy.STATELESS`). The server does not store session IDs in memory or redis.
   - When a user logs in, the server signs a cryptographically secure token using HMAC-SHA256 (`Keys.hmacShaKeyFor`) containing the user's email and expiration.
   - For every protected request, `JwtAuthenticationFilter` intercepts the request, verifies the signature, and sets the authenticated principal in Spring's `SecurityContextHolder`.

2. **Why BCrypt for Password Storage?**
   - BCrypt automatically generates and incorporates a unique cryptographic salt into each password hash, defending against rainbow table attacks. It also has an adaptive cost factor (`log2(iterations)`), which can be tuned as hardware performance increases.

3. **How Is User Data Isolated?**
   - Rather than relying on simple `findById(id)`, we use `findByIdAndUserId(id, currentUser.getId())`.
   - If User A attempts to view, update, or delete User B's expense, the query returns empty, causing a `ResourceNotFoundException`. This avoids leaking whether an expense ID belongs to another user.

4. **How Did You Implement Category Aggregation?**
   - In `ExpenseRepository`, we utilized Spring Data JPA Interface Projections with JPQL `GROUP BY` and `SUM(e.amount)`. This computes aggregations at the database engine level rather than loading thousands of records into JVM memory.
