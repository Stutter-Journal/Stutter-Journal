package server

import (
	"context"
	"net/http"
	"time"

	"backend/ent"
	"backend/ent/doctorpatientlink"
	"backend/ent/entry"

	"github.com/charmbracelet/log"
	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

type entryDTO struct {
	ID               string    `json:"id"`
	PatientID        string    `json:"patientId"`
	HappenedAt       time.Time `json:"happenedAt"`
	Situation        *string   `json:"situation,omitempty"`
	Emotions         any       `json:"emotions,omitempty"`
	Triggers         any       `json:"triggers,omitempty"`
	Techniques       any       `json:"techniques,omitempty"`
	StutterFrequency *int      `json:"stutterFrequency,omitempty"`
	Notes            *string   `json:"notes,omitempty"`
	Tags             any       `json:"tags,omitempty"`
	CreatedAt        time.Time `json:"createdAt"`
	UpdatedAt        time.Time `json:"updatedAt"`
}

type entriesResponse struct {
	Entries []entryDTO `json:"entries"`
}

// patientEntriesHandler lists patient entries with optional time range filtering.
// @Summary List patient entries (doctor must have approved link)
// @Tags Entries
// @Produce json
// @Security SessionCookie
// @Param id path string true "Patient ID"
// @Param from query string false "ISO timestamp (RFC3339) lower bound"
// @Param to query string false "ISO timestamp (RFC3339) upper bound"
// @Success 200 {object} EntriesResponse
// @Failure 400 {object} ErrorResponse
// @Failure 401 {object} ErrorResponse
// @Failure 403 {object} ErrorResponse
// @Router /patients/{id}/entries [get]
func (s *Server) patientEntriesHandler(w http.ResponseWriter, r *http.Request) {
	doc, ok := currentDoctor(r.Context())
	if !ok {
		s.writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	patientIDParam := chi.URLParam(r, "id")
	patientID, err := uuid.Parse(patientIDParam)
	if err != nil {
		s.writeError(w, http.StatusBadRequest, "invalid patient id")
		return
	}

	if ok := s.hasApprovedLink(r.Context(), doc.ID, patientID); !ok {
		s.writeError(w, http.StatusForbidden, "no approved link for patient")
		return
	}

	from, to, err := parseTimeRange(r)
	if err != nil {
		s.writeError(w, http.StatusBadRequest, "invalid time range")
		return
	}

	q := s.Db.Ent().Entry.Query().
		Where(entry.PatientIDEQ(patientID)).
		Order(entry.ByHappenedAt())

	if from != nil {
		q = q.Where(entry.HappenedAtGTE(*from))
	}
	if to != nil {
		q = q.Where(entry.HappenedAtLTE(*to))
	}

	entries, err := q.All(r.Context())
	if err != nil {
		log.Error("failed to list entries", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not list entries")
		return
	}

	resp := entriesResponse{Entries: make([]entryDTO, 0, len(entries))}
	for _, e := range entries {
		resp.Entries = append(resp.Entries, mapEntryDTO(e))
	}

	s.writeJSON(w, http.StatusOK, resp)
}

func (s *Server) hasApprovedLink(ctx context.Context, doctorID, patientID uuid.UUID) bool {
	count, err := s.Db.Ent().DoctorPatientLink.
		Query().
		Where(
			doctorpatientlink.DoctorIDEQ(doctorID),
			doctorpatientlink.PatientIDEQ(patientID),
			doctorpatientlink.StatusEQ(doctorpatientlink.StatusApproved),
		).
		Count(ctx)
	if err != nil {
		log.Error("failed to check link", "err", err)
		return false
	}
	return count > 0
}

func mapEntryDTO(e *ent.Entry) entryDTO {
	return entryDTO{
		ID:               e.ID.String(),
		PatientID:        e.PatientID.String(),
		HappenedAt:       e.HappenedAt,
		Situation:        e.Situation,
		Emotions:         e.Emotions,
		Triggers:         e.Triggers,
		Techniques:       e.Techniques,
		StutterFrequency: e.StutterFrequency,
		Notes:            e.Notes,
		Tags:             e.Tags,
		CreatedAt:        e.CreatedAt,
		UpdatedAt:        e.UpdatedAt,
	}
}

func parseTimeRange(r *http.Request) (from *time.Time, to *time.Time, err error) {
	rawFrom := r.URL.Query().Get("from")
	rawTo := r.URL.Query().Get("to")

	if rawFrom != "" {
		t, e := time.Parse(time.RFC3339, rawFrom)
		if e != nil {
			return nil, nil, e
		}
		from = &t
	}
	if rawTo != "" {
		t, e := time.Parse(time.RFC3339, rawTo)
		if e != nil {
			return nil, nil, e
		}
		to = &t
	}
	return
}
