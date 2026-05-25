variable "environment" {
  description = "Environment name"
  type        = string
}

variable "ecs_cluster_name" {
  description = "ECS Cluster name"
  type        = string
}

variable "ecs_service_name" {
  description = "ECS Service name"
  type        = string
}

variable "db_instance_id" {
  description = "RDS instance identifier"
  type        = string
}

variable "alarm_email" {
  description = "Email address for alarm notifications"
  type        = string
  default     = ""
}