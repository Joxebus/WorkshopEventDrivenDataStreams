# Backend Module - Implementation Guide

This file provides guidance for implementing the Spring Boot Kafka Workshop backend module.

## Purpose

The backend module serves as the persistence and business logic layer, providing:
- REST API endpoints for synchronous data access
- Kafka consumer for asynchronous purchase order processing
- MySQL database persistence with JPA
- CQRS pattern implementation (CommandService/QueryService)

## Module Responsibilities

### Core Functions
1. **Data Persistence**: Store and retrieve products and orders using MySQL/JPA
2. **REST API**: Expose HTTP endpoints for product management and order queries
3. **Event Processing**: Consume purchase order events from Kafka
4. **Business Logic**: Validate orders, manage inventory, update order status
5. **CQRS Implementation**: Separate command (write) and query (read) operations

## Package Structure

Organize code under `io.github.joxebus` with the following sub-packages:

```
io.github.joxebus/
├── SpringBootKafkaBackendApplication.java    # Main application class
├── entity/                                    # JPA entities
│   ├── Product.java                          # Product catalog entity
│   ├── Order.java                            # Purchase order header entity
│   └── OrderItem.java                        # Order line item entity
├── repository/                                # Spring Data JPA repositories
│   ├── ProductRepository.java
│   ├── OrderRepository.java
│   └── OrderItemRepository.java
├── service/                                   # Business logic (CQRS pattern)
│   ├── ProductCommandService.java            # Product write operations
│   ├── ProductQueryService.java              # Product read operations
│   ├── OrderCommandService.java              # Order write operations
│   └── OrderQueryService.java                # Order read operations
├── controller/                                # REST API controllers
│   ├── ProductController.java                # Product endpoints
│   └── OrderController.java                  # Order endpoints
├── kafka/                                     # Kafka integration
│   └── consumer/
│       └── PurchaseOrderConsumer.java        # Consumes purchase-orders topic
└── config/                                    # Configuration classes
    ├── KafkaConsumerConfig.java              # Kafka consumer configuration
    └── JpaConfig.java                        # JPA configuration (if needed)
```

## REST API Endpoints

### Product Endpoints
- `GET /api/products` - List all products (calls ProductQueryService)
- `GET /api/products/{id}` - Get product by ID (calls ProductQueryService)
- `POST /api/products` - Create new product (calls ProductCommandService)
- `PUT /api/products/{id}` - Update product (calls ProductCommandService)

### Order Endpoints
- `GET /api/orders` - List all orders (calls OrderQueryService)
- `GET /api/orders/{id}` - Get order details with items (calls OrderQueryService)

**Note**: Orders are not created via REST API - they are created asynchronously via Kafka consumer.

## Database Entities

### Product Entity
```java
@Entity
@Table(name = "products")
class Product {
    @Id
    private String id;              // UUID as String
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String category;
    // timestamps: createdAt, updatedAt
}
```

### Order Entity
```java
@Entity
@Table(name = "orders")
class Order {
    @Id
    private String id;              // UUID as String
    private String customerId;
    private Double total;
    private LocalDateTime orderDate;
    @Enumerated(EnumType.STRING)
    private PurchaseOrderStatus status;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;
}
```

### OrderItem Entity
```java
@Entity
@Table(name = "order_items")
class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    private String productId;       // Reference to Product
    private Integer quantity;
    private Double price;           // Price at time of order
    private Double subtotal;
}
```

## CQRS Implementation

### Command Services (Write Operations)
- **ProductCommandService**:
  - `createProduct(ProductDTO dto)`: Validate and save new product
  - `updateProduct(String id, ProductDTO dto)`: Update existing product
  - Generate UUID for new products
  
- **OrderCommandService**:
  - `createOrder(PurchaseOrderDTO dto)`: Process order from Kafka message
  - `updateOrderStatus(String orderId, PurchaseOrderStatus status)`: Update order status
  - Validate product availability (stock check)
  - Decrement product stock on order creation

### Query Services (Read Operations)
- **ProductQueryService**:
  - `getAllProducts()`: List all products
  - `getProductById(String id)`: Find product by ID
  
- **OrderQueryService**:
  - `getAllOrders()`: List all orders
  - `getOrderById(String id)`: Get order with items
  - `getOrdersByCustomerId(String customerId)`: Get customer orders
  - `getOrdersByStatus(PurchaseOrderStatus status)`: Filter by status

### Teaching Point
- Commands change state (write to database)
- Queries return data (read from database)
- Clear separation of responsibilities improves testability and scalability
- Future: Can separate read/write databases, materialized views

## Kafka Consumer

### Configuration
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: backend-consumer-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: io.github.joxebus.dto
```

### PurchaseOrderConsumer
```java
@Component
class PurchaseOrderConsumer {
    
    @Autowired
    private OrderCommandService orderCommandService;
    
    @KafkaListener(topics = "purchase-orders", groupId = "backend-consumer-group")
    public void consume(PurchaseOrderDTO orderDTO) {
        // Validate order
        // Process order (save to database)
        // Update order status
        orderCommandService.createOrder(orderDTO);
    }
}
```

### Processing Logic
1. Receive PurchaseOrderDTO from Kafka topic
2. Validate order data (products exist, sufficient stock)
3. Create Order entity and OrderItem entities
4. Persist to MySQL database
5. Update order status (CREATED → PENDING → COMPLETED)
6. Handle errors gracefully (mark as CANCELLED if validation fails)

## Application Configuration

### application.yaml
```yaml
spring:
  application:
    name: spring-boot-kafka-backend
  
  datasource:
    url: jdbc:mysql://localhost:3306/workshop?createDatabaseIfNotExist=true
    username: workshop
    password: workshop
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update                    # For workshop simplicity
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect
  
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: backend-consumer-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: io.github.joxebus.dto

server:
  port: 8080
```

## Dependencies Required

Add to `pom.xml`:

```xml
<dependencies>
    <!-- Existing from parent: spring-boot-starter-kafka -->
    
    <!-- Web for REST API -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- JPA for database -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- MySQL driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- JSON serialization (likely already included via spring-boot-starter-web) -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
    
    <!-- Common module with DTOs -->
    <dependency>
        <groupId>io.github.joxebus</groupId>
        <artifactId>spring-boot-kafka-common</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

## Testing Strategy

### Unit Tests (Spock Framework)
Location: `src/test/groovy/io/github/joxebus/`

**Service Tests**:
```groovy
class ProductCommandServiceSpec extends Specification {
    def "should create product with generated UUID"() {
        given:
        def repository = Mock(ProductRepository)
        def service = new ProductCommandService(repository)
        def dto = new ProductDTO(null, "Test", "Desc", 10.0, 5, "Category")
        
        when:
        def result = service.createProduct(dto)
        
        then:
        1 * repository.save(_) >> { Product p -> p }
        result.id != null
    }
}
```

### Integration Tests

**Repository Tests** (H2 or Testcontainers):
```groovy
@DataJpaTest
class ProductRepositorySpec extends Specification {
    @Autowired
    ProductRepository repository
    
    def "should save and retrieve product"() {
        given:
        def product = new Product(UUID.randomUUID().toString(), "Test", "Desc", 10.0, 5, "Cat")
        
        when:
        repository.save(product)
        def found = repository.findById(product.id)
        
        then:
        found.isPresent()
        found.get().name == "Test"
    }
}
```

**Kafka Consumer Tests** (@EmbeddedKafka):
```groovy
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = ["purchase-orders"])
class PurchaseOrderConsumerSpec extends Specification {
    @Autowired
    EmbeddedKafkaBroker embeddedKafka
    
    @Autowired
    OrderRepository orderRepository
    
    def "should consume and process purchase order"() {
        // Test Kafka message consumption
    }
}
```

**REST Controller Tests** (MockMvc):
```groovy
@WebMvcTest(ProductController)
class ProductControllerSpec extends Specification {
    @Autowired
    MockMvc mockMvc
    
    @MockBean
    ProductQueryService queryService
    
    def "GET /api/products should return product list"() {
        given:
        queryService.getAllProducts() >> [new ProductDTO("1", "Test", "Desc", 10.0, 5, "Cat")]
        
        expect:
        mockMvc.perform(get("/api/products"))
               .andExpect(status().isOk())
               .andExpect(jsonPath('$[0].name').value("Test"))
    }
}
```

## Event Sourcing Notes (Future)

### Phase 2 Enhancements
- **Event Store**: Persist domain events (OrderCreatedEvent, OrderShippedEvent, etc.)
- **Event Replay**: Rebuild order state from events
- **Kafka Streams**: Real-time order processing pipeline
- **Apache Flink**: Complex event processing (CEP) for fraud detection
- **Avro Schemas**: Migrate from JSON to Avro with Schema Registry
- **Schema Evolution**: Handle backward/forward compatibility

### Current State vs Future
- **Current**: Direct state persistence (CRUD operations)
- **Future**: Event-first persistence (event sourcing)

## Implementation Checklist

- [ ] Create JPA entities (Product, Order, OrderItem)
- [ ] Create Spring Data JPA repositories
- [ ] Implement CQRS services (Command/Query separation)
- [ ] Create REST controllers with proper HTTP methods
- [ ] Implement Kafka consumer for purchase-orders topic
- [ ] Configure application.yaml (MySQL, Kafka settings)
- [ ] Add required dependencies to pom.xml
- [ ] Write Spock tests (unit, integration, controller)
- [ ] Test end-to-end flow: REST API → Kafka → Consumer → Database
- [ ] Handle error cases (invalid product, insufficient stock)

## Error Handling

### Order Processing Errors
- **Invalid Product ID**: Mark order as CANCELLED
- **Insufficient Stock**: Mark order as CANCELLED, return stock
- **Database Errors**: Log error, retry with exponential backoff
- **Kafka Errors**: Configure retry policy and dead letter queue (DLQ)

### REST API Errors
- **404 Not Found**: Product or order doesn't exist
- **400 Bad Request**: Invalid input data
- **500 Internal Server Error**: Unexpected errors (log and return generic message)

## Performance Considerations

- **Connection Pooling**: Configure HikariCP for database connections
- **Kafka Consumer**: Tune batch size and fetch settings
- **Indexing**: Add database indexes on frequently queried columns (customerId, status, orderDate)
- **Caching**: Consider Spring Cache for frequently accessed products (future enhancement)

## Security Notes

- **SQL Injection**: Use JPA/JPQL parameterized queries (handled by Spring Data)
- **Input Validation**: Validate all DTOs with @Valid and custom validators
- **CORS**: Configure CORS for frontend communication
- **No Authentication**: Workshop simplification (no JWT/OAuth required)
