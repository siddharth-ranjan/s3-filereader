# S3 File Loader Service

A Spring Boot-based service that reads CSV files from AWS S3, caches them in Redis, and provides an API to fetch records in chunks. It uses Redisson for distributed locking to ensure synchronized access to S3 resources.

## 🚀 Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 4.0.3
- **Package Manager:** Maven
- **Infrastructure:**
  - Redis (for caching and synchronization)
  - Redisson (distributed locking)
  - AWS SDK for S3 (v2.24.4)

## 📋 Requirements

- JDK 17 or higher
- Redis Server (running and accessible)
- AWS S3 Bucket access
- Maven (or use the provided `./mvnw`)

## 🛠 Project Structure

```text
loader/
├── src/main/java/com/filereader/loader/
│   ├── config/             # Configuration classes (Redisson)
│   ├── controller/         # REST Controllers (LoaderController)
│   ├── model/              # Data models (AccountRecord)
│   ├── redis/              # Redis caching logic (LoaderRedisService)
│   ├── s3Client/           # S3 client and reader (S3CsvReaderService)
│   └── LoaderApplication.java # Main application entry point
├── src/main/resources/
│   └── application.properties # Application configuration
├── src/test/java/          # Unit and integration tests
├── HELP.md                 # Spring Boot auto-generated help
├── mvnw                    # Maven wrapper script
└── pom.xml                 # Project dependencies and build config
```

## ⚙️ Configuration

The application can be configured via `src/main/resources/application.properties`.

### Key Properties

| Property | Description | Default/Example |
|----------|-------------|-----------------|
| `spring.data.redis.host` | Redis server host | `98.130.40.39` |
| `spring.data.redis.port` | Redis server port | `6379` |
| `aws.region` | AWS region for S3 | `ap-south-1` |
| `aws.s3.bucket` | S3 bucket name | `account-data-store` |
| `aws.s3.folder` | S3 folder path | `data` |
| `aws.accessKeyId` | AWS access key | `AKIA...` |
| `aws.secretAccessKey` | AWS secret key | `a6RU...` |

> **Note:** For production environments, it is recommended to use environment variables or a secure vault to store AWS credentials.

## 🏃 Setup & Run

### 1. Build the Application
```bash
./mvnw clean install
```

### 2. Run the Application
```bash
./mvnw spring-boot:run
```
By default, the application starts on port `8080`.

## 📖 API Usage

### Fetch Next Records
Retrieves records from a specified CSV file in S3.

**Endpoint:** `GET /getNext`

**Parameters:**
- `filename` (required): Name of the CSV file in the S3 folder.
- `count` (optional, default: 1): Number of records to fetch.

**Example Request:**
```bash
curl "http://localhost:8080/getNext?filename=accounts.csv&count=10"
```

**Response:**
- `200 OK`: A JSON list of `AccountRecord` objects.
- `200 OK` (Body: `EOF: No more records found.`): When the end of file is reached.
- `429 Too Many Requests`: If there is a lock acquisition timeout or other runtime issue.

## 🧪 Tests

To run the automated tests:
```bash
./mvnw test
```

## 📜 License

TODO: Specify the license (current `pom.xml` has an empty `<license>` tag).
