package tests

import (
	"context"
	"io"
	"net/http"
	"os"
	"testing"
	"time"

	"backend/ent/doctorpatientlink"
	"backend/ent/entryshare"
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

func TestPatientEntriesSync_UploadSharesWithApprovedDoctor(t *testing.T) {
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

	patientClient := bddtest.NewClient(env.BaseURL)
	doctorClient := bddtest.NewClient(env.BaseURL)

	if err := doctorClient.PostJSON("/doctor/register", map[string]string{
		"email":       "syncdoctor@example.com",
		"password":    "SuperSecret1",
		"displayName": "Sync Doctor",
	}); err != nil {
		t.Fatalf("register doctor: %v", err)
	}
	if err := doctorClient.RequireStatus(http.StatusCreated); err != nil {
		t.Fatalf("register doctor status: %v", err)
	}
	doctorIDStr, err := bddtest.ExtractField(doctorClient.LastBody, "doctor.id")
	if err != nil {
		t.Fatalf("extract doctor id: %v", err)
	}
	doctorID, err := uuid.Parse(doctorIDStr)
	if err != nil {
		t.Fatalf("parse doctor id: %v", err)
	}

	if err := patientClient.PostJSON("/patient/register", map[string]string{
		"email":       "syncpatientshare@example.com",
		"password":    "SuperSecret1",
		"displayName": "Sync Patient Share",
	}); err != nil {
		t.Fatalf("register patient: %v", err)
	}
	if err := patientClient.RequireStatus(http.StatusCreated); err != nil {
		t.Fatalf("register patient status: %v", err)
	}
	patientIDStr, err := bddtest.ExtractField(patientClient.LastBody, "patient.id")
	if err != nil {
		t.Fatalf("extract patient id: %v", err)
	}
	patientID, err := uuid.Parse(patientIDStr)
	if err != nil {
		t.Fatalf("parse patient id: %v", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	if _, err := env.DB.Ent().DoctorPatientLink.
		Create().
		SetDoctorID(doctorID).
		SetPatientID(patientID).
		SetStatus(doctorpatientlink.StatusApproved).
		SetRequestedAt(time.Now()).
		SetApprovedAt(time.Now()).
		SetApprovedByDoctorID(doctorID).
		Save(ctx); err != nil {
		t.Fatalf("create doctor-patient link: %v", err)
	}

	if err := patientClient.PostJSON("/patient/login", map[string]string{
		"email":    "syncpatientshare@example.com",
		"password": "SuperSecret1",
	}); err != nil {
		t.Fatalf("patient login: %v", err)
	}
	if err := patientClient.RequireStatus(http.StatusOK); err != nil {
		t.Fatalf("login status: %v", err)
	}

	entryID := uuid.New()
	createdAt := time.Date(2026, 2, 18, 11, 42, 9, 123000000, time.UTC)
	happenedAt := createdAt
	updatedAt := createdAt

	payload := map[string]any{
		"entries": []map[string]any{
			{
				"id":         entryID.String(),
				"createdAt":  createdAt,
				"happenedAt": happenedAt,
				"notes":      "Entry 2026-02-18\n\nshared",
				"tags":       []string{"date:2026-02-18", "intensity:3"},
				"updatedAt":  updatedAt,
			},
		},
	}

	if err := patientClient.PostJSON("/patient/entries/sync", payload); err != nil {
		t.Fatalf("sync post: %v", err)
	}
	if err := patientClient.RequireStatus(http.StatusOK); err != nil {
		t.Fatalf("sync status: %v", err)
	}

	share, err := env.DB.Ent().EntryShare.
		Query().
		Where(
			entryshare.EntryIDEQ(entryID),
			entryshare.SharedWithDoctorIDEQ(doctorID),
		).
		Only(ctx)
	if err != nil {
		t.Fatalf("entry share not found in db: %v", err)
	}
	if share.SharedByPatientID != patientID {
		t.Fatalf("expected shared_by_patient_id %s, got %s", patientID, share.SharedByPatientID)
	}
	if share.RevokedAt != nil {
		t.Fatalf("expected share to be active, got revoked_at %v", share.RevokedAt)
	}
}
