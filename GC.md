# Java Garbage Collectors (GC) and How to Switch Between Them

## 🔄 Available GC Types and Use Cases

| GC Name       | JVM Option                 | Use Case                                                                 |
|---------------|----------------------------|--------------------------------------------------------------------------|
| Serial GC     | `-XX:+UseSerialGC`         | Best for small apps or low-memory environments. Single-threaded.         |
| Parallel GC   | `-XX:+UseParallelGC`       | High throughput, multi-threaded. Good for batch processing.              |
| CMS GC        | `-XX:+UseConcMarkSweepGC`  | Low pause times, deprecated since JDK 9, removed in JDK 14.              |
| G1 GC         | `-XX:+UseG1GC`             | Balanced throughput and pause times. Default from JDK 9 to JDK 14.       |
| ZGC           | `-XX:+UseZGC`              | Low latency, large heap support. Experimental in JDK 11, stable in 15+.  |
| Shenandoah GC | `-XX:+UseShenandoahGC`     | Low-pause GC from Red Hat. Similar to ZGC but more available.            |
| Epsilon GC    | `-XX:+UseEpsilonGC`        | No-op GC (no memory reclamation). For testing/benchmarking only.         |

> **Note:** Always match the GC with the app's size, responsiveness needs, and Java version compatibility.

---

## 🛠 How to Switch GC in Different Environments

### 🔹 1. Single Java File

```bash
java -XX:+UseG1GC YourFile.java
```

Or compile and run:
javac YourFile.java
java -XX:+UseG1GC YourFile

### 📦 2. Executable JAR File
java -XX:+UseZGC -jar your-app.jar

You can also add other JVM options, like memory settings:

java -Xmx1G -XX:+UseShenandoahGC -jar your-app.jar

### 🧪 3. Maven Project
#### Option A: Using exec-maven-plugin
Add this to your pom.xml:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>exec-maven-plugin</artifactId>
      <version>3.1.0</version>
      <configuration>
        <mainClass>your.main.Class</mainClass>
        <arguments></arguments>
        <systemProperties>
          <property>
            <name>exec.args</name>
            <value>-XX:+UseZGC</value>
          </property>
        </systemProperties>
      </configuration>
    </plugin>
  </plugins>
</build>
```

#### Option B: Using Environment Variable
```bash
export JAVA_TOOL_OPTIONS="-XX:+UseParallelGC"
mvn clean install
mvn exec:java
```

### 🛠️ 4. Gradle Project
#### Option A: In build.gradle
```groovy
run {
    jvmArgs = ["-XX:+UseG1GC"]
}
```

#### Option B: From Command Line
./gradlew run --no-daemon -Dorg.gradle.jvmargs="-XX:+UseShenandoahGC"

### 🧾 Check Active GC at Runtime
Use this to confirm the JVM flags in use:
java -XX:+PrintCommandLineFlags -version

Or print GC logs (optional):
java -Xlog:gc -XX:+UseZGC -jar your-app.jar
