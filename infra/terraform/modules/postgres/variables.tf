variable "namespace" {
  type        = string
  description = "Namespace to deploy Postgres into"
}

variable "release_name" {
  type        = string
  description = "Helm release name"
  default     = "postgres"
}

variable "chart_version" {
  type        = string
  description = "Bitnami postgresql chart version"
  default     = "15.5.38"
}

variable "username" {
  type        = string
  description = "Application DB username"
  default     = "eloquia"
}

variable "database" {
  type        = string
  description = "Application DB name"
  default     = "eloquia"
}

variable "pvc_size" {
  type        = string
  description = "PVC size"
  default     = "8Gi"
}

variable "storage_class" {
  type        = string
  description = "Optional storageClass for PVC"
  default     = ""
}
