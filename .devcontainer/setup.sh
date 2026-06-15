#!/bin/bash
set -e

echo "🚀 Setting up Spring Boot Kafka Workshop environment..."

# Update package lists
echo "📦 Updating package lists..."
sudo apt-get update

# Install essential tools
echo "🔧 Installing essential tools..."
sudo apt-get install -y \
  curl \
  wget \
  git \
  vim \
  netcat-openbsd \
  jq \
  tree

# Verify Java installation
echo "☕ Verifying Java 25 installation..."
java -version
if [ $? -eq 0 ]; then
  echo "✅ Java 25 is installed"
else
  echo "❌ Java 25 installation failed"
  exit 1
fi

# Verify Maven installation
echo "🔨 Verifying Maven installation..."
mvn -version
if [ $? -eq 0 ]; then
  echo "✅ Maven is installed"
else
  echo "❌ Maven installation failed"
  exit 1
fi

# Install Docker Compose (if not already installed)
echo "🐳 Verifying Docker Compose..."
if ! command -v docker-compose &> /dev/null; then
  echo "Installing Docker Compose..."
  sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
  sudo chmod +x /usr/local/bin/docker-compose
fi
docker-compose --version

# Build the project
echo "🏗️  Building Maven project (this may take a few minutes)..."
mvn clean verify -DskipTests

# Create useful aliases
echo "📝 Creating helpful aliases..."
cat >> ~/.bashrc << 'EOF'

# Workshop aliases
alias backend='cd /workspaces/$(basename $PWD)/spring-boot-kafka-backend && mvn spring-boot:run'
alias frontend='cd /workspaces/$(basename $PWD)/spring-boot-kafka-frontend && mvn spring-boot:run'
alias infra-up='docker-compose up -d'
alias infra-down='docker-compose down'
alias infra-logs='docker-compose logs -f'
alias infra-status='docker-compose ps'
alias build-all='mvn clean verify'
alias test-all='mvn test'
alias workshop-status='echo "=== Infrastructure ===" && docker-compose ps && echo && echo "=== Ports ===" && ss -tlnp 2>/dev/null | grep -E ":(8080|8081|3306|9092|2181)"'

# Kafka CLI helpers
alias kafka-topics='docker exec -it workshop-kafka kafka-topics --bootstrap-server localhost:9092'
alias kafka-console-consumer='docker exec -it workshop-kafka kafka-console-consumer --bootstrap-server localhost:9092'
alias kafka-console-producer='docker exec -it workshop-kafka kafka-console-producer --bootstrap-server localhost:9092'

# MySQL helper
alias mysql-connect='docker exec -it workshop-mysql mysql -uworkshop -pworkshop workshop'

EOF

# Print helpful information
echo ""
cat .devcontainer/welcome.txt
echo ""
