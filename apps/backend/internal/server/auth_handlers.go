package server

import (
	"context"
	"encoding/json"
	"errors"
	"net/http"
	"net/mail"
	"strings"

	"backend/ent"
	"backend/ent/doctor"
	"backend/internal/auth"

	"github.com/charmbracelet/log"
)

type doctorContextKey struct{}

type doctorRegisterRequest struct {
	Email       string `json:"email"`
	DisplayName string `json:"displayName"`
	Password    string `json:"password"`
}

type doctorLoginRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type doctorResponse struct {
	ID          string  `json:"id"`
	Email       string  `json:"email"`
	DisplayName string  `json:"displayName"`
	Role        string  `json:"role"`
	PracticeID  *string `json:"practiceId,omitempty"`
}

func (s *Server) doctorRegisterHandler(w http.ResponseWriter, r *http.Request) {
	if !s.ensureAuthReady(w) {
		return
	}

	var req doctorRegisterRequest
	if !s.decodeJSON(w, r, &req) {
		return
	}

	req.Email = strings.ToLower(strings.TrimSpace(req.Email))
	req.DisplayName = strings.TrimSpace(req.DisplayName)

	if req.Email == "" || req.DisplayName == "" || req.Password == "" {
		s.writeError(w, http.StatusBadRequest, "email, displayName, and password are required")
		return
	}

	if _, err := mail.ParseAddress(req.Email); err != nil {
		s.writeError(w, http.StatusBadRequest, "email is invalid")
		return
	}

	hash, err := s.Auth.HashPassword(req.Password)
	if err != nil {
		s.writeError(w, http.StatusBadRequest, err.Error())
		return
	}

	doc, err := s.Db.Ent().Doctor.
		Create().
		SetEmail(req.Email).
		SetDisplayName(req.DisplayName).
		SetPasswordHash(hash).
		Save(r.Context())
	if err != nil {
		if ent.IsConstraintError(err) {
			s.writeError(w, http.StatusConflict, "an account with that email already exists")
			return
		}
		log.Error("failed to create doctor", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not create account")
		return
	}

	if err := s.Auth.IssueSession(w, doc.ID); err != nil {
		log.Error("failed to set session cookie", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not create session")
		return
	}

	s.writeJSON(w, http.StatusCreated, map[string]any{
		"doctor": buildDoctorResponse(doc),
	})
}

func (s *Server) doctorLoginHandler(w http.ResponseWriter, r *http.Request) {
	if !s.ensureAuthReady(w) {
		return
	}

	var req doctorLoginRequest
	if !s.decodeJSON(w, r, &req) {
		return
	}

	req.Email = strings.ToLower(strings.TrimSpace(req.Email))
	if req.Email == "" || req.Password == "" {
		s.writeError(w, http.StatusBadRequest, "email and password are required")
		return
	}

	doc, err := s.Db.Ent().Doctor.Query().
		Where(doctor.EmailEQ(req.Email)).
		Only(r.Context())
	if err != nil {
		if ent.IsNotFound(err) {
			s.writeError(w, http.StatusUnauthorized, "invalid email or password")
			return
		}
		log.Error("failed to query doctor", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not check credentials")
		return
	}

	if err := s.Auth.VerifyPassword(doc.PasswordHash, req.Password); err != nil {
		s.writeError(w, http.StatusUnauthorized, "invalid email or password")
		return
	}

	if err := s.Auth.IssueSession(w, doc.ID); err != nil {
		log.Error("failed to set session cookie", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not create session")
		return
	}

	s.writeJSON(w, http.StatusOK, map[string]any{
		"doctor": buildDoctorResponse(doc),
	})
}

func (s *Server) doctorMeHandler(w http.ResponseWriter, r *http.Request) {
	doctor, ok := currentDoctor(r.Context())
	if !ok {
		s.writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	s.writeJSON(w, http.StatusOK, map[string]any{
		"doctor": buildDoctorResponse(doctor),
	})
}

func (s *Server) doctorLogoutHandler(w http.ResponseWriter, r *http.Request) {
	s.Auth.ClearSession(w)
	s.writeJSON(w, http.StatusOK, map[string]string{"status": "logged out"})
}

func (s *Server) requireDoctor(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if !s.ensureAuthReady(w) {
			return
		}

		claims, err := s.Auth.ReadSession(r)
		if err != nil {
			if errors.Is(err, auth.ErrExpiredSession) || errors.Is(err, auth.ErrInvalidSession) {
				s.Auth.ClearSession(w)
			}
			s.writeError(w, http.StatusUnauthorized, "unauthorized")
			return
		}

		doc, err := s.Db.Ent().Doctor.Get(r.Context(), claims.DoctorID)
		if err != nil {
			if ent.IsNotFound(err) {
				s.Auth.ClearSession(w)
				s.writeError(w, http.StatusUnauthorized, "unauthorized")
				return
			}
			log.Error("failed to load doctor for session", "err", err)
			s.writeError(w, http.StatusInternalServerError, "could not load account")
			return
		}

		ctx := context.WithValue(r.Context(), doctorContextKey{}, doc)
		next.ServeHTTP(w, r.WithContext(ctx))
	})
}

func (s *Server) ensureAuthReady(w http.ResponseWriter) bool {
	if s.Auth == nil || s.Db == nil || s.Db.Ent() == nil {
		s.writeError(w, http.StatusInternalServerError, "authentication not configured")
		return false
	}
	return true
}

func buildDoctorResponse(doc *ent.Doctor) doctorResponse {
	var practiceID *string
	if doc.PracticeID != nil {
		id := doc.PracticeID.String()
		practiceID = &id
	}

	return doctorResponse{
		ID:          doc.ID.String(),
		Email:       doc.Email,
		DisplayName: doc.DisplayName,
		Role:        doc.Role.String(),
		PracticeID:  practiceID,
	}
}

func currentDoctor(ctx context.Context) (*ent.Doctor, bool) {
	val := ctx.Value(doctorContextKey{})
	if val == nil {
		return nil, false
	}
	doc, ok := val.(*ent.Doctor)
	return doc, ok
}

func (s *Server) decodeJSON(w http.ResponseWriter, r *http.Request, dst any) bool {
	r.Body = http.MaxBytesReader(w, r.Body, 1<<20) // 1MB limit
	dec := json.NewDecoder(r.Body)
	dec.DisallowUnknownFields()
	if err := dec.Decode(dst); err != nil {
		s.writeError(w, http.StatusBadRequest, "invalid JSON payload")
		return false
	}
	return true
}
