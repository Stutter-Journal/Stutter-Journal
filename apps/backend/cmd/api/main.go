package main

import (
	"context"
	"fmt"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"backend/internal/database"
	"backend/internal/server"
	"backend/internal/server/docs"

	"github.com/charmbracelet/lipgloss"
	log "github.com/charmbracelet/log"
)

func gracefulShutdown(apiServer *http.Server, db *database.Client, done chan bool) {
	// Create context that listens for the interrupt signal from the OS.
	ctx, stop := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer stop()

	// Listen for the interrupt signal.
	<-ctx.Done()

	log.Info("shutting down gracefully, press Ctrl+C again to force")
	stop() // Allow Ctrl+C to force shutdown

	// The context is used to inform the server it has 5 seconds to finish
	// the request it is currently handling
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	if err := apiServer.Shutdown(ctx); err != nil {
		log.Printf("Server forced to shutdown with error: %v", err)
	}

	if db != nil {
		if err := db.Close(); err != nil {
			log.Warn("failed closing database client", "err", err)
		}
	}

	log.Info("Server exiting")

	// Notify the main goroutine that the shutdown is complete
	done <- true
}

func main() {

	titleStyle := lipgloss.NewStyle().
		Bold(true).
		Foreground(lipgloss.Color("#FAFAFA")).
		Background(lipgloss.Color("#7D56F4")).
		PaddingTop(1).PaddingBottom(1).
		PaddingLeft(2).PaddingRight(2).
		Align(lipgloss.Center)

	subtitleStyle := lipgloss.NewStyle().
		Italic(true).
		Foreground(lipgloss.Color("#CCCCCC")).
		PaddingTop(0).PaddingBottom(1).
		Align(lipgloss.Center)

	title := titleStyle.Render("Eloquia Backend")
	subtitle := subtitleStyle.Render("A tiny TUI powered by Lip Gloss")

	fmt.Println(title)
	fmt.Println(subtitle)

	logger := log.NewWithOptions(os.Stdout, log.Options{ReportTimestamp: true, Level: log.DebugLevel})
	log.SetDefault(logger)

	ctx := context.Background()

	dbClient, err := database.New(ctx, logger)
	if err != nil {
		log.Fatalf("failed to connect to database: %v", err)
	}

	server := server.NewServer(dbClient)
	// Configure Swagger metadata served at /docs.
	docs.SwaggerInfo.Host = "localhost:8080"
	docs.SwaggerInfo.BasePath = "/"
	docs.SwaggerInfo.Schemes = []string{"http"}

	// Create a done channel to signal when the shutdown is complete
	done := make(chan bool, 1)

	// Run graceful shutdown in a separate goroutine
	go gracefulShutdown(server, dbClient, done)

	err = server.ListenAndServe()
	if err != nil && err != http.ErrServerClosed {
		panic(fmt.Sprintf("http server error: %s", err))
	}

	// Wait for the graceful shutdown to complete
	<-done
	log.Info("Graceful shutdown complete.")
}
