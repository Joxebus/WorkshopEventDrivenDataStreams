#!/bin/bash
set -e

echo "🚀 Setting up Spring Boot Kafka Workshop environment..."

# Update package lists
echo "📦 Updating package lists..."
sudo apt-get update -qq

# Install essential tools
echo "🔧 Installing essential tools..."
sudo apt-get install -y -qq \
  netcat-openbsd \
  jq \
  tree

# Verify Java installation
echo "☕ Verifying Java 25 installation..."
java -version
if [ $? -ne 0 ]; then
  echo "❌ Java installation failed"
  exit 1
fi
echo "✅ Java 25 is installed"

# Verify Maven installation
echo "🔨 Verifying Maven installation..."
mvn -version
if [ $? -ne 0 ]; then
  echo "❌ Maven installation failed"
  exit 1
fi
echo "✅ Maven is installed"

# Build the project
echo "🏗️  Building Maven project (this may take a few minutes)..."
mvn clean verify -DskipTests

# Create useful aliases
echo "📝 Creating helpful aliases..."
cat >> ~/.bashrc << 'EOF'

# Workshop aliases - using java -jar for faster startup with dev profile
alias backend='java -jar -Dspring.profiles.active=dev spring-boot-kafka-backend/target/spring-boot-kafka-backend-1.0-SNAPSHOT.jar'
alias frontend='java -jar -Dspring.profiles.active=dev spring-boot-kafka-frontend/target/spring-boot-kafka-frontend-1.0-SNAPSHOT.war'

# Alternative: run with Maven (slower startup, uses SPRING_PROFILES_ACTIVE env var)
alias backend-mvn='cd spring-boot-kafka-backend && mvn spring-boot:run'
alias frontend-mvn='cd spring-boot-kafka-frontend && mvn spring-boot:run'

# Infrastructure management
alias infra-up='docker-compose up -d'
alias infra-down='docker-compose down'
alias infra-logs='docker-compose logs -f'
alias infra-status='docker-compose ps'

# Build and test
alias build-all='mvn clean verify'
alias test-all='mvn test'
alias rebuild='mvn clean package -DskipTests'

# System status
alias workshop-status='echo "=== Infrastructure ===" && docker-compose ps && echo && echo "=== Ports ===" && ss -tlnp 2>/dev/null | grep -E ":(8080|8081|3306|9092|2181)" || true'

# Kafka CLI helpers
alias kafka-topics='docker exec -it workshop-kafka kafka-topics --bootstrap-server localhost:9092'
alias kafka-console-consumer='docker exec -it workshop-kafka kafka-console-consumer --bootstrap-server localhost:9092'
alias kafka-console-producer='docker exec -it workshop-kafka kafka-console-producer --bootstrap-server localhost:9092'

# MySQL helper
alias mysql-connect='docker exec -it workshop-mysql mysql -uworkshop -pworkshop workshop'

EOF


