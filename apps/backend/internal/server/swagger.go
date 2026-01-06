package server

//go:generate swag init --dir .,../../cmd/api --generalInfo ../../cmd/api/main.go --output ./docs --parseDependency --parseInternal

// @title Eloquia API
// @version 0.1.0
// @description Backend HTTP API for Eloquia.
// @BasePath /
// @securityDefinitions.apikey SessionCookie
// @in cookie
// @name eloquia_session
