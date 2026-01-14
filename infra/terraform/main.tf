locals {
  api_host = "api.${var.base_host}"
  app_host = "app.${var.base_host}"
}

module "namespace" {
  source = "./modules/namespace"
  name   = var.namespace
}

module "ingress_nginx" {
  source            = "./modules/ingress_nginx"
  ingress_namespace = var.ingress_namespace
}

module "postgres" {
  source    = "./modules/postgres"
  namespace = module.namespace.name

  release_name = "eloquia-postgres"
  username     = "eloquia"
  database     = "eloquia"
}

module "backend" {
  source    = "./modules/backend"
  namespace = module.namespace.name

  image            = var.backend_image
  migrate_image    = var.backend_migrate_image
  environment      = var.environment
  apply_migrations = false

  database_url       = module.postgres.database_url
  auth_cookie_secret = module.postgres.auth_cookie_secret

  ingress_host = local.api_host
}

module "desktop" {
  source    = "./modules/desktop"
  namespace = module.namespace.name

  image        = var.desktop_image
  ingress_host = local.app_host

  # BFF proxies to the in-cluster backend service.
  api_base_url = "http://backend:${module.backend.service_port}"
}
