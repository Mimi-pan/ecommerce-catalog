# E-Commerce Product Catalog API
![CI](https://github.com/Mimi-pan/ecommerce-catalog/actions/workflows/ci.yml/badge.svg)


A RESTful API built with **Java 17** and **Spring Boot 3** for managing an e-commerce product catalog. Demonstrates clean layered architecture, JWT authentication, input validation, paginated queries, and containerization — built as a portfolio project for a Java Backend position.

---

## Tech Stack

| Layer        | Technology                          |
|--------------|--------------------------------------|
| Language     | Java 17                              |
| Framework    | Spring Boot 3.2                      |
| Security     | Spring Security 6 + JWT (JJWT 0.12) |
| Data Access  | Spring Data JPA + Hibernate          |
| Database     | H2 (dev) / PostgreSQL (prod-ready)   |
| Validation   | Jakarta Bean Validation              |
| API Docs     | SpringDoc OpenAPI 3 (Swagger UI)     |
| Build Tool   | Maven                                |
| Containers   | Docker + Docker Compose              |
| CI           | GitHub Actions                       |
| Utilities    | Lombok                               |

---

## Getting Started

### Option 1 — Run locally (requires Java 17 + Maven)

```bash
git clone https://github.com/Mimi-pan/ecommerce-catalog.git
cd ecommerce-catalog
./mvnw spring-boot:run
```

### Option 2 — Run with Docker

```bash
docker-compose up --build
```

The API will be available at `http://localhost:8080` either way.

---

## Authentication (JWT)

**GET** endpoints are public — no token needed.
**POST, PUT, DELETE** endpoints require a valid JWT in the `Authorization` header.

### 1. Register an account

```http
POST /auth/register
Content-Type: application/json

{
  "username": "mimi",
  "password": "secret123"
}
```

### 2. Log in

```http
POST /auth/login
Content-Type: application/json

{
  "username": "mimi",
  "password": "secret123"
}
```

Both return:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "username": "mimi",
  "role": "USER"
}
```

### 3. Use the token

Include it in the `Authorization` header for all protected requests:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## Interactive API Docs (Swagger UI)

Open **`http://localhost:8080/swagger-ui.html`** in your browser.

- Click **Authorize** (top right) and paste your JWT token
- All endpoints are documented with request/response examples
- Try any endpoint directly in the browser — no Postman needed

Raw OpenAPI JSON: `http://localhost:8080/api-docs`

---

## H2 Database Console (dev only)

Open `http://localhost:8080/h2-console` and connect with:

| Field    | Value                              |
|----------|------------------------------------|
| JDBC URL | `jdbc:h2:mem:ecommercedb`          |
| Username | `sa`                               |
| Password | *(leave empty)*                    |

The database is seeded automatically with **5 categories** and **11 products** on startup.

---

## API Endpoints

### Authentication

| Method | Endpoint         | Auth | Description                  |
|--------|------------------|------|------------------------------|
| POST   | `/auth/register` | —    | Create a new account         |
| POST   | `/auth/login`    | —    | Log in and receive JWT token |

### Categories

| Method | Endpoint                  | Auth    | Description                          |
|--------|---------------------------|---------|--------------------------------------|
| GET    | `/api/v1/categories`      | —       | List all categories (with product count) |
| GET    | `/api/v1/categories/{id}` | —       | Get category by ID                   |
| POST   | `/api/v1/categories`      | **JWT** | Create a new category                |
| PUT    | `/api/v1/categories/{id}` | **JWT** | Update a category                    |
| DELETE | `/api/v1/categories/{id}` | **JWT** | Delete a category (if no products)   |

### Products

| Method | Endpoint                                  | Auth    | Description                             |
|--------|-------------------------------------------|---------|-----------------------------------------|
| GET    | `/api/v1/products`                        | —       | List active products (paginated)        |
| GET    | `/api/v1/products/{id}`                   | —       | Get product by ID                       |
| GET    | `/api/v1/products/category/{id}`          | —       | Products by category (paginated)        |
| GET    | `/api/v1/products/search?name=...`        | —       | Search by name (partial, case-insensitive) |
| GET    | `/api/v1/products/price-range?min=&max=`  | —       | Filter by price range                   |
| GET    | `/api/v1/products/in-stock`               | —       | List in-stock products                  |
| POST   | `/api/v1/products`                        | **JWT** | Create a new product                    |
| PUT    | `/api/v1/products/{id}`                   | **JWT** | Update a product                        |
| DELETE | `/api/v1/products/{id}`                   | **JWT** | Soft-delete a product (marks inactive)  |

---

## Pagination & Sorting

```
GET /api/v1/products?page=0&size=5&sort=price,asc
GET /api/v1/products?page=1&size=10&sort=name,desc
```

---

## Running Tests

```bash
mvn test
```

The test suite includes:
- **Integration tests** for all controller endpoints (MockMvc + H2)
- **Unit tests** for the service layer (Mockito, no Spring context)
- **Smoke test** verifying the full application context loads

---

## Error Handling

All errors follow a consistent JSON format:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: '99'",
  "path": "/api/v1/products/99",
  "timestamp": "2026-03-18T10:00:00"
}
```

Validation errors include a `fieldErrors` map:

```json
{
  "status": 422,
  "error": "Validation Failed",
  "message": "One or more fields are invalid",
  "fieldErrors": {
    "price": "Price must be greater than zero",
    "name": "Product name is required"
  }
}
```

---

## Key Design Decisions

- **JWT authentication:** Stateless token-based auth — no server-side sessions. The filter validates the token on every request.
- **Soft delete:** Products are marked `active = false` instead of being physically removed, preserving data integrity.
- **DTO pattern:** Entities are never exposed directly — request and response DTOs decouple the API contract from the persistence model.
- **Service interfaces:** Controllers depend on interfaces, not implementations, making the code easily testable and extensible.
- **Paginated responses:** All list endpoints support `page`, `size`, and `sort` parameters to handle large datasets efficiently.
- **Consistent error responses:** A single `@RestControllerAdvice` handler produces uniform error JSON for all exception types.
- **N+1 prevention:** `CategoryServiceImpl` uses `LEFT JOIN FETCH` to load categories and their product counts in a single SQL query.

---

## Switching to PostgreSQL

Update `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=false
```

And add the PostgreSQL driver to `pom.xml`:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## Project Structure

```
src/
└── main/
    ├── java/com/portfolio/ecommerce/
    │   ├── EcommerceApplication.java
    │   ├── config/
    │   │   ├── OpenApiConfig.java          # Swagger + JWT bearer scheme
    │   │   ├── SecurityConfig.java         # Spring Security + CORS
    │   │   └── WebConfig.java              # CORS (backup)
    │   ├── controller/
    │   │   ├── AuthController.java         # /auth/register, /auth/login
    │   │   ├── CategoryController.java
    │   │   └── ProductController.java
    │   ├── dto/
    │   │   ├── auth/                       # RegisterRequestDTO, LoginRequestDTO, AuthResponseDTO
    │   │   ├── CategoryDTO / CategoryRequestDTO
    │   │   └── ProductDTO / ProductRequestDTO
    │   ├── model/
    │   │   ├── Category.java
    │   │   ├── Product.java                # @PrePersist / @PreUpdate for timestamps
    │   │   └── User.java                   # USER / ADMIN roles
    │   ├── repository/
    │   │   ├── CategoryRepository.java     # findAllWithProducts() — JOIN FETCH
    │   │   ├── ProductRepository.java      # custom JPQL queries
    │   │   └── UserRepository.java
    │   ├── security/
    │   │   ├── JwtTokenProvider.java       # generate / validate / extract JWT
    │   │   ├── JwtAuthenticationFilter.java # OncePerRequestFilter
    │   │   └── UserDetailsServiceImpl.java
    │   ├── service/
    │   │   ├── CategoryService / CategoryServiceImpl
    │   │   └── ProductService / ProductServiceImpl (soft-delete)
    │   └── exception/
    │       ├── GlobalExceptionHandler.java  # @RestControllerAdvice
    │       ├── ResourceNotFoundException.java
    │       ├── BusinessException.java
    │       └── ErrorResponse.java
    └── resources/
        ├── application.properties
        └── data.sql                         # 5 categories, 11 products
```

---

## Author

Built by **Mimi** — portfolio project for Java Backend Junior position.
