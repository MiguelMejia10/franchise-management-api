# Deploy en Render - Guía Completa

## 🚀 Deploy Rápido en Render

### Opción 1: Deploy con Blueprint (Recomendado)

1. **Ir a Render Dashboard**
   - https://dashboard.render.com

2. **New → Blueprint**
   - Conectar repositorio de GitHub
   - Render detectará automáticamente `render.yaml`

3. **Configurar Variables de Entorno** (Ver abajo)

4. **Deploy**
   - Click "Apply" y esperar ~5-10 minutos

### Opción 2: Deploy Manual

1. **Crear Base de Datos PostgreSQL**
   ```
   New → PostgreSQL
   - Name: franchise-postgresql
   - Plan: Free
   - Database: franchisedb
   - User: franchiseuser
   ```

2. **Crear Web Service**
   ```
   New → Web Service
   - Connect GitHub repo
   - Runtime: Docker
   - Plan: Free
   - Build Command: (vacío)
   - Start Command: (vacío - usa Dockerfile)
   ```

3. **Configurar Variables de Entorno** (Ver abajo)

---

## 🔐 Variables de Entorno Requeridas

### Configuración en Render Dashboard

Ve a: **Service → Environment**

#### Variables Obligatorias:

```bash
# Conexión a Base de Datos
SPRING_R2DBC_URL=r2dbc:postgresql://HOST:PORT/DATABASE
SPRING_R2DBC_USERNAME=user
SPRING_R2DBC_PASSWORD=password

# Perfil de Spring
SPRING_PROFILES_ACTIVE=production

# Puerto (Render usa el que asigna, pero Spring debe escuchar en 8080)
SERVER_PORT=8080
```

#### Variables Opcionales:

```bash
# Logging (reducir para producción)
LOGGING_LEVEL_IO_R2DBC=INFO
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_DATA_R2DBC=INFO
LOGGING_LEVEL_ROOT=INFO

# Swagger (deshabilitar en producción si quieres)
SPRINGDOC_SWAGGER_UI_ENABLED=true
```

---

## 📋 Configuración Paso a Paso

### 1. Crear PostgreSQL Database en Render

```
Dashboard → New → PostgreSQL

Settings:
- Name: franchise-postgresql
- Database Name: franchisedb
- Plan: Free (or Starter $7/mo)
- Region: Oregon

Después de crear, copia estos valores:
✅ Internal Connection String
✅ Username
✅ Password
```

**Formato de conexión para R2DBC:**
```
Original (Render): postgresql://HOST:PORT/DATABASE
Convertir a:       r2dbc:postgresql://HOST:PORT/DATABASE
```

### 2. Crear Web Service

```
Dashboard → New → Web Service → Connect GitHub

Repository Settings:
- Repository: tu-repo/franchise-management-api
- Branch: main

Build Settings:
- Runtime: Docker
- Dockerfile Path: ./Dockerfile
- Docker Context: .
- Build Command: (leave empty)
- Start Command: (leave empty)

Plan:
- Free (512MB RAM, 0.1 CPU)
- o Starter ($7/mo - 512MB RAM, 0.5 CPU)
```

### 3. Configurar Environment Variables

En el Web Service → Environment:

```bash
# Desde PostgreSQL database que creaste
SPRING_R2DBC_URL=r2dbc:postgresql://dpg-xxxxx.oregon-postgres.render.com:5432/franchisedb
SPRING_R2DBC_USERNAME=franchiseuser
SPRING_R2DBC_PASSWORD=xxxxxxxxxxx

# Configuración adicional
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
```

**💡 Tip:** Puedes usar las "Internal Connection Strings" de Render para mejor performance.

### 4. Deploy

```
Click "Create Web Service" o "Manual Deploy"
```

Render:
1. Clona el repo
2. Ejecuta `docker build`
3. Inicia el contenedor
4. Expone en URL pública

---

## 🔗 Obtener Variables de PostgreSQL

### Después de crear PostgreSQL en Render:

1. Ve a tu PostgreSQL database en Render
2. Copia estos valores de la sección "Connections":

```
Internal Database URL:
postgresql://franchiseuser:pass@dpg-xxxxx/franchisedb

Convertir a R2DBC:
r2dbc:postgresql://franchiseuser:pass@dpg-xxxxx/franchisedb
```

**O manualmente:**
```bash
SPRING_R2DBC_URL=r2dbc:postgresql://[HOSTNAME]:[PORT]/[DATABASE]
SPRING_R2DBC_USERNAME=[USERNAME]
SPRING_R2DBC_PASSWORD=[PASSWORD]
```

---

## 📝 Ejemplo de Configuración Completa

### En Render Web Service → Environment:

| Key | Value | Tipo |
|-----|-------|------|
| `SPRING_R2DBC_URL` | `r2dbc:postgresql://dpg-xxxxx.oregon-postgres.render.com:5432/franchisedb` | Secret |
| `SPRING_R2DBC_USERNAME` | `franchiseuser` | Secret |
| `SPRING_R2DBC_PASSWORD` | `abc123xyz456` | Secret |
| `SPRING_PROFILES_ACTIVE` | `production` | Plain |
| `SERVER_PORT` | `8080` | Plain |
| `LOGGING_LEVEL_IO_R2DBC` | `INFO` | Plain |

---

## 🎯 Verificar Deployment

### 1. Ver Logs
```
Service → Logs

Buscar:
✅ "Started FranchiseManagementApiApplication"
✅ "Netty started on port 8080"
❌ Si ves errores de conexión a BD, revisar variables
```

### 2. Probar Health Endpoint
```bash
# Tu URL será algo como:
https://franchise-management-api.onrender.com/actuator/health

# Debe retornar:
{"status":"UP"}
```

### 3. Probar Swagger UI
```
https://franchise-management-api.onrender.com/swagger-ui.html
```

### 4. Probar API
```bash
curl -X POST https://franchise-management-api.onrender.com/api/franchises \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Franchise"}'
```

---

## 🐛 Troubleshooting

### Error: "Unable to connect to database"

**Causa:** Variables de entorno incorrectas

**Solución:**
1. Verificar que `SPRING_R2DBC_URL` empieza con `r2dbc:postgresql://`
2. Verificar hostname, port, database name
3. Verificar username y password
4. Usar "Internal Connection String" de Render (más rápido)

### Error: "Application failed to start"

**Causa:** Puerto incorrecto

**Solución:**
```bash
SERVER_PORT=8080  # Spring debe escuchar en 8080
```

Render asigna un puerto dinámico externamente, pero internamente usa el que configuraste.

### Error: "Health check failed"

**Causa:** Aplicación no responde en el puerto correcto

**Solución:**
1. Verificar que Dockerfile expone puerto 8080
2. Verificar logs: `Netty started on port 8080`

### Error: "Out of memory"

**Causa:** Free tier tiene 512MB RAM

**Solución:**
- Upgrade a Starter plan ($7/mo)
- O optimizar JVM options en Dockerfile:
```dockerfile
ENTRYPOINT ["java", "-Xmx400m", "-jar", "app.jar"]
```

---

## 💰 Costos

### Free Tier
- ✅ Web Service: Free (con limitaciones)
- ✅ PostgreSQL: Free (100MB storage, 90 días inactividad)
- ⚠️ Se suspende después de 15 min de inactividad
- ⚠️ Tarda ~30s en despertar (cold start)

### Starter Plan ($7/mo)
- ✅ Sin suspensión
- ✅ 512MB RAM garantizada
- ✅ Sin cold starts

---

## 🔄 CI/CD con GitHub

### Auto-Deploy en cada Push

Render detecta automáticamente cambios en:
- Branch `main` (configurable)
- Archivo `render.yaml`

**Flujo:**
```bash
git push origin main
→ Render detecta cambio
→ Build Docker image
→ Deploy automático
```

### Deshabilitar Auto-Deploy

En Render Service → Settings:
- Auto-Deploy: Off

Después hacer deploy manual:
- Click "Manual Deploy"

---

## 📊 Monitoreo

### Metrics en Render
```
Service → Metrics

Ver:
- CPU usage
- Memory usage
- Request count
- Response times
```

### Logs en Tiempo Real
```
Service → Logs → Live logs

Filtrar por:
- ERROR
- WARN
- INFO
```

---

## 🎯 Checklist de Deploy

- [ ] PostgreSQL database creada en Render
- [ ] Variables de entorno configuradas
- [ ] `SPRING_R2DBC_URL` comienza con `r2dbc:postgresql://`
- [ ] `SERVER_PORT=8080` configurado
- [ ] Web Service conectado a GitHub
- [ ] Deploy completado exitosamente
- [ ] Health check pasando (`/actuator/health`)
- [ ] Swagger UI accesible
- [ ] API respondiendo correctamente

---

## 🔗 URLs Útiles

- **Render Dashboard**: https://dashboard.render.com
- **Docs**: https://render.com/docs
- **Docker Support**: https://render.com/docs/docker
- **PostgreSQL**: https://render.com/docs/databases

---

## 📝 Archivo render.yaml

Ya incluido en el proyecto:

```yaml
services:
  - type: web
    name: franchise-management-api
    runtime: docker

    envVars:
      - key: SPRING_R2DBC_URL
        sync: false  # Configurar manualmente
      - key: SPRING_R2DBC_USERNAME
        sync: false
      - key: SPRING_R2DBC_PASSWORD
        sync: false
      - key: SPRING_PROFILES_ACTIVE
        value: production

databases:
  - name: franchise-postgresql
    databaseName: franchisedb
```

**💡 Con este archivo, solo necesitas:**
1. Push a GitHub
2. Render → New → Blueprint
3. Configurar las 3 variables de BD
4. Deploy!

---

**¡Listo para desplegar en Render!** 🚀

**Tiempo estimado:** 10-15 minutos