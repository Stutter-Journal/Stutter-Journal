package server

import (
	_ "embed"
	"net/http"

	"github.com/go-chi/chi/v5"
)

// openAPISpec holds the embedded OpenAPI definition.
//
//go:embed docs/openapi.yaml
var openAPISpec []byte

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
    Redoc.init('/docs/openapi.yaml', {
      hideLoading: true,
      expandResponses: '200,201',
      scrollYOffset: 24
    }, document.getElementById('redoc-container'))
  </script>
</body>
</html>`

func (s *Server) registerDocsRoutes(r chi.Router) {
	r.Route("/docs", func(r chi.Router) {
		r.Get("/", func(w http.ResponseWriter, _ *http.Request) {
			w.Header().Set("Content-Type", "text/html; charset=utf-8")
			_, _ = w.Write([]byte(redocPage))
		})

		r.Get("/openapi.yaml", func(w http.ResponseWriter, _ *http.Request) {
			w.Header().Set("Content-Type", "application/yaml; charset=utf-8")
			_, _ = w.Write(openAPISpec)
		})
	})
}
