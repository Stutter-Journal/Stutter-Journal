locals {
  labels = {
    app = "desktop"
  }
}

resource "kubernetes_deployment_v1" "desktop" {
  wait_for_rollout = false

  metadata {
    name      = "desktop"
    namespace = var.namespace
    labels    = local.labels
  }

  spec {
    replicas = var.replicas

    selector {
      match_labels = local.labels
    }

    template {
      metadata {
        labels = local.labels
      }

      spec {
        container {
          name              = "portal"
          image             = var.image
          image_pull_policy = "IfNotPresent"

          command = ["/usr/local/bin/run-portal"]

          env {
            name  = "PORT"
            value = tostring(var.portal_port)
          }

          port {
            name           = "portal"
            container_port = var.portal_port
          }

          readiness_probe {
            tcp_socket {
              port = var.portal_port
            }
            initial_delay_seconds = 10
            period_seconds        = 10
          }

          liveness_probe {
            tcp_socket {
              port = var.portal_port
            }
            initial_delay_seconds = 20
            period_seconds        = 20
          }
        }

        container {
          name              = "bff"
          image             = var.image
          image_pull_policy = "IfNotPresent"

          command = ["/usr/local/bin/run-bff"]

          env {
            name  = "PORT"
            value = tostring(var.bff_port)
          }

          env {
            name  = "ELOQUIA_API_BASE_URL"
            value = var.api_base_url
          }

          port {
            name           = "bff"
            container_port = var.bff_port
          }

          readiness_probe {
            tcp_socket {
              port = var.bff_port
            }
            initial_delay_seconds = 10
            period_seconds        = 10
          }

          liveness_probe {
            tcp_socket {
              port = var.bff_port
            }
            initial_delay_seconds = 20
            period_seconds        = 20
          }
        }
      }
    }
  }
}

resource "kubernetes_service_v1" "desktop" {
  metadata {
    name      = "desktop"
    namespace = var.namespace
    labels    = local.labels
  }

  spec {
    selector = local.labels

    port {
      name        = "portal"
      port        = var.portal_port
      target_port = var.portal_port
    }

    port {
      name        = "bff"
      port        = var.bff_port
      target_port = var.bff_port
    }

    type = "ClusterIP"
  }
}

resource "kubernetes_ingress_v1" "desktop" {
  metadata {
    name      = "desktop"
    namespace = var.namespace
  }

  spec {
    ingress_class_name = var.ingress_class_name

    rule {
      host = var.ingress_host

      http {
        # BFF
        path {
          path      = "/api"
          path_type = "Prefix"

          backend {
            service {
              name = kubernetes_service_v1.desktop.metadata[0].name
              port {
                number = var.bff_port
              }
            }
          }
        }

        # Portal
        path {
          path      = "/"
          path_type = "Prefix"

          backend {
            service {
              name = kubernetes_service_v1.desktop.metadata[0].name
              port {
                number = var.portal_port
              }
            }
          }
        }
      }
    }
  }
}
