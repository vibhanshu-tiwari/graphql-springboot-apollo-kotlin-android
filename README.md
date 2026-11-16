# GraphQL in Practice: From Spring Boot APIs to Android with Apollo Kotlin

A hands-on GraphQL learning project demonstrating end-to-end GraphQL integration between a Spring Boot backend and an Android application using Apollo Kotlin.

This repository contains two independent projects that work together to demonstrate GraphQL fundamentals, API development, Android integration, CRUD operations, real-time subscriptions, and WebSocket connection management.

The goal is to explore GraphQL through practical implementation, understand how backend and mobile applications communicate, and document the challenges and solutions encountered during development.

## Project Overview

This repository consists of two independent applications.

### 1. Spring Boot GraphQL API

A backend application developed using Java 21, Spring Boot, and Maven.

It exposes GraphQL APIs for product management and supports real-time product updates through GraphQL subscriptions.

**Key features:**

- GraphQL schema definition.
- Product queries and mutations.
- Fetch all products and individual products.
- Add, update, and delete products.
- Real-time product updates using GraphQL subscriptions.
- WebSocket endpoint for subscription communication.
- Reactor Sinks for publishing product events.
- In-memory product storage for demonstration purposes.

### 2. Android Apollo Kotlin Client

An Android application developed using Kotlin and Apollo Kotlin to communicate with the Spring Boot GraphQL backend.

**Key features:**

- Apollo Kotlin client integration.
- GraphQL schema introspection and code generation.
- Fetch all products and individual products.
- Add, update, and delete products.
- GraphQL subscription integration.
- WebSocket communication using the `graphql-transport-ws` protocol.
- Subscription error handling and automatic retry.
- WebSocket handshake monitoring.
- Connection status monitoring.
- Handling real-time product updates.

## Disclaimer

This repository is created for educational and learning purposes.

The implementations demonstrate practical GraphQL integration between a Spring Boot backend and an Android application using Apollo Kotlin.

The code is intended to help developers understand the concepts, explore the implementation, and adapt the approaches to their own project requirements.

This is a learning project, not a production-ready application. Developers should review, test, and adapt the implementation according to their application's architecture, security requirements, and business needs.

## Architecture

The project demonstrates two communication mechanisms between the Android application and the Spring Boot backend.

```text
                  Android Application
                   (Kotlin + Apollo)
                           |
              +------------+------------+
              |                         |
           HTTP                      WebSocket
        GraphQL API                Subscriptions
              |                         |
              +------------+------------+
                           |
                   Spring Boot API
                       (Java 21)
                           |
                    GraphQL Layer
                           |
                   Product Controller
                           |
                   In-Memory Storage
                           |
                      Reactor Sinks
```

**HTTP communication**

- GraphQL queries.
- GraphQL mutations.

**WebSocket communication**

- GraphQL subscriptions.
- Real-time product event delivery.
- Connection lifecycle monitoring.
- Reconnection after connection failures.

## Technology Stack

| Area | Technology |
|---|---|
| Backend Language | Java 21 |
| Backend Framework | Spring Boot 4.1.1 |
| Backend Build Tool | Maven |
| API Technology | GraphQL |
| Reactive Programming | Project Reactor |
| Mobile Platform | Android |
| Mobile Language | Kotlin |
| Android GraphQL Client | Apollo Kotlin 5.2.0 |
| Android Build Tool | Gradle |
| Android Java Compatibility | Java 21 |
| Real-Time Communication | WebSocket |
| WebSocket Protocol | GraphQL over WebSocket (`graphql-transport-ws`) |
| Development IDE | IntelliJ IDEA and Android Studio |

## Repository Structure

```text
graphql-springboot-apollo-kotlin-android/
│
├── graphql-spring-api/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   │           └── graphql/
│   │               └── product.graphqls
│   └── pom.xml
│
├── GraphQLApolloDemo/
│   ├── app/
│   │   └── src/
│   │       └── main/
│   │           └── graphql/
│   │               ├── AddProduct.graphql
│   │               ├── DeleteProduct.graphql
│   │               ├── GetProduct.graphql
│   │               ├── GetProducts.graphql
│   │               ├── ProductAdded.graphql
│   │               ├── UpdateProduct.graphql
│   │               └── product.graphqls
│   ├── gradle/
│   ├── build.gradle.kts
│   └── settings.gradle.kts
│
├── .gitignore
├── README.md
└── LICENSE
```

The backend schema defines the GraphQL API. The Android project contains a local copy of the schema used by Apollo Kotlin for type-safe code generation, together with the operation definitions.

## GraphQL API

The backend exposes the following GraphQL operations.

### Queries

| Operation | Description |
|---|---|
| `products` | Fetch all products |
| `product(id)` | Fetch a product using its ID |

### Mutations

| Operation | Description |
|---|---|
| `addProduct` | Add a new product |
| `updateProduct` | Update an existing product |
| `deleteProduct` | Delete a product |

### Subscription

| Operation | Description |
|---|---|
| `productAdded` | Receive real-time notifications when a product is added |

## Getting Started

### Prerequisites

Install the following development tools:

- JDK 21
- IntelliJ IDEA
- Android Studio
- Android SDK
- Git
- Maven (if you are not using the included Maven Wrapper)
- Android emulator or compatible Android device

### 1. Clone the Repository

```bash
git clone https://github.com/vibhanshu-tiwari/graphql-springboot-apollo-kotlin-android.git
```

Navigate to the repository:

```bash
cd graphql-springboot-apollo-kotlin-android
```

### 2. Run the Spring Boot Backend

Open the `graphql-spring-api` project in IntelliJ IDEA.

Navigate to the backend directory:

```bash
cd graphql-spring-api
```

Build the project:

```bash
mvn clean package
```

Run the application:

```bash
mvn spring-boot:run
```

If the Maven Wrapper is included, you can use the following commands instead.

**Windows:**

```bash
mvnw.cmd spring-boot:run
```

**Linux/macOS:**

```bash
./mvnw spring-boot:run
```

The GraphQL HTTP endpoint is:

```text
http://localhost:8080/graphql
```

The GraphQL WebSocket endpoint is:

```text
ws://localhost:8080/graphql
```

### 3. Run the Android Application

Open the `GraphQLApolloDemo` project in Android Studio.

1. Allow Gradle synchronization to complete.
2. Verify the Apollo Kotlin configuration.
3. Make sure the Spring Boot backend is running.
4. Configure the backend URL for your Android execution environment.
5. Build and run the Android application.

For a standard Android emulator, the host machine's localhost can be accessed using:

**GraphQL HTTP endpoint:**

```text
http://10.0.2.2:8080/graphql
```

**GraphQL WebSocket endpoint:**

```text
ws://10.0.2.2:8080/graphql
```

**Note:** The `10.0.2.2` address is intended for the standard Android emulator. For a physical Android device, configure the backend address using your development machine's reachable network IP and ensure the required network access is available.

## Apollo Kotlin Integration

The Android application uses Apollo Kotlin to execute GraphQL operations.

Apollo Kotlin generates type-safe Kotlin models and operation classes using the GraphQL schema and operation definitions.

The project includes operations for:

- GetProducts
- GetProduct
- AddProduct
- UpdateProduct
- DeleteProduct
- ProductAdded

The backend GraphQL schema is used as the source for Android code generation. The Android project maintains its local schema file so Apollo can generate the required Kotlin types during the build.

## Real-Time GraphQL Subscriptions

The project demonstrates real-time communication using GraphQL subscriptions.

The Android application establishes a WebSocket connection with the Spring Boot backend using the `graphql-transport-ws` protocol.

When a new product is added:

1. The backend processes the mutation.
2. The backend publishes the product event through Reactor Sinks.
3. The GraphQL subscription receives the event.
4. Apollo Kotlin delivers the subscription result to the Android application.
5. The application updates its local product list.

The subscription implementation also demonstrates connection failure handling and retry behavior.

## WebSocket Connection Monitoring

An additional part of this project explores how to monitor the actual WebSocket connection lifecycle on Android.

A custom WebSocket factory wraps the underlying WebSocket listener to observe connection events while forwarding callbacks to Apollo Kotlin.

The implementation monitors:

- WebSocket handshake initiation.
- Successful HTTP `101 Switching Protocols` response.
- WebSocket connection acknowledgment (`connection_ack`).
- Incoming WebSocket frames.
- Ping frames.
- Connection closing and closed events.
- Handshake or connection failures.
- Reconnection behavior.

This helps distinguish between a successful WebSocket handshake, GraphQL protocol acknowledgment, and actual subscription event delivery.

## Learning Objectives

Through this project, we explore:

- Understanding GraphQL fundamentals.
- Comparing REST APIs and GraphQL APIs.
- Building GraphQL APIs using Spring Boot.
- Defining GraphQL schemas, queries, mutations, and subscriptions.
- Integrating Apollo Kotlin into Android.
- Generating type-safe Kotlin models from GraphQL schemas.
- Managing GraphQL responses and errors.
- Implementing real-time communication.
- Understanding WebSocket connection lifecycle.
- Handling network failures and subscription reconnection.

## GraphQL Article Series

This repository accompanies a practical article series covering the concepts and implementations demonstrated in this project.

The articles will be published incrementally.

1. **Why GraphQL? Understanding GraphQL from an Android Developer's Perspective**
2. **Building a GraphQL API with Spring Boot**
3. **Integrating Apollo Kotlin into an Android Application**
4. **Working with GraphQL Queries and Mutations in Android Using Apollo Kotlin**
5. **Implementing Real-Time GraphQL Subscriptions with Apollo Kotlin and Spring Boot**
6. **Understanding WebSocket Handshake, Connection Monitoring and Reconnection in Apollo Kotlin**

Article links will be added here as they are published.

## Current Project Scope and Limitations

This is an educational and practical implementation intended to explore GraphQL integration between a Spring Boot backend and an Android application.

Current implementation characteristics:

- Product data is stored in memory.
- Product data is not persisted across backend restarts.
- The project focuses on GraphQL integration and communication rather than production infrastructure.
- Subscription event delivery depends on an active connection; persistent event delivery and replay are not implemented.
- WebSocket connection monitoring has been implemented and tested with the current single-subscription use case. Managing connection status across multiple simultaneous subscriptions is outside the current scope.

## License

This project is licensed under the MIT License.

See the [LICENSE](LICENSE) file for details.
