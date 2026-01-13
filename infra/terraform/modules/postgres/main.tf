resource "random_password" "postgres" {
  length  = 24
  special = false
}

# Used by the backend for signing/encrypting auth cookies.
# Must be at least 32 bytes (plain or base64-encoded).
resource "random_password" "auth_cookie_secret" {
  length  = 48
  special = false
}

resource "random_password" "app" {
  length  = 24
  special = false
}

resource "kubernetes_secret_v1" "auth" {
  metadata {
    name      = "${var.release_name}-auth"
    namespace = var.namespace
  }

  type = "Opaque"

  data = {
    "postgres-password" = random_password.postgres.result
    "password"          = random_password.app.result
  }
}

locals {
  labels = {
    app = "postgres"
  }

  host_fqdn = "${var.release_name}.${var.namespace}.svc.cluster.local"
}

# Headless service for StatefulSet stable network identity.
resource "kubernetes_service_v1" "headless" {
  metadata {
    name      = "${var.release_name}-headless"
    namespace = var.namespace
    labels    = local.labels
  }

  spec {
    cluster_ip = "None"
    selector   = local.labels

    port {
      name        = "postgres"
      port        = 5432
      target_port = 5432
    }
  }
}

# Client-facing ClusterIP service.
resource "kubernetes_service_v1" "client" {
  metadata {
    name      = var.release_name
    namespace = var.namespace
    labels    = local.labels
  }

  spec {
    selector = local.labels

    port {
      name        = "postgres"
      port        = 5432
      target_port = 5432
    }

    type = "ClusterIP"
  }
}

resource "kubernetes_stateful_set_v1" "postgres" {
  metadata {
    name      = var.release_name
    namespace = var.namespace
    labels    = local.labels
  }

  spec {
    service_name = kubernetes_service_v1.headless.metadata[0].name
    replicas     = 1

    selector {
      match_labels = local.labels
    }

    template {
      metadata {
        labels = local.labels
      }

      spec {
        init_container {
          name  = "cleanup-legacy-pgdata"
          image = "busybox:1.36"

          command = [
            "sh",
            "-c",
            "set -eu; if [ -d /var/lib/postgresql/data ] && [ \"$(ls -A /var/lib/postgresql/data 2>/dev/null || true)\" != \"\" ]; then mv /var/lib/postgresql/data /var/lib/postgresql/data-legacy-$(date +%s); fi",
          ]

          volume_mount {
            name       = "data"
            mount_path = "/var/lib/postgresql"
          }
        }

        container {
          name              = "postgres"
          image             = "postgres:18"
          image_pull_policy = "IfNotPresent"

          port {
            name           = "postgres"
            container_port = 5432
          }

          env {
            name  = "POSTGRES_USER"
            value = var.username
          }

          # Postgres 18+ images use a versioned data-dir layout; mount the parent directory.
          env {
            name  = "PGDATA"
            value = "/var/lib/postgresql/18/docker"
          }

          env {
            name = "POSTGRES_PASSWORD"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.auth.metadata[0].name
                key  = "password"
              }
            }
          }

          env {
            name  = "POSTGRES_DB"
            value = var.database
          }

          volume_mount {
            name       = "data"
            mount_path = "/var/lib/postgresql"
          }

          readiness_probe {
            exec {
              command = ["sh", "-c", "pg_isready -U \"$POSTGRES_USER\" -d \"$POSTGRES_DB\" -h 127.0.0.1"]
            }
            initial_delay_seconds = 10
            period_seconds        = 5
            timeout_seconds       = 2
            failure_threshold     = 12
          }

          liveness_probe {
            exec {
              command = ["sh", "-c", "pg_isready -U \"$POSTGRES_USER\" -d \"$POSTGRES_DB\" -h 127.0.0.1"]
            }
            initial_delay_seconds = 20
            period_seconds        = 10
            timeout_seconds       = 2
            failure_threshold     = 6
          }
        }
      }
    }

    volume_claim_template {
      metadata {
        name = "data"
      }

      spec {
        access_modes       = ["ReadWriteOnce"]
        storage_class_name = var.storage_class != "" ? var.storage_class : null

        resources {
          requests = {
            storage = var.pvc_size
          }
        }
      }
    }
  }
}
