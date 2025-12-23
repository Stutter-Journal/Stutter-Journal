package database

import (
	"context"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"time"
)

func (c *Client) applyMigrations(ctx context.Context, dsn string) error {
	migrateCtx, cancel := context.WithTimeout(ctx, 2*time.Minute)
	defer cancel()

	if err := c.runScript(migrateCtx, dsn); err == nil {
		return nil
	} else if c.logger != nil {
		c.logger.Debug("migration script unavailable, falling back to atlas", "reason", err)
	}

	atlasPath, err := exec.LookPath("atlas")
	if err != nil {
		return fmt.Errorf("atlas binary not found in PATH: %w (install atlas or run make migrate)", err)
	}

	cmd := exec.CommandContext(
		migrateCtx,
		atlasPath,
		"migrate",
		"apply",
		"--dir", c.cfg.MigrationDir,
		"--url", dsn,
	)
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	cmd.Env = append(
		os.Environ(),
		fmt.Sprintf("BLUEPRINT_DATABASE_URL=%s", dsn),
		fmt.Sprintf("BLUEPRINT_DB_MIGRATION_DIR=%s", c.cfg.MigrationDir),
	)

	if err := cmd.Run(); err != nil {
		return fmt.Errorf("apply migrations: %w", err)
	}
	return nil
}

func (c *Client) runScript(ctx context.Context, dsn string) error {
	scriptPath := filepath.Join(".", "scripts", "migrate-dev.sh")
	if info, err := os.Stat(scriptPath); err != nil || info.IsDir() {
		return fmt.Errorf("migration script not found")
	}

	cmd := exec.CommandContext(ctx, scriptPath)
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	cmd.Env = append(
		os.Environ(),
		fmt.Sprintf("BLUEPRINT_DATABASE_URL=%s", dsn),
		fmt.Sprintf("BLUEPRINT_DB_MIGRATION_DIR=%s", c.cfg.MigrationDir),
	)

	return cmd.Run()
}
