package auth

import (
	"crypto/rand"
	"encoding/base64"
	"errors"
	"fmt"
	"net/http"
	"os"
	"strconv"
	"strings"
	"time"

	"github.com/charmbracelet/log"
)

const (
	defaultCookieName   = "eloquia_session"
	defaultCookiePath   = "/"
	defaultSessionTTL   = 24 * time.Hour
	defaultBcryptCost   = 12
	envAuthSecret       = "AUTH_COOKIE_SECRET"
	envAuthCookieName   = "AUTH_COOKIE_NAME"
	envAuthCookieDomain = "AUTH_COOKIE_DOMAIN"
	envAuthCookiePath   = "AUTH_COOKIE_PATH"
	envAuthCookieSecure = "AUTH_COOKIE_SECURE"
	envAuthSameSite     = "AUTH_COOKIE_SAMESITE"
	envAuthTTL          = "AUTH_SESSION_TTL"
	envAuthBcryptCost   = "AUTH_BCRYPT_COST"
)

// Config captures the settings needed to issue and validate session cookies.
// The cookie uses an HMAC signature (no encryption) via securecookie.
type Config struct {
	CookieName     string
	CookieDomain   string
	CookiePath     string
	CookieSecure   bool
	CookieHTTPOnly bool
	CookieSameSite http.SameSite
	SessionTTL     time.Duration
	BcryptCost     int
	SecretKey      []byte
}

// LoadConfig reads environment variables and returns an auth Config.
// AUTH_COOKIE_SECRET is required; others fall back to safe defaults.
func LoadConfig(logger *log.Logger) (Config, error) {
	if logger == nil {
		logger = log.Default()
	}

	cfg := Config{
		CookieName:     getEnvOrDefault(envAuthCookieName, defaultCookieName),
		CookieDomain:   strings.TrimSpace(os.Getenv(envAuthCookieDomain)),
		CookiePath:     getEnvOrDefault(envAuthCookiePath, defaultCookiePath),
		CookieSecure:   parseBoolWithDefault(envAuthCookieSecure, inferSecureDefault()),
		CookieHTTPOnly: true,
		CookieSameSite: parseSameSite(getEnvOrDefault(envAuthSameSite, "Lax")),
		SessionTTL:     parseDurationWithDefault(envAuthTTL, defaultSessionTTL),
		BcryptCost:     parseIntWithDefault(envAuthBcryptCost, defaultBcryptCost),
	}

	secret, err := loadSecret(os.Getenv(envAuthSecret))
	if err != nil {
		return Config{}, err
	}

	if len(secret) == 0 {
		// Generate an ephemeral secret to avoid hard failures in local dev.
		secret = mustRandomBytes(32)
		logger.Warn("AUTH_COOKIE_SECRET is not set; generated an ephemeral secret for this process")
	}

	cfg.SecretKey = secret

	if cfg.SessionTTL <= 0 {
		return Config{}, fmt.Errorf("session TTL must be positive; got %v", cfg.SessionTTL)
	}

	if cfg.BcryptCost < 4 {
		return Config{}, fmt.Errorf("bcrypt cost too low (%d); set AUTH_BCRYPT_COST to 10-14", cfg.BcryptCost)
	}

	return cfg, nil
}

func loadSecret(raw string) ([]byte, error) {
	raw = strings.TrimSpace(raw)
	if raw == "" {
		return nil, nil
	}

	// Try base64 first to support binary keys.
	if decoded, err := base64.StdEncoding.DecodeString(raw); err == nil {
		if len(decoded) >= 32 {
			return decoded, nil
		}
	}

	if len(raw) < 32 {
		return nil, errors.New("AUTH_COOKIE_SECRET must be at least 32 bytes (base64-encoded or plain)")
	}

	return []byte(raw), nil
}

func parseBoolWithDefault(key string, fallback bool) bool {
	raw := strings.TrimSpace(os.Getenv(key))
	if raw == "" {
		return fallback
	}
	switch strings.ToLower(raw) {
	case "1", "true", "yes", "on":
		return true
	case "0", "false", "no", "off":
		return false
	default:
		return fallback
	}
}

func parseDurationWithDefault(key string, fallback time.Duration) time.Duration {
	raw := strings.TrimSpace(os.Getenv(key))
	if raw == "" {
		return fallback
	}
	d, err := time.ParseDuration(raw)
	if err != nil {
		return fallback
	}
	return d
}

func parseIntWithDefault(key string, fallback int) int {
	raw := strings.TrimSpace(os.Getenv(key))
	if raw == "" {
		return fallback
	}
	val, err := strconv.Atoi(raw)
	if err != nil {
		return fallback
	}
	return val
}

func getEnvOrDefault(key, fallback string) string {
	val := strings.TrimSpace(os.Getenv(key))
	if val == "" {
		return fallback
	}
	return val
}

func parseSameSite(raw string) http.SameSite {
	switch strings.ToLower(strings.TrimSpace(raw)) {
	case "strict":
		return http.SameSiteStrictMode
	case "none":
		return http.SameSiteNoneMode
	case "lax":
		fallthrough
	default:
		return http.SameSiteLaxMode
	}
}

func inferSecureDefault() bool {
	env := strings.ToLower(strings.TrimSpace(firstNonEmpty(os.Getenv("ENVIRONMENT"), os.Getenv("APP_ENV"), os.Getenv("BLUEPRINT_ENV"))))
	if env == "" {
		return false
	}
	return env == "production" || env == "prod"
}

func firstNonEmpty(values ...string) string {
	for _, v := range values {
		if strings.TrimSpace(v) != "" {
			return v
		}
	}
	return ""
}

func mustRandomBytes(n int) []byte {
	buf := make([]byte, n)
	if _, err := rand.Read(buf); err != nil {
		panic(fmt.Errorf("generate random bytes: %w", err))
	}
	return buf
}
