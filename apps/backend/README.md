# Eloquia Backend API

Go-based API service for the Stutter Journal platform. Uses PostgreSQL with Ent schemas and Atlas migrations.

## Prerequisites

- Go 1.25+
- Docker Desktop (for PostgreSQL)

## Quick Start

1. **Review environment variables**

	The backend reads database settings from `apps/backend/.env`.

2. **Start PostgreSQL**

```bash
make db-up
```

3. **Run migrations**

```bash
make migrate
```

4. **Run the API**

```bash
make run
```

The API starts on `http://localhost:8080`.

Health endpoints:

- `GET /health` basic liveness check
- `GET /ready` pings the database connection

## Common Make Targets

```bash
make all        # build + test
make build      # build binary
make run        # run API
make dev        # start db + run API
make migrate    # run Atlas migrations
make watch      # live reload (uses air)
make test       # unit tests
make itest      # integration tests
make bdd        # BDD tests (Docker required)
make clean      # remove build artifacts
```
