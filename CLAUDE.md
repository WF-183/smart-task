# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

smart-task is a Spring Boot-based microservice application with Spring Cloud integration. The project consists of two modules:
- `smart-task-api`: Empty API module for interface definitions
- `smart-task-provider`: Main implementation module containing scheduled jobs and utilities

The application includes FTP file operations and UC Cloud bookmark export functionality.

## Technology Stack

- Java 8
- Spring Boot 2.2.0.RELEASE
- Spring Cloud Hoxton.RELEASE
- Maven (multi-module project)
- Commons Net (FTP operations)
- OkHttp3 (HTTP client)
- Lombok

## Build and Run

### Building the Project

```bash
# Build entire project from root
mvn clean install

# Build specific module
cd smart-task-provider
mvn clean install

# Build with specific profile (local is default)
mvn clean install -P local
mvn clean install -P dev
mvn clean install -P pre
mvn clean install -P prod
```

### Running the Application

```bash
# Run from smart-task-provider directory
cd smart-task-provider
mvn spring-boot:run

# Or run with specific profile
mvn spring-boot:run -P dev
```

The application runs on port **20051** by default.

### Testing

```bash
# Run tests (currently skipped by default: maven.test.skip=true)
mvn test

# Run tests with code coverage
mvn clean test jacoco:report
```

Coverage reports exclude `**/config/**` and `**/model/**` packages.

## Project Architecture

### Module Structure

- **smart-task (root)**: Parent POM managing dependencies and common configuration
  - **smart-task-api**: Empty module intended for API interfaces/contracts
  - **smart-task-provider**: Main application module with business logic

### Key Components

**Main Application**: `com.code.maker.SmartTaskApplication`
- Entry point with `@SpringBootApplication`
- Scheduling enabled via `@EnableScheduling`
- Feign clients enabled for `com.code.maker` packages
- Database auto-configuration is excluded (commented out MyBatis/MySQL dependencies)
- Health check endpoint at `/` and `/health`

**Scheduled Jobs**: `com.code.maker.job.FtpJob`
- Executes daily at 14:00 (2 PM)
- Performs FTP file operations (rename and move)

**Utilities**:
- `com.code.maker.utils.FtpUtils`: FTP file upload/download, rename, and move operations
- `com.code.maker.utils.UcCloudBookmarkExportUtil`: UC Cloud bookmark export tool

### Configuration

Environment-specific properties are in `smart-task-provider/src/main/filters/`:
- `local.properties` (default)
- `dev.properties`
- `pre.properties`
- `prod.properties`

Application configuration in `smart-task-provider/src/main/resources/`:
- `bootstrap.yml`: Minimal Spring application name configuration
- `application.yml`: Main configuration (server port, Eureka, Feign, Hystrix, datasource)

### Important Notes

1. **Database**: MyBatis and database dependencies are commented out. DataSourceAutoConfiguration is excluded.

2. **Service Discovery**: Eureka client is configured but registration is disabled (`register-with-eureka: false`)

3. **FTP Configuration**: FTP credentials are hardcoded in `FtpJob` and `FtpUtils`. Production deployments should externalize these.

4. **Profiles**: Use Maven profiles to switch between environments during build. Properties are filtered into resources at build time.

5. **Logging**: Uses Log4j2 with SLF4J. Configuration in `logback-spring.xml`.

## Development Workflow

When adding new scheduled jobs:
1. Create class in `com.code.maker.job` package
2. Annotate with `@Component` and `@Slf4j`
3. Use `@Scheduled` annotation with cron expression
4. Job will be automatically picked up due to component scanning

When adding new utilities:
1. Create class in `com.code.maker.utils` package
2. Use static methods for stateless operations
3. Consider connection pooling for FTP operations

When modifying configuration:
1. Update filter properties files for environment-specific values
2. Update `application.yml` for common configuration
3. Rebuild to apply filtered properties: `mvn clean install`
