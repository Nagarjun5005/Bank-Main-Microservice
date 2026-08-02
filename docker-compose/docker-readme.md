# Docker Compose with Common Configuration
## Complete Notes for Spring Boot Microservices

---

# Table of Contents

1. Introduction
2. What is Docker Compose?
3. Why use Docker Compose for Microservices?
4. Project Architecture
5. Folder Structure
6. docker-compose.yml Explained
7. RabbitMQ Service
8. Config Server Service
9. Accounts Service
10. Loans Service
11. Cards Service
12. Docker Networks
13. Docker Health Checks
14. depends_on with service_healthy
15. Environment Variables
16. Docker DNS & Container Communication
17. common-config.yml Explained
18. Docker Compose Inheritance (`extends`)
19. Startup Flow
20. Docker Compose Commands
21. Best Practices
22. Summary

---

# 1. Introduction

This project demonstrates how to deploy multiple Spring Boot microservices using Docker Compose.

The application consists of

- RabbitMQ
- Spring Cloud Config Server
- Accounts Service
- Loans Service
- Cards Service

Instead of configuring every service individually, a separate file named

```
common-config.yml
```

contains reusable configurations that can be inherited by multiple services.

This keeps the Docker Compose file clean and easy to maintain.

---

# 2. What is Docker Compose?

Docker Compose is a tool used to define and run multiple Docker containers together.

Instead of running many docker commands manually,

```
docker run ...

docker run ...

docker run ...
```

we describe everything in one YAML file.

Example

```yaml
services:

  accounts:

  loans:

  cards:
```

Then start the complete application using

```bash
docker compose up
```

Docker automatically

- Creates networks
- Creates containers
- Starts services
- Connects services together

---

# 3. Why use Docker Compose for Microservices?

A microservice application usually contains many services.

Example

```
Accounts

Loans

Cards

RabbitMQ

Kafka

Config Server

Database

Redis
```

Starting each service manually becomes difficult.

Docker Compose solves this problem by managing everything from a single configuration file.

---

# 4. Project Architecture

```
                      +----------------------+
                      |      RabbitMQ        |
                      | Ports 5672,15672     |
                      +----------+-----------+
                                 |
                                 |
                                 ▼
                     +-----------------------+
                     |    Config Server      |
                     |       Port 8071       |
                     +-----------+-----------+
                                 |
          -----------------------------------------------
          |                    |                        |
          ▼                    ▼                        ▼

     +----------+        +-----------+          +-----------+
     | Accounts |        |  Loans    |          |  Cards    |
     |   8080   |        |   8090    |          |   9000    |
     +----------+        +-----------+          +-----------+
```

All services communicate over a Docker bridge network.

---

# 5. Folder Structure

```
project

│

├── docker-compose.yml

└── common-config.yml
```

## docker-compose.yml

Contains

- Service definitions
- Images
- Ports
- Dependencies
- Health checks

## common-config.yml

Contains reusable configurations like

- Networks
- Memory limits
- Environment variables

---

# 6. docker-compose.yml Explained

The file begins with

```yaml
services:
```

Everything inside services becomes a Docker container.

Example

```yaml
services:

  rabbit

  configserver

  accounts

  loans

  cards
```

Five services = Five containers.

---

# 7. RabbitMQ Service

```
rabbit:
```

RabbitMQ acts as the message broker.

Spring Boot applications communicate asynchronously using RabbitMQ.

---

## Image

```yaml
image: rabbitmq:4-management
```

Docker downloads

```
rabbitmq:4-management
```

from Docker Hub.

---

## Hostname

```yaml
hostname: rabbitmq
```

Creates an internal hostname

```
rabbitmq
```

Other containers can connect using

```
rabbit:5672
```

instead of an IP address.

---

## Ports

```yaml
ports:

- "5672:5672"

- "15672:15672"
```

5672

RabbitMQ messaging protocol (AMQP)

15672

RabbitMQ Management Dashboard

Open

```
http://localhost:15672
```

Default login

```
guest

guest
```

---

## Health Check

```yaml
healthcheck:

  test: rabbitmq-diagnostics check_port_connectivity
```

Docker executes

```
rabbitmq-diagnostics check_port_connectivity
```

inside the RabbitMQ container.

If successful

```
Healthy
```

Otherwise

```
Unhealthy
```

Docker checks every

```
10 seconds
```

---

## Why Health Checks?

Without a health check

Docker would only know

```
Container Running
```

It would not know whether RabbitMQ is actually accepting connections.

---

# 8. Config Server

The Config Server stores configuration for all Spring Boot services.

---

## depends_on

```yaml
depends_on:

  rabbit:

    condition: service_healthy
```

Meaning

```
Don't start Config Server

until RabbitMQ becomes Healthy.
```

---

## Health Check

```yaml
curl

localhost:8071/actuator/health/readiness
```

Spring Boot returns

```json
{

"status":"UP"

}
```

If UP

Container becomes Healthy.

---

## Why readiness endpoint?

Spring Boot takes time to start.

Without this check

Accounts

Loans

Cards

might try connecting before Config Server is ready.

---

# 9. Accounts Service

Accounts extends

```
microservice-configserver-config
```

Automatically receives

- Network
- Memory limit
- Config Server URL
- Spring Profile

The only unique property is

```yaml
SPRING_APPLICATION_NAME: accounts
```

---

# 10. Loans Service

Same configuration.

Only

```
SPRING_APPLICATION_NAME
```

changes.

---

# 11. Cards Service

Same configuration.

Only

```
SPRING_APPLICATION_NAME
```

changes.

---

# 12. Docker Networks

```
networks:

  nagarjun20

    driver: bridge
```

Docker creates a private network.

```
Rabbit

|

Config Server

|

Accounts

|

Loans

|

Cards
```

All containers communicate privately.

---

## Why not localhost?

Suppose Accounts wants RabbitMQ.

Inside the Accounts container

```
localhost
```

means

```
Accounts Container
```

NOT RabbitMQ.

Instead Docker provides DNS.

Simply connect using

```
rabbit
```

or

```
configserver
```

Example

```
spring.rabbitmq.host=rabbit

spring.config.import=configserver:http://configserver:8071
```

---

# 13. Docker Health Checks

A health check periodically runs a command.

Example

```yaml
healthcheck:

  test: curl ...

  interval:10s
```

Docker executes

```
Every 10 seconds
```

Healthy

↓

Container Status

```
Healthy
```

Failure

↓

Container Status

```
Unhealthy
```

---

## Health Check Properties

### interval

Time between checks.

### timeout

Maximum execution time.

### retries

Number of failures before marking unhealthy.

### start_period

Grace period before checking starts.

---

# 14. depends_on with service_healthy

Old Compose

```
depends_on
```

only controlled

```
Start Order
```

Example

Rabbit

↓

Config Server

↓

Accounts

Config Server may still be starting.

Accounts may fail.

Using

```
condition:

service_healthy
```

Docker waits until the health check passes.

---

# 15. Environment Variables

Instead of writing

```properties
spring.application.name=accounts
```

we pass

```yaml
SPRING_APPLICATION_NAME
```

Docker automatically maps it to Spring Boot.

---

## Config Import

```yaml
SPRING_CONFIG_IMPORT
```

Equivalent

```properties
spring.config.import=
```

---

## Profiles

```yaml
SPRING_PROFILES_ACTIVE
```

Equivalent

```properties
spring.profiles.active
```

---

# 16. Docker DNS

Docker automatically creates DNS entries.

Example

```
rabbit

configserver

accounts

loans

cards
```

Every container can communicate using these names.

No IP address required.

---

# 17. common-config.yml

Purpose

Avoid duplicate configuration.

Without it

Every service contains

```
deploy

networks

environment
```

Repeated multiple times.

---

## network-deploy-service

Provides

```
Network Configuration
```

Every service extending this configuration automatically joins

```
nagarjun20
```

---

## microservice-base-config

Extends

```
network-deploy-service
```

Adds

```
Memory Limits
```

```
700 MB
```

---

## microservice-configserver-config

Extends

```
microservice-base-config
```

Adds

```
SPRING_CONFIG_IMPORT

SPRING_PROFILES_ACTIVE
```

Now every microservice automatically receives these values.

---

# 18. extends

Instead of repeating

```yaml
deploy

environment

networks
```

we write

```yaml
extends:

file: common-config.yml

service:

microservice-configserver-config
```

Docker copies the common configuration into the service.

This is similar to inheritance in Java.

```
Parent Class

↓

Child Class
```

---

# Inheritance Hierarchy

```
network-deploy-service

↓

microservice-base-config

↓

microservice-configserver-config

↓

Accounts

Loans

Cards
```

Config Server extends

```
microservice-base-config
```

because it does not need

```
SPRING_CONFIG_IMPORT
```

RabbitMQ extends only

```
network-deploy-service
```

because it doesn't require Spring Boot configuration.

---

# 19. Startup Flow

```
docker compose up

↓

Create Network

↓

Start RabbitMQ

↓

RabbitMQ Healthy

↓

Start Config Server

↓

Config Server Healthy

↓

Start Accounts

↓

Start Loans

↓

Start Cards
```

Everything starts in the correct order.

---

# 20. Useful Docker Compose Commands

Start application

```bash
docker compose up
```

Detached mode

```bash
docker compose up -d
```

Stop application

```bash
docker compose down
```

View logs

```bash
docker compose logs
```

Logs of one service

```bash
docker compose logs accounts
```

Restart

```bash
docker compose restart accounts
```

List containers

```bash
docker compose ps
```

---

# 21. Best Practices

✔ Use one network for related services.

✔ Use health checks.

✔ Use `depends_on` with `service_healthy`.

✔ Keep common configuration in a separate file.

✔ Store only service-specific configuration inside `docker-compose.yml`.

✔ Avoid hardcoding IP addresses.

✔ Use Docker DNS (service names).

✔ Keep memory limits for every service.

✔ Keep environment variables external whenever possible.

---

# 22. Summary

This project demonstrates several important Docker Compose concepts:

- Deploying multiple Spring Boot microservices.
- Running RabbitMQ with the Management UI.
- Creating a shared Docker bridge network.
- Using Docker DNS for service-to-service communication.
- Using health checks to verify service readiness.
- Starting services in the correct order with `depends_on`.
- Reducing duplicate configuration through `common-config.yml`.
- Reusing common settings with `extends`.
- Managing Spring Boot configuration using environment variables.

Together, these practices make the Compose configuration cleaner, easier to maintain, and closer to how multi-container applications are organized in real-world development environments.