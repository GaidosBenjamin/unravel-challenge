# Unravel Challenge

This README provides instructions on how to run the Unravel Challenge project.

## Prerequisites

The only prerequisite to running this project is having Docker installed.

## Running Tests

To run both Unit Tests (UT) and Integration Tests (IT), use the following command from the project root:

```bash
docker compose --file docker-compose-it.yml up --exit-code-from integration-tests
```

## Starting the Application

To start the application, run the following command from the project root (only first build may take some time):

```bash
docker compose up --build
```

During the application run, 5 containers are turned on:
- Redis
- MySQL
- Spring-App
- Prometheus
- Grafana

Endpoints are documented using Swagger UI, which can be accessed at:
```bash
http://localhost:8080/swagger-ui.html
```

## Project Components

### 1. Session Management API

This component has both Unit Tests and Integration Tests.

To run the Unit Tests separately:
```bash
./mvnw -pl unravel-service test -Dtest=RedisSessionManagerTest
```

Endpoints are exposed at: `/dummy/sessions`

### 2. Memory Management Issues

This component has both Unit Tests and Integration Tests.

To run a stress test and generate a heapdump:
```bash
./mvnw test -pl unravel-service -Dtest=OldMemoryManagerStressTest -DargLine="-Xms512m -Xmx512m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./heapdump.hprof"
```

To generate a JFR file:
```bash
./mvnw test -pl unravel-service -Dtest=OldMemoryManagerStressTest -DargLine="-Xms512m -Xmx512m -XX:+FlightRecorder -XX:StartFlightRecording=dumponexit=true,filename=./recording.jfr"
```

After the test is run, the heapdump.hprof can be investigated using different tools, e.g., IntelliJ, JMC.

### 3. Producer-Consumer Concurrency Problem

This component has only Unit Tests.

To run the tests:
```bash
./mvnw -pl unravel-service test -Dtest=LogProcessorTest
```

### 4. Deadlock Example

This component has only Unit Tests.

To run a test that produces a deadlock:
```bash
./mvnw -pl unravel-service test -Dtest=DeadlockSimulatorTest
```

To run a solution that doesn't produce a deadlock:
```bash
./mvnw -pl unravel-service test -Dtest=NewDeadlockSimulatorTest
```

### 5. Database Connection Pooling

After the application is running, go to the Grafana Dashboard:
```
http://localhost:3000/d/hikari-cp-status
```

To start the stress test, call:
```bash
curl --location --request POST 'http://localhost:8080/database/stress-test'
```