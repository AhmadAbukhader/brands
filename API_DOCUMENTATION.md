# Brands Management API Documentation

## Overview
This is a RESTful API for managing brands and products with JWT-based authentication.

## Base URL
```
http://localhost:8089
```

## Swagger/OpenAPI Documentation
Access interactive API documentation at:
- **Swagger UI**: http://localhost:8089/swagger-ui.html
- **API Docs**: http://localhost:8089/v3/api-docs

## Authentication

### Register a New User
**POST** `/auth/signup`

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "password123",
  "name": "John Doe"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "john_doe"
}
```

### Login
**POST** `/auth/login`

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "john_doe",
  "expiresIn": 86400000
}
```

## User Management

### Get Current User
**GET** `/user/me`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response:**
```json
{
  "id": 1,
  "username": "john_doe",
  "name": "John Doe"
}
```

## Brand Management

All brand endpoints require authentication. Include the JWT token in the Authorization header.

### Get All Brands
**GET** `/api/brands`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "Nike",
    "products": [...]
  }
]
```

### Get Brand by ID
**GET** `/api/brands/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response:**
```json
{
  "id": 1,
  "name": "Nike",
  "products": [
    {
      "id": 1,
      "brandId": 1,
      "brandName": "Nike",
      "name": "Air Max 90",
      "quantity": "100",
      "packaging": "Box",
      "unit": "Piece"
    }
  ]
}
```

### Create a Brand
**POST** `/api/brands`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Nike"
}
```

**Response:** (201 Created)
```json
{
  "id": 1,
  "name": "Nike",
  "products": null
}
```

### Update a Brand
**PUT** `/api/brands/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Nike Updated"
}
```

**Response:**
```json
{
  "id": 1,
  "name": "Nike Updated",
  "products": [...]
}
```

### Delete a Brand
**DELETE** `/api/brands/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response:** (204 No Content)

## Product Management

All product endpoints require authentication.

### Get All Products
**GET** `/api/products`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response:**
```json
[
  {
    "id": 1,
    "brandId": 1,
    "brandName": "Nike",
    "name": "Air Max 90",
    "quantity": "100",
    "packaging": "Box"
  }
]
```

### Get Product by ID
**GET** `/api/products/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response:**
```json
{
  "id": 1,
  "brandId": 1,
  "brandName": "Nike",
  "name": "Air Max 90",
  "quantity": "100",
  "packaging": "Box",
  "unit": "Piece"
}
```

### Get Products by Brand ID
**GET** `/api/products/brand/{brandId}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response:**
```json
[
  {
    "id": 1,
    "brandId": 1,
    "brandName": "Nike",
    "name": "Air Max 90",
    "quantity": "100",
    "packaging": "Box",
    "unit": "Piece"
  }
]
```

### Create a Product
**POST** `/api/products`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "brandId": 1,
  "name": "Air Max 90",
  "quantity": "100",
  "packaging": "Box",
  "unit": "Piece"
}
```

**Response:** (201 Created)
```json
{
  "id": 1,
  "brandId": 1,
  "brandName": "Nike",
  "name": "Air Max 90",
  "quantity": "100",
  "packaging": "Box",
  "unit": "Piece"
}
```

### Update a Product
**PUT** `/api/products/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "brandId": 1,
  "name": "Air Max 90 Updated",
  "quantity": "150",
  "packaging": "Premium Box",
  "unit": "Pair"
}
```

**Response:**
```json
{
  "id": 1,
  "brandId": 1,
  "brandName": "Nike",
  "name": "Air Max 90 Updated",
  "quantity": "150",
  "packaging": "Premium Box",
  "unit": "Pair"
}
```

### Delete a Product
**DELETE** `/api/products/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response:** (204 No Content)

## Error Responses

### 400 Bad Request
```json
{
  "error": "Invalid input or resource already exists"
}
```

### 401 Unauthorized
```json
{
  "error": "Invalid or missing authentication token"
}
```

### 404 Not Found
```json
{
  "error": "Resource not found"
}
```

## Testing with Swagger UI

1. Start your application
2. Open http://localhost:8089/swagger-ui.html
3. First, authenticate:
   - Use `/auth/login` or `/auth/signup` endpoint
   - Copy the JWT token from the response
4. Click the "Authorize" button at the top
5. Enter: `Bearer <your_jwt_token>`
6. Click "Authorize"
7. Now you can test all protected endpoints!

## Testing with cURL

### Example: Complete Workflow

```bash
# 1. Sign up
curl -X POST http://localhost:8089/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"pass123","name":"John Doe"}'

# 2. Login (save the token)
TOKEN=$(curl -X POST http://localhost:8089/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"pass123"}' \
  | jq -r '.token')

# 3. Create a brand
curl -X POST http://localhost:8089/api/brands \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Nike"}'

# 4. Create a product
curl -X POST http://localhost:8089/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"brandId":1,"name":"Air Max 90","quantity":"100","packaging":"Box"}'

# 5. Get all products
curl -X GET http://localhost:8089/api/products \
  -H "Authorization: Bearer $TOKEN"
```

## Database Schema

### Users Table
```sql
CREATE TABLE brands_schema.users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255)
);
```

### Brands Table
```sql
CREATE TABLE brands_schema.brands (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);
```

### Products Table
```sql
CREATE TABLE brands_schema.products (
    id SERIAL PRIMARY KEY,
    brand_id INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity VARCHAR(255),
    packaging VARCHAR(255),
    FOREIGN KEY (brand_id) REFERENCES brands(id) ON DELETE CASCADE
);
```

## Running the Application

1. Ensure PostgreSQL is running
2. Update database credentials in `application.properties`
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. Access Swagger UI at http://localhost:8089/swagger-ui.html

