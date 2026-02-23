# Signal Broker - Gestionnaire de Tickets

> Projet réalisé dans le cadre du cours Streaming Lake

## Description

Signal Broker est un système de gestion de tickets pour équipes de support technique. Il offre une solution complète pour la gestion, le suivi et l'analyse des demandes de support en temps réel.

## Fonctionnalités MVP

- **Labelisation automatique** : Classification intelligente des tickets utilisant l'IA
- **Dashboard & KPI** : Visualisation en temps réel des métriques clés de performance
- **Alerting** : Notifications instantanées via Discord pour les événements critiques
- **Fil de discussion interne** : Communication entre membres de l'équipe sur les tickets

## Architecture Technique

### Stack Technologique

**Backend**
- Java (Spring Boot)
- Maven

**Frontend**
- React
- TypeScript
- Vite
- shadcn/ui

**Infrastructure**
- **Message Broker** : Apache Kafka
- **Base de données** : PostgreSQL
- **Stockage objet** : Garage
- **Intégrations** : Discord API
- **Containerisation** : Docker & Docker Compose

### Structure du Projet

```
signal-broker/
├── backend/          # API REST Spring Boot
├── frontend/         # Interface React
└── docker-compose.yml # Orchestration des services
```

## Installation

### Prérequis

- Docker & Docker Compose
- Java 21 (pour développement backend)
- Node.js 18+ (pour développement frontend)

### Démarrage rapide

1. Cloner le repository
```bash
git clone <repository-url>
cd signal-broker
```

2. Lancer l'environnement avec Docker Compose
```bash
docker-compose up -d
```

3. Accéder à l'application
- Frontend : http://localhost:5173
- Backend : http://localhost:8080

## Développement

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| Frontend | 5173 | Interface utilisateur React |
| Backend | 8080 | API REST Spring Boot |
| Kafka | 9092 | Message broker |
| PostgreSQL | 5432 | Base de données |
| Garage | 3900 | Stockage objet S3-compatible |

## Licence

Projet académique - Cours Streaming Lake

## Infrastructure

![alt text](infrastructure-lesson1.png)