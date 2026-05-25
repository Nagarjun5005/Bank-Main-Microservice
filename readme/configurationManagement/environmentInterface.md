# Spring Boot Environment Interface

## What is Environment?
Environment is a Spring interface used to read configuration values programmatically at runtime

It can read values from:

- application.properties
- application.yml
- Environment Variables
- JVM Properties
- Command Line Arguments

### IMPORT
```JAVA
import org.springframework.core.env.Environment;
```


### EXAMPLE

```properties
app.name=order service
```

```java
@Service
public class OrderService {

    private final Environment environment;

    public OrderService(Environment environment) {
        this.environment = environment;
    }

    public void printName() {

        String appName =
                environment.getProperty("app.name");

        System.out.println(appName);
    }
}

```

### MAIN METHODS

| Method | Purpose |
|---|---|
| `getProperty()` | Read property |
| `containsProperty()` | Check whether property exists |
| `getActiveProfiles()` | Get active Spring profiles |


### READING PROPERTY
```properties
environment.getProperty("app.name");
```

### DEFAULT VALUE
```JAVA
environment.getProperty(
    "app.version",
    "1.0"
);
```


### READING INTEGER/BOOLEAN
```java
Integer timeout =
    environment.getProperty(
        "app.timeout",
        Integer.class
    );

Boolean enabled =
    environment.getProperty(
        "app.enabled",
        Boolean.class
    );
```


### ACTIVE PROFILES
```properties
spring.profiles.active=dev
```

```java
environment.getActiveProfiles();
```

### READING ENVIRONMENT VARIABLES
```properties
DB_PASSWORD=secret

```
```java
environment.getProperty("DB_PASSWORD");
```
### Environment vs @Value

| @Value | @Value                        |
|---|-------------------------------|
| Declarative | Programmatic                  |
| Cleaner| Flexible |
| Static |Dynamic    |


### When to Use Environment?

Use when:

* runtime decision needed
* dynamic property lookup
* profile checking
* optional configs

### Limitations
* More verbose
* No type safety
* Manual property handling

