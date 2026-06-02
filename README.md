# Spring Boot Kafka Workshop

A hands-on workshop demonstrating **Event-Driven Architecture** and **CQRS** patterns using Spring Boot and Apache Kafka. This project teaches modern distributed system concepts through practical implementations of synchronous REST APIs and asynchronous message-driven communication.

## 🚀 Quick Start

```bash
# 1. Start infrastructure (MySQL, Kafka, Zookeeper)
docker-compose up -d

# 2. Build the project
mvn clean verify

# 3. Start backend service (in new terminal)
cd spring-boot-kafka-backend
mvn spring-boot:run

# 4. Start frontend service (in new terminal)
cd spring-boot-kafka-frontend
mvn spring-boot:run

# 5. Access the application
open http://localhost:8080
```

## 📋 Technologies

### Core Stack
- **Java 25** - Modern Java features with sdkman
- **Spring Boot 4.0.6** - Application framework
- **Maven 3.x** - Multi-module build management

### Messaging & Streaming
- **Apache Kafka** - Distributed event streaming platform
- **Spring Kafka** - Spring integration for Kafka
- **Zookeeper** - Kafka coordination service

### Persistence
- **MySQL 8.0** - Relational database
- **Spring Data JPA** - Database access layer
- **Hibernate** - ORM implementation

### Frontend
- **Thymeleaf** - Server-side template engine
- **Tailwind CSS** - Utility-first CSS framework
- **RestTemplate** - HTTP client for backend communication

### Testing
- **Spock Framework 2.4-M7** - BDD-style testing
- **Groovy 5.0.0-alpha-11** - Test DSL (Java 25 compatible)
- **JUnit Platform** - Test execution

### Infrastructure
- **Docker Compose** - Local development environment
- **Maven Multi-Module** - Modular project structure

## 📚 Prerequisites

### Required Software

```bash
# Java 25 (via sdkman)
sdk install java 25.0.3-amzn
sdk use java 25.0.3-amzn
java -version  # Verify installation

# Maven 3.x
mvn -version

# Docker & Docker Compose
docker --version
docker-compose --version

# Git
git --version
```

### Platform Notes

- **Apple Silicon (M1/M2/M3)**: Docker Compose is configured with `platform: linux/arm64` for MySQL to run natively on ARM architecture
- **Intel/AMD (x86_64)**: MySQL runs with `linux/amd64` platform - update `docker-compose.yml` if needed
- Kafka and Zookeeper images are multi-platform and work on both architectures

## 🏗️ Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                            USER                                      │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    FRONTEND (Port 8080)                              │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │  Thymeleaf Views │  │  REST Client     │  │  Kafka Producer  │  │
│  │  (Tailwind CSS)  │  │  (RestTemplate)  │  │                  │  │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘  │
└─────────┬──────────────────────┬─────────────────────┬──────────────┘
          │                      │                     │
          │ HTTP GET/POST        │                     │ Kafka Message
          │                      │                     │
          ▼                      ▼                     ▼
┌────────────────────┐  ┌─────────────────────────────────────────────┐
│   BACKEND API      │  │        KAFKA (Port 9092)                    │
│   (Port 8081)      │  │  ┌─────────────────────────────────────┐   │
│                    │  │  │  Topic: purchase-orders             │   │
│  ┌──────────────┐  │  │  │  Format: JSON (PurchaseOrderDTO)    │   │
│  │ REST APIs    │  │  │  └─────────────────────────────────────┘   │
│  │ /api/products│  │  └───────────────────┬─────────────────────────┘
│  │ /api/orders  │  │                      │
│  └──────────────┘  │                      │ Kafka Consumer
│                    │                      │
│  ┌──────────────┐  │                      ▼
│  │   CQRS       │◄─┼──────────────────────┘
│  │ CommandSvc   │  │
│  │ QuerySvc     │  │
│  └──────────────┘  │
│                    │
│  ┌──────────────┐  │
│  │ Repositories │  │
│  │ (JPA/MySQL)  │  │
│  └──────────────┘  │
└─────────┬──────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    MYSQL (Port 3306)                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │   Product    │  │    Order     │  │  OrderItem   │              │
│  │  (Catalog)   │  │   (Header)   │  │  (Details)   │              │
│  └──────────────┘  └──────────────┘  └──────────────┘              │
└─────────────────────────────────────────────────────────────────────┘
```

### Communication Patterns

This workshop demonstrates **two complementary communication patterns**:

#### 🔄 Synchronous (HTTP REST)
**Use for**: Queries and operations requiring immediate response

- **Product Management**
    - `GET /api/products` - List all products
    - `POST /api/products` - Create new product
    - `PUT /api/products/{id}` - Update product
    - `DELETE /api/products/{id}` - Delete product

- **Order Queries**
    - `GET /api/orders` - List all orders
    - `GET /api/orders/{id}` - Get order details
    - `GET /api/orders?status=COMPLETED` - Filter by status

#### ⚡ Asynchronous (Kafka)
**Use for**: Operations processed independently, high throughput, multiple consumers

- **Purchase Order Creation**
    1. Frontend produces message to `purchase-orders` topic
    2. User receives immediate acknowledgment (fire-and-forget)
    3. Backend consumes message asynchronously
    4. Order persisted to MySQL database
    5. Frontend polls for order status updates

**Key Learning**: Asynchronous patterns decouple services, improve scalability, and enable event-driven architectures.

### CQRS Implementation

The backend implements **CQRS (Command Query Responsibility Segregation)** at the service layer:

```
┌─────────────────────────────────────────────────────────────┐
│                    BACKEND SERVICES                          │
│                                                              │
│  ┌─────────────────────────┐  ┌─────────────────────────┐  │
│  │   CommandService        │  │    QueryService         │  │
│  │   (Write Operations)    │  │    (Read Operations)    │  │
│  │                         │  │                         │  │
│  │ • createProduct()       │  │ • getAllProducts()      │  │
│  │ • updateProduct()       │  │ • getProductById()      │  │
│  │ • deleteProduct()       │  │ • searchProducts()      │  │
│  │ • processOrder()        │  │ • getAllOrders()        │  │
│  │                         │  │ • getOrderById()        │  │
│  └───────────┬─────────────┘  └───────────┬─────────────┘  │
│              │                            │                 │
│              └────────────┬───────────────┘                 │
│                           ▼                                 │
│                  ┌─────────────────┐                        │
│                  │  Repositories   │                        │
│                  │    (JPA/MySQL)  │                        │
│                  └─────────────────┘                        │
└─────────────────────────────────────────────────────────────┘
```

**Teaching Point**: CQRS separates read and write concerns, allowing independent optimization and scaling. This workshop uses a simplified CQRS with shared database for learning; production systems often use separate read/write databases with event-driven synchronization.

### Multi-Module Structure

```
SpringBootKafkaWorkshop/
├── spring-boot-kafka-common/       # Shared module
│   ├── dto/                        # Data Transfer Objects
│   │   ├── ProductDTO
│   │   ├── PurchaseOrderDTO
│   │   └── ProductOrderDTO
│   └── enums/
│       └── PurchaseOrderStatus
│
├── spring-boot-kafka-backend/      # Backend service
│   ├── entity/                     # JPA Entities
│   ├── repository/                 # Spring Data repositories
│   ├── service/                    # CommandService, QueryService
│   ├── controller/                 # REST controllers
│   ├── kafka/consumer/             # Kafka message consumers
│   └── config/                     # Configuration classes
│
└── spring-boot-kafka-frontend/     # Frontend service
    ├── controller/                 # Thymeleaf controllers
    ├── service/                    # Backend REST client
    ├── kafka/producer/             # Kafka message producers
    ├── config/                     # Configuration classes
    └── templates/                  # Thymeleaf views
        ├── products/               # Product management views
        └── orders/                 # Order management views
```

## 🔨 Building the Project

```bash
# Build entire project (all three modules)
mvn clean verify

# Build specific module
cd spring-boot-kafka-backend
mvn clean verify

# Skip tests for faster build
mvn clean verify -DskipTests

# Build and install to local Maven repository
mvn clean install
```

## 🐳 Starting Infrastructure

### Docker Compose Services

This project uses Docker Compose to run local infrastructure:

- **MySQL 8.0**: Relational database on port 3306 (database: `workshop`)
- **Apache Kafka**: Message broker on port 9092
- **Zookeeper**: Kafka coordination service on port 2181

All services include health checks to ensure proper startup order:
- **Zookeeper**: Health check via netcat on port 2181
- **Kafka**: Waits for Zookeeper, then checks broker API
- **MySQL**: Health check via mysqladmin ping

### Start Services

```bash
# Start all services in background
docker-compose up -d

# View service status
docker-compose ps

# Check service health
docker-compose ps --format "table {{.Service}}\t{{.State}}\t{{.Status}}"

# View logs for all services
docker-compose logs -f

# View logs for specific service
docker-compose logs -f kafka
docker-compose logs -f mysql
docker-compose logs -f zookeeper
```

### Stop Services

```bash
# Stop services (data persists)
docker-compose down

# Stop services and remove volumes (clean slate)
docker-compose down -v
```

### Platform Configuration

**Apple Silicon (M1/M2/M3)**: MySQL is configured with `platform: linux/arm64` in `docker-compose.yml`:

```yaml
mysql:
  image: mysql:8.0
  platform: linux/arm64
```

**Intel/AMD**: Change to `platform: linux/amd64` if needed.

## 🌐 Running the Application

### Startup Order

1. **Infrastructure** (Docker Compose)
2. **Backend** (port 8081)
3. **Frontend** (port 8080)

### Backend Service

```bash
cd spring-boot-kafka-backend
mvn spring-boot:run
```

Backend will start on **http://localhost:8081** with:
- REST API endpoints at `/api/products` and `/api/orders`
- Kafka consumer listening to `purchase-orders` topic
- JPA connection to MySQL database

### Frontend Service

```bash
cd spring-boot-kafka-frontend
mvn spring-boot:run
```

Frontend will start on **http://localhost:8080** with:
- Web UI for product and order management
- HTTP client for backend API calls
- Kafka producer for purchase orders

## 📊 Access Points

| Service | URL | Description |
|---------|-----|-------------|
| **Frontend** | http://localhost:8080 | Web UI (Thymeleaf + Tailwind CSS) |
| **Backend API** | http://localhost:8081 | REST API endpoints |
| **MySQL** | localhost:3306 | Database (user: `root`, password: `admin`, database: `workshop`) |
| **Kafka** | localhost:9092 | Message broker |
| **Zookeeper** | localhost:2181 | Kafka coordination |

## 🗄️ Database Configuration

### Connection Details
- **Database**: MySQL 8.x
- **Host**: localhost:3306
- **Database Name**: workshop
- **Username**: root
- **Password**: admin
- **Schema Management**: JPA auto-ddl (`spring.jpa.hibernate.ddl-auto=update`)

### Entity Relationships

```
┌─────────────────┐
│    Product      │
│─────────────────│
│ id (UUID)       │
│ name            │
│ description     │
│ price           │
│ stock           │
│ category        │
└─────────────────┘

┌─────────────────┐          ┌─────────────────┐
│     Order       │          │   OrderItem     │
│─────────────────│          │─────────────────│
│ id (UUID)       │◄────────┤│ id (Long)       │
│ customerId      │         1│ orderId (FK)    │
│ total           │          │ productId (FK)  │
│ orderDate       │          │ quantity        │
│ status          │          │ price           │
└─────────────────┘          │ total           │
                             └─────────────────┘
                                      │
                                      │ N:1
                                      ▼
                             ┌─────────────────┐
                             │    Product      │
                             └─────────────────┘
```

**Entities**:
- **Product**: Standalone product catalog
- **Order**: Purchase order header (1:N with OrderItem)
- **OrderItem**: Order line items (N:1 with Order, references Product)

## 🔗 API Endpoints

### Products API

```bash
# Get all products
curl http://localhost:8081/api/products

# Create new product
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99,
    "stock": 10,
    "category": "Electronics"
  }'

# Update product
curl -X PUT http://localhost:8081/api/products/{productId} \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Laptop",
    "description": "High-end gaming laptop",
    "price": 1299.99,
    "stock": 5,
    "category": "Electronics"
  }'

# Delete product
curl -X DELETE http://localhost:8081/api/products/{productId}

# Filter by category
curl "http://localhost:8081/api/products?category=Electronics"
```

### Orders API

```bash
# Get all orders
curl http://localhost:8081/api/orders

# Get order by ID
curl http://localhost:8081/api/orders/{orderId}

# Filter by customer
curl "http://localhost:8081/api/orders?customerId=CUSTOMER-001"

# Filter by status
curl "http://localhost:8081/api/orders?status=COMPLETED"
```

### Kafka Topic: purchase-orders

**Topic Configuration**:
- **Name**: `purchase-orders`
- **Producers**: Frontend application (PurchaseOrderProducer)
- **Consumers**: Backend application (PurchaseOrderConsumer)
- **Message Type**: PurchaseOrderDTO
- **Serialization**: JSON (Phase 1) → Avro with Schema Registry (Phase 2)
- **Partitioning**: By customerId (future enhancement)

**Message Format** (JSON):
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUSTOMER-001",
  "products": [
    {
      "productId": "123e4567-e89b-12d3-a456-426614174000",
      "quantity": 2,
      "price": 999.99,
      "total": 1999.98
    }
  ],
  "total": 1999.98,
  "orderDate": "2026-06-01T10:30:00",
  "status": "CREATED"
}
```

## 🔍 Domain Model

### ProductDTO
- `id` (String/UUID): Unique product identifier
- `name` (String): Product name
- `description` (String): Product description
- `price` (Double): Product price
- `stock` (Integer): Available inventory
- `category` (String): Product category

### PurchaseOrderDTO
- `orderId` (String/UUID): Unique order identifier
- `customerId` (String): Customer identifier (hardcoded "CUSTOMER-001")
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

**See [ARCHITECTURE.md](dev-docs/ARCHITECTURE.md) for detailed ER diagrams and data flow.**

## 🧪 Testing

### Test Framework

This project uses **Spock Framework 2.4-M7** with **Groovy 5.0.0-alpha-11** for all tests:
- Expressive Given-When-Then BDD syntax
- Built-in mocking with `Mock()`, `Stub()`, and interaction verification
- Java 25 compatible (requires Groovy 5.0+)

### Running Tests

```bash
# Run all tests (backend + frontend)
mvn test

# Run backend tests only
cd spring-boot-kafka-backend && mvn test

# Run frontend tests only
cd spring-boot-kafka-frontend && mvn test

# Run specific test class
mvn test -Dtest=ProductCommandServiceSpec

# Run with verbose output
mvn test -X

# Skip tests during build
mvn clean verify -DskipTests
```

### Test Coverage

| Module | Tests | Coverage |
|--------|-------|----------|
| **Backend** | 27 tests | Service layer (CQRS implementation) |
| **Frontend** | 12 tests | Service layer, Kafka producer |
| **Total** | **39 tests** | Unit tests with Spock Framework |

**Backend Tests**:
- `ProductCommandServiceSpec`: Product creation, update, deletion, stock management (7 tests)
- `ProductQueryServiceSpec`: Product queries and searches (7 tests)
- `OrderCommandServiceSpec`: Order creation and processing (5 tests)
- `OrderQueryServiceSpec`: Order queries by ID, customer, status (7 tests)
- `ApplicationSpec`: Application context validation (1 test)

**Frontend Tests**:
- `ProductServiceSpec`: REST client operations (6 tests)
- `OrderServiceSpec`: Order queries via REST (3 tests)
- `PurchaseOrderProducerSpec`: Kafka message publishing (2 tests)
- `ApplicationSpec`: Application context validation (1 test)

## 🛠️ Troubleshooting

### Common Issues

#### 1. Java Version Mismatch
**Error**: `Unsupported class file major version 69`

**Solution**:
```bash
java -version  # Should show Java 25
sdk use java 25.0.3-amzn
```

#### 2. Docker Containers Not Starting
**Error**: Containers stuck in "starting" state

**Solution**:
```bash
docker-compose ps  # Check service status
docker-compose logs -f mysql  # Check logs
docker-compose down -v && docker-compose up -d  # Clean restart
```

#### 3. Port Conflicts
**Error**: `Address already in use`

**Solution**:
```bash
# Check what's using the ports
lsof -i :8080  # Frontend
lsof -i :8081  # Backend
lsof -i :3306  # MySQL
lsof -i :9092  # Kafka

# Kill conflicting process
kill -9 <PID>
```

#### 4. Groovy Bytecode Errors
**Error**: `Unsupported class file major version 69`

**Cause**: Groovy 4.0.x does not support Java 25 bytecode

**Solution**: This project uses Groovy 5.0.0-alpha-11 (Java 25 compatible). Verify in `pom.xml`:
```xml
<groovy.version>5.0.0-alpha-11</groovy.version>
<spock.version>2.4-M7-groovy-5.0</spock.version>
```

#### 5. Kafka Connection Errors
**Error**: `Failed to update metadata after 60000 ms`

**Solution**:
```bash
# Verify Kafka is healthy
docker-compose ps kafka

# Check Kafka logs
docker-compose logs -f kafka

# Restart Kafka if needed
docker-compose restart kafka

# Wait for Kafka to be ready before starting apps
```

#### 6. MySQL Connection Refused
**Error**: `Communications link failure`

**Solution**:
```bash
# Check MySQL status
docker-compose ps mysql

# Verify MySQL is accepting connections
docker exec -it springbootkafkaworkshop-mysql-1 mysql -uroot -padmin -e "SELECT 1"

# Restart MySQL if needed
docker-compose restart mysql
```

**For comprehensive troubleshooting, see [SETUP.md](dev-docs/SETUP.md).**

## 📊 Learning Topics

This workshop progressively teaches modern distributed system patterns:

- ✅ **Event-Driven Architecture**: Building systems around events and message streams
- ✅ **CQRS Pattern**: Command Query Responsibility Segregation for scalable services
- ✅ **Apache Kafka Integration**: Producer-consumer patterns with Spring Kafka
- ✅ **Synchronous vs Asynchronous Communication**: REST APIs vs message-driven
- ✅ **Domain-Driven Design**: DTOs, Entities, Services, Repositories
- ✅ **Multi-Module Maven Projects**: Shared dependencies and module isolation
- ✅ **Docker Compose**: Local development environment orchestration
- 🔄 **Kafka Streams** (Phase 2): Real-time stream processing
- 🔄 **Schema Evolution with Avro** (Phase 2): Confluent Schema Registry integration
- 🔄 **Apache Flink Integration** (Phase 2): Advanced stream processing
- 🔄 **Late Data Management** (Phase 2): Handling out-of-order events

## 🔄 Development Workflow

1. **Start infrastructure**
   ```bash
   docker-compose up -d
   ```

2. **Verify services are healthy**
   ```bash
   docker-compose ps
   ```

3. **Run backend service** (in new terminal)
   ```bash
   cd spring-boot-kafka-backend
   mvn spring-boot:run
   ```

4. **Run frontend service** (in new terminal)
   ```bash
   cd spring-boot-kafka-frontend
   mvn spring-boot:run
   ```

5. **Access web UI**
   ```bash
   open http://localhost:8080
   ```

6. **Make code changes** (hot reload enabled with Spring Boot DevTools)

7. **Run tests**
   ```bash
   mvn test
   ```

8. **Create git commit** (follow convention)
   ```bash
   git add .
   git commit -m "Step # - <Changes applied>

   Brief description of changes in 1-2 paragraphs."
   ```

## 📖 Documentation

For detailed information, refer to the comprehensive documentation in `dev-docs/`:

| Document | Description | Lines | Status |
|----------|-------------|-------|--------|
| [SETUP.md](dev-docs/SETUP.md) | Installation & configuration guide with troubleshooting | ~500 | ✅ |
| [ARCHITECTURE.md](dev-docs/ARCHITECTURE.md) | System architecture, design patterns, data flow diagrams | ~800 | ✅ |
| [API.md](dev-docs/API.md) | REST API reference with curl examples | ~300 | ✅ |
| [dev-docs/README.md](dev-docs/README.md) | Documentation index and quick reference | ~200 | ✅ |
| [CLAUDE.md](CLAUDE.md) | Development guidelines and conventions | ~800 | ✅ |

## 🤝 Contributing

### Development Guidelines

- **Follow CQRS pattern**: Use CommandService for writes, QueryService for reads
- **Module separation**: Keep backend and frontend concerns in respective modules
- **Testing**: Write tests using Spock Framework (Groovy 5.0.0-alpha-11)
- **Shared code**: Place common DTOs and enums in `spring-boot-kafka-common` module
- **Git commits**: Use convention: `Step # - <Changes applied>`

### Commit Convention

```bash
# Format
Step # - <Short description of changes>

<1-2 paragraphs describing what was changed and why>

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
```

### Code Style

- Use Lombok for reducing boilerplate in DTOs and entities
- Follow Spring Boot best practices and conventions
- Keep Kafka consumers in backend module
- Keep Kafka producers in frontend module
- Write descriptive test names with Given-When-Then structure

**See [CLAUDE.md](CLAUDE.md) for detailed development conventions.**

## 📝 License

This is an educational/workshop project. Free to use and modify for learning purposes.

## 🙋 Support

- **Issues**: Report issues on GitHub
- **Documentation**: Refer to `dev-docs/` for detailed guides
- **Troubleshooting**: Check [SETUP.md](dev-docs/SETUP.md) troubleshooting section
- **Architecture**: See [ARCHITECTURE.md](dev-docs/ARCHITECTURE.md) for design patterns
- **API Reference**: Consult [API.md](dev-docs/API.md) for endpoint details

---

Built with ❤️ using Spring Boot, Apache Kafka, and Event-Driven Architecture principles
