package server

import (
	"crypto/rand"
	"encoding/json"
	"errors"
	"math/big"
	"net/http"
	"regexp"
	"strings"
	"time"

	"backend/ent"
	"backend/ent/doctorpatientlink"
	"backend/ent/pairingcode"

	"github.com/charmbracelet/log"
)

const pairingCodeTTL = 2 * time.Minute

var pairingCodeRe = regexp.MustCompile(`^\d{6}$`)

type PairingCodeCreateResponse struct {
	Code      string    `json:"code"`
	ExpiresAt time.Time `json:"expiresAt"`
	QRText    string    `json:"qrText"`
}

type PairingCodeRedeemRequest struct {
	Code string `json:"code"`
}

// createPairingCodeHandler creates a short-lived 6-digit code a patient can redeem.
// @Summary Create a short-lived patient pairing code
// @Tags Links
// @Produce json
// @Security SessionCookie
// @Success 201 {object} PairingCodeCreateResponse
// @Failure 401 {object} ErrorResponse
// @Failure 500 {object} ErrorResponse
// @Router /links/pairing-code [post]
func (s *Server) createPairingCodeHandler(w http.ResponseWriter, r *http.Request) {
	doc, ok := currentDoctor(r.Context())
	if !ok {
		s.writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	now := time.Now().UTC()

	// Invalidate any active codes for this doctor (one active code at a time).
	_, _ = s.Db.Ent().PairingCode.
		Update().
		Where(
			pairingcode.DoctorIDEQ(doc.ID),
			pairingcode.ConsumedAtIsNil(),
			pairingcode.ExpiresAtGT(now),
		).
		SetExpiresAt(now).
		Save(r.Context())

	// Generate a collision-free code among currently-active codes.
	for attempt := 0; attempt < 25; attempt++ {
		code, err := generateSixDigitCode()
		if err != nil {
			log.Error("failed to generate pairing code", "err", err)
			s.writeError(w, http.StatusInternalServerError, "could not generate code")
			return
		}

		activeCount, err := s.Db.Ent().PairingCode.
			Query().
			Where(
				pairingcode.CodeEQ(code),
				pairingcode.ConsumedAtIsNil(),
				pairingcode.ExpiresAtGT(now),
			).
			Count(r.Context())
		if err != nil {
			log.Error("failed to check pairing code collision", "err", err)
			s.writeError(w, http.StatusInternalServerError, "could not generate code")
			return
		}
		if activeCount > 0 {
			continue
		}

		expiresAt := now.Add(pairingCodeTTL)
		created, err := s.Db.Ent().PairingCode.
			Create().
			SetCode(code).
			SetDoctorID(doc.ID).
			SetExpiresAt(expiresAt).
			Save(r.Context())
		if err != nil {
			log.Error("failed to save pairing code", "err", err)
			s.writeError(w, http.StatusInternalServerError, "could not generate code")
			return
		}

		resp := PairingCodeCreateResponse{
			Code:      created.Code,
			ExpiresAt: created.ExpiresAt,
			QRText:    created.Code,
		}
		s.writeJSON(w, http.StatusCreated, resp)
		return
	}

	s.writeError(w, http.StatusInternalServerError, "could not generate code")
}

// redeemPairingCodeHandler redeems a 6-digit code and creates/approves the doctor-patient link.
// @Summary Redeem a patient pairing code
// @Tags Links
// @Accept json
// @Produce json
// @Security SessionCookie
// @Param request body PairingCodeRedeemRequest true "Redeem payload"
// @Success 200 {object} LinkResponse
// @Failure 400 {object} ErrorResponse
// @Failure 401 {object} ErrorResponse
// @Failure 404 {object} ErrorResponse
// @Failure 409 {object} ErrorResponse
// @Failure 500 {object} ErrorResponse
// @Router /links/pairing-code/redeem [post]
func (s *Server) redeemPairingCodeHandler(w http.ResponseWriter, r *http.Request) {
	p, ok := currentPatient(r.Context())
	if !ok {
		s.writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req PairingCodeRedeemRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		s.writeError(w, http.StatusBadRequest, "invalid JSON")
		return
	}

	code := strings.TrimSpace(req.Code)
	code = strings.ReplaceAll(code, " ", "")
	code = strings.ReplaceAll(code, "-", "")

	if !pairingCodeRe.MatchString(code) {
		s.writeError(w, http.StatusBadRequest, "code must be 6 digits")
		return
	}

	now := time.Now().UTC()

	pc, err := s.Db.Ent().PairingCode.
		Query().
		Where(
			pairingcode.CodeEQ(code),
			pairingcode.ConsumedAtIsNil(),
			pairingcode.ExpiresAtGT(now),
		).
		Order(ent.Desc(pairingcode.FieldExpiresAt)).
		First(r.Context())
	if ent.IsNotFound(err) {
		s.writeError(w, http.StatusNotFound, "code not found or expired")
		return
	} else if err != nil {
		log.Error("failed to load pairing code", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not redeem code")
		return
	}

	tx, err := s.Db.Ent().Tx(r.Context())
	if err != nil {
		log.Error("failed to begin transaction", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not redeem code")
		return
	}
	defer func() {
		_ = tx.Rollback()
	}()

	// Consume atomically (avoid races).
	_, err = tx.PairingCode.
		UpdateOneID(pc.ID).
		Where(
			pairingcode.ConsumedAtIsNil(),
			pairingcode.ExpiresAtGT(now),
		).
		SetConsumedAt(now).
		SetConsumedByPatientID(p.ID).
		Save(r.Context())
	if ent.IsNotFound(err) {
		s.writeError(w, http.StatusNotFound, "code not found or expired")
		return
	} else if err != nil {
		log.Error("failed to consume pairing code", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not redeem code")
		return
	}

	// Create (or approve) the link.
	link, err := tx.DoctorPatientLink.
		Query().
		Where(
			doctorpatientlink.DoctorIDEQ(pc.DoctorID),
			doctorpatientlink.PatientIDEQ(p.ID),
		).
		Only(r.Context())

	if ent.IsNotFound(err) {
		created, createErr := tx.DoctorPatientLink.
			Create().
			SetDoctorID(pc.DoctorID).
			SetPatientID(p.ID).
			SetStatus(doctorpatientlink.StatusApproved).
			SetRequestedAt(now).
			SetApprovedAt(now).
			SetApprovedByDoctorID(pc.DoctorID).
			Save(r.Context())
		if createErr != nil {
			log.Error("failed to create doctor-patient link", "err", createErr)
			s.writeError(w, http.StatusInternalServerError, "could not create link")
			return
		}
		link = created
	} else if err != nil {
		log.Error("failed to check existing link", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not redeem code")
		return
	} else {
		// If a pending/denied link exists, upgrade it to approved.
		if link.Status != doctorpatientlink.StatusApproved {
			updated, updErr := tx.DoctorPatientLink.
				UpdateOneID(link.ID).
				SetStatus(doctorpatientlink.StatusApproved).
				SetApprovedAt(now).
				SetApprovedByDoctorID(pc.DoctorID).
				Save(r.Context())
			if updErr != nil {
				log.Error("failed to approve existing link", "err", updErr)
				s.writeError(w, http.StatusInternalServerError, "could not approve link")
				return
			}
			link = updated
		}
	}

	if err := tx.Commit(); err != nil {
		log.Error("failed to commit pairing redeem", "err", err)
		s.writeError(w, http.StatusInternalServerError, "could not redeem code")
		return
	}

	s.writeJSON(w, http.StatusOK, map[string]any{
		"link":    buildLinkDTO(link),
		"patient": buildPatientDTO(p),
	})
}

func generateSixDigitCode() (string, error) {
	max := big.NewInt(1000000) // 0..999999
	n, err := rand.Int(rand.Reader, max)
	if err != nil {
		return "", err
	}
	return formatSixDigits(n.Int64())
}

func formatSixDigits(n int64) (string, error) {
	if n < 0 || n >= 1000000 {
		return "", errors.New("invalid range")
	}
	// Manual zero-padding without fmt to keep deps minimal.
	s := []byte{'0', '0', '0', '0', '0', '0'}
	for i := 5; i >= 0; i-- {
		s[i] = byte('0' + (n % 10))
		n /= 10
	}
	return string(s), nil
}
