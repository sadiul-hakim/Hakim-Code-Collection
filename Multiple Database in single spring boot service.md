Multiple `DataSource` + multiple `EntityManagerFactory`** in a Spring Boot service using **MySQL and PostgreSQL**, with detailed wiring, configuration steps, and code examples.

---

## Step-by-Step Implementation

### 1. **Project Setup and Dependencies**

In your `pom.xml`, include:

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>
  <dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
  </dependency>
  <dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
  </dependency>
</dependencies>
```

These are based on both tutorials (Bouguern and Writech) ([Medium][1], [DEV Community][2]).

---

### 2. **`application.properties` Configuration**

Define connection settings for both databases:

```properties
# MySQL (e.g., userdb)
spring.datasource.mysql.url=jdbc:mysql://localhost:3306/userdb
spring.datasource.mysql.username=your_mysql_user
spring.datasource.mysql.password=secret
spring.datasource.mysql.driver-class-name=com.mysql.cj.jdbc.Driver

# PostgreSQL (e.g., productdb)
spring.datasource.postgres.url=jdbc:postgresql://localhost:5432/productdb
spring.datasource.postgres.username=your_pg_user
spring.datasource.postgres.password=secret
spring.datasource.postgres.driver-class-name=org.postgresql.Driver
```

As guided in the DEV tutorial—which uses the prefixes `spring.datasource.account` and `spring.datasource.company`—you can adapt naming as needed ([DEV Community][2]).

---

### 3. **Entity and Repository Structure**

Keep entities and repositories in separate packages for clarity and to help Spring wire them properly:

```
com.yourapp.mysql.entity        →  MySQL domain entities
com.yourapp.mysql.repository    →  MySQL Spring Data repositories

com.yourapp.postgres.entity     →  PostgreSQL domain entities
com.yourapp.postgres.repository →  PostgreSQL Spring Data repositories
```

This segregation aligns with both Baeldung and Writech’s advice on package structuring ([Baeldung on Kotlin][3], [DEV Community][2]).

---

### 4. **Configuration Classes**

#### **MySQL Configuration**

```java
@Configuration
@EnableJpaRepositories(
  basePackages = "com.yourapp.mysql.repository",
  entityManagerFactoryRef = "mysqlEntityManagerFactory",
  transactionManagerRef = "mysqlTransactionManager"
)
public class MySQLConfig {

  @Bean
  @ConfigurationProperties("spring.datasource.mysql")
  public DataSourceProperties mysqlDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  public DataSource mysqlDataSource() {
    return mysqlDataSourceProperties()
             .initializeDataSourceBuilder()
             .build();
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(
      EntityManagerFactoryBuilder builder) {
    return builder
        .dataSource(mysqlDataSource())
        .packages("com.yourapp.mysql.entity")
        .persistenceUnit("mysqlPU")
        .build();
  }

  @Bean
  public PlatformTransactionManager mysqlTransactionManager(
      @Qualifier("mysqlEntityManagerFactory") EntityManagerFactory emf) {
    return new JpaTransactionManager(emf);
  }
}
```

#### **PostgreSQL Configuration**

Similarly:

```java
@Configuration
@EnableJpaRepositories(
  basePackages = "com.yourapp.postgres.repository",
  entityManagerFactoryRef = "postgresEntityManagerFactory",
  transactionManagerRef = "postgresTransactionManager"
)
public class PostgresConfig {

  @Bean
  @ConfigurationProperties("spring.datasource.postgres")
  public DataSourceProperties postgresDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  public DataSource postgresDataSource() {
    return postgresDataSourceProperties()
             .initializeDataSourceBuilder()
             .build();
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean postgresEntityManagerFactory(
      EntityManagerFactoryBuilder builder) {
    return builder
        .dataSource(postgresDataSource())
        .packages("com.yourapp.postgres.entity")
        .persistenceUnit("postgresPU")
        .build();
  }

  @Bean
  public PlatformTransactionManager postgresTransactionManager(
      @Qualifier("postgresEntityManagerFactory") EntityManagerFactory emf) {
    return new JpaTransactionManager(emf);
  }
}
```

This pattern follows the guidelines in both Medium and Baeldung tutorials, including `@EnableJpaRepositories` wiring ([Medium][1], [Baeldung on Kotlin][3]).

---

### 5. **Usage in Services**

Inject and use your respective repositories:

```java
@Service
public class BusinessService {

  private final UserRepository userRepo;         // MySQL
  private final ProductRepository productRepo;   // Postgres

  public BusinessService(UserRepository userRepo,
                         ProductRepository productRepo) {
    this.userRepo = userRepo;
    this.productRepo = productRepo;
  }

  public void doSomething() {
    // MySQL operation
    userRepo.save(new User("Alice"));

    // PostgreSQL operation
    productRepo.save(new Product("Gadget", 99.99));
  }
}
```

Make sure each repository belongs to its specific `@EnableJpaRepositories` configuration.

---

### 6. **Wrap-up & Tips**

* **One config per DB**: Provides clarity and separation.
* **Package organization matters**: Prevents cross-talk between repositories and entities.
* **No global transactions**: Each DB uses its own transaction manager.
* **Boilerplate setup**: But fully maintainable and scalable for production systems.

---

## Optional: Video Tutorial

Here’s a helpful YouTube walkthrough illustrating a Spring Boot setup with PostgreSQL and MySQL:

[Connect Multiple Data Sources with PostgreSQL & MySQL \[2024\]](https://www.youtube.com/watch?v=huaXMckKkyA&utm_source=chatgpt.com)

---
