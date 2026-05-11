# 🌾 Farmer Assistant – AI-Powered Backend

A production-ready Spring Boot backend that helps farmers detect crop diseases and get expert farming advice using OpenAI's GPT-4o API. Fully secured with JWT authentication.

---

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup & Run](#setup--run)
  - [Option A: Local (Maven)](#option-a-local-maven)
  - [Option B: Docker Compose](#option-b-docker-compose)
- [API Reference](#api-reference)
- [Security Flow](#security-flow)
- [Rate Limiting](#rate-limiting)
- [Environment Variables](#environment-variables)
- [Database Schema](#database-schema)

---

## ✨ Features

| Feature | Details |
|---|---|
| 🔐 JWT Authentication | Register + Login, BCrypt passwords, Bearer token |
| 🌿 Crop Disease Detection | Upload image → GPT-4o Vision → disease, cure, prevention |
| 💬 AI Farming Chatbot | Ask any farming question → GPT-4o → actionable answer |
| 🗄️ History | Disease logs + chat history stored in MySQL, paginated |
| 🚦 Rate Limiting | Per-user bucket4j limits (chat: 10/min, disease: 5/min) |
| ⚠️ Error Handling | Global exception handler, consistent JSON error format |
| 🐳 Docker | Multi-stage Dockerfile + docker-compose with MySQL |
| 📝 Logging | SLF4J + rolling file appender |

---

## 🛠 Tech Stack

- **Java 17**
- **Spring Boot 3.2**
- **Spring Security** (JWT stateless)
- **Spring Data JPA** (Hibernate)
- **MySQL 8**
- **OpenAI API** (GPT-4o vision + chat)
- **Bucket4j** (rate limiting)
- **Lombok**
- **Maven**
- **Docker + Docker Compose**

---

## 🏛 Architecture

```
Controller  →  Service (Interface)  →  ServiceImpl
                    ↓                      ↓
              OpenAiService          Repository (JPA)
                    ↓                      ↓
              OpenAI API              MySQL Database
```

Clean layered architecture:

```
com.farmerassistant/
├── config/          Spring Security, WebClient config
├── controller/      REST endpoints (Auth, Disease, Chat)
├── dto/
│   ├── request/     RegisterRequest, LoginRequest, ChatRequest
│   └── response/    AuthResponse, DiseaseDetectionResponse, ChatResponse, ApiResponse
├── entity/          User, DiseaseLog, ChatHistory (JPA)
├── exception/       Custom exceptions + GlobalExceptionHandler
├── filter/          JwtAuthenticationFilter
├── repository/      Spring Data JPA repositories
├── security/        CustomUserDetailsService
├── service/         Service interfaces
│   └── impl/        AuthServiceImpl, DiseaseDetectionServiceImpl, ChatServiceImpl
└── util/            JwtUtil
```

---

## 📁 Project Structure

```
farmer-assistant/
├── src/
│   └── main/
│       ├── java/com/farmerassistant/
│       │   ├── FarmerAssistantApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   └── WebClientConfig.java
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   ├── ChatController.java
│       │   │   └── DiseaseDetectionController.java
│       │   ├── dto/
│       │   │   ├── request/
│       │   │   │   ├── ChatRequest.java
│       │   │   │   ├── LoginRequest.java
│       │   │   │   └── RegisterRequest.java
│       │   │   └── response/
│       │   │       ├── ApiResponse.java
│       │   │       ├── AuthResponse.java
│       │   │       ├── ChatResponse.java
│       │   │       └── DiseaseDetectionResponse.java
│       │   ├── entity/
│       │   │   ├── ChatHistory.java
│       │   │   ├── DiseaseLog.java
│       │   │   └── User.java
│       │   ├── exception/
│       │   │   ├── GlobalExceptionHandler.java
│       │   │   ├── InvalidImageException.java
│       │   │   ├── OpenAiException.java
│       │   │   ├── RateLimitException.java
│       │   │   ├── ResourceNotFoundException.java
│       │   │   └── UserAlreadyExistsException.java
│       │   ├── filter/
│       │   │   └── JwtAuthenticationFilter.java
│       │   ├── repository/
│       │   │   ├── ChatHistoryRepository.java
│       │   │   ├── DiseaseLogRepository.java
│       │   │   └── UserRepository.java
│       │   ├── security/
│       │   │   └── CustomUserDetailsService.java
│       │   ├── service/
│       │   │   ├── AuthService.java
│       │   │   ├── ChatService.java
│       │   │   ├── DiseaseDetectionService.java
│       │   │   ├── OpenAiService.java
│       │   │   ├── RateLimiterService.java
│       │   │   └── impl/
│       │   │       ├── AuthServiceImpl.java
│       │   │       ├── ChatServiceImpl.java
│       │   │       └── DiseaseDetectionServiceImpl.java
│       │   └── util/
│       │       └── JwtUtil.java
│       └── resources/
│           └── application.properties
├── docker/
│   └── init.sql
├── .env.example
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── FarmerAssistant.postman_collection.json
├── pom.xml
└── README.md
```

---

## ✅ Prerequisites

| Tool | Version |
|---|---|
| Java | 17+ |
| Maven | 3.8+ |
| MySQL | 8.0+ (or Docker) |
| OpenAI API Key | GPT-4o access required |

---

## 🚀 Setup & Run

### Option A: Local (Maven)

**Step 1 – Clone the project**
```bash
git clone https://github.com/your-org/farmer-assistant.git
cd farmer-assistant
```

**Step 2 – Create MySQL database**
```sql
CREATE DATABASE farmer_assistant_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

**Step 3 – Set environment variables**
```bash
export DB_URL=jdbc:mysql://localhost:3306/farmer_assistant_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
export JWT_SECRET=your-super-secret-jwt-key-min-32-chars-long
export OPENAI_API_KEY=sk-your-openai-api-key
```

Or create a `.env` file and source it:
```bash
cp .env.example .env
# Edit .env with your values
source .env
```

**Step 4 – Build and run**
```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

The server starts at **http://localhost:8080**

---

### Option B: Docker Compose

**Step 1 – Create `.env` file**
```bash
cp .env.example .env
```

Edit `.env`:
```env
DB_USERNAME=farmer_user
DB_PASSWORD=farmer_pass
JWT_SECRET=your-super-secret-jwt-key-min-32-chars-long
OPENAI_API_KEY=sk-your-openai-api-key
JWT_EXPIRATION=86400000
```

**Step 2 – Build and launch**
```bash
docker-compose up --build
```

**Step 3 – Verify health**
```bash
curl http://localhost:8080/actuator/health
```

**Stop containers:**
```bash
docker-compose down
# To also remove volumes (wipes DB):
docker-compose down -v
```

---

## 📡 API Reference

### Base URL
```
http://localhost:8080
```

### Authentication Header (for protected routes)
```
Authorization: Bearer <your_jwt_token>
```

---

### 🔐 Auth Endpoints (Public)

#### `POST /api/auth/register`
Register a new farmer account.

**Request:**
```json
{
  "name": "Ranjit Singh",
  "email": "ranjit@farm.com",
  "password": "secret123"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Farmer registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "name": "Ranjit Singh",
    "email": "ranjit@farm.com",
    "role": "USER",
    "expiresAt": "2025-01-15T12:00:00",
    "message": "Registration successful! Welcome to Farmer Assistant."
  },
  "timestamp": "2025-01-14T12:00:00"
}
```

---

#### `POST /api/auth/login`
Login and receive a JWT token.

**Request:**
```json
{
  "email": "ranjit@farm.com",
  "password": "secret123"
}
```

**Response (200 OK):** Same structure as register.

---

### 🌿 Disease Detection Endpoints (🔒 JWT Required)

#### `POST /api/disease/detect`
Upload a crop image for AI disease analysis.

**Request:** `multipart/form-data`
```
image: <file> (JPEG/PNG/WebP, max 10MB)
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Crop image analyzed successfully",
  "data": {
    "logId": 1,
    "imageName": "wheat_leaf.jpg",
    "diseaseName": "Wheat Rust (Puccinia triticina)",
    "confidenceLevel": "High",
    "cure": "1. Apply fungicide containing Propiconazole...\n2. Remove infected leaves...",
    "preventionTips": "• Use rust-resistant wheat varieties\n• Rotate crops annually\n• Monitor humidity levels",
    "status": "SUCCESS",
    "detectedAt": "2025-01-14T12:00:00",
    "message": "Crop image analyzed successfully."
  }
}
```

---

#### `GET /api/disease/history?page=0&size=10`
Retrieve paginated disease detection history.

---

### 💬 Chat Endpoints (🔒 JWT Required)

#### `POST /api/chat`
Ask a farming question.

**Request:**
```json
{
  "question": "What is the best time to sow wheat in Punjab?"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "AI response generated successfully",
  "data": {
    "chatId": 1,
    "question": "What is the best time to sow wheat in Punjab?",
    "answer": "The best time to sow wheat in Punjab is between October 25 and November 10...",
    "responseTimeMs": 1240,
    "askedAt": "2025-01-14T12:00:00"
  }
}
```

---

#### `GET /api/chat/history?page=0&size=10`
Retrieve paginated chat history.

---

### ⚠️ Error Responses

All errors follow this format:
```json
{
  "success": false,
  "message": "Human-readable error message",
  "error": "ERROR_CODE",
  "timestamp": "2025-01-14T12:00:00"
}
```

| HTTP Code | Error Code | Cause |
|---|---|---|
| 400 | INVALID_IMAGE | Wrong file type or empty file |
| 400 | BAD_REQUEST | Validation failure |
| 401 | UNAUTHORIZED | Missing/invalid JWT |
| 401 | INVALID_CREDENTIALS | Wrong email or password |
| 409 | USER_ALREADY_EXISTS | Email already registered |
| 429 | RATE_LIMIT_EXCEEDED | Too many requests |
| 503 | AI_SERVICE_ERROR | OpenAI API unavailable |

---

## 🔐 Security Flow

```
Request
  │
  ▼
JwtAuthenticationFilter
  ├── No token?       → Pass through (public routes work, protected routes get 401 from Spring Security)
  ├── Valid token?    → Populate SecurityContextHolder → Continue filter chain
  └── Invalid token? → Clear context → Continue (Spring Security blocks protected routes)
  │
  ▼
Spring Security AuthorizationFilter
  ├── /api/auth/**   → PERMIT ALL
  ├── /api/chat/**   → AUTHENTICATED only
  └── /api/disease/** → AUTHENTICATED only
```

Passwords are hashed with **BCrypt (strength 10)** and never stored in plain text.

---

## 🚦 Rate Limiting

Per-user token bucket limits (resets gradually):

| Endpoint | Limit | Window |
|---|---|---|
| `POST /api/chat` | 10 requests | 60 seconds |
| `POST /api/disease/detect` | 5 requests | 60 seconds |

Exceeding the limit returns **HTTP 429** with a descriptive error message.

---

## ⚙️ Environment Variables

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | Full MySQL JDBC URL | `jdbc:mysql://localhost:3306/farmer_assistant_db...` |
| `DB_USERNAME` | MySQL username | `root` |
| `DB_PASSWORD` | MySQL password | `root` |
| `JWT_SECRET` | HS256 signing secret (min 32 chars) | *(required)* |
| `JWT_EXPIRATION` | Token TTL in milliseconds | `86400000` (24h) |
| `OPENAI_API_KEY` | OpenAI API key | *(required)* |

---

## 🗄️ Database Schema

### `users`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| name | VARCHAR(100) | Required |
| email | VARCHAR(150) | Unique index |
| password | VARCHAR(255) | BCrypt hash |
| role | ENUM | USER / ADMIN |
| is_active | BOOLEAN | Soft delete flag |
| created_at | DATETIME | Auto-set |
| updated_at | DATETIME | Auto-updated |

### `disease_logs`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| user_id | BIGINT FK | → users.id |
| image_name | VARCHAR(255) | Original filename |
| image_content_type | VARCHAR(50) | MIME type |
| disease_name | VARCHAR(200) | AI result |
| confidence_level | VARCHAR(50) | Low / Medium / High |
| cure | TEXT | Treatment steps |
| prevention_tips | TEXT | Prevention advice |
| raw_ai_response | TEXT | Full AI output |
| status | VARCHAR(20) | SUCCESS / ERROR |
| detected_at | DATETIME | Auto-set |

### `chat_history`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| user_id | BIGINT FK | → users.id |
| question | TEXT | Farmer's question |
| answer | TEXT | AI answer |
| tokens_used | INT | OpenAI token count |
| response_time_ms | BIGINT | Latency |
| status | VARCHAR(20) | SUCCESS / ERROR |
| asked_at | DATETIME | Auto-set |

> Tables are auto-created by Spring Boot JPA (`ddl-auto=update`). No manual SQL migrations needed.

---

## 📬 Postman Collection

Import `FarmerAssistant.postman_collection.json` into Postman.

The collection:
- Auto-saves JWT token after Register/Login via test scripts
- Pre-fills `Authorization: Bearer {{jwt_token}}` on all protected requests
- Includes error test cases (401, 400, 409)

Set collection variable `base_url` to `http://localhost:8080` (already default).

---

## 👨‍💻 Development Tips

**Run tests:**
```bash
mvn test
```

**Build JAR only:**
```bash
mvn clean package -DskipTests
java -jar target/farmer-assistant-1.0.0.jar
```

**View logs:**
```bash
tail -f logs/farmer-assistant.log
```

**MySQL quick check:**
```sql
USE farmer_assistant_db;
SELECT * FROM users;
SELECT * FROM disease_logs ORDER BY detected_at DESC LIMIT 5;
SELECT * FROM chat_history ORDER BY asked_at DESC LIMIT 5;
```

---

## 📄 License

MIT License — free to use and modify for your farming projects.
