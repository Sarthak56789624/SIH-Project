# ===================================================================
# Stage 1: Build Spring Boot Application
# ===================================================================
FROM maven:3.9-eclipse-temurin-21 AS backend-builder

WORKDIR /build

COPY AIchainID-backend-main/pom.xml .
RUN mvn dependency:go-offline -B

COPY AIchainID-backend-main/src ./src
RUN mvn clean package -DskipTests -B

# ===================================================================
# Stage 2: Unified Production Runner (Java 21 + Python 3 + Database)
# ===================================================================
FROM eclipse-temurin:21-jre-jammy

ENV DEBIAN_FRONTEND=noninteractive
ENV PYTHONUNBUFFERED=1

WORKDIR /app

# Install Python 3, venv, MariaDB database engine, and curl
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3 \
    python3-pip \
    python3-venv \
    mariadb-server \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Setup virtual environment for Python AI microservice
RUN python3 -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# Install Python dependencies
WORKDIR /app/ai-service
COPY ai-chainid/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy Python AI Engine code & trained ML models
COPY ai-chainid .

# Ensure ML models exist
RUN python -c "import os; from pathlib import Path; p = Path('models/risk_engine.joblib'); \
    exit(0) if p.exists() else os.system('python scripts/train_models.py')"

# Copy built Spring Boot JAR from builder
WORKDIR /app
COPY --from=backend-builder /build/target/*.jar /app/app.jar

# Setup entrypoint launcher
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Default ports (Render will bind to its dynamically assigned $PORT)
EXPOSE 8080 10000

ENTRYPOINT ["/app/entrypoint.sh"]
