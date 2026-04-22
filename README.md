# Smart Campus Sensor & Room Management API

## API Design Overview

This project is a fully functional **RESTful API** developed for the **Smart Campus** initiative. The system provides a robust backend service for managing campus **Rooms**, the **Sensors** deployed within them (such as CO2 monitors, temperature sensors, and occupancy trackers), and a historical log of **Sensor Readings**.

The system is designed to serve campus facilities managers and automated building management systems through a clean, consistent, and standards-compliant RESTful interface.

## Technologies Used
 
| Technology | Purpose |
|---|---|
| Java 21 | Core programming language |
| JAX-RS — Jersey 2.41 | RESTful API framework |
| Grizzly HTTP Server | Lightweight embedded server |
| Jackson | JSON serialization and deserialization |
| Maven | Build and dependency management |

### Base URL
```
http://localhost:8080/api/v1
```
 
### Resource Hierarchy
The API follows a logical, hierarchical resource structure that mirrors the physical layout of the campus:
 
```
/api/v1                                    → Discovery (API metadata)
/api/v1/rooms                              → All rooms
/api/v1/rooms/{roomId}                     → A specific room
/api/v1/sensors                            → All sensors
/api/v1/sensors?type={type}                → Sensors filtered by type
/api/v1/sensors/{sensorId}                 → A specific sensor
/api/v1/sensors/{sensorId}/readings        → Readings for a specific sensor
```

### Key Design Decisions
 
- **HATEOAS**: The discovery endpoint returns navigable links to all primary resources, making the API self-documenting and easy to explore.
- **Resource Nesting**: Sensor readings are modelled as a sub-resource of sensors (`/sensors/{id}/readings`), reflecting the real-world ownership relationship.
- **Referential Integrity**: A sensor cannot be registered with a `roomId` that does not exist, and a room cannot be deleted while it still has sensors assigned.
- **Data Consistency**: When a new sensor reading is posted, the parent sensor's `currentValue` is automatically updated.
- **Safe Error Handling**: All exceptions are mapped to structured JSON responses — no raw Java stack traces are ever exposed to the client.

### HTTP Status Codes Used

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | Successful GET requests |
| 201 | Created | Successful POST (resource created) |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Missing or invalid request fields |
| 403 | Forbidden | Posting a reading to a MAINTENANCE sensor |
| 404 | Not Found | Resource does not exist |
| 409 | Conflict | Deleting a room that still contains sensors |
| 415 | Unsupported Media Type | Wrong Content-Type header |
| 422 | Unprocessable Entity | Valid JSON but referencing a non-existent room |
| 500 | Internal Server Error | Unexpected server-side failure (safely handled) |

---

## How to Build and Run
 
### Prerequisites

Before running the project, ensure you have the following installed:

| Tool | Version | Download |
|------|---------|----------|
| Java JDK | 21 (LTS) | [adoptium.net](https://adoptium.net/) |
| Apache Maven | 3.8+ | [maven.apache.org](https://maven.apache.org/) |
| Apache NetBeans | 21+ (recommended) | [netbeans.apache.org](https://netbeans.apache.org/) |

---

### Step 1 - Clone the Repository
 
Open your terminal or command prompt and run:
 
```bash
git clone https://github.com/yourusername/smart_campus.git
cd smart_campus
```
 
---
 
### Step 2 - Build the Project
 
Run the following Maven command to compile the source code and package the application:
 
```bash
mvn clean package
```
 
You should see `BUILD SUCCESS` at the end of the output.
 
---

### Step 3 - Launch the Server

Run the application using Maven's exec plugin:

```bash
mvn exec:java -Dexec.mainClass="com.smartcampus.Main"
```

Or if using **Apache NetBeans**:
1. Right-click the project → **Clean and Build**
2. Right-click `Main.java` → **Run File**

A successful startup displays:

```
----------------------------------------------
  Smart Campus API is running!
  Base URL : http://localhost:8080/api/v1/
  Press ENTER to stop the server...
----------------------------------------------
```

---

### Step 4 - Verify the Server is Running

Open a browser or use curl to test the discovery endpoint:

```bash
curl http://localhost:8080/api/v1/
```

Expected response:

```json
{
    "apiName": "Smart Campus Sensor & Room Management API",
    "version": "1.0",
    "description": "A RESTful API for managing campus rooms and IoT sensors",
    "contact": "admin@smartcampus.ac.uk",
    "basePath": "/api/v1",
    "resources": {
        "rooms": "/api/v1/rooms",
        "sensors": "/api/v1/sensors"
    }
}
```

---

### Step 5 - Stop the Server

Press **ENTER** in the terminal where the server is running to shut it down gracefully.

---

## Sample curl Commands

The following curl commands demonstrate successful interactions with different parts of the API. These can be run from any terminal on the same machine as the running server.

---

### 1. Discovery - Get API Metadata
```bash
curl -X GET http://localhost:8080/api/v1/ -H "Accept: application/json"
```
**Expected Response (200 OK):**
```json
{
  "apiName": "Smart Campus Sensor & Room Management API",
  "version": "1.0",
  "description": "A RESTful API for managing campus rooms and IoT sensors",
  "contact": "admin@smartcampus.ac.uk",
  "basePath": "/api/v1",
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```
 
---

### 2. Register a Sensor with roomId Validation (POST)

Registers a new sensor linked to an existing room. If the `roomId` does not exist, returns `422 Unprocessable Entity`.

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"HUM-002\", \"type\": \"Humidity\", \"status\": \"ACTIVE\", \"roomId\": \"LAB-201\"}"
```

**Expected Response:** `201 Created`
```json
{
    "id": "HUM-002",
    "type": "Humidity",
    "status": "ACTIVE",
    "currentValue": 0.0,
    "roomId": "LAB-201"
}
```

---

### 3. Filter Sensors by Type (GET with Query Parameter)

Retrieves all sensors matching a specific type using the `?type=` query parameter.

```bash
curl "http://localhost:8080/api/v1/sensors?type=Temperature"
```

**Expected Response:** `200 OK`
```json
[
    {
        "id": "TEMP-001",
        "type": "Temperature",
        "status": "ACTIVE",
        "currentValue": 22.5,
        "roomId": "1LE - GP"
    },
    {
        "id": "TEMP-002",
        "type": "Temperature",
        "status": "ACTIVE",
        "currentValue": 19.8,
        "roomId": "1st Floor - SP"
    }
]
```

---

### 4. Post a Sensor Reading and Update currentValue (POST sub-resource)

Adds a new reading to a sensor's history. As a side effect, the parent sensor's `currentValue` field is automatically updated to reflect the latest measurement.

```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\": 26.7}"
```

**Expected Response:** `201 Created`

Then verify the `currentValue` was updated:

```bash
curl http://localhost:8080/api/v1/sensors/TEMP-001
```

The response will show `"currentValue": 26.7`.

---

### 5. Attempt to Delete a Room Containing Sensors (409 Conflict)

Demonstrates the business logic constraint that prevents data orphaning — a room cannot be deleted while sensors are still assigned to it.

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/1LE%20-%20GP -v
```

**Expected Response:** `409 Conflict`
```json
{
    "status": 409,
    "error": "Conflict",
    "message": "Room '1LE - GP' cannot be deleted. It still has 2 active sensor(s) assigned: [TEMP-001, CO2-001]"
}
```

---

### 6. Post a Reading to a Sensor Under Maintenance (403 Forbidden)

Demonstrates exception handling — sensor `OCC-001` has status `MAINTENANCE`, so recording a new reading is blocked.

```bash
curl -X POST http://localhost:8080/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\": 15.0}"
```

**Expected Response:** `403 Forbidden`
```json
{
    "status": 403,
    "error": "Forbidden",
    "message": "Sensor 'OCC-001' is currently under maintenance and cannot accept new readings."
}
```

---

### 7. Error — Register Sensor With Invalid Room (422 Unprocessable Entity)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "CO2-999", "type": "CO2", "status": "ACTIVE", "currentValue": 400.0, "roomId": "INVALID-ROOM"}'
```
**Expected Response (422 Unprocessable Entity):**
```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Cannot register sensor 'CO2-999'. The referenced roomId 'INVALID-ROOM' does not exist in the system.",
  "hint": "Ensure the roomId exists before registering a sensor."
}
```
 
---

##  Conceptual Questions & Analysis

---

### Part 1.1 - JAX-RS Lifecycle & In-Memory Data Synchronization

**Question:** Explain the default lifecycle of a JAX-RS Resource class. Is a new instance
instantiated for every incoming request, or does the runtime treat it as a singleton?
Elaborate on how this architectural decision impacts the way you manage and synchronize
your in-memory data structures (maps/lists) to prevent data loss or race conditions.

**Answer:**

By default, JAX-RS follows a **per-request lifecycle** for resource classes. This means
the JAX-RS runtime instantiates a brand new instance of the resource class for every
incoming HTTP request, and once the response is delivered, that instance is immediately
discarded and becomes eligible for garbage collection. This behaviour is defined in the
JAX-RS specification and applies to all root resource classes unless explicitly overridden
using the `@Singleton` annotation.

This architectural decision has a direct and critical impact on how shared, in-memory data
must be managed. Since each resource instance is freshly created per request, any data
stored as an instance variable within the resource class would be re-initialised on every
call, resulting in complete data loss between requests. For example, if a `HashMap` storing
room data were declared as an instance variable inside `SensorRoomResource`, every new
request would begin with an empty map, making persistent state impossible.

To address this, the project implements the **Singleton design pattern** through a dedicated
`DataStore` class. This class maintains a single shared instance across the entire
application lifecycle using a `private static` variable and a `synchronized getInstance()`
method. All resource classes - regardless of how many instances are created per request -
always reference the same shared `LinkedHashMap` collections for rooms, sensors, and readings.

Furthermore, since JAX-RS processes multiple concurrent requests through multi-threading,
**race conditions** become a serious concern. A race condition occurs when two or more
threads simultaneously attempt to read and modify the same data structure, potentially
causing data corruption or inconsistent state. To prevent this, all data access methods
within `DataStore` are declared with the `synchronized` keyword. This ensures mutual
exclusion - only one thread can execute a given method at any point in time - effectively
serialising access to the shared collections and guaranteeing data integrity under
concurrent load.

---

### Part 1.2 - HATEOAS and Hypermedia-Driven API Design

**Question:** Why is the provision of Hypermedia (links and navigation within responses)
considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach
benefit client developers compared to static documentation?

**Answer:**

HATEOAS, which stands for **Hypermedia as the Engine of Application State**, is widely
regarded as the hallmark of advanced RESTful API design and represents the highest level
of the Richardson Maturity Model. It is a principle where the server embeds navigable
hyperlinks within every response, dynamically informing the client of what actions are
available and where related resources can be found - rather than requiring clients to
construct or memorise URLs independently.

In this project, the discovery endpoint at `GET /api/v1` implements this principle by
returning the locations of all primary resource collections directly within the response:

```json
{
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

This means a client application can begin at the discovery endpoint and navigate the
entire API purely through the links provided, without any hardcoded paths.

Compared to static documentation, HATEOAS offers significant advantages for client
developers. Static documentation becomes outdated the moment an API changes, forcing
developers to manually update their code to reflect new URL structures or resource paths.
This creates tight coupling between the client and server. With HATEOAS, the API
communicates its own structure dynamically through responses, meaning client applications
can adapt automatically to changes without breaking. This dramatically reduces maintenance
overhead, accelerates development, and makes the API genuinely self-documenting. Client
developers can explore and integrate the API with minimal prior knowledge, simply by
following the links provided in each response - much like navigating a website by clicking
links rather than memorising every URL.

---

### Part 2.1 - Returning IDs vs Full Room Objects

**Question:** When returning a list of rooms, what are the implications of returning only
IDs versus returning the full room objects? Consider network bandwidth and client side
processing.

**Answer:**

When designing a collection endpoint such as `GET /api/v1/rooms`, there are two common
approaches: returning only resource identifiers, or returning complete resource objects.
Each carries distinct trade-offs in terms of network bandwidth and client-side processing.

Returning **only IDs** minimises the size of the initial response, which may seem
beneficial in bandwidth-constrained environments. However, this forces the client to make
additional HTTP requests to retrieve full details for each room individually. For example,
if the system contains 50 rooms, the client would need to issue 50 separate
`GET /api/v1/rooms/{roomId}` requests to obtain all necessary data. This pattern is
commonly known as the **N+1 problem** and introduces significant latency, increases server
load, and complicates client-side logic considerably.

Returning **full room objects** in a single response - as implemented in this project -
allows clients to retrieve all necessary data in one network call. This eliminates the N+1
problem entirely, reduces round-trip latency, and simplifies the client implementation
significantly. The trade-off is a slightly larger response payload. However, for a campus
management system where room objects contain only a small number of fields, the additional
payload overhead is negligible compared to the performance and simplicity benefits gained.

Therefore, returning full room objects is the superior approach for this system. It
prioritises reduced network latency, lower server load, and simpler client logic - all of
which are critical qualities in a real-world, high-availability campus infrastructure API.

---

### Part 2.2 - DELETE Idempotency

**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed
justification by describing what happens if a client mistakenly sends the exact same DELETE
request for a room multiple times.

**Answer:**

In HTTP, an operation is considered **idempotent** if executing the same request multiple
times produces the same server state as executing it once, regardless of how many times it
is called. According to the HTTP specification, the DELETE method is formally defined as
idempotent.

In this project's implementation, the DELETE operation is **effectively idempotent**,
though the HTTP response code intentionally differs between the first and subsequent calls.
When a client sends `DELETE /api/v1/rooms/{roomId}` for the first time, the system first
verifies that the room exists and that no sensors are currently assigned to it. If both
conditions are satisfied, the room is permanently removed from the `DataStore` and the
server returns **HTTP 204 No Content**, confirming successful deletion.

If the identical DELETE request is sent a second time, the room no longer exists in the
system. The server returns **HTTP 404 Not Found** with a structured JSON error body.
Critically, the **server state remains identical** after both the first and all subsequent
requests - the room does not exist in either case and no unintended side effects have
occurred.

This behaviour fully satisfies the principle of idempotency because the end state of the
server is consistent across all repeated calls. The variation in response code between 204
and 404 is not a violation of idempotency - it is an accurate and expected reflection of
the current resource state. This design ensures predictable, safe behaviour for automated
building systems that may inadvertently send duplicate deletion requests.

---

### Part 3.1 - @Consumes and Content-Type Mismatch

**Question:** We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation on
the POST method. Explain the technical consequences if a client attempts to send data in
a different format, such as text/plain or application/xml. How does JAX-RS handle this
mismatch?

**Answer:**

The `@Consumes(MediaType.APPLICATION_JSON)` annotation explicitly declares that the POST
endpoint for sensor registration only accepts request bodies formatted as JSON, with the
`Content-Type: application/json` header. This annotation acts as a **content negotiation
contract** between the client and the server, enforced entirely by the JAX-RS runtime
before the request ever reaches the resource method.

When a client attempts to send a request with a mismatched `Content-Type` header - for
example `text/plain` or `application/xml` - the JAX-RS runtime intercepts the request at
the framework level and immediately rejects it without invoking the resource method at all.
The runtime automatically returns an **HTTP 415 Unsupported Media Type** response to the
client, indicating that the server is unable to process the request body in the format
provided.

This behaviour is significant for several reasons. Firstly, it protects the resource method
from receiving malformed or unparseable data, preventing potential `NullPointerException`
or deserialization errors deeper in the application logic. Secondly, it enforces a strict
and consistent data contract across the API, ensuring that all incoming sensor data is
properly structured JSON that Jackson can deserialize into a `Sensor` object. Thirdly, it
eliminates the need for manual content-type validation inside the resource method itself,
keeping the business logic clean and focused.

In this project, the `@Consumes` annotation on `POST /api/v1/sensors` ensures that only
well-formed JSON payloads containing valid `Sensor` fields are accepted, and any client
sending plain text or XML is immediately informed of the mismatch through the standardised
415 response - without any risk of corrupting the in-memory data store.

---

### Part 3.2 - @QueryParam vs @PathParam for Filtering

**Question:** You implemented this filtering using @QueryParam. Contrast this with an
alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2).
Why is the query parameter approach generally considered superior for filtering and
searching collections?

**Answer:**

In RESTful API design, both `@QueryParam` and `@PathParam` can technically be used to pass
filtering criteria to a resource endpoint. However, they serve fundamentally different
semantic purposes, and understanding this distinction is essential to designing a clean,
intuitive API.

**Path parameters** are intended to identify a specific, unique resource within the
hierarchy. For example, `/api/v1/sensors/{sensorId}` uses a path parameter because
`sensorId` uniquely identifies a single sensor resource. Using path parameters for
filtering - such as `/api/v1/sensors/type/CO2` - implies that `type/CO2` is itself a
distinct, addressable resource, which is semantically incorrect. It also creates URL
conflicts, as the JAX-RS runtime may struggle to differentiate between
`/api/v1/sensors/{sensorId}` and `/api/v1/sensors/type/CO2`, potentially causing routing
ambiguity and unexpected behaviour.

**Query parameters**, on the other hand, are specifically designed for optional refinement,
filtering, and searching of a collection. The URL `GET /api/v1/sensors?type=CO2` correctly
expresses the intent: "retrieve the sensors collection, filtered by type CO2." This
approach offers several clear advantages. Firstly, query parameters are entirely optional -
omitting the parameter returns the full collection, while including it applies the filter.
This is implemented in this project using `@QueryParam("type")`, where a null value returns
all sensors and a provided value filters by type. Secondly, multiple filters can be
combined naturally and intuitively, for example `?type=CO2&status=ACTIVE`, without
requiring additional path segments or route definitions. Thirdly, it keeps the URL
structure clean, logical, and consistent with established REST conventions, making the API
significantly easier to understand and use for client developers.

Therefore, the query parameter approach is semantically superior, more flexible, and better
aligned with RESTful design principles for collection filtering and searching.

---

### Part 4.1 - Sub-Resource Locator Pattern

**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How
does delegating logic to separate classes help manage complexity in large APIs compared to
defining every nested path in one massive controller class?

**Answer:**

The **Sub-Resource Locator pattern** is an advanced JAX-RS architectural technique where a
resource method does not directly handle a request but instead returns an instance of
another resource class that is responsible for processing the remainder of the URL path.
In this project, the `SensorResource` class implements this pattern through a dedicated
locator method:

```java
@Path("{sensorId}/readings")
public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId);
}
```

Rather than handling reading-related endpoints directly within `SensorResource`, the
runtime delegates all requests matching `/sensors/{sensorId}/readings` to a dedicated
`SensorReadingResource` class, which encapsulates all reading-specific logic independently.

This approach delivers significant architectural benefits, particularly as an API grows in
complexity. Firstly, it enforces the **Single Responsibility Principle** - each resource
class has a clearly defined and focused purpose. `SensorRoomResource` manages rooms,
`SensorResource` manages sensors, and `SensorReadingResource` exclusively manages reading
history. This separation makes each class significantly easier to understand, test, and
maintain in isolation.

Secondly, it dramatically improves **scalability of the codebase**. In a monolithic
controller approach, defining every nested path such as `/sensors/{id}/readings/{rid}`
within a single class would result in an increasingly large and unmanageable file as the
API grows. New developers joining the project would struggle to navigate such a class,
increasing the risk of introducing bugs. By contrast, the sub-resource locator pattern
allows new nested resources to be added as entirely separate classes without modifying
existing code, adhering to the **Open/Closed Principle**.

Thirdly, it enables **contextual state passing** between resource classes. In this project,
the `sensorId` is cleanly passed as a constructor argument to `SensorReadingResource`,
giving it the context it needs to retrieve and manage readings for the correct sensor -
without polluting the parent class with reading-specific logic.

Overall, the sub-resource locator pattern is a hallmark of professional, maintainable API
architecture that scales gracefully with increasing complexity.

---

### Part 5.2 - HTTP 422 vs HTTP 404 Semantics

**Question:** Why is HTTP 422 often considered more semantically accurate than a standard
404 when the issue is a missing reference inside a valid JSON payload?

**Answer:**

The distinction between HTTP 404 and HTTP 422 is a matter of **semantic precision** -
using the most accurate status code to communicate exactly what went wrong to the client
developer.

**HTTP 404 Not Found** is specifically designed to indicate that the requested URL or
endpoint does not exist on the server. For example, if a client requests
`GET /api/v1/rooms/INVALID-ID`, a 404 is entirely appropriate because the resource
identified by that URL cannot be located. The problem in this case is with the **URL
itself**.

However, when a client sends a `POST /api/v1/sensors` request with a valid URL and a
well-formed JSON body, but references a `roomId` that does not exist within the system,
the situation is fundamentally different. The endpoint `/api/v1/sensors` exists and is
fully operational. The JSON payload is syntactically correct and properly formatted. The
problem is not with the URL or the payload structure - it is with the **semantic validity
of the data contained within the payload**. Specifically, the `roomId` field references a
resource that cannot be resolved within the system.

This is precisely the scenario that **HTTP 422 Unprocessable Entity** was designed to
address. It communicates to the client that the request was received successfully, the URL
was valid, the JSON was well-formed, but the **business logic validation failed** due to
an unresolvable reference within the payload. This gives the client developer a much more
precise and actionable error message, clearly indicating that the problem lies within the
request body rather than the URL structure.

In this project, when a sensor is registered with a non-existent `roomId`, the
`LinkedResourceNotFoundException` is thrown and mapped to HTTP 422 by the
`LinkedResourceNotFoundExceptionMapper`, returning a structured JSON response that clearly
explains the validation failure and guides the client towards the correct resolution.

---

### Part 5.4 - Cybersecurity Risks of Exposing Stack Traces

**Question:** From a cybersecurity standpoint, explain the risks associated with exposing
internal Java stack traces to external API consumers. What specific information could an
attacker gather from such a trace?

**Answer:**

Exposing raw Java stack traces to external API consumers represents a serious **information
disclosure vulnerability** that can significantly compromise the security posture of a web
service. Stack traces are invaluable for internal debugging, but when exposed publicly they
provide attackers with a detailed roadmap of the application's internal architecture.

There are several categories of sensitive information that an attacker could extract from
an exposed stack trace. Firstly, stack traces reveal the **internal package and class
structure** of the application. For example, a trace showing
`com.smartcampus.store.DataStore.getRoom(DataStore.java:87)` immediately discloses the
project's package naming convention, class names, and even the exact line number where the
error occurred. This information allows an attacker to build an accurate mental model of
the codebase, making it significantly easier to identify and target specific components.

Secondly, stack traces expose the **technology stack and library versions** being used.
References to Jersey, Jackson, or Grizzly in a stack trace reveal exactly which frameworks
and versions are deployed. Attackers can then cross-reference these versions against
publicly known **CVE databases** to identify unpatched vulnerabilities that can be directly
exploited.

Thirdly, stack traces can inadvertently reveal **internal logic flaws and data flow**. The
sequence of method calls shown in a trace exposes how the application processes requests
internally, potentially highlighting weak points such as insufficient input validation or
unhandled edge cases that could be exploited through carefully crafted malicious requests.

To mitigate these risks, this project implements a **GlobalExceptionMapper** that intercepts
all unexpected `Throwable` instances before they reach the client. Instead of exposing the
raw stack trace, it returns a generic and safe **HTTP 500 Internal Server Error** response
with a clean JSON body, while logging the full technical details securely on the server
side for developer debugging only:

```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred. Please contact the system administrator."
}
```

This approach ensures the API remains completely **leak-proof** - protecting sensitive
implementation details from malicious actors while maintaining full observability for the
development team.

---
  
### Part 5.5 - JAX-RS Filters for Cross-Cutting Concerns

**Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like
logging, rather than manually inserting Logger.info() statements inside every single
resource method?

**Answer:**

A **cross-cutting concern** is a functionality that applies broadly across the entire
application, regardless of which specific resource or business logic is being executed.
Logging is a classic example - every single incoming request and outgoing response should
be recorded, regardless of whether it targets rooms, sensors, or readings.

Implementing logging through **JAX-RS filters** - as done in this project through
`LoggingFilter` which implements both `ContainerRequestFilter` and
`ContainerResponseFilter` - offers several compelling advantages over manually inserting
`Logger.info()` statements inside every resource method.

Firstly, it enforces the **DRY principle** (Don't Repeat Yourself). Manually inserting
logging statements into every resource method means duplicating the same logging logic
across `SensorRoomResource`, `SensorResource`, `SensorReadingResource`, and every
exception mapper. This creates significant code duplication that is difficult to maintain.
With a single filter class, logging logic is defined once and applied automatically and
consistently to every request and response across the entire API.

Secondly, it ensures **guaranteed coverage**. When logging is embedded manually in resource
methods, it is easy to accidentally omit it from a newly added method or forget to log
error scenarios. A JAX-RS filter, registered with the `@Provider` annotation, is invoked
automatically by the runtime for every single request and response without exception -
making it impossible to inadvertently miss logging any interaction.

Thirdly, it promotes **separation of concerns**. Resource methods should focus exclusively
on their business logic - managing rooms, registering sensors, recording readings.
Embedding logging statements within these methods pollutes the business logic with
infrastructure concerns, reducing readability and maintainability. Filters cleanly separate
these responsibilities, keeping resource classes focused and uncluttered.

Finally, filters can be **enabled, disabled, or modified** in one place without touching
any business logic code, making future changes to logging behaviour significantly simpler
to implement across the entire API.
