# Signal Broker

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-6DB33F?logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-Event%20Streaming-231F20?logo=apachekafka&logoColor=white)](https://kafka.apache.org/)

## Description

Signal Broker is a comprehensive support ticket management system designed for technical support teams.

It offers a complete solution for real-time management, tracking, and analysis of support requests. It utilizes AI for automatic ticket classification and provides a robust dashboard for monitoring KPIs in real-time.
For local testing, the recommended input source is the built-in Spring simulator (mail + WhatsApp examples), controlled through `.env`.

### Core Principles

- **Automatic Labeling**: Intelligent classification of tickets using AI (Ollama).
- **Ticket Grouping**: Related tickets are grouped together on the fly via AI similarity checks.
- **Dashboard & KPIs**: Real-time visualization of key performance metrics.
- **Alerting**: Instant notifications via Discord and other channels for critical events.
- **Internal Discussion**: Dedicated communication threads for team members on each ticket.
- **Event-Driven Architecture**: Built around Apache Kafka for scalability and real-time processing.
- **Long-term Storage**: Integration with Garage (S3 compatible) via Kafka Connect to archive raw tickets in a Data Lake.

![alt text](assets/infrastructure-lesson1.png)

## Getting Started

The application is divided into 2 main parts: **backend** and **frontend**.

### Prerequisites

- Java 21
- Node.js 20+
- Docker & Docker Compose
- Ollama running locally (for AI labeling), with a model such as `qwen2.5:3b-instruct`

### Environment

Create your local environment file from the template:

```bash
cp .env.example .env
```

To run end-to-end tests with generated tickets, enable the simulator in `.env`:

```bash
SIMULATOR_ENABLED=true
```

## Running

### Locally (development mode)

**Backend:**
```bash
cd backend
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

Then go to:
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080

### Locally (fully dockerized)

```bash
docker compose up -d --build
```

Then go to:
- Frontend: http://localhost:80
- Backend API: http://localhost:8080
- Kafbat UI (Kafka Management): http://localhost:8082

### Recommended Local Test Flow (Simulator)

1. Set `SIMULATOR_ENABLED=true` in `.env`.
2. Start the stack with `docker compose up -d --build`.
3. Follow backend logs to confirm message generation:
```bash
docker compose logs -f backend
```
4. Open the dashboard and Kafbat UI to observe ticket ingestion, formatting, labeling, and persistence.

The simulator publishes realistic examples from:
- `backend/src/main/resources/simulator/mail.json`
- `backend/src/main/resources/simulator/whatsapp.json`

To stop the application:
```bash
docker compose down
```

Use `-v` if you want to remove volumes too.

## Code Quality

For code quality, we use:
- **Backend**: Spotless with Google Java Format
- **Frontend**: ESLint

### Commands

**Backend:**
```bash
cd backend
./mvnw spotless:check  # Check formatting
./mvnw spotless:apply  # Fix formatting
```

**Frontend:**
```bash
cd frontend
npm run lint          # Check linting
```

> **Warning**: Those are mandatory before pushing code, if it is not done the CI will fail.

## Repository

GitHub repository: https://github.com/axelfrache/signal-broker
