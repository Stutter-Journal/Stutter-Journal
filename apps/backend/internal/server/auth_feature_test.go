package server

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/http/cookiejar"
	"net/http/httptest"
	"os"
	"path/filepath"
	"strings"
	"testing"
	"time"

	"backend/ent/doctor"
	"backend/internal/auth"
	"backend/internal/database"

	"github.com/charmbracelet/log"
	"github.com/cucumber/godog"
	"github.com/gorilla/securecookie"
	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/modules/postgres"
	"github.com/testcontainers/testcontainers-go/wait"
)

func TestAuthFeatures(t *testing.T) {
	env := startTestEnv(t)

	opts := &godog.Options{
		Format:   "pretty",
		Strict:   true,
		Paths:    []string{filepath.Join("..", "..", "features", "auth.feature")},
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

type testEnv struct {
	baseURL   string
	db        *database.Client
	container testcontainers.Container
	server    *httptest.Server
}

type authFeature struct {
	env    *testEnv
	client *http.Client
	resp   *http.Response
	body   []byte
}

func initAuthScenario(sc *godog.ScenarioContext, env *testEnv) {
	state := &authFeature{env: env}

	sc.Before(func(ctx context.Context, sc *godog.Scenario) (context.Context, error) {
		if err := state.reset(); err != nil {
			return ctx, err
		}
		return ctx, nil
	})

	sc.Step(`^the API is running$`, state.theApiIsRunning)

	// Old string-based step
	sc.Step(`^I register a doctor with email "([^"]+)" password "([^"]+)" displayName "([^"]+)"$`, state.registerDoctor)

	// NEW: Table-based registration
	sc.Step(`^I register a doctor:$`, state.registerDoctorWithTable)
	sc.Step(`^I register a doctor with the following details:$`, state.registerDoctorWithTable)

	sc.Step(`^the response status should be (\d+)$`, state.responseStatusShouldBe)
	sc.Step(`^the doctor with email "([^"]+)" exists in the database$`, state.doctorExistsInDatabase)
	sc.Step(`^the response JSON field "([^"]+)" should be "([^"]+)"$`, state.responseJsonFieldShouldBe)

	// NEW: Table-based response matching
	sc.Step(`^the response should match:$`, state.responseFieldsShouldMatch)
	sc.Step(`^the response should contain:$`, state.responseFieldsShouldMatch)

	// Old string-based login
	sc.Step(`^I log in as doctor with email "([^"]+)" password "([^"]+)"$`, state.loginDoctor)

	// NEW: Table-based login
	sc.Step(`^I log in with:$`, state.loginDoctorWithTable)
	sc.Step(`^I log in with credentials:$`, state.loginDoctorWithTable)

	sc.Step(`^I call GET "([^"]+)"$`, state.callGet)
	sc.Step(`^I log out$`, state.logout)
	sc.Step(`^the response should be unauthorized$`, state.responseShouldBeUnauthorized)
}

// Register doctor using a table
func (a *authFeature) registerDoctorWithTable(table *godog.Table) error {
	data := tableToMap(table)

	payload := map[string]string{
		"email":       data["email"],
		"password":    data["password"],
		"displayName": data["displayName"],
	}

	return a.postJson("/doctor/register", payload)
}

// Login doctor using a table
func (a *authFeature) loginDoctorWithTable(table *godog.Table) error {
	data := tableToMap(table)

	payload := map[string]string{
		"email":    data["email"],
		"password": data["password"],
	}

	return a.postJson("/doctor/login", payload)
}

// Verify multiple response fields from a table
func (a *authFeature) responseFieldsShouldMatch(table *godog.Table) error {
	if a.body == nil {
		return fmt.Errorf("no response body")
	}

	data := tableToMap(table)

	for fieldPath, expected := range data {
		got, err := extractField(a.body, fieldPath)
		if err != nil {
			return fmt.Errorf("field %s: %w", fieldPath, err)
		}
		if got != expected {
			return fmt.Errorf("field %s: expected %s, got %s", fieldPath, expected, got)
		}
	}

	return nil
}

// Helper function to convert godog table to map
func tableToMap(table *godog.Table) map[string]string {
	data := make(map[string]string)

	// Assuming table format is:
	// | field    | value |
	// | email    | test@example.com |
	// | password | secret |

	for i, row := range table.Rows {
		if i == 0 {
			continue // Skip header row
		}
		if len(row.Cells) >= 2 {
			key := row.Cells[0].Value
			value := row.Cells[1].Value
			data[key] = value
		}
	}

	return data
}

func (a *authFeature) reset() error {
	jar, _ := cookiejar.New(nil)
	a.client = &http.Client{Jar: jar, Timeout: 10 * time.Second}
	a.resp = nil
	a.body = nil

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	if _, err := a.env.db.Ent().Doctor.Delete().Exec(ctx); err != nil {
		return fmt.Errorf("clean doctors: %w", err)
	}
	return nil
}

func (a *authFeature) theApiIsRunning() error {
	if err := a.get("/ready"); err != nil {
		return err
	}
	if a.resp.StatusCode != http.StatusOK {
		return fmt.Errorf("expected ready 200, got %d: %s", a.resp.StatusCode, string(a.body))
	}
	return nil
}

func (a *authFeature) registerDoctor(email, password, displayName string) error {
	payload := map[string]string{
		"email":       email,
		"password":    password,
		"displayName": displayName,
	}
	return a.postJson("/doctor/register", payload)
}

func (a *authFeature) responseStatusShouldBe(code int) error {
	if a.resp == nil {
		return fmt.Errorf("no response recorded")
	}
	if a.resp.StatusCode != code {
		return fmt.Errorf("expected status %d, got %d: %s", code, a.resp.StatusCode, string(a.body))
	}
	return nil
}

func (a *authFeature) doctorExistsInDatabase(email string) error {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	_, err := a.env.db.Ent().Doctor.Query().Where(doctor.EmailEQ(strings.ToLower(email))).Only(ctx)
	if err != nil {
		return fmt.Errorf("doctor not found: %w", err)
	}
	return nil
}

func (a *authFeature) responseJsonFieldShouldBe(fieldPath, expected string) error {
	if a.body == nil {
		return fmt.Errorf("no response body")
	}
	got, err := extractField(a.body, fieldPath)
	if err != nil {
		return err
	}
	if got != expected {
		return fmt.Errorf("expected %s to be %s, got %s", fieldPath, expected, got)
	}
	return nil
}

func (a *authFeature) loginDoctor(email, password string) error {
	payload := map[string]string{"email": email, "password": password}
	return a.postJson("/doctor/login", payload)
}

func (a *authFeature) callGet(path string) error {
	return a.get(path)
}

func (a *authFeature) logout() error {
	return a.postJson("/doctor/logout", nil)
}

func (a *authFeature) responseShouldBeUnauthorized() error {
	if a.resp == nil {
		return fmt.Errorf("no response recorded")
	}
	if a.resp.StatusCode != http.StatusUnauthorized {
		return fmt.Errorf("expected status 401, got %d: %s", a.resp.StatusCode, string(a.body))
	}
	return nil
}

func (a *authFeature) get(path string) error {
	req, err := http.NewRequest(http.MethodGet, a.env.baseURL+path, nil)
	if err != nil {
		return err
	}
	return a.do(req)
}

func (a *authFeature) postJson(path string, payload any) error {
	var body io.Reader
	if payload != nil {
		buf, err := json.Marshal(payload)
		if err != nil {
			return err
		}
		body = strings.NewReader(string(buf))
	}

	req, err := http.NewRequest(http.MethodPost, a.env.baseURL+path, body)
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	return a.do(req)
}

func (a *authFeature) do(req *http.Request) error {
	resp, err := a.client.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return err
	}

	a.resp = resp
	a.body = body

	return nil
}

func extractField(body []byte, path string) (string, error) {
	var data any
	if err := json.Unmarshal(body, &data); err != nil {
		return "", err
	}

	current := data
	parts := strings.SplitSeq(path, ".")
	for part := range parts {
		m, ok := current.(map[string]any)
		if !ok {
			return "", fmt.Errorf("invalid path %s", path)
		}
		val, ok := m[part]
		if !ok {
			return "", fmt.Errorf("field %s missing", path)
		}
		current = val
	}

	return fmt.Sprint(current), nil
}

func projectRoot() string {
	wd, err := os.Getwd()
	if err != nil {
		return "."
	}

	dir := wd
	for range 5 {
		if _, err := os.Stat(filepath.Join(dir, "go.mod")); err == nil {
			return dir
		}
		parent := filepath.Dir(dir)
		if parent == dir {
			break
		}
		dir = parent
	}
	return wd
}

func startTestEnv(t *testing.T) *testEnv {
	t.Helper()
	ctx := context.Background()

	dbName := "authdb"
	dbUser := "authuser"
	dbPwd := "authpass"

	container, err := postgres.Run(
		ctx,
		"postgres:latest",
		postgres.WithDatabase(dbName),
		postgres.WithUsername(dbUser),
		postgres.WithPassword(dbPwd),
		testcontainers.WithWaitStrategy(
			wait.ForLog("database system is ready to accept connections").
				WithOccurrence(2).
				WithStartupTimeout(30*time.Second),
		),
	)
	if err != nil {
		t.Fatalf("start postgres container: %v", err)
	}

	host, err := container.Host(ctx)
	if err != nil {
		t.Fatalf("get container host: %v", err)
	}

	mappedPort, err := container.MappedPort(ctx, "5432/tcp")
	if err != nil {
		t.Fatalf("get container port: %v", err)
	}

	logger := log.NewWithOptions(io.Discard, log.Options{})
	migrationsDir := filepath.Join(projectRoot(), "ent", "migrate", "migrations")
	dbCfg := database.Config{
		Host:            host,
		Port:            mappedPort.Port(),
		Username:        dbUser,
		Password:        dbPwd,
		Database:        dbName,
		Schema:          "public",
		Environment:     "test",
		ApplyMigrations: true,
		MigrationDir:    fmt.Sprintf("file://%s", migrationsDir),
	}

	dbClient, err := database.NewWithConfig(ctx, dbCfg, logger)
	if err != nil {
		container.Terminate(ctx)
		t.Fatalf("create database client: %v", err)
	}

	_ = os.Setenv("AUTH_COOKIE_SECRET", "super-secrect-integration-secret-if-isg-wouldn't-suck")
	authCfg, err := auth.LoadConfig(logger)
	if err != nil {
		container.Terminate(ctx)
		t.Fatalf("load auth config: %v", err)
	}

	if len(authCfg.SecretKey) == 0 {
		authCfg.SecretKey = securecookie.GenerateRandomKey(32)
	}

	authManager, err := auth.NewManager(authCfg)
	if err != nil {
		container.Terminate(ctx)
		t.Fatalf("init auth manager: %v", err)
	}

	s := &Server{
		db:   dbClient,
		auth: authManager,
	}

	ts := httptest.NewServer(s.RegisterRoutes())

	t.Cleanup(func() {
		ts.Close()
		dbClient.Close()
		container.Terminate(context.Background())
	})

	return &testEnv{
		baseURL:   ts.URL,
		db:        dbClient,
		container: container,
		server:    ts,
	}
}
