# Setup and Installation Guide

## Prerequisites

### Required Software
- **Java 25** (Amazon Corretto via sdkman)
- **Maven 3.x**
- **Docker** and **Docker Compose**
- **Git**

### Optional Tools
- **IntelliJ IDEA** or **VS Code** (with Java extensions)
- **Postman** or **curl** for API testing
- **Kafka Manager** or **Confluent Control Center** for Kafka monitoring

---

## Installation Steps

### 1. Install Java 25 with sdkman

```bash
# Install sdkman if not already installed
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 25 from Amazon Corretto
sdk install java 25.0.3-amzn

# Set as default
sdk default java 25.0.3-amzn

# Verify installation
java -version
```

Expected output:
```
openjdk version "25.0.3" 2026-04-21 LTS
OpenJDK Runtime Environment Corretto-25.0.3.9.1 (build 25.0.3+9-LTS)
```

### 2. Clone the Repository

```bash
git clone <repository-url>
cd SpringBootKafkaWorkshop
```

### 3. Build the Project

```bash
mvn clean verify
```

This will:
- Compile all modules (common, backend, frontend)
- Run Lombok annotation processors
- Create executable JAR/WAR files
- Install artifacts to local Maven repository

---

## Running the Application

### Option 1: Full Stack with Docker Compose

#### Step 1: Start Infrastructure
```bash
docker-compose up -d
```

This starts:
- **Zookeeper** on port 2181
- **Kafka** on port 9092
- **MySQL** on port 3306

Verify services are running:
```bash
docker-compose ps
```

#### Step 2: Start Backend
```bash
cd spring-boot-kafka-backend
mvn spring-boot:run
```

The backend will:
- Connect to MySQL (auto-create tables)
- Connect to Kafka broker
- Start REST API on http://localhost:8080
- Begin listening for messages on `purchase-orders` topic

#### Step 3: Start Frontend
```bash
# In a new terminal
cd spring-boot-kafka-frontend
mvn spring-boot:run
```

The frontend will:
- Start Thymeleaf server on http://localhost:8081
- Connect to backend REST API
- Initialize Kafka producer

#### Step 4: Access the Application
Open your browser and navigate to:
```
http://localhost:8081
```

---

### Option 2: Run from JAR Files

#### Build JAR files
```bash
mvn clean package
```

#### Run Backend
```bash
java -jar spring-boot-kafka-backend/target/spring-boot-kafka-backend-1.0-SNAPSHOT.jar
```

#### Run Frontend
```bash
java -jar spring-boot-kafka-frontend/target/spring-boot-kafka-frontend-1.0-SNAPSHOT.war
```

---

### Option 3: IDE Configuration

#### IntelliJ IDEA

1. **Import Project**
   - Open IntelliJ IDEA
   - File → Open → Select `pom.xml` in root directory
   - Import as Maven project

2. **Configure Java 25**
   - File → Project Structure → Project
   - Set SDK to Java 25 (Amazon Corretto)
   - Set Language Level to 25

3. **Enable Lombok**
   - Settings → Plugins → Install "Lombok Plugin"
   - Settings → Build → Compiler → Annotation Processors
   - Enable "Enable annotation processing"

4. **Run Backend**
   - Navigate to `SpringBootKafkaBackendApplication.java`
   - Right-click → Run

5. **Run Frontend**
   - Navigate to `SpringBootKafkaFrontendApplication.java`
   - Right-click → Run

---

## Configuration

### Backend Configuration
**File**: `spring-boot-kafka-backend/src/main/resources/application.yaml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/workshop?createDatabaseIfNotExist=true
    username: workshop
    password: workshop
  
  jpa:
    hibernate:
      ddl-auto: update
  
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: backend-consumer-group

server:
  port: 8080
```

### Frontend Configuration
**File**: `spring-boot-kafka-frontend/src/main/resources/application.yaml`

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092

server:
  port: 8081

backend:
  api:
    url: http://localhost:8080/api
```

---

## Database Setup

### MySQL via Docker Compose
The docker-compose.yml automatically creates:
- Database: `workshop`
- User: `workshop`
- Password: `workshop`

### Manual MySQL Setup (Alternative)
```sql
CREATE DATABASE workshop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'workshop'@'localhost' IDENTIFIED BY 'workshop';
GRANT ALL PRIVILEGES ON workshop.* TO 'workshop'@'localhost';
FLUSH PRIVILEGES;
```

### Database Schema
Tables are auto-created by JPA on first run:
- `products` - Product catalog
- `orders` - Purchase orders
- `order_items` - Order line items

---

## Kafka Setup

### Using Docker Compose (Recommended)
The provided docker-compose.yml includes:
- Kafka broker on localhost:9092
- Zookeeper on localhost:2181
- Auto-creation enabled for topics

### Manual Kafka Setup (Alternative)

#### Download and Start Kafka
```bash
# Download Kafka
wget https://downloads.apache.org/kafka/3.6.0/kafka_2.13-3.6.0.tgz
tar -xzf kafka_2.13-3.6.0.tgz
cd kafka_2.13-3.6.0

# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka
bin/kafka-server-start.sh config/server.properties
```

#### Create Topic Manually
```bash
bin/kafka-topics.sh --create \
  --topic purchase-orders \
  --bootstrap-server localhost:9092 \
  --partitions 1 \
  --replication-factor 1
```

#### Verify Topic
```bash
bin/kafka-topics.sh --list --bootstrap-server localhost:9092
```

---

## Verification Steps

### 1. Check Docker Services
```bash
docker-compose ps
```

Expected output:
```
NAME                  STATUS         PORTS
workshop-kafka        Up             0.0.0.0:9092->9092/tcp
workshop-mysql        Up             0.0.0.0:3306->3306/tcp
workshop-zookeeper    Up             0.0.0.0:2181->2181/tcp
```

### 2. Check Backend API
```bash
curl http://localhost:8080/api/products
```

Expected: `[]` (empty array initially)

### 3. Check Frontend
Open browser: http://localhost:8081

Expected: Home page with product and order links

### 4. Monitor Kafka Messages
```bash
# Using Kafka console consumer
docker exec -it workshop-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic purchase-orders \
  --from-beginning
```

---

## Troubleshooting

### Port Already in Use
```bash
# Check what's using the port
lsof -i :8080  # or :8081, :9092, :3306

# Kill the process
kill -9 <PID>
```

### Docker Compose Issues
```bash
# Stop and remove containers
docker-compose down

# Remove volumes (clean start)
docker-compose down -v

# Rebuild and start
docker-compose up -d --build
```

### Build Errors
```bash
# Clean and rebuild
mvn clean verify -U

# Skip tests
mvn clean verify -DskipTests

# Check Java version
java -version  # Should be 25.x.x
```

### Lombok Not Working
```bash
# Ensure annotation processing is enabled
mvn clean compile

# In IDE, enable annotation processing:
# IntelliJ: Settings → Compiler → Annotation Processors
# Eclipse: Project Properties → Java Compiler → Annotation Processing
```

### Database Connection Issues
```bash
# Check MySQL is running
docker ps | grep mysql

# Check connection
mysql -h localhost -u workshop -p workshop

# Reset database
docker-compose down -v
docker-compose up -d
```

### Kafka Connection Issues
```bash
# Check Kafka is running
docker ps | grep kafka

# Check Kafka logs
docker logs workshop-kafka

# Test connectivity
telnet localhost 9092
```

---

## Development Workflow

### Hot Reload (Dev Mode)

#### Backend
```bash
cd spring-boot-kafka-backend
mvn spring-boot:run
```

Changes to Java files require restart (or use Spring DevTools).

#### Frontend
```bash
cd spring-boot-kafka-frontend
mvn spring-boot:run -Dspring.thymeleaf.cache=false
```

Thymeleaf templates reload automatically when `cache=false`.

### Running Tests
```bash
# All tests
mvn test

# Specific module
cd spring-boot-kafka-backend
mvn test

# Single test
mvn test -Dtest=ProductCommandServiceSpec
```

### Code Quality
```bash
# Format code
mvn fmt:format

# Check style
mvn checkstyle:check

# Find bugs
mvn spotbugs:check
```

---

## Production Deployment

### Build Production JAR
```bash
mvn clean package -Pprod -DskipTests
```

### Docker Image
```dockerfile
FROM amazoncorretto:25
COPY target/spring-boot-kafka-backend-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Environment Variables
```bash
# Backend
SPRING_DATASOURCE_URL=jdbc:mysql://prod-db:3306/workshop
SPRING_KAFKA_BOOTSTRAP_SERVERS=prod-kafka:9092

# Frontend
BACKEND_API_URL=https://api.prod.example.com/api
SPRING_KAFKA_BOOTSTRAP_SERVERS=prod-kafka:9092
```

### Health Checks
```bash
# Backend
curl http://localhost:8080/actuator/health

# Frontend
curl http://localhost:8081/actuator/health
```

---

## Next Steps

After successful setup:
1. Create sample products via frontend UI
2. Place test orders
3. Monitor Kafka messages
4. Check database tables
5. Review application logs
6. Explore REST API endpoints

For more information, see:
- [ARCHITECTURE.md](ARCHITECTURE.md) - System architecture and patterns
- [API.md](API.md) - REST API documentation
- [CLAUDE.md](../CLAUDE.md) - Development guidelines
