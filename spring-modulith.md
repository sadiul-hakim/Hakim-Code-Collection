# 1. Modulith

Dependencies

1. spring-modulith-events-jpa (for saving events)
2. spring-modulith-actuator (for /actuator/modulith but requires spring-boot-starter-actuator)
3. spring-modulith-starter-jpa (if jpa is used in project. If you add this there is no need of
   spring-modulith-starter-core)
4. spring-modulith-observability

Under the root package `xyz.sadiulhakim` all the packages are called module in Spring Modulith.
And files directly under modules `not under any nested package` are called public api like: AppConfig
under config module is called public Api. Any `public` class under any package under any module can not be accessed from
other module by default
`package private, private, protected file are not accessable outside as they are still java types`.

## Ways to make them available outside

### Testing - This testing makes sure you do not violate modularity rules.

Crate a test file then declare a ApplicationModules instance like this
`static ApplicationModules modules = ApplicationModules.of(Application.class);` then
inside a test method call `modules.verify()`.

1. Named Interface
   `Create package-info.java file under sub packages and use @NamedInterface annotation to give a name or just use @NamedAnnotation on the type(class,record) name. NamedInterface make type(class,record) avilable outside`.
2. Open Application Modules
   `Create package-info.java file under module and use @ApplicationModule annotation to select type Open, but that makes all the files avilable outside of the module`
3. allowedDependencies
   `this paramater is used inside @ApplicationModule to specify which modules are allowed to be this modules dependency. After the = sign use {} to pass command seprated modules name and use {moduleName} :: {NameFilterface name} this syntax to pass alowed NamedInterfaces name.`

### Modulith Events

In Spring Modulith, instead of making circular dependency we can make use of spring events to do dependent tasks. Like
you have to delete a UserRole by id,
but before deleting first you have to make sure any user does not have this role. If any user has this role you should
not delete this. In this scenario, we need to inject UserService
into RoleService but that is what would make Spring Moldulith Angry. How to solve this? When delete method is called
publish an event with role id and do not inject UserService in
RoleService. Then in a different EventListener class inject Role And User Service. For listening to an event you can use
`EventListsner or TransactionalEventListener` Annotation.
You can use `@Async` annotation to make listening Asynchronous.

***But remember for `@TransactionalEventListener` the
publishing method or class should be annotated with `@Transactional`.
Same to `@ApplicationModuleListener` as it is annotated with
`@TransactionalEventListener , @Async and @Transactional(propagation = Propagation.REQUIRES_NE)`***.

#### Example of a dependency cycle

Suppose we have modules like this

```
user - module
   web (package)
      UserController (class)
   model (package)
      UserService (class)

role - module
   RoleEventListener (class)
   model (package)
      RoleService (class)
   web (package)
      RoleController
```

UserController has dependency of RoleService `which is public api (directly under module)` which belongs to role module
and RoleEventListener `which is a public api` has dependency of UserService which belongs to user module.
Though dependent classes are public api still there is a cycle. It is all about module, does not matter if the class is
a public api or not.

# 2. Spring Events (Observer Pattern, One Subject multiple Observer)

Spring is Event Driven. Spring itself publishes some events when the application starts like WebServerInitializedEvent
etc.

***We can create custom event and publish from anywhere of our spring application. Then we can listen to that
event from listener. And take actions.***

1. Implement ApplicationEvent to create an Event
2. Implement ApplicationListener or Use @EventListener,@TransactionalEventListener to capture an Event
   `When the task is Transactional we should use @TransactionalEventListener otherwise the event would not be listened to.`
3. Use ApplicationEventPublisher to publish an Event

# 3. Spring Session with Redis

Dependency

1. spring-boot-starter-data-redis
2. spring-session-data-redis

In Java, use  `HttpSession` to access and Send session.
In Thymeleaf use `${session}` to access session.

# 4. Spring Batch Overview

Spring Batch is a framework designed for processing large volumes of data in batch jobs. It provides robust support for
transaction management, job processing, and parallel execution while ensuring fault tolerance, scalability, and
performance.

## Key Features of Spring Batch

- **Chunk-based processing:** Reads large data sets in chunks for efficient processing.
- **Transaction management:** Supports rollback and retry mechanisms.
- **Job scheduling & execution:** Jobs can be triggered manually, scheduled, or event-driven.
- **Parallel processing & scalability:** Supports multi-threading and partitioning for high performance.
- **Built-in readers & writers:** Provides various data sources such as databases, files, and messaging systems.
- **Error handling & retries:** Built-in mechanisms for skipping and retrying failed records.

---

## 1. Dependency for Spring Batch

To use Spring Batch, add the following dependencies to your `pom.xml` (for Maven-based projects):

```xml

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-batch</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.hsqldb</groupId>
        <artifactId>hsqldb</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

For **Gradle**:

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-batch'
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    runtimeOnly 'org.hsqldb:hsqldb'
}
```

---

## 2. Key Elements of Spring Batch

Spring Batch is built around several core concepts:

### i. Job

A **Job** is the main container for batch processing. It consists of **one or more Steps**.

```java

@Bean
public Job myJob(JobRepository jobRepository, Step step1) {
    return new JobBuilder("myJob", jobRepository)
            .start(step1)
            .build();
}
```

### ii. Step

A **Step** defines a stage in the job, such as reading, processing, and writing data.

```java

@Bean
public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("step1", jobRepository)
            .<String, String>chunk(10, transactionManager)
            .reader(itemReader())
            .processor(itemProcessor())
            .writer(itemWriter())
            .build();
}
```

### iii. ItemReader

Reads data from a source (e.g., database, file, or API).

```java

@Bean
public ItemReader<String> itemReader() {
    return new ListItemReader<>(List.of("item1", "item2", "item3"));
}
```

### iv. ItemProcessor

Processes each item before writing.

```java

@Bean
public ItemProcessor<String, String> itemProcessor() {
    return item -> item.toUpperCase(); // Convert to uppercase
}
```

### v. ItemWriter

Writes the processed data to an output source.

```java

@Bean
public ItemWriter<String> itemWriter() {
    return items -> items.forEach(System.out::println);
}
```

---

## 3. How Spring Batch Works

Spring Batch follows a structured flow:

1. **JobLauncher** starts a Job.
2. **Job** contains multiple Steps.
3. Each **Step** processes data in a sequence:
    - **ItemReader** reads data.
    - **ItemProcessor** transforms data.
    - **ItemWriter** writes data.
4. **Job Repository** stores job execution details.
5. Job completes, fails, or restarts as needed.

---

## 4. Basic Example of a Spring Batch Job

```java

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Bean
    public Job myJob(JobRepository jobRepository, Step step1) {
        return new JobBuilder("myJob", jobRepository)
                .start(step1)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step1", jobRepository)
                .<String, String>chunk(10, transactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public ItemReader<String> itemReader() {
        return new ListItemReader<>(List.of("Java", "Spring", "Batch"));
    }

    @Bean
    public ItemProcessor<String, String> itemProcessor() {
        return item -> "Processed: " + item;
    }

    @Bean
    public ItemWriter<String> itemWriter() {
        return items -> items.forEach(System.out::println);
    }
}
```

### Expected Output

```
Processed: Java
Processed: Spring
Processed: Batch
```

---

## Conclusion

Spring Batch is a powerful framework for handling batch processing tasks efficiently. By defining **Jobs, Steps,
ItemReaders, Processors, and Writers**, you can create scalable and robust data processing pipelines. 🚀

# 5. WebSocket

***Spring WebSocket is a module in the Spring Framework that provides real-time, full-duplex communication between
client and server over WebSocket protocol. Unlike traditional HTTP request-response cycles, WebSockets maintain a
persistent connection, allowing bidirectional communication without re-establishing the connection for each message.***

***In WebSocket, when someone connects to the socket and sends a message, we can receive that message and broadcast it
to everyone or send it to a specific user.***

## Dependency

1. `spring-boot-starter-websocket`
2. `spring-boot-starter-web`
3. `spring-boot-starter-messaging` (If using STOMP with SockJS)

## Configuration

1. configureMessageBroker
2. registerStompEndpoints

These two methods are part of Spring WebSockets with STOMP (Simple Text Oriented Messaging Protocol).
They help configure message handling between the client (browser) and server.

1️⃣ configureMessageBroker(MessageBrokerRegistry registry)
👉 What does it do?
It sets up the message broker (like a post office) that handles sending and receiving messages.

👉 Why do we need it?
Without a broker, clients cannot send or receive real-time messages.

👉 How does it work?

```java

@Override
public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/queue", "/topic");
    registry.setApplicationDestinationPrefixes(destinationPrefix);
}
```

Enables a built-in message broker for sending messages.
Messages with "/queue" and "/topic" will be handled by this broker.
/topic is for broadcasting messages to multiple users (like a news feed).
/queue is for sending messages to a specific user (like a private message).
registry.setApplicationDestinationPrefixes(destinationPrefix);

Defines a prefix (destinationPrefix) for application-level messages.
Clients will send messages to destinations starting with this prefix.
Example: If destinationPrefix = "/app", clients will send messages like:

```javascript
stompClient.send("/app/stockPrice", {}, JSON.stringify({symbol: "HK"}));
```

The server will receive this and process it.

2️⃣ registerStompEndpoints(StompEndpointRegistry registry)
👉 What does it do?
It sets up WebSocket endpoints (like a URL) `where clients can connect`.

👉 Why do we need it?
Without this, clients won’t know where to connect to WebSockets.

👉 How does it work?

```java

@Override
public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
            .withSockJS();
}
```

Adds an endpoint (e.g., /ws) for clients to connect.
withSockJS() enables SockJS, which helps when WebSockets are not supported.
Example:

If registerEndpoint = "/ws", the client will connect like this:

```javascript
var stompClient = Stomp.over(new SockJS('/ws'));
stompClient.connect({}, function () {
    console.log("Connected!");
});
```

This establishes a WebSocket connection to /ws.

### Summary

| Method                   | What it does               | Why it's needed             | How it works                                    |
|--------------------------|----------------------------|-----------------------------|-------------------------------------------------|
| `configureMessageBroker` | Sets up message broker     | Enables real-time messaging | Defines `/topic` and `/queue` for communication |
| `registerStompEndpoints` | Creates WebSocket endpoint | Allows clients to connect   | Uses SockJS for compatibility                   |

## Elements of Spring WebSocket

Spring provides multiple ways to work with WebSockets:

1. **WebSocket API**: Raw WebSocket implementation.
2. **STOMP (Simple Text Oriented Messaging Protocol)**: A higher-level protocol that supports features like message
   routing, subscriptions, and message acknowledgments.
3. **SockJS**: A fallback mechanism for browsers that do not support WebSockets.

## How to Send a Sample Message in Spring WebSocket

### 1. Configure WebSocket

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // Enables a simple broker for broadcasting messages
        registry.setApplicationDestinationPrefixes("/app"); // Prefix for messages from clients
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // WebSocket endpoint
                .setAllowedOrigins("*") // Allow cross-origin requests
                .withSockJS(); // Enable SockJS fallback
    }
}
```

### 2. Create WebSocket Controller

```java
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/message") // Client sends message to "/app/message"
    @SendTo("/topic/response")  // Server broadcasts message to "/topic/response"
    public String processMessage(String message) {
        return "Server Response: " + message;
    }
}
```

### 3. Client JavaScript Code to Send and Receive Messages

```html
<!DOCTYPE html>
<html>
<head>
    <title>WebSocket Example</title>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.5.1/sockjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
</head>
<body>
<input type="text" id="message" placeholder="Type a message">
<button onclick="sendMessage()">Send</button>
<div id="messages"></div>

<script>
    var socket = new SockJS('/ws');
    var stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        // Subscribe to receive messages
        stompClient.subscribe('/topic/response', function (message) {
            document.getElementById("messages").innerHTML += "<p>" + message.body + "</p>";
        });
    });

    function sendMessage() {
        var message = document.getElementById("message").value;
        stompClient.send("/app/message", {}, message);
    }
</script>
</body>
</html>
```

### 4. How Messages are Sent and Received

1. **Client sends a message**

    - Sends to `/app/message` (configured in `@MessageMapping`).
    - Example: `stompClient.send("/app/message", {}, "Hello!")`.

2. **Server receives and processes message**

    - `@MessageMapping("/message")` handles the message.

3. **Server sends response to topic**

    - Uses `@SendTo("/topic/response")`.
    - Message gets broadcasted to all clients subscribed to `/topic/response`.

4. **Clients receive the message**

    - Subscribed clients to `/topic/response` receive the message.

