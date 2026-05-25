# PostgreSQL en Render - Guía Paso a Paso

## Paso 1: Crear PostgreSQL Database en Render

### 1.1 Acceder a Render Dashboard
```
https://dashboard.render.com
```

### 1.2 Crear Nueva Base de Datos
1. Click en **"New +"** (esquina superior derecha)
2. Seleccionar **"PostgreSQL"**

### 1.3 Configurar Base de Datos

**Formulario de creación:**

| Campo | Valor Recomendado | Descripción |
|-------|-------------------|-------------|
| **Name** | `franchise-postgres` | Nombre identificador |
| **Database** | `franchisedb` | Nombre de la base de datos |
| **User** | `franchiseuser` | Usuario PostgreSQL (auto-generado) |
| **Region** | `Oregon (US West)` | Más cercano a ti |
| **PostgreSQL Version** | `16` (default) | Versión de PostgreSQL |
| **Instance Type** | **Free** | 1 GB storage, $0/mes |

**Opciones de Plan:**

- **Free**: 1 GB storage, 90 días de expiración si no se usa, sin backups
- **Starter**: $7/mes, 10 GB storage, backups diarios, no expira
- **Standard**: $20/mes, 50 GB storage, backups continuos

### 1.4 Crear Database
Click en **"Create Database"**

Render empezará a provisionar la base de datos (tarda 1-3 minutos).

---

## Paso 2: Obtener Credenciales de Conexión

### 2.1 Esperar a que esté "Available"

En el dashboard de tu PostgreSQL database verás:
```
Status: Available ✅
```

### 2.2 Ir a la Pestaña "Info"

Aquí encontrarás:

#### **Internal Database URL** (Recomendado - más rápido)
```
postgres://franchiseuser:abc123xyz456@dpg-ct123abc456-a.oregon-postgres.render.com/franchisedb
```

#### **External Database URL** (Público)
```
postgres://franchiseuser:abc123xyz456@dpg-ct123abc456-a.oregon-postgres.render.com/franchisedb
```

#### **Desglose de Credenciales:**
```
Hostname: dpg-ct123abc456-a.oregon-postgres.render.com
Port: 5432
Database: franchisedb
Username: franchiseuser
Password: abc123xyz456 (ejemplo, será diferente)
```

---

## Paso 3: Convertir a Formato R2DBC

### 3.1 URL Original de Render
```
postgres://franchiseuser:pass@dpg-xxxxx.oregon-postgres.render.com/franchisedb
```

### 3.2 Convertir a R2DBC
**Formato R2DBC:**
```
r2dbc:postgresql://HOST:PORT/DATABASE
```

**Ejemplo Real:**
```
Original (Render):
postgres://franchiseuser:abc123@dpg-ct123abc456-a.oregon-postgres.render.com/franchisedb

Convertido a R2DBC:
r2dbc:postgresql://dpg-ct123abc456-a.oregon-postgres.render.com:5432/franchisedb
```

### 3.3 Separar Componentes para Variables de Entorno

```bash
SPRING_R2DBC_URL=r2dbc:postgresql://dpg-ct123abc456-a.oregon-postgres.render.com:5432/franchisedb
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
SPRING_R2DBC_URL=r2dbc:postgresql://dpg-ct123abc456-a.oregon-postgres.render.com:5432/franchisedb
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
| `SPRING_R2DBC_URL` | `r2dbc:postgresql://dpg-xxxxx:5432/franchisedb` | Secret ✅ |
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

## Paso 6: Conectar a PostgreSQL (Opcional - Para Verificar)

### Usando psql Client Local

```bash
psql -h dpg-ct123abc456-a.oregon-postgres.render.com \
     -U franchiseuser \
     -d franchisedb
```

**Ingresar password cuando se solicite.**

### Verificar Tablas Creadas

```sql
\dt

-- Deberías ver:
--  Schema |    Name     | Type  |     Owner
-- --------+-------------+-------+---------------
--  public | franchises  | table | franchiseuser
--  public | branches    | table | franchiseuser
--  public | products    | table | franchiseuser
```

### Ver Estructura de Tablas

```sql
\d franchises

-- Deberías ver:
--                                Table "public.franchises"
--  Column |          Type          | Collation | Nullable |                Default
-- --------+------------------------+-----------+----------+---------------------------------------
--  id     | bigint                 |           | not null | nextval('franchises_id_seq'::regclass)
--  name   | character varying(255) |           | not null |
```

---

## Resumen de URLs y Credenciales

### PostgreSQL Database Info

```
Dashboard URL: https://dashboard.render.com/postgres/[tu-postgres-id]

Connection Details:
- Internal URL: postgres://user:pass@host/db
- Hostname: dpg-xxxxx.oregon-postgres.render.com
- Port: 5432
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
# Agregar estas variables:
SPRING_R2DBC_POOL_INITIAL_SIZE=5
SPRING_R2DBC_POOL_MAX_SIZE=10
```

### Error: "SSL connection required"

**Causa:** PostgreSQL en Render requiere SSL

**Solución:**
```bash
# Modificar URL para incluir SSL:
SPRING_R2DBC_URL=r2dbc:postgresql://host:5432/db?sslMode=require
```

### Error: "FATAL: remaining connection slots are reserved"

**Causa:** Free tier de PostgreSQL tiene límite de 97 conexiones

**Solución:**
- Reducir pool de conexiones
- Upgrade a Starter plan ($7/mo con 113 conexiones)

### Error: "relation does not exist"

**Causa:** Tablas no creadas o schema incorrecto

**Solución:**
- Verificar que schema.sql se ejecutó correctamente
- Verificar logs al iniciar la aplicación
- Conectar con psql y verificar: `\dt`

### Database inactiva después de 90 días (Free tier)

**Causa:** Free tier expira si no se usa por 90 días

**Solución:**
- Upgrade a Starter plan ($7/mo)
- Hacer queries periódicos para mantenerla activa

---

## Ventajas de PostgreSQL vs MySQL

### ✅ En Render Free Tier

| Característica | PostgreSQL | MySQL |
|---------------|------------|-------|
| Disponibilidad | ✅ Gratis | ❌ No disponible |
| Storage | 1 GB | N/A |
| Conexiones | 97 | N/A |
| Backups | ❌ No | N/A |
| Precio | $0 | N/A |

### ✅ Características de PostgreSQL

- **Compliance con SQL**: PostgreSQL es más estricto con estándares SQL
- **JSON nativo**: Mejor soporte para JSON/JSONB
- **Extensiones**: PostGIS, pg_trgm, etc.
- **ACID completo**: Mayor consistencia en transacciones
- **Tipos de datos avanzados**: Arrays, HSTORE, UUID nativo
- **Full-text search**: Búsqueda de texto integrada

---

## Checklist Final

- [ ] PostgreSQL database creada en Render
- [ ] Estado: "Available"
- [ ] Credenciales copiadas (URL, username, password)
- [ ] URL convertida a formato R2DBC (`r2dbc:postgresql://...`)
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

4. **Backup de base de datos** (solo Starter plan):
   - Render hace backups automáticos diarios
   - Free tier NO tiene backups (hacer dumps manuales si es crítico)

---

## Comandos Útiles PostgreSQL

### Backup Manual (desde local)
```bash
pg_dump -h dpg-xxxxx.oregon-postgres.render.com \
        -U franchiseuser \
        -d franchisedb \
        -F c -b -v -f backup.dump
```

### Restore Manual
```bash
pg_restore -h dpg-xxxxx.oregon-postgres.render.com \
           -U franchiseuser \
           -d franchisedb \
           -v backup.dump
```

### Ver Conexiones Activas
```sql
SELECT count(*) FROM pg_stat_activity;
```

### Ver Tamaño de Base de Datos
```sql
SELECT pg_size_pretty(pg_database_size('franchisedb'));
```

---

¡PostgreSQL desplegado exitosamente en Render! 🚀