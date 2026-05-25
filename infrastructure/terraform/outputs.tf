output "vpc_id" {
  description = "VPC ID"
  value       = module.vpc.vpc_id
}

output "private_subnet_ids" {
  description = "Private subnet IDs"
  value       = module.vpc.private_subnet_ids
}

output "public_subnet_ids" {
  description = "Public subnet IDs"
  value       = module.vpc.public_subnet_ids
}

output "db_endpoint" {
  description = "RDS MySQL endpoint"
  value       = module.rds.db_endpoint
}

output "db_name" {
  description = "Database name"
  value       = var.db_name
}

output "ecs_cluster_name" {
  description = "ECS Cluster name"
  value       = module.ecs.cluster_name
}

output "ecs_service_name" {
  description = "ECS Service name"
  value       = module.ecs.service_name
}

output "load_balancer_dns" {
  description = "Load Balancer DNS name"
  value       = module.ecs.load_balancer_dns
}

output "api_url" {
  description = "API URL"
  value       = "http://${module.ecs.load_balancer_dns}"
}

output "ecr_repository_url" {
  description = "ECR Repository URL"
  value       = module.ecs.ecr_repository_url
}