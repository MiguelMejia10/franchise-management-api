# Acceso a Swagger UI

## ✅ Problema Resuelto

Se actualizó **springdoc-openapi** de versión `2.3.0` a `2.6.0` para compatibilidad con Spring Boot 4.0.6.

## 🚀 Cómo Iniciar la Aplicación

### 1. Iniciar Base de Datos MySQL
```bash
docker-compose up -d
```

Esto iniciará MySQL en el puerto 3306.

### 2. Ejecutar la Aplicación
```bash
./gradlew bootRun
```

La aplicación iniciará en el puerto 8080.

## 📚 URLs Disponibles

### Swagger UI (Interfaz Interactiva)
```
http://localhost:8080/swagger-ui.html
```
O también:
```
http://localhost:8080/swagger-ui/index.html
```

### API Docs (JSON)
```
http://localhost:8080/v3/api-docs
```

### Actuator Health Check
```
http://localhost:8080/actuator/health
```

### Actuator Endpoints
```
http://localhost:8080/actuator
```

## 🧪 Probar Endpoints desde Swagger UI

Una vez en http://localhost:8080/swagger-ui.html verás todos los endpoints:

### 1. Crear Franquicia
- **POST** `/api/franchises`
- Body:
```json
{
  "name": "Mi Franquicia"
}
```

### 2. Agregar Sucursal
- **POST** `/api/franchises/{franchiseId}/branches`
- Body:
```json
{
  "name": "Sucursal Norte"
}
```

### 3. Agregar Producto
- **POST** `/api/franchises/branches/{branchId}/products`
- Body:
```json
{
  "name": "Laptop",
  "stock": 50
}
```

### 4. Obtener Productos con Mayor Stock
- **GET** `/api/franchises/{franchiseId}/top-stock-products`

## ⚠️ Troubleshooting

### Si la aplicación no inicia:
```bash
# Verificar que MySQL está corriendo
docker ps

# Verificar logs de MySQL
docker logs <container-id>

# Reiniciar MySQL
docker-compose restart
```

### Si Swagger no carga:
```bash
# Verificar que la app está corriendo
curl http://localhost:8080/actuator/health

# Ver logs de la aplicación
./gradlew bootRun
```

### Si hay error de conexión a BD:
```bash
# Verificar configuración en application.yaml
cat src/main/resources/application.yaml

# Debe tener:
# spring.r2dbc.url: r2dbc:mysql://localhost:3306/mydatabase
# spring.r2dbc.username: myuser
# spring.r2dbc.password: secret
```

## 🎯 Flujo Completo de Prueba

```bash
# 1. Iniciar MySQL
docker-compose up -d

# 2. Ejecutar app
./gradlew bootRun

# 3. Abrir Swagger
open http://localhost:8080/swagger-ui.html

# 4. O usar el script de pruebas
./test-api.sh
```

## 🔧 Cambio Realizado

### Antes (Con Error)
```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0'
```

### Ahora (Funcional)
```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webflux-ui:2.6.0'
```

## ✅ URLs de Acceso Rápido

Copia y pega en tu navegador:

- 🌐 **Swagger UI**: http://localhost:8080/swagger-ui.html
- 📄 **API Docs**: http://localhost:8080/v3/api-docs
- ❤️ **Health**: http://localhost:8080/actuator/health
- 📊 **Metrics**: http://localhost:8080/actuator/metrics

¡Listo para usar! 🚀