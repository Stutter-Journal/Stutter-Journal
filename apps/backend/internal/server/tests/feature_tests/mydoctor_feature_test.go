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

func TestMyDoctorFeatures(t *testing.T) {
	env := bddtest.NewEnv(t, func(db *database.Client) (http.Handler, error) {
		logger := log.NewWithOptions(io.Discard, log.Options{})
		_ = os.Setenv("AUTH_COOKIE_SECRET", "super-secret-integration-secret-for-mydoctor-tests")

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
		Paths:    []string{filepath.Join("..", "..", "..", "..", "features", "mydoctor.feature")},
		TestingT: t,
	}

	suite := godog.TestSuite{
		Name:                "mydoctor",
		ScenarioInitializer: func(sc *godog.ScenarioContext) { initMyDoctorScenario(sc, env) },
		Options:             opts,
	}

	if suite.Run() != 0 {
		t.Fatalf("godog suite failed")
	}
}

type myDoctorFeature struct {
	env *bddtest.Env

	doctorClient  *bddtest.Client
	patientClient *bddtest.Client
	lastClient    *bddtest.Client

	pairingCode string
}

func initMyDoctorScenario(sc *godog.ScenarioContext, env *bddtest.Env) {
	f := &myDoctorFeature{env: env}

	sc.Before(func(ctx context.Context, _ *godog.Scenario) (context.Context, error) {
		f.doctorClient = bddtest.NewClient(env.BaseURL)
		f.patientClient = bddtest.NewClient(env.BaseURL)
		f.lastClient = nil
		f.pairingCode = ""

		cctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
		defer cancel()
		if _, err := env.DB.Ent().DoctorPatientLink.Delete().Exec(cctx); err != nil {
			return ctx, err
		}
		if _, err := env.DB.Ent().PairingCode.Delete().Exec(cctx); err != nil {
			return ctx, err
		}
		if _, err := env.DB.Ent().Patient.Delete().Exec(cctx); err != nil {
			return ctx, err
		}
		if _, err := env.DB.Ent().Doctor.Delete().Exec(cctx); err != nil {
			return ctx, err
		}
		if _, err := env.DB.Ent().Practice.Delete().Exec(cctx); err != nil {
			return ctx, err
		}

		return ctx, nil
	})

	sc.Step(`^the API is running$`, f.apiIsRunning)
	sc.Step(`^I register a doctor with email "([^"]+)" password "([^"]+)" displayName "([^"]+)"$`, f.registerDoctor)
	sc.Step(`^I create a practice:$`, f.createPractice)
	sc.Step(`^I create a pairing code$`, f.createPairingCode)
	sc.Step(`^I register a patient with email "([^"]+)" password "([^"]+)" displayName "([^"]+)"$`, f.registerPatient)
	sc.Step(`^I redeem the pairing code$`, f.redeemPairingCode)
	sc.Step(`^I invite the patient by email:$`, f.invitePatientByEmail)
	sc.Step(`^I call patient GET "([^"]+)"$`, f.callPatientGet)
	sc.Step(`^the response status should be (\d+)$`, f.statusShouldBe)
	sc.Step(`^the response JSON field "([^"]+)" should be "([^"]+)"$`, f.jsonFieldShouldBe)
}

func (f *myDoctorFeature) apiIsRunning() error {
	if err := f.doctorClient.Get("/ready"); err != nil {
		return err
	}
	f.lastClient = f.doctorClient
	return f.doctorClient.RequireStatus(http.StatusOK)
}

func (f *myDoctorFeature) registerDoctor(email, password, displayName string) error {
	if err := f.doctorClient.PostJSON("/doctor/register", map[string]string{
		"email":       email,
		"password":    password,
		"displayName": displayName,
	}); err != nil {
		return err
	}
	f.lastClient = f.doctorClient
	return f.doctorClient.RequireStatus(http.StatusCreated)
}

func (f *myDoctorFeature) createPractice(table *godog.Table) error {
	payload := bddtest.TableToMap(table)
	if err := f.doctorClient.PostJSON("/practice", payload); err != nil {
		return err
	}
	f.lastClient = f.doctorClient
	return f.doctorClient.RequireStatus(http.StatusCreated)
}

func (f *myDoctorFeature) createPairingCode() error {
	if err := f.doctorClient.PostJSON("/links/pairing-code", nil); err != nil {
		return err
	}
	f.lastClient = f.doctorClient
	if err := f.doctorClient.RequireStatus(http.StatusCreated); err != nil {
		return err
	}
	code, err := bddtest.ExtractField(f.doctorClient.LastBody, "code")
	if err != nil {
		return err
	}
	f.pairingCode = code
	return nil
}

func (f *myDoctorFeature) registerPatient(email, password, displayName string) error {
	if err := f.patientClient.PostJSON("/patient/register", map[string]string{
		"email":       email,
		"password":    password,
		"displayName": displayName,
	}); err != nil {
		return err
	}
	f.lastClient = f.patientClient
	return f.patientClient.RequireStatus(http.StatusCreated)
}

func (f *myDoctorFeature) redeemPairingCode() error {
	if f.pairingCode == "" {
		return fmt.Errorf("no pairing code available")
	}
	if err := f.patientClient.PostJSON("/links/pairing-code/redeem", map[string]string{
		"code": f.pairingCode,
	}); err != nil {
		return err
	}
	f.lastClient = f.patientClient
	return f.patientClient.RequireStatus(http.StatusOK)
}

func (f *myDoctorFeature) invitePatientByEmail(table *godog.Table) error {
	payload := bddtest.TableToMap(table)
	if err := f.doctorClient.PostJSON("/links/invite", payload); err != nil {
		return err
	}
	f.lastClient = f.doctorClient
	return f.doctorClient.RequireStatus(http.StatusCreated)
}

func (f *myDoctorFeature) callPatientGet(path string) error {
	if err := f.patientClient.Get(path); err != nil {
		return err
	}
	f.lastClient = f.patientClient
	return nil
}

func (f *myDoctorFeature) statusShouldBe(code int) error {
	if f.lastClient == nil {
		return fmt.Errorf("no response recorded")
	}
	return f.lastClient.RequireStatus(code)
}

func (f *myDoctorFeature) jsonFieldShouldBe(field, expected string) error {
	if f.lastClient == nil || f.lastClient.LastBody == nil {
		return fmt.Errorf("no response body")
	}
	got, err := bddtest.ExtractField(f.lastClient.LastBody, field)
	if err != nil {
		return err
	}
	if got != expected {
		return fmt.Errorf("expected %s to be %s, got %s", field, expected, got)
	}
	return nil
}
