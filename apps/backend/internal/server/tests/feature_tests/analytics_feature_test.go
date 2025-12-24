package tests

import (
	"context"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"testing"
	"time"

	"backend/ent/schema"
	"backend/internal/auth"
	"backend/internal/database"
	"backend/internal/server"
	"backend/internal/server/bddtest"

	"github.com/charmbracelet/log"
	"github.com/cucumber/godog"
	"github.com/google/uuid"
	"github.com/gorilla/securecookie"
)

func TestAnalyticsFeatures(t *testing.T) {
	env := bddtest.NewEnv(t, func(db *database.Client) (http.Handler, error) {
		logger := log.NewWithOptions(io.Discard, log.Options{})
		_ = os.Setenv("AUTH_COOKIE_SECRET", "super-secret-integration-secret-for-analytics-minimum-32-bytes")

		authCfg, err := auth.LoadConfig(logger)
		if err != nil {
			return nil, err
		}
		if len(authCfg.SecretKey) == 0 {
			authCfg.SecretKey = securecookie.GenerateRandomKey(32)
		}

		authManager, err := auth.NewManager(authCfg)
		if err != nil {
			return nil, err
		}

		s := &server.Server{
			Db:   db,
			Auth: authManager,
		}
		return s.RegisterRoutes(), nil
	})

	opts := &godog.Options{
		Format:   "pretty",
		Strict:   true,
		Paths:    []string{filepath.Join("..", "..", "..", "..", "features", "analytics.feature")},
		TestingT: t,
	}

	suite := godog.TestSuite{
		Name:                "analytics",
		ScenarioInitializer: func(sc *godog.ScenarioContext) { initAnalyticsScenario(sc, env) },
		Options:             opts,
	}

	if suite.Run() != 0 {
		t.Fatalf("godog suite failed")
	}
}

type analyticsFeature struct {
	env       *bddtest.Env
	client    *bddtest.Client
	doctorID  string
	patientID uuid.UUID
}

func initAnalyticsScenario(sc *godog.ScenarioContext, env *bddtest.Env) {
	f := &analyticsFeature{env: env}

	sc.Before(func(ctx context.Context, _ *godog.Scenario) (context.Context, error) {
		f.client = bddtest.NewClient(env.BaseURL)
		f.doctorID = ""
		f.patientID = uuid.Nil

		cctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
		defer cancel()
		if _, err := env.DB.Ent().Doctor.Delete().Exec(cctx); err != nil {
			return ctx, err
		}
		if _, err := env.DB.Ent().Patient.Delete().Exec(cctx); err != nil {
			return ctx, err
		}
		if _, err := env.DB.Ent().Entry.Delete().Exec(cctx); err != nil {
			return ctx, err
		}
		if _, err := env.DB.Ent().DoctorPatientLink.Delete().Exec(cctx); err != nil {
			return ctx, err
		}
		return ctx, nil
	})

	sc.Step(`^the API is running$`, f.apiIsRunning)
	sc.Step(`^I register a doctor with email "([^"]+)" password "([^"]+)" displayName "([^"]+)"$`, f.registerDoctor)
	sc.Step(`^patient "([^"]+)" has analytics entries:$`, f.patientHasEntries)
	sc.Step(`^I call GET "([^"]+)"$`, f.callGet)
	sc.Step(`^the response status should be (\d+)$`, f.statusShouldBe)
	sc.Step(`^the response JSON field "([^"]+)" should be "([^"]+)"$`, f.jsonFieldShouldBe)
}

func (f *analyticsFeature) apiIsRunning() error {
	if err := f.client.Get("/ready"); err != nil {
		return err
	}
	return f.client.RequireStatus(http.StatusOK)
}

func (f *analyticsFeature) registerDoctor(email, password, displayName string) error {
	if err := f.client.PostJSON("/doctor/register", map[string]string{
		"email":       email,
		"password":    password,
		"displayName": displayName,
	}); err != nil {
		return err
	}
	if err := f.client.RequireStatus(http.StatusCreated); err != nil {
		return err
	}
	id, err := bddtest.ExtractField(f.client.LastBody, "doctor.id")
	if err != nil {
		return err
	}
	f.doctorID = id
	return nil
}

func (f *analyticsFeature) patientHasEntries(name string, table *godog.Table) error {
	if f.doctorID == "" {
		return fmt.Errorf("doctor not registered")
	}

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	p, err := f.env.DB.Ent().Patient.Create().SetDisplayName(name).Save(ctx)
	if err != nil {
		return err
	}

	docUUID, err := uuid.Parse(f.doctorID)
	if err != nil {
		return err
	}

	if _, err := f.env.DB.Ent().DoctorPatientLink.
		Create().
		SetDoctorID(docUUID).
		SetPatientID(p.ID).
		SetStatus("Approved").
		SetRequestedAt(time.Now()).
		SetApprovedAt(time.Now()).
		SetApprovedByDoctorID(docUUID).
		Save(ctx); err != nil {
		return err
	}

	for i, row := range table.Rows {
		if i == 0 {
			continue
		}
		if len(row.Cells) < 5 {
			continue
		}
		happenedAt, err := parseDateCell(strings.TrimSpace(row.Cells[0].Value))
		if err != nil {
			return err
		}
		stutterVal := strings.TrimSpace(row.Cells[1].Value)
		sf := parseIntOrDefault(stutterVal, 0)
		emotions := parseList(row.Cells[2].Value)
		triggers := parseList(row.Cells[3].Value)
		techniques := parseList(row.Cells[4].Value)

		var emoObjs []schema.Emotion
		for _, e := range emotions {
			emoObjs = append(emoObjs, schema.Emotion{Name: e})
		}

		if _, err := f.env.DB.Ent().Entry.Create().
			SetPatientID(p.ID).
			SetHappenedAt(happenedAt).
			SetStutterFrequency(sf).
			SetEmotions(emoObjs).
			SetTriggers(triggers).
			SetTechniques(techniques).
			Save(ctx); err != nil {
			return err
		}
	}

	f.patientID = p.ID
	return nil
}

func (f *analyticsFeature) callGet(path string) error {
	if f.patientID != uuid.Nil {
		path = strings.ReplaceAll(path, "{patOneID}", f.patientID.String())
	}
	return f.client.Get(path)
}

func (f *analyticsFeature) statusShouldBe(code int) error {
	return f.client.RequireStatus(code)
}

func (f *analyticsFeature) jsonFieldShouldBe(field, expected string) error {
	got, err := bddtest.ExtractField(f.client.LastBody, field)
	if err != nil {
		return err
	}
	if got != expected {
		return fmt.Errorf("expected %s to be %s, got %s", field, expected, got)
	}
	return nil
}

func parseList(raw string) []string {
	parts := strings.Split(raw, ",")
	out := make([]string, 0, len(parts))
	for _, p := range parts {
		val := strings.TrimSpace(p)
		if val != "" {
			out = append(out, val)
		}
	}
	return out
}

func parseIntOrDefault(raw string, fallback int) int {
	val := strings.TrimSpace(raw)
	if val == "" {
		return fallback
	}
	if n, err := strconv.Atoi(val); err == nil {
		return n
	}
	return fallback
}

func parseDateCell(raw string) (time.Time, error) {
	switch {
	case raw == "today":
		return time.Now().UTC().Truncate(24 * time.Hour).Add(10 * time.Hour), nil
	case strings.HasPrefix(raw, "today-"):
		offset := strings.TrimPrefix(raw, "today-")
		if strings.HasSuffix(offset, "d") {
			offset = strings.TrimSuffix(offset, "d")
			if days, err := strconv.Atoi(offset); err == nil {
				return time.Now().UTC().AddDate(0, 0, -days).Truncate(24 * time.Hour).Add(10 * time.Hour), nil
			}
		}
	}
	return time.Parse(time.RFC3339, raw)
}
