# SNS Topic for Alarms
resource "aws_sns_topic" "alarms" {
  name = "${var.environment}-franchise-alarms"

  tags = {
    Name = "${var.environment}-franchise-alarms"
  }
}

resource "aws_sns_topic_subscription" "alarms_email" {
  count     = var.alarm_email != "" ? 1 : 0
  topic_arn = aws_sns_topic.alarms.arn
  protocol  = "email"
  endpoint  = var.alarm_email
}

# ECS Service Alarms

resource "aws_cloudwatch_metric_alarm" "ecs_cpu_high" {
  alarm_name          = "${var.environment}-franchise-ecs-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "ECS Service CPU utilization is too high"
  alarm_actions       = [aws_sns_topic.alarms.arn]

  dimensions = {
    ClusterName = var.ecs_cluster_name
    ServiceName = var.ecs_service_name
  }
}

resource "aws_cloudwatch_metric_alarm" "ecs_memory_high" {
  alarm_name          = "${var.environment}-franchise-ecs-memory-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "MemoryUtilization"
  namespace           = "AWS/ECS"
  period              = 300
  statistic           = "Average"
  threshold           = 85
  alarm_description   = "ECS Service memory utilization is too high"
  alarm_actions       = [aws_sns_topic.alarms.arn]

  dimensions = {
    ClusterName = var.ecs_cluster_name
    ServiceName = var.ecs_service_name
  }
}

resource "aws_cloudwatch_metric_alarm" "ecs_task_count_low" {
  alarm_name          = "${var.environment}-franchise-ecs-task-count-low"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 1
  metric_name         = "RunningTaskCount"
  namespace           = "ECS/ContainerInsights"
  period              = 60
  statistic           = "Average"
  threshold           = 1
  alarm_description   = "ECS Service has less than 1 running task"
  alarm_actions       = [aws_sns_topic.alarms.arn]
  treat_missing_data  = "breaching"

  dimensions = {
    ClusterName = var.ecs_cluster_name
    ServiceName = var.ecs_service_name
  }
}

# RDS Alarms

resource "aws_cloudwatch_metric_alarm" "rds_cpu_high" {
  alarm_name          = "${var.environment}-franchise-rds-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "RDS CPU utilization is too high"
  alarm_actions       = [aws_sns_topic.alarms.arn]

  dimensions = {
    DBInstanceIdentifier = var.db_instance_id
  }
}

resource "aws_cloudwatch_metric_alarm" "rds_storage_low" {
  alarm_name          = "${var.environment}-franchise-rds-storage-low"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 1
  metric_name         = "FreeStorageSpace"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = 2000000000  # 2GB in bytes
  alarm_description   = "RDS free storage space is low"
  alarm_actions       = [aws_sns_topic.alarms.arn]

  dimensions = {
    DBInstanceIdentifier = var.db_instance_id
  }
}

resource "aws_cloudwatch_metric_alarm" "rds_connection_count_high" {
  alarm_name          = "${var.environment}-franchise-rds-connections-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "DatabaseConnections"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "RDS connection count is too high"
  alarm_actions       = [aws_sns_topic.alarms.arn]

  dimensions = {
    DBInstanceIdentifier = var.db_instance_id
  }
}

# CloudWatch Dashboard
resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "${var.environment}-franchise-dashboard"

  dashboard_body = jsonencode({
    widgets = [
      {
        type = "metric"
        properties = {
          metrics = [
            ["AWS/ECS", "CPUUtilization", { stat = "Average" }],
            [".", "MemoryUtilization"]
          ]
          period = 300
          stat   = "Average"
          region = data.aws_region.current.name
          title  = "ECS Service Metrics"
        }
      },
      {
        type = "metric"
        properties = {
          metrics = [
            ["AWS/RDS", "CPUUtilization", { stat = "Average" }],
            [".", "DatabaseConnections"],
            [".", "FreeStorageSpace"]
          ]
          period = 300
          stat   = "Average"
          region = data.aws_region.current.name
          title  = "RDS Metrics"
        }
      },
      {
        type = "log"
        properties = {
          query = <<-EOT
            SOURCE '/ecs/${var.environment}/franchise-api'
            | fields @timestamp, @message
            | sort @timestamp desc
            | limit 100
          EOT
          region = data.aws_region.current.name
          title  = "Recent Application Logs"
        }
      }
    ]
  })
}

data "aws_region" "current" {}