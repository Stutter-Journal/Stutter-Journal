package tests

import (
	"context"
	"io"
	"net/http"
	"os"
	"testing"
	"time"

	"backend/internal/auth"
	"backend/internal/database"
	"backend/internal/server"
	"backend/internal/server/bddtest"

	"github.com/charmbracelet/log"
	"github.com/google/uuid"
	"github.com/gorilla/securecookie"
)

func TestPatientEntriesSync_UploadWritesToDB(t *testing.T) {
	env := bddtest.NewEnv(t, func(db *database.Client) (http.Handler, error) {
		logger := log.NewWithOptions(io.Discard, log.Options{})
		_ = os.Setenv("AUTH_COOKIE_SECRET", "super-secret-integration-secret-for-sync-test-minimum-32-bytes")

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

		s := &server.Server{Db: db, Auth: authManager}
		return s.RegisterRoutes(), nil
	})

	client := bddtest.NewClient(env.BaseURL)

	if err := client.PostJSON("/patient/register", map[string]string{
		"email":       "syncpatient@example.com",
		"password":    "SuperSecret1",
		"displayName": "Sync Patient",
	}); err != nil {
		t.Fatalf("register patient: %v", err)
	}
	if err := client.RequireStatus(http.StatusCreated); err != nil {
		t.Fatalf("register status: %v", err)
	}
	patientIDStr, err := bddtest.ExtractField(client.LastBody, "patient.id")
	if err != nil {
		t.Fatalf("extract patient id: %v", err)
	}
	patientID, err := uuid.Parse(patientIDStr)
	if err != nil {
		t.Fatalf("parse patient id: %v", err)
	}

	if err := client.PostJSON("/patient/login", map[string]string{
		"email":    "syncpatient@example.com",
		"password": "SuperSecret1",
	}); err != nil {
		t.Fatalf("patient login: %v", err)
	}
	if err := client.RequireStatus(http.StatusOK); err != nil {
		t.Fatalf("login status: %v", err)
	}

	entryID := uuid.New()
	createdAt := time.Date(2026, 1, 17, 16, 19, 58, 335000000, time.UTC)
	happenedAt := createdAt
	updatedAt := createdAt

	payload := map[string]any{
		"entries": []map[string]any{
			{
				"id":        entryID.String(),
				"createdAt": createdAt,
				"happenedAt": happenedAt,
				"notes":     "Entry 2026-01-17\n\nasdf",
				"tags":      []string{"date:2026-01-17", "intensity:5"},
				"updatedAt": updatedAt,
			},
		},
	}

	if err := client.PostJSON("/patient/entries/sync", payload); err != nil {
		t.Fatalf("sync post: %v", err)
	}
	if err := client.RequireStatus(http.StatusOK); err != nil {
		t.Fatalf("sync status: %v", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	e, err := env.DB.Ent().Entry.Get(ctx, entryID)
	if err != nil {
		t.Fatalf("entry not found in db: %v", err)
	}
	if e.PatientID != patientID {
		t.Fatalf("expected patient_id %s, got %s", patientID, e.PatientID)
	}
	if !e.HappenedAt.Equal(happenedAt) {
		t.Fatalf("expected happened_at %s, got %s", happenedAt, e.HappenedAt)
	}
	if e.Notes == nil || *e.Notes != "Entry 2026-01-17\n\nasdf" {
		t.Fatalf("expected notes to be set, got %#v", e.Notes)
	}
	if len(e.Tags) != 2 {
		t.Fatalf("expected 2 tags, got %v", e.Tags)
	}
}
