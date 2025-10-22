## SPRING CORE COMPLETE TUTORIAL

**By: Sadiul’s Study Guide**

---

## 1. Introduction to Spring Framework

### What is Spring?

Spring is a **Java framework** that helps you build powerful, testable, and maintainable applications easily.
It manages your application’s **objects (beans)** — how they are created, connected, and destroyed.

### Why use Spring?

* Reduces boilerplate code.
* Makes applications loosely coupled.
* Supports modular design.
* Provides integration with other frameworks (Hibernate, JPA, etc.).
* Easy to test.

---

### Inversion of Control (IoC)

Instead of **you creating objects manually** using `new`, **Spring Container creates and manages them** for you.

**Without Spring:**

```java
UserService service = new UserService(new UserRepository());
```

**With Spring:**

```java
@Autowired
UserService service;
```

➡ Spring automatically creates and injects the `UserService` and its dependencies.

---

### Dependency Injection (DI)

It’s the process of **injecting dependent objects** into a class rather than creating them inside the class.

---

## 2. Spring IoC Container

The **container** is the brain of Spring. It creates and manages beans.

There are two main containers:

* `BeanFactory` → Basic, lazy-loading.
* `ApplicationContext` → Advanced, supports events, AOP, etc.

---

### Example 1: XML Configuration

**beans.xml**

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
       http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="greetingService" class="com.example.GreetingService"/>
</beans>
```

**Main.java**

```java
ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
GreetingService service = context.getBean("greetingService", GreetingService.class);
service.sayHello();
```

---

### Example 2: Java Configuration

**AppConfig.java**

```java
@Configuration
public class AppConfig {
    @Bean
    public GreetingService greetingService() {
        return new GreetingService();
    }
}
```

**Main.java**

```java
AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
GreetingService service = context.getBean(GreetingService.class);
service.sayHello();
```

---

## 3. Spring Beans

A **bean** is just an **object managed by the Spring container**.

### Example

```java
@Component
public class GreetingService {
    public void sayHello() {
        System.out.println("Hello, Spring!");
    }
}
```

### Bean Scopes

| Scope       | Description                   |
| ----------- | ----------------------------- |
| `singleton` | One shared instance (default) |
| `prototype` | New instance each time        |
| `request`   | One per HTTP request          |
| `session`   | One per HTTP session          |

```java
@Scope("prototype")
@Component
public class ReportGenerator { }
```

---

### Bean Lifecycle

1. Bean instantiated
2. Dependencies injected
3. `@PostConstruct` → called after init
4. `@PreDestroy` → called before destroy

```java
@Component
public class DemoBean {
    @PostConstruct
    void init() { System.out.println("Bean initialized!"); }

    @PreDestroy
    void destroy() { System.out.println("Bean destroyed!"); }
}
```

---

## 4. Dependency Injection (DI)

### 1️Constructor Injection

```java
@Component
public class Car {
    private final Engine engine;

    @Autowired
    public Car(Engine engine) {
        this.engine = engine;
    }
}
```

### Setter Injection

```java
@Component
public class Car {
    private Engine engine;

    @Autowired
    public void setEngine(Engine engine) {
        this.engine = engine;
    }
}
```

### Field Injection

```java
@Autowired
private Engine engine;
```

*Not recommended for testing — constructor is preferred.*

---

### `@Qualifier` and `@Primary`

When multiple beans of same type exist:

```java
@Component("v8")
public class V8Engine implements Engine {}

@Component("v6")
@Primary
public class V6Engine implements Engine {}
```

Now `Car` gets `V6Engine` by default.

---

## 🧵 5. Spring Configuration

### External Configuration (properties file)

**application.properties**

```properties
app.name=MySpringApp
```

**Java Config**

```java
@Value("${app.name}")
private String appName;
```

---

### Profiles

You can create environment-specific beans.

```java
@Profile("dev")
@Bean
public DataSource devDataSource() { ... }

@Profile("prod")
@Bean
public DataSource prodDataSource() { ... }
```

Activate with:

```bash
-Dspring.profiles.active=dev
```

---

## 6. Spring Expression Language (SpEL)

You can inject values dynamically using expressions.

```java
@Component
public class MathBean {

    @Value("#{5 + 10}")
    private int sum;  // 15

    @Value("#{T(java.lang.Math).random() * 100}")
    private double randomValue;
}
```

You can also reference other beans:

```java
@Value("#{greetingService.sayHello()}")
private String message;
```

---

## 7. Spring AOP (Aspect-Oriented Programming)

Used to separate **cross-cutting concerns** (like logging, security, transactions).

### Example

```java
@Aspect
@Component
public class LoggingAspect {

    @Before("execution(* com.example.service.*.*(..))")
    public void logBefore(JoinPoint jp) {
        System.out.println("Method called: " + jp.getSignature());
    }
}
```

**Main Config**

```java
@EnableAspectJAutoProxy
@Configuration
@ComponentScan("com.example")
public class AppConfig { }
```

Now, whenever a method in `com.example.service` runs, the aspect prints a log automatically.

---

## 8. Event Handling

Spring supports **application events** (publish-subscribe model).

```java
@Component
public class MyEventListener {

    @EventListener
    public void handleEvent(ContextRefreshedEvent event) {
        System.out.println("Context initialized!");
    }
}
```

You can also publish custom events:

```java
public class UserCreatedEvent extends ApplicationEvent {
    public UserCreatedEvent(Object source) { super(source); }
}

@Component
public class EventPublisher {
    @Autowired private ApplicationEventPublisher publisher;

    public void publishEvent() {
        publisher.publishEvent(new UserCreatedEvent(this));
    }
}
```

---

## 🧾 9. Common Spring Annotations

| Annotation       | Description                    |
| ---------------- | ------------------------------ |
| `@Configuration` | Declares a configuration class |
| `@Bean`          | Defines a bean method          |
| `@ComponentScan` | Scans for components           |
| `@Component`     | Marks a general bean           |
| `@Service`       | Marks a service-layer bean     |
| `@Repository`    | Marks a DAO-layer bean         |
| `@Controller`    | Marks a controller-layer bean  |
| `@Autowired`     | Injects dependency             |
| `@Qualifier`     | Chooses specific bean          |
| `@Primary`       | Marks preferred bean           |
| `@Scope`         | Defines bean scope             |
| `@Value`         | Injects property value         |
| `@Profile`       | Environment-specific beans     |

---

## 10. Spring Boot Core Add-ons

Spring Boot builds on top of Spring Core, simplifying configuration.

### Example

```java
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

* Automatically scans components
* Handles configuration automatically
* External config via `application.yml` or `.properties`
* Includes embedded Tomcat

---

## Summary

| Topic            | Key Idea                             |
| ---------------- | ------------------------------------ |
| 1. Intro         | Spring manages your objects          |
| 2. IoC           | Container creates & injects beans    |
| 3. Beans         | Managed objects with lifecycle       |
| 4. DI            | Inject dependencies, not `new` them  |
| 5. Config        | Java, XML, or properties-based setup |
| 6. SpEL          | Dynamic expressions in annotations   |
| 7. AOP           | Separate cross-cutting concerns      |
| 8. Events        | Publish and listen for events        |
| 9. Annotations   | Declarative setup for everything     |
| 10. Boot Add-ons | Auto configuration & simplicity      |
