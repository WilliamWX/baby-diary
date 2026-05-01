# BabyDiary — Record · Share · Grow

<p align="center">
  <img src="https://img.shields.io/badge/license-MIT-green?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/PRs-welcome-brightgreen?style=flat-square" alt="PRs Welcome">
</p>

## A Note from a Programmer Dad

The birth of my baby changed everything.

From the first time I held that tiny, soft body in my arms, to watching the first roll-over, first crawl, first wobbly steps toward me... every moment was too precious to let slip away. As a programmer, I solve problems with code — so **BabyDiary** was born: a community for capturing every step of a child's growth journey.

No business plan, no product roadmap — just a new father's **endless love** for his baby, and an irresistible urge to share that joy with the world.

> Code ages and frameworks fade, but memories of our children deserve to be cherished forever.

---

## Features

| Module | Description |
|--------|-------------|
| Diary | Photo-rich daily journal, up to 9 images, presented in a social-media grid layout |
| Growth Tracker | Height & weight charts, feeding log, sleep log, developmental milestones |
| Community | Posts with likes, comments, bookmarks, and follow system |
| Baby Profile | Support for multiple children, with avatar, birthday, and gender |
| AI Doctor | Powered by DeepSeek — ask pediatric questions publicly or anonymously |
| Profile | Avatar upload, editable bio, activity stats |

## Tech Stack

| Layer | Technology |
|-------|-------------|
| Frontend | Vue 3 + Vite + Element Plus + Pinia + Vue Router + ECharts |
| Backend | Spring Boot 3.2.5 + MyBatis-Plus + Spring Security + JWT |
| Database | MySQL 8.0 |
| Cache | Redis 7 |
| Storage | MinIO |
| AI | DeepSeek API |

## Quick Start

### Prerequisites

- Java 17+
- Node.js 18+
- MySQL 8.0
- Redis 7
- MinIO

### Run

```bash
# 1. Ensure MySQL / Redis / MinIO are running

# 2. Start backend (port 8080)
cd backend
mvn spring-boot:run -DskipTests

# 3. Start frontend (port 3000)
cd frontend
npm install
npm run dev
```

### Configuration

Edit `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    username: root
    password: <your-mysql-password>

deepseek:
  api-key: <your-deepseek-api-key>

minio:
  access-key: minioadmin
  secret-key: minioadmin
```

## Project Structure

```
baby-diary/
├── backend/src/main/java/com/babydiary/
│   ├── config/        # Security, CORS, MyBatis-Plus configuration
│   ├── controller/    # REST API controllers
│   ├── service/       # Business logic (including DeepSeek AI service)
│   ├── mapper/        # MyBatis-Plus data access layer
│   ├── entity/        # Database entities
│   ├── dto/           # Request DTOs
│   └── vo/            # Response VOs
├── frontend/src/
│   ├── api/           # API client modules
│   ├── components/    # Shared components (Logo, Layout)
│   ├── views/         # Page views (Login, Diary, AI Doctor, etc.)
│   ├── router/        # Vue Router configuration
│   └── stores/        # Pinia state management
└── docker-compose.yml # Docker middleware orchestration
```

## Contribute

BabyDiary is far from perfect — there are features to add, rough edges to smooth, and plenty of code to clean up. If you're a fellow parent who also writes code, or just find this project interesting:

- ⭐ Star the repo to help more parents discover it
- 🐛 Submit an Issue for bugs or feature suggestions
- 🎨 Open a PR and help build something meaningful

Every line of code means more precious childhood moments preserved for families everywhere.

> **Code with warmth. Let every milestone be seen.**

## License

MIT © WilliamWX
