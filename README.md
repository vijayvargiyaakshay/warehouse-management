# Fulfilment Monolith

A small [Quarkus](https://quarkus.io/) service that manages **Warehouses**, **Stores**, and
**Products** for a fulfilment domain. It demonstrates a clean (hexagonal) design for the Warehouse
feature, an OpenAPI-generated REST layer, transactional integration with a legacy system, and a
test suite with enforced code coverage.

## Requirements

- **JDK 17+** — make sure `JAVA_HOME` points at a 17+ JDK and `java` is on the `PATH`.
- **Docker** — used automatically by Quarkus Dev Services to start a throwaway PostgreSQL
  database for `dev` and `test`. (You can run your own PostgreSQL instead; see
  [Running in JVM mode](#running-in-jvm-mode).)

No local Maven install is needed — the project ships with the Maven Wrapper (`./mvnw`).

## Running

Start the app in live-coding (dev) mode:

```sh
./mvnw quarkus:dev
```

The API is served at <http://localhost:8080> and a landing page at
<http://localhost:8080/index.html>. Code and even JPA entity changes hot-reload on save. Quarkus
starts a PostgreSQL container for you and seeds it from `src/main/resources/import.sql`.

### Running in JVM mode

```sh
# start a PostgreSQL matching src/main/resources/application.properties (%prod)
docker run -it --rm --name quarkus_test \
  -e POSTGRES_USER=quarkus_test -e POSTGRES_PASSWORD=quarkus_test -e POSTGRES_DB=quarkus_test \
  -p 15432:5432 postgres:13.3

./mvnw package
java -jar ./target/quarkus-app/quarkus-run.jar
```

## Testing & coverage

Run the full build, tests, and coverage gate:

```sh
./mvnw clean verify
```

This runs unit tests (JUnit 5 + Mockito) and `@QuarkusTest` endpoint tests (RestAssured, against a
Dev Services PostgreSQL), then produces a **JaCoCo** report and **fails the build if line coverage
drops below 80%**.

Open the HTML coverage report after the build:

```sh
open target/site/jacoco/index.html      # macOS
xdg-open target/site/jacoco/index.html  # Linux
```

Generated OpenAPI code and trivial model/entity classes are excluded from the coverage figure.

## Endpoints

Base URL: `http://localhost:8080`

### Warehouse (`/warehouse`) — OpenAPI-generated interface

| Method | Path                                      | Description                        | Success |
|--------|-------------------------------------------|------------------------------------|---------|
| GET    | `/warehouse`                              | List active warehouses             | 200     |
| POST   | `/warehouse`                              | Create a warehouse                 | 201     |
| GET    | `/warehouse/{id}`                         | Get an active warehouse by id      | 200     |
| DELETE | `/warehouse/{id}`                         | Archive a warehouse (never deletes)| 204     |
| POST   | `/warehouse/{businessUnitCode}/replacement` | Replace the active warehouse     | 200     |

```sh
# List
curl http://localhost:8080/warehouse

# Create (201)
curl -X POST http://localhost:8080/warehouse \
  -H 'Content-Type: application/json' \
  -d '{"businessUnitCode":"MWH.500","location":"AMSTERDAM-002","capacity":50,"stock":20}'

# Get by id (200, or 404 if missing/archived)
curl http://localhost:8080/warehouse/1

# Archive by id (204)
curl -X DELETE http://localhost:8080/warehouse/1

# Replace the active warehouse for a business unit code (200)
curl -X POST http://localhost:8080/warehouse/MWH.500/replacement \
  -H 'Content-Type: application/json' \
  -d '{"businessUnitCode":"MWH.500","location":"AMSTERDAM-002","capacity":60,"stock":20}'
```

Errors return a JSON body `{"code": <status>, "error": "<message>"}`:
**400** validation error, **404** not found, **409** duplicate business unit code.

### Store (`/store`)

| Method | Path          | Description        | Success |
|--------|---------------|--------------------|---------|
| GET    | `/store`      | List stores        | 200     |
| GET    | `/store/{id}` | Get a store by id  | 200     |
| POST   | `/store`      | Create a store     | 201     |
| PUT    | `/store/{id}` | Replace a store    | 200     |
| PATCH  | `/store/{id}` | Update a store     | 200     |
| DELETE | `/store/{id}` | Delete a store     | 204     |

```sh
curl -X POST http://localhost:8080/store \
  -H 'Content-Type: application/json' \
  -d '{"name":"WOODSTORE","quantityProductsInStock":7}'
```

### Product (`/product`)

| Method | Path            | Description         | Success |
|--------|-----------------|---------------------|---------|
| GET    | `/product`      | List products       | 200     |
| GET    | `/product/{id}` | Get a product by id | 200     |
| POST   | `/product`      | Create a product    | 201     |
| PUT    | `/product/{id}` | Update a product    | 200     |
| DELETE | `/product/{id}` | Delete a product    | 204     |

```sh
curl -X POST http://localhost:8080/product \
  -H 'Content-Type: application/json' \
  -d '{"name":"CHAIR","stock":4}'
```

## Validation rules

**Creating a warehouse** (`POST /warehouse`):

- `businessUnitCode` must be present and must **not** already belong to an active warehouse → `409`.
- `location` must be a known location → `400`.
- The number of active warehouses at that location must be **below** the location's
  `maxNumberOfWarehouses` → `400`.
- `capacity` must be `> 0`, and the **sum of active capacities** at the location (including this
  one) must not exceed the location's `maxCapacity` → `400`.
- `stock` must be `<= capacity` → `400`.

**Replacing a warehouse** (`POST /warehouse/{businessUnitCode}/replacement`):

- An **active** warehouse with that business unit code must exist → `404`.
- New `capacity` must be `> 0` and `>=` the replaced warehouse's stock (accommodate its stock) → `400`.
- New `stock` must **equal** the replaced warehouse's stock → `400`.
- On success the old warehouse is **archived** and the new one is created under the same business
  unit code.

**Archiving** sets `archivedAt` and never deletes the row; archived warehouses are excluded from
listings and lookups.

## Architecture

The Warehouse feature follows a **hexagonal (ports & adapters)** layout so business rules stay free
of framework and persistence concerns:

```
warehouses/
├── domain/
│   ├── models/      Warehouse, Location            (plain domain objects)
│   ├── ports/       WarehouseStore, LocationResolver,
│   │                Create/Replace/ArchiveWarehouseOperation   (interfaces)
│   ├── usecases/    Create/Replace/ArchiveWarehouseUseCase     (business rules)
│   └── exceptions/  WarehouseException + Validation/NotFound/Duplicate
└── adapters/
    ├── database/    WarehouseRepository (implements WarehouseStore via Panache), DbWarehouse
    └── restapi/     WarehouseResourceImpl (implements the generated WarehouseResource),
                     WarehouseExceptionMapper, WarehouseResponseStatusFilter
```

- **Use cases** depend only on **ports** (interfaces). Adapters provide the implementations:
  `WarehouseRepository` for persistence, `LocationGateway` for location lookup. This makes the use
  cases unit-testable with Mockito and no database.
- The **REST layer is generated** from `src/main/resources/openapi/warehouse-openapi.yaml`
  (quarkus-openapi-generator); `WarehouseResourceImpl` implements that interface. Because the
  generated `create` method returns the `Warehouse` bean (serialized as 200), a small
  `@ServerResponseFilter` (`WarehouseResponseStatusFilter`) upgrades that one response to **201**.
- **Domain errors** extend `WarehouseException` (carrying an HTTP status) and are translated to JSON
  by `WarehouseExceptionMapper` (400 / 404 / 409).
- **Legacy sync after commit:** `StoreResource` fires a CDI `StoreChangedEvent` *inside* the
  transaction; `StoreLegacySyncObserver` observes it with
  `@Observes(during = TransactionPhase.AFTER_SUCCESS)` and only then calls
  `LegacyStoreManagerGateway`. This guarantees the legacy system is notified **only after** the
  change is committed — if the transaction rolls back, nothing is sent.
- **Store** and **Product** use a simpler active-record / direct-repository style (Panache), kept as
  a deliberate contrast to the Warehouse hexagonal design.

The domain generation uses `drop-and-create` with `import.sql` seed data for dev/test.
```
