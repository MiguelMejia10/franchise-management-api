# DB Subnet Group
resource "aws_db_subnet_group" "main" {
  name       = "${var.environment}-franchise-db-subnet-group"
  subnet_ids = var.private_subnet_ids

  tags = {
    Name = "${var.environment}-franchise-db-subnet-group"
  }
}

# Security Group for RDS
resource "aws_security_group" "rds" {
  name        = "${var.environment}-franchise-rds-sg"
  description = "Security group for RDS MySQL instance"
  vpc_id      = var.vpc_id

  ingress {
    description = "MySQL from VPC"
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/16"]
  }

  egress {
    description = "Allow all outbound"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-franchise-rds-sg"
  }
}

# RDS MySQL Instance
resource "aws_db_instance" "main" {
  identifier     = "${var.environment}-franchise-mysql"
  engine         = "mysql"
  engine_version = "8.0"

  instance_class        = var.db_instance_class
  allocated_storage     = var.db_allocated_storage
  storage_type          = "gp3"
  storage_encrypted     = true
  max_allocated_storage = var.db_allocated_storage * 2

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password
  port     = 3306

  multi_az               = var.multi_az
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false

  backup_retention_period = var.backup_retention_period
  backup_window           = "03:00-04:00"
  maintenance_window      = "mon:04:00-mon:05:00"

  skip_final_snapshot       = var.environment != "production"
  final_snapshot_identifier = var.environment == "production" ? "${var.environment}-franchise-mysql-final-snapshot-${formatdate("YYYY-MM-DD-hhmm", timestamp())}" : null
  copy_tags_to_snapshot     = true
  deletion_protection       = var.environment == "production"

  enabled_cloudwatch_logs_exports = ["error", "general", "slowquery"]
  monitoring_interval             = 60
  monitoring_role_arn             = aws_iam_role.rds_monitoring.arn

  parameter_group_name = aws_db_parameter_group.main.name

  tags = {
    Name        = "${var.environment}-franchise-mysql"
    Environment = var.environment
  }
}

# DB Parameter Group
resource "aws_db_parameter_group" "main" {
  name   = "${var.environment}-franchise-mysql-params"
  family = "mysql8.0"

  parameter {
    name  = "character_set_server"
    value = "utf8mb4"
  }

  parameter {
    name  = "collation_server"
    value = "utf8mb4_unicode_ci"
  }

  parameter {
    name  = "max_connections"
    value = "200"
  }

  parameter {
    name  = "slow_query_log"
    value = "1"
  }

  parameter {
    name  = "long_query_time"
    value = "2"
  }

  tags = {
    Name = "${var.environment}-franchise-mysql-params"
  }
}

# IAM Role for Enhanced Monitoring
resource "aws_iam_role" "rds_monitoring" {
  name = "${var.environment}-rds-monitoring-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "monitoring.rds.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "rds_monitoring" {
  role       = aws_iam_role.rds_monitoring.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonRDSEnhancedMonitoringRole"
}