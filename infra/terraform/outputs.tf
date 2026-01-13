output "namespace" {
  value = var.namespace
}

output "api_url" {
  value = "http://api.${var.base_host}"
}

output "app_url" {
  value = "http://app.${var.base_host}"
}

output "postgres_host" {
  value = module.postgres.host
}

output "postgres_database_url" {
  value     = module.postgres.database_url
  sensitive = true
}

output "postgres_password" {
  value     = module.postgres.password
  sensitive = true
}

output "ingress_controller_service" {
  value = {
    namespace = module.ingress_nginx.service_namespace
    name      = module.ingress_nginx.service_name
  }
}
