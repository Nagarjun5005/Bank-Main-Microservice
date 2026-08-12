# Running MySQL Databases Using Docker Containers

## Overview

In a microservices architecture, each service should ideally own its own database. This follows the **Database per Service** pattern, where each microservice manages its own data independently.

In this project, three separate MySQL containers are created:

- Accounts Database
- Loans Database
- Cards Database

Each database runs inside its own Docker container and is mapped to a different port on the host machine.

---

# Why Separate Databases?

Instead of sharing one database, every microservice has its own database.

Benefits:

- Loose coupling
- Independent deployments
- Independent schema evolution
- Better security
- Better scalability
- No direct database sharing between services

Architecture

```
                Spring Boot Microservices

        +------------+    +------------+    +------------+
        | Accounts   |    | Loans      |    | Cards      |
        +------+-----+    +------+-----+    +------+-----+
               |                  |                  |
               |                  |                  |
               ▼                  ▼                  ▼
        +------------+    +------------+    +------------+
        | AccountsDB |    | LoansDB    |    | CardsDB    |
        +------------+    +------------+    +------------+
```

Each service communicates only with its own database.

---

# Why Different Ports?

MySQL uses port

```
3306
```

by default.

The local machine already had MySQL running on port

```
3306
```

Therefore Docker could not expose another container on the same port.

To avoid the conflict, different host ports were assigned.

| Container | Host Port | Container Port |
|-----------|----------:|---------------:|
| AccountsDB | 3307 | 3306 |
| LoansDB | 3308 | 3306 |
| CardsDB | 3309 | 3306 |

Inside every container MySQL still runs on port **3306**.

Only the host ports are different.

---

# Creating the Accounts Database Container

Command

```bash
docker run \
-p 3307:3306 \
--name accountsdb \
-e MYSQL_ROOT_PASSWORD=root \
-e MYSQL_DATABASE=accountsdb \
-d mysql
```

---

## Command Breakdown

### docker run

Creates and starts a new Docker container.

---

### -p 3307:3306

Port Mapping

```
Host Machine

3307
   │
   ▼
Docker Container

3306
```

Meaning

- Host Port = 3307
- Container Port = 3306

Applications running on the host connect using

```
localhost:3307
```

---

### --name accountsdb

Assigns the container name

```
accountsdb
```

instead of a random Docker-generated name.

Useful commands

```bash
docker start accountsdb

docker stop accountsdb

docker logs accountsdb
```

---

### -e MYSQL_ROOT_PASSWORD=root

Creates the MySQL root user.

```
Username

root
```

Password

```
root
```

---

### -e MYSQL_DATABASE=accountsdb

Automatically creates the database

```
accountsdb
```

when the container starts for the first time.

No manual SQL commands are required.

---

### -d

Runs the container in detached mode.

The terminal is immediately returned while the container continues running in the background.

---

# Creating the Loans Database

Command

```bash
docker run \
-p 3308:3306 \
--name loansdb \
-e MYSQL_ROOT_PASSWORD=root \
-e MYSQL_DATABASE=loansdb \
-d mysql
```

Host Port

```
3308
```

Database

```
loansdb
```

---

# Creating the Cards Database

Command

```bash
docker run \
-p 3309:3306 \
--name cardsdb \
-e MYSQL_ROOT_PASSWORD=root \
-e MYSQL_DATABASE=cardsdb \
-d mysql
```

Host Port

```
3309
```

Database

```
cardsdb
```

---

# Docker Port Mapping

Although every MySQL container uses

```
3306
```

internally,

Docker maps them to different ports on the host.

```
Host Machine

3307  ─────────► AccountsDB

3308  ─────────► LoansDB

3309  ─────────► CardsDB

                    │
                    ▼

             MySQL (3306)
```

This avoids port conflicts while allowing multiple MySQL containers to run simultaneously.

---

# Verifying Running Containers

List running containers

```bash
docker ps
```

Example

```
CONTAINER ID   IMAGE   PORTS

accountsdb     mysql   3307->3306

loansdb        mysql   3308->3306

cardsdb        mysql   3309->3306
```

---

# Viewing All Containers

```bash
docker ps -a
```

Displays both running and stopped containers.

---

# Viewing Logs

Accounts Database

```bash
docker logs accountsdb
```

Loans Database

```bash
docker logs loansdb
```

Cards Database

```bash
docker logs cardsdb
```

---

# Starting a Container

```bash
docker start accountsdb
```

---

# Stopping a Container

```bash
docker stop accountsdb
```

---

# Removing a Container

```bash
docker rm accountsdb
```

Container must be stopped before removing.

---

# Connecting Spring Boot Applications

Accounts Service

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/accountsdb
spring.datasource.username=root
spring.datasource.password=root
```

Loans Service

```properties
spring.datasource.url=jdbc:mysql://localhost:3308/loansdb
spring.datasource.username=root
spring.datasource.password=root
```

Cards Service

```properties
spring.datasource.url=jdbc:mysql://localhost:3309/cardsdb
spring.datasource.username=root
spring.datasource.password=root
```

> **Note:** Ensure the database name matches the one you created. If you created `loanssdb` (with two `s` characters), use that exact name or recreate the container with the intended database name.

---

# Why Not Use localhost:3306?

The host machine already had a MySQL server listening on

```
3306
```

Running another MySQL container on the same host port would cause a conflict.

Docker would return an error similar to:

```
Ports are not available

listen tcp 0.0.0.0:3306

Only one usage of each socket address is normally permitted.
```

Changing the host port resolves this issue.

---

# Complete Architecture

```
                Host Machine

        localhost:3307
               │
               ▼
        +---------------+
        | AccountsDB    |
        | MySQL:3306    |
        +---------------+

        localhost:3308
               │
               ▼
        +---------------+
        | LoansDB       |
        | MySQL:3306    |
        +---------------+

        localhost:3309
               │
               ▼
        +---------------+
        | CardsDB       |
        | MySQL:3306    |
        +---------------+
```

---

# Summary

- Each microservice owns its own database.
- Three independent MySQL Docker containers are created.
- All MySQL instances use port **3306** internally.
- Docker maps each container to a unique host port (3307, 3308, 3309).
- `MYSQL_ROOT_PASSWORD` sets the root password.
- `MYSQL_DATABASE` creates the initial database automatically.
- `-d` runs the container in the background.
- Spring Boot applications connect using the mapped host ports.
- This approach follows the **Database per Service** pattern commonly used in microservices.