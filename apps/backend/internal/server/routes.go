package server

import (
	"encoding/json"
	"net/http"

	"github.com/charmbracelet/log"
	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
	"github.com/go-chi/cors"
)

func (s *Server) RegisterRoutes() http.Handler {
	r := chi.NewRouter()
	r.Use(middleware.Logger)

	r.Use(cors.Handler(cors.Options{
		AllowedOrigins:   []string{"https://*", "http://*"},
		AllowedMethods:   []string{"GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"},
		AllowedHeaders:   []string{"Accept", "Authorization", "Content-Type"},
		AllowCredentials: true,
		MaxAge:           300,
	}))

	r.Get("/", s.HelloWorldHandler)

	r.Get("/health", s.HealthHandler)
	r.Get("/ready", s.ReadyHandler)

	s.registerDocsRoutes(r)

	if s.Auth == nil {
		log.Warn("auth manager missing; authentication routes are disabled")
	} else {
		s.registerDoctorRoutes(r)
		s.registerPracticeRoutes(r)
	}

	return r
}

func (s *Server) HelloWorldHandler(w http.ResponseWriter, r *http.Request) {
	s.writeJSON(w, http.StatusOK, map[string]string{"message": "Hello World"})
}

func (s *Server) HealthHandler(w http.ResponseWriter, r *http.Request) {
	s.writeJSON(w, http.StatusOK, map[string]string{"status": "ok"})
}

func (s *Server) ReadyHandler(w http.ResponseWriter, r *http.Request) {
	if s.Db == nil {
		s.writeJSON(w, http.StatusServiceUnavailable, map[string]string{"status": "unavailable", "reason": "database not configured"})
		return
	}

	if err := s.Db.Ping(r.Context()); err != nil {
		log.Warn("database ping failed", "err", err)
		s.writeJSON(w, http.StatusServiceUnavailable, map[string]string{"status": "unavailable"})
		return
	}

	s.writeJSON(w, http.StatusOK, map[string]string{"status": "ready"})
}

func (s *Server) writeJSON(w http.ResponseWriter, status int, payload any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)

	if err := json.NewEncoder(w).Encode(payload); err != nil {
		log.Error("failed to encode response", "err", err)
	}
}

func (s *Server) writeError(w http.ResponseWriter, status int, message string) {
	s.writeJSON(w, status, map[string]string{"error": message})
}

func (s *Server) registerDoctorRoutes(r chi.Router) {
	r.Route("/doctor", func(r chi.Router) {
		r.Post("/register", s.doctorRegisterHandler)
		r.Post("/login", s.doctorLoginHandler)

		r.Group(func(r chi.Router) {
			r.Use(s.requireDoctor)
			r.Get("/me", s.doctorMeHandler)
			r.Post("/logout", s.doctorLogoutHandler)
		})
	})
}

func (s *Server) registerPracticeRoutes(r chi.Router) {
	r.Group(func(r chi.Router) {
		r.Use(s.requireDoctor)
		r.Post("/practice", s.practiceCreateHandler)
	})
}
