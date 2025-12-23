package server

import (
	"context"
	"fmt"
	"net/http"
	"os"
	"strconv"
	"time"

	_ "github.com/joho/godotenv/autoload"

	"github.com/charmbracelet/log"
)

const defaultPort = 8080

type ReadyChecker interface {
	Ping(ctx context.Context) error
}

type Server struct {
	port int

	db ReadyChecker
}

func NewServer(db ReadyChecker) *http.Server {
	port := parsePort()
	if db == nil {
		log.Warn("server started without a database client; /ready will fail")
	}

	s := &Server{
		port: port,
		db:   db,
	}

	server := &http.Server{
		Addr:         fmt.Sprintf(":%d", s.port),
		Handler:      s.RegisterRoutes(),
		IdleTimeout:  time.Minute,
		ReadTimeout:  10 * time.Second,
		WriteTimeout: 30 * time.Second,
	}

	log.Infof("Server listening on :%d", s.port)

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
