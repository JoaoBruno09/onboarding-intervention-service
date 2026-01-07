## 🧩 Intervention Service

The Intervention Service is a dedicated microservice responsible for managing customer interventions within the banking account onboarding system. An intervention represents a customer’s role or participation in a specific account (e.g., primary holder, co-holder, authorized user). This service is designed to be highly focused and event-driven, handling the lifecycle of interventions while ensuring consistency across the distributed system. It follows Domain-Driven Design (DDD) principles and communicates asynchronously with other microservices using Apache Kafka to maintain loose coupling and fault isolation.

## 🔍 Key Features

- Deletion of interventions with cascading consistency handling
- Event-based communication with dependent services
- Isolation of intervention logic from customer and account domains
- Independent persistence using the Database per Service pattern

## 🔗 API Endpoints
- DELETE /interventions/{interventionId} - Delete interventions in the account
  
## 👨‍💻 Technologies

<div style="display: inline_block"><br>
<img align="center" alt="Java" height="40" width="40" src="https://github.com/devicons/devicon/blob/master/icons/java/java-original.svg">
<img align="center" alt="Spring" height="40" width="40" src="https://github.com/devicons/devicon/blob/master/icons/spring/spring-original.svg">
<img align="center" alt="Docker" height="40" width="40" src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/docker/docker-original.svg" />
<img align="center" alt="PostgreSQL" height="40" width="40" src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/postgresql/postgresql-original.svg" />
</div>

## 📂 Repository Structure

The repository is organized as follows:

- `boot`: Module that includes the application startup.
- `services/src/main/java/com/bank/onboarding/accountservice/services`: Contains services and their implementation.
- `web/src/main/java/com/bank/onboarding/accountservice/controllers`: Contains all the controllers of the application.

## 📋 Prerequisites

- Java 17+
- Maven
- Docker
- PostgreSQL database instance (local or containerized)

## 🌟 Additional Resources

- [Master's dissertation](http://hdl.handle.net/10400.22/26586)
