Here’s a list of **Apache data-related tools** that are written in **Java** or have a **Java implementation/core**:

---

### 🔹 **Apache Data Processing & Storage Tools (Java-based)**

#### 🧱 **Storage / Database / Data Warehousing**

* **Apache Cassandra** – NoSQL distributed database (Java core)
* **Apache HBase** – Hadoop-based distributed wide-column store (Java)
* **Apache Derby** – Lightweight, embedded relational database (100% Java)
* **Apache Phoenix** – SQL layer over HBase (Java)
* **Apache Hive** – Data warehouse infrastructure on Hadoop (Java)
* **Apache Druid** – Real-time analytics database (Java backend)
* **Apache Geode** – In-memory data grid (Java)

---

#### ⚙️ **Data Processing / Pipelines**

* **Apache Hadoop** – Distributed storage & batch processing (Java)
* **Apache Spark** – Large-scale data processing (Java/Scala-based)
* **Apache Flink** – Stream & batch processing (Java/Scala)
* **Apache Beam** – Unified batch/stream processing model (Java SDK)
* **Apache Storm** – Real-time computation system (Java)

---

#### 🔄 **Data Integration / Ingestion / ETL**

* **Apache NiFi** – Data flow automation and ETL (Java)
* **Apache Camel** – Integration framework (Java, supports EIPs)
* **Apache Hop** – Metadata-driven data integration platform (Java)
* **Apache Sqoop** – Transfer between Hadoop and RDBMS (Java)
* **Apache Gobblin** – Data ingestion framework (Java)
* **Apache Flume** – Distributed log/data collection (Java)

---

#### 🛢️ **Data Serialization / Formats**

* **Apache Avro** – Data serialization system (Java)
* **Apache Parquet** – Columnar storage format (Java library)
* **Apache ORC** – Optimized row columnar format (Java)

---

#### 🔎 **Search / Indexing**

* **Apache Lucene** – Search engine library (Java)
* **Apache Solr** – Search platform built on Lucene (Java)

---

#### 🧠 **Machine Learning / Analytics**

* **Apache Mahout** – Scalable machine learning (Java)
* **Apache SAMOA** – Stream mining and machine learning (Java)

---

#### 📊 **Visualization / Reporting**

* **Apache Superset** – *Not Java-based* (Python)
* **Apache Zeppelin** – Web-based notebook for data exploration (Java backend with JVM interpreters)

---

Good catch!

### 🔍 **Apache Drill**

* ✅ **Language**: **Written in Java**
* 📦 **Category**: SQL-on-anything / Query Engine
* 🧠 **Purpose**: Schema-free SQL query engine for **big data** and **NoSQL systems**.

---

### 🔹 What Drill Does:

* Allows querying **structured and semi-structured data** using standard **ANSI SQL**.
* Supports **JSON, Parquet, Avro, CSV**, HBase, MongoDB, S3, Hive, and more — **without needing schema definitions**.
* Often called "schema-free SQL" or "self-describing data" engine.

---

### 🔹 Tech Stack:

* Core engine: **Java**
* Execution engine: Based on **vectorized processing** for performance
* Uses **Apache Calcite** (also Java-based) for SQL parsing and planning

---

### 🔹 Use Cases:

* Ad hoc querying across disparate data sources
* Quick exploration of semi-structured data (like JSON logs)
* SQL-on-Hadoop/NoSQL for developers familiar with SQL

---
Here are **more Apache data-related tools written in Java** — beyond the common ones like Hadoop, Spark, NiFi, Drill, etc.

---

## 🧱 **Storage / Data Engines**

| Tool                  | Description                                                                                               |
| --------------------- | --------------------------------------------------------------------------------------------------------- |
| **Apache Accumulo**   | Secure, sorted, distributed key-value store built on HDFS (inspired by Google BigTable). Written in Java. |
| **Apache Jackrabbit** | Hierarchical content repository (JCR implementation), Java-based. Used in CMS/document systems.           |
| **Apache Jena**       | Java framework for building Semantic Web and Linked Data apps. RDF/OWL/SPARQL support.                    |
| **Apache Ratis**      | Java implementation of the RAFT consensus algorithm, used in distributed storage systems like Ozone.      |

---

## ⚙️ **Data Processing / ETL / Ingestion**

| Tool                   | Description                                                                                                 |
| ---------------------- | ----------------------------------------------------------------------------------------------------------- |
| **Apache DataFu**      | Java libraries for common data tasks, like aggregations, used with Pig and Hive.                            |
| **Apache Chukwa**      | Data collection system for monitoring large distributed systems. Java-based.                                |
| **Apache StreamPipes** | Java-based framework for building IoT data pipelines with a visual interface.                               |
| **Apache Tika**        | Java content detection and analysis framework — extracts metadata and text from many file types.            |
| **Apache UIMA**        | Unstructured Information Management Architecture for analyzing unstructured content like text, images, etc. |

---

## 🔄 **Integration / Messaging**

| Tool                               | Description                                                                                        |
| ---------------------------------- | -------------------------------------------------------------------------------------------------- |
| **Apache ServiceMix**              | ESB (Enterprise Service Bus) built on OSGi, Java-based. Integrates Camel, CXF, ActiveMQ, etc.      |
| **Apache MINA**                    | Java framework for developing high-performance and scalable network applications (e.g., TCP, UDP). |
| **Apache Thrift (Java binding)**   | Cross-language RPC framework, with full Java support for generating code.                          |
| **Apache Pulsar (partially Java)** | Java-based broker, client libraries in many languages. Competes with Kafka.                        |

---

## 🧠 **Query / Metadata / Governance**

| Tool                | Description                                                                                                  |
| ------------------- | ------------------------------------------------------------------------------------------------------------ |
| **Apache Calcite**  | Java SQL engine and optimizer used under the hood by Hive, Drill, Flink, etc.                                |
| **Apache Atlas**    | Metadata and governance framework for Hadoop. Core services in Java.                                         |
| **Apache Daffodil** | Java-based implementation of DFDL (Data Format Description Language), for parsing non-standard data formats. |

---

## 📈 **Monitoring / Visualization / Logs**

| Tool                  | Description                                                                            |
| --------------------- | -------------------------------------------------------------------------------------- |
| **Apache Log4j**      | Java logging framework. While not strictly “data” tool, critical in data-heavy apps.   |
| **Apache Chainsaw**   | Visual log viewer for Log4j logs. Java Swing application.                              |
| **Apache SkyWalking** | Application performance monitoring (APM) and observability platform — written in Java. |

---
