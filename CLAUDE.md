# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot Kafka Workshop designed to teach **Event-Driven and Data Streaming Architecture** patterns. The project demonstrates practical implementations of modern distributed system concepts using Apache Kafka as the messaging backbone.

### Learning Topics

This workshop covers:
- **Event Sourcing Principles**: Building systems around immutable event logs
- **CQRS (Command Query Responsibility Segregation)**: Separating read and write operations with CommandService and QueryService patterns
- **Kafka Streams and Apache Flink**: Real-time stream processing (Phase 2)
- **Schema Evolution**: Progressive migration from JSON to Avro with Confluent Schema Registry (Phase 2)
- **Late Data Management**: Handling out-of-order events in distributed systems

### Multi-Module Structure

The project consists of three Maven modules:

- **spring-boot-kafka-common**: Shared DTOs, enums, configuration classes, and utilities used by both backend and frontend
- **spring-boot-kafka-backend**: MySQL/JPA persistence layer, REST API endpoints, Kafka consumer for purchase orders, service and repository layers implementing CQRS pattern
- **spring-boot-kafka-frontend**: Thymeleaf views with Tailwind CSS, HTTP client for backend communication, Kafka producer for asynchronous order placement

## Technology Stack

- **Spring Boot 4.0.6** with Java 25
- **Spring Kafka** for Kafka integration
- **MySQL** for persistent data storage
- **Spring Data JPA** for database access
- **Thymeleaf + Tailwind CSS** for frontend UI
- **Spock Framework** (Groovy 4.0) for testing
- **Maven** for build management (multi-module structure)
- **Docker Compose** for local infrastructure (Kafka, Zookeeper, MySQL)

## Local Development Setup

### Prerequisites
- Java 25
- Maven 3.x
- Docker and Docker Compose

### Start Infrastructure

Start MySQL, Kafka, and Zookeeper with Docker Compose:

```bash
docker-compose up -d
```

This will start:
- **MySQL** on localhost:3306 (database: workshop)
- **Zookeeper** on localhost:2181
- **Kafka** on localhost:9092

### Startup Order

1. **Infrastructure** (Docker Compose)
2. **Backend** (port 8080)
3. **Frontend** (port 8081)

### Building and Running

#### Build entire project
```bash
mvn clean install
```

#### Build specific module
```bash
cd spring-boot-kafka-backend
mvn clean install
```

#### Run backend service
```bash
cd spring-boot-kafka-backend
mvn spring-boot:run
```

Backend runs on **http://localhost:8080**

#### Run frontend service
```bash
cd spring-boot-kafka-frontend
mvn spring-boot:run
```

Frontend runs on **http://localhost:8081**

#### Run tests
```bash
# All tests
mvn test

# Single module tests
cd spring-boot-kafka-backend
mvn test

# Run specific test class (Spock)
mvn test -Dtest=ApplicationSpec
```

## Project Structure

```
SpringBootKafkaWorkshop/
├── docker-compose.yml                # Local infrastructure setup
├── pom.xml                           # Parent POM with shared dependencies
├── spring-boot-kafka-common/         # Shared DTOs, enums, utilities
│   ├── pom.xml
│   └── src/main/java/io/github/joxebus/
│       ├── dto/                      # ProductDTO, PurchaseOrderDTO, ProductOrderDTO
│       └── enums/                    # PurchaseOrderStatus
├── spring-boot-kafka-backend/        # Backend service (REST API + Kafka consumer)
│   ├── pom.xml
│   ├── AGENTS.md                     # Backend-specific implementation guidance
│   └── src/
│       ├── main/java/io/github/joxebus/
│       │   ├── entity/               # JPA entities: Product, Order, OrderItem
│       │   ├── repository/           # Spring Data JPA repositories
│       │   ├── service/              # CommandService, QueryService (CQRS)
│       │   ├── controller/           # REST controllers
│       │   ├── kafka/consumer/       # Kafka message consumers
│       │   └── config/               # Kafka, JPA configuration
│       ├── resources/application.yaml
│       └── test/groovy/              # Spock tests
└── spring-boot-kafka-frontend/       # Frontend web application
    ├── pom.xml
    ├── AGENTS.md                     # Frontend-specific implementation guidance
    └── src/
        ├── main/java/io/github/joxebus/
        │   ├── controller/           # Thymeleaf controllers
        │   ├── service/              # Backend communication service
        │   ├── kafka/producer/       # Kafka message producers
        │   └── config/               # Kafka producer, RestTemplate config
        ├── resources/
        │   ├── application.yaml
        │   ├── static/               # CSS, JS, images
        │   └── templates/            # Thymeleaf templates
        │       ├── layout/main.html  # Base layout
        │       ├── products/         # Product views
        │       │   ├── list.html
        │       │   └── form.html
        │       └── orders/           # Order views
        │           ├── purchase.html
        │           ├── list.html
        │           └── details.html
        └── test/groovy/              # Spock tests
```

## Architecture

### Communication Patterns

This workshop demonstrates both **synchronous** and **asynchronous** communication patterns:

#### Synchronous (HTTP REST)
- **Product Management**: Frontend ↔ Backend REST API
  - GET `/api/products` - List products
  - POST `/api/products` - Create product
  - PUT `/api/products/{id}` - Update product
- **Order Queries**: Frontend ↔ Backend REST API
  - GET `/api/orders` - List orders
  - GET `/api/orders/{id}` - Get order details

#### Asynchronous (Kafka)
- **Purchase Order Creation**: Frontend → Kafka → Backend
  - Frontend produces message to `purchase-orders` topic
  - Backend consumes and processes order asynchronously
  - Order persisted to MySQL database

### Teaching Point: When to Use Each Pattern

- **Synchronous REST**: Use for queries and operations requiring immediate response (product lists, order details)
- **Asynchronous Kafka**: Use for operations that can be processed independently, require high throughput, or involve multiple consumers (purchase order processing)

### CQRS Implementation

The backend implements CQRS (Command Query Responsibility Segregation) at the service layer:

- **CommandService**: Handles write operations (create/update products, process orders)
- **QueryService**: Handles read operations (list products, list orders, query order details)
- Both services use the same MySQL database (simplified CQRS for teaching)
- Future enhancement: Separate read/write databases with event-driven synchronization

### Data Flow

```
User → Frontend (Thymeleaf) → RestTemplate → Backend REST API → CommandService → Repository → MySQL
                             ↓
                          KafkaProducer → purchase-orders topic
                                                ↓
                                          KafkaConsumer (Backend)
                                                ↓
                                          CommandService → Repository → MySQL
                                                ↓
                                          QueryService ← Frontend (order status polling)
```

## Domain Model

The project uses shared DTOs from the `spring-boot-kafka-common` module:

### ProductDTO
- `id` (String/UUID): Unique product identifier
- `name` (String): Product name
- `description` (String): Product description
- `price` (Double): Product price
- `stock` (Integer): Available inventory
- `category` (String): Product category

### PurchaseOrderDTO
- `orderId` (String/UUID): Unique order identifier
- `customerId` (String): Customer identifier (hardcoded "CUSTOMER-001", no authentication)
- `products` (List<ProductOrderDTO>): List of ordered products
- `total` (Double): Total order amount
- `orderDate` (LocalDateTime): Order creation timestamp
- `status` (PurchaseOrderStatus): Order status

### ProductOrderDTO
- `productId` (String/UUID): Reference to product
- `quantity` (Integer): Quantity ordered
- `price` (Double): Unit price at time of order
- `total` (Double): Line item total

### PurchaseOrderStatus Enum
- `CREATED`: Order received
- `PENDING`: Order being processed
- `COMPLETED`: Order fulfilled
- `CANCELLED`: Order cancelled
- `SHIPPED`: Order dispatched
- `DELIVERED`: Order completed

## Database

### Configuration
- **Database**: MySQL 8.x
- **Connection**: localhost:3306/workshop
- **Schema Management**: JPA auto-ddl (`spring.jpa.hibernate.ddl-auto=update`) for workshop simplicity
- **Entities**: Product, Order, OrderItem (JPA entities in backend module)

### Entity Relationships
- **Product**: Standalone product catalog
- **Order**: Purchase order header (1:N with OrderItem)
- **OrderItem**: Order line items (N:1 with Order, references Product)

## Kafka Topics

### purchase-orders
- **Purpose**: Asynchronous purchase order processing
- **Producers**: Frontend application
- **Consumers**: Backend application
- **Message Type**: PurchaseOrderDTO
- **Serialization**: 
  - Phase 1: JSON (JsonSerializer/JsonDeserializer)
  - Phase 2: Avro with Confluent Schema Registry (for schema evolution learning)
- **Partitioning**: By customerId (future enhancement)

## Views Architecture (Frontend)

### Products Module
- **`/products`** (list.html): Display all products in a table, allow product selection for purchase
- **`/products/new`** (form.html): Create new product (UUID auto-generated)
- **`/products/{id}/edit`** (form.html): Edit existing product (ID non-editable)

### Orders Module
- **`/orders/purchase`** (purchase.html): Shopping cart showing selected products with total, submit creates Kafka message
- **`/orders`** (list.html): Display all orders with status badges, links to details
- **`/orders/{id}`** (details.html): Show order details including items, amounts, and current status

### Layout
- **`layout/main.html`**: Base Thymeleaf template with Tailwind CSS CDN, navigation menu, common header/footer

### Styling
- **Tailwind CSS**: Utility-first CSS framework via CDN
- **Color Scheme**: Blue (primary actions), Green (success), Yellow (warnings), Red (errors)
- **Components**: Cards for products, tables for listings, forms with validation

## Testing Strategy

### Framework
- **Spock Framework** 2.4-M4 with Groovy 4.0 for all tests
- Tests located in `src/test/groovy/` directories

### Backend Tests
- **Unit Tests**: Service layer logic with mocked dependencies
- **Integration Tests**: Kafka consumers with @EmbeddedKafka
- **Repository Tests**: JPA repositories with H2 in-memory database or Testcontainers
- **REST Tests**: Controller endpoints with MockMvc

### Frontend Tests
- **Unit Tests**: Service layer (backend communication)
- **Controller Tests**: Thymeleaf controllers with MockMvc
- **Template Tests**: View rendering with spring-boot-starter-thymeleaf-test

## Serialization Strategy

### Phase 1: JSON (Current)
- **Producer**: JsonSerializer
- **Consumer**: JsonDeserializer
- **Advantage**: Simple setup, easy debugging, human-readable
- **Limitation**: No schema enforcement, versioning challenges

### Phase 2: Avro + Schema Registry (Future)
- **Producer**: KafkaAvroSerializer
- **Consumer**: KafkaAvroDeserializer
- **Schema Registry**: Confluent Schema Registry
- **Benefits**: Schema evolution, backward/forward compatibility, compact binary format
- **Teaching Point**: Demonstrates proper schema management in production systems

## Customer Management

- **Approach**: Simplified hardcoded customerId
- **Default**: "CUSTOMER-001"
- **Alternative**: Session-based UUID generation
- **No Authentication**: Workshop focuses on event-driven patterns, not auth complexity

## Port Configuration

- **Backend**: 8080 (REST API)
- **Frontend**: 8081 (Web UI)
- **MySQL**: 3306
- **Kafka**: 9092
- **Zookeeper**: 2181

## Development Notes

### Package Structure
- All Java code resides under `io.github.joxebus` package
- Main application classes: `SpringBootKafka[Module]Application.java`
- Use sub-packages for organization: entity, repository, service, controller, kafka, config

### Kafka Configuration
- Kafka configuration in application.yaml of each module
- Backend: Consumer configuration (group-id, deserializers, topics)
- Frontend: Producer configuration (serializers)
- Bootstrap servers: localhost:9092

### Module-Specific Documentation
- See `spring-boot-kafka-backend/AGENTS.md` for backend implementation details
- See `spring-boot-kafka-frontend/AGENTS.md` for frontend implementation details

## Important Conventions

- This is a workshop/demo project focused on teaching Event-Driven Architecture patterns
- Keep backend and frontend concerns separated in their respective modules
- Place Kafka consumers in backend module
- Place Kafka producers in frontend module
- Share common DTOs and enums via the common module
- Use Lombok for reducing boilerplate in DTOs and entities
- Follow CQRS pattern in backend services (CommandService/QueryService)
- Write tests using Spock Framework (Groovy)

## Version management

Use git to commit changes after complete each iteration to have a history of changes, 
do not create everything in one step you must follow the next steps:

1. Analyze stage
2. Implement changes in chunks one step before passing to the next
3. After complete any stage generate a commit with the changes implemented using the following structure:
   - Use clear short commit title with format: Step # - <Changes applied>
   - Include a 1 paragraph in the body description for the changes
4. Start again
