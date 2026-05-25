output "cluster_id" {
  description = "ECS Cluster ID"
  value       = aws_ecs_cluster.main.id
}

output "cluster_name" {
  description = "ECS Cluster name"
  value       = aws_ecs_cluster.main.name
}

output "service_name" {
  description = "ECS Service name"
  value       = aws_ecs_service.app.name
}

output "service_id" {
  description = "ECS Service ID"
  value       = aws_ecs_service.app.id
}

output "task_definition_arn" {
  description = "ECS Task Definition ARN"
  value       = aws_ecs_task_definition.app.arn
}

output "load_balancer_dns" {
  description = "Load Balancer DNS name"
  value       = aws_lb.main.dns_name
}

output "load_balancer_arn" {
  description = "Load Balancer ARN"
  value       = aws_lb.main.arn
}

output "target_group_arn" {
  description = "Target Group ARN"
  value       = aws_lb_target_group.app.arn
}

output "ecr_repository_url" {
  description = "ECR Repository URL"
  value       = aws_ecr_repository.app.repository_url
}

output "ecr_repository_arn" {
  description = "ECR Repository ARN"
  value       = aws_ecr_repository.app.arn
}