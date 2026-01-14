locals {
  labels = {
    app = "backend"
  }
}

resource "kubernetes_secret_v1" "backend" {
  metadata {
    name      = "backend-secrets"
    namespace = var.namespace
  }

  type = "Opaque"

  data = {
    DATABASE_URL       = var.database_url
    AUTH_COOKIE_SECRET = var.auth_cookie_secret
  }
}

resource "kubernetes_config_map_v1" "backend" {
  metadata {
    name      = "backend-config"
    namespace = var.namespace
  }

  data = {
    PORT                          = tostring(var.service_port)
    ENVIRONMENT                   = var.environment
    BLUEPRINT_ENV                 = var.environment
    BLUEPRINT_DB_APPLY_MIGRATIONS = var.apply_migrations ? "true" : "false"
  }
}

resource "kubernetes_job_v1" "migrate" {
  wait_for_completion = true

  metadata {
    name      = "backend-migrate"
    namespace = var.namespace

    labels = {
      app = "backend-migrate"
    }
  }

  spec {
    backoff_limit = 2

    template {
      metadata {
        labels = {
          app = "backend-migrate"
        }
      }

      spec {
        restart_policy = "Never"

        container {
          name              = "atlas"
          image             = var.migrate_image
          image_pull_policy = "IfNotPresent"

          env_from {
            secret_ref {
              name = kubernetes_secret_v1.backend.metadata[0].name
            }
          }
        }
      }
    }
  }
}

resource "kubernetes_deployment_v1" "backend" {
  wait_for_rollout = false

  depends_on = [kubernetes_job_v1.migrate]

  metadata {
    name      = "backend"
    namespace = var.namespace
    labels    = local.labels
  }

  spec {
    replicas = var.replicas

    strategy {
      type = "Recreate"
    }

    selector {
      match_labels = local.labels
    }

    template {
      metadata {
        labels = local.labels
      }

      spec {
        init_container {
          name              = "wait-for-db"
          image             = "postgres:18"
          image_pull_policy = "IfNotPresent"

          command = [
            "sh",
            "-c",
            "until pg_isready -d \"$DATABASE_URL\"; do echo 'waiting for postgres...'; sleep 2; done",
          ]

          env_from {
            secret_ref {
              name = kubernetes_secret_v1.backend.metadata[0].name
            }
          }
        }

        container {
          name              = "backend"
          image             = var.image
          image_pull_policy = "IfNotPresent"

          port {
            name           = "http"
            container_port = var.service_port
          }

          env_from {
            config_map_ref {
              name = kubernetes_config_map_v1.backend.metadata[0].name
            }
          }

          env_from {
            secret_ref {
              name = kubernetes_secret_v1.backend.metadata[0].name
            }
          }

          liveness_probe {
            http_get {
              path = "/health"
              port = var.service_port
            }
            initial_delay_seconds = 10
            period_seconds        = 10
            timeout_seconds       = 2
            failure_threshold     = 6
          }

          readiness_probe {
            http_get {
              path = "/ready"
              port = var.service_port
            }
            initial_delay_seconds = 5
            period_seconds        = 5
            timeout_seconds       = 2
            failure_threshold     = 12
          }
        }
      }
    }
  }
}

resource "kubernetes_service_v1" "backend" {
  metadata {
    name      = "backend"
    namespace = var.namespace
    labels    = local.labels
  }

  spec {
    selector = local.labels

    port {
      name        = "http"
      port        = var.service_port
      target_port = var.service_port
    }

    type = "ClusterIP"
  }
}

resource "kubernetes_ingress_v1" "backend" {
  metadata {
    name      = "backend"
    namespace = var.namespace
  }

  spec {
    ingress_class_name = var.ingress_class_name

    rule {
      host = var.ingress_host

      http {
        path {
          path      = "/"
          path_type = "Prefix"

          backend {
            service {
              name = kubernetes_service_v1.backend.metadata[0].name
              port {
                number = var.service_port
              }
            }
          }
        }
      }
    }
  }
}
