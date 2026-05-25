terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket         = "franchise-api-terraform-state"
    key            = "terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "terraform-state-lock"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "Franchise Management API"
      Environment = var.environment
      ManagedBy   = "Terraform"
      Owner       = "DevOps Team"
    }
  }
}

# VPC Module
module "vpc" {
  source = "./modules/vpc"

  environment         = var.environment
  vpc_cidr            = var.vpc_cidr
  availability_zones  = var.availability_zones
  private_subnet_cidrs = var.private_subnet_cidrs
  public_subnet_cidrs  = var.public_subnet_cidrs
}

# RDS MySQL Module
module "rds" {
  source = "./modules/rds"

  environment           = var.environment
  vpc_id                = module.vpc.vpc_id
  private_subnet_ids    = module.vpc.private_subnet_ids
  db_name               = var.db_name
  db_username           = var.db_username
  db_password           = var.db_password
  db_instance_class     = var.db_instance_class
  db_allocated_storage  = var.db_allocated_storage
  backup_retention_period = var.backup_retention_period
  multi_az              = var.environment == "production"
}

# ECS Cluster Module
module "ecs" {
  source = "./modules/ecs"

  environment         = var.environment
  vpc_id              = module.vpc.vpc_id
  private_subnet_ids  = module.vpc.private_subnet_ids
  public_subnet_ids   = module.vpc.public_subnet_ids
  container_image     = var.container_image
  container_port      = var.container_port
  cpu                 = var.ecs_task_cpu
  memory              = var.ecs_task_memory
  desired_count       = var.ecs_desired_count

  # Database connection
  db_host             = module.rds.db_endpoint
  db_port             = module.rds.db_port
  db_name             = var.db_name
  db_username         = var.db_username
  db_password         = var.db_password
}

# CloudWatch Module
module "monitoring" {
  source = "./modules/monitoring"

  environment        = var.environment
  ecs_cluster_name   = module.ecs.cluster_name
  ecs_service_name   = module.ecs.service_name
  db_instance_id     = module.rds.db_instance_id
  alarm_email        = var.alarm_email
}