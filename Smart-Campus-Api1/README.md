
# SmartCampus Sensor & Room Management API

## Overview

This is a RESTful API built using **JAX-RS (Jersey)** and an embedded **Grizzly HTTP server** for the University of Westminster "Smart Campus" initiative. The API manages campus Rooms and Sensors, including historical sensor readings, with full error handling and request/response logging.

The system is built entirely in Java using JAX-RS — no Spring Boot, no database. All data is stored in-memory using `ConcurrentHashMap`.

**Base URL:** `http://localhost:8080/api/v1`

---

## API Design Overview

The API follows RESTful principles with a clear resource hierarchy:

```
/api/v1                          → Discovery endpoint
/api/v1/rooms                    → Room collection
/api/v1/rooms/{roomId}           → Single room
/api/v1/sensors                  → Sensor collection
/api/v1/sensors?type={type}      → Filter sensors by type
/api/v1/sensors/{sensorId}/readings  → Sensor reading history (sub-resource)
```

### Resource Models

**Room** — represents a physical campus room with a unique ID, name, capacity, and a list of assigned sensor IDs.

**Sensor** — represents a hardware sensor with a type (e.g. Temperature, CO2, Occupancy), a status (ACTIVE, MAINTENANCE, OFFLINE), a current value, and a reference to its room.

**SensorReading** — represents a historical data point captured by a sensor, with a UUID, epoch timestamp, and measured value.

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Core language |
| JAX-RS 3.1 (Jersey) | REST framework |
| Grizzly HTTP Server | Embedded server |
| Jackson | JSON serialisation |
| Maven | Build tool |
| ConcurrentHashMap | In-memory data store |

---

## How to Build and Run

### Prerequisites
- Java 17 or higher installed
- Apache Maven 3.8+ installed
- NetBeans IDE (or any IDE with Maven support)

### Step 1 — Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/SmartCampusAPI.git
cd SmartCampusAPI
```

### Step 2 — Build the Project

```bash
mvn clean package
```

This will compile the code and produce a fat JAR with all dependencies included.

### Step 3 — Run the Server

```bash
java -jar target/smart-campus-api-1.0-SNAPSHOT-jar-with-dependencies.jar
```

You should see:

```
Server started at: http://localhost:8080/api/v1
Press ENTER to stop the server...
```

### Step 4 — Test the API

Open Postman or use the curl commands below.

---

## Sample curl Commands

### 1. Discovery Endpoint — GET /api/v1

```bash
curl -X GET http://localhost:8080/api/v1
```

**Expected Response (200 OK):**
```json
{
  "version": "1.0",
  "contact": "admin@smartcampus.ac.uk",
  "links": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

---

### 2. Create a Room — POST /api/v1/rooms

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"LIB-301\",\"name\":\"Library Quiet Study\",\"capacity\":50}"
```

**Expected Response (201 Created):**
```json
{
  "id": "LIB-301",
  "name": "Library Quiet Study",
  "capacity": 50,
  "sensorIds": []
}
```

---

### 3. Get All Rooms — GET /api/v1/rooms

```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

**Expected Response (200 OK):**
```json
[
  {
    "id": "LIB-301",
    "name": "Library Quiet Study",
    "capacity": 50,
    "sensorIds": ["TEMP-001"]
  }
]
```

---

### 4. Create a Sensor — POST /api/v1/sensors

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"TEMP-001\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":0.0,\"roomId\":\"LIB-301\"}"
```

**Expected Response (201 Created):**
```json
{
  "id": "TEMP-001",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 0.0,
  "roomId": "LIB-301"
}
```

---

### 5. Filter Sensors by Type — GET /api/v1/sensors?type=Temperature

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```

**Expected Response (200 OK):**
```json
[
  {
    "id": "TEMP-001",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 22.5,
    "roomId": "LIB-301"
  }
]
```

---

### 6. Add a Sensor Reading — POST /api/v1/sensors/{sensorId}/readings

```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":22.5}"
```

**Expected Response (201 Created):**
```json
{
  "id": "a1b2c3d4-...",
  "timestamp": 1714000000000,
  "value": 22.5
}
```

---

### 7. Get Sensor Reading History — GET /api/v1/sensors/{sensorId}/readings

```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

---

### 8. Delete a Room — DELETE /api/v1/rooms/{roomId}

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

**If room has sensors (409 Conflict):**
```json
{
  "error": "Room still has active sensors assigned. Remove all sensors before deleting the room."
}
```

---

## Error Handling

| HTTP Status | Scenario | Exception |
|---|---|---|
| 409 Conflict | Deleting a room that still has sensors | `RoomNotEmptyException` |
| 422 Unprocessable Entity | Creating a sensor with a non-existent roomId | `LinkedResourceNotFoundException` |
| 403 Forbidden | Posting a reading to a MAINTENANCE sensor | `SensorUnavailableException` |
| 500 Internal Server Error | Any unexpected runtime error | `GlobalExceptionMapper` |

---

## Conceptual Report — Question Answers

### Part 1 — JAX-RS Resource Lifecycle

By default, JAX-RS creates a **new instance of each Resource class for every incoming HTTP request** (per-request lifecycle). This means resource classes are not singletons — each request gets its own fresh object. As a result, you cannot store shared data as instance fields inside resource classes, because that data would be lost after the request ends. To manage shared in-memory data safely, a **singleton `DataStore` class** is used. It holds all `ConcurrentHashMap` collections and is accessed via a static `getInstance()` method. `ConcurrentHashMap` is used instead of `HashMap` to prevent race conditions when multiple requests read and write simultaneously.

---

### Part 1 — HATEOAS

HATEOAS (Hypermedia as the Engine of Application State) means that API responses include **links to related resources and available actions**, rather than just data. This allows clients to navigate the API dynamically without needing to read static documentation. For example, a response for a room could include a link to its sensors. This reduces coupling between the client and server — if a URL changes, the client discovers the new path from the response rather than breaking. It also makes the API self-documenting and easier to explore.

---

### Part 2 — Returning IDs vs Full Objects

Returning only IDs in a room list is more **bandwidth-efficient**, but forces the client to make additional requests to fetch details for each room — this is the "N+1 problem". Returning full room objects in one response is better for clients that need all the data immediately, reduces round trips, and simplifies client-side code. The trade-off is a larger initial payload. For a campus API with moderate data sizes, returning full objects is generally preferred.

---

### Part 2 — Is DELETE Idempotent?

In this implementation, DELETE is **idempotent in effect but not in response**. The first DELETE on a room removes it and returns `204 No Content`. A second identical DELETE finds no room and returns `404 Not Found`. The server state is the same after both calls (the room is gone), which satisfies the idempotency requirement — the outcome does not change. However, the HTTP status code differs between calls. This is acceptable and standard REST behaviour.

---

### Part 3 — @Consumes and Content-Type Mismatch

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS that the POST endpoint only accepts `application/json` request bodies. If a client sends data as `text/plain` or `application/xml`, JAX-RS automatically returns a **415 Unsupported Media Type** response without the method ever being invoked. This protects the API from malformed or unexpected input formats without requiring any manual checking in the resource method.

---

### Part 3 — @QueryParam vs Path Segment for Filtering

Using `@QueryParam` for filtering (e.g. `/sensors?type=CO2`) is superior because query parameters are semantically designed for **optional filtering, searching, and sorting** of a collection. The path segment approach (e.g. `/sensors/type/CO2`) implies that `type/CO2` is a distinct resource, which is semantically incorrect — you are filtering a collection, not identifying a specific resource. Query parameters are also easier to combine (e.g. `?type=CO2&status=ACTIVE`) and are the standard convention understood by all HTTP clients and caching layers.

---

### Part 4 — Sub-Resource Locator Pattern

The Sub-Resource Locator pattern allows a resource method to **delegate handling to another class** rather than defining all nested paths in one place. In this API, `SensorResource` delegates `/sensors/{sensorId}/readings` to `SensorReadingResource`. This improves separation of concerns — each class has one responsibility. It also makes large APIs easier to maintain, test, and extend. Without this pattern, one resource class would grow to handle dozens of paths, making it hard to read and modify.

---

### Part 5 — Why 422 Instead of 404

A `404 Not Found` means the **requested URL does not exist**. But when a client POSTs a sensor with a non-existent `roomId`, the URL `/api/v1/sensors` is perfectly valid — the problem is inside the request body. A `422 Unprocessable Entity` is more accurate because it signals that the server understood the request format and the URL, but the **semantic content of the payload is invalid** (it references something that doesn't exist). This gives the client a clearer signal about where the problem lies.

---

### Part 5 — Risks of Exposing Stack Traces

Exposing Java stack traces to external API consumers is a serious security risk. A stack trace reveals: the **internal package and class names** of the application (helping attackers map the codebase), the **exact line numbers** where errors occur (making targeted exploits easier), the **framework and library versions** in use (allowing attackers to look up known vulnerabilities for those versions), and sometimes **method signatures and data values** that expose business logic. The global `ExceptionMapper<Throwable>` prevents this by catching all unexpected errors and returning only a generic `500 Internal Server Error` message.

---

### Part 5 — Why Use Filters for Logging

Using JAX-RS filters for logging is better than inserting `Logger.info()` calls in every resource method because filters implement **cross-cutting concerns** — logic that applies uniformly across all endpoints. With filters, logging is defined once and applies automatically to every request and response. Manual logging in each method is error-prone (easy to forget), creates code duplication, and makes the resource methods harder to read. Filters also allow the logging behaviour to be changed or disabled in one place without touching any resource code.

---

## Project Structure

```
src/main/java/com/smart/campus/api1/
├── SmartCampusApp.java          # JAX-RS Application config (@ApplicationPath)
├── DataStore.java               # Singleton in-memory data store
├── model/
│   ├── Room.java
│   ├── Sensor.java
│   └── SensorReading.java
├── resources/
│   ├── DiscoveryResource.java   # GET /api/v1
│   ├── RoomResource.java        # /api/v1/rooms
│   ├── SensorResource.java      # /api/v1/sensors
│   └── SensorReadingResource.java  # /api/v1/sensors/{id}/readings
├── exception/
│   ├── RoomNotEmptyException.java
│   ├── LinkedResourceNotFoundException.java
│   └── SensorUnavailableException.java
├── mapper/
│   ├── RoomNotEmptyExceptionMapper.java
│   ├── LinkedResourceNotFoundExceptionMapper.java
│   ├── SensorUnavailableExceptionMapper.java
│   └── GlobalExceptionMapper.java
└── filter/
    └── LoggingFilter.java
```
