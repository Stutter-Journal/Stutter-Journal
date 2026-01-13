resource "helm_release" "ingress_nginx" {
  name             = var.release_name
  repository       = "https://kubernetes.github.io/ingress-nginx"
  chart            = "ingress-nginx"
  version          = var.chart_version
  namespace        = var.ingress_namespace
  create_namespace = true

  wait    = true
  timeout = 30

  set {
    name  = "controller.service.type"
    value = "LoadBalancer"
  }

  set {
    name  = "controller.publishService.enabled"
    value = "true"
  }
}

data "kubernetes_service_v1" "controller" {
  metadata {
    name      = "${var.release_name}-controller"
    namespace = var.ingress_namespace
  }

  depends_on = [helm_release.ingress_nginx]
}
