variable "ingress_namespace" {
  type        = string
  description = "Namespace where ingress-nginx is installed"
  default     = "ingress-nginx"
}

variable "release_name" {
  type        = string
  description = "Helm release name for ingress-nginx"
  default     = "ingress-nginx"
}

variable "chart_version" {
  type        = string
  description = "ingress-nginx chart version"
  default     = "4.11.3"
}
