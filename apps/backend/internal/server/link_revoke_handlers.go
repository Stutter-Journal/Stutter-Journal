package server

import (
    "net/http"

    "backend/ent/doctorpatientlink"

    "github.com/charmbracelet/log"
)

type revokeLinksResponse struct {
    Revoked int `json:"revoked"`
}

// revokeMyLinksHandler revokes (disconnects) the current patient's approved doctor links.
//
// Today the mobile UX treats the therapist relationship as a single "connection".
// This endpoint revokes all approved links for the current patient.
// @Summary Revoke linked doctors for the current patient
// @Tags Links
// @Produce json
// @Security SessionCookie
// @Success 200 {object} revokeLinksResponse
// @Failure 401 {object} ErrorResponse
// @Failure 500 {object} ErrorResponse
// @Router /links/revoke [post]
func (s *Server) revokeMyLinksHandler(w http.ResponseWriter, r *http.Request) {
    p, ok := currentPatient(r.Context())
    if !ok {
        s.writeError(w, http.StatusUnauthorized, "unauthorized")
        return
    }

    ctx := r.Context()

    revoked, err := s.Db.Ent().DoctorPatientLink.
        Update().
        Where(
            doctorpatientlink.PatientIDEQ(p.ID),
            doctorpatientlink.StatusEQ(doctorpatientlink.StatusApproved),
        ).
        SetStatus(doctorpatientlink.StatusRevoked).
        Save(ctx)
    if err != nil {
        log.Error("failed to revoke links", "err", err)
        s.writeError(w, http.StatusInternalServerError, "could not revoke link")
        return
    }

    s.writeJSON(w, http.StatusOK, revokeLinksResponse{Revoked: revoked})
}
