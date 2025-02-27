# 4. Spring Integration

Dependency

1. spring-integration-jpa

Spring Integration is an extension of the Spring framework that provides a lightweight and flexible solution for
building
enterprise integration solutions. It supports messaging-based architectures to connect different systems, applications,
or processes within an enterprise. Using well-defined patterns, it enables seamless communication and data exchange
between
components while ensuring loose coupling and scalability.

## Key Elements of Spring Integration

1. `Endpoints` are components that enable interaction between external systems and the integration framework. Examples
   include inbound adapters (consume data from external sources) and outbound adapters (send data to external systems).
2. `Filters` determine whether a message should be allowed to proceed through the integration flow. They act as
   decision-makers, accepting or rejecting messages based on specified criteria.
3. `Transformers` modify or convert the content of a message to a required format. For example, they can convert a JSON
   payload into a Java object or vice versa.
4. `Routers` determine the path a message should take based on its content or metadata. For example, a router can direct
   messages to different endpoints based on a header value.
5. `Service activators` are endpoints that connect a Spring service (business logic) to the messaging system. They
   process
   incoming messages and produce responses, if necessary.
6. `Channels` are the conduits for passing messages between different components in Spring Integration. They decouple
   the
   sender and receiver to ensure flexibility and scalability.

## Types of Channels in Spring Integration

1. `Direct Channel` - Messages are sent directly from the sender to the receiver within the same thread. This is
   synchronous and has low overhead.
2. `Queue Channel` - Acts like a message queue where messages are stored until consumed by a receiver. It supports
   asynchronous communication.
3. `Publish-Subscribe Channel` - Allows multiple consumers to subscribe to the same channel and receive copies of the
   same message, enabling broadcast messaging.
4. `Executor Channel` - Utilizes a task executor for asynchronous message handling. It decouples the sender and receiver
   threads for better performance.
5. `Priority Channel` - Processes messages based on priority rather than arrival order, enabling prioritized message
   handling.

## Integration Flow

An integration flow defines the sequence of components through which messages pass in a Spring Integration application.
It typically consists of:

1. Message Source – Generates or receives messages.
2. Channels – Pass messages between components.
3. Message Processors – Includes filters, transformers, routers, and service activators.
4. Message Destination – The endpoint where messages are sent or processed.

## How Integration Flow Works

1. A Message Source (e.g., an inbound adapter) receives data from an external system or generates it.
2. The message is passed into a Channel.
3. A Filter may determine if the message should proceed.
4. If accepted, a Transformer converts the message into the desired format.
5. A Router can direct the message to appropriate components or endpoints.
6. The Service Activator processes the message or performs business logic.
7. The message is delivered to its final Message Destination, which could be another system, database, or file.

## Examples

### Direct Channel

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.handler.ServiceActivatingHandler;
import org.springframework.integration.annotation.ServiceActivator;

@Configuration
public class IntegrationConfig {

    // Define Direct Channel
    @Bean
    public DirectChannel directChannel() {
        return new DirectChannel();
    }

    // Define Service Activator to handle the message from the channel
    @Bean
    @ServiceActivator(inputChannel = "directChannel")
    public ServiceActivatingHandler serviceActivator() {
        return new ServiceActivatingHandler(msg -> {
            System.out.println("Message received: " + msg.getPayload());
        });
    }
}
```

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.core.MessageChannel;

@SpringBootApplication
public class SpringIntegrationApp implements CommandLineRunner {

    @Autowired
    private DirectChannel directChannel;

    public static void main(String[] args) {
        SpringApplication.run(SpringIntegrationApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Send a message to the direct channel
        directChannel.send(MessageBuilder.withPayload("Hello, Spring Integration!").build());
    }
}

```

### Publish-Subscribe Channel

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.handler.ServiceActivatingHandler;
import org.springframework.integration.annotation.ServiceActivator;

@Configuration
public class IntegrationConfig {

    // Define Publish-Subscribe Channel
    @Bean
    public PublishSubscribeChannel publishSubscribeChannel() {
        return new PublishSubscribeChannel();
    }

    // First Subscriber
    @Bean
    @ServiceActivator(inputChannel = "publishSubscribeChannel")
    public ServiceActivatingHandler subscriberOne() {
        return new ServiceActivatingHandler(msg -> {
            System.out.println("Subscriber 1 received: " + msg.getPayload());
        });
    }

    // Second Subscriber
    @Bean
    @ServiceActivator(inputChannel = "publishSubscribeChannel")
    public ServiceActivatingHandler subscriberTwo() {
        return new ServiceActivatingHandler(msg -> {
            System.out.println("Subscriber 2 received: " + msg.getPayload());
        });
    }
}

```

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.core.MessageChannel;

@SpringBootApplication
public class SpringIntegrationApp implements CommandLineRunner {

    @Autowired
    private PublishSubscribeChannel publishSubscribeChannel;

    public static void main(String[] args) {
        SpringApplication.run(SpringIntegrationApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Send a message to the Publish-Subscribe channel
        publishSubscribeChannel.send(MessageBuilder.withPayload("Hello to multiple subscribers!").build());
    }
}
```
### File Processor

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.io.File;
import java.io.IOException;

@Configuration
public class FileProcessingIntegrationConfig {

    private static final String INPUT_DIR = "F:\\Spring Integration\\input";  // Directory to read files from
    private static final String OUTPUT_DIR = "F:\\Spring Integration\\output"; // Directory for processed files

    @Bean
    public MessageChannel fileInputChannel() {
        return MessageChannels.queue().getObject();
    }

    @Bean
    public MessageChannel processedFileChannel() {
        return MessageChannels.direct().getObject();
    }

    @Bean
    public IntegrationFlow fileProcessorRow() {
        return IntegrationFlow
                .from(
                        Files.inboundAdapter(new File(INPUT_DIR))
                                .filter(new SimplePatternFileListFilter("*.txt")) // Accept only .txt files
                                .autoCreateDirectory(true),
                        e -> e.poller(p -> p.fixedDelay(10_000))// Poll every 10 second
                ).channel(fileInputChannel()) // Input channel
                // Transformer to convert file content to uppercase
                .<File, String>transform(file -> {
                    try {
                        return new String(java.nio.file.Files.readAllBytes(file.toPath())).toUpperCase();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to read file", e);
                    }
                }).channel(processedFileChannel()) // Pass to the next channel
                // Service Activator to process the transformed message
                .handle(processFileService())
                .get();
    }


    @Bean
    @ServiceActivator(inputChannel = "processedFileChannel")
    public MessageHandler processFileService() {
        return message -> {
            var result = (String) message.getPayload();
            System.out.println("Processed Content: " + result);

            // Optionally, save the processed content to a file
            File outputFile = new File(OUTPUT_DIR, "processed_" + System.currentTimeMillis() + ".txt");
            try {
                outputFile.getParentFile().mkdirs(); // Ensure directory exists
                java.nio.file.Files.write(outputFile.toPath(), result.getBytes());
                System.out.println("Saved processed file to: " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to write processed file", e);
            }
        };
    }
}
```
