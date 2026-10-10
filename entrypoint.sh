#!/bin/bash
set -e

echo "================================================================"
echo "    AI-ChainID — Unified Enterprise Platform Launcher (Render)  "
echo "================================================================"

# 1. Initialize Database
if [ -z "$DB_URL" ]; then
    echo "[1/3] Starting internal database service (MariaDB)..."
    service mariadb start || service mysql start

    echo "Waiting for database daemon to become ready..."
    for i in {1..30}; do
        if mysqladmin ping -h 127.0.0.1 --silent; then
            break
        fi
        sleep 1
    done

    # Create default database if needed
    mariadb -u root -e "CREATE DATABASE IF NOT EXISTS AIchinId;" 2>/dev/null || mysql -u root -e "CREATE DATABASE IF NOT EXISTS AIchinId;" 2>/dev/null || true
    mariadb -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root'; FLUSH PRIVILEGES;" 2>/dev/null || mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root'; FLUSH PRIVILEGES;" 2>/dev/null || true
    
    export DB_URL="jdbc:mysql://127.0.0.1:3306/AIchinId?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
    export DB_USERNAME="root"
    export DB_PASSWORD="root"
    echo "✓ Database engine is online and ready (AIchinId)."
else
    echo "✓ Using configured external database: $DB_URL"
fi

# 2. Launch Python AI Microservice in the background
echo "[2/3] Launching Python AI Engine on port 8000..."
cd /app/ai-service
python -m uvicorn app.main:app --host 127.0.0.1 --port 8000 &
AI_PID=$!
echo "✓ Python AI microservice started with PID $AI_PID."

# Wait briefly for AI service health
sleep 3

# 3. Launch Spring Boot Unified Gateway & Web UI in the foreground
PORT="${PORT:-8080}"
echo "[3/3] Launching AI-ChainID Platform Gateway on port $PORT..."
cd /app
exec java -Dserver.port="$PORT" -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar
