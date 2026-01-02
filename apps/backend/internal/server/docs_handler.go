package server

import (
	"net/http"

	docs "backend/internal/server/docs"
	"github.com/go-chi/chi/v5"
)

const redocPage = `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <title>Eloquia API Docs</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <style>
    body { margin: 0; padding: 0; }
    #redoc-container { width: 100vw; height: 100vh; }
  </style>
</head>
<body>
  <div id="redoc-container"></div>
  <script src="https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js"></script>
  <script>
    Redoc.init('/docs/doc.json', {
      hideLoading: true,
      expandResponses: '200,201',
      scrollYOffset: 24
    }, document.getElementById('redoc-container'))
  </script>
</body>
</html>`

func (s *Server) registerDocsRoutes(r chi.Router) {
	r.Get("/docs/doc.json", func(w http.ResponseWriter, _ *http.Request) {
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		_, _ = w.Write([]byte(docs.SwaggerInfo.ReadDoc()))
	})

	r.Get("/docs", func(w http.ResponseWriter, _ *http.Request) {
		w.Header().Set("Content-Type", "text/html; charset=utf-8")
		_, _ = w.Write([]byte(redocPage))
	})

	r.Get("/docs/", func(w http.ResponseWriter, req *http.Request) {
		http.Redirect(w, req, "/docs", http.StatusPermanentRedirect)
	})
}
