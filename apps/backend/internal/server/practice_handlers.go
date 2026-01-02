package server

import (
	"net/http"
	"strings"

	"backend/ent"
	"backend/ent/doctor"

	"github.com/charmbracelet/log"
)

type practiceCreateRequest struct {
	Name    string  `json:"name"`
	Address *string `json:"address,omitempty"`
	LogoURL *string `json:"logoUrl,omitempty"`
}

type practiceResponse struct {
	ID      string  `json:"id"`
	Name    string  `json:"name"`
	Address *string `json:"address,omitempty"`
	LogoURL *string `json:"logoUrl,omitempty"`
}

// practiceCreateHandler creates a practice and assigns the current doctor.
// @Summary Create a practice and assign the current doctor
// @Tags Practice
// @Accept json
// @Produce json
// @Security SessionCookie
// @Param request body PracticeCreateRequest true "Practice create payload"
// @Success 201 {object} PracticeCreateResponse
// @Failure 400 {object} ErrorResponse
// @Failure 401 {object} ErrorResponse
// @Router /practice [post]
func (s *Server) practiceCreateHandler(w http.ResponseWriter, r *http.Request) {
	doc, ok := currentDoctor(r.Context())
	if !ok {
		s.writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req practiceCreateRequest
	if !s.decodeJSON(w, r, &req) {
		return
	}

	req.Name = strings.TrimSpace(req.Name)
	if req.Name == "" {
		s.writeError(w, http.StatusBadRequest, "name is required")
		return
	}

	builder := s.Db.Ent().Practice.Create().SetName(req.Name)
	if req.Address != nil {
		addr := strings.TrimSpace(*req.Address)
		builder.SetAddress(addr)
	}
	if req.LogoURL != nil {
		url := strings.TrimSpace(*req.LogoURL)
		builder.SetLogoURL(url)
	}

	practice, err := builder.Save(r.Context())
	if err != nil {
		log.Error("failed to create practice", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not create practice")
		return
	}

	update := s.Db.Ent().Doctor.UpdateOneID(doc.ID).SetPracticeID(practice.ID)
	if doc.Role != doctor.RoleOwner {
		update.SetRole(doctor.RoleOwner)
	}

	updatedDoctor, err := update.Save(r.Context())
	if err != nil {
		log.Error("failed to assign practice to doctor", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not assign practice")
		return
	}

	s.writeJSON(w, http.StatusCreated, map[string]any{
		"practice": buildPracticeResponse(practice),
		"doctor":   buildDoctorResponse(updatedDoctor),
	})
}

func buildPracticeResponse(p *ent.Practice) practiceResponse {
	var addr, logo *string
	if p.Address != nil {
		val := strings.TrimSpace(*p.Address)
		addr = &val
	}
	if p.LogoURL != nil {
		val := strings.TrimSpace(*p.LogoURL)
		logo = &val
	}

	return practiceResponse{
		ID:      p.ID.String(),
		Name:    p.Name,
		Address: addr,
		LogoURL: logo,
	}
}
