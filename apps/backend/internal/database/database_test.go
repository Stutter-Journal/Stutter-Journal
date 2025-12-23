package database

import (
	"context"
	"io"
	"strings"
	"testing"
	"time"

	"github.com/charmbracelet/log"
	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/modules/postgres"
	"github.com/testcontainers/testcontainers-go/wait"
)

func mustStartPostgresContainer(tb testing.TB) (Config, func()) {
	tb.Helper()

	var (
		dbName = "database"
		dbPwd  = "password"
		dbUser = "user"
	)

	dbContainer, err := postgres.Run(
		context.Background(),
		"postgres:latest",
		postgres.WithDatabase(dbName),
		postgres.WithUsername(dbUser),
		postgres.WithPassword(dbPwd),
		testcontainers.WithWaitStrategy(
			wait.ForLog("database system is ready to accept connections").
				WithOccurrence(2).
				WithStartupTimeout(10*time.Second)),
	)
	if err != nil {
		tb.Fatalf("could not start postgres container: %v", err)
	}

	dbHost, err := dbContainer.Host(context.Background())
	if err != nil {
		tb.Fatalf("could not get postgres host: %v", err)
	}

	dbPort, err := dbContainer.MappedPort(context.Background(), "5432/tcp")
	if err != nil {
		tb.Fatalf("could not get postgres port: %v", err)
	}

	cfg := Config{
		Host:     dbHost,
		Port:     dbPort.Port(),
		Username: dbUser,
		Password: dbPwd,
		Database: dbName,
		Schema:   "public",
	}

	return cfg, func() {
		if err := dbContainer.Terminate(context.Background()); err != nil {
			tb.Fatalf("could not teardown postgres container: %v", err)
		}
	}
}

func TestDSNFromConfig(t *testing.T) {
	cfg := Config{
		Host:     "localhost",
		Port:     "5432",
		Username: "user",
		Password: "pass",
		Database: "db",
		Schema:   "custom",
	}

	dsn, err := cfg.DSN()
	if err != nil {
		t.Fatalf("unexpected error building DSN: %v", err)
	}

	if !strings.Contains(dsn, "search_path=custom") {
		t.Fatalf("expected DSN to include schema, got %s", dsn)
	}
}

func TestDSNRespectsURL(t *testing.T) {
	cfg := Config{
		URL:      "postgres://example",
		Database: "ignored",
	}

	dsn, err := cfg.DSN()
	if err != nil {
		t.Fatalf("unexpected error building DSN: %v", err)
	}

	if dsn != cfg.URL {
		t.Fatalf("expected DSN to match provided URL, got %s", dsn)
	}
}

func TestShouldApplyMigrations(t *testing.T) {
	cfg := Config{ApplyMigrations: true, Environment: "production"}
	if cfg.ShouldApplyMigrations() {
		t.Fatalf("should not apply migrations in production")
	}

	cfg.Environment = "development"
	if !cfg.ShouldApplyMigrations() {
		t.Fatalf("should apply migrations outside production when enabled")
	}
}

func TestClientPing(t *testing.T) {
	cfg, cleanup := mustStartPostgresContainer(t)
	defer cleanup()

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	logger := log.NewWithOptions(io.Discard, log.Options{})
	client, err := NewWithConfig(ctx, cfg, logger)
	if err != nil {
		t.Fatalf("failed to create client: %v", err)
	}
	defer client.Close()

	if err := client.Ping(ctx); err != nil {
		t.Fatalf("expected ping to succeed, got %v", err)
	}
}
