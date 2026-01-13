output "host" {
  value = local.host_fqdn
}

output "port" {
  value = 5432
}

output "database" {
  value = var.database
}

output "username" {
  value = var.username
}

output "password" {
  value     = random_password.app.result
  sensitive = true
}

output "postgres_password" {
  value     = random_password.postgres.result
  sensitive = true
}

output "secret_name" {
  value = kubernetes_secret_v1.auth.metadata[0].name
}

output "database_url" {
  value     = "postgres://${var.username}:${random_password.app.result}@${local.host_fqdn}:5432/${var.database}?sslmode=disable"
  sensitive = true
}

# Convenience: use this as a stable secret for backend session cookies
output "auth_cookie_secret" {
  value     = random_password.auth_cookie_secret.result
  sensitive = true
}
