# Runtime Configuration Refresh using Spring Cloud Config Monitor, Spring Cloud Bus & GitHub Webhooks

## Overview

By default, Spring Cloud provides two mechanisms to refresh configuration at runtime:

- **`/actuator/refresh`** – Refreshes configuration only for the current application instance.
- **`/actuator/busrefresh`** – Publishes a refresh event through Spring Cloud Bus (RabbitMQ) to refresh all subscribed microservices.

Although both approaches eliminate the need to restart applications, they still require **manual API invocation**. :contentReference[oaicite:0]{index=0}

To fully automate configuration refresh, we can integrate **GitHub Webhooks** with **Spring Cloud Config Monitor**. Whenever a configuration file is updated and pushed to the GitHub Config Repository, GitHub automatically sends a webhook request to the Config Server, which then publishes a refresh event through Spring Cloud Bus.

---

# Architecture

```
                +----------------------+
                |  GitHub Config Repo  |
                +----------+-----------+
                           |
                     Push Changes
                           |
                           ▼
                    GitHub Webhook
                           |
                           ▼
                  Hookdeck (Local Tunnel)
                           |
                           ▼
      http://localhost:8071/monitor
                           |
                           ▼
           Spring Cloud Config Server
                           |
                           ▼
              Spring Cloud Config Monitor
                           |
                           ▼
                 Spring Cloud Bus
                           |
                           ▼
                      RabbitMQ
                           |
          --------------------------------
          |              |               |
          ▼              ▼               ▼
     Accounts        Loans          Cards
```

---

# How it Works

1. Developer updates a configuration file in the GitHub Config Repository.
2. Changes are committed and pushed.
3. GitHub triggers a **Push Event**.
4. GitHub sends a **POST** request to the Config Server's `/monitor` endpoint.
5. Spring Cloud Config Monitor receives the webhook.
6. Config Monitor publishes a refresh event using **Spring Cloud Bus**.
7. RabbitMQ distributes the event to all subscribed microservices.
8. Beans annotated with `@RefreshScope` are recreated and begin using the latest configuration values automatically. :contentReference[oaicite:1]{index=1}

---

# Prerequisites

- Spring Boot
- Spring Cloud Config Server
- Spring Cloud Config Client
- Spring Boot Actuator
- RabbitMQ
- Spring Cloud Bus (AMQP)
- GitHub Config Repository
- Hookdeck (for local development)
- Docker

---

# Dependencies

## All Microservices

```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
implementation 'org.springframework.cloud:spring-cloud-starter-bus-amqp'
```

---

## Config Server

In addition to the above dependencies, add:

```gradle
implementation 'org.springframework.cloud:spring-cloud-config-monitor'
```

The `spring-cloud-config-monitor` dependency exposes a new REST endpoint:

```
POST /monitor
```

> **Note:** `/monitor` is **not** an Actuator endpoint. It is provided by Spring Cloud Config Server. :contentReference[oaicite:2]{index=2}

---

# Actuator Configuration

Expose all required management endpoints in the Config Server.

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

This allows the Config Server to internally trigger the Bus Refresh endpoint whenever a webhook is received. :contentReference[oaicite:3]{index=3}

---

# RabbitMQ Configuration

Configure RabbitMQ in the Config Server (optional if using defaults, but recommended for consistency).

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

Use the same RabbitMQ configuration across all microservices. :contentReference[oaicite:4]{index=4}

---

# Start RabbitMQ

Using Docker:

```bash
docker run -d \
  --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:4-management
```

Verify:

```
http://localhost:15672
```

Default Credentials

```
Username : guest
Password : guest
```

---

# Configure GitHub Webhook

Navigate to:

```
GitHub Repository
    ↓
Settings
    ↓
Webhooks
    ↓
Add Webhook
```

Configure:

### Payload URL

For production:

```
https://your-domain.com/monitor
```

For local development (using Hookdeck):

```
https://hkdk.events/<generated-id>
```

---

### Content Type

```
application/json
```

---

### Secret

Leave empty unless secret validation is configured.

---

### Events

Select:

```
Just the push event
```

The webhook should only trigger whenever configuration changes are pushed to the repository. :contentReference[oaicite:5]{index=5}

---

# Local Development using Hookdeck

GitHub cannot invoke:

```
http://localhost:8071/monitor
```

because `localhost` is only accessible from your local machine.

Hookdeck provides a public HTTPS endpoint that forwards incoming webhook requests to your local Config Server.

## Install Hookdeck

```bash
scoop bucket add hookdeck https://github.com/hookdeck/scoop-hookdeck-cli.git
scoop install hookdeck
```

Login:

```bash
hookdeck login
```

Create a listener:

```bash
hookdeck listen 8071
```

Configure:

```
Port : 8071
Path : /monitor
```

Hookdeck generates a public webhook URL similar to:

```
https://hkdk.events/96xpm20ks07em2
```

GitHub sends webhook requests to this URL, and Hookdeck forwards them to:

```
http://localhost:8071/monitor
```

:contentReference[oaicite:6]{index=6}

---

# Runtime Flow

### Before Change

```
GitHub Config Repo

cards-prod.yml

contact:
  message: "Prod APIs"
```

Microservice Response

```
Prod APIs
```

---

### Update Configuration

Modify:

```
Prod APIs
```

to

```
Webhook APIs
```

Commit and push the change.

---

### Automatic Flow

```
GitHub Push
      │
      ▼
Webhook
      │
      ▼
Hookdeck
      │
      ▼
POST /monitor
      │
      ▼
Spring Cloud Config Monitor
      │
      ▼
Spring Cloud Bus
      │
      ▼
RabbitMQ
      │
      ▼
All Microservices Refreshed
```

Without calling:

```
POST /actuator/refresh
```

or

```
POST /actuator/busrefresh
```

the updated configuration is automatically reflected in all subscribed services. :contentReference[oaicite:7]{index=7}

---

# Verification

1. Start the Config Server.
2. Start RabbitMQ.
3. Start all microservices.
4. Push a configuration change to GitHub.
5. Observe the Config Server logs.

Expected:

```
POST /monitor
```

Followed by Spring Cloud Bus publishing a refresh event.

Verify the updated property by invoking the corresponding microservice endpoint.

---

# Summary of Implementation Steps

1. Add **Spring Boot Actuator** to all microservices.
2. Enable the **Bus Refresh** actuator endpoint.
3. Add **Spring Cloud Bus (AMQP)** dependency to every service.
4. Add **Spring Cloud Config Monitor** dependency **only** to the Config Server.
5. Start RabbitMQ.
6. Configure a GitHub Webhook pointing to the Config Server's `/monitor` endpoint (via Hookdeck for local development).
7. Push configuration changes to GitHub.
8. Configuration refresh occurs automatically across all microservices without any manual API invocation. :contentReference[oaicite:8]{index=8}

---

# Advantages

- No application restart required.
- No manual `/refresh` calls.
- No manual `/busrefresh` calls.
- Fully event-driven configuration refresh.
- Automatically refreshes all subscribed microservices.
- Production-ready solution.
- Easily integrates with GitHub, GitLab, or Bitbucket webhooks. :contentReference[oaicite:9]{index=9}