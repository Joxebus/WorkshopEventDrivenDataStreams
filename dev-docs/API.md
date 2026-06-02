# REST API Documentation

## Base URL
```
http://localhost:8080/api
```

## Products API

### Get All Products
```http
GET /api/products
```

**Response:** `200 OK`
```json
[
  {
    "id": "uuid-string",
    "name": "Product Name",
    "description": "Product description",
    "price": 29.99,
    "stock": 100,
    "category": "Electronics"
  }
]
```

### Get Product by ID
```http
GET /api/products/{id}
```

**Parameters:**
- `id` (path) - Product UUID

**Response:** `200 OK`
```json
{
  "id": "uuid-string",
  "name": "Product Name",
  "description": "Product description",
  "price": 29.99,
  "stock": 100,
  "category": "Electronics"
}
```

**Error:** `404 Not Found` if product doesn't exist

### Get Products by Category
```http
GET /api/products/category/{category}
```

**Parameters:**
- `category` (path) - Category name

**Response:** `200 OK` with array of products

### Search Products by Name
```http
GET /api/products/search?name={searchTerm}
```

**Query Parameters:**
- `name` - Search term (case-insensitive)

**Response:** `200 OK` with array of matching products

### Create Product
```http
POST /api/products
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "New Product",
  "description": "Product description",
  "price": 29.99,
  "stock": 100,
  "category": "Electronics"
}
```

**Response:** `201 Created`
```json
{
  "id": "generated-uuid",
  "name": "New Product",
  "description": "Product description",
  "price": 29.99,
  "stock": 100,
  "category": "Electronics"
}
```

### Update Product
```http
PUT /api/products/{id}
Content-Type: application/json
```

**Parameters:**
- `id` (path) - Product UUID

**Request Body:**
```json
{
  "name": "Updated Product",
  "description": "Updated description",
  "price": 34.99,
  "stock": 150,
  "category": "Electronics"
}
```

**Response:** `200 OK` with updated product

**Error:** `404 Not Found` if product doesn't exist

### Delete Product
```http
DELETE /api/products/{id}
```

**Parameters:**
- `id` (path) - Product UUID

**Response:** `204 No Content`

---

## Orders API

### Get All Orders
```http
GET /api/orders
```

**Response:** `200 OK`
```json
[
  {
    "orderId": "uuid-string",
    "customerId": "CUSTOMER-001",
    "products": [
      {
        "productId": "product-uuid",
        "quantity": 2,
        "price": 29.99,
        "total": 59.98
      }
    ],
    "total": 59.98,
    "orderDate": "2026-04-30T19:30:00",
    "status": "PENDING"
  }
]
```

### Get Order by ID
```http
GET /api/orders/{id}
```

**Parameters:**
- `id` (path) - Order UUID

**Response:** `200 OK` with order details

**Error:** `404 Not Found` if order doesn't exist

### Get Orders by Customer
```http
GET /api/orders/customer/{customerId}
```

**Parameters:**
- `customerId` (path) - Customer identifier

**Response:** `200 OK` with array of customer orders

### Get Orders by Status
```http
GET /api/orders/status/{status}
```

**Parameters:**
- `status` (path) - One of: `CREATED`, `PENDING`, `COMPLETED`, `CANCELLED`, `SHIPPED`, `DELIVERED`

**Response:** `200 OK` with array of orders matching status

---

## Order Status Enum

| Status | Description |
|--------|-------------|
| `CREATED` | Order received from Kafka |
| `PENDING` | Order being processed |
| `COMPLETED` | Order fulfilled |
| `CANCELLED` | Order cancelled (validation failed) |
| `SHIPPED` | Order dispatched |
| `DELIVERED` | Order completed |

---

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2026-04-30T19:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input data",
  "path": "/api/products"
}
```

### 404 Not Found
```json
{
  "timestamp": "2026-04-30T19:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found: {id}",
  "path": "/api/products/{id}"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2026-04-30T19:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Unexpected error occurred",
  "path": "/api/..."
}
```

---

## CORS Configuration

The backend API allows cross-origin requests from:
- `http://localhost:8081` (Frontend application)

Headers allowed:
- `Content-Type`
- `Accept`

Methods allowed:
- `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`

---

## Rate Limiting

**Current:** No rate limiting implemented (workshop environment)

**Production Recommendation:** Implement rate limiting using:
- Spring Cloud Gateway
- Bucket4j
- Redis-based token bucket

---

## Testing with cURL

### Create a Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "description": "A test product",
    "price": 19.99,
    "stock": 50,
    "category": "Test"
  }'
```

### Get All Products
```bash
curl http://localhost:8080/api/products
```

### Get Product by ID
```bash
curl http://localhost:8080/api/products/{uuid}
```

### Update Product
```bash
curl -X PUT http://localhost:8080/api/products/{uuid} \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Product",
    "description": "Updated description",
    "price": 24.99,
    "stock": 75,
    "category": "Test"
  }'
```

### Get All Orders
```bash
curl http://localhost:8080/api/orders
```

---

## Kafka Message Format

While not a REST endpoint, purchase orders are created asynchronously via Kafka:

### Topic: `purchase-orders`

**Message Format (JSON):**
```json
{
  "orderId": "uuid-string",
  "customerId": "CUSTOMER-001",
  "products": [
    {
      "productId": "product-uuid",
      "quantity": 2,
      "price": 29.99,
      "total": 59.98
    }
  ],
  "total": 59.98,
  "orderDate": "2026-04-30T19:30:00",
  "status": "CREATED"
}
```

**Producer:** Frontend application  
**Consumer:** Backend application  
**Processing:** Asynchronous, decoupled from user request
