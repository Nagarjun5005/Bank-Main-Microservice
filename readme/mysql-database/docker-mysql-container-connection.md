# Docker Complete Guide --- Bank App Microservices

## 1. Overview

This project is a Spring Boot microservices application running with
Docker Compose.

The environment currently contains:

-   Config Server
-   Accounts Service
-   Cards Service
-   Loans Service
-   RabbitMQ
-   MySQL databases for Accounts, Cards and Loans
-   Separate Docker Compose configurations for `default`, `qa`, and
    `prod`

The important Docker flow is:

``` text
Java/Spring Boot source code
        |
        v
     pom.xml
        |
        | Maven / Jib builds Docker image
        v
Docker image
        |
        | Push to Docker Hub
        v
nagarjun20/accounts:<tag>
nagarjun20/cards:<tag>
nagarjun20/loans:<tag>
nagarjun20/cloud-config:<tag>
        |
        | docker compose up
        v
Docker containers
        |
        v
Running microservices
```

------------------------------------------------------------------------

# 2. Dockerfile vs Docker Image vs Docker Container

These three terms are easy to confuse.

## Dockerfile

A `Dockerfile` is a set of instructions used to build an image.

Example:

``` dockerfile
FROM eclipse-temurin:21-jre

COPY target/accounts.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
```

It says:

1.  Start from a Java runtime image.
2.  Copy the application JAR.
3.  Start the Spring Boot application.

## Docker Image

An image is the packaged result of the Dockerfile/build process.

Example:

``` text
nagarjun20/accounts:docker
```

The format is:

``` text
repository/image:tag
```

For example:

``` text
nagarjun20/accounts:docker
             |       |
             |       +-- tag
             +---------- image name
```

## Docker Container

A container is a running instance of an image.

For example:

``` text
Image:
nagarjun20/accounts:docker

        |
        v

Container:
accounts-ms
```

The image is the package; the container is the running application
created from that package.

------------------------------------------------------------------------

# 3. What Is a Docker Image Tag?

A tag identifies a particular version/variant of an image.

Your project has shown examples such as:

``` text
nagarjun20/accounts:docker
nagarjun20/accounts:docker-mysql
```

These are two different image references.

For example:

``` text
nagarjun20/accounts:docker
```

and

``` text
nagarjun20/accounts:docker-mysql
```

may contain different configurations, dependencies, build settings, or
application versions depending on how they were built.

Docker does not assume that `docker-mysql` is automatically related to
MySQL.

It is simply a tag chosen by the image-building configuration.

------------------------------------------------------------------------

# 4. Why Docker Desktop Shows `:docker`

The most important concept:

**Docker Compose decides which image to run.**

For example, if `prod/docker-compose.yml` contains:

``` yaml
services:
  accounts:
    image: nagarjun20/accounts:docker
```

Docker Compose will run:

``` text
nagarjun20/accounts:docker
```

It does not inspect your `pom.xml` at runtime.

Similarly:

``` yaml
services:
  accounts:
    image: nagarjun20/accounts:docker-mysql
```

will run:

``` text
nagarjun20/accounts:docker-mysql
```

Therefore:

``` text
pom.xml
   |
   | builds image
   v
Docker image/tag
   |
   | compose selects image
   v
Docker container
```

------------------------------------------------------------------------

# 5. Role of `pom.xml`

The `pom.xml` belongs to Maven.

It controls how the Java application is built and can also control how
the Docker image is created when using tools such as Jib.

A Jib configuration can look conceptually like:

``` xml
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>

    <configuration>
        <to>
            <image>nagarjun20/accounts:docker</image>
        </to>
    </configuration>
</plugin>
```

This tells Maven/Jib to build an image called:

``` text
nagarjun20/accounts:docker
```

The exact configuration in your project may be different.

The important distinction is:

``` text
pom.xml
= build configuration

docker-compose.yml
= container/runtime configuration
```

------------------------------------------------------------------------

# 6. Jib and Dockerfile

A Spring Boot application does not necessarily need a traditional
Dockerfile.

Jib can build a container image directly from Maven.

Typical command:

``` powershell
mvn compile jib:dockerBuild
```

or:

``` powershell
mvn compile jib:build
```

The exact command depends on your project's Jib configuration.

Conceptually:

``` text
pom.xml
   |
   v
Jib
   |
   v
Docker image
```

With a traditional Dockerfile:

``` text
pom.xml
   |
   v
Maven builds JAR
   |
   v
Dockerfile
   |
   v
Docker image
```

Both approaches are valid.

------------------------------------------------------------------------

# 7. `docker` vs `docker-mysql` Tags

Your environments have shown image names like:

``` text
nagarjun20/accounts:docker
```

and previously:

``` text
nagarjun20/accounts:docker-mysql
```

The tag itself does not tell Docker what database is being used.

For example:

``` text
:docker-mysql
```

does NOT mean:

> Docker automatically connects this image to MySQL.

The actual database configuration is normally controlled by Spring
configuration such as:

``` properties
spring.datasource.url=jdbc:mysql://accountsdb:3306/accountsdb
```

or YAML:

``` yaml
spring:
  datasource:
    url: jdbc:mysql://accountsdb:3306/accountsdb
```

The Docker image tag is just an identifier.

------------------------------------------------------------------------

# 8. Docker Compose

Docker Compose allows multiple containers to be defined in one YAML
file.

A simplified example:

``` yaml
services:

  configserver:
    image: nagarjun20/cloud-config:docker
    ports:
      - "8071:8071"

  accounts:
    image: nagarjun20/accounts:docker
    ports:
      - "8080:8080"

  cards:
    image: nagarjun20/cards:docker
    ports:
      - "9000:9000"

  loans:
    image: nagarjun20/loans:docker
    ports:
      - "8090:8090"

  rabbit:
    image: rabbitmq:4-management
```

When you execute:

``` powershell
docker compose up -d
```

Compose:

1.  Reads the YAML.
2.  Pulls missing images.
3.  Creates the network.
4.  Creates containers.
5.  Starts containers.
6.  Runs them in the background because of `-d`.

------------------------------------------------------------------------

# 9. `docker compose down`

Command:

``` powershell
docker compose down
```

This normally stops and removes containers and the Compose network
created by that Compose project.

It does not automatically delete Docker images.

It also does not remove unrelated containers.

This is important when you have:

``` text
default
qa
prod
```

because each Compose project can have its own resources.

------------------------------------------------------------------------

# 10. `docker compose up -d`

Command:

``` powershell
docker compose up -d
```

Means:

``` text
up       = create/start services
-d       = detached/background mode
```

Example:

``` powershell
cd C:\2026\Bank-App-Microservices\docker-compose\prod

docker compose up -d
```

Docker will create/start the Prod services defined in the Prod Compose
file.

------------------------------------------------------------------------

# 11. Why `docker compose up` Pulled Images

You previously saw:

``` text
loans Pulled
configserver Pulled
cards Pulled
accounts Pulled
```

This means Docker Compose needed the specified images locally and pulled
them from the configured registry.

For example:

``` text
nagarjun20/loans:docker
```

may be pulled from Docker Hub.

The process is:

``` text
docker-compose.yml
       |
       v
image: nagarjun20/loans:docker
       |
       v
Is image available locally?
       |
    +--+--+
    |     |
   YES    NO
    |     |
    |     v
    |   Pull image
    |     |
    +--+--+
       |
       v
Create container
       |
       v
Start container
```

------------------------------------------------------------------------

# 12. Docker Compose Ports

A Compose entry such as:

``` yaml
ports:
  - "8080:8080"
```

means:

``` text
HOST PORT : CONTAINER PORT
```

Therefore:

``` text
localhost:8080
        |
        v
container:8080
```

Your current Prod environment showed:

``` text
accounts-ms     8080:8080
cards-ms        9000:9000
loans-ms        8090:8090
configserver-ms 8071:8071
```

So you can access them through the host using those ports, assuming the
applications are healthy.

------------------------------------------------------------------------

# 13. Docker Networks

Compose normally creates a network for the application.

For example:

``` text
prod_nagarjun20
```

Containers connected to the same Docker network can communicate using
service/container DNS names.

For example:

``` text
accounts-ms
cards-ms
loans-ms
rabbit-1
configserver-ms
```

A container generally should not use:

``` text
localhost
```

to communicate with another container.

Inside a container:

``` text
localhost
```

means:

> this same container

Instead, use the Docker service/container hostname.

For example:

``` text
jdbc:mysql://accountsdb:3306/accountsdb
```

rather than:

``` text
jdbc:mysql://localhost:3307/accountsdb
```

The host port `3307` is primarily for access from your Windows machine.

------------------------------------------------------------------------

# 14. MySQL Containers

Your setup has separate MySQL databases.

You previously had:

``` text
accountsdb
cardsdb
loansdb
```

Example:

``` yaml
accountsdb:
  image: mysql
  ports:
    - "3307:3306"
```

This means:

``` text
Windows:
localhost:3307

        |
        v

MySQL container:
3306
```

Similarly:

``` text
cardsdb:
localhost:3309 -> container 3306

loansdb:
localhost:3308 -> container 3306
```

The fact that the host ports are different is necessary because all
three MySQL containers internally use port `3306`.

------------------------------------------------------------------------

# 15. RabbitMQ

Your setup also uses:

``` text
rabbitmq:4-management
```

The management interface is commonly exposed through:

``` text
localhost:15672
```

The AMQP service commonly uses:

``` text
localhost:5672
```

From another container, applications should normally connect using the
RabbitMQ container/service name rather than `localhost`.

For example:

``` text
rabbit-1:5672
```

depending on your Compose service name and configuration.

------------------------------------------------------------------------

# 16. Health Checks

You previously saw:

``` text
configserver-ms   Up 2 hours (healthy)
accountsdb        Up 2 hours (healthy)
loansdb           Up 2 hours (healthy)
```

`healthy` means the container's configured Docker health check is
passing.

Typical Compose configuration:

``` yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8071/actuator/health"]
  interval: 10s
  timeout: 5s
  retries: 5
```

Health status is different from merely:

``` text
Up
```

A container can be running but the application inside it may not
actually be ready.

------------------------------------------------------------------------

# 17. `docker ps`

Use:

``` powershell
docker ps
```

to see currently running containers.

Example:

``` text
CONTAINER ID
IMAGE
STATUS
PORTS
NAMES
```

Use:

``` powershell
docker ps -a
```

to see both running and stopped containers.

This is particularly useful for finding:

``` text
Exited (1)
```

because it indicates that a container stopped with an error.

------------------------------------------------------------------------

# 18. Checking Container Logs

For example:

``` powershell
docker logs accounts-ms
```

Follow logs live:

``` powershell
docker logs -f accounts-ms
```

The `-f` means follow.

For Prod:

``` powershell
docker logs configserver-ms
docker logs accounts-ms
docker logs cards-ms
docker logs loans-ms
docker logs rabbit-1
```

If a service exits:

``` text
Exited (1)
```

check its logs first.

------------------------------------------------------------------------

# 19. `docker compose ps`

When you are inside the Prod directory:

``` powershell
docker compose ps
```

shows services belonging to that Compose project.

This is usually better than only using:

``` powershell
docker ps
```

when troubleshooting Compose.

------------------------------------------------------------------------

# 20. `docker compose config`

One of the best troubleshooting commands is:

``` powershell
docker compose config
```

It shows the final Compose configuration after Compose processes the
YAML.

To see image definitions:

``` powershell
docker compose config | Select-String "image:"
```

This is particularly useful for your current question about:

``` text
:docker
```

vs

``` text
:docker-mysql
```

If Prod says:

``` yaml
image: nagarjun20/accounts:docker
```

then Docker will run:

``` text
nagarjun20/accounts:docker
```

regardless of what you remember seeing in the `pom.xml`.

------------------------------------------------------------------------

# 21. Container Name Conflict

You encountered:

``` text
Conflict. The container name "/configserver-ms" is already in use
```

This happens when Compose tries to create:

``` text
configserver-ms
```

but another container already has that exact name.

For example:

``` text
default
   |
   +-- configserver-ms

prod
   |
   +-- configserver-ms   <-- conflict
```

Docker container names are globally unique on the Docker host.

------------------------------------------------------------------------

# 22. Why `default`, `qa`, and `prod` Can Conflict

Suppose all three Compose files contain:

``` yaml
container_name: configserver-ms
```

Then:

``` text
default -> configserver-ms
qa      -> configserver-ms
prod    -> configserver-ms
```

They cannot all exist simultaneously.

A better approach is generally to let Compose generate names.

Instead of:

``` yaml
container_name: configserver-ms
```

use the service name:

``` yaml
services:
  configserver:
    image: ...
```

Compose can then generate names such as:

``` text
default-configserver-1
qa-configserver-1
prod-configserver-1
```

This makes running multiple environments easier.

------------------------------------------------------------------------

# 23. Compose Project Names

Another way to separate environments is to use different Compose project
names.

For example:

``` powershell
docker compose -p default up -d
docker compose -p qa up -d
docker compose -p prod up -d
```

Then Compose resources are grouped under:

``` text
default
qa
prod
```

This is especially useful when the same Compose structure is reused for
multiple environments.

------------------------------------------------------------------------

# 24. Docker Image Build vs Docker Compose

These are two different operations.

## Build

Creates an image:

``` powershell
mvn compile jib:dockerBuild
```

or another project-specific build command.

Result:

``` text
nagarjun20/accounts:docker
```

## Run

Starts containers from images:

``` powershell
docker compose up -d
```

Result:

``` text
accounts-ms
```

So:

``` text
BUILD
  |
  v
IMAGE
  |
  v
COMPOSE
  |
  v
CONTAINER
```

------------------------------------------------------------------------

# 25. Push vs Pull

When you build an image locally:

``` text
Local machine
    |
    v
nagarjun20/accounts:docker
```

When you push it:

``` text
Local Docker
    |
    v
Docker Hub
```

Another machine can then run:

``` powershell
docker compose up -d
```

and Docker can pull:

``` text
nagarjun20/accounts:docker
```

from Docker Hub.

------------------------------------------------------------------------

# 26. Useful Docker Commands

## List images

``` powershell
docker images
```

## List running containers

``` powershell
docker ps
```

## List all containers

``` powershell
docker ps -a
```

## Start a container

``` powershell
docker start <container-name>
```

## Stop a container

``` powershell
docker stop <container-name>
```

## Remove a container

``` powershell
docker rm <container-name>
```

## Force remove a container

``` powershell
docker rm -f <container-name>
```

## View logs

``` powershell
docker logs <container-name>
```

## Follow logs

``` powershell
docker logs -f <container-name>
```

## Inspect container

``` powershell
docker inspect <container-name>
```

## List networks

``` powershell
docker network ls
```

## Inspect network

``` powershell
docker network inspect <network-name>
```

## List images

``` powershell
docker image ls
```

------------------------------------------------------------------------

# 27. Useful Docker Compose Commands

Start:

``` powershell
docker compose up -d
```

Stop and remove:

``` powershell
docker compose down
```

Show services:

``` powershell
docker compose ps
```

Show final configuration:

``` powershell
docker compose config
```

Build:

``` powershell
docker compose build
```

Build and start:

``` powershell
docker compose up -d --build
```

View logs:

``` powershell
docker compose logs
```

View one service:

``` powershell
docker compose logs accounts
```

Follow logs:

``` powershell
docker compose logs -f accounts
```

------------------------------------------------------------------------

# 28. Recommended Troubleshooting Flow

When Prod does not start, use this sequence.

## Step 1 --- Go to Prod

``` powershell
cd C:\2026\Bank-App-Microservices\docker-compose\prod
```

## Step 2 --- Validate Compose

``` powershell
docker compose config
```

## Step 3 --- Check images

``` powershell
docker compose config | Select-String "image:"
```

## Step 4 --- Start

``` powershell
docker compose up -d
```

## Step 5 --- Check status

``` powershell
docker compose ps
```

## Step 6 --- Check all containers

``` powershell
docker ps -a
```

## Step 7 --- Check failed containers

If you see:

``` text
Exited (1)
```

run:

``` powershell
docker logs <container-name>
```

## Step 8 --- Check application endpoints

For your current Prod port mapping:

``` text
Config Server -> http://localhost:8071
Accounts      -> http://localhost:8080
Cards         -> http://localhost:9000
Loans         -> http://localhost:8090
```

Whether the root URL responds successfully depends on the endpoints
implemented by each Spring Boot service. Actuator health endpoints are
preferable if enabled.

------------------------------------------------------------------------

# 29. Understanding Your Current Prod Setup

Based on the Docker Desktop state you showed, Prod was successfully
started after the previous conflict was resolved.

The current Prod containers shown were:

``` text
rabbit-1
configserver-ms
accounts-ms
cards-ms
loans-ms
```

with ports:

``` text
RabbitMQ management -> 15672
Config Server       -> 8071
Accounts            -> 8080
Cards               -> 9000
Loans               -> 8090
```

The important observation is that the current Prod images were displayed
as:

``` text
nagarjun20/cloud-config:docker
nagarjun20/accounts:docker
nagarjun20/cards:docker
nagarjun20/loans:docker
```

This is determined by the Prod Compose configuration.

It is separate from the image tag used by your Default environment,
where you previously saw:

``` text
nagarjun20/cloud-config:docker-mysql
nagarjun20/accounts:docker-mysql
nagarjun20/cards:docker-mysql
nagarjun20/loans:docker-mysql
```

To determine exactly why those tags differ, inspect both:

``` powershell
docker compose config | Select-String "image:"
```

from each environment and then compare the relevant Maven/Jib
configuration in `pom.xml`.

------------------------------------------------------------------------

# 30. Mental Model to Remember

The easiest way to understand the whole setup is:

``` text
                         pom.xml
                            |
                            v
                    Maven / Jib Build
                            |
                            v
                    Docker Image
              +-------------+-------------+
              |                           |
              v                           v
accounts:docker                 accounts:docker-mysql
              |                           |
              +-------------+-------------+
                            |
                            v
                  docker-compose.yml
                            |
                            v
                     Docker Container
                            |
                            v
                       Spring Boot
                            |
              +-------------+-------------+
              |             |             |
              v             v             v
           MySQL         RabbitMQ     Config Server
```

And remember the key rule:

> **`pom.xml` helps build the image. `docker-compose.yml` chooses which
> image to run.**

That single distinction explains why Docker Desktop can show `:docker`
even when you were looking at MySQL-related configuration in your Maven
project.

------------------------------------------------------------------------

# 31. Recommended Project Structure

A clean structure can look like:

``` text
Bank-App-Microservices/
|
+-- accounts/
|   +-- pom.xml
|   +-- src/
|
+-- cards/
|   +-- pom.xml
|   +-- src/
|
+-- loans/
|   +-- pom.xml
|   +-- src/
|
+-- configserver/
|   +-- pom.xml
|   +-- src/
|
+-- docker-compose/
    |
    +-- default/
    |   +-- docker-compose.yml
    |
    +-- qa/
    |   +-- docker-compose.yml
    |
    +-- prod/
        +-- docker-compose.yml
```

The application modules build images.

The environment-specific Compose files decide how those images are run.

------------------------------------------------------------------------

# 32. Final Checklist

When you want to verify that Prod is working:

``` powershell
cd C:\2026\Bank-App-Microservices\docker-compose\prod

docker compose config

docker compose config | Select-String "image:"

docker compose up -d

docker compose ps

docker ps -a
```

Then investigate any:

``` text
Exited
Created
Unhealthy
Restarting
```

containers using:

``` powershell
docker logs <container-name>
```

The final goal is:

``` text
Prod
 |
 +-- Config Server     -> Running/Healthy
 +-- Accounts          -> Running
 +-- Cards             -> Running
 +-- Loans             -> Running
 +-- RabbitMQ          -> Running/Healthy
 +-- Accounts DB       -> Running/Healthy
 +-- Cards DB          -> Running/Healthy
 +-- Loans DB          -> Running/Healthy
```

Once all of these are healthy/running, test the application endpoints
and service-to-service communication.
