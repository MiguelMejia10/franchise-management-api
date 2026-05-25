# Infrastructure as Code

Este directorio contiene toda la infraestructura como código para desplegar la aplicación Franchise Management API en diferentes proveedores de cloud.

## 📁 Estructura

```
infrastructure/
├── terraform/              # Terraform IaC para AWS
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   ├── terraform.tfvars.example
│   └── modules/
│       ├── vpc/           # Networking
│       ├── rds/           # Database
│       ├── ecs/           # Container orchestration
│       └── monitoring/    # CloudWatch + SNS
├── aws/                   # Configuraciones específicas de AWS
│   └── task-definition.json
└── README.md
```

## 🚀 Proveedores Soportados

### ✅ AWS (Completamente implementado)
- **Compute**: ECS Fargate
- **Database**: RDS MySQL 8.0
- **Networking**: VPC, ALB, NAT Gateway
- **Registry**: ECR
- **Monitoring**: CloudWatch, SNS

### ⚠️ Google Cloud Platform (Preparado en CI/CD)
- Cloud Run
- Cloud SQL
- Container Registry

### ⚠️ Azure (Preparado en CI/CD)
- Azure Container Instances
- Azure Database for MySQL
- Azure Container Registry

## 📊 Arquitectura AWS

```
┌─────────────────────────────────────────────────────────────┐
│                        Internet                              │
└────────────────────┬────────────────────────────────────────┘
                     │
          ┌──────────▼──────────┐
          │  Application Load    │
          │     Balancer         │
          └──────────┬──────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
┌───────▼────────┐       ┌───────▼────────┐
│   Public       │       │   Public       │
│   Subnet 1     │       │   Subnet 2     │
│   (us-east-1a) │       │   (us-east-1b) │
└────────┬───────┘       └───────┬────────┘
         │                       │
    ┌────▼───┐              ┌────▼───┐
    │  NAT   │              │  NAT   │
    │Gateway │              │Gateway │
    └────┬───┘              └───┬────┘
         │                      │
┌────────▼────────┐    ┌───────▼────────┐
│   Private       │    │   Private      │
│   Subnet 1      │    │   Subnet 2     │
│   (us-east-1a)  │    │   (us-east-1b) │
│                 │    │                │
│  ┌──────────┐   │    │  ┌──────────┐  │
│  │ECS Task 1│   │    │  │ECS Task 2│  │
│  │(Fargate) │   │    │  │(Fargate) │  │
│  └─────┬────┘   │    │  └─────┬────┘  │
│        │        │    │        │       │
└────────┼────────┘    └────────┼───────┘
         │                      │
         └──────────┬───────────┘
                    │
            ┌───────▼────────┐
            │   RDS MySQL    │
            │   (Multi-AZ)   │
            └────────────────┘
```

## 🛠️ Componentes Desplegados

### VPC
- **CIDR**: 10.0.0.0/16
- **Subnets Públicas**: 2 (para ALB y NAT Gateway)
- **Subnets Privadas**: 2 (para ECS y RDS)
- **Alta Disponibilidad**: 2 Availability Zones

### RDS MySQL
- **Engine**: MySQL 8.0
- **Clase**: Configurable (db.t3.micro - db.t3.medium)
- **Storage**: 20GB con auto-scaling
- **Backup**: Retención de 7-30 días
- **Multi-AZ**: Opcional (habilitado en producción)
- **Encryption**: Habilitado

### ECS Fargate
- **Cluster**: Dedicado por ambiente
- **Tasks**: 1-3 según ambiente
- **CPU**: 256-1024 vCPU
- **Memory**: 512MB - 2GB
- **Auto Scaling**: CPU y Memory based
- **Health Checks**: Actuator endpoint

### Application Load Balancer
- **Tipo**: Application
- **Listeners**: HTTP (80)
- **Target Group**: ECS Tasks
- **Health Check**: /actuator/health

### ECR Repository
- **Scanning**: Habilitado en push
- **Lifecycle**: Mantiene últimas 10 imágenes
- **Encryption**: AES256

### Monitoring
- **Alarms**:
  - ECS CPU/Memory > 80%
  - RDS CPU > 80%
  - RDS Storage < 2GB
  - ECS Running Tasks < 1
- **Dashboard**: Métricas en tiempo real
- **Notifications**: SNS + Email

## 🚀 Despliegue Rápido

### Prerequisitos
```bash
# AWS CLI
aws configure

# Terraform
brew install terraform

# Docker
docker --version
```

### Pasos

1. **Setup Terraform Backend**
```bash
cd infrastructure/terraform
./scripts/setup-backend.sh
```

2. **Configurar Variables**
```bash
cp terraform.tfvars.example terraform.tfvars
# Editar terraform.tfvars con tus valores
```

3. **Desplegar Infraestructura**
```bash
terraform init
terraform plan
terraform apply
```

4. **Build y Push Docker Image**
```bash
# Obtener ECR URL
ECR_URL=$(terraform output -raw ecr_repository_url)

# Login
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin $ECR_URL

# Build y Push
cd ../..
docker build -t franchise-api .
docker tag franchise-api:latest $ECR_URL:latest
docker push $ECR_URL:latest
```

5. **Verificar Despliegue**
```bash
# Obtener URL de la API
terraform output api_url

# Probar
curl http://$(terraform output -raw load_balancer_dns)/actuator/health
```

## 🔄 CI/CD con GitHub Actions

El proyecto incluye workflows de GitHub Actions para:

### CI Pipeline (`.github/workflows/ci-cd.yml`)
- ✅ Run tests
- ✅ Build application
- ✅ Build y push Docker image a GHCR
- ✅ Security scanning (Trivy)
- ✅ Code quality analysis

### Deploy Pipeline (`.github/workflows/deploy.yml`)
- ✅ Deploy a AWS ECS
- ⚠️ Deploy a GCP Cloud Run (deshabilitado)
- ⚠️ Deploy a Azure ACI (deshabilitado)

### Secrets Requeridos en GitHub

```yaml
# AWS
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY

# GCP (opcional)
GCP_CREDENTIALS
GCP_PROJECT_ID

# Azure (opcional)
AZURE_CREDENTIALS
REGISTRY_LOGIN_SERVER
REGISTRY_USERNAME
REGISTRY_PASSWORD
AZURE_RESOURCE_GROUP
```

## 💰 Estimación de Costos

### Development (Mínimo)
| Servicio | Costo Mensual |
|----------|---------------|
| VPC | Gratis |
| NAT Gateway (2) | $32 |
| RDS db.t3.micro | $15 |
| ECS Fargate (1 task) | $15 |
| ALB | $20 |
| **TOTAL** | **~$82/mes** |

### Production (Recomendado)
| Servicio | Costo Mensual |
|----------|---------------|
| VPC | Gratis |
| NAT Gateway (2) | $32 |
| RDS db.t3.medium Multi-AZ | $120 |
| ECS Fargate (3 tasks) | $90 |
| ALB | $20 |
| CloudWatch | $10 |
| **TOTAL** | **~$272/mes** |

## 🔒 Seguridad

### Implementado
- ✅ Encryption at rest (RDS, ECR)
- ✅ Encryption in transit (HTTPS)
- ✅ Security Groups restrictivos
- ✅ Private subnets para compute y database
- ✅ IAM Roles con mínimos privilegios
- ✅ Secrets en AWS Secrets Manager
- ✅ Container image scanning

### Recomendaciones Adicionales
- [ ] AWS WAF en ALB
- [ ] VPC Flow Logs
- [ ] GuardDuty
- [ ] AWS Config
- [ ] Certificate Manager para HTTPS

## 📚 Documentación

- [Terraform README](./terraform/README.md) - Guía detallada de Terraform
- [AWS Best Practices](https://aws.amazon.com/architecture/well-architected/)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)

## 🆘 Soporte

Para problemas o preguntas:
1. Revisar [Terraform README](./terraform/README.md#-troubleshooting)
2. Verificar CloudWatch Logs
3. Revisar GitHub Actions logs
4. Crear issue en el repositorio

## 🧹 Limpieza

```bash
cd infrastructure/terraform
terraform destroy
```

⚠️ **ADVERTENCIA**: Esto eliminará toda la infraestructura.