# Spring Boot Kafka Workshop - Documentation

Welcome to the Spring Boot Kafka Workshop documentation. This workshop demonstrates Event-Driven Architecture patterns using Spring Boot and Apache Kafka.

## 📚 Documentation Index

### Getting Started
- **[SETUP.md](SETUP.md)** - Installation and configuration guide
  - Prerequisites and requirements
  - Step-by-step installation
  - Running the application
  - Troubleshooting

### Architecture
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - System architecture and design patterns
  - System overview with diagrams
  - Communication patterns (Sync vs Async)
  - CQRS implementation
  - Data flow diagrams
  - Domain model (Entity-Relationship)
  - Technology stack
  - Security and performance considerations

### API Reference
- **[API.md](API.md)** - REST API documentation
  - Products endpoints
  - Orders endpoints
  - Error responses
  - Testing with cURL
  - Kafka message formats

## 🎯 Quick Start

```bash
# 1. Start infrastructure
docker-compose up -d

# 2. Build project
mvn clean verify

# 3. Start backend
cd spring-boot-kafka-backend && mvn spring-boot:run

# 4. Start frontend (in new terminal)
cd spring-boot-kafka-frontend && mvn spring-boot:run

# 5. Open browser
open http://localhost:8081
```

## 🏗️ Project Structure

```
SpringBootKafkaWorkshop/
├── docs/                                  # Documentation
│   ├── README.md                         # This file
│   ├── SETUP.md                          # Installation guide
│   ├── ARCHITECTURE.md                   # Architecture documentation
│   └── API.md                            # REST API reference
├── spring-boot-kafka-common/             # Shared DTOs and enums
├── spring-boot-kafka-backend/            # Backend service
│   ├── AGENTS.md                         # Backend implementation guide
│   └── src/
│       ├── main/java/.../
│       │   ├── entity/                   # JPA entities
│       │   ├── repository/               # Data access
│       │   ├── service/                  # Business logic (CQRS)
│       │   ├── controller/               # REST controllers
│       │   └── kafka/consumer/           # Kafka consumers
│       └── resources/
│           └── application.yaml          # Backend configuration
├── spring-boot-kafka-frontend/           # Frontend service
│   ├── AGENTS.md                         # Frontend implementation guide
│   └── src/
│       ├── main/java/.../
│       │   ├── controller/               # Thymeleaf controllers
│       │   ├── service/                  # Backend communication
│       │   └── kafka/producer/           # Kafka producers
│       └── resources/
│           ├── templates/                # Thymeleaf views
│           └── application.yaml          # Frontend configuration
├── docker-compose.yml                    # Infrastructure setup
├── CLAUDE.md                             # Development guidelines
└── pom.xml                               # Maven parent POM
```

## 🎓 Learning Topics

This workshop covers:

### Event-Driven Architecture
- **Event Sourcing** - Building systems around immutable events
- **CQRS Pattern** - Separating read and write operations
- **Asynchronous Messaging** - Kafka producers and consumers
- **Message Serialization** - JSON (Phase 1) and Avro (Phase 2)

### Spring Boot Integration
- **Spring Kafka** - Producer and consumer configuration
- **Spring Data JPA** - Database persistence
- **Spring Web** - REST API development
- **Thymeleaf** - Server-side templating

### Best Practices
- Multi-module Maven project structure
- CQRS service layer separation
- RESTful API design
- Domain-driven design patterns
- Docker Compose for local development

## 🔄 Data Flow

### Synchronous Flow (REST API)
```
User → Frontend → REST API → Backend → Database → Response
```
**Use Case**: Product listings, order queries

### Asynchronous Flow (Kafka)
```
User → Frontend → Kafka Producer → Topic → Consumer → Backend → Database
```
**Use Case**: Purchase order processing

## 🛠️ Technology Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Spring Boot 4.0.6, Java 25 |
| **Frontend** | Thymeleaf, Tailwind CSS |
| **Database** | MySQL 8.x |
| **Messaging** | Apache Kafka 3.x |
| **Build** | Maven 3.x |
| **Testing** | Spock Framework (Groovy) |
| **Infrastructure** | Docker Compose |

## 📊 System Ports

| Service | Port | URL |
|---------|------|-----|
| Frontend | 8081 | http://localhost:8081 |
| Backend API | 8080 | http://localhost:8080/api |
| MySQL | 3306 | jdbc:mysql://localhost:3306/workshop |
| Kafka | 9092 | localhost:9092 |
| Zookeeper | 2181 | localhost:2181 |

## 🔐 Security Note

**⚠️ Workshop Environment**: This project uses simplified security for educational purposes:
- No authentication (hardcoded customer ID)
- CORS enabled for localhost
- Simple error handling

**For Production**: Implement proper authentication, authorization, encryption, and monitoring.

## 📖 Additional Resources

### Project Documentation
- [CLAUDE.md](../CLAUDE.md) - Development guidelines and conventions
- [Backend AGENTS.md](../spring-boot-kafka-backend/AGENTS.md) - Backend implementation guide
- [Frontend AGENTS.md](../spring-boot-kafka-frontend/AGENTS.md) - Frontend implementation guide

### External Resources
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Thymeleaf Documentation](https://www.thymeleaf.org/documentation.html)
- [Spock Framework](https://spockframework.org/spock/docs/)

## 🐛 Troubleshooting

Common issues and solutions:

| Issue | Solution |
|-------|----------|
| Port 8080 in use | `lsof -i :8080` and kill process |
| Docker services not starting | `docker-compose down -v && docker-compose up -d` |
| Build errors | `mvn clean verify -U` |
| Lombok not working | Enable annotation processing in IDE |
| Database connection failed | Check MySQL is running: `docker ps` |

See [SETUP.md](SETUP.md) for detailed troubleshooting guide.

## 🤝 Contributing

This is a workshop project. For improvements or bug fixes:
1. Follow the git commit format in CLAUDE.md
2. Use step-by-step commits
3. Include Co-Authored-By tag
4. Update documentation as needed

## 📝 Version Management

Follow the guidelines in [CLAUDE.md](../CLAUDE.md#version-management):
1. Analyze the stage
2. Implement changes in chunks
3. Create step-by-step commits: `Step # - <Changes applied>`
4. Include descriptive body paragraph

## 📄 License

This project is for educational purposes as part of the Spring Boot Kafka Workshop.

## 🎯 Workshop Goals

By completing this workshop, you will learn:
- ✅ Event-Driven Architecture patterns
- ✅ CQRS implementation in practice
- ✅ Kafka producer and consumer development
- ✅ Spring Boot multi-module project structure
- ✅ Asynchronous vs synchronous communication
- ✅ Domain-driven design principles
- ✅ Testing with Spock Framework
- ✅ Docker Compose for local development

## 📬 Support

For questions or issues:
1. Check [SETUP.md](SETUP.md) troubleshooting section
2. Review [ARCHITECTURE.md](ARCHITECTURE.md) for design decisions
3. Consult [API.md](API.md) for endpoint details

---

**Happy Learning! 🚀**
