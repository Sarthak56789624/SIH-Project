# AI-ChainID — Unified Enterprise Platform Deployment Guide

This guide describes how to deploy the entire **AI-ChainID** platform (Spring Boot Core Gateway, Web UI, Python AI Microservice, and MySQL Database) **integrated as a single platform**.

---

## 🏛️ Architecture Overview

When deployed as an integrated platform:
```
                                 ┌──────────────────────────────────────────────┐
                                 │                 USERS / BROWSER              │
                                 └──────────────────────┬───────────────────────┘
                                                        │
                                                        ▼ Port 8080 (Public Entrypoint)
┌───────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ AI-ChainID UNIFIED DOCKER NETWORK                                                                     │
│                                                                                                       │
│   ┌──────────────────────────────────────────────────┐                                                │
│   │  Core Gateway & Frontend (`backend`)             │                                                │
│   │  - Serves Integrated Web UI (HTML5/CSS/JS)       │                                                │
│   │  - Handles Auth, RBAC, NIST Break-Glass & Admin  │                                                │
│   │  - Spring Boot 3.4.1 (Java 21)                   │                                                │
│   └───────────────┬──────────────────┬───────────────┘                                                │
│                   │                  │                                                                │
│                   │ Internal :3306   │ Internal :8000                                                 │
│                   ▼                  ▼                                                                │
│   ┌───────────────────────────┐  ┌────────────────────────────────────────────────┐                   │
│   │  Database (`mysql`)       │  │  AI Microservice (`ai-service`)                │                   │
│   │  - MySQL 8.0              │  │  - Python 3.11 FastAPI                         │                   │
│   │  - Schema: `AIchinId`     │  │  - Random Forest & Isolation Forest ML Engines │                   │
│   │  - Persistent storage vol │  │  - Zero-Trust Permission & Risk Scorers        │                   │
│   └───────────────────────────┘  └────────────────────────────────────────────────┘                   │
└───────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

The user only connects to **one single port (8080)** or domain. The frontend, backend APIs, and AI models are fully wired together out-of-the-box.

---

## 🚀 Option 1: Deploy with Docker Compose (Any Cloud VPS or Server)

Ideal for **DigitalOcean Droplet**, **AWS EC2**, **Hetzner Cloud**, or local staging.

### Prerequisites
- Docker Engine & Docker Compose (`docker compose version` >= 2.0)
- Git

### Deployment Steps

1. **Clone the repository on your server**:
   ```bash
   git clone https://github.com/Sarthak56789624/SIH-Project.git
   cd SIH-Project
   ```

2. **Configure Environment Variables** (Optional, defaults provided):
   ```bash
   cp .env.example .env
   ```

3. **Start the Entire Integrated Platform in Background**:
   ```bash
   docker compose up -d --build
   ```

4. **Verify Container Health**:
   ```bash
   docker compose ps
   ```

5. **Access the Unified Platform**:
   - **Integrated Web UI:** `http://<SERVER_IP>:8080/`
   - **Login Credentials:** `admin@aichainid.org` / `Admin@12345`
   - **AI Microservice Direct API:** `http://<SERVER_IP>:8000/docs`
   - **Swagger / OpenAPI Documentation:** `http://<SERVER_IP>:8080/swagger-ui.html`

6. **To Stop or Restart**:
   ```bash
   # Stop services
   docker compose down

   # View live aggregated logs
   docker compose logs -f
   ```

---

## ☁️ Option 2: 1-Click Cloud Deployment via Railway.app

**Railway** natively supports multi-service repositories with private networking.

1. Create a free account on [Railway.app](https://railway.app).
2. Click **New Project** → **Deploy from GitHub repo**.
3. Select `Sarthak56789624/SIH-Project`.
4. Railway will detect the `docker-compose.yml` or allow you to deploy services:
   - **MySQL**: Add a MySQL database service.
   - **AI Service**: Set root directory to `ai-chainid`.
   - **Backend**: Set root directory to `AIchainID-backend-main`.
5. Connect backend variables:
   - `DB_URL`: `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`
   - `DB_USERNAME`: `${{MySQL.MYSQLUSER}}`
   - `DB_PASSWORD`: `${{MySQL.MYSQLPASSWORD}}`
   - `AI_SERVICE_URL`: `http://${{ai-service.RAILWAY_PRIVATE_DOMAIN}}:8000`
6. Click **Deploy**. Generate a public domain on the Backend service to access your live application.

---

## 🌐 Option 3: Deploy via Render.com

1. Create an account on [Render.com](https://render.com).
2. **Database:** Create a free PostgreSQL/MySQL instance (via Aiven or TiDB Cloud).
3. **AI Service:** Create a Web Service:
   - Environment: `Docker`
   - Root directory: `ai-chainid`
   - Port: `8000`
4. **Backend & Frontend:** Create a Web Service:
   - Environment: `Docker`
   - Root directory: `AIchainID-backend-main`
   - Port: `8080`
   - Environment variables:
     - `AI_SERVICE_URL`: `https://<your-render-ai-service>.onrender.com`
     - `DB_URL`: JDBC connection string to your database.
