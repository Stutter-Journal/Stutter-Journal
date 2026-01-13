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

variable "portal_port" {
  type    = number
  default = 4000
}

variable "bff_port" {
  type    = number
  default = 3000
}

variable "ingress_host" {
  type = string
}

variable "api_base_url" {
  type        = string
  description = "Upstream base URL for the BFF to forward to"
}

variable "ingress_class_name" {
  type    = string
  default = "nginx"
}
