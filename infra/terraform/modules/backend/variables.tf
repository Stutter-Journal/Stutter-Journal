variable "namespace" {
  type = string
}

variable "image" {
  type = string
}

variable "replicas" {
  type    = number
  default = 1
}

variable "service_port" {
  type    = number
  default = 8080
}

variable "ingress_host" {
  type = string
}

variable "database_url" {
  type      = string
  sensitive = true
}

variable "auth_cookie_secret" {
  type      = string
  sensitive = true
}

variable "environment" {
  type    = string
  default = "development"
}

variable "apply_migrations" {
  type    = bool
  default = true
}

variable "ingress_class_name" {
  type    = string
  default = "nginx"
}
