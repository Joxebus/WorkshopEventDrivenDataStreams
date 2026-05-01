# Frontend Module - Implementation Guide

This file provides guidance for implementing the Spring Boot Kafka Workshop frontend module.

## Purpose

The frontend module serves as the user interface layer, providing:
- Thymeleaf-based web views styled with Tailwind CSS
- HTTP client for synchronous backend communication
- Kafka producer for asynchronous purchase order creation
- Product management and order placement workflows

## Module Responsibilities

### Core Functions
1. **Product Views**: Display, create, and edit products
2. **Order Views**: Create purchase orders, view order list and details
3. **Backend Communication**: REST API calls for product and order data
4. **Event Production**: Send purchase orders to Kafka topic
5. **User Experience**: Clean, responsive UI with Tailwind CSS

## Package Structure

Organize code under `io.github.joxebus` with the following sub-packages:

```
io.github.joxebus/
├── SpringBootKafkaFrontendApplication.java   # Main application class
├── controller/                                # Thymeleaf controllers
│   ├── ProductController.java                # Product management views
│   ├── OrderController.java                  # Order management views
│   └── HomeController.java                   # Landing page (optional)
├── service/                                   # Business logic
│   ├── ProductService.java                   # Backend API client for products
│   └── OrderService.java                     # Backend API client for orders
├── kafka/                                     # Kafka integration
│   └── producer/
│       └── PurchaseOrderProducer.java        # Produces to purchase-orders topic
└── config/                                    # Configuration classes
    ├── KafkaProducerConfig.java              # Kafka producer configuration
    └── RestClientConfig.java                 # RestTemplate/WebClient bean
```

## Views Architecture

### Template Structure
Location: `src/main/resources/templates/`

```
templates/
├── layout/
│   └── main.html                 # Base layout with Tailwind CSS, navigation
├── products/
│   ├── list.html                 # Product list with selection
│   └── form.html                 # Create/Edit product form
├── orders/
│   ├── purchase.html             # Shopping cart / purchase order creation
│   ├── list.html                 # Orders list with status badges
│   └── details.html              # Order details view
└── index.html                    # Home page (optional)
```

### Base Layout (layout/main.html)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${title}">Spring Boot Kafka Workshop</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100">
    <nav class="bg-blue-600 text-white p-4">
        <div class="container mx-auto flex justify-between items-center">
            <a href="/" class="text-xl font-bold">Kafka Workshop</a>
            <div class="space-x-4">
                <a href="/products" class="hover:underline">Products</a>
                <a href="/orders" class="hover:underline">Orders</a>
            </div>
        </div>
    </nav>
    
    <main class="container mx-auto p-4" th:insert="~{::content}">
        <!-- Content from child templates -->
    </main>
    
    <footer class="bg-gray-800 text-white text-center p-4 mt-8">
        <p>&copy; 2026 Spring Boot Kafka Workshop</p>
    </footer>
</body>
</html>
```

### Products List (products/list.html)

Features:
- Display products in a table or card grid
- "Add to Cart" button for each product
- Link to create new product
- Link to edit existing products
- Show product stock levels

### Products Form (products/form.html)

Features:
- Create/Edit form with fields: name, description, price, stock, category
- Product ID auto-generated (UUID) for new products, non-editable for existing
- Form validation (required fields, numeric validation)
- Submit button calls backend REST API (POST/PUT)

### Purchase Order (orders/purchase.html)

Features:
- Display selected products in cart
- Show quantity selector for each item
- Calculate and display total amount
- "Confirm Order" button sends to Kafka topic
- Clear cart functionality

### Orders List (orders/list.html)

Features:
- Display all orders in a table
- Show order ID, customer ID, total, order date, status
- Status badges with colors (CREATED=blue, PENDING=yellow, COMPLETED=green, CANCELLED=red)
- "View Details" link for each order

### Order Details (orders/details.html)

Features:
- Order header (ID, customer, date, status, total)
- Order items table (product, quantity, price, subtotal)
- Status timeline/progression
- Back to orders list link

## Thymeleaf Controllers

### ProductController

```java
@Controller
@RequestMapping("/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    public String list(Model model) {
        List<ProductDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("title", "Products");
        return "products/list";
    }
    
    @GetMapping("/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new ProductDTO());
        model.addAttribute("title", "Create Product");
        model.addAttribute("isEdit", false);
        return "products/form";
    }
    
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable String id, Model model) {
        ProductDTO product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("title", "Edit Product");
        model.addAttribute("isEdit", true);
        return "products/form";
    }
    
    @PostMapping
    public String create(@ModelAttribute ProductDTO product, RedirectAttributes redirectAttributes) {
        productService.createProduct(product);
        redirectAttributes.addFlashAttribute("message", "Product created successfully");
        return "redirect:/products";
    }
    
    @PostMapping("/{id}")
    public String update(@PathVariable String id, @ModelAttribute ProductDTO product, 
                         RedirectAttributes redirectAttributes) {
        productService.updateProduct(id, product);
        redirectAttributes.addFlashAttribute("message", "Product updated successfully");
        return "redirect:/products";
    }
}
```

### OrderController

```java
@Controller
@RequestMapping("/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private PurchaseOrderProducer orderProducer;
    
    @GetMapping
    public String list(Model model) {
        List<PurchaseOrderDTO> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("title", "Orders");
        return "orders/list";
    }
    
    @GetMapping("/{id}")
    public String details(@PathVariable String id, Model model) {
        PurchaseOrderDTO order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("title", "Order Details");
        return "orders/details";
    }
    
    @GetMapping("/purchase")
    public String purchase(HttpSession session, Model model) {
        // Get selected products from session
        List<ProductOrderDTO> cartItems = (List<ProductOrderDTO>) session.getAttribute("cart");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("title", "Purchase Order");
        return "orders/purchase";
    }
    
    @PostMapping("/purchase")
    public String createPurchase(HttpSession session, RedirectAttributes redirectAttributes) {
        // Get cart from session
        List<ProductOrderDTO> cartItems = (List<ProductOrderDTO>) session.getAttribute("cart");
        
        if (cartItems == null || cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Cart is empty");
            return "redirect:/products";
        }
        
        // Create PurchaseOrderDTO
        PurchaseOrderDTO order = new PurchaseOrderDTO(
            UUID.randomUUID().toString(),
            "CUSTOMER-001",  // Hardcoded customer ID
            cartItems,
            calculateTotal(cartItems),
            LocalDateTime.now(),
            PurchaseOrderStatus.CREATED
        );
        
        // Send to Kafka
        orderProducer.sendOrder(order);
        
        // Clear cart
        session.removeAttribute("cart");
        
        redirectAttributes.addFlashAttribute("message", "Order placed successfully");
        return "redirect:/orders";
    }
}
```

## Backend Communication

### ProductService (REST Client)

```java
@Service
public class ProductService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${backend.api.url}")
    private String backendUrl;
    
    public List<ProductDTO> getAllProducts() {
        String url = backendUrl + "/products";
        ProductDTO[] products = restTemplate.getForObject(url, ProductDTO[].class);
        return Arrays.asList(products);
    }
    
    public ProductDTO getProductById(String id) {
        String url = backendUrl + "/products/" + id;
        return restTemplate.getForObject(url, ProductDTO.class);
    }
    
    public ProductDTO createProduct(ProductDTO product) {
        String url = backendUrl + "/products";
        return restTemplate.postForObject(url, product, ProductDTO.class);
    }
    
    public void updateProduct(String id, ProductDTO product) {
        String url = backendUrl + "/products/" + id;
        restTemplate.put(url, product);
    }
}
```

### OrderService (REST Client)

```java
@Service
public class OrderService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${backend.api.url}")
    private String backendUrl;
    
    public List<PurchaseOrderDTO> getAllOrders() {
        String url = backendUrl + "/orders";
        PurchaseOrderDTO[] orders = restTemplate.getForObject(url, PurchaseOrderDTO[].class);
        return Arrays.asList(orders);
    }
    
    public PurchaseOrderDTO getOrderById(String id) {
        String url = backendUrl + "/orders/" + id;
        return restTemplate.getForObject(url, PurchaseOrderDTO.class);
    }
}
```

## Kafka Producer

### Configuration
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

### PurchaseOrderProducer

```java
@Component
public class PurchaseOrderProducer {
    
    @Autowired
    private KafkaTemplate<String, PurchaseOrderDTO> kafkaTemplate;
    
    private static final String TOPIC = "purchase-orders";
    
    public void sendOrder(PurchaseOrderDTO order) {
        kafkaTemplate.send(TOPIC, order.getOrderId(), order);
    }
}
```

## Application Configuration

### application.yaml
```yaml
spring:
  application:
    name: spring-boot-kafka-frontend
  
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
  
  thymeleaf:
    cache: false                          # Disable cache in development

server:
  port: 8081

backend:
  api:
    url: http://localhost:8080/api       # Backend REST API base URL
```

## Dependencies Required

Add to `pom.xml`:

```xml
<dependencies>
    <!-- Existing from parent: spring-boot-starter-kafka -->
    
    <!-- Web for REST client -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Thymeleaf (already included from parent) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    
    <!-- WebMVC (already included from parent) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
    
    <!-- JSON serialization (likely already included via spring-boot-starter-web) -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
    
    <!-- Jackson Java 8 date/time -->
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
    </dependency>
    
    <!-- Common module with DTOs -->
    <dependency>
        <groupId>io.github.joxebus</groupId>
        <artifactId>spring-boot-kafka-common</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

## Styling with Tailwind CSS

### Utility Classes Examples

**Buttons**:
```html
<button class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
    Submit
</button>

<button class="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded">
    Add to Cart
</button>
```

**Status Badges**:
```html
<span th:if="${order.status.name() == 'CREATED'}" 
      class="px-2 py-1 bg-blue-100 text-blue-800 rounded">
    Created
</span>

<span th:if="${order.status.name() == 'COMPLETED'}" 
      class="px-2 py-1 bg-green-100 text-green-800 rounded">
    Completed
</span>
```

**Tables**:
```html
<table class="min-w-full bg-white shadow-md rounded">
    <thead class="bg-gray-200">
        <tr>
            <th class="px-4 py-2">Product</th>
            <th class="px-4 py-2">Price</th>
        </tr>
    </thead>
    <tbody>
        <tr th:each="product : ${products}" class="border-t hover:bg-gray-50">
            <td class="px-4 py-2" th:text="${product.name}">Product Name</td>
            <td class="px-4 py-2" th:text="${product.price}">$10.00</td>
        </tr>
    </tbody>
</table>
```

**Forms**:
```html
<form th:action="@{/products}" th:object="${product}" method="post" 
      class="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4">
    
    <div class="mb-4">
        <label class="block text-gray-700 text-sm font-bold mb-2" for="name">
            Product Name
        </label>
        <input th:field="*{name}" 
               class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700" 
               type="text" required>
    </div>
    
    <button type="submit" 
            class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
        Save Product
    </button>
</form>
```

## Session Management

### Shopping Cart
- Store selected products in HTTP session
- Session attribute: `cart` (List<ProductOrderDTO>)
- Add product to cart from products list
- Display cart on purchase page
- Clear cart after order submission

### Session Flow
1. User views products → selects items → added to session cart
2. User navigates to purchase page → cart retrieved from session
3. User confirms order → Kafka message sent → cart cleared from session

## Testing Strategy

### Unit Tests (Spock Framework)
Location: `src/test/groovy/io/github/joxebus/`

**Service Tests** (Mock RestTemplate):
```groovy
class ProductServiceSpec extends Specification {
    RestTemplate restTemplate = Mock()
    ProductService service = new ProductService(restTemplate: restTemplate)
    
    def "should retrieve all products from backend"() {
        given:
        def products = [new ProductDTO("1", "Test", "Desc", 10.0, 5, "Cat")] as ProductDTO[]
        restTemplate.getForObject(_, ProductDTO[].class) >> products
        
        when:
        def result = service.getAllProducts()
        
        then:
        result.size() == 1
        result[0].name == "Test"
    }
}
```

### Controller Tests (MockMvc)
```groovy
@WebMvcTest(ProductController)
class ProductControllerSpec extends Specification {
    @Autowired
    MockMvc mockMvc
    
    @MockBean
    ProductService productService
    
    def "GET /products should display product list"() {
        given:
        productService.getAllProducts() >> [new ProductDTO("1", "Test", "Desc", 10.0, 5, "Cat")]
        
        expect:
        mockMvc.perform(get("/products"))
               .andExpect(status().isOk())
               .andExpect(view().name("products/list"))
               .andExpect(model().attributeExists("products"))
    }
}
```

### Template Tests
```groovy
@SpringBootTest
class ThymeleafTemplateSpec extends Specification {
    @Autowired
    TemplateEngine templateEngine
    
    def "should render products list template"() {
        given:
        def context = new Context()
        context.setVariable("products", [new ProductDTO("1", "Test", "Desc", 10.0, 5, "Cat")])
        
        when:
        def result = templateEngine.process("products/list", context)
        
        then:
        result.contains("Test")
        result.contains("$10.0")
    }
}
```

## User Flow

### Product Management Flow
1. User navigates to `/products`
2. Frontend calls `GET http://localhost:8080/api/products`
3. Products displayed in list view
4. User clicks "Create Product" → form displayed
5. User submits form → Frontend calls `POST http://localhost:8080/api/products`
6. Redirect to products list with success message

### Purchase Order Flow
1. User selects products from list → added to session cart
2. User navigates to `/orders/purchase`
3. Cart retrieved from session and displayed
4. User confirms order → Frontend creates PurchaseOrderDTO
5. Frontend sends message to Kafka topic `purchase-orders`
6. Cart cleared from session
7. Redirect to orders list with success message
8. Backend consumes message asynchronously (separate process)
9. User can view order status on `/orders` page

## Error Handling

### Backend Communication Errors
- **Connection Refused**: Display user-friendly error (backend not running)
- **404 Not Found**: Product or order doesn't exist
- **500 Server Error**: Backend error, display generic message
- Use `try-catch` in service layer, show error in Thymeleaf with flash attributes

### Kafka Errors
- **Send Failure**: Log error, notify user order couldn't be placed
- **Broker Unavailable**: Display error message, suggest retry

### Form Validation
- Client-side validation with HTML5 attributes (required, min, max)
- Server-side validation with Spring Validation (@Valid, custom validators)
- Display validation errors in Thymeleaf forms

## Customer Management

### Simple Approach
- Hardcoded customerId: `"CUSTOMER-001"`
- No login/registration required
- All orders attributed to same customer

### Alternative: Session-Based
```java
public String getOrCreateCustomerId(HttpSession session) {
    String customerId = (String) session.getAttribute("customerId");
    if (customerId == null) {
        customerId = "CUSTOMER-" + UUID.randomUUID().toString();
        session.setAttribute("customerId", customerId);
    }
    return customerId;
}
```

## Implementation Checklist

- [ ] Create Thymeleaf controllers (Product, Order)
- [ ] Create service layer for backend communication (RestTemplate)
- [ ] Implement Kafka producer for purchase orders
- [ ] Create base layout template with Tailwind CSS
- [ ] Create product views (list, form)
- [ ] Create order views (purchase, list, details)
- [ ] Configure application.yaml (backend URL, Kafka settings)
- [ ] Add required dependencies to pom.xml
- [ ] Implement session-based cart management
- [ ] Write Spock tests (controllers, services, templates)
- [ ] Test end-to-end user flows
- [ ] Handle error cases gracefully

## Performance Considerations

- **RestTemplate Connection Pooling**: Configure connection pool size
- **Session Management**: Use sticky sessions in production or externalize session store
- **Thymeleaf Caching**: Enable in production (`spring.thymeleaf.cache=true`)
- **Static Resources**: Consider CDN for Tailwind CSS in production

## Security Notes

- **CSRF Protection**: Enabled by default in Spring Security (not needed without auth)
- **XSS Prevention**: Thymeleaf auto-escapes output (use `th:text`, not `th:utext`)
- **Input Validation**: Validate all form inputs
- **No Authentication**: Simplified for workshop (add Spring Security in production)

## UI/UX Best Practices

- **Responsive Design**: Use Tailwind's responsive utilities (`sm:`, `md:`, `lg:`)
- **Loading States**: Show spinners during async operations
- **Success/Error Messages**: Use flash attributes for user feedback
- **Breadcrumbs**: Help users navigate (Home > Products > Edit Product)
- **Confirmation Dialogs**: Confirm destructive actions (delete, clear cart)
