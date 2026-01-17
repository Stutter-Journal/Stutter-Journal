package auth

import (
	"errors"
	"net/http"
	"time"

	"github.com/google/uuid"
	"github.com/gorilla/securecookie"
	"golang.org/x/crypto/bcrypt"
)

var (
	ErrNoSession      = errors.New("no session cookie")
	ErrInvalidSession = errors.New("invalid session cookie")
	ErrExpiredSession = errors.New("session expired")
)

// SessionClaims is the payload stored in the signed cookie.
type SessionClaims struct {
	DoctorID  uuid.UUID `json:"doctor_id"`
	PatientID uuid.UUID `json:"patient_id"`
	IssuedAt  time.Time `json:"issued_at"`
	ExpiresAt time.Time `json:"expires_at"`
}

// Manager issues and validates signed session cookies.
type Manager struct {
	cfg   Config
	codec *securecookie.SecureCookie
	now   func() time.Time
}

func NewManager(cfg Config) (*Manager, error) {
	codec := securecookie.New(cfg.SecretKey, nil)
	codec.SetSerializer(securecookie.JSONEncoder{})
	codec.MaxAge(int(cfg.SessionTTL.Seconds()))

	return &Manager{
		cfg:   cfg,
		codec: codec,
		now:   time.Now,
	}, nil
}

// IssueSession signs a new session cookie for the given doctor ID.
func (m *Manager) IssueSession(w http.ResponseWriter, doctorID uuid.UUID) error {
	issued := m.now().UTC()
	expires := issued.Add(m.cfg.SessionTTL)

	claims := SessionClaims{
		DoctorID:  doctorID,
		PatientID: uuid.Nil,
		IssuedAt:  issued,
		ExpiresAt: expires,
	}

	token, err := m.codec.Encode(m.cfg.CookieName, claims)
	if err != nil {
		return err
	}

	cookie := &http.Cookie{
		Name:     m.cfg.CookieName,
		Value:    token,
		Path:     m.cfg.CookiePath,
		Domain:   m.cfg.CookieDomain,
		Secure:   m.cfg.CookieSecure,
		HttpOnly: m.cfg.CookieHTTPOnly,
		SameSite: m.cfg.CookieSameSite,
		Expires:  expires,
		MaxAge:   int(m.cfg.SessionTTL.Seconds()),
	}

	http.SetCookie(w, cookie)
	return nil
}

// IssuePatientSession signs a new session cookie for the given patient ID.
func (m *Manager) IssuePatientSession(w http.ResponseWriter, patientID uuid.UUID) error {
	issued := m.now().UTC()
	expires := issued.Add(m.cfg.SessionTTL)

	claims := SessionClaims{
		DoctorID:  uuid.Nil,
		PatientID: patientID,
		IssuedAt:  issued,
		ExpiresAt: expires,
	}

	token, err := m.codec.Encode(m.cfg.CookieName, claims)
	if err != nil {
		return err
	}

	cookie := &http.Cookie{
		Name:     m.cfg.CookieName,
		Value:    token,
		Path:     m.cfg.CookiePath,
		Domain:   m.cfg.CookieDomain,
		Secure:   m.cfg.CookieSecure,
		HttpOnly: m.cfg.CookieHTTPOnly,
		SameSite: m.cfg.CookieSameSite,
		Expires:  expires,
		MaxAge:   int(m.cfg.SessionTTL.Seconds()),
	}

	http.SetCookie(w, cookie)
	return nil
}

// ReadSession verifies and returns the claims from the request cookie.
func (m *Manager) ReadSession(r *http.Request) (*SessionClaims, error) {
	cookie, err := r.Cookie(m.cfg.CookieName)
	if err != nil {
		return nil, ErrNoSession
	}

	var claims SessionClaims
	if err := m.codec.Decode(m.cfg.CookieName, cookie.Value, &claims); err != nil {
		return nil, ErrInvalidSession
	}

	if claims.ExpiresAt.Before(m.now()) {
		return nil, ErrExpiredSession
	}

	// Exactly one subject must be present.
	if (claims.DoctorID == uuid.Nil) == (claims.PatientID == uuid.Nil) {
		return nil, ErrInvalidSession
	}

	return &claims, nil
}

// ClearSession deletes the session cookie on the client.
func (m *Manager) ClearSession(w http.ResponseWriter) {
	http.SetCookie(w, &http.Cookie{
		Name:     m.cfg.CookieName,
		Value:    "",
		Path:     m.cfg.CookiePath,
		Domain:   m.cfg.CookieDomain,
		Secure:   m.cfg.CookieSecure,
		HttpOnly: m.cfg.CookieHTTPOnly,
		SameSite: m.cfg.CookieSameSite,
		Expires:  time.Unix(0, 0),
		MaxAge:   -1,
	})
}

func (m *Manager) HashPassword(plaintext string) (string, error) {
	if len(plaintext) < 8 {
		return "", errors.New("password must be at least 8 characters")
	}

	hash, err := bcrypt.GenerateFromPassword([]byte(plaintext), m.cfg.BcryptCost)
	if err != nil {
		return "", err
	}
	return string(hash), nil
}

func (m *Manager) VerifyPassword(hash string, plaintext string) error {
	if hash == "" {
		return errors.New("empty password hash")
	}
	return bcrypt.CompareHashAndPassword([]byte(hash), []byte(plaintext))
}
