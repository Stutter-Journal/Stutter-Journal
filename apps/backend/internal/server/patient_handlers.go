package server

import (
	"context"
	"errors"
	"net/http"
	"net/mail"
	"strings"

	"backend/ent"
	"backend/ent/doctorpatientlink"
	"backend/ent/patient"
	"backend/internal/auth"

	"github.com/charmbracelet/log"
	"github.com/google/uuid"
)

type patientContextKey struct{}

type patientRegisterRequest struct {
	Email       string `json:"email"`
	DisplayName string `json:"displayName"`
	Password    string `json:"password"`
}

type patientLoginRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type patientResponse struct {
	ID          string `json:"id"`
	Email       string `json:"email"`
	DisplayName string `json:"displayName"`
	Status      string `json:"status"`
}

type myDoctorPracticeResponse struct {
	Name    string  `json:"name"`
	Address *string `json:"address,omitempty"`
}

type myDoctorResponse struct {
	Email       string                   `json:"email"`
	DisplayName string                   `json:"displayName"`
	Practice    myDoctorPracticeResponse `json:"myDoctorPractice"`
}

// myDoctorHandler retrieves the patients' assigned therapist
// @Summary Retrieve the patients' assigned therapist
// @Tags Patient
// @Accept json
// @Produce json
// @Security SessionCookie
// @Success 200 {object} myDoctorResponse
// @Failure 401 {object} ErrorResponse
// @Failure 404 {object} ErrorResponse
// @Router /patient/mydoctor [get]
func (s *Server) myDoctorHandler(w http.ResponseWriter, r *http.Request) {
	if !s.ensureAuthReady(w) {
		return
	}

	p, ok := currentPatient(r.Context())
	if !ok {
		s.writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	link, err := s.Db.Ent().DoctorPatientLink.
		Query().
		Where(
			doctorpatientlink.PatientIDEQ(p.ID),
			doctorpatientlink.StatusEQ(doctorpatientlink.StatusApproved),
		).
		WithDoctor(func(q *ent.DoctorQuery) {
			q.WithPractice()
		}).
		Order(ent.Desc(doctorpatientlink.FieldApprovedAt)).
		First(r.Context())
	if ent.IsNotFound(err) {
		s.writeError(w, http.StatusNotFound, "no doctor assigned")
		return
	}
	if err != nil {
		log.Error("failed to query patient doctor link", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not retrieve doctor information")
		return
	}

	doc := link.Edges.Doctor
	if doc == nil {
		log.Error("doctor link missing doctor edge", "link_id", link.ID)
		s.writeError(w, http.StatusInternalServerError, "could not retrieve doctor information")
		return
	}

	// Build the response
	practice := myDoctorPracticeResponse{}
	if doc.Edges.Practice != nil {
		practice = myDoctorPracticeResponse{
			Name:    doc.Edges.Practice.Name,
			Address: doc.Edges.Practice.Address,
		}
	}

	response := myDoctorResponse{
		Email:       doc.Email,
		DisplayName: doc.DisplayName,
		Practice:    practice,
	}

	s.writeJSON(w, http.StatusOK, map[string]any{
		"doctor": response,
	})
}

// patientRegisterHandler registers a new patient account.
// @Summary Register a patient account
// @Tags Patient
// @Accept json
// @Produce json
// @Param request body PatientRegisterRequest true "Patient registration payload"
// @Success 201 {object} PatientResponse
// @Failure 400 {object} ErrorResponse
// @Failure 409 {object} ErrorResponse
// @Router /patient/register [post]
func (s *Server) patientRegisterHandler(w http.ResponseWriter, r *http.Request) {
	if !s.ensureAuthReady(w) {
		return
	}

	var req patientRegisterRequest
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

	p, err := s.Db.Ent().Patient.
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
		log.Error("failed to create patient", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not create account")
		return
	}

	if err := s.Auth.IssuePatientSession(w, p.ID); err != nil {
		log.Error("failed to set session cookie", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not create session")
		return
	}

	s.writeJSON(w, http.StatusCreated, map[string]any{
		"patient": buildPatientResponse(p),
	})
}

// patientLoginHandler authenticates a patient and issues a session cookie.
// @Summary Authenticate a patient
// @Tags Patient
// @Accept json
// @Produce json
// @Param request body PatientLoginRequest true "Patient login payload"
// @Success 200 {object} PatientResponse
// @Failure 400 {object} ErrorResponse
// @Failure 401 {object} ErrorResponse
// @Router /patient/login [post]
func (s *Server) patientLoginHandler(w http.ResponseWriter, r *http.Request) {
	if !s.ensureAuthReady(w) {
		return
	}

	var req patientLoginRequest
	if !s.decodeJSON(w, r, &req) {
		return
	}

	req.Email = strings.ToLower(strings.TrimSpace(req.Email))
	if req.Email == "" || req.Password == "" {
		s.writeError(w, http.StatusBadRequest, "email and password are required")
		return
	}

	p, err := s.Db.Ent().Patient.Query().
		Where(patient.EmailEQ(req.Email)).
		Only(r.Context())
	if err != nil {
		if ent.IsNotFound(err) {
			s.writeError(w, http.StatusUnauthorized, "invalid email or password")
			return
		}
		log.Error("failed to query patient", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not check credentials")
		return
	}

	if p.PasswordHash == nil || strings.TrimSpace(*p.PasswordHash) == "" {
		s.writeError(w, http.StatusUnauthorized, "invalid email or password")
		return
	}

	if err := s.Auth.VerifyPassword(*p.PasswordHash, req.Password); err != nil {
		s.writeError(w, http.StatusUnauthorized, "invalid email or password")
		return
	}

	if err := s.Auth.IssuePatientSession(w, p.ID); err != nil {
		log.Error("failed to set session cookie", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not create session")
		return
	}

	s.writeJSON(w, http.StatusOK, map[string]any{
		"patient": buildPatientResponse(p),
	})
}

// patientMeHandler returns the current patient profile.
// @Summary Fetch the current patient
// @Tags Patient
// @Produce json
// @Security SessionCookie
// @Success 200 {object} PatientResponse
// @Failure 401 {object} ErrorResponse
// @Router /patient/me [get]
func (s *Server) patientMeHandler(w http.ResponseWriter, r *http.Request) {
	p, ok := currentPatient(r.Context())
	if !ok {
		s.writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	s.writeJSON(w, http.StatusOK, map[string]any{
		"patient": buildPatientResponse(p),
	})
}

// patientLogoutHandler clears the session cookie.
// @Summary Terminate the current patient session
// @Tags Patient
// @Produce json
// @Security SessionCookie
// @Success 200 {object} StatusResponse
// @Failure 401 {object} ErrorResponse
// @Router /patient/logout [post]
func (s *Server) patientLogoutHandler(w http.ResponseWriter, r *http.Request) {
	s.Auth.ClearSession(w)
	s.writeJSON(w, http.StatusOK, map[string]string{"status": "logged out"})
}

func (s *Server) requirePatient(next http.Handler) http.Handler {
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

		// Ensure we don't accept doctor sessions for patient-only routes.
		if claims.PatientID == uuid.Nil {
			s.writeError(w, http.StatusUnauthorized, "unauthorized")
			return
		}

		p, err := s.Db.Ent().Patient.Get(r.Context(), claims.PatientID)
		if err != nil {
			if ent.IsNotFound(err) {
				s.Auth.ClearSession(w)
				s.writeError(w, http.StatusUnauthorized, "unauthorized")
				return
			}
			log.Error("failed to load patient for session", "err", err)
			s.writeError(w, http.StatusInternalServerError, "could not load account")
			return
		}

		ctx := context.WithValue(r.Context(), patientContextKey{}, p)
		next.ServeHTTP(w, r.WithContext(ctx))
	})
}

func buildPatientResponse(p *ent.Patient) patientResponse {
	email := ""
	if p.Email != nil {
		email = strings.TrimSpace(*p.Email)
	}
	return patientResponse{
		ID:          p.ID.String(),
		Email:       email,
		DisplayName: p.DisplayName,
		Status:      p.Status.String(),
	}
}

func currentPatient(ctx context.Context) (*ent.Patient, bool) {
	val := ctx.Value(patientContextKey{})
	if val == nil {
		return nil, false
	}
	p, ok := val.(*ent.Patient)
	return p, ok
}
