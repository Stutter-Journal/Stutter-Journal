package server

import (
	"context"
	"errors"
	"net/http"
	"strings"
	"time"

	"backend/ent"
	"backend/ent/doctorpatientlink"
	"backend/ent/patient"
	internal_errors "backend/internal/server/errors"

	"github.com/charmbracelet/log"
	"github.com/google/uuid"
)

type linkInviteRequest struct {
	PatientID    *string `json:"patientId,omitempty"`
	PatientEmail *string `json:"patientEmail,omitempty"`
	PatientCode  *string `json:"patientCode,omitempty"`
	DisplayName  *string `json:"displayName,omitempty"`
}

type linkApproveResponse struct {
	Link    linkDTO    `json:"link"`
	Patient patientDTO `json:"patient"`
}

type linkDTO struct {
	ID          string     `json:"id"`
	DoctorID    string     `json:"doctorId"`
	PatientID   string     `json:"patientId"`
	Status      string     `json:"status"`
	RequestedAt time.Time  `json:"requestedAt"`
	ApprovedAt  *time.Time `json:"approvedAt,omitempty"`
}

type patientDTO struct {
	ID          string  `json:"id"`
	DisplayName string  `json:"displayName"`
	Email       *string `json:"email,omitempty"`
	Code        *string `json:"patientCode,omitempty"`
}

type patientsListResponse struct {
	Patients     []patientDTO `json:"patients"`
	PendingLinks []linkDTO    `json:"pendingLinks"`
}

// inviteLinkHandler invites or creates a patient and establishes a pending link.
// @Summary Invite a patient to link with the doctor
// @Tags Links
// @Accept json
// @Produce json
// @Security SessionCookie
// @Param request body LinkInviteRequest true "Invite payload"
// @Success 201 {object} LinkResponse
// @Failure 400 {object} ErrorResponse
// @Failure 401 {object} ErrorResponse
// @Failure 409 {object} ErrorResponse
// @Router /links/invite [post]
func (s *Server) inviteLinkHandler(w http.ResponseWriter, r *http.Request) {
	doc, ok := currentDoctor(r.Context())
	if !ok {
		s.writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req linkInviteRequest
	if !s.decodeJSON(w, r, &req) {
		return
	}

	p, err := s.resolvePatient(r.Context(), req)
	if err != nil {
		// resolvePatient already returns user-safe errors in the right cases
		if errors.Is(err, internal_errors.ErrPatientNotFound) {
			s.writeError(w, http.StatusNotFound, err.Error())
			return
		}

		s.writeError(w, http.StatusBadRequest, err.Error())
		return
	}

	link, err := s.Db.Ent().DoctorPatientLink.
		Create().
		SetDoctorID(doc.ID).
		SetPatientID(p.ID).
		Save(r.Context())

	if ent.IsConstraintError(err) {
		s.writeError(w, http.StatusConflict, "link already exists")
		return
	} else if err != nil {
		log.Error("failed to create link", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not create link")
		return
	}

	s.writeJSON(w, http.StatusCreated, map[string]any{
		"link":    buildLinkDTO(link),
		"patient": buildPatientDTO(p),
	})
}

// requestLinkHandler proxies patient-side link request.
// @Summary Patient-side link request (placeholder)
// @Tags Links
// @Accept json
// @Produce json
// @Security SessionCookie
// @Param request body LinkInviteRequest true "Invite payload"
// @Success 201 {object} LinkResponse
// @Failure 400 {object} ErrorResponse
// @Failure 401 {object} ErrorResponse
// @Failure 409 {object} ErrorResponse
// @Router /links/request [post]
func (s *Server) requestLinkHandler(w http.ResponseWriter, r *http.Request) {
	// For now reuse invite semantics but keep endpoint available.
	s.inviteLinkHandler(w, r)
}

// approveLinkHandler approves a pending link by ID.
// @Summary Approve a pending doctor-patient link
// @Tags Links
// @Produce json
// @Security SessionCookie
// @Param id path string true "Link ID"
// @Success 200 {object} LinkApproveResponse
// @Failure 400 {object} ErrorResponse
// @Failure 401 {object} ErrorResponse
// @Failure 404 {object} ErrorResponse
// @Router /links/{id}/approve [post]
func (s *Server) approveLinkHandler(w http.ResponseWriter, r *http.Request) {
	doc, ok := currentDoctor(r.Context())
	if !ok {
		s.writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	linkID := strings.TrimPrefix(r.URL.Path, "/links/")
	linkID = strings.TrimSuffix(linkID, "/approve")
	if linkID == "" {
		s.writeError(w, http.StatusBadRequest, "link id is required")
		return
	}

	id, err := uuid.Parse(linkID)
	if err != nil {
		s.writeError(w, http.StatusBadRequest, "invalid link id")
		return
	}

	now := time.Now()
	link, err := s.Db.Ent().DoctorPatientLink.
		UpdateOneID(id).
		SetStatus(doctorpatientlink.StatusApproved).
		SetApprovedAt(now).
		SetApprovedByDoctorID(doc.ID).
		Save(r.Context())
	if ent.IsNotFound(err) {
		s.writeError(w, http.StatusNotFound, "link not found")
		return
	} else if err != nil {
		log.Error("failed to approve link", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not approve link")
		return
	}

	p, err := s.Db.Ent().Patient.Get(r.Context(), link.PatientID)
	if err != nil {
		log.Error("failed to load patient", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not load patient")
		return
	}

	s.writeJSON(w, http.StatusOK, linkApproveResponse{
		Link:    buildLinkDTO(link),
		Patient: buildPatientDTO(p),
	})
}

// listPatientsHandler returns linked and pending patients for the doctor.
// @Summary List patients and pending links for the current doctor
// @Tags Patients
// @Produce json
// @Security SessionCookie
// @Success 200 {object} PatientsResponse
// @Failure 401 {object} ErrorResponse
// @Router /patients [get]
func (s *Server) listPatientsHandler(w http.ResponseWriter, r *http.Request) {
	doc, ok := currentDoctor(r.Context())
	if !ok {
		s.writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	ctx := r.Context()

	approvedLinks, err := s.Db.Ent().DoctorPatientLink.Query().
		Where(
			doctorpatientlink.DoctorIDEQ(doc.ID),
			doctorpatientlink.StatusEQ(doctorpatientlink.StatusApproved),
		).
		WithPatient().
		All(ctx)
	if err != nil {
		log.Error("failed to list approved links", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not list patients")
		return
	}

	pendingLinks, err := s.Db.Ent().DoctorPatientLink.Query().
		Where(
			doctorpatientlink.DoctorIDEQ(doc.ID),
			doctorpatientlink.StatusEQ(doctorpatientlink.StatusPending),
		).
		WithPatient().
		All(ctx)
	if err != nil {
		log.Error("failed to list pending links", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not list patients")
		return
	}

	resp := patientsListResponse{
		Patients:     []patientDTO{},
		PendingLinks: []linkDTO{},
	}

	for _, l := range approvedLinks {
		resp.Patients = append(resp.Patients, buildPatientDTO(l.Edges.Patient))
	}
	for _, l := range pendingLinks {
		resp.PendingLinks = append(resp.PendingLinks, buildLinkDTO(l))
	}

	s.writeJSON(w, http.StatusOK, resp)
}

func (s *Server) resolvePatient(ctx context.Context, req linkInviteRequest) (*ent.Patient, error) {
	q := s.Db.Ent().Patient.Query()

	switch {
	case req.PatientID != nil && strings.TrimSpace(*req.PatientID) != "":
		id, err := uuid.Parse(strings.TrimSpace(*req.PatientID))
		if err != nil {
			return nil, errors.New("invalid patientId")
		}
		p, err := s.Db.Ent().Patient.Get(ctx, id)
		if ent.IsNotFound(err) {
			return nil, internal_errors.ErrPatientNotFound
		}
		return p, err

	case req.PatientEmail != nil && strings.TrimSpace(*req.PatientEmail) != "":
		email := strings.ToLower(strings.TrimSpace(*req.PatientEmail))
		p, err := q.Where(patient.EmailEQ(email)).Only(ctx)
		if ent.IsNotFound(err) {
			displayName := strings.TrimSpace(valOrDefault(req.DisplayName, ""))
			if displayName == "" {
				return nil, errors.New("displayName is required")
			}

			created, createErr := s.Db.Ent().Patient.
				Create().
				SetEmail(email).
				SetDisplayName(displayName).
				Save(ctx)
			if ent.IsConstraintError(createErr) {
				// Another request may have created it concurrently; fetch and continue.
				return q.Where(patient.EmailEQ(email)).Only(ctx)
			}
			return created, createErr
		}
		return p, err

	case req.PatientCode != nil && strings.TrimSpace(*req.PatientCode) != "":
		code := strings.TrimSpace(*req.PatientCode)
		p, err := q.Where(patient.PatientCodeEQ(code)).Only(ctx)
		if ent.IsNotFound(err) {
			displayName := strings.TrimSpace(valOrDefault(req.DisplayName, ""))
			if displayName == "" {
				return nil, errors.New("displayName is required")
			}

			created, createErr := s.Db.Ent().Patient.
				Create().
				SetPatientCode(code).
				SetDisplayName(displayName).
				Save(ctx)
			if ent.IsConstraintError(createErr) {
				return q.Where(patient.PatientCodeEQ(code)).Only(ctx)
			}
			return created, createErr
		}
		return p, err

	default:
		return nil, errors.New("provide patientId, patientEmail, or patientCode")
	}
}

func buildLinkDTO(l *ent.DoctorPatientLink) linkDTO {
	var approvedAt *time.Time
	if l.ApprovedAt != nil {
		approvedAt = l.ApprovedAt
	}
	return linkDTO{
		ID:          l.ID.String(),
		DoctorID:    l.DoctorID.String(),
		PatientID:   l.PatientID.String(),
		Status:      l.Status.String(),
		RequestedAt: l.RequestedAt,
		ApprovedAt:  approvedAt,
	}
}

func buildPatientDTO(p *ent.Patient) patientDTO {
	var email, code *string
	if p.Email != nil {
		val := strings.TrimSpace(*p.Email)
		email = &val
	}
	if p.PatientCode != nil {
		val := strings.TrimSpace(*p.PatientCode)
		code = &val
	}
	return patientDTO{
		ID:          p.ID.String(),
		DisplayName: p.DisplayName,
		Email:       email,
		Code:        code,
	}
}

func valOrDefault(val *string, fallback string) string {
	if val == nil {
		return fallback
	}
	if strings.TrimSpace(*val) == "" {
		return fallback
	}
	return *val
}
