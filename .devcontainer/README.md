# GitHub Codespaces Configuration

This directory contains the configuration for running the Spring Boot Kafka Workshop in GitHub Codespaces.

## 🚀 Quick Start

1. **Open in Codespaces**
   - Click the green "Code" button on GitHub
   - Select "Codespaces" tab
   - Click "Create codespace on main"

2. **Wait for Setup**
   - Initial build takes ~3-5 minutes
   - Maven dependencies are downloaded
   - Infrastructure services start automatically

3. **Start the Application**
   ```bash
   # Terminal 1 - Backend (fast startup with java -jar)
   backend
   
   # Terminal 2 - Frontend (fast startup with java -jar)
   frontend
   ```
   
   **Alternative (slower startup):**
   ```bash
   backend-mvn    # Uses mvn spring-boot:run
   frontend-mvn   # Uses mvn spring-boot:run
   ```

4. **Access the Application**
   - Frontend UI: `http://localhost:8080` (forwarded by Codespaces)
   - Backend API: `http://localhost:8081`

## 📦 What's Included

### Development Environment
- **Java 25** (Amazon Corretto)
- **Maven 3.x**
- **Docker-in-Docker** for running infrastructure
- **Git** with GitHub CLI

### VS Code Extensions
- **Java Extension Pack** - Complete Java development suite
- **Spring Boot Extension Pack** - Spring Boot dashboard and tools
- **Docker Extension** - Container management
- **Thunder Client** - API testing (alternative to Postman)
- **GitLens** - Enhanced git capabilities

### Infrastructure Services
Automatically started via `docker-compose`:
- **MySQL 8.0** - Database (port 3306)
- **Apache Kafka** - Message broker (port 9092)
- **Zookeeper** - Kafka coordination (port 2181)

### Forwarded Ports
- `8080` - Frontend (Thymeleaf UI)
- `8081` - Backend (REST API)
- `3306` - MySQL Database
- `9092` - Kafka Broker
- `2181` - Zookeeper

## 🛠️ Helpful Aliases

The setup script creates useful aliases in your shell:

### Application Management
```bash
backend          # Start backend with java -jar + dev profile (fast)
frontend         # Start frontend with java -jar + dev profile (fast)
backend-mvn      # Start backend with Maven + dev profile (slower, hot reload)
frontend-mvn     # Start frontend with Maven + dev profile (slower, hot reload)
```

> 💡 All aliases automatically use the `dev` profile for Codespaces compatibility (proper redirects, forwarded headers)

### Infrastructure Management
```bash
infra-up         # Start Docker services
infra-down       # Stop Docker services
infra-status     # Check service status
infra-logs       # View service logs
workshop-status  # Complete system status
```

### Build & Test
```bash
build-all        # mvn clean verify (full build with tests)
rebuild          # mvn clean package -DskipTests (quick build)
test-all         # mvn test
```

### Kafka Tools
```bash
kafka-topics                     # List topics
kafka-console-consumer           # Consume messages
kafka-console-producer           # Produce messages

# Example: List all topics
kafka-topics --list

# Example: Consume from purchase-orders topic
kafka-console-consumer --topic purchase-orders --from-beginning
```

### Database Access
```bash
mysql-connect    # Connect to MySQL CLI
```

## 🔧 Configuration Files

### `devcontainer.json`
Main configuration file that defines:
- Base Docker image (Java 25 Debian Bookworm)
- Features (Docker-in-Docker, Git, GitHub CLI)
- VS Code extensions and settings
- Port forwarding rules
- Environment variables
- Post-creation and post-start commands

### `setup.sh`
Initialization script that:
- Verifies Java and Maven installation
- Installs Docker Compose
- Builds the Maven project
- Creates helpful shell aliases
- Displays quick start guide

## 📊 Port Forwarding

Codespaces automatically forwards ports and provides HTTPS URLs:

| Port | Service | Access |
|------|---------|--------|
| 8080 | Frontend | Click "Ports" tab → Open in Browser |
| 8081 | Backend API | Use forwarded URL for API calls |
| 3306 | MySQL | Connect via forwarded port |
| 9092 | Kafka | Internal use by applications |
| 2181 | Zookeeper | Internal use by Kafka |

## 🐛 Troubleshooting

### Infrastructure Not Starting
```bash
# Check Docker status
docker ps

# Restart infrastructure
docker-compose down -v
docker-compose up -d

# Check logs
infra-logs
```

### Port Already in Use
```bash
# Check what's using ports
workshop-status

# Kill specific service
docker-compose stop mysql
docker-compose start mysql
```

### Maven Build Fails
```bash
# Clean build
mvn clean install -DskipTests

# Verify Java version
java -version  # Should show Java 25
```

### Services Not Healthy
```bash
# Check service health
docker-compose ps

# View specific service logs
docker-compose logs -f kafka
docker-compose logs -f mysql
docker-compose logs -f zookeeper

# Restart all services
docker-compose restart
```

### Can't Access Forwarded Ports
1. Check "Ports" tab in VS Code
2. Verify port visibility (should be "Public" or "Private")
3. Click the globe icon to open in browser
4. Check firewall/network settings

## 🔄 Updating the Environment

### Rebuild Container
If you modify `.devcontainer/devcontainer.json`:
1. Command Palette (Cmd/Ctrl + Shift + P)
2. Search: "Codespaces: Rebuild Container"
3. Wait for rebuild (keeps workspace data)

### Update Dependencies
```bash
# Update Maven dependencies
mvn clean install -U

# Pull latest Docker images
docker-compose pull
docker-compose up -d
```

## 📝 Development Workflow

### Typical Session

1. **Start infrastructure** (auto-started, verify status)
   ```bash
   infra-status
   ```

2. **Start backend** (Terminal 1)
   ```bash
   cd spring-boot-kafka-backend
   mvn spring-boot:run
   ```

3. **Start frontend** (Terminal 2)
   ```bash
   cd spring-boot-kafka-frontend
   mvn spring-boot:run
   ```

4. **Make changes**
   - Edit code in VS Code
   - Spring Boot DevTools provides hot reload

5. **Test changes**
   ```bash
   test-all
   ```

6. **Check infrastructure**
   ```bash
   workshop-status
   ```

## 🌐 Resources

- [GitHub Codespaces Documentation](https://docs.github.com/en/codespaces)
- [Dev Container Specification](https://containers.dev/)
- [VS Code Remote Development](https://code.visualstudio.com/docs/remote/remote-overview)
- [Workshop Documentation](../README.md)
- [Architecture Guide](../dev-docs/ARCHITECTURE.md)
- [Setup Guide](../dev-docs/SETUP.md)

## 💡 Tips

- **Save costs**: Stop your codespace when not using it
- **Multiple terminals**: Use split terminal for backend/frontend
- **Extensions**: Install additional extensions from VS Code marketplace
- **Persistence**: Files in `/workspaces` persist between stops
- **Snapshots**: Create snapshots for backup/branching
- **Port visibility**: Set ports to "Public" to share with others

## 🎯 What Happens Automatically

When you create a Codespace:

1. ✅ Java 25 environment is configured
2. ✅ Maven is ready to use
3. ✅ Docker-in-Docker is enabled
4. ✅ VS Code extensions are installed
5. ✅ Maven project is built (`mvn clean verify -DskipTests`)
6. ✅ Shell aliases are created
7. ✅ Infrastructure services start (`docker-compose up -d`)
8. ✅ Ports are forwarded automatically

You just need to:
- Wait ~30 seconds for services to be healthy
- Start backend service
- Start frontend service
- Open browser to access the app

Happy coding! 🚀
