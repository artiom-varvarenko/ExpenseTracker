# Expense Tracker Application


### Prerequisites

- Java 21
- Docker & Docker Compose
- Node.js (for the client application)

### Running the Application

1. **Start the database:**
   ```bash
   docker-compose up -d
   ```

2. **Run the Spring Boot application:**
   ```bash
   ./gradlew bootRun
   ```

3. **Access the application:**
  - Web Interface: http://localhost:8080
  - API Documentation: http://localhost:8080/api/expenses

##  Project Structure

```
├── src/main/java/          # Spring Boot application
├── src/main/resources/     # Configuration & templates
├── src/test/              # Test files
├── docker-compose.yml     # PostgreSQL database
└── Client/               # Frontend application (separate repo)
```

## Demo Users

The application comes with pre-configured demo users:

| Email | Password | Role |
|-------|----------|------|
| admin@expenses.com | admin123 | ADMIN |
| manager@expenses.com | manager123 | MANAGER |
| john@expenses.com | john123 | USER |
| jane@expenses.com | jane123 | USER |

## Technologies

- **Backend:** Spring Boot 3.2, Spring Security, JPA/Hibernate
- **Database:** PostgreSQL
- **Frontend:** Thymeleaf, Bootstrap 5, JavaScript
- **Build:** Gradle
- **Testing:** JUnit 5, MockMvc
- **CI/CD:** GitLab CI

## Features

-  User authentication & authorization
-  Expense CRUD operations
-  Category management
-  Role-based access control
-  REST API endpoints
-  Responsive web interface
-  Admin dashboard
-  Comprehensive testing

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/expenses` | Get all expenses |
| GET | `/api/expenses/{id}` | Get expense by ID |
| POST | `/api/expenses` | Create new expense |
| PATCH | `/api/expenses/{id}` | Update expense |
| DELETE | `/api/expenses/{id}` | Delete expense |

## Testing

```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport
```

## Database Configuration

The application uses PostgreSQL running in Docker. The database is automatically configured with the connection details in `docker-compose.yml`.

Default connection:
- **Host:** localhost:5433
- **Database:** expensetracker
- **Username:** spring
- **Password:** spring

## Security

The application implements Spring Security with:
- Form-based authentication
- CSRF protection
- Role-based authorization (USER, ADMIN, MANAGER)
- Password encryption with BCrypt

## Development Notes

- Uses JPA best practices (lazy loading, proper fetch strategies)
- Implements proper exception handling
- Follows REST API conventions
- Includes comprehensive test coverage
- CI/CD ready with GitLab CI
