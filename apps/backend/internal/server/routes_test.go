package server

import (
	"context"
	"encoding/json"
	"io"
	"net/http"
	"net/http/httptest"
	"testing"

	"backend/ent"
)

type fakeDB struct {
	err error
}

func (f *fakeDB) Ping(ctx context.Context) error {
	return f.err
}

func (f *fakeDB) Ent() *ent.Client {
	return nil
}

func TestHelloWorldHandler(t *testing.T) {
	s := &Server{}
	req := httptest.NewRequest(http.MethodGet, "/", nil)
	w := httptest.NewRecorder()

	s.HelloWorldHandler(w, req)

	resp := w.Result()
	defer func() {
		_ = resp.Body.Close()
	}()

	if resp.StatusCode != http.StatusOK {
		t.Fatalf("expected status OK; got %v", resp.Status)
	}

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		t.Fatalf("error reading response body. Err: %v", err)
	}

	expected := `{"message":"Hello World"}` + "\n"
	if expected != string(body) {
		t.Fatalf("expected response body to be %v; got %v", expected, string(body))
	}
}

func TestReadyHandler_Success(t *testing.T) {
	s := &Server{Db: &fakeDB{}}
	req := httptest.NewRequest(http.MethodGet, "/ready", nil)
	w := httptest.NewRecorder()

	s.readyHandler(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Fatalf("expected status OK; got %v", w.Result().Status)
	}

	var payload map[string]string
	if err := json.NewDecoder(w.Body).Decode(&payload); err != nil {
		t.Fatalf("failed decoding response: %v", err)
	}

	if payload["status"] != "ready" {
		t.Fatalf("expected status ready, got %s", payload["status"])
	}
}

func TestReadyHandler_Failure(t *testing.T) {
	s := &Server{Db: &fakeDB{err: context.DeadlineExceeded}}
	req := httptest.NewRequest(http.MethodGet, "/ready", nil)
	w := httptest.NewRecorder()

	s.readyHandler(w, req)

	if w.Result().StatusCode != http.StatusServiceUnavailable {
		t.Fatalf("expected status ServiceUnavailable; got %v", w.Result().Status)
	}
}

func TestHealthHandler(t *testing.T) {
	s := &Server{}
	req := httptest.NewRequest(http.MethodGet, "/health", nil)
	w := httptest.NewRecorder()

	s.healthHandler(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Fatalf("expected status OK; got %v", w.Result().Status)
	}

	body, err := io.ReadAll(w.Body)
	if err != nil {
		t.Fatalf("error reading response body. Err: %v", err)
	}

	expected := `{"status":"ok"}` + "\n"
	if expected != string(body) {
		t.Fatalf("expected response body to be %v; got %v", expected, string(body))
	}
}
