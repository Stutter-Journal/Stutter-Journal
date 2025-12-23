# Simple Makefile for a Go project

# 1. Load .env file
ifneq (,$(wildcard ./.env))
    include .env
    export
endif

# Build the application
all: build test

build:
	@echo "Building..."
	@go build -o main cmd/api/main.go

# Run the application
run:
	@go run cmd/api/main.go

# Create DB container
docker-run:
	@if docker compose up --build 2>/dev/null; then \
		: ; \
	else \
		echo "Falling back to Docker Compose V1"; \
		docker-compose up --build; \
	fi

# Shutdown DB container
docker-down:
	@if docker compose down 2>/dev/null; then \
		: ; \
	else \
		echo "Falling back to Docker Compose V1"; \
		docker-compose down; \
	fi

# Start DB container in background
db-up:
	@echo "Starting database container..."
	@if docker compose up -d 2>/dev/null; then \
		: ; \
	else \
		echo "Falling back to Docker Compose V1"; \
		docker-compose up -d; \
	fi

# Development mode
dev: db-up run

# Run Atlas Migrations
migrate:
	@echo "Running migrations..."
	@# Ensure variables are set to avoid empty connection string errors
	@if [ -z "${BLUEPRINT_DB_USER}" ] || [ -z "${BLUEPRINT_DB_DATABASE}" ]; then \
		echo "Error: Environment variables (DB_USER, DB_NAME, etc.) not found in .env"; \
		exit 1; \
	fi
	@atlas migrate apply \
		--url "postgres://${BLUEPRINT_DB_USERNAME}:${BLUEPRINT_DB_PASSWORD}@${BLUEPRINT_DB_HOST}:${BLUEPRINT_DB_PORT}/${BLUEPRINT_DB_DATABASE}?sslmode=disable" \
		--dir "file://migrations"

# Test the application
test:
	@echo "Testing..."
	@go test ./... -v

# Integrations Tests
itest:
	@echo "Running integration tests..."
	@go test ./internal/database -v

# Clean the binary
clean:
	@echo "Cleaning..."
	@rm -f main

# Live Reload
watch:
	@if command -v air > /dev/null; then \
            air; \
            echo "Watching...";\
        else \
            read -p "Go's 'air' is not installed on your machine. Do you want to install it? [Y/n] " choice; \
            if [ "$$choice" != "n" ] && [ "$$choice" != "N" ]; then \
                go install github.com/air-verse/air@latest; \
                air; \
                echo "Watching...";\
            else \
                echo "You chose not to install air. Exiting..."; \
                exit 1; \
            fi; \
        fi

.PHONY: all build run test clean watch docker-run docker-down db-up dev migrate itest
