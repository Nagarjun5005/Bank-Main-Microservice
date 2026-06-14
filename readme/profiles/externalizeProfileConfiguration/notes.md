# Externalized Configuration in Spring Boot

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Complete guide to externalized configuration in Spring Boot - providing configuration values outside application code for multi-environment deployment.

---

## 📋 Table of Contents

- [Overview](#overview)
- [What is Externalized Configuration](#what-is-externalized-configuration)
- [Why Use Externalized Configuration](#why-use-externalized-configuration)
- [Configuration Sources](#configuration-sources)
- [Configuration Priority Order](#configuration-priority-order)
- [Methods to Override Configuration](#methods-to-override-configuration)
    - [Command Line Arguments](#command-line-arguments)
    - [JVM System Properties](#jvm-system-properties)
    - [Environment Variables](#environment-variables)
    - [Profile-Specific Files](#profile-specific-files)
- [IntelliJ IDEA Setup](#intellij-idea-setup)
- [Real-World Examples](#real-world-examples)
- [Environment Mapping](#environment-mapping)
- [Interview Questions](#interview-questions)
- [Practical Scenarios](#practical-scenarios)
- [Best Practices](#best-practices)

---

## 🎯 Overview

Externalized Configuration is a fundamental principle in modern application development that allows you to keep configuration values **outside the application code**. This enables the same application to run in different environments (Local, QA, UAT, Production) without recompiling or modifying the code.

### Key Benefits

✅ **Environment Independence** - Same JAR runs everywhere  
✅ **Security** - Sensitive data not in source code  
✅ **Flexibility** - Change config without redeployment  
✅ **Scalability** - Easy multi-environment management  
✅ **DevOps Friendly** - Supports containerization & orchestration

---

## 📝 What is Externalized Configuration

Externalized Configuration means **storing application configuration outside the codebase** in:

- Configuration files (`application.yml`, `application.properties`)
- Environment variables
- JVM system properties
- Command-line arguments
- Config servers (Spring Cloud Config)
- Cloud platforms (AWS, Azure, GCP)

### Example

Instead of hardcoding:

```java
// ❌ BAD - Hardcoded
String dbUrl = "jdbc:mysql://localhost:3306/localdb";
String environment = "local";
```

Use externalized configuration:

```java
// ✅ GOOD - Externalized
@Value("${database.url}")
private String dbUrl;

@Value("${app.environment}")
private String environment;
```

**application.yml:**
```yaml
database:
  url: jdbc:mysql://localhost:3306/localdb
app:
  environment: local
```

---

## 🤔 Why Use Externalized Configuration

### Problem Without Externalization

```
Code Changes Required for Different Environments
↓
Environment: Local  →  Rebuild with local config
Environment: QA    →  Rebuild with QA config
Environment: UAT   →  Rebuild with UAT config
Environment: Prod  →  Rebuild with prod config
```

**Issues:**
- ❌ Recompilation required for each environment
- ❌ Risk of deploying wrong version
- ❌ Configuration drift
- ❌ Security risks (credentials in code)
- ❌ Difficult to maintain

### Solution With Externalization

```
Single Build Artifact (JAR/WAR)
↓
Deploy to Any Environment with Different Config
↓
No Recompilation Needed
```

**Benefits:**
- ✅ One build for all environments
- ✅ Configuration management independent of code
- ✅ Secure credential handling
- ✅ Easy rollback & updates
- ✅ DevOps friendly

---

## 🔧 Configuration Sources

### 1. Base Configuration File

**application.yml:**
```yaml
build:
  version: "1.0"

app:
  name: My Application
  environment: local

server:
  port: 8080

database:
  url: jdbc:mysql://localhost:3306/mydb
  username: admin
  password: secret

feature:
  analytics:
    enabled: true
  dark-mode:
    enabled: false
```

**Or application.properties:**
```properties
build.version=1.0
app.name=My Application
app.environment=local
server.port=8080
database.url=jdbc:mysql://localhost:3306/mydb
database.username=admin
database.password=secret
feature.analytics.enabled=true
feature.dark-mode.enabled=false
```

### 2. Profile-Specific Files

**application-qa.yml:**
```yaml
app:
  environment: qa
database:
  url: jdbc:mysql://qa-server:3306/qa_db
  username: qa_user
  password: qa_pass
```

**application-prod.yml:**
```yaml
app:
  environment: production
database:
  url: jdbc:mysql://prod-server:3306/prod_db
  username: prod_user
  password: ${DB_PASSWORD}  # From environment variable
```

### 3. Reading Values in Code

```java
@RestController
@RequestMapping("api")
public class ConfigController {

    @Value("${build.version}")
    private String buildVersion;

    @Value("${app.name}")
    private String appName;

    @Value("${app.environment}")
    private String environment;

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("version", buildVersion);
        config.put("name", appName);
        config.put("environment", environment);
        return ResponseEntity.ok(config);
    }
}
```

**Default Output:**
```
version: 1.0
name: My Application
environment: local
```

---

## ⬆️ Configuration Priority Order

Spring Boot follows a **strict priority order** for configuration resolution. **Higher priority overrides lower priority.**

### Priority Levels (Highest to Lowest)

| Priority | Source | Example |
|----------|--------|---------|
| **1** 🔴 | Command Line Arguments | `--build.version=2.0` |
| **2** 🟠 | JVM System Properties | `-Dbuild.version=3.0` |
| **3** 🟡 | OS Environment Variables | `BUILD_VERSION=4.0` |
| **4** 🟢 | Profile-Specific Properties | `application-qa.yml` |
| **5** 🔵 | Base Properties | `application.yml` |

### Visual Representation

```
Command Line Arguments           ▲
        ↓                        │
JVM System Properties            │ Higher
        ↓                        │ Priority
Environment Variables            │
        ↓                        │
Profile-Specific Config          │
        ↓                        ▼
Base Configuration (application.yml/properties)
```

### Example: Priority Demonstration

#### Base Configuration (Lowest Priority)

**application.yml:**
```yaml
build:
  version: "1.0"
```

#### Profile-Specific Configuration

**application-qa.yml:**
```yaml
build:
  version: "1.5"
```

#### Environment Variable

```bash
export BUILD_VERSION=2.0
```

#### JVM Option

```bash
-Dbuild.version=3.0
```

#### Command Line Argument (Highest Priority)

```bash
--build.version=4.0
```

#### Final Output

```
4.0
```

**Why 4.0?**

Command Line Arguments have the **highest priority** and override all other sources.

---

## 🔄 Methods to Override Configuration

### Method 1: Command Line Arguments

**Highest Priority** - Used when running JAR files.

#### Syntax

```bash
java -jar myapp.jar --property.name=value
```

#### Example

```bash
java -jar myapp.jar --build.version=2.0 --server.port=9090
```

#### In IntelliJ IDEA

1. Go to **Run → Edit Configurations**
2. Find **Program Arguments** field
3. Add: `--build.version=2.0`
4. Click **Apply** and **OK**

#### Code Example

```java
@Value("${build.version}")
private String buildVersion;

// If run with: --build.version=2.0
// Output: 2.0
```

#### Use Cases

- ✅ Override single property for testing
- ✅ Production deployments with specific values
- ✅ Quick testing without configuration files
- ✅ Kubernetes environment variables

---

### Method 2: JVM System Properties

**Second Highest Priority** - Set before application starts.

#### Syntax

```bash
java -Dproperty.name=value -jar myapp.jar
```

#### Example

```bash
java -Dbuild.version=3.0 -Dserver.port=9090 -jar myapp.jar
```

#### In IntelliJ IDEA

1. Go to **Run → Edit Configurations**
2. Find **VM Options** field
3. Add: `-Dbuild.version=3.0`
4. Click **Apply** and **OK**

#### Code Example

```java
@Value("${build.version}")
private String buildVersion;

// If run with: -Dbuild.version=3.0
// Output: 3.0
```

#### Use Cases

- ✅ JVM memory settings
- ✅ System-wide properties
- ✅ Performance tuning parameters
- ✅ Debug and logging configurations

---

### Method 3: Environment Variables

**Third Priority** - OS-level variables, commonly used in containers.

#### Syntax

```bash
export PROPERTY_NAME=value
java -jar myapp.jar
```

#### Naming Convention

Spring automatically converts environment variable names to property names:

| Environment Variable | Property Name |
|---------------------|----------------|
| `BUILD_VERSION` | `build.version` |
| `ACCOUNTS_MESSAGE` | `accounts.message` |
| `SPRING_DATASOURCE_URL` | `spring.datasource.url` |
| `DATABASE_HOST` | `database.host` |
| `DATABASE_USERNAME` | `database.username` |

**Rule:** Uppercase with underscores → Lowercase with dots

#### Example

```bash
# Set environment variable
export BUILD_VERSION=4.0
export SERVER_PORT=9090
export DATABASE_URL=jdbc:mysql://prod-server:3306/proddb

# Run application
java -jar myapp.jar
```

#### In IntelliJ IDEA

1. Go to **Run → Edit Configurations**
2. Find **Environment Variables** section
3. Add: `BUILD_VERSION=4.0`
4. Add: `SERVER_PORT=9090`
5. Click **Apply** and **OK**

#### Code Example

```java
@Value("${build.version}")
private String buildVersion;

@Value("${server.port}")
private int serverPort;

@Value("${database.url}")
private String databaseUrl;

// If run with environment variables:
// BUILD_VERSION=4.0
// SERVER_PORT=9090
// DATABASE_URL=jdbc:mysql://prod-server:3306/proddb
// Output:
// buildVersion: 4.0
// serverPort: 9090
// databaseUrl: jdbc:mysql://prod-server:3306/proddb
```

#### Use Cases

- ✅ Docker containers (ENTRYPOINT with env vars)
- ✅ Kubernetes (ConfigMaps & Secrets)
- ✅ Cloud platforms (AWS, Azure, GCP)
- ✅ CI/CD pipelines
- ✅ Linux/Windows system-wide settings

#### Docker Example

**Dockerfile:**
```dockerfile
FROM openjdk:17-jdk-slim
COPY myapp.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Run with environment variables:**
```bash
docker run \
  -e BUILD_VERSION=4.0 \
  -e DATABASE_URL=jdbc:mysql://db-server:3306/proddb \
  -e SERVER_PORT=8080 \
  myapp:latest
```

#### Kubernetes Example

**ConfigMap:**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  BUILD_VERSION: "4.0"
  DATABASE_URL: "jdbc:mysql://db-server:3306/proddb"
  SERVER_PORT: "8080"
```

**Pod Spec:**
```yaml
spec:
  containers:
  - name: myapp
    image: myapp:latest
    envFrom:
    - configMapRef:
        name: app-config
```

---

### Method 4: Profile-Specific Files

**Fourth Priority** - Different configurations per environment.

#### File Structure

```
src/main/resources/
├── application.yml                 (Base configuration)
├── application-local.yml           (Local environment)
├── application-dev.yml             (Development)
├── application-qa.yml              (QA)
├── application-uat.yml             (UAT)
└── application-prod.yml            (Production)
```

#### Base Configuration

**application.yml:**
```yaml
build:
  version: "1.0"

app:
  name: My Application

database:
  driver: com.mysql.cj.jdbc.Driver
  connection-timeout: 5000

logging:
  level:
    root: INFO
```

#### Local Profile

**application-local.yml:**
```yaml
app:
  environment: local
  debug: true

database:
  url: jdbc:mysql://localhost:3306/local_db
  username: local_user
  password: local_pass

server:
  port: 8080

logging:
  level:
    root: DEBUG
    com.myapp: TRACE
```

#### Development Profile

**application-dev.yml:**
```yaml
app:
  environment: development
  debug: true

database:
  url: jdbc:mysql://dev-server:3306/dev_db
  username: dev_user
  password: ${DATABASE_PASSWORD}

server:
  port: 8080

logging:
  level:
    root: INFO
```

#### QA Profile

**application-qa.yml:**
```yaml
app:
  environment: qa
  debug: false

database:
  url: jdbc:mysql://qa-server:3306/qa_db
  username: qa_user
  password: ${DATABASE_PASSWORD}

server:
  port: 8080

logging:
  level:
    root: INFO
```

#### Production Profile

**application-prod.yml:**
```yaml
app:
  environment: production
  debug: false

database:
  url: jdbc:mysql://prod-server:3306/prod_db
  username: ${DATABASE_USERNAME}
  password: ${DATABASE_PASSWORD}

server:
  port: 8080
  ssl:
    enabled: true
    key-store: ${KEYSTORE_PATH}
    key-store-password: ${KEYSTORE_PASSWORD}

logging:
  level:
    root: WARN
    com.myapp: INFO
```

#### Activating Profiles

**Option 1: Command Line**
```bash
java -jar myapp.jar --spring.profiles.active=prod
```

**Option 2: Program Argument in IntelliJ**
```
--spring.profiles.active=qa
```

**Option 3: Environment Variable**
```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar myapp.jar
```

**Option 4: application.properties**
```properties
spring.profiles.active=qa
```

**Option 5: IntelliJ Environment Variables**
```
SPRING_PROFILES_ACTIVE=qa
```

---

## 🖥️ IntelliJ IDEA Setup

### Complete Configuration Steps

#### Step 1: Open Run Configurations

1. Click **Run** menu
2. Select **Edit Configurations...**

#### Step 2: Configure Run Profile

1. Select your application configuration (or create new)
2. Configure the following fields:

### Field 1: Active Profiles

```
Active Profiles: qa
```

**What it does:**
- Loads `application-qa.yml` in addition to `application.yml`
- Values in `application-qa.yml` override `application.yml`

**Example:**
```yaml
# cards.yml
app.environment: local

# cards-qa.yml
app.environment: qa

# Result: qa
```

### Field 2: Program Arguments

```
Program Arguments: --build.version=2.0 --server.port=9090
```

**What it does:**
- Passes command-line arguments to the application
- **Highest priority** - overrides all other sources

**Example:**
```bash
# Results in:
build.version = 2.0
server.port = 9090
```

### Field 3: VM Options

```
VM Options: -Dbuild.version=3.0 -Dserver.port=9090
```

**What it does:**
- Sets JVM system properties
- **Second highest priority**

**Example:**
```bash
# Results in:
build.version = 3.0
server.port = 9090
```

### Field 4: Environment Variables

```
BUILD_VERSION=4.0
SERVER_PORT=9090
DATABASE_URL=jdbc:mysql://localhost:3306/mydb
SPRING_PROFILES_ACTIVE=qa
```

**What it does:**
- Sets OS environment variables
- **Third highest priority**

**Example:**
```bash
# Results in:
build.version = 4.0 (from BUILD_VERSION)
server.port = 9090 (from SERVER_PORT)
database.url = jdbc:mysql://localhost:3306/mydb
# Also loads cards-qa.yml
```

### IntelliJ Configuration Summary

| Field | Value | Priority |
|-------|-------|----------|
| **Active Profiles** | `qa` | 4️⃣ |
| **Program Arguments** | `--build.version=2.0` | 1️⃣ |
| **VM Options** | `-Dbuild.version=3.0` | 2️⃣ |
| **Environment Variables** | `BUILD_VERSION=4.0` | 3️⃣ |

---

## 🌍 Environment Mapping

Spring Boot automatically converts environment variable names to property names.

### Conversion Rules

**Rule:** Uppercase with underscores → Lowercase with dots

### Mapping Examples

| YAML Property | Environment Variable |
|---------------|---------------------|
| `build.version` | `BUILD_VERSION` |
| `server.port` | `SERVER_PORT` |
| `accounts.message` | `ACCOUNTS_MESSAGE` |
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` |
| `database.connection.timeout` | `DATABASE_CONNECTION_TIMEOUT` |
| `app.security.jwt.secret` | `APP_SECURITY_JWT_SECRET` |
| `feature.dark.mode.enabled` | `FEATURE_DARK_MODE_ENABLED` |

### Example Conversion

**application.yml:**
```yaml
database:
  host: localhost
  port: 3306
  username: admin
  password: secret
```

**Environment Variables:**
```bash
export DATABASE_HOST=prod-server
export DATABASE_PORT=3306
export DATABASE_USERNAME=prod_user
export DATABASE_PASSWORD=prod_secret
```

**Result:**
```
database.host → prod-server
database.port → 3306
database.username → prod_user
database.password → prod_secret
```

---

## 💼 Real-World Examples

### Example 1: Local Development

**application.yml (Base):**
```yaml
app:
  name: My App
  version: 1.0
  
database:
  url: jdbc:mysql://localhost:3306/local_db
```

**IntelliJ Settings:**
- Active Profiles: (empty)
- Environment Variables: (none)

**Output:**
```
app.name = My App
database.url = jdbc:mysql://localhost:3306/local_db
```

---

### Example 2: QA Environment

**application.yml:**
```yaml
app:
  name: My App
  version: 1.0
  
database:
  url: jdbc:mysql://localhost:3306/local_db
```

**application-qa.yml:**
```yaml
app:
  version: 1.0-QA
  
database:
  url: jdbc:mysql://qa-server:3306/qa_db
```

**IntelliJ Settings:**
- Active Profiles: `qa`

**Output:**
```
app.name = My App (from base)
app.version = 1.0-QA (overridden by qa profile)
database.url = jdbc:mysql://qa-server:3306/qa_db (overridden by qa profile)
```

---

### Example 3: Production with Overrides

**application.yml:**
```yaml
app:
  name: My App
  version: 1.0
  
database:
  url: jdbc:mysql://localhost:3306/local_db
  username: admin
  password: secret
  
server:
  port: 8080
```

**application-prod.yml:**
```yaml
app:
  version: 2.0-PROD
  
database:
  url: jdbc:mysql://prod-server:3306/prod_db
  username: ${DATABASE_USERNAME}
  password: ${DATABASE_PASSWORD}

server:
  port: 443
```

**Command to Run:**
```bash
java -jar myapp.jar \
  --spring.profiles.active=prod \
  --server.port=8443
```

**Environment Variables:**
```bash
export DATABASE_USERNAME=prod_user
export DATABASE_PASSWORD=prod_secure_pass
```

**Final Output:**
```
app.name = My App
app.version = 2.0-PROD
database.url = jdbc:mysql://prod-server:3306/prod_db
database.username = prod_user
database.password = prod_secure_pass
server.port = 8443 (overridden by command line arg)
```

---

### Example 4: Multi-Source Override

**Scenario:** All configuration methods are used simultaneously.

**application.yml:**
```yaml
build:
  version: "1.0"
server:
  port: 8080
```

**application-qa.yml:**
```yaml
build:
  version: "1.5"
server:
  port: 8081
```

**Environment Variable:**
```bash
BUILD_VERSION=2.0
```

**JVM Option:**
```bash
-Dbuild.version=3.0
```

**Command Line Argument:**
```bash
--build.version=4.0
```

**Final Output:**
```
build.version = 4.0  (Highest: Command Line)
server.port = 8081   (From qa profile)
```

**Why?**
- Command line argument (4.0) overrides JVM property (3.0)
- JVM property would override environment variable
- Environment variable would override profile
- Profile would override base config

---

## ❓ Interview Questions

### Q1: What is Externalized Configuration?

**Answer:**
Externalized Configuration means keeping configuration values **outside the application code** in separate files or external sources. This allows the same application to run in different environments (Local, QA, UAT, Production) without code changes or recompilation.

**Key Points:**
- Configuration stored separately from code
- Same JAR/WAR deployed everywhere
- Environment-specific values provided at runtime
- Improves security, flexibility, and maintainability

---

### Q2: How can Configuration Values be Overridden?

**Answer:**
Configuration can be overridden using four methods (in order of priority):

1. **Command Line Arguments** (Highest)
   ```bash
   --build.version=2.0
   ```

2. **JVM System Properties** (Second)
   ```bash
   -Dbuild.version=3.0
   ```

3. **Environment Variables** (Third)
   ```bash
   BUILD_VERSION=4.0
   ```

4. **Profile-Specific Files** (Lowest)
   ```yaml
   cards-qa.yml
   ```

**Higher priority always overrides lower priority.**

---

### Q3: Which Configuration Source has the Highest Priority?

**Answer:**
**Command Line Arguments** have the highest priority.

**Example:**
```bash
java -jar myapp.jar --build.version=4.0
```

This will override:
- JVM properties (`-Dbuild.version=3.0`)
- Environment variables (`BUILD_VERSION=2.0`)
- Profile files (`application.yml`)

**Use Case:** Quick overrides without changing code or environment.

---

### Q4: Which Approach is Commonly Used in Kubernetes?

**Answer:**
**Environment Variables** are commonly used in Kubernetes, along with:
- **ConfigMaps** (for non-sensitive configuration)
- **Secrets** (for sensitive data like passwords)

**Example:**

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  BUILD_VERSION: "2.0"
  DATABASE_HOST: "prod-server"
  
---
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
type: Opaque
stringData:
  DATABASE_PASSWORD: "secure_password"
  
---
apiVersion: v1
kind: Pod
metadata:
  name: myapp
spec:
  containers:
  - name: app
    image: myapp:latest
    envFrom:
    - configMapRef:
        name: app-config
    - secretRef:
        name: app-secrets
```

**Why Kubernetes prefers this approach:**
- ✅ Secure (Secrets are encrypted)
- ✅ Easy to manage
- ✅ Can be updated without pod restart
- ✅ Standard Kubernetes pattern

---

### Q5: How does Spring Map Environment Variables to Properties?

**Answer:**
Spring uses automatic conversion following these rules:

**Rule:** Uppercase with underscores → Lowercase with dots

**Examples:**

| Environment Variable | Property Name |
|---------------------|----------------|
| `BUILD_VERSION` | `build.version` |
| `SERVER_PORT` | `server.port` |
| `DATABASE_URL` | `database.url` |
| `SPRING_DATASOURCE_PASSWORD` | `spring.datasource.password` |

**Conversion Process:**
```
BUILD_VERSION
    ↓
Convert to lowercase: build_version
    ↓
Replace underscores with dots: build.version
```

---

### Q6: What is the Difference between Profile-Specific Files and Environment Variables?

**Answer:**

| Aspect | Profile-Specific Files | Environment Variables |
|--------|----------------------|----------------------|
| **Storage** | Inside JAR/classpath | OS/Container level |
| **Priority** | Lower (4th) | Higher (3rd) |
| **Change Method** | Rebuild required | No rebuild needed |
| **Best For** | Static environment config | Dynamic values |
| **Security** | May be exposed in JAR | Can be encrypted (Secrets) |
| **Example** | `application-prod.yml` | `DATABASE_PASSWORD=xyz` |

---

### Q7: Why Not Hardcode Configuration?

**Answer:**
Hardcoding configuration is **BAD** because:

❌ **Code Duplication** - Different versions for each environment  
❌ **Security Risk** - Credentials in source code  
❌ **Maintenance Nightmare** - Difficult to update  
❌ **Deployment Risk** - Chance of deploying wrong version  
❌ **Not Cloud-Native** - Doesn't work with containers/Kubernetes  
❌ **Compliance Issues** - Fails security audits

**Example of Bad Practice:**
```java
// ❌ NEVER DO THIS
if (environment.equals("prod")) {
    dbUrl = "jdbc:mysql://prod-server:3306/proddb";
    apiKey = "prod_key_123";
} else if (environment.equals("qa")) {
    dbUrl = "jdbc:mysql://qa-server:3306/qadb";
    apiKey = "qa_key_456";
}
```

**Correct Approach:**
```java
// ✅ DO THIS
@Value("${database.url}")
private String dbUrl;

@Value("${api.key}")
private String apiKey;
```

---

## 📊 Practical Scenarios

### Scenario 1: Running Same App in Three Environments

**Setup:**
```bash
# Local Development
./mvnw spring-boot:run

# QA Environment
java -jar app.jar --spring.profiles.active=qa

# Production
java -jar app.jar --spring.profiles.active=prod
```

**Same JAR** (`app.jar`) runs everywhere with different configurations!

---

### Scenario 2: Docker Deployment

**Dockerfile:**
```dockerfile
FROM openjdk:17-jdk-slim
COPY app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Run Locally:**
```bash
docker run \
  -e SPRING_PROFILES_ACTIVE=local \
  -e DATABASE_URL=jdbc:mysql://localhost:3306/localdb \
  myapp:latest
```

**Run in QA:**
```bash
docker run \
  -e SPRING_PROFILES_ACTIVE=qa \
  -e DATABASE_URL=jdbc:mysql://qa-server:3306/qadb \
  -e DATABASE_USERNAME=qa_user \
  -e DATABASE_PASSWORD=qa_pass \
  myapp:latest
```

**Run in Production:**
```bash
docker run \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:mysql://prod-server:3306/proddb \
  -e DATABASE_USERNAME=${DB_USER} \
  -e DATABASE_PASSWORD=${DB_PASS} \
  -e SERVER_PORT=8443 \
  myapp:latest
```

---

### Scenario 3: Kubernetes Deployment

**values.yaml (Helm):**
```yaml
app:
  version: "2.0"
  environment: prod
  
database:
  url: jdbc:mysql://db-server:3306/proddb
  username: prod_user
```

**deployment.yaml:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp
spec:
  template:
    spec:
      containers:
      - name: app
        image: myapp:2.0
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: prod
        - name: DATABASE_URL
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: database-url
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: db-password
```

---

### Scenario 4: Multi-Region Deployment

**application.yml (Base):**
```yaml
app:
  name: My Global App
  version: 1.0
```

**application-us-east.yml:**
```yaml
app:
  region: us-east
  database:
    url: jdbc:mysql://us-east-db:3306/db
```

**application-eu-west.yml:**
```yaml
app:
  region: eu-west
  database:
    url: jdbc:mysql://eu-west-db:3306/db
```

**Deploy to US:**
```bash
java -jar app.jar --spring.profiles.active=us-east
```

**Deploy to Europe:**
```bash
java -jar app.jar --spring.profiles.active=eu-west
```

---

## ✅ Best Practices

### 1. Use Profiles for Environment-Specific Config

```yaml
# ✅ GOOD
cards.yml          # Base config
application-dev.yml      # Dev specific
cards-prod.yml     # Prod specific
```

### 2. Never Hardcode Sensitive Data

```java
// ❌ BAD
private String apiKey = "secret_key_123";

// ✅ GOOD
@Value("${api.key}")
private String apiKey;
```

### 3. Use Environment Variables for Secrets

```bash
# ✅ GOOD - In production
export API_KEY=secret_key_123
export DATABASE_PASSWORD=secure_pass
```

### 4. Document Configuration Options

```properties
# application.properties
# Build Configuration
build.version=1.0
build.timestamp=${timestamp}

# Server Configuration
server.port=8080
server.servlet.context-path=/api

# Database Configuration
database.url=jdbc:mysql://localhost:3306/mydb
database.username=admin
database.password=secret
```

### 5. Use Meaningful Property Names

```yaml
# ✅ GOOD - Clear and hierarchical
app:
  name: My Application
  version: 1.0
  security:
    jwt:
      secret: mySecret
      expiration: 3600000

# ❌ BAD - Unclear names
val1: My Application
val2: 1.0
jwtSecret: mySecret
```

### 6. Validate Configuration Early

```java
@Component
public class ConfigValidator {
    
    @Autowired
    private Environment env;
    
    @PostConstruct
    public void validate() {
        if (!env.containsProperty("database.url")) {
            throw new IllegalStateException(
                "database.url property is required"
            );
        }
    }
}
```

---

## 📚 Summary Table

### Configuration Methods Summary

| Method | Priority | Use Case | Example |
|--------|----------|----------|---------|
| **Command Line** | 1st (Highest) | Override for testing | `--build.version=2.0` |
| **JVM Property** | 2nd | System-level config | `-Dbuild.version=3.0` |
| **Env Variable** | 3rd | Containers/Kubernetes | `BUILD_VERSION=4.0` |
| **Profile File** | 4th | Environment setup | `application-qa.yml` |
| **Base File** | 5th (Lowest) | Default values | `application.yml` |

### IntelliJ Configuration Fields

| Field | Priority | Example |
|-------|----------|---------|
| **Program Arguments** | 1st | `--build.version=2.0` |
| **VM Options** | 2nd | `-Dbuild.version=3.0` |
| **Environment Variables** | 3rd | `BUILD_VERSION=4.0` |
| **Active Profiles** | 4th | `qa` |

---

## 🎓 Learning Path

1. ✅ Understand externalized configuration concept
2. ✅ Learn configuration priority order
3. ✅ Practice with command-line arguments
4. ✅ Configure JVM properties
5. ✅ Set up environment variables
6. ✅ Create profile-specific files
7. ✅ Test multi-environment setup
8. ✅ Practice in IntelliJ IDEA
9. ✅ Deploy to Docker/Kubernetes
10. ✅ Implement in real projects

---

## 🔗 Related Topics

- [Spring Boot Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Environment Variables](https://docs.spring.io/spring-framework/reference/core/beans/environment.html)
- [Spring Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [@Value Annotation](https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/value-annotations.html)
- [@ConfigurationProperties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.typesafe-configuration-properties)

---

<div align="center">

**Made with ❤️ by the Spring Community**

⭐ Star this repository if you found it helpful!

[⬆ Back to Top](#externalized-configuration-in-spring-boot)

</div>