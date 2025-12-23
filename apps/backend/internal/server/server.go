package server

import (
	"context"
	"fmt"
	"net/http"
	"os"
	"strconv"
	"time"

	"backend/ent"
	"backend/internal/auth"
	_ "github.com/joho/godotenv/autoload"

	"github.com/charmbracelet/log"
)

const defaultPort = 8080

type Database interface {
	Ping(ctx context.Context) error
	Ent() *ent.Client
}

type Server struct {
	Port int

	Db   Database
	Auth *auth.Manager
}

func NewServer(db Database) *http.Server {
	port := parsePort()
	if db == nil {
		log.Warn("server started without a database client; /ready will fail")
	}

	authCfg, err := auth.LoadConfig(log.Default())
	if err != nil {
		log.Fatalf("auth configuration error: %v", err)
	}

	authManager, err := auth.NewManager(authCfg)
	if err != nil {
		log.Fatalf("failed to initialize auth manager: %v", err)
	}

	s := &Server{
		Port: port,
		Db:   db,
		Auth: authManager,
	}

	server := &http.Server{
		Addr:         fmt.Sprintf(":%d", s.Port),
		Handler:      s.RegisterRoutes(),
		IdleTimeout:  time.Minute,
		ReadTimeout:  10 * time.Second,
		WriteTimeout: 30 * time.Second,
	}

	log.Infof("Server listening on :%d", s.Port)

	return server
}

func parsePort() int {
	portVal := os.Getenv("PORT")
	if portVal == "" {
		return defaultPort
	}

	port, err := strconv.Atoi(portVal)
	if err != nil {
		log.Warnf("invalid PORT value %q, falling back to %d", portVal, defaultPort)
		return defaultPort
	}
	return port
}
