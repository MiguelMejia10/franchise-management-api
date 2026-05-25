# MySQL en Render - Guía Paso a Paso

## Paso 1: Crear MySQL Database en Render

### 1.1 Acceder a Render Dashboard
```
https://dashboard.render.com
```

### 1.2 Crear Nueva Base de Datos
1. Click en **"New +"** (esquina superior derecha)
2. Seleccionar **"MySQL"**

### 1.3 Configurar Base de Datos

**Formulario de creación:**

| Campo | Valor Recomendado | Descripción |
|-------|-------------------|-------------|
| **Name** | `franchise-mysql` | Nombre identificador |
| **Database** | `franchisedb` | Nombre de la base de datos |
| **User** | `franchiseuser` | Usuario MySQL (auto-generado) |
| **Region** | `Oregon (US West)` | Más cercano a ti |
| **MySQL Version** | `8.0` (default) | Versión de MySQL |
| **Instance Type** | **Free** | 1 GB storage, $0/mes |

**Opciones de Plan:**

- **Free**: 1 GB storage, expira en 90 días de inactividad
- **Starter**: $7/mes, 10 GB storage, no expira

### 1.4 Crear Database
Click en **"Create Database"**

Render empezará a provisionar la base de datos (tarda 2-5 minutos).

---

## Paso 2: Obtener Credenciales de Conexión

### 2.1 Esperar a que esté "Available"

En el dashboard de tu MySQL database verás:
```
Status: Available ✅
```

### 2.2 Ir a la Pestaña "Info"

Aquí encontrarás:

#### **Internal Database URL** (Recomendado - más rápido)
```
mysql://franchiseuser:abc123xyz456@dpg-ct123abc456-a.oregon-postgres.render.com/franchisedb
```

#### **External Database URL** (Público)
```
mysql://franchiseuser:abc123xyz456@dpg-ct123abc456-a.oregon-postgres.render.com/franchisedb
```

#### **Desglose de Credenciales:**
```
Hostname: dpg-ct123abc456-a.oregon-postgres.render.com
Port: 3306
Database: franchisedb
Username: franchiseuser
Password: abc123xyz456 (ejemplo, será diferente)
```

---

## Paso 3: Convertir a Formato R2DBC

### 3.1 URL Original de Render
```
mysql://franchiseuser:pass@dpg-xxxxx.oregon-postgres.render.com/franchisedb
```

### 3.2 Convertir a R2DBC
**Formato R2DBC:**
```
r2dbc:mysql://HOST:PORT/DATABASE
```

**Ejemplo Real:**
```
Original (Render):
mysql://franchiseuser:abc123@dpg-ct123abc456-a.oregon-postgres.render.com/franchisedb

Convertido a R2DBC:
r2dbc:mysql://dpg-ct123abc456-a.oregon-postgres.render.com:3306/franchisedb
```

### 3.3 Separar Componentes para Variables de Entorno

```bash
SPRING_R2DBC_URL=r2dbc:mysql://dpg-ct123abc456-a.oregon-postgres.render.com:3306/franchisedb
SPRING_R2DBC_USERNAME=franchiseuser
SPRING_R2DBC_PASSWORD=abc123xyz456
```

---

## Paso 4: Configurar Variables en Web Service

### 4.1 Ir a tu Web Service
```
Dashboard → Services → franchise-management-api
```

### 4.2 Abrir Configuración de Environment
```
Service → Environment tab
```

### 4.3 Agregar Variables de Entorno

**Opción A: Bulk Import (Más Rápido)**

1. Click en **"Add from .env"**
2. Copiar contenido de `.env.render` (con valores reales):

```bash
SPRING_R2DBC_URL=r2dbc:mysql://dpg-ct123abc456-a.oregon-postgres.render.com:3306/franchisedb
SPRING_R2DBC_USERNAME=franchiseuser
SPRING_R2DBC_PASSWORD=abc123xyz456
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
LOGGING_LEVEL_IO_R2DBC=INFO
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_DATA_R2DBC=INFO
SPRINGDOC_SWAGGER_UI_ENABLED=true
SPRING_DOCKER_COMPOSE_ENABLED=false
```

3. Click **"Save Changes"**

**Opción B: Manual (Una por una)**

Click en **"Add Environment Variable"** y agregar:

| Key | Value | Type |
|-----|-------|------|
| `SPRING_R2DBC_URL` | `r2dbc:mysql://dpg-xxxxx:3306/franchisedb` | Secret ✅ |
| `SPRING_R2DBC_USERNAME` | `franchiseuser` | Secret ✅ |
| `SPRING_R2DBC_PASSWORD` | `abc123xyz456` | Secret ✅ |
| `SPRING_PROFILES_ACTIVE` | `production` | Plain |
| `SERVER_PORT` | `8080` | Plain |

**IMPORTANTE:** Marcar las credenciales de BD como **"Secret"** para que no se muestren en logs.

### 4.4 Guardar Cambios
Click en **"Save Changes"** (abajo de la página)

---

## Paso 5: Deploy y Verificar

### 5.1 Triggerar Deploy

Después de guardar las variables, Render hará **auto-deploy** automáticamente.

O manualmente:
```
Service → Manual Deploy → Deploy latest commit
```

### 5.2 Monitorear Logs

```
Service → Logs tab
```

**Buscar estas líneas de éxito:**

```bash
✅ "Starting FranchiseManagementApiApplication"
✅ "Netty started on port 8080"
✅ "Started FranchiseManagementApiApplication in X seconds"
```

**Si hay errores de conexión:**

```bash
❌ "Unable to connect to database"
   → Verificar SPRING_R2DBC_URL formato correcto

❌ "Authentication failed for user"
   → Verificar SPRING_R2DBC_USERNAME y PASSWORD

❌ "Unknown database 'franchisedb'"
   → Verificar nombre de database en URL
```

### 5.3 Verificar Health Endpoint

```bash
curl https://franchise-management-api.onrender.com/actuator/health
```

**Respuesta esperada:**
```json
{
  "status": "UP"
}
```

### 5.4 Probar Swagger UI

```
https://franchise-management-api.onrender.com/swagger-ui.html
```

### 5.5 Probar Endpoint de API

```bash
curl -X POST https://franchise-management-api.onrender.com/api/franchises \
  -H "Content-Type: application/json" \
  -d '{"name": "Mi Primera Franquicia"}'
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "name": "Mi Primera Franquicia"
}
```

---

## Paso 6: Conectar a MySQL (Opcional - Para Verificar)

### Usando MySQL Client Local

```bash
mysql -h dpg-ct123abc456-a.oregon-postgres.render.com \
      -u franchiseuser \
      -p \
      franchisedb
```

**Ingresar password cuando se solicite.**

### Verificar Tablas Creadas

```sql
SHOW TABLES;

-- Deberías ver:
-- +------------------------+
-- | Tables_in_franchisedb  |
-- +------------------------+
-- | franchises             |
-- | branches               |
-- | products               |
-- +------------------------+
```

---

## Resumen de URLs y Credenciales

### MySQL Database Info

```
Dashboard URL: https://dashboard.render.com/mysql/[tu-mysql-id]

Connection Details:
- Internal URL: mysql://user:pass@host/db
- Hostname: dpg-xxxxx.oregon-postgres.render.com
- Port: 3306
- Database: franchisedb
- Username: franchiseuser
- Password: [copiar de Render]
```

### Web Service Info

```
Dashboard URL: https://dashboard.render.com/web/[tu-service-id]

Public URL: https://franchise-management-api.onrender.com

Environment Variables: 5 configuradas
- SPRING_R2DBC_URL (Secret)
- SPRING_R2DBC_USERNAME (Secret)
- SPRING_R2DBC_PASSWORD (Secret)
- SPRING_PROFILES_ACTIVE (Plain)
- SERVER_PORT (Plain)
```

---

## Troubleshooting Común

### Error: "Pool has been disposed"

**Causa:** Conexión a BD cerrada incorrectamente

**Solución:**
```bash
# Agregar esta variable:
SPRING_R2DBC_POOL_INITIAL_SIZE=5
SPRING_R2DBC_POOL_MAX_SIZE=10
```

### Error: "SSL connection required"

**Causa:** MySQL requiere SSL en producción

**Solución:**
```bash
# Modificar URL para incluir SSL:
SPRING_R2DBC_URL=r2dbc:mysql://host:3306/db?useSSL=true&requireSSL=true
```

### Error: "Too many connections"

**Causa:** Free tier de MySQL tiene límite de conexiones

**Solución:**
- Reducir pool de conexiones
- Upgrade a Starter plan ($7/mo)

### Database inactiva después de 90 días (Free tier)

**Causa:** Free tier expira si no se usa

**Solución:**
- Upgrade a Starter plan ($7/mo)
- Hacer queries periódicos para mantenerla activa

---

## Checklist Final

- [ ] MySQL database creada en Render
- [ ] Estado: "Available"
- [ ] Credenciales copiadas (URL, username, password)
- [ ] URL convertida a formato R2DBC
- [ ] Variables de entorno agregadas al Web Service
- [ ] Variables marcadas como "Secret"
- [ ] Deploy completado exitosamente
- [ ] Logs muestran "Started FranchiseManagementApiApplication"
- [ ] Health endpoint responde `{"status":"UP"}`
- [ ] Swagger UI accesible
- [ ] API puede crear franquicias

---

## Próximos Pasos

1. **Probar todos los endpoints:**
   - POST `/api/franchises` - Crear franquicia
   - POST `/api/branches` - Agregar sucursal
   - POST `/api/products` - Agregar producto
   - PATCH `/api/products/{id}` - Actualizar stock
   - DELETE `/api/products/{id}` - Eliminar producto
   - GET `/api/franchises/{id}/top-products` - Top productos

2. **Monitorear métricas:**
   - Service → Metrics
   - Ver CPU, memoria, requests

3. **Configurar alertas** (opcional):
   - Render puede notificar si el servicio falla

4. **Backup de base de datos** (Starter plan):
   - Render hace backups automáticos
   - Free tier NO tiene backups

---

¡MySQL desplegado exitosamente en Render! 🚀