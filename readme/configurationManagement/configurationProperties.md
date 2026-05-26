

# Spring Boot @ConfigurationProperties

## What is @ConfigurationProperties?
@ConfigurationProperties is a Spring Boot annotation used to bind 
grouped configuration properties into a Java class.

It is the MOST recommended approach for enterprise microservices.

### Why Do We Need It?
Using multiple @Value annotations becomes messy.

```java

@Value("${payment.url}")
private String url;

@Value("${payment.timeout}")
private int timeout;

@Value("${payment.enabled}")
private boolean enabled;
```

Instead , we group configs together

###  MAIN ADVANTAGES

| Feature | Benefit |
|---|---|
| Type Safe | Compile-time support |
| Grouped Configs | Cleaner code |
| Easy Maintenance | Centralized configs |
| Validation Support | Supports `@NotNull`, `@Min` |
| Best for Microservices | Production-friendly |

### BASIC EXAMPLE

```yaml
payment:
  gateway:
    url: https://api.stripe.com
    timeout: 5000
    enabled: true
```
```java

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payment.gateway")
public class PaymentProperties {

    private String url;
    private int timeout;
    private boolean enabled;

    // getters and setters
}

@Service
public class PaymentService {

    private final PaymentProperties properties;

    public PaymentService(PaymentProperties properties) {
        this.properties = properties;
    }

    public void printConfig() {

        System.out.println(properties.getUrl());
        System.out.println(properties.getTimeout());
    }
}

```

### IMPORTANT ANNOTATION

```properties
@ConfigurationProperties
```
Used for binding configs.
```java

@ConfigurationProperties(prefix = "payment.gateway");
```


### ALTERNATE MODERN APPROACH -->IMMUTABLE CONFIGURATION
```JAVA
@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(
    String url,
    int timeout
) {
}
```
### `@ConfigurationProperties` vs `@Value`

| `@Value` | `@ConfigurationProperties` |
|---|---|
| Simple values | Grouped configs |
| No type safety | Type safe |
| Scattered | Centralized |
| Weak validation | Strong validation |
| Small configs | Enterprise configs |

### When to Use?

Use for:

* Database configs
* Kafka configs
* API configs
* Retry configs
* Feature configs
* Large microservice
