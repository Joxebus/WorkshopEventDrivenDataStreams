# GitHub Codespaces Setup Guide

This guide walks you through using GitHub Codespaces for the Spring Boot Kafka Workshop.

## 🎯 What is GitHub Codespaces?

GitHub Codespaces provides a complete, cloud-based development environment directly in your browser. No local setup required!

## ✨ Benefits for This Workshop

- ✅ **No local installation** - Java 25, Maven, Docker all pre-configured
- ✅ **Consistent environment** - Everyone uses the same setup
- ✅ **Fast start** - 3-5 minutes from zero to running app
- ✅ **Works anywhere** - Any device with a browser
- ✅ **Automatic infrastructure** - MySQL, Kafka, Zookeeper start automatically
- ✅ **Pre-installed tools** - VS Code extensions, CLI utilities ready

## 🚀 Getting Started

### Step 1: Create Your Codespace

1. Go to the repository on GitHub
2. Click the green **"Code"** button
3. Select the **"Codespaces"** tab
4. Click **"Create codespace on main"**

### Step 2: Wait for Setup (3-5 minutes)

During this time, Codespaces automatically:
- ✅ Creates a Java 25 development environment
- ✅ Installs all Maven dependencies
- ✅ Builds the project (`mvn clean verify`)
- ✅ Starts Docker infrastructure (MySQL, Kafka, Zookeeper)
- ✅ Installs VS Code extensions
- ✅ Sets up helpful shell aliases

**Watch the terminal for progress!**

### Step 3: Start the Applications

Once setup completes, you'll see helpful information in the terminal.

**Open two terminals** (use split terminal or Terminal → New Terminal):

**Terminal 1 - Backend:**
```bash
backend
```

**Terminal 2 - Frontend:**
```bash
frontend
```

> 💡 **Tip:** These commands use `java -jar` for fast startup. The JARs are pre-built during setup!
>
> If you make code changes, rebuild first:
> ```bash
> rebuild    # Quick build (skips tests)
> backend    # Start with new changes
> ```

### Step 4: Access the Application

1. Wait for both services to start (look for "Started Application" in logs)
2. Click the **"Ports"** tab at the bottom
3. Find port **8080** (Frontend)
4. Click the globe icon 🌐 to open in browser
5. You should see the Workshop UI!

## 🛠️ Useful Features

### Forwarded Ports

Codespaces automatically forwards these ports:

| Port | Service | How to Access |
|------|---------|---------------|
| 8080 | Frontend UI | Open from Ports tab |
| 8081 | Backend API | Use forwarded URL in API calls |
| 3306 | MySQL | Connect via forwarded port |
| 9092 | Kafka | Used internally by apps |
| 2181 | Zookeeper | Used internally by Kafka |

### Pre-configured Aliases

Type these commands directly in the terminal:

```bash
# Application management (All use 'dev' profile for Codespaces)
backend          # Start backend with java -jar (fast, dev profile)
frontend         # Start frontend with java -jar (fast, dev profile)
backend-mvn      # Start backend with Maven (slower, dev profile, hot reload)
frontend-mvn     # Start frontend with Maven (slower, dev profile, hot reload)

# Infrastructure management  
infra-up         # Start Docker services
infra-down       # Stop Docker services
infra-status     # Check service health
infra-logs       # View all service logs
workshop-status  # Complete system overview

# Build and test
build-all        # mvn clean verify (full build)
rebuild          # mvn clean package -DskipTests (quick)
test-all         # mvn test

# Kafka tools
kafka-topics --list                                    # List all topics
kafka-console-consumer --topic purchase-orders --from-beginning  # Consume messages

# Database access
mysql-connect    # Connect to MySQL CLI
```

### VS Code Extensions

Pre-installed extensions:
- **Java Extension Pack** - Full Java IDE features
- **Spring Boot Dashboard** - Manage Spring Boot apps
- **Spring Boot Tools** - Spring Boot development
- **Docker Extension** - Container management
- **Thunder Client** - Test REST APIs
- **GitLens** - Git superpowers

## 📊 Monitoring Infrastructure

### Check Service Status

```bash
# Quick status
infra-status

# Detailed status
docker-compose ps

# Complete overview
workshop-status
```

### View Logs

```bash
# All services
infra-logs

# Specific service
docker-compose logs -f kafka
docker-compose logs -f mysql
docker-compose logs -f zookeeper
```

### Restart Services

```bash
# Restart all
docker-compose restart

# Restart specific service
docker-compose restart mysql

# Complete reset
docker-compose down -v
docker-compose up -d
```

## 🧪 Testing the Application

### Using the Web UI

1. Open http://localhost:8080 (via forwarded port)
2. Navigate to **Products** section
3. Add a new product
4. Navigate to **Orders** section
5. Create a purchase order
6. Observe asynchronous processing via Kafka

### Using Thunder Client (API Testing)

1. Click Thunder Client icon in VS Code sidebar
2. Create a new request
3. Use forwarded URL for port 8081
4. Test REST endpoints:
   - `GET /api/products`
   - `POST /api/products`
   - `GET /api/orders`

### Using curl

```bash
# Get all products
curl http://localhost:8081/api/products

# Create a product
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "description": "Created in Codespaces",
    "price": 99.99,
    "stock": 10,
    "category": "Test"
  }'
```

## 🔄 Development Workflow

### Making Code Changes

1. **Edit code** in VS Code
2. **Rebuild the project** (if using `java -jar` aliases):
   ```bash
   rebuild    # Fast build, skips tests
   ```
3. **Restart the service**:
   ```bash
   # Stop the running service (Ctrl+C)
   backend    # or frontend
   ```
4. **Refresh browser** to see changes

> 💡 **Alternative:** Use `backend-mvn` / `frontend-mvn` for Maven with DevTools hot reload (slower startup)

### Running Tests

```bash
# Run all tests
test-all

# Run specific module tests
cd spring-boot-kafka-backend && mvn test

# Run specific test class
mvn test -Dtest=ProductCommandServiceSpec
```

### Committing Changes

```bash
git add .
git commit -m "Your commit message"
git push
```

## 🐛 Troubleshooting

### Infrastructure Not Starting

```bash
# Check status
docker-compose ps

# View logs
docker-compose logs -f

# Restart infrastructure
docker-compose down
docker-compose up -d

# Wait ~30 seconds for health checks
```

### Port Already in Use

```bash
# Check what's running on ports
workshop-status

# Stop and restart services
docker-compose restart
```

### Application Won't Start

```bash
# Verify infrastructure is healthy
infra-status

# Check Java version
java -version  # Should be Java 25

# Rebuild project
build-all

# Try starting again
cd spring-boot-kafka-backend && mvn spring-boot:run
```

### Can't Access Forwarded Port

1. Check **Ports** tab in VS Code
2. Verify port **8080** is listed
3. Check **Visibility** (should be "Private" or "Public")
4. Click the **globe icon** 🌐
5. If still not working, try:
   - Stop and restart the frontend
   - Reload the Codespace window

### Codespace Disconnected

If your codespace disconnects:
1. Go to GitHub → Your codespaces
2. Find your codespace
3. Click "Open in browser" or "Open in VS Code"
4. Services should still be running

## 💰 Cost Management

### Free Tier
- GitHub provides **60 hours/month** free for personal accounts
- **120 hours/month** free for GitHub Pro

### Best Practices
- ⏸️ **Stop** codespace when not using it (saves hours)
- 🗑️ **Delete** old/unused codespaces
- ⏱️ Set **auto-stop timeout** (Settings → Timeout)
- 📊 Check usage: GitHub → Settings → Billing

### Stopping Your Codespace

**From GitHub:**
1. Go to github.com/codespaces
2. Click `•••` → Stop codespace

**From VS Code:**
1. Command Palette (Ctrl/Cmd + Shift + P)
2. "Codespaces: Stop Current Codespace"

**Important:** Stopping saves your work! Files persist between stops.

## 🎓 Learning Path

### Day 1: Setup & Exploration
1. ✅ Create codespace
2. ✅ Start applications
3. ✅ Explore the UI
4. ✅ Test product creation
5. ✅ Monitor infrastructure

### Day 2: Understanding the Flow
1. ✅ Create products via API
2. ✅ Create orders via UI
3. ✅ Watch Kafka messages
4. ✅ Query database
5. ✅ Follow CQRS pattern

### Day 3: Code Deep Dive
1. ✅ Review backend code
2. ✅ Review frontend code
3. ✅ Understand Kafka integration
4. ✅ Run tests
5. ✅ Make modifications

## 📚 Additional Resources

### Workshop Documentation
- [Main README](../README.md) - Complete project overview
- [Architecture Guide](../dev-docs/ARCHITECTURE.md) - System design and patterns
- [API Reference](../dev-docs/API.md) - REST endpoint documentation
- [Setup Guide](../dev-docs/SETUP.md) - Local development setup

### GitHub Codespaces
- [Codespaces Documentation](https://docs.github.com/en/codespaces)
- [Dev Container Spec](https://containers.dev/)
- [VS Code Remote](https://code.visualstudio.com/docs/remote/remote-overview)

### Technologies
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Apache Kafka](https://kafka.apache.org/)
- [Spring Kafka](https://spring.io/projects/spring-kafka)

## 💡 Pro Tips

1. **Use Multiple Terminals**
   - Split terminal: Drag terminal tab to split view
   - View backend and frontend logs simultaneously

2. **Monitor Kafka Messages**
   ```bash
   kafka-console-consumer --topic purchase-orders --from-beginning
   ```
   Leave this running in a third terminal to see live messages

3. **Database Exploration**
   ```bash
   mysql-connect
   SHOW TABLES;
   SELECT * FROM product;
   SELECT * FROM orders;
   ```

4. **Quick Restart**
   ```bash
   # If services are acting weird
   docker-compose restart
   # Wait ~30 seconds
   # Restart backend and frontend
   ```

5. **Save Your Aliases**
   Aliases are persistent! Use them every time you open the codespace.

6. **Share Your Codespace**
   - Make port visibility "Public"
   - Share the forwarded URL with collaborators
   - Great for pair programming!

## ❓ FAQ

**Q: How long does initial setup take?**  
A: 3-5 minutes for complete environment setup

**Q: Do I need to install anything locally?**  
A: No! Everything runs in the cloud browser-based environment

**Q: Will my changes persist?**  
A: Yes! Stopping a codespace saves all your files and git commits

**Q: Can I use my local VS Code?**  
A: Yes! Click "Open in VS Code" when creating/accessing the codespace

**Q: What if I run out of free hours?**  
A: Consider local development or upgrade to GitHub Pro for more hours

**Q: Can multiple people use the same codespace?**  
A: No, but you can share a running app via public port forwarding

**Q: How do I start fresh?**  
A: Delete the codespace and create a new one

**Q: What happens to Docker containers when I stop?**  
A: They stop but data persists. Use `docker-compose up -d` next time.

---

**Ready to start? Create your codespace now!** 🚀

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://github.com/codespaces/new?hide_repo_select=true&ref=main)
