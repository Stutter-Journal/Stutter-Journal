variable "kubeconfig_path" {
  type        = string
  description = "Path to kubeconfig used by the kubernetes/helm providers."
  default     = "~/.kube/config"
}

variable "namespace" {
  type        = string
  description = "Namespace for Eloquia dev resources."
  default     = "eloquia-dev"
}

variable "ingress_namespace" {
  type        = string
  description = "Namespace for ingress-nginx controller."
  default     = "ingress-nginx"
}

variable "base_host" {
  type        = string
  description = "Base host used for ingress (e.g. <ip>.sslip.io or eloquia.test)."
}

variable "backend_image" {
  type        = string
  description = "Backend container image."
  default     = "eloquia/backend:dev"
}

variable "desktop_image" {
  type        = string
  description = "Desktop container image (contains both portal + bff)."
  default     = "eloquia/desktop:dev"
}

variable "environment" {
  type        = string
  description = "App environment name (development/production-ish)."
  default     = "development"
}
