# 🎲 Tài Xỉu Game Platform

Web-based Tai Xiu (Sic Bo) game simulation with virtual currency, real-time gameplay, and AI chat integration.

## 🛠 Tech Stack

| Component | Technology |
|-----------|------------|
| Backend | Spring Boot 3.5, Java 21, Spring Security, JWT, WebSocket |
| Frontend | React 18, Vite, Zustand, STOMP.js |
| Database | MySQL 8 |
| AI | Google Gemini API |
| Deployment | Docker, GitHub Actions |

## 🚀 Quick Start

### Prerequisites
- Java 21
- Node.js 20+
- MySQL 8
- (Optional) Docker & Docker Compose

### Local Development

**1. Start MySQL and create database:**
```sql
CREATE DATABASE taixiu_db;
```

**2. Run Backend:**
```bash
cd taixiu-be
./mvnw spring-boot:run
```

**3. Run Frontend:**
```bash
cd taixiu-fe
npm install
npm run dev
```

**4. Open Browser:**
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080

### Demo Accounts
| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| Player | player1 | player123 |

## 🐳 Docker Deployment

**1. Create `.env` file:**
```bash
cp .env.example .env
# Edit .env with your credentials
```

**2. Build and run:**
```bash
docker-compose up -d --build
```

**3. Access:**
- http://localhost (Frontend)
- http://localhost:8080 (Backend API)

## 📁 Project Structure

```
taixiu/
├── taixiu-be/          # Spring Boot Backend
│   ├── src/main/java/
│   │   └── com/tamdao/taixiu_be/
│   │       ├── config/        # Security, WebSocket config
│   │       ├── controller/    # REST controllers
│   │       ├── entity/        # JPA entities
│   │       ├── repository/    # Data repositories
│   │       ├── service/       # Business logic
│   │       └── security/      # JWT authentication
│   └── Dockerfile
│
├── taixiu-fe/          # React Frontend
│   ├── src/
│   │   ├── components/    # Reusable components
│   │   ├── pages/         # Page components
│   │   ├── services/      # API & WebSocket
│   │   └── store/         # Zustand stores
│   └── Dockerfile
│
├── .github/workflows/  # CI/CD pipelines
└── docker-compose.yml  # Full stack deployment
```

## ⚙️ Configuration

### Backend (application.yml)
```yaml
# Database
spring.datasource.url: jdbc:mysql://localhost:3306/taixiu_db

# JWT Secret
jwt.secret: your-secret-key

# Gemini AI API Key
gemini.api-key: your-api-key
```

## 🎮 Features

- ✅ Real-time Tai Xiu game (30s rounds)
- ✅ JWT Authentication
- ✅ WebSocket for live updates
- ✅ AI Chatbot (Gemini)
- ✅ Admin dashboard
- ✅ Gift code system
- ✅ Leaderboard
- ✅ Transaction history

## 📄 License

MIT License - For educational purposes only.
