# E-Commerce Product Catalog API

A RESTful API built with **Java 17** and **Spring Boot 3** for managing an e-commerce product catalog. This project demonstrates clean layered architecture, proper REST design, input validation, and paginated queries — built as a portfolio project for a Java Backend position.

---

## Tech Stack

| Layer        | Technology                          |
|--------------|--------------------------------------|
| Language     | Java 17                              |
| Framework    | Spring Boot 3.2                      |
| Data Access  | Spring Data JPA + Hibernate          |
| Database     | H2 (dev) / PostgreSQL (prod-ready)   |
| Validation   | Jakarta Bean Validation              |
| Build Tool   | Maven                                |
| Utilities    | Lombok                               |

---

## Project Structure

```
src/
└── main/
    ├── java/com/portfolio/ecommerce/
    │   ├── EcommerceApplication.java       # Entry point
    │   ├── controller/
    │   │   ├── CategoryController.java     # Category endpoints
    │   │   └── ProductController.java      # Product endpoints
    │   ├── model/
    │   │   ├── Category.java               # Category entity
    │   │   └── Product.java                # Product entity (@PrePersist/@PreUpdate)
    │   ├── repository/
    │   │   ├── CategoryRepository.java     # JPA repository
    │   │   └── ProductRepository.java      # JPA + JPQL custom queries
    │   ├── service/
    │   │   ├── CategoryService.java        # Interface
    │   │   ├── CategoryServiceImpl.java    # Implementation
    │   │   ├── ProductService.java         # Interface
    │   │   └── ProductServiceImpl.java     # Implementation (soft-delete)
    │   ├── dto/
    │   │   ├── CategoryDTO.java            # Response DTO
    │   │   ├── CategoryRequestDTO.java     # Request DTO (validated)
    │   │   ├── ProductDTO.java             # Response DTO
    │   │   └── ProductRequestDTO.java      # Request DTO (validated)
    │   └── exception/
    │       ├── GlobalExceptionHandler.java # @RestControllerAdvice
    │       ├── ResourceNotFoundException.java
    │       ├── BusinessException.java
    │       └── ErrorResponse.java          # Consistent error format
    └── resources/
        ├── application.properties          # Config
        └── data.sql                        # Seed data (5 categories, 11 products)
```

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+

### Run locally

```bash
# Clone the repo
git clone https://github.com/YOUR_USERNAME/ecommerce-catalog.git
cd ecommerce-catalog

# Run
./mvnw spring-boot:run
```

The API will start at `http://localhost:8080`.

### H2 Console (database browser)
Open `http://localhost:8080/h2-console` and connect with:
- **JDBC URL:** `jdbc:h2:mem:ecommercedb`
- **Username:** `sa`
- **Password:** *(leave empty)*

---

## API Endpoints

### Categories

| Method | Endpoint                  | Description                          |
|--------|---------------------------|--------------------------------------|
| GET    | `/api/v1/categories`      | List all categories                  |
| GET    | `/api/v1/categories/{id}` | Get category by ID                   |
| POST   | `/api/v1/categories`      | Create a new category                |
| PUT    | `/api/v1/categories/{id}` | Update a category                    |
| DELETE | `/api/v1/categories/{id}` | Delete a category (if no products)   |

### Products

| Method | Endpoint                              | Description                             |
|--------|---------------------------------------|-----------------------------------------|
| GET    | `/api/v1/products`                    | List active products (paginated)        |
| GET    | `/api/v1/products/{id}`               | Get product by ID                       |
| GET    | `/api/v1/products/category/{id}`      | Products by category (paginated)        |
| GET    | `/api/v1/products/search?name=...`    | Search by name (partial, case-insensitive) |
| GET    | `/api/v1/products/price-range?min=&max=` | Filter by price range               |
| GET    | `/api/v1/products/in-stock`           | List in-stock products                  |
| POST   | `/api/v1/products`                    | Create a new product                    |
| PUT    | `/api/v1/products/{id}`               | Update a product                        |
| DELETE | `/api/v1/products/{id}`               | Soft-delete a product (marks inactive)  |

### Pagination & Sorting

```
GET /api/v1/products?page=0&size=5&sort=price,asc
GET /api/v1/products?page=1&size=10&sort=name,desc
```

---

## Example Requests

### Create a Category
```http
POST /api/v1/categories
Content-Type: application/json

{
  "name": "Gaming",
  "description": "Consoles, accessories, and games"
}
```

### Create a Product
```http
POST /api/v1/products
Content-Type: application/json

{
  "name": "Gaming Controller",
  "description": "Wireless controller with vibration feedback",
  "price": 59.99,
  "stockQuantity": 40,
  "sku": "GAME-001",
  "categoryId": 1
}
```

### Search Products
```http
GET /api/v1/products/search?name=keyboard&page=0&size=5
```

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

- **Soft delete:** Products are marked `active = false` instead of being physically removed, preserving data integrity.
- **DTO pattern:** Entities are never exposed directly — request and response DTOs decouple the API contract from the persistence model.
- **Service interfaces:** Controllers depend on interfaces, not implementations, making the code easily testable and extensible.
- **Paginated responses:** All list endpoints support `page`, `size`, and `sort` parameters to handle large datasets efficiently.
- **Consistent error responses:** A single `@RestControllerAdvice` handler produces uniform error JSON for all exception types.

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

## Author

Built by **[Your Name]** — portfolio project for Java Backend Junior position.
