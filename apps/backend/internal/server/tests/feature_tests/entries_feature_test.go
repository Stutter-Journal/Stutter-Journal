package tests

import (
	"context"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"strings"
	"testing"
	"time"

	"backend/internal/auth"
	"backend/internal/database"
	"backend/internal/server"
	"backend/internal/server/bddtest"

	"github.com/charmbracelet/log"
	"github.com/cucumber/godog"
	"github.com/google/uuid"
	"github.com/gorilla/securecookie"
)

func TestEntriesFeatures(t *testing.T) {
	env := bddtest.NewEnv(t, func(db *database.Client) (http.Handler, error) {
		logger := log.NewWithOptions(io.Discard, log.Options{})
		_ = os.Setenv("AUTH_COOKIE_SECRET", "super-secret-integration-secret-for-entries-test-minimum-32-bytes")

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
		Paths:    []string{filepath.Join("..", "..", "..", "..", "features", "entries.feature")},
		TestingT: t,
	}

	suite := godog.TestSuite{
		Name:                "entries",
		ScenarioInitializer: func(sc *godog.ScenarioContext) { initEntriesScenario(sc, env) },
		Options:             opts,
	}

	if suite.Run() != 0 {
		t.Fatalf("godog suite failed")
	}
}

type entriesFeature struct {
	env       *bddtest.Env
	client    *bddtest.Client
	doctorID  string
	patientID uuid.UUID
}

func initEntriesScenario(sc *godog.ScenarioContext, env *bddtest.Env) {
	f := &entriesFeature{env: env}

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
	sc.Step(`^patient "([^"]+)" has entries:$`, f.patientHasEntries)
	sc.Step(`^I call GET "([^"]+)"$`, f.callGet)
	sc.Step(`^the response status should be (\d+)$`, f.statusShouldBe)
	sc.Step(`^the response JSON field "([^"]+)" should be "([^"]+)"$`, f.jsonFieldShouldBe)
}

func (f *entriesFeature) apiIsRunning() error {
	if err := f.client.Get("/ready"); err != nil {
		return err
	}
	return f.client.RequireStatus(http.StatusOK)
}

func (f *entriesFeature) registerDoctor(email, password, displayName string) error {
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

func (f *entriesFeature) patientHasEntries(name string, table *godog.Table) error {
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
		if len(row.Cells) < 3 {
			continue
		}
		happenedAt, err := time.Parse(time.RFC3339, strings.TrimSpace(row.Cells[0].Value))
		if err != nil {
			return err
		}
		situation := strings.TrimSpace(row.Cells[1].Value)
		notes := strings.TrimSpace(row.Cells[2].Value)

		if _, err := f.env.DB.Ent().Entry.Create().
			SetPatientID(p.ID).
			SetHappenedAt(happenedAt).
			SetSituation(situation).
			SetNotes(notes).
			Save(ctx); err != nil {
			return err
		}
	}

	f.patientID = p.ID
	return nil
}

func (f *entriesFeature) callGet(path string) error {
	if f.patientID != uuid.Nil {
		path = strings.ReplaceAll(path, "{patOneID}", f.patientID.String())
	}
	return f.client.Get(path)
}

func (f *entriesFeature) statusShouldBe(code int) error {
	return f.client.RequireStatus(code)
}

func (f *entriesFeature) jsonFieldShouldBe(field, expected string) error {
	got, err := bddtest.ExtractField(f.client.LastBody, field)
	if err != nil {
		return err
	}
	if got != expected {
		return fmt.Errorf("expected %s to be %s, got %s", field, expected, got)
	}
	return nil
}
