# GitHub Actions - Configuración

```yaml
jobs:
  test:
    name: Run Tests
    runs-on: ubuntu-latest
    permissions:
      contents: read      # Leer el código
      checks: write       # Crear check runs ✅
      pull-requests: write # Comentar en PRs ✅
```

## 🔐 Permisos Configurados por Job

### Job: test
```yaml
permissions:
  contents: read
  checks: write           # Para dorny/test-reporter
  pull-requests: write    # Para comentar resultados en PRs
```

### Job: docker-build
```yaml
permissions:
  contents: read
  packages: write         # Para push a GitHub Container Registry
```

### Job: code-quality
```yaml
# No necesita permisos especiales (continueOnError: true)
```

### Job: security-scan
```yaml
permissions:
  contents: read
  security-events: write    # Para upload-sarif a GitHub Security ✅
  actions: read
```

## 🚀 Workflows Implementados

### 1. CI/CD Pipeline (`.github/workflows/ci-cd.yml`)

**Triggers:**
- Push a `main` o `develop`
- Pull Requests a `main`

**Jobs:**

#### 1️⃣ Test (Always runs)
```bash
- Checkout código
- Setup JDK 21
- Run tests con Gradle
- Generar reporte de tests
- Upload test results como artifact
```

#### 2️⃣ Build (Después de tests)
```bash
- Checkout código
- Setup JDK 21
- Build con Gradle (sin tests)
- Upload JAR como artifact
```

#### 3️⃣ Docker Build (Solo en push a main)
```bash
- Checkout código
- Setup Docker Buildx
- Login a GHCR
- Build imagen
- Push a ghcr.io/USUARIO/REPO:latest
```

#### 4️⃣ Code Quality (Opcional)
```bash
- Análisis con SonarCloud
- Continue on error
```

#### 5️⃣ Security Scan
```bash
- Trivy vulnerability scan
- Upload a GitHub Security
```

### 2. Deploy Pipeline (`.github/workflows/deploy.yml`)

**Triggers:**
- Manual dispatch (workflow_dispatch)
- Push a `main`
- Tags `v*`

**Jobs:**

#### 1️⃣ Deploy to AWS ECS
```bash
- Configure AWS credentials
- Login a Amazon ECR
- Build y push imagen
- Update task definition
- Deploy a ECS
```

#### 2️⃣ Deploy to GCP (Deshabilitado)
```yaml
if: false  # Cambiar a true para habilitar
```

#### 3️⃣ Deploy to Azure (Deshabilitado)
```yaml
if: false  # Cambiar a true para habilitar
```

## 🔧 Configuración de Secrets

### Secrets Requeridos en GitHub

Ir a: **Settings → Secrets and variables → Actions → New repository secret**

#### Para AWS Deployment:
```bash
AWS_ACCESS_KEY_ID          # AWS Access Key
AWS_SECRET_ACCESS_KEY      # AWS Secret Key
```

#### Para GCP Deployment (opcional):
```bash
GCP_CREDENTIALS           # Service Account JSON
GCP_PROJECT_ID            # GCP Project ID
```

#### Para Azure Deployment (opcional):
```bash
AZURE_CREDENTIALS         # Azure Service Principal
REGISTRY_LOGIN_SERVER     # ACR login server
REGISTRY_USERNAME         # ACR username
REGISTRY_PASSWORD         # ACR password
AZURE_RESOURCE_GROUP      # Resource group name
```

#### Para SonarCloud (opcional):
```bash
SONAR_TOKEN              # SonarCloud token
```

## 📊 Flujo Completo de CI/CD

### Desarrollo Local → GitHub
```bash
# 1. Hacer cambios
git add .
git commit -m "feat: add new feature"

# 2. Push a develop (para testing)
git push origin develop

# GitHub Actions ejecuta:
# ✅ Tests
# ✅ Build
# ❌ Docker build (solo en main)
```

### Merge a Main → Production
```bash
# 1. Crear PR de develop a main
gh pr create --base main --head develop

# 2. GitHub Actions ejecuta en el PR:
# ✅ Tests
# ✅ Build
# ✅ Code quality
# ✅ Security scan

# 3. Merge del PR
gh pr merge

# 4. GitHub Actions ejecuta en main:
# ✅ Tests
# ✅ Build
# ✅ Docker build y push a GHCR ✨
# ✅ Deploy a AWS ECS (si está configurado)
```

## 🎯 Verificar Workflows en GitHub

### Ver Status de Workflows
```bash
# Ver último run
gh run list --limit 5

# Ver detalles de un run
gh run view <run-id>

# Ver logs
gh run view <run-id> --log
```

### Ver en Web UI
1. Ir a **Actions** tab en GitHub
2. Seleccionar workflow (CI/CD Pipeline)
3. Ver runs y detalles

## 🐛 Troubleshooting

### Error: "Resource not accessible by integration"
✅ **Solucionado** - Agregados permisos de `checks: write`

### Error: "secrets.SONAR_TOKEN not found"
```yaml
# En ci-cd.yml, cambiar:
env:
  SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}

# Por:
continue-on-error: true
```

### Error: "Docker login failed"
```bash
# Verificar que GITHUB_TOKEN tiene permisos
# En Settings → Actions → General → Workflow permissions
# Seleccionar: "Read and write permissions"
```

### Error: Tests fallan en CI pero pasan en local
```bash
# Verificar versión de Java
java -version  # Local
# vs
# Setup JDK 21 en workflow

# Verificar que las dependencias están en cache
- uses: actions/setup-java@v4
  with:
    cache: gradle  # ✅ Importante
```

### Error: "Gradle wrapper not found"
```bash
# Asegurarse de incluir gradle wrapper
git add gradlew
git add gradlew.bat
git add gradle/wrapper/
git commit -m "Add gradle wrapper"
```

## 📈 Monitoreo de Pipelines

### Badges en README

Agregar a `README.md`:

```markdown
# Franchise Management API

![CI/CD](https://github.com/USUARIO/REPO/workflows/CI%2FCD%20Pipeline/badge.svg)
![Tests](https://github.com/USUARIO/REPO/workflows/CI%2FCD%20Pipeline/badge.svg?job=test)
![Docker](https://github.com/USUARIO/REPO/workflows/CI%2FCD%20Pipeline/badge.svg?job=docker-build)
```

### Notifications

GitHub envía notificaciones automáticamente cuando:
- ❌ Un workflow falla
- ✅ Un workflow se recupera después de fallo
- 🔄 PR checks completan

## 🔄 Actualizar Workflows

### Modificar Workflow Existente
```bash
# 1. Editar archivo
vim .github/workflows/ci-cd.yml

# 2. Commit y push
git add .github/workflows/ci-cd.yml
git commit -m "ci: update workflow"
git push

# 3. El workflow se actualiza automáticamente
```

### Agregar Nuevo Workflow
```bash
# 1. Crear archivo
touch .github/workflows/new-workflow.yml

# 2. Agregar contenido (ver ejemplos arriba)

# 3. Commit y push
git add .github/workflows/new-workflow.yml
git commit -m "ci: add new workflow"
git push
```

## ✅ Checklist de Configuración

- [x] ✅ Workflows creados
- [x] ✅ Permisos configurados
- [x] ✅ Tests pasando
- [ ] ⚠️ Secrets de AWS configurados (opcional)
- [ ] ⚠️ Secrets de GCP configurados (opcional)
- [ ] ⚠️ Secrets de Azure configurados (opcional)
- [ ] ⚠️ SonarCloud token configurado (opcional)

## 📚 Referencias

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Workflow Syntax](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions)
- [Permissions](https://docs.github.com/en/actions/using-jobs/assigning-permissions-to-jobs)
- [Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)

---

**¡Pipelines listos para usar!** 🚀

Los workflows ejecutarán automáticamente en cada push o PR.