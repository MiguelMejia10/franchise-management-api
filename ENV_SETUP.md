# Variables de Entorno - Guía de Configuración

## 📋 Archivos de Entorno

### `.env.example`
Template completo con todas las variables y documentación.

### `.env.render`
Versión simplificada lista para importar en Render.

### `.env` (local - NO commitear)
Tu archivo local con valores reales.

---

## 🚀 Importar en Render

### Método 1: Bulk Import (Recomendado)

1. **Ir a tu Web Service en Render**
   ```
   Dashboard → Tu servicio → Environment
   ```

2. **Click en "Add from .env"**

3. **Copiar contenido de `.env.render`**

4. **Reemplazar valores:**
   ```bash
   # ANTES DE IMPORTAR, reemplazar:
   SPRING_R2DBC_URL=r2dbc:postgresql://TU_HOST:5432/TU_DATABASE
   SPRING_R2DBC_USERNAME=TU_USUARIO
   SPRING_R2DBC_PASSWORD=TU_PASSWORD
   ```

5. **Click "Add" o "Import"**

### Método 2: Manual

Agregar una por una en Render Environment:

| Variable | Valor | Secret? |
|----------|-------|---------|
| `SPRING_R2DBC_URL` | `r2dbc:postgresql://...` | ✅ Yes |
| `SPRING_R2DBC_USERNAME` | `franchiseuser` | ✅ Yes |
| `SPRING_R2DBC_PASSWORD` | `***` | ✅ Yes |
| `SPRING_PROFILES_ACTIVE` | `production` | ❌ No |
| `SERVER_PORT` | `8080` | ❌ No |

---

## 🔐 Obtener Credenciales de PostgreSQL en Render

### Paso 1: Crear PostgreSQL Database

```
Render Dashboard → New → PostgreSQL

Settings:
- Name: franchise-postgresql
- Database: franchisedb
- Plan: Free
```

### Paso 2: Copiar Conexión

Después de crear, ve a la database y copia:

```
Internal Database URL (ejemplo):
postgresql://franchiseuser:abc123@dpg-xxxxx.oregon-postgres.render.com:5432/franchisedb

Convertir a R2DBC:
r2dbc:postgresql://franchiseuser:abc123@dpg-xxxxx.oregon-postgres.render.com:5432/franchisedb
```

### Paso 3: Separar Componentes

```bash
SPRING_R2DBC_URL=r2dbc:postgresql://dpg-xxxxx.oregon-postgres.render.com:5432/franchisedb
SPRING_R2DBC_USERNAME=franchiseuser
SPRING_R2DBC_PASSWORD=abc123
```

---

## 📝 Variables Explicadas

### Obligatorias

#### `SPRING_R2DBC_URL`
```bash
# Formato:
r2dbc:postgresql://HOST:PORT/DATABASE

# Ejemplo:
r2dbc:postgresql://dpg-abc123.oregon-postgres.render.com:5432/franchisedb

# Componentes:
# - Protocolo: r2dbc:postgresql://
# - Host: dpg-abc123.oregon-postgres.render.com
# - Puerto: 5432
# - Database: franchisedb
```

#### `SPRING_R2DBC_USERNAME`
```bash
# Usuario de PostgreSQL
# Ejemplo: franchiseuser
```

#### `SPRING_R2DBC_PASSWORD`
```bash
# Contraseña de PostgreSQL
# Ejemplo: xJ9$kL2mP8qR
```

### Recomendadas

#### `SPRING_PROFILES_ACTIVE`
```bash
# Valores: development, production
production

# ¿Qué hace?
# - development: Más logging, Swagger habilitado
# - production: Menos logging, optimizado
```

#### `SERVER_PORT`
```bash
# Puerto donde Spring Boot escucha
8080

# Nota: Render maneja el puerto externo automáticamente
```

### Opcionales

#### `LOGGING_LEVEL_*`
```bash
# Niveles: TRACE, DEBUG, INFO, WARN, ERROR

# General
LOGGING_LEVEL_ROOT=INFO

# R2DBC (conexión BD)
LOGGING_LEVEL_IO_R2DBC=INFO

# Spring Data
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_DATA_R2DBC=INFO
```

#### `SPRINGDOC_SWAGGER_UI_ENABLED`
```bash
# true = Swagger accesible
# false = Swagger deshabilitado (más seguro en producción)
true
```

---

## 🔄 Desarrollo Local vs Producción

### Local (`.env` o `application.yaml`)
```bash
SPRING_R2DBC_URL=r2dbc:postgresql://localhost:5432/mydatabase
SPRING_R2DBC_USERNAME=myuser
SPRING_R2DBC_PASSWORD=secret
SPRING_PROFILES_ACTIVE=development
SPRING_DOCKER_COMPOSE_ENABLED=true
```

### Producción Render (`.env.render`)
```bash
SPRING_R2DBC_URL=r2dbc:postgresql://dpg-xxxxx.render.com:5432/franchisedb
SPRING_R2DBC_USERNAME=franchiseuser
SPRING_R2DBC_PASSWORD=secure_password
SPRING_PROFILES_ACTIVE=production
SPRING_DOCKER_COMPOSE_ENABLED=false
```

---

## 🎯 Pasos para Configurar

### 1. Preparar archivo local
```bash
# Copiar template
cp .env.render .env

# Editar con tus valores reales
vim .env
```

### 2. Obtener credenciales de Render
```
Dashboard → PostgreSQL Database → Connection Details
```

### 3. Actualizar .env
```bash
SPRING_R2DBC_URL=r2dbc:postgresql://TU_HOST_REAL:5432/TU_BD
SPRING_R2DBC_USERNAME=TU_USUARIO_REAL
SPRING_R2DBC_PASSWORD=TU_PASSWORD_REAL
```

### 4. Importar en Render
```
Service → Environment → Add from .env
Copiar contenido de tu .env
Save Changes
```

### 5. Deploy
```
Render detecta cambios → Auto-deploy
o
Manual Deploy
```

---

## ✅ Verificar Configuración

### En Render Logs
```bash
# Buscar estas líneas al iniciar:
✅ "Started FranchiseManagementApiApplication"
✅ "Netty started on port 8080"
✅ "r2dbc:postgresql://dpg-xxxxx..."

# Si ves errores:
❌ "Unable to connect to database"
   → Verificar SPRING_R2DBC_URL
❌ "Authentication failed"
   → Verificar USERNAME y PASSWORD
```

### Probar Health Endpoint
```bash
curl https://tu-app.onrender.com/actuator/health

# Respuesta esperada:
{
  "status": "UP"
}
```

---

## 🔒 Seguridad

### ❌ NO Commitear
```bash
# Agregar a .gitignore:
.env
.env.local
.env.production
*.env

# Solo commitear:
✅ .env.example
✅ .env.render (sin valores reales)
```

### ✅ Marcar como Secret en Render
```
Variables sensibles:
- SPRING_R2DBC_URL → Secret ✅
- SPRING_R2DBC_USERNAME → Secret ✅
- SPRING_R2DBC_PASSWORD → Secret ✅

Variables públicas:
- SPRING_PROFILES_ACTIVE → Plain
- SERVER_PORT → Plain
```

---

## 📋 Template para Copy/Paste

### Mínimo Requerido (Render)
```bash
SPRING_R2DBC_URL=r2dbc:postgresql://HOST:5432/DATABASE
SPRING_R2DBC_USERNAME=username
SPRING_R2DBC_PASSWORD=password
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
```

### Configuración Completa (Render)
```bash
SPRING_R2DBC_URL=r2dbc:postgresql://HOST:5432/DATABASE
SPRING_R2DBC_USERNAME=username
SPRING_R2DBC_PASSWORD=password
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
LOGGING_LEVEL_IO_R2DBC=INFO
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_DATA_R2DBC=INFO
SPRINGDOC_SWAGGER_UI_ENABLED=true
SPRING_DOCKER_COMPOSE_ENABLED=false
```

---

## 🐛 Troubleshooting

### Error: "Property 'url' threw exception"
```bash
# Problema: URL mal formada
# Solución: Verificar que empieza con r2dbc:postgresql://
SPRING_R2DBC_URL=r2dbc:postgresql://...  # ✅ Correcto
SPRING_R2DBC_URL=postgresql://...        # ❌ Incorrecto
```

### Error: "Connection refused"
```bash
# Problema: Host o puerto incorrecto
# Solución: Usar Internal Connection String de Render
# Verificar que el host termina en .render.com
```

### Error: "Authentication failed"
```bash
# Problema: Usuario o password incorrecto
# Solución: Copiar exactamente desde Render PostgreSQL dashboard
```

---

**Archivos creados:**
- ✅ `.env.example` - Template completo
- ✅ `.env.render` - Para importar en Render
- ✅ `ENV_SETUP.md` - Esta guía

**Próximo paso:** Copiar `.env.render`, actualizar valores y importar en Render! 🚀