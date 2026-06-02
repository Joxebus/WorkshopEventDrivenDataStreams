# Spring Boot Kafka Workshop - Architecture Documentation

## Overview

This project demonstrates Event-Driven Architecture patterns using Spring Boot and Apache Kafka. It implements a product ordering system with both synchronous (REST) and asynchronous (Kafka) communication patterns.

## System Architecture

```mermaid
graph TB
    subgraph "Frontend (Port 8081)"
        UI[Thymeleaf Views]
        FC[Frontend Controllers]
        FS[Frontend Services]
        KP[Kafka Producer]
    end

    subgraph "Kafka Cluster"
        TOPIC[purchase-orders Topic]
    end

    subgraph "Backend (Port 8080)"
        RC[REST Controllers]
        CS[Command Services]
        QS[Query Services]
        R[Repositories]
        KC[Kafka Consumer]
    end

    subgraph "Data Layer"
        DB[(MySQL Database)]
    end

    UI --> FC
    FC --> FS
    FC --> KP
    FS -->|HTTP GET/POST| RC
    KP -->|Async Messages| TOPIC
    TOPIC -->|Consume| KC
    RC --> CS
    RC --> QS
    KC --> CS
    CS --> R
    QS --> R
    R --> DB
```

## Communication Patterns

### 1. Synchronous Communication (HTTP REST)

Used for operations requiring immediate response:

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant Backend
    participant Database

    User->>Frontend: Browse Products
    Frontend->>Backend: GET /api/products
    Backend->>Database: Query Products
    Database-->>Backend: Product List
    Backend-->>Frontend: 200 OK + Products
    Frontend-->>User: Display Products
```

**Use Cases:**
- Product listing (GET /api/products)
- Product details (GET /api/products/{id})
- Order listing (GET /api/orders)
- Order details (GET /api/orders/{id})

### 2. Asynchronous Communication (Kafka)

Used for operations that can be processed independently:

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant Kafka
    participant Backend
    participant Database

    User->>Frontend: Place Order
    Frontend->>Frontend: Create PurchaseOrderDTO
    Frontend->>Kafka: Produce Message (purchase-orders)
    Frontend-->>User: Order Placed (Confirmation)
    
    Note over Kafka,Backend: Asynchronous Processing
    
    Kafka->>Backend: Consume Message
    Backend->>Backend: Validate Order
    Backend->>Database: Save Order
    Backend->>Database: Update Stock
    Database-->>Backend: Success
    Backend->>Backend: Update Order Status
```

**Use Cases:**
- Purchase order creation
- High-throughput operations
- Decoupled processing
- Multiple consumer support (future)

## CQRS Pattern Implementation

The backend implements Command Query Responsibility Segregation at the service layer:

```mermaid
graph LR
    subgraph "Command Side (Write)"
        CC[Controllers]
        KFC[Kafka Consumer]
        CS[CommandService]
    end

    subgraph "Query Side (Read)"
        QC[Controllers]
        QS[QueryService]
    end

    subgraph "Shared"
        R[Repositories]
        DB[(Database)]
    end

    CC --> CS
    KFC --> CS
    QC --> QS
    CS --> R
    QS --> R
    R --> DB
```

### Command Services (Write Operations)
- **ProductCommandService**:
  - `createProduct()` - Create new product with UUID
  - `updateProduct()` - Update existing product
  - `decrementStock()` - Reduce product inventory

- **OrderCommandService**:
  - `createOrder()` - Process purchase order from Kafka
  - `updateOrderStatus()` - Change order state
  - Validates product availability
  - Manages stock decrements

### Query Services (Read Operations)
- **ProductQueryService**:
  - `getAllProducts()` - List all products
  - `getProductById()` - Find by ID
  - `getProductsByCategory()` - Filter by category

- **OrderQueryService**:
  - `getAllOrders()` - List all orders
  - `getOrderById()` - Get order with items
  - `getOrdersByCustomerId()` - Filter by customer
  - `getOrdersByStatus()` - Filter by status

## Data Flow

### Product Management Flow (Synchronous)

```mermaid
flowchart TD
    A[User Creates Product] --> B[Frontend Controller]
    B --> C[Product Service REST Call]
    C --> D[Backend REST Controller]
    D --> E[ProductCommandService]
    E --> F[ProductRepository]
    F --> G[MySQL Database]
    G --> H[Product Saved]
    H --> I[Response to Frontend]
    I --> J[Success Message Displayed]
```

### Purchase Order Flow (Asynchronous)

```mermaid
flowchart TD
    A[User Adds Products to Cart] --> B[Session Storage]
    B --> C[User Confirms Order]
    C --> D[OrderController]
    D --> E[Create PurchaseOrderDTO]
    E --> F{Generate UUID}
    F --> G[Set Customer ID]
    G --> H[Set Status: CREATED]
    H --> I[Kafka Producer]
    I --> J[Send to purchase-orders Topic]
    J --> K[Clear Cart]
    K --> L[Redirect to Orders]

    M[Kafka Consumer] --> N{Receive Message}
    N --> O[OrderCommandService]
    O --> P{Validate Products}
    P -->|Valid| Q[Create Order Entity]
    P -->|Invalid| R[Mark as CANCELLED]
    Q --> S{Check Stock}
    S -->|Sufficient| T[Decrement Stock]
    S -->|Insufficient| R
    T --> U[Set Status: PENDING]
    U --> V[Save to Database]
    V --> W[Order Processed]
```

## Domain Model

### Entity Relationships

```mermaid
erDiagram
    PRODUCT ||--o{ ORDER_ITEM : references
    ORDER ||--o{ ORDER_ITEM : contains
    
    PRODUCT {
        string id PK
        string name
        string description
        double price
        integer stock
        string category
        datetime created_at
        datetime updated_at
    }
    
    ORDER {
        string id PK
        string customer_id
        double total
        datetime order_date
        enum status
    }
    
    ORDER_ITEM {
        long id PK
        string order_id FK
        string product_id FK
        integer quantity
        double price
        double subtotal
    }
```

### DTOs and Data Transfer

```mermaid
graph LR
    subgraph "Common Module"
        PD[ProductDTO]
        POD[PurchaseOrderDTO]
        POI[ProductOrderDTO]
        PS[PurchaseOrderStatus]
    end

    subgraph "Backend Entities"
        PE[Product Entity]
        OE[Order Entity]
        OIE[OrderItem Entity]
    end

    subgraph "Frontend"
        CI[CartItem Session]
    end

    PD <--> PE
    POD --> OE
    POI --> OIE
    PS --> OE
    CI --> POD
```

## Technology Stack

### Backend
- **Spring Boot 4.0.6** - Application framework
- **Spring Data JPA** - Data access layer
- **Spring Kafka** - Kafka integration
- **MySQL 8.x** - Relational database
- **Lombok** - Boilerplate reduction
- **Jakarta EE** - Enterprise specifications

### Frontend
- **Spring Boot 4.0.6** - Application framework
- **Thymeleaf** - Template engine
- **Tailwind CSS** - Utility-first CSS
- **Spring Kafka** - Kafka producer
- **RestTemplate** - HTTP client

### Infrastructure
- **Apache Kafka** - Message broker
- **Zookeeper** - Kafka coordination
- **Docker Compose** - Container orchestration
- **Maven** - Build tool
- **Java 25** - Programming language

## Serialization Strategy

### Phase 1: JSON (Current)

```mermaid
graph LR
    subgraph "Producer (Frontend)"
        DTO1[PurchaseOrderDTO] --> JS[JsonSerializer]
    end
    
    JS --> K[Kafka Topic]
    
    subgraph "Consumer (Backend)"
        K --> JD[JsonDeserializer]
        JD --> DTO2[PurchaseOrderDTO]
    end
```

**Advantages:**
- Simple configuration
- Human-readable messages
- Easy debugging
- No schema registry needed

**Limitations:**
- No schema enforcement
- Larger message size
- No versioning support
- No backward/forward compatibility

### Phase 2: Avro (Future)

```mermaid
graph LR
    subgraph "Producer"
        DTO1[PurchaseOrderDTO] --> AS[AvroSerializer]
    end
    
    AS --> SR[Schema Registry]
    AS --> K[Kafka Topic]
    
    subgraph "Consumer"
        K --> AD[AvroDeserializer]
        AD --> SR
        AD --> DTO2[PurchaseOrderDTO]
    end
```

**Advantages:**
- Schema evolution support
- Compact binary format
- Backward/forward compatibility
- Schema validation
- Better performance

## Deployment Architecture

```mermaid
graph TB
    subgraph "Local Development"
        DC[Docker Compose]
        ZK[Zookeeper:2181]
        KF[Kafka:9092]
        DB[MySQL:3306]
    end

    subgraph "Spring Boot Applications"
        BE[Backend:8080]
        FE[Frontend:8081]
    end

    subgraph "External Access"
        BR[Browser]
    end

    DC --> ZK
    DC --> KF
    DC --> DB
    
    ZK --> KF
    BE --> KF
    BE --> DB
    FE --> KF
    FE --> BE
    BR --> FE
```

### Port Configuration
- **Frontend**: 8081 (Web UI)
- **Backend**: 8080 (REST API)
- **Kafka**: 9092
- **Zookeeper**: 2181
- **MySQL**: 3306

## Security Considerations

### Current Implementation
- **No Authentication**: Hardcoded customer ID ("CUSTOMER-001")
- **CORS Enabled**: Frontend can access backend API
- **Simplified for Workshop**: Focus on architecture patterns

### Production Recommendations
- Implement Spring Security with JWT
- Add API Gateway (Spring Cloud Gateway)
- Use OAuth2/OIDC for authentication
- Implement rate limiting
- Add SSL/TLS encryption
- Secure Kafka with SASL/SSL
- Implement audit logging

## Performance Considerations

### Optimizations Implemented
- **Connection Pooling**: HikariCP for database
- **Lazy Loading**: JPA entities use FetchType.LAZY
- **Session Management**: Lightweight cart storage
- **Index Strategy**: Primary keys on all tables

### Future Enhancements
- **Caching**: Add Spring Cache for products
- **Kafka Batch Processing**: Tune consumer batch size
- **Database Indexing**: Add indexes on frequently queried columns
- **CDN**: Offload static assets
- **Read Replicas**: Separate read/write databases

## Monitoring and Observability

### Recommended Tools
- **Metrics**: Spring Actuator + Prometheus
- **Logging**: SLF4J + Logback
- **Tracing**: Spring Cloud Sleuth + Zipkin
- **Kafka**: Kafka Manager / Confluent Control Center

### Key Metrics to Monitor
- Kafka consumer lag
- Message processing time
- Database query performance
- HTTP response times
- Error rates

## Testing Strategy

### Unit Tests (Spock Framework)
- Service layer logic
- Business rule validation
- DTO mapping
- Mock external dependencies

### Integration Tests
- Kafka producer/consumer with @EmbeddedKafka
- Repository tests with H2 or Testcontainers
- REST API tests with MockMvc

### End-to-End Tests
- Full user flows
- Cross-service integration
- Performance testing

## Future Enhancements

### Phase 2 Features
1. **Schema Registry**: Avro serialization
2. **Kafka Streams**: Real-time processing
3. **Event Store**: Full event sourcing
4. **Separate Read/Write DBs**: Complete CQRS
5. **Apache Flink**: Complex event processing
6. **Late Data Handling**: Windowing and watermarks

### Additional Capabilities
- Multiple consumer groups
- Dead letter queues
- Saga pattern for distributed transactions
- API versioning
- GraphQL endpoint
