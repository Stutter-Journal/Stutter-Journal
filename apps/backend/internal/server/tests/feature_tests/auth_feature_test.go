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

	"backend/ent/doctor"
	"backend/internal/auth"
	"backend/internal/database"
	"backend/internal/server"
	"backend/internal/server/bddtest"

	"github.com/charmbracelet/log"
	"github.com/cucumber/godog"
	"github.com/gorilla/securecookie"
)

func TestAuthFeatures(t *testing.T) {
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

	// TODO: This is crap, I have no idea from which directory the go tests are being executed and what current working directory we're operating from, so I don't want to rely on relative paths, but rather absolute paths... This also applies to other feature tests
	opts := &godog.Options{
		Format:   "pretty",
		Strict:   true,
		Paths:    []string{filepath.Join("..", "..", "..", "..", "features", "auth.feature")},
		TestingT: t,
	}

	suite := godog.TestSuite{
		Name:                "auth",
		ScenarioInitializer: func(sc *godog.ScenarioContext) { initAuthScenario(sc, env) },
		Options:             opts,
	}

	if suite.Run() != 0 {
		t.Fatalf("godog suite failed")
	}
}

type authFeature struct {
	env    *bddtest.Env
	client *bddtest.Client
}

func initAuthScenario(sc *godog.ScenarioContext, env *bddtest.Env) {
	s := &authFeature{env: env}

	sc.Before(func(ctx context.Context, _ *godog.Scenario) (context.Context, error) {
		s.client = bddtest.NewClient(env.BaseURL)

		// clean slate
		cctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
		defer cancel()
		if _, err := env.DB.Ent().Doctor.Delete().Exec(cctx); err != nil {
			return ctx, fmt.Errorf("clean doctors: %w", err)
		}
		return ctx, nil
	})

	sc.Step(`^the API is running$`, s.apiIsRunning)

	sc.Step(`^I register a doctor:$`, s.registerDoctorTable)
	sc.Step(`^I register a doctor with the following details:$`, s.registerDoctorTable)
	sc.Step(`^I register a doctor with email "([^"]+)" password "([^"]+)" displayName "([^"]+)"$`, s.registerDoctorArgs)

	sc.Step(`^I log in with:$`, s.loginTable)
	sc.Step(`^I log in with credentials:$`, s.loginTable)
	sc.Step(`^I log in as doctor with email "([^"]+)" password "([^"]+)"$`, s.loginArgs)

	sc.Step(`^I call GET "([^"]+)"$`, s.callGet)
	sc.Step(`^I log out$`, s.logout)

	sc.Step(`^the response status should be (\d+)$`, s.statusShouldBe)
	sc.Step(`^the response should be unauthorized$`, s.unauthorized)
	sc.Step(`^the doctor with email "([^"]+)" exists in the database$`, s.doctorExists)

	sc.Step(`^the response JSON field "([^"]+)" should be "([^"]+)"$`, s.jsonFieldShouldBe)
	sc.Step(`^the response field "([^"]+)" should be "([^"]+)"$`, s.jsonFieldShouldBe)
	sc.Step(`^the response should match:$`, s.responseMatchesTable)
	sc.Step(`^the response should contain:$`, s.responseMatchesTable)
}

func (a *authFeature) apiIsRunning() error {
	if err := a.client.Get("/ready"); err != nil {
		return err
	}
	return a.client.RequireStatus(200)
}

func (a *authFeature) registerDoctorTable(table *godog.Table) error {
	data := bddtest.TableToMap(table)
	return a.client.PostJSON("/doctor/register", map[string]string{
		"email":       data["email"],
		"password":    data["password"],
		"displayName": data["displayName"],
	})
}

func (a *authFeature) registerDoctorArgs(email, password, displayName string) error {
	return a.client.PostJSON("/doctor/register", map[string]string{
		"email":       email,
		"password":    password,
		"displayName": displayName,
	})
}

func (a *authFeature) loginTable(table *godog.Table) error {
	data := bddtest.TableToMap(table)
	return a.client.PostJSON("/doctor/login", map[string]string{
		"email":    data["email"],
		"password": data["password"],
	})
}

func (a *authFeature) loginArgs(email, password string) error {
	return a.client.PostJSON("/doctor/login", map[string]string{
		"email":    email,
		"password": password,
	})
}

func (a *authFeature) callGet(path string) error { return a.client.Get(path) }
func (a *authFeature) logout() error             { return a.client.PostJSON("/doctor/logout", nil) }

func (a *authFeature) statusShouldBe(code int) error { return a.client.RequireStatus(code) }

func (a *authFeature) unauthorized() error {
	if err := a.client.RequireStatus(401); err != nil {
		return err
	}
	return nil
}

func (a *authFeature) doctorExists(email string) error {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	_, err := a.env.DB.Ent().Doctor.
		Query().
		Where(doctor.EmailEQ(strings.ToLower(email))).
		Only(ctx)
	if err != nil {
		return fmt.Errorf("doctor not found: %w", err)
	}
	return nil
}

func (a *authFeature) jsonFieldShouldBe(path, expected string) error {
	if a.client.LastBody == nil {
		return fmt.Errorf("no response body")
	}
	got, err := bddtest.ExtractField(a.client.LastBody, path)
	if err != nil {
		return err
	}
	if got != expected {
		return fmt.Errorf("expected %s to be %s, got %s", path, expected, got)
	}
	return nil
}

func (a *authFeature) responseMatchesTable(table *godog.Table) error {
	if a.client.LastBody == nil {
		return fmt.Errorf("no response body")
	}

	for path, expected := range bddtest.TableToMap(table) {
		got, err := bddtest.ExtractField(a.client.LastBody, path)
		if err != nil {
			return fmt.Errorf("field %s: %w", path, err)
		}
		if got != expected {
			return fmt.Errorf("field %s: expected %s, got %s", path, expected, got)
		}
	}
	return nil
}
