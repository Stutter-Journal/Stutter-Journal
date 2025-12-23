# Project backend

One Paragraph of project description goes here

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

## MakeFile

Run build make command with tests
```bash
make all
```

Build the application
```bash
make build
```

Run the application
```bash
make run
```

Database migrations (uses Atlas):
```bash
make migrate
```
Set `BLUEPRINT_DB_APPLY_MIGRATIONS=true` in non-production environments if you want migrations to run automatically on startup. The helper script expects the `BLUEPRINT_DB_*` variables or `BLUEPRINT_DATABASE_URL` to be set.

Health endpoints:
- `GET /health` basic liveness check
- `GET /ready` pings the database connection
Create DB container
```bash
make docker-run
```

Shutdown DB Container
```bash
make docker-down
```

DB Integrations Test:
```bash
make itest
```

Live reload the application:
```bash
make watch
```

Run the test suite:
```bash
make test
```

Clean up binary from the last build:
```bash
make clean
```
