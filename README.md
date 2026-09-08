# Modular Monolith Spring Boot Project

This project shows a modular monolith structure in Spring Boot using Maven modules:

- `shared`: cross-cutting reusable types and contracts
- `auth`: authentication and user management module
- `orders`: order management module
- `app`: Spring Boot application bootstrap and configuration

## Run locally

```bash
mvn clean install
mvn -pl app spring-boot:run
```

## Available endpoints

### Auth
- `POST /api/auth/register` — registers a new user (email + password). Passwords are stored hashed.
- `POST /api/auth/login` — returns access + refresh tokens on successful authentication. Use the access token in the Authorization header: `Authorization: Bearer <token>`
- `POST /api/auth/refresh` — rotates the refresh token and returns a fresh token pair
- `POST /api/auth/logout` — revokes a refresh token and logs the user out
- `GET /api/auth/admin/health` — admin-only endpoint, available only for users with the `ROLE_ADMIN` authority

### Orders
- `POST /api/orders` — requires Authorization bearer token and either `ROLE_USER` or `ROLE_ADMIN`
- `GET /api/orders` — requires Authorization bearer token and either `ROLE_USER` or `ROLE_ADMIN`

### Default admin user

The app boots an admin account automatically:

- Email: `admin@example.com`
- Password: `Admin@123`

## API docs

Swagger UI is available at:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

Use the `Authorize` button in Swagger and paste the JWT bearer token to test the protected endpoints.

## Docker

Build and run the app with Postgres:

```bash
docker compose up --build
```

The compose files include health checks for both the database and the app. The app waits for Postgres to report healthy before starting, and the application exposes `/actuator/health` for readiness/liveness checks.

Then access:

- App: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Postgres: `localhost:5432`
- Health endpoint: `http://localhost:8080/actuator/health`

The compose stack uses:

- username: `postgres`
- password: `postgres`
- database: `modular_monolith`

## Production profile

A production profile is included at:

- `app/src/main/resources/application-prod.yml`

This profile uses Flyway for schema migration and disables Hibernate schema auto-generation. The app expects the database schema to already exist or be created by migration scripts under:

- `app/src/main/resources/db/migration`

Run it with:

```bash
SPRING_PROFILES_ACTIVE=prod \
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/modular_monolith \
SPRING_DATASOURCE_USERNAME=postgres \
SPRING_DATASOURCE_PASSWORD=postgres \
JWT_SECRET='<base64-encoded-256bit-secret>' \
java -jar app/target/app-1.0.0.jar
```

## Database

The app uses an embedded H2 database with the console enabled at:

- `http://localhost:8080/h2-console`

Use these credentials:

- JDBC URL: `jdbc:h2:mem:modular-monolith;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- Username: `sa`
- Password: `password`
