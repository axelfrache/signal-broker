# Signal Broker - Backend

The core API and event streaming processing engine for the Signal Broker platform.

## Architecture

This service is built with **Java 21** and **Spring Boot 3**. It heavily relies on an Event-Driven Architecture, leveraging **Apache Kafka** as the central message broker to process support tickets asynchronously.

### Key Components

- **Ticket Ingestion & Processing**: Receives incoming support tickets and processes them through Kafka streams.
- **AI Labeling Service**: Integrates with an instance of Ollama to automatically classify tickets (category, priority, ticket type) using an LLM.
- **Ticket Grouping**: Evaluates the similarity of incoming tickets to group duplicate or related issues together via a `commonId`.
- **PostgreSQL Database**: Persistent storage for processed and labeled tickets.
- **Data Lake (Garage)**: Object storage (S3 compatible) used as a data lake. Raw tickets are archived here using Kafka Connect.
- **Kafka Connect**: Manages data pipelines to sink events from Kafka topics directly to PostgreSQL and Garage object storage.
- **Schema Registry**: Enforces schema evolution for Kafka topics.

## Tech Stack

- **Framework**: Spring Boot 3
- **Language**: Java 21
- **Event Streaming**: Spring Kafka
- **Database**: PostgreSQL with Spring Data JPA
- **AI Integration**: Spring WebClient connecting to Ollama API
- **Build Tool**: Maven

## Development Setup

### Requirements

- Java 21
- A running instance of Kafka, PostgreSQL, and Schema Registry (typically via Docker Compose from the root project).

### Running Locally

To run the Spring Boot application locally:

```bash
./mvnw spring-boot:run
```

Ensure that the infrastructure services (Kafka, DB, Ollama, etc.) are running and properly configured in your `application.properties` or environment variables.

### Build & Test

To compile the application:
```bash
./mvnw clean compile
```

To run the test suite:
```bash
./mvnw test
```

## Structure overview

- `controller/`: REST endpoints exposed to the frontend web dashboard.
- `service/`: Core business logic, including labeling and AI grouping integrations.
- `repository/`: Data access layer for retrieving data from PostgreSQL.
- `model/`: Entity, DTO, and Kafka event representations.
- `config/`: Spring bean and Kafka configurations.
