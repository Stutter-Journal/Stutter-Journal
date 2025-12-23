// backend/internal/server/bddtest/env.go
package bddtest

import (
	"context"
	"fmt"
	"io"
	"net/http"
	"net/http/httptest"
	"os"
	"path/filepath"
	"testing"
	"time"

	"backend/internal/database"

	"github.com/charmbracelet/log"
	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/modules/postgres"
	"github.com/testcontainers/testcontainers-go/wait"
)

type Env struct {
	BaseURL string
	DB      *database.Client

	container testcontainers.Container
	server    *httptest.Server
}

type HandlerFactory func(db *database.Client) (http.Handler, error)

func NewEnv(t *testing.T, newHandler HandlerFactory) *Env {
	t.Helper()

	ctx := context.Background()

	const (
		dbName = "authdb"
		dbUser = "authuser"
		dbPwd  = "authpass"
	)

	container, err := postgres.Run(
		ctx,
		"postgres:latest",
		postgres.WithDatabase(dbName),
		postgres.WithUsername(dbUser),
		postgres.WithPassword(dbPwd),
		testcontainers.WithWaitStrategy(
			wait.ForLog("database system is ready to accept connections").
				WithOccurrence(2).
				WithStartupTimeout(30*time.Second),
		),
	)
	if err != nil {
		t.Fatalf("start postgres container: %v", err)
	}

	host, err := container.Host(ctx)
	if err != nil {
		_ = container.Terminate(ctx)
		t.Fatalf("get container host: %v", err)
	}

	mappedPort, err := container.MappedPort(ctx, "5432/tcp")
	if err != nil {
		_ = container.Terminate(ctx)
		t.Fatalf("get container port: %v", err)
	}

	logger := log.NewWithOptions(io.Discard, log.Options{})

	migrationsDir := filepath.Join(projectRoot(), "ent", "migrate", "migrations")
	dbCfg := database.Config{
		Host:            host,
		Port:            mappedPort.Port(),
		Username:        dbUser,
		Password:        dbPwd,
		Database:        dbName,
		Schema:          "public",
		Environment:     "test",
		ApplyMigrations: true,
		MigrationDir:    fmt.Sprintf("file://%s", migrationsDir),
	}

	dbClient, err := database.NewWithConfig(ctx, dbCfg, logger)
	if err != nil {
		_ = container.Terminate(ctx)
		t.Fatalf("create database client: %v", err)
	}

	// optional helper: some suites want stable env vars
	_ = os.Setenv("ENVIRONMENT", "test")

	h, err := newHandler(dbClient)
	if err != nil {
		dbClient.Close()
		_ = container.Terminate(ctx)
		t.Fatalf("build handler: %v", err)
	}

	ts := httptest.NewServer(h)

	env := &Env{
		BaseURL:   ts.URL,
		DB:        dbClient,
		container: container,
		server:    ts,
	}

	t.Cleanup(func() {
		ts.Close()
		dbClient.Close()
		_ = container.Terminate(context.Background())
	})

	return env
}

func projectRoot() string {
	wd, err := os.Getwd()
	if err != nil {
		return "."
	}

	dir := wd
	for range 8 {
		if _, err := os.Stat(filepath.Join(dir, "go.mod")); err == nil {
			return dir
		}
		parent := filepath.Dir(dir)
		if parent == dir {
			break
		}
		dir = parent
	}
	return wd
}
