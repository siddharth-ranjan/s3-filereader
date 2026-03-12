# AGENTS.md

## Project Overview

Spring Boot 4.0.3 / Java 17 service that reads CSV files from AWS S3, caches rows in Redis, and serves them via a REST API in sequential chunks. Designed for concurrent access using Redisson distributed locks.

## Architecture & Data Flow

```
Client → LoaderController (GET /getNext) → LoaderRedisService → Redis cache
                                                ↓ (cache miss)
                                           S3CsvReaderService → AWS S3
```

1. **Controller** (`controller/LoaderController.java`) — Single endpoint `GET /getNext?filename=&count=`. Delegates to `LoaderRedisService`. Returns `AccountRecord` list, `"EOF"` string, or `429` on lock timeout.
2. **Redis Service** (`redis/LoaderRedisService.java`) — Core orchestration. Checks Redis list cache; on miss, acquires a Redisson lock, fetches `ceil(remaining / 20) * 20` rows from S3 in **one call**, pushes to Redis, and tracks position via a counter key.
3. **S3 Reader** (`s3Client/S3CsvReaderService.java`) — Stateless. Reads CSV rows starting at `startRow` by skipping lines. Also contains `S3Config` (package-private `@Configuration` class in same file) that builds the `S3Client` bean.
4. **Model** (`model/AccountRecord.java`) — Plain POJO with 3 fields: `accountNumber`, `accountType`, `status`. Parsed from CSV lines via `split(",", 3)`.

## S3 Fetch Strategy

The fetch size is **not** fixed at `CHUNK_SIZE` (20). When the cache is insufficient for a request:
- `fetchSize = ceil(remaining / CHUNK_SIZE) * CHUNK_SIZE` — e.g., request for 50 rows fetches 60 from S3 in a single call.
- The `while` loop re-checks only if S3 returned fewer rows than expected without reaching EOF.
- This avoids multiple round-trips to S3 for large requests.

## Key Redis Patterns

- **Cache key:** `cache:{filename}` — Redis list of raw CSV lines, consumed from the left.
- **Counter key:** `counter:{filename}` — Next S3 row offset. Resets to `"1"` at EOF (row 0 is header).
- **Lock key:** `lock:s3:{filename}` — Redisson distributed lock with 5s wait timeout.
- Cache fetch is **destructive**: `range` + `trim` removes returned rows. Do not read from cache keys externally.

## Build & Run

```bash
./mvnw clean install          # Build + test
./mvnw spring-boot:run        # Run on port 8080
./mvnw test                   # Tests only
```

Requires a running Redis instance and valid AWS credentials in `application.properties`.

## Conventions

- **No Lombok** — POJOs use explicit getters/setters and constructors.
- **Constructor injection** — All `@Service`/`@RestController` classes use constructor DI (no `@Autowired` fields).
- **`@Value` for config** — AWS and Redis properties injected directly via `@Value("${...}")`, not typed config classes.
- **S3Config is co-located** — `S3Client` bean defined in package-private `@Configuration` class inside `S3CsvReaderService.java`, not a separate file.
- **Flat exception handling** — Controller catches `RuntimeException` → `429`. No `@ControllerAdvice` or custom exceptions.
- **CSV parsing** — Inline in `LoaderRedisService.parseRecords()` via `split(",", 3)`. No CSV library.
- **RedissonConfig** — Lives in `config/RedissonConfig.java`, reads `spring.data.redis.host`/`port` via `@Value`.

## External Dependencies

| Dependency | Purpose |
|---|---|
| `spring-boot-starter-webmvc` | REST API |
| `spring-boot-starter-data-redis` | `StringRedisTemplate` for cache |
| `org.redisson:redisson:3.37.0` | Distributed locking (`RLock`) |
| `software.amazon.awssdk:s3:2.24.4` | S3 file reads |

## Gotchas

- AWS credentials are hardcoded in `application.properties` — do not commit real secrets.
- `S3CsvReaderService.readRowsFromS3` re-reads from the beginning on every call (skipping rows sequentially), so large offsets are expensive.
- Counter resets to `"1"` (not `"0"`) because row 0 is assumed to be a CSV header.
- The `while` loop in `getNextRecords` has an EOF-reset path: if the counter is already past EOF and nothing is cached, it resets counter to `"1"` and retries once.
