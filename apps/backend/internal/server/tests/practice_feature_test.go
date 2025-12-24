package tests

import (
	"context"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"testing"
	"time"

	"backend/ent/doctor"
	"backend/internal/auth"
	"backend/internal/database"
	"backend/internal/server"
	"backend/internal/server/bddtest"

	"github.com/charmbracelet/log"
	"github.com/cucumber/godog"
	"github.com/google/uuid"
	"github.com/gorilla/securecookie"
)

func TestPracticeFeatures(t *testing.T) {
	env := bddtest.NewEnv(t, func(db *database.Client) (http.Handler, error) {
		logger := log.NewWithOptions(io.Discard, log.Options{})
		_ = os.Setenv("AUTH_COOKIE_SECRET", "integration-secret")

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
		Paths:    []string{filepath.Join("..", "..", "features", "practice.feature")},
		TestingT: t,
	}

	suite := godog.TestSuite{
		Name:                "practice",
		ScenarioInitializer: func(sc *godog.ScenarioContext) { initPracticeScenario(sc, env) },
		Options:             opts,
	}

	if suite.Run() != 0 {
		t.Fatalf("godog suite failed")
	}
}

type practiceFeature struct {
	env        *bddtest.Env
	client     *bddtest.Client
	doctorID   string
	practiceID string
}

func initPracticeScenario(sc *godog.ScenarioContext, env *bddtest.Env) {
	p := &practiceFeature{env: env}

	sc.Before(func(ctx context.Context, _ *godog.Scenario) (context.Context, error) {
		p.client = bddtest.NewClient(env.BaseURL)
		p.doctorID = ""
		p.practiceID = ""

		cctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
		defer cancel()
		if _, err := env.DB.Ent().Doctor.Delete().Exec(cctx); err != nil {
			return ctx, err
		}
		if _, err := env.DB.Ent().Practice.Delete().Exec(cctx); err != nil {
			return ctx, err
		}
		return ctx, nil
	})

	sc.Step(`^the API is running$`, p.apiIsRunning)
	sc.Step(`^I register a doctor with email "([^"]+)" password "([^"]+)" displayName "([^"]+)"$`, p.registerDoctorArgs)
	sc.Step(`^I create a practice:$`, p.createPractice)
	sc.Step(`^the response status should be (\d+)$`, p.statusShouldBe)
	sc.Step(`^the response JSON field "([^"]+)" should be "([^"]+)"$`, p.jsonFieldShouldBe)
	sc.Step(`^the current doctor is assigned to the created practice$`, p.doctorAssignedToPractice)
	sc.Step(`^the current doctor role is "([^"]+)"$`, p.doctorRoleIs)
}

func (p *practiceFeature) apiIsRunning() error {
	if err := p.client.Get("/ready"); err != nil {
		return err
	}
	return p.client.RequireStatus(http.StatusOK)
}

func (p *practiceFeature) registerDoctorArgs(email, password, displayName string) error {
	if err := p.client.PostJSON("/doctor/register", map[string]string{
		"email":       email,
		"password":    password,
		"displayName": displayName,
	}); err != nil {
		return err
	}
	if err := p.client.RequireStatus(http.StatusCreated); err != nil {
		return err
	}
	id, err := bddtest.ExtractField(p.client.LastBody, "doctor.id")
	if err != nil {
		return err
	}
	p.doctorID = id
	return nil
}

func (p *practiceFeature) createPractice(table *godog.Table) error {
	payload := bddtest.TableToMap(table)
	if err := p.client.PostJSON("/practice", payload); err != nil {
		return err
	}
	if err := p.client.RequireStatus(http.StatusCreated); err != nil {
		return err
	}
	id, err := bddtest.ExtractField(p.client.LastBody, "practice.id")
	if err == nil {
		p.practiceID = id
	}
	return nil
}

func (p *practiceFeature) statusShouldBe(code int) error {
	return p.client.RequireStatus(code)
}

func (p *practiceFeature) jsonFieldShouldBe(field, expected string) error {
	got, err := bddtest.ExtractField(p.client.LastBody, field)
	if err != nil {
		return err
	}
	if got != expected {
		return fmt.Errorf("expected %s to be %s, got %s", field, expected, got)
	}
	return nil
}

func (p *practiceFeature) doctorAssignedToPractice() error {
	if p.doctorID == "" {
		return fmt.Errorf("doctor not registered")
	}
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	docUUID, err := uuid.Parse(p.doctorID)
	if err != nil {
		return err
	}

	doc, err := p.env.DB.Ent().Doctor.Get(ctx, docUUID)
	if err != nil {
		return err
	}

	if doc.PracticeID == nil {
		return fmt.Errorf("practice_id not set on doctor")
	}

	if p.practiceID == "" {
		p.practiceID = doc.PracticeID.String()
	}

	if doc.PracticeID.String() != p.practiceID {
		return fmt.Errorf("expected practice_id %s, got %s", p.practiceID, doc.PracticeID.String())
	}

	return nil
}

func (p *practiceFeature) doctorRoleIs(role string) error {
	if p.doctorID == "" {
		return fmt.Errorf("doctor not registered")
	}

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	docUUID, err := uuid.Parse(p.doctorID)
	if err != nil {
		return err
	}

	doc, err := p.env.DB.Ent().Doctor.Get(ctx, docUUID)
	if err != nil {
		return err
	}

	if doc.Role.String() != role {
		return fmt.Errorf("expected role %s, got %s", role, doc.Role)
	}

	if role == doctor.RoleOwner.String() && doc.PracticeID == nil {
		return fmt.Errorf("owner should be assigned a practice")
	}

	return nil
}
