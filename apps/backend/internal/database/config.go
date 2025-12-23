package database

import (
	"fmt"
	"os"
	"strings"
)

const (
	defaultSchema       = "public"
	defaultEnvironment  = "development"
	defaultPort         = "5432"
	defaultMigrationDir = "file://ent/migrate/migrations"
)

// Config captures the settings required to open the Ent client.
type Config struct {
	URL             string
	Host            string
	Port            string
	Username        string
	Password        string
	Database        string
	Schema          string
	Environment     string
	ApplyMigrations bool
	MigrationDir    string
}

// LoadConfig reads configuration from the Blueprint environment variables.
func LoadConfig() Config {
	return Config{
		URL:             firstNonEmpty(os.Getenv("BLUEPRINT_DATABASE_URL"), os.Getenv("DATABASE_URL")),
		Host:            strings.TrimSpace(os.Getenv("BLUEPRINT_DB_HOST")),
		Port:            strings.TrimSpace(os.Getenv("BLUEPRINT_DB_PORT")),
		Username:        firstNonEmpty(os.Getenv("BLUEPRINT_DB_USERNAME"), os.Getenv("BLUEPRINT_DB_USER")),
		Password:        strings.TrimSpace(os.Getenv("BLUEPRINT_DB_PASSWORD")),
		Database:        strings.TrimSpace(os.Getenv("BLUEPRINT_DB_DATABASE")),
		Schema:          defaultIfBlank(os.Getenv("BLUEPRINT_DB_SCHEMA"), defaultSchema),
		Environment:     defaultIfBlank(firstNonEmpty(os.Getenv("BLUEPRINT_ENV"), os.Getenv("ENVIRONMENT"), os.Getenv("APP_ENV")), defaultEnvironment),
		ApplyMigrations: boolFromEnv("BLUEPRINT_DB_APPLY_MIGRATIONS"),
		MigrationDir:    defaultIfBlank(os.Getenv("BLUEPRINT_DB_MIGRATION_DIR"), defaultMigrationDir),
	}
}

// DSN builds a PostgreSQL DSN from the configuration or returns an explicit URL if provided.
func (c Config) DSN() (string, error) {
	if c.URL != "" {
		return c.URL, nil
	}

	host := strings.TrimSpace(c.Host)
	port := defaultIfBlank(c.Port, defaultPort)
	user := strings.TrimSpace(c.Username)
	dbName := strings.TrimSpace(c.Database)
	schema := defaultIfBlank(c.Schema, defaultSchema)

	var missing []string
	if host == "" {
		missing = append(missing, "BLUEPRINT_DB_HOST")
	}
	if user == "" {
		missing = append(missing, "BLUEPRINT_DB_USERNAME")
	}
	if dbName == "" {
		missing = append(missing, "BLUEPRINT_DB_DATABASE")
	}
	if len(missing) > 0 {
		return "", fmt.Errorf("missing database configuration: %s", strings.Join(missing, ", "))
	}

	return fmt.Sprintf(
		"postgres://%s:%s@%s:%s/%s?sslmode=disable&search_path=%s",
		user, c.Password, host, port, dbName, schema,
	), nil
}

// ShouldApplyMigrations returns true when migrations should run automatically.
func (c Config) ShouldApplyMigrations() bool {
	return c.ApplyMigrations && strings.ToLower(c.Environment) != "production"
}

func boolFromEnv(key string) bool {
	val := strings.ToLower(strings.TrimSpace(os.Getenv(key)))
	return val == "1" || val == "true" || val == "yes" || val == "on"
}

func defaultIfBlank(value, fallback string) string {
	if strings.TrimSpace(value) == "" {
		return fallback
	}
	return strings.TrimSpace(value)
}

func firstNonEmpty(values ...string) string {
	for _, v := range values {
		if strings.TrimSpace(v) != "" {
			return strings.TrimSpace(v)
		}
	}
	return ""
}

// Validate ensures a DSN can be built.
func (c Config) Validate() error {
	if _, err := c.DSN(); err != nil {
		return err
	}
	return nil
}
