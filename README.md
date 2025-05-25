# Expense Tracker Application

This application is a Spring Boot-based expense tracker with Spring Security integration.

## Security Implementation

The application implements Spring Security with the following features:
- User authentication with email and password
- Password hashing using BCrypt
- Custom login and registration pages
- Role-based authorization
- Public and protected pages
- Record ownership security

## Demo Users

The following users are created on application startup:

| Email | Password | Role |
|-------|----------|------|
| admin@expenses.com | admin123 | ADMIN |
| manager@expenses.com | manager123 | MANAGER |
| john@expenses.com | john123 | USER |
| jane@expenses.com | jane123 | USER |

## Role Permissions

### Unauthenticated Users
- View the home page
- View the list of expenses (read-only)
- Register a new account
- Log in

### Authenticated Users (ROLE_USER)
- All permissions of unauthenticated users
- Create new expenses (which are associated with their account)
- Edit their own expenses
- Delete their own expenses
- View the categories page

### Managers (ROLE_MANAGER)
- All permissions of authenticated users
- Access to the [Reports Dashboard](http://localhost:8080/reports) with expense analytics
- View expense summaries and statistics

### Administrators (ROLE_ADMIN)
- All permissions of managers
- Access to the [Admin Dashboard](http://localhost:8080/admin)
- Edit and delete any user's expenses
- Manage user accounts

## User-Entity Relationship

- Users are associated with Expenses through a one-to-many relationship
- When a user creates an expense, they become the owner of that expense
- Only the owner of an expense or an administrator can modify or delete that expense
- The [Expenses Page](http://localhost:8080/expenses) shows different content based on authentication status
- Authenticated users see their personal expenses and can manage them
- CSRF protection is enabled for web forms, with an exception for API endpoints

## Hidden Features for Unauthenticated Users

- The Categories page is completely hidden and inaccessible
- The ability to create, edit, or delete expenses is hidden
- Admin and Reports dashboards are hidden
- The [Expenses Page](http://localhost:8080/expenses) shows limited functionality

## Authorization Implementation

- Frontend: Navigation links and action buttons are conditionally displayed based on user role and ownership
- Backend: All operations that modify data verify both authentication status and record ownership
- API endpoints that modify data require authentication
- The application uses both method-level security and programmatic security checks

## Page Access

- **Public Pages**: 
  - [Home Page](http://localhost:8080/)
  - [Expenses Page](http://localhost:8080/expenses) (with limited functionality for anonymous users)
  - Login and Registration pages

- **Protected Pages**:
  - [Categories Page](http://localhost:8080/categories) (requires authentication)

## Implementation Details

- User entity implements Spring Security's UserDetails interface
- Passwords are hashed in the database using BCrypt
- CSRF protection is temporarily disabled as per requirements
- The navigation bar shows login/register links for anonymous users and username/logout for authenticated users
- The Expenses page shows different content based on authentication status

## Week 2

### HTTP Requests and Responses

#### Get All Expenses - OK
```
GET http://localhost:8080/api/expenses
Accept: application/json
```

Response (200 OK):
```
[
  {
    "id": 1,
    "description": "Groceries",
    "amount": 75.50,
    "date": "2023-05-10",
    "user": {
      "id": 1,
      "name": "John Doe"
    }
  },
  {
    "id": 2,
    "description": "Internet Bill",
    "amount": 59.99,
    "date": "2023-05-05",
    "user": {
      "id": 1,
      "name": "John Doe"
    }
  }
]
```

#### Get Expense by ID - OK
```
GET http://localhost:8080/api/expenses/1
Accept: application/json
```

Response (200 OK):
```
{
  "id": 1,
  "description": "Groceries",
  "amount": 75.50,
  "date": "2023-05-10",
  "user": {
    "id": 1,
    "name": "John Doe"
  },
  "expenseCategories": [
    {
      "id": 1,
      "category": {
        "id": 1,
        "name": "Food"
      }
    }
  ]
}
```

#### Get Expense by ID - Not Found
```
GET http://localhost:8080/api/expenses/999
Accept: application/json
```

Response (404 Not Found):
```
```

#### Get Expenses by User ID - OK
```
GET http://localhost:8080/api/expenses/user/1
Accept: application/json
```

Response (200 OK):
```
[
  {
    "id": 1,
    "description": "Groceries",
    "amount": 75.50,
    "date": "2023-05-10"
  },
  {
    "id": 2,
    "description": "Internet Bill",
    "amount": 59.99,
    "date": "2023-05-05"
  }
]
```

#### Get Expenses by User ID - Not Found
```
GET http://localhost:8080/api/expenses/user/999
Accept: application/json
```

Response (404 Not Found):
```
```

#### Delete Expense - Success
```
DELETE http://localhost:8080/api/expenses/1
Accept: application/json
```

Response (204 No Content):
```
```

#### Delete Expense - Error
```
DELETE http://localhost:8080/api/expenses/999
Accept: application/json
```

Response (500 Internal Server Error):
```
```

## Week 3

### HTTP Requests and Responses

#### Creating an Expense - Success
```
POST http://localhost:8080/api/expenses
Content-Type: application/json
Accept: application/json

{
  "description": "New Laptop",
  "amount": 1299.99,
  "date": "2023-05-15",
  "userId": 1,
  "categoryIds": [1, 2]
}
```

Response (201 Created):
```
{
  "id": 3,
  "description": "New Laptop",
  "amount": 1299.99,
  "date": "2023-05-15",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "categories": [
    {
      "id": 1,
      "name": "Electronics",
      "description": "Electronic devices and accessories"
    },
    {
      "id": 2,
      "name": "Work",
      "description": "Work-related expenses"
    }
  ]
}
```

#### Creating an Expense - Bad Request (Missing Fields)
```
POST http://localhost:8080/api/expenses
Content-Type: application/json
Accept: application/json

{
  "description": "New Laptop",
  "amount": 1299.99,
  "date": "2023-05-15"
  // Missing userId
}
```

Response (400 Bad Request):
```
{
  "userId": "User ID is required"
}
```

#### Creating an Expense - Bad Request (Invalid Data)
```
POST http://localhost:8080/api/expenses
Content-Type: application/json
Accept: application/json

{
  "description": "AB", // Too short
  "amount": -1299.99, // Negative amount
  "date": "2030-01-01", // Future date
  "userId": 1
}
```

Response (400 Bad Request):
```
{
  "description": "Description must be between 3 and 100 characters",
  "amount": "Amount must be positive",
  "date": "Date cannot be in the future"
}
```

#### Updating an Expense - Success
```
PATCH http://localhost:8080/api/expenses/1
Content-Type: application/json
Accept: application/json

{
  "description": "Updated Description",
  "amount": 150.00
}
```

Response (200 OK):
```
{
  "id": 1,
  "description": "Updated Description",
  "amount": 150.00,
  "date": "2023-05-10",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "categories": [
    {
      "id": 1,
      "name": "Food",
      "description": "Food and grocery expenses"
    }
  ]
}
```

#### Updating an Expense - Not Found
```
PATCH http://localhost:8080/api/expenses/999
Content-Type: application/json
Accept: application/json

{
  "description": "Updated Description"
}
```

Response (404 Not Found):
```
```

#### Updating an Expense - Bad Request
```
PATCH http://localhost:8080/api/expenses/1
Content-Type: application/json
Accept: application/json

{
  "description": "A", // Too short
  "amount": -10.00 // Negative amount
}
```

Response (400 Bad Request):
```
{
  "description": "Description must be between 3 and 100 characters",
  "amount": "Amount must be positive"
}
``` 