output "service_namespace" {
  value = data.kubernetes_service_v1.controller.metadata[0].namespace
}

output "service_name" {
  value = data.kubernetes_service_v1.controller.metadata[0].name
}

output "load_balancer_ingress" {
  value = try(data.kubernetes_service_v1.controller.status[0].load_balancer[0].ingress, [])
}
