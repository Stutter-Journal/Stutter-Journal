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

	"backend/internal/auth"
	"backend/internal/database"
	"backend/internal/server"
	"backend/internal/server/bddtest"

	"github.com/charmbracelet/log"
	"github.com/cucumber/godog"
	"github.com/gorilla/securecookie"
)

func TestLinkFeatures(t *testing.T) {
	env := bddtest.NewEnv(t, func(db *database.Client) (http.Handler, error) {
		logger := log.NewWithOptions(io.Discard, log.Options{})
		_ = os.Setenv("AUTH_COOKIE_SECRET", "super-secrect-integration-secret-if-isg-wouldn't-suck")

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
		Paths:    []string{filepath.Join("..", "..", "..", "..", "features", "linking.feature")},
		TestingT: t,
	}

	suite := godog.TestSuite{
		Name:                "linking",
		ScenarioInitializer: func(sc *godog.ScenarioContext) { initLinkScenario(sc, env) },
		Options:             opts,
	}

	if suite.Run() != 0 {
		t.Fatalf("godog suite failed")
	}
}

type linkFeature struct {
	env       *bddtest.Env
	client    *bddtest.Client
	pendingID string
}

func initLinkScenario(sc *godog.ScenarioContext, env *bddtest.Env) {
	lf := &linkFeature{env: env}

	sc.Before(func(ctx context.Context, _ *godog.Scenario) (context.Context, error) {
		lf.client = bddtest.NewClient(env.BaseURL)
		lf.pendingID = ""

		cctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
		defer cancel()
		if _, err := env.DB.Ent().Doctor.Delete().Exec(cctx); err != nil {
			return ctx, err
		}
		if _, err := env.DB.Ent().Patient.Delete().Exec(cctx); err != nil {
			return ctx, err
		}
		if _, err := env.DB.Ent().DoctorPatientLink.Delete().Exec(cctx); err != nil {
			return ctx, err
		}
		return ctx, nil
	})

	sc.Step(`^the API is running$`, lf.apiIsRunning)
	sc.Step(`^I register a doctor with email "([^"]+)" password "([^"]+)" displayName "([^"]+)"$`, lf.registerDoctorArgs)
	sc.Step(`^I invite a patient:$`, lf.invitePatient)
	sc.Step(`^I call GET "([^"]+)"$`, lf.callGet)
	sc.Step(`^I approve the pending link$`, lf.approvePendingLink)
	sc.Step(`^the response status should be (\d+)$`, lf.statusShouldBe)
	sc.Step(`^the response JSON field "([^"]+)" should be "([^"]+)"$`, lf.jsonFieldShouldBe)
}

func (lf *linkFeature) apiIsRunning() error {
	if err := lf.client.Get("/ready"); err != nil {
		return err
	}
	return lf.client.RequireStatus(http.StatusOK)
}

func (lf *linkFeature) registerDoctorArgs(email, password, displayName string) error {
	if err := lf.client.PostJSON("/doctor/register", map[string]string{
		"email":       email,
		"password":    password,
		"displayName": displayName,
	}); err != nil {
		return err
	}
	return lf.client.RequireStatus(http.StatusCreated)
}

func (lf *linkFeature) invitePatient(table *godog.Table) error {
	payload := bddtest.TableToMap(table)
	if err := lf.client.PostJSON("/links/invite", payload); err != nil {
		return err
	}
	if err := lf.client.RequireStatus(http.StatusCreated); err != nil {
		return err
	}
	if id, err := bddtest.ExtractField(lf.client.LastBody, "link.id"); err == nil {
		lf.pendingID = id
	}
	return nil
}

func (lf *linkFeature) callGet(path string) error {
	if err := lf.client.Get(path); err != nil {
		return err
	}
	return nil
}

func (lf *linkFeature) approvePendingLink() error {
	if lf.pendingID == "" {
		return fmt.Errorf("no pending link recorded")
	}
	endpoint := fmt.Sprintf("/links/%s/approve", lf.pendingID)
	if err := lf.client.PostJSON(endpoint, nil); err != nil {
		return err
	}
	return lf.client.RequireStatus(http.StatusOK)
}

func (lf *linkFeature) statusShouldBe(code int) error {
	return lf.client.RequireStatus(code)
}

func (lf *linkFeature) jsonFieldShouldBe(field, expected string) error {
	got, err := bddtest.ExtractField(lf.client.LastBody, field)
	if err != nil {
		return err
	}
	if got != expected {
		return fmt.Errorf("expected %s to be %s, got %s", field, expected, got)
	}
	return nil
}
