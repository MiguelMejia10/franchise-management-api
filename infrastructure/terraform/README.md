# Terraform Infrastructure as Code

Este directorio contiene la infraestructura como código (IaC) para desplegar la aplicación Franchise Management API en AWS.

## 📁 Estructura

```
terraform/
├── main.tf                    # Configuración principal
├── variables.tf               # Variables de entrada
├── outputs.tf                 # Outputs del despliegue
├── terraform.tfvars.example   # Ejemplo de valores de variables
└── modules/
    ├── vpc/                   # Módulo VPC
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    ├── rds/                   # Módulo RDS MySQL
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    ├── ecs/                   # Módulo ECS + ALB + ECR
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    └── monitoring/            # Módulo CloudWatch + SNS
        ├── main.tf
        ├── variables.tf
        └── outputs.tf
```

## 🏗️ Componentes de Infraestructura

### 1. VPC Module
- VPC con CIDR configurable
- 2 Subnets públicas (para ALB)
- 2 Subnets privadas (para ECS y RDS)
- Internet Gateway
- 2 NAT Gateways (alta disponibilidad)
- Route Tables

### 2. RDS Module
- RDS MySQL 8.0
- Multi-AZ opcional (producción)
- Automated backups
- Enhanced monitoring
- Parameter group optimizado
- Security groups
- Encryption at rest

### 3. ECS Module
- ECS Fargate Cluster
- ECR Repository con lifecycle policy
- Task Definition con R2DBC configurado
- ECS Service con auto-scaling
- Application Load Balancer
- Target Groups con health checks
- Security Groups
- CloudWatch Logs

### 4. Monitoring Module
- CloudWatch Alarms:
  - ECS CPU/Memory utilization
  - RDS CPU/Connections/Storage
  - Task count
- SNS Topic para notificaciones
- CloudWatch Dashboard
- Email notifications

## 🚀 Uso

### Prerequisitos

1. **AWS CLI configurado**
```bash
aws configure
```

2. **Terraform instalado** (>= 1.0)
```bash
brew install terraform  # macOS
```

3. **Credenciales AWS** con permisos para:
   - VPC, EC2, RDS
   - ECS, ECR
   - IAM
   - CloudWatch
   - S3 (para state backend)

### Configuración Inicial

1. **Copiar archivo de variables**
```bash
cp terraform.tfvars.example terraform.tfvars
```

2. **Editar terraform.tfvars**
```hcl
environment = "dev"
db_password = "tu_password_seguro"
alarm_email = "tu-email@example.com"
```

3. **Crear bucket S3 para Terraform State** (una sola vez)
```bash
aws s3 mb s3://franchise-api-terraform-state --region us-east-1
aws s3api put-bucket-versioning \
  --bucket franchise-api-terraform-state \
  --versioning-configuration Status=Enabled

# DynamoDB para state locking
aws dynamodb create-table \
  --table-name terraform-state-lock \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region us-east-1
```

### Despliegue

1. **Inicializar Terraform**
```bash
cd infrastructure/terraform
terraform init
```

2. **Validar configuración**
```bash
terraform validate
terraform fmt
```

3. **Ver plan de cambios**
```bash
terraform plan
```

4. **Aplicar cambios**
```bash
terraform apply
```

5. **Obtener outputs**
```bash
terraform output
terraform output -json > outputs.json
```

### Construir y Pushear Imagen Docker a ECR

Después de crear la infraestructura:

```bash
# Obtener ECR URL del output
ECR_URL=$(terraform output -raw ecr_repository_url)

# Login a ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin $ECR_URL

# Build imagen
cd ../..
docker build -t franchise-api .

# Tag imagen
docker tag franchise-api:latest $ECR_URL:latest

# Push a ECR
docker push $ECR_URL:latest
```

### Actualizar ECS Service

```bash
# Forzar nuevo deployment con la imagen actualizada
aws ecs update-service \
  --cluster $(terraform output -raw ecs_cluster_name) \
  --service $(terraform output -raw ecs_service_name) \
  --force-new-deployment \
  --region us-east-1
```

## 🔧 Configuración por Ambiente

### Development
```hcl
environment = "dev"
db_instance_class = "db.t3.micro"
ecs_desired_count = 1
ecs_task_cpu = 256
ecs_task_memory = 512
```

### Staging
```hcl
environment = "staging"
db_instance_class = "db.t3.small"
ecs_desired_count = 2
ecs_task_cpu = 512
ecs_task_memory = 1024
```

### Production
```hcl
environment = "production"
db_instance_class = "db.t3.medium"
ecs_desired_count = 3
ecs_task_cpu = 1024
ecs_task_memory = 2048
multi_az = true
backup_retention_period = 30
```

## 📊 Monitoreo

### CloudWatch Dashboard
```bash
# Obtener URL del dashboard
terraform output dashboard_url
```

### Ver logs de aplicación
```bash
aws logs tail /ecs/dev/franchise-api --follow
```

### Métricas de ECS
```bash
aws ecs describe-services \
  --cluster $(terraform output -raw ecs_cluster_name) \
  --services $(terraform output -raw ecs_service_name)
```

## 🔒 Seguridad

### Secrets Management

Para producción, se recomienda usar AWS Secrets Manager:

```bash
# Crear secret para DB password
aws secretsmanager create-secret \
  --name franchise-api/db-password \
  --secret-string "tu_password_seguro"

# Actualizar task definition para usar el secret
# (modificar modules/ecs/main.tf para usar secrets en lugar de environment variables)
```

### Variables Sensibles

**NUNCA** commitear:
- `terraform.tfvars` con passwords reales
- `.terraform/` directory
- `*.tfstate` files

Usar `.gitignore`:
```
.terraform/
*.tfstate
*.tfstate.backup
terraform.tfvars
```

## 🧹 Limpieza

### Destruir infraestructura

```bash
terraform destroy
```

**ADVERTENCIA**: Esto eliminará todos los recursos. En producción, asegurarse de:
1. Hacer backup de la base de datos
2. Exportar logs importantes
3. Guardar snapshots si es necesario

## 💰 Estimación de Costos

### Ambiente Dev (us-east-1)
- VPC: Gratis
- NAT Gateways: ~$32/mes (2 NAT @ $0.045/hora)
- RDS db.t3.micro: ~$15/mes
- ECS Fargate: ~$15/mes (1 task @ 0.25 vCPU, 0.5GB)
- ALB: ~$20/mes
- **Total: ~$82/mes**

### Ambiente Production
- VPC: Gratis
- NAT Gateways: ~$32/mes
- RDS db.t3.medium Multi-AZ: ~$120/mes
- ECS Fargate: ~$90/mes (3 tasks @ 1 vCPU, 2GB)
- ALB: ~$20/mes
- **Total: ~$262/mes**

*Nota: Costos estimados pueden variar según uso y región*

## 🔄 Workspace por Ambiente

```bash
# Crear workspaces para diferentes ambientes
terraform workspace new dev
terraform workspace new staging
terraform workspace new production

# Cambiar entre workspaces
terraform workspace select dev
terraform plan -var="environment=dev"
terraform apply -var="environment=dev"

terraform workspace select production
terraform plan -var="environment=production"
terraform apply -var="environment=production"
```

## 📚 Referencias

- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS ECS Best Practices](https://docs.aws.amazon.com/AmazonECS/latest/bestpracticesguide/intro.html)
- [AWS RDS Best Practices](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_BestPractices.html)
- [Terraform Best Practices](https://www.terraform-best-practices.com/)

## 🆘 Troubleshooting

### Error: Backend S3 no existe
```bash
# Crear bucket manualmente
aws s3 mb s3://franchise-api-terraform-state
```

### Error: ECR image not found
```bash
# Pushear imagen inicial a ECR antes de crear ECS service
# Ver sección "Construir y Pushear Imagen Docker a ECR"
```

### Task no inicia
```bash
# Ver logs del task
aws ecs describe-tasks \
  --cluster $(terraform output -raw ecs_cluster_name) \
  --tasks <task-id>

# Ver logs de CloudWatch
aws logs tail /ecs/dev/franchise-api --follow
```

### RDS Connection issues
```bash
# Verificar security groups
# Asegurarse que ECS tasks pueden conectar a RDS en puerto 3306
# Verificar que el endpoint de RDS esté correcto en variables de entorno
```