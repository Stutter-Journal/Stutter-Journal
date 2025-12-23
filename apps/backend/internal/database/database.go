package database

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"strings"
	"time"

	"backend/ent"

	"entgo.io/ent/dialect"
	entsql "entgo.io/ent/dialect/sql"
	"github.com/charmbracelet/log"
	_ "github.com/jackc/pgx/v5/stdlib"
	_ "github.com/joho/godotenv/autoload"
)

// Client wraps the Ent client and provides helpers for readiness checks.
type Client struct {
	cfg    Config
	ent    *ent.Client
	sqlDB  *sql.DB
	logger *log.Logger
}

// New opens an Ent client using configuration loaded from the environment.
// It optionally runs migrations (only in non-production environments) when requested.
func New(ctx context.Context, logger *log.Logger) (*Client, error) {
	return NewWithConfig(ctx, LoadConfig(), logger)
}

// NewWithConfig opens an Ent client using the provided config.
func NewWithConfig(ctx context.Context, cfg Config, logger *log.Logger) (*Client, error) {
	if logger == nil {
		logger = log.Default()
	}

	dsn, err := cfg.DSN()
	if err != nil {
		return nil, err
	}

	sqlDB, err := sql.Open("pgx", dsn)
	if err != nil {
		return nil, fmt.Errorf("open postgres connection: %w", err)
	}

	sqlDB.SetConnMaxIdleTime(5 * time.Minute)
	sqlDB.SetConnMaxLifetime(time.Hour)
	sqlDB.SetMaxIdleConns(5)
	sqlDB.SetMaxOpenConns(10)

	pingCtx, cancel := context.WithTimeout(ctx, 5*time.Second)
	defer cancel()
	if err := sqlDB.PingContext(pingCtx); err != nil {
		_ = sqlDB.Close()
		return nil, fmt.Errorf("ping database: %w", err)
	}

	drv := entsql.OpenDB(dialect.Postgres, sqlDB)
	entClient := ent.NewClient(ent.Driver(drv))

	client := &Client{
		cfg:    cfg,
		ent:    entClient,
		sqlDB:  sqlDB,
		logger: logger,
	}

	if cfg.ApplyMigrations && strings.ToLower(cfg.Environment) == "production" {
		logger.Warn("skipping automatic migrations in production environment")
	}

	if cfg.ShouldApplyMigrations() {
		logger.Infof("applying migrations from %s", cfg.MigrationDir)
		if err := client.applyMigrations(ctx, dsn); err != nil {
			_ = client.Close()
			return nil, err
		}
	}

	logger.Infof("connected to %s on %s:%s (schema %s)", cfg.Database, cfg.Host, cfg.Port, cfg.Schema)
	return client, nil
}

// Ent exposes the underlying ent.Client.
func (c *Client) Ent() *ent.Client {
	return c.ent
}

// Ping verifies that the SQL connection is still alive.
func (c *Client) Ping(ctx context.Context) error {
	pingCtx, cancel := context.WithTimeout(ctx, 2*time.Second)
	defer cancel()
	return c.sqlDB.PingContext(pingCtx)
}

// Close closes the Ent and SQL connections.
func (c *Client) Close() error {
	var errs []error
	if c.ent != nil {
		if err := c.ent.Close(); err != nil {
			errs = append(errs, err)
		}
	}
	if c.sqlDB != nil {
		if err := c.sqlDB.Close(); err != nil {
			errs = append(errs, err)
		}
	}
	if len(errs) > 0 {
		return errors.Join(errs...)
	}
	return nil
}
