output "service_name" {
  value = kubernetes_service_v1.desktop.metadata[0].name
}
