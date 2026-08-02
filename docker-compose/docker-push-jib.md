# Building and Publishing Docker Images Using Jib

## Overview

After creating the `docker-compose.yml` file, the next step is to create Docker images for each Spring Boot microservice and publish them to Docker Hub. Instead of writing a `Dockerfile` for every microservice, this project uses the **Google Jib Maven Plugin**.

The overall workflow is:

```
Java Source Code
        │
        ▼
 Maven Build (Compile)
        │
        ▼
  Jib Maven Plugin
        │
        ▼
 Local Docker Image
        │
        ▼
 Docker Hub
        │
        ▼
 Docker Compose
        │
        ▼
 Running Containers
```

---

# Why Do We Need Docker Images?

Docker cannot execute Java source code directly.

Our Spring Boot application exists as:

```
Accounts

├── src
├── pom.xml
├── target
└── ...
```

Before Docker can run the application, it must be packaged into a Docker image.

Traditionally, this is done using a Dockerfile.

Example:

```dockerfile
FROM eclipse-temurin:21

COPY target/accounts.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
```

Then build the image using:

```bash
docker build -t accounts .
```

However, maintaining a Dockerfile for every microservice can become repetitive.

---

# Why Use Jib?

Jib is a Maven plugin developed by Google that builds optimized Docker images directly from a Maven project.

Advantages:

* No Dockerfile required
* No manual `docker build`
* Creates optimized image layers
* Faster incremental builds
* Integrates directly with Maven
* Can build and push images directly to Docker Hub

---

# Adding the Jib Plugin

The following plugin is added to the `pom.xml` of **every microservice**.

```xml
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>
    <version>3.4.6</version>

    <configuration>
        <to>
            <image>nagarjun20/accounts:docker</image>
        </to>
    </configuration>
</plugin>
```

Each service has its own image name.

Examples:

Accounts

```xml
<image>nagarjun20/accounts:docker</image>
```

Loans

```xml
<image>nagarjun20/loans:docker</image>
```

Cards

```xml
<image>nagarjun20/cards:docker</image>
```

Config Server

```xml
<image>nagarjun20/configserver:docker</image>
```

Each microservice requires its own Docker image because each service runs inside its own container.

---

# Building the Docker Image

The following command is executed:

```bash
mvn compile jib:dockerBuild
```

This command performs two operations.

## Step 1 - Compile the Application

Maven compiles the Java source code.

```
Java Source

↓

Compiled Classes

↓

Spring Boot Application
```

Equivalent to running:

```bash
mvn compile
```

---

## Step 2 - Jib Builds the Docker Image

After compilation, Jib automatically:

* Downloads a suitable Java runtime base image (if required)
* Packages the Spring Boot application
* Creates optimized Docker image layers
* Stores the image in the local Docker Engine

No Dockerfile is required.

Result:

```
Spring Boot Application

↓

Docker Image
```

---

# Docker Image Layers

A Docker image consists of multiple layers.

Typical Jib image:

```
Base Java Runtime

↓

Dependencies

↓

Resources

↓

Application Classes

↓

Container Configuration
```

Since dependencies change less frequently than application code, Jib only rebuilds the layers that have changed, making builds much faster.

---

# Local Docker Repository

After executing

```bash
mvn compile jib:dockerBuild
```

the image is available locally.

Verify using:

```bash
docker images
```

Example output:

```
REPOSITORY                  TAG

nagarjun20/accounts         docker

nagarjun20/loans            docker

nagarjun20/cards            docker

nagarjun20/configserver     docker
```

At this point, the image exists only on the local machine.

---

# Pushing Images to Docker Hub

To make the images available to other machines, they are pushed to Docker Hub.

Example:

```bash
docker image push docker.io/nagarjun20/accounts:docker
```

Similarly,

```bash
docker image push docker.io/nagarjun20/loans:docker

docker image push docker.io/nagarjun20/cards:docker

docker image push docker.io/nagarjun20/configserver:docker
```

This uploads the local Docker image to the Docker Hub registry.

---

# Why Push Images?

Suppose another developer clones the project.

Without Docker Hub, they would have to:

* Clone the project
* Install Java
* Install Maven
* Build every project
* Build Docker images manually

With Docker Hub, they only need:

```bash
docker compose up
```

Docker automatically downloads the required images.

---

# Docker Hub

Docker Hub is a cloud-based Docker image registry.

It is similar to GitHub.

GitHub stores:

```
Source Code
```

Docker Hub stores:

```
Docker Images
```

Example:

```
GitHub

↓

Java Source Code

↓

Developer
```

```
Docker Hub

↓

Docker Images

↓

Docker Engine
```

---

# How Docker Compose Uses the Images

In `docker-compose.yml`, each service references an image.

Example:

```yaml
accounts:

  image: nagarjun20/accounts:docker
```

Notice that Compose does **not** build the image.

Instead, Docker Compose performs the following steps.

```
Image exists locally?

        │

   Yes ─────────► Start Container

        │

        No

        ▼

Download from Docker Hub

        ▼

Start Container
```

Thus, `docker-compose.yml` uses the already-built Docker images.

---

# Complete Deployment Flow

```
Developer

↓

Write Java Code

↓

Maven Compile

↓

Jib Plugin

↓

Docker Image

↓

Docker Hub

↓

docker compose up

↓

Docker Downloads Images

↓

Containers Start
```

---

# End-to-End Workflow

```
                Write Java Code
                        │
                        ▼
                 mvn compile
                        │
                        ▼
             Jib Maven Plugin
                        │
                        ▼
            Docker Image (Local)
                        │
         docker image push
                        │
                        ▼
                 Docker Hub
                        │
             docker compose up
                        │
                        ▼
          Docker Pulls Images
                        │
                        ▼
         Creates Docker Containers
                        │
                        ▼
 RabbitMQ → Config Server → Accounts → Loans → Cards
```

---

# Why Build a Separate Image for Each Microservice?

Each Spring Boot application is an independent microservice.

Each microservice has:

* Its own source code
* Its own dependencies
* Its own executable JAR
* Its own Docker image
* Its own Docker container

Therefore, every service requires a separate Docker image.

---

# Useful Commands

Build Docker image

```bash
mvn compile jib:dockerBuild
```

View local images

```bash
docker images
```

Push image to Docker Hub

```bash
docker image push docker.io/nagarjun20/accounts:docker
```

Download image

```bash
docker pull nagarjun20/accounts:docker
```

Start the application

```bash
docker compose up
```

Run in background

```bash
docker compose up -d
```

Stop all containers

```bash
docker compose down
```

---

# Key Takeaways

* Docker cannot execute Java source code directly; it requires a Docker image.
* Jib eliminates the need to create and maintain Dockerfiles for Spring Boot applications.
* `mvn compile jib:dockerBuild` compiles the application and creates a Docker image in the local Docker Engine.
* `docker image push` uploads the image to Docker Hub, making it available to other developers and deployment environments.
* `docker-compose.yml` references Docker images rather than building them.
* When `docker compose up` is executed, Docker pulls any missing images from Docker Hub and starts the containers.
* Each microservice has its own Docker image because every microservice is an independent deployable unit.
