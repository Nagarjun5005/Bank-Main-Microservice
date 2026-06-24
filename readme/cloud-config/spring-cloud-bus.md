# Spring Cloud Bus - Runtime Configuration Refresh

## 📚 Overview

Spring Cloud Bus enables **runtime configuration refresh** for microservices without restarting them. It uses a message broker (RabbitMQ) to broadcast configuration change events across all service instances.

---

## 🏗️ Architecture

```
Git Repository (config files)
        ↓
Config Server (Port 8071)
        ↓ (publishes events)
RabbitMQ (Message Broker)
    springCloudBus exchange
        ↓ (routes messages)
    Per-service queues
        ↓
Microservices (Port 8080+)
    @RefreshScope beans reload configs
```

---

## 🔑 Key Components

### 1. **Config Server**
- Reads configurations from Git repository
- Serves configs to client applications
- Publishes refresh events to RabbitMQ

### 2. **RabbitMQ**
- Message broker that handles communication
- Creates `springCloudBus` exchange
- Routes refresh events to all subscribers

### 3. **Client Applications (Microservices)**
- Fetch initial config from Config Server
- Listen for refresh events on RabbitMQ
- Reload `@RefreshScope` beans when events arrive

### 4. **Git Repository**
- Stores configuration files
- One file per service/profile
- Example: `accounts-prod.yml`, `accounts.yml`

---

## ⚙️ Configuration

### **Minimal Setup**

```yaml
spring:
  application:
    name: accounts
  
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
  
  cloud:
    bus:
      enabled: true
    stream:
      default-binder: rabbit
      bindings:
        springCloudBusInput:
          destination: springCloudBus
          group: ${spring.application.name}

management:
  endpoints:
    web:
      exposure:
        include: '*'
```

### **Key Properties Explained**

| Property | Purpose | Required |
|----------|---------|----------|
| `cloud.bus.enabled` | Activates Spring Cloud Bus | ✅ Yes |
| `stream.default-binder` | Uses RabbitMQ as broker | ✅ Yes |
| `bindings.group` | Creates named queue (prevents anonymous queue errors) | ✅ Yes |
| `rabbitmq.host/port` | RabbitMQ connection | ✅ Yes |
| `management.endpoints.exposure` | Exposes refresh endpoint | ✅ Yes |

---

## 🚀 How It Works

### **Step-by-Step Flow**

```
1. Developer updates accounts-prod.yml in Git
   └─> Commits and pushes to main branch

2. Config Server polls Git repo (every 5 seconds by default)
   └─> Detects change in accounts-prod.yml

3. Developer triggers refresh via endpoint
   └─> POST http://localhost:8080/actuator/busrefresh

4. Config Server publishes RefreshRemoteApplicationEvent
   └─> Event goes to RabbitMQ springCloudBus exchange

5. RabbitMQ routes event to springCloudBus.accounts queue
   └─> All instances of Accounts Service receive it

6. Accounts Service receives refresh event
   └─> Finds all @RefreshScope beans
   └─> Destroys old bean instances
   └─> Creates new instances with updated config
   └─> @Value annotated fields get new values

7. Response sent back to client
   └─> Lists all properties that were refreshed
```



## 🔄 Triggering Refresh

### **Option 1: Refresh Single Instance**
```bash
curl -X POST http://localhost:8080/actuator/refresh
```
**Response:** Lists changed properties
```json
[
  "accounts.message",
  "accounts.contactDetails.name",
  "accounts.contactDetails.email"
]
```

### **Option 2: Broadcast to All Instances (Recommended)**
```bash
curl -X POST http://localhost:8080/actuator/busrefresh
```
**Effect:** All connected services with same app name receive refresh

### **Option 3: Refresh Specific Service Only**
```bash
curl -X POST http://localhost:8080/actuator/busrefresh?destination=accounts
```

---

## 🐛 Common Issues & Solutions

### **Issue 1: Queue Declaration Failed**

**Error:**
```
Failed to declare queue: springCloudBus.anonymous.xxx
NOT_FOUND - no queue
```

**Cause:** Using anonymous queues instead of named groups

**Fix:**
```yaml
spring:
  cloud:
    stream:
      bindings:
        springCloudBusInput:
          group: ${spring.application.name}  # ← Use named group!
```

---

### **Issue 2: YAML Syntax Error in Config Files**

**Error:**
```
while parsing a block mapping
expected <block end>, but found '<scalar>'
```

**Cause:** Incorrect indentation in Git repository YAML files

**Fix:**
```yaml
# ❌ WRONG - Bad indentation
accounts:
message: "Welcome"

# ✅ CORRECT - Proper 2-space indentation
accounts:
  message: Welcome
  contactDetails:
    name: John
    email: john@example.com
```

---

### **Issue 3: Cannot Find Config Server**

**Error:**
```
Unable to connect to configserver
```

**Cause:** Config Server not started or wrong URL

**Fix:**
```yaml
spring:
  config:
    import: optional:configserver:http://localhost:8071/
    # Use 'optional:' to allow startup without config server
```

---

### **Issue 4: Branch Not Found**

**Error:**
```
No such label: main
Ref master cannot be resolved
```

**Cause:** Git branch doesn't exist or mismatched name

**Fix:**
```bash
# Check available branches
git branch -a

# Update config to match your branch
spring:
  cloud:
    config:
      server:
        git:
          default-label: main  # or 'master' based on your repo
```

---

## 📋 Git Repository Setup

### **Required Files Structure**

```
microservices-config/
├── application.yml              (shared configs)
├── accounts.yml                 (default profile)
├── accounts-prod.yml            (production profile)
├── accounts-dev.yml             (development profile)
├── orders.yml
├── orders-prod.yml
└── README.md
```

### **File Naming Convention**

```
{application-name}-{profile}.yml
or
{application-name}.yml (for default profile)
```

---

## 🔐 Security Considerations

### **1. RabbitMQ Credentials**
```yaml
spring:
  rabbitmq:
    username: ${RABBITMQ_USER}      # Use env variables
    password: ${RABBITMQ_PASSWORD}
```

### **2. Git Repository Credentials**
```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/user/config.git
          username: ${GIT_USERNAME}   # For private repos
          password: ${GIT_TOKEN}      # Use personal access token
```

### **3. Limit Actuator Endpoints**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: refresh,busrefresh,health  # Only needed endpoints
```

---

## 📊 Best Practices

### **1. Use Named Groups (Not Anonymous Queues)**
```yaml
bindings:
  springCloudBusInput:
    group: ${spring.application.name}  # ✅ Named queue
```

### **2. Always Use @RefreshScope**
```java
@RestController
@RefreshScope  // ✅ Required for refresh to work
public class MyController {
    @Value("${my.property}")
    private String property;
}
```

### **3. Organize Config by Profile**
```
accounts.yml          # Default
accounts-dev.yml      # Development
accounts-prod.yml     # Production
```

### **4. Use Meaningful Property Names**
```yaml
# ✅ Good - Hierarchical and clear
accounts:
  message: "..."
  database:
    url: "..."

# ❌ Bad - Flat and unclear
accounts_message: "..."
accounts_database_url: "..."
```

### **5. Test Config Changes Before Production**
```bash
# 1. Update Git in dev branch
# 2. Trigger refresh in dev environment
# 3. Verify changes work
# 4. Merge to main branch
# 5. Trigger refresh in production
```

### **6. Log Configuration Changes**
```java
@Component
@RefreshScope
public class ConfigTracker {
    private static final Logger log = LoggerFactory.getLogger(ConfigTracker.class);
    
    @Value("${accounts.message}")
    private String message;
    
    public void onConfigRefresh() {
        log.info("Configuration refreshed! New message: {}", message);
    }
}
```

---

## 🎯 Complete Example Workflow

### **1. Initial Setup**
```bash
# Start RabbitMQ
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:4-management

# Start Config Server (Port 8071)
mvn spring-boot:run

# Start Accounts Service (Port 8080)
mvn spring-boot:run
```

### **2. Test Initial Config**
```bash
curl http://localhost:8080/accounts/config
# Returns: {"message": "Welcome to NagsBank accounts APIs", ...}
```

### **3. Update Configuration in Git**
```bash
# Edit accounts-prod.yml in GitHub
# Change: message: "Welcome to NagsBank v2.0"
# Commit and push
```

### **4. Trigger Refresh**
```bash
curl -X POST http://localhost:8080/actuator/busrefresh
# Returns: ["accounts.message"]
```

### **5. Verify New Config**
```bash
curl http://localhost:8080/accounts/config
# Returns: {"message": "Welcome to NagsBank v2.0", ...}
```

---

## 📈 What Gets Refreshed

### **✅ These WILL Refresh**
- `@Value` annotated fields in `@RefreshScope` beans
- `@ConfigurationProperties` in `@RefreshScope` components
- Environment variables injected via `@Value`

### **❌ These WON'T Refresh**
- `@Autowired` bean dependencies (recreated, but dependencies not updated)
- Static final fields
- Application name, server port, other bootstrap properties
- Non-@RefreshScope beans

---

## 🚨 Important Notes

1. **@RefreshScope is Mandatory** - Without it, refresh does nothing
2. **RabbitMQ Must be Running** - No message broker = no refresh events
3. **Named Groups Prevent Errors** - Always use `group: ${spring.application.name}`
4. **YAML Indentation Matters** - Use 2 spaces, not tabs
5. **Config Server Must be Reachable** - Client needs access to server URL
6. **Git Push is Required** - Config Server needs to see changes in Git

---

## 📞 Endpoints Reference

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/actuator/refresh` | POST | Refresh single instance |
| `/actuator/busrefresh` | POST | Broadcast refresh to all |
| `/actuator/health` | GET | Check service health |
| `/actuator/env` | GET | View all environment properties |
| `http://configserver:8071/{app}/{profile}` | GET | Get config from server |

---

## 🔗 Quick Links to Remember

- Config Server: `http://localhost:8071`
- Service: `http://localhost:8080`
- RabbitMQ UI: `http://localhost:15672` (guest/guest)
- Refresh Endpoint: `POST /actuator/busrefresh`
- Git Repo: `https://github.com/username/microservices-config`

---

## 📝 Summary

| Aspect | Details |
|--------|---------|
| **Purpose** | Refresh app configs without restart |
| **Trigger** | `POST /actuator/busrefresh` endpoint |
| **Transport** | RabbitMQ message broker |
| **Storage** | Git repository |
| **Scope** | `@RefreshScope` annotated beans |
| **Broadcast** | To all instances of a service |
| **Type** | Event-driven architecture |

**Spring Cloud Bus = Config Server + RabbitMQ + @RefreshScope = Runtime Config Refresh** ✅