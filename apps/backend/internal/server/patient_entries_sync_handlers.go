package server

import (
	"encoding/json"
	"io"
	"net/http"
	"time"

	"backend/ent"
	"backend/ent/entry"

	"entgo.io/ent/dialect/sql"
	"github.com/charmbracelet/log"
	"github.com/google/uuid"
)

type patientEntriesSyncRequest struct {
	// UpdatedSince is an RFC3339 timestamp. If set, only entries updated after this time are returned.
	UpdatedSince string `json:"updatedSince,omitempty"`

	// From/To optionally filter the happened_at timestamp (RFC3339).
	From string `json:"from,omitempty"`
	To   string `json:"to,omitempty"`

	// Entries is an optional upload payload (phone -> server). When present, the server will upsert entries.
	Entries []entrySyncDTO `json:"entries,omitempty"`
}

type entrySyncDTO struct {
	ID         string    `json:"id"`
	CreatedAt  time.Time `json:"createdAt"`
	HappenedAt time.Time `json:"happenedAt"`
	Notes      string    `json:"notes"`
	Tags       []string  `json:"tags"`
	UpdatedAt  time.Time `json:"updatedAt"`
}

type entriesSyncResponse struct {
	Entries []entrySyncDTO `json:"entries"`
}

// patientEntriesSyncHandler returns the authenticated patient's entries for offline sync.
//
// Currently this behaves as a "download" endpoint and supports optional incremental sync:
// - query: updatedSince, from, to (RFC3339)
// - body (optional JSON): { updatedSince, from, to }
func (s *Server) patientEntriesSyncHandler(w http.ResponseWriter, r *http.Request) {
	log := log.With(
		"handler", "patientEntriesSync",
		"method", r.Method,
		"path", r.URL.Path,
		"remote", r.RemoteAddr,
	)

	log.Info("request started")

	// ---------------------------------------------------------------------
	// Auth / patient
	// ---------------------------------------------------------------------
	p, ok := currentPatient(r.Context())
	if !ok {
		log.Warn("unauthorized request")
		s.writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}
	log.Info("authenticated patient", "patient_id", p.ID)

	// ---------------------------------------------------------------------
	// Decode body (optional)
	// ---------------------------------------------------------------------
	req := patientEntriesSyncRequest{}
	if r.Body != nil {
		defer func() {
			_ = r.Body.Close()
			log.Debug("request body closed")
		}()

		dec := json.NewDecoder(r.Body)
		if err := dec.Decode(&req); err != nil && err != io.EOF {
			log.Warn("failed to decode json body", "err", err)
			s.writeError(w, http.StatusBadRequest, "invalid json")
			return
		}

		log.Debug("decoded request body",
			"updatedSince", req.UpdatedSince,
			"from", req.From,
			"to", req.To,
			"entries", len(req.Entries),
		)
	}

	// ---------------------------------------------------------------------
	// Upload (optional): phone is the source of truth (one-way)
	// ---------------------------------------------------------------------
	uploadMode := len(req.Entries) > 0
	if uploadMode {
		log.Info("uploading entries", "count", len(req.Entries))
		for i, incoming := range req.Entries {
			if incoming.ID == "" {
				log.Warn("entry missing id", "index", i)
				s.writeError(w, http.StatusBadRequest, "entry missing id")
				return
			}

			id, err := uuid.Parse(incoming.ID)
			if err != nil {
				log.Warn("invalid entry id", "index", i, "id", incoming.ID, "err", err)
				s.writeError(w, http.StatusBadRequest, "invalid entry id")
				return
			}

			if incoming.HappenedAt.IsZero() {
				log.Warn("entry missing happenedAt", "index", i, "id", incoming.ID)
				s.writeError(w, http.StatusBadRequest, "entry missing happenedAt")
				return
			}

			existing, err := s.Db.Ent().Entry.Get(r.Context(), id)
			if err != nil {
				if !ent.IsNotFound(err) {
					log.Error("failed to load entry", "index", i, "id", incoming.ID, "err", err)
					s.writeError(w, http.StatusInternalServerError, "could not sync entries")
					return
				}

				create := s.Db.Ent().Entry.Create().
					SetID(id).
					SetPatientID(p.ID).
					SetHappenedAt(incoming.HappenedAt)

				// createdAt is immutable after creation; accept it if client provided.
				if !incoming.CreatedAt.IsZero() {
					create.SetCreatedAt(incoming.CreatedAt)
				}
				if !incoming.UpdatedAt.IsZero() {
					create.SetUpdatedAt(incoming.UpdatedAt)
				}
				if incoming.Notes != "" {
					create.SetNotes(incoming.Notes)
				}
				if incoming.Tags != nil {
					create.SetTags(incoming.Tags)
				}

				if _, err := create.Save(r.Context()); err != nil {
					log.Error("failed to create entry", "index", i, "id", incoming.ID, "err", err)
					s.writeError(w, http.StatusInternalServerError, "could not sync entries")
					return
				}
				continue
			}

			if existing.PatientID != p.ID {
				log.Warn("entry patient mismatch", "index", i, "id", incoming.ID, "entry_patient_id", existing.PatientID, "session_patient_id", p.ID)
				s.writeError(w, http.StatusForbidden, "entry does not belong to patient")
				return
			}

			update := s.Db.Ent().Entry.UpdateOneID(id).
				SetHappenedAt(incoming.HappenedAt)
			if !incoming.UpdatedAt.IsZero() {
				update.SetUpdatedAt(incoming.UpdatedAt)
			}
			if incoming.Notes == "" {
				update.ClearNotes()
			} else {
				update.SetNotes(incoming.Notes)
			}
			if incoming.Tags != nil {
				update.SetTags(incoming.Tags)
			}

			if _, err := update.Save(r.Context()); err != nil {
				log.Error("failed to update entry", "index", i, "id", incoming.ID, "err", err)
				s.writeError(w, http.StatusInternalServerError, "could not sync entries")
				return
			}
		}

	}

	// ---------------------------------------------------------------------
	// Resolve parameters (query > body)
	// ---------------------------------------------------------------------
	updatedSinceRaw := firstNonEmpty(r.URL.Query().Get("updatedSince"), req.UpdatedSince)
	fromRaw := firstNonEmpty(r.URL.Query().Get("from"), req.From)
	toRaw := firstNonEmpty(r.URL.Query().Get("to"), req.To)
	if uploadMode {
		// In upload mode, return the full dataset so the client can verify server state.
		updatedSinceRaw = ""
		fromRaw = ""
		toRaw = ""
	}

	log.Debug("resolved raw parameters",
		"updatedSince", updatedSinceRaw,
		"from", fromRaw,
		"to", toRaw,
	)

	// ---------------------------------------------------------------------
	// Parse timestamps
	// ---------------------------------------------------------------------
	var updatedSince *time.Time
	if updatedSinceRaw != "" {
		t, err := time.Parse(time.RFC3339, updatedSinceRaw)
		if err != nil {
			log.Warn("invalid updatedSince timestamp", "value", updatedSinceRaw, "err", err)
			s.writeError(w, http.StatusBadRequest, "invalid updatedSince")
			return
		}
		t = t.UTC()
		updatedSince = &t
		log.Debug("parsed updatedSince", "value", t)
	}

	var from *time.Time
	if fromRaw != "" {
		t, err := time.Parse(time.RFC3339, fromRaw)
		if err != nil {
			log.Warn("invalid from timestamp", "value", fromRaw, "err", err)
			s.writeError(w, http.StatusBadRequest, "invalid from")
			return
		}
		t = t.UTC()
		from = &t
		log.Debug("parsed from", "value", t)
	}

	var to *time.Time
	if toRaw != "" {
		t, err := time.Parse(time.RFC3339, toRaw)
		if err != nil {
			log.Warn("invalid to timestamp", "value", toRaw, "err", err)
			s.writeError(w, http.StatusBadRequest, "invalid to")
			return
		}
		t = t.UTC()
		to = &t
		log.Debug("parsed to", "value", t)
	}

	// ---------------------------------------------------------------------
	// Build query
	// ---------------------------------------------------------------------
	log.Debug("building entry query")

	q := s.Db.Ent().Entry.Query().
		Where(entry.PatientIDEQ(p.ID)).
		Order(entry.ByHappenedAt(sql.OrderDesc()))

	if updatedSince != nil {
		log.Debug("applying updatedSince filter", "updatedSince", *updatedSince)
		q = q.Where(entry.UpdatedAtGT(*updatedSince))
	}
	if from != nil {
		log.Debug("applying from filter", "from", *from)
		q = q.Where(entry.HappenedAtGTE(*from))
	}
	if to != nil {
		log.Debug("applying to filter", "to", *to)
		q = q.Where(entry.HappenedAtLTE(*to))
	}

	// ---------------------------------------------------------------------
	// Execute query
	// ---------------------------------------------------------------------
	log.Info("executing entry query")
	entries, err := q.All(r.Context())
	if err != nil {
		log.Error("failed to sync patient entries", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not sync entries")
		return
	}

	log.Info("entries fetched", "count", len(entries))

	// ---------------------------------------------------------------------
	// Build response
	// ---------------------------------------------------------------------
	resp := entriesSyncResponse{
		Entries: make([]entrySyncDTO, 0, len(entries)),
	}

	for _, e := range entries {
		notes := ""
		if e.Notes != nil {
			notes = *e.Notes
		}

		tags := e.Tags
		if tags == nil {
			tags = []string{}
		}

		resp.Entries = append(resp.Entries, entrySyncDTO{
			ID:         e.ID.String(),
			CreatedAt:  e.CreatedAt,
			HappenedAt: e.HappenedAt,
			Notes:      notes,
			Tags:       tags,
			UpdatedAt:  e.UpdatedAt,
		})
	}

	log.Info("response built", "entries", len(resp.Entries))

	// ---------------------------------------------------------------------
	// Write response
	// ---------------------------------------------------------------------
	s.writeJSON(w, http.StatusOK, resp)
	log.Info("request completed successfully")
}

func firstNonEmpty(a, b string) string {
	if a != "" {
		return a
	}
	return b
}
