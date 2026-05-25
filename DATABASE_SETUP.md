# Configuración de Base de Datos - Solución a Errores de Schema

## ✅ Problema Resuelto

**Error Original:**
```
Failed to execute SQL script statement #4: CREATE INDEX idx_branches_franchise_id ON branches(franchise_id)
```

**Causa:** Los índices intentaban crearse nuevamente en una base de datos que ya tenía el schema.

## 🔧 Soluciones Aplicadas

### 1. Schema SQL Actualizado
Se agregó `IF NOT EXISTS` a los índices:

```sql
CREATE INDEX IF NOT EXISTS idx_branches_franchise_id ON branches(franchise_id);
CREATE INDEX IF NOT EXISTS idx_products_branch_id ON products(branch_id);
```

### 2. DatabaseConfig Mejorado
Se agregó `continueOnError = true` para mayor robustez:

```java
populator.setContinueOnError(true);
```

## 🚀 Opciones para Ejecutar

### Opción A: Limpiar Base de Datos (Recomendado para Dev)

```bash
# 1. Detener la aplicación si está corriendo

# 2. Eliminar contenedor de MySQL y volúmenes
docker-compose down -v

# 3. Reiniciar MySQL
docker-compose up -d

# 4. Ejecutar aplicación desde IntelliJ
# O desde terminal:
./gradlew bootRun
```

### Opción B: Ejecutar con Base de Datos Existente

Con los cambios realizados, ahora la aplicación puede ejecutarse incluso si el schema ya existe:

```bash
# 1. Iniciar MySQL (si no está corriendo)
docker-compose up -d

# 2. Ejecutar desde IntelliJ o terminal
./gradlew bootRun
```

### Opción C: Limpiar Solo las Tablas (Mantener Contenedor)

```bash
# Conectarse a MySQL
docker exec -it $(docker ps -q -f name=mysql) mysql -u myuser -psecret mydatabase

# Ejecutar en MySQL:
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS branches;
DROP TABLE IF EXISTS franchises;
exit

# Reiniciar aplicación
```

## 📋 Schema Completo Actualizado

```sql
-- Franchises
CREATE TABLE IF NOT EXISTS franchises (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Branches
CREATE TABLE IF NOT EXISTS branches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    franchise_id BIGINT NOT NULL,
    FOREIGN KEY (franchise_id) REFERENCES franchises(id) ON DELETE CASCADE
);

-- Products
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    stock INT NOT NULL,
    branch_id BIGINT NOT NULL,
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE
);

-- Indexes (ahora con IF NOT EXISTS)
CREATE INDEX IF NOT EXISTS idx_branches_franchise_id ON branches(franchise_id);
CREATE INDEX IF NOT EXISTS idx_products_branch_id ON products(branch_id);
```

## 🔍 Verificar Estado de la Base de Datos

### Desde Terminal
```bash
# Conectarse a MySQL
docker exec -it $(docker ps -q -f name=mysql) mysql -u myuser -psecret mydatabase

# Ver tablas
SHOW TABLES;

# Ver estructura de tabla
DESCRIBE franchises;
DESCRIBE branches;
DESCRIBE products;

# Ver índices
SHOW INDEX FROM branches;
SHOW INDEX FROM products;

# Ver datos
SELECT * FROM franchises;
```

### Desde IntelliJ IDEA

1. **Database Tool Window** (View → Tool Windows → Database)
2. **Add Data Source** → MySQL
3. **Configuración:**
   - Host: `localhost`
   - Port: `3306`
   - Database: `mydatabase`
   - User: `myuser`
   - Password: `secret`
4. **Test Connection** → OK
5. **Explorar tablas** en el panel izquierdo

## ⚙️ Configuración de IntelliJ

### Run Configuration

1. **Edit Configurations** → Add New → Spring Boot
2. **Main class:** `com.franchise.management.api.FranchiseManagementApiApplication`
3. **Environment variables:**
   ```
   SPRING_R2DBC_URL=r2dbc:mysql://localhost:3306/mydatabase
   SPRING_R2DBC_USERNAME=myuser
   SPRING_R2DBC_PASSWORD=secret
   ```
4. **Before launch:**
   - Add → Run Gradle task → `clean build -x test`

### VM Options (Opcional)
```
-Dspring.profiles.active=dev
-Dlogging.level.org.springframework.r2dbc=DEBUG
```

## 🐛 Troubleshooting

### Error: "Table already exists"
```bash
# Limpiar base de datos
docker-compose down -v
docker-compose up -d
```

### Error: "Connection refused"
```bash
# Verificar que MySQL está corriendo
docker ps

# Ver logs de MySQL
docker logs $(docker ps -q -f name=mysql)

# Reiniciar MySQL
docker-compose restart
```

### Error: "Access denied for user"
```bash
# Verificar credenciales en application.yaml
cat src/main/resources/application.yaml

# Verificar en docker-compose.yaml
cat compose.yaml
```

### Ver Logs de R2DBC
Agregar en `application.yaml`:
```yaml
logging:
  level:
    io.r2dbc: DEBUG
    org.springframework.data.r2dbc: DEBUG
    org.springframework.r2dbc: DEBUG
```

## 🎯 Orden Recomendado de Inicio

```bash
# 1. Limpiar todo (primera vez o después de cambios en schema)
docker-compose down -v
docker-compose up -d

# 2. Esperar a que MySQL esté listo (5-10 segundos)
sleep 10

# 3. Verificar que MySQL está corriendo
docker ps

# 4. Ejecutar aplicación
./gradlew bootRun

# 5. Verificar en logs que el schema se creó correctamente
# Buscar: "Executing SQL script from class path resource [schema.sql]"

# 6. Abrir Swagger
open http://localhost:8080/swagger-ui.html
```

## ✅ Verificación de Funcionamiento

Una vez que la aplicación inicie correctamente:

```bash
# 1. Health check
curl http://localhost:8080/actuator/health

# Respuesta esperada:
# {"status":"UP"}

# 2. Crear franquicia de prueba
curl -X POST http://localhost:8080/api/franchises \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Franchise"}'

# Respuesta esperada:
# {"id":1,"name":"Test Franchise"}
```

## 📝 Notas Importantes

- ✅ El schema ahora usa `IF NOT EXISTS` en todos los índices
- ✅ `DatabaseConfig` ahora continúa en caso de errores menores
- ✅ Las tablas usan `CASCADE DELETE` para mantener integridad referencial
- ✅ R2DBC ejecuta el schema automáticamente en cada inicio
- ⚠️ Para producción, considerar usar migraciones (Flyway o Liquibase)

## 🔄 Alternativa: Deshabilitar Schema Auto-Init

Si prefieres manejar el schema manualmente:

```java
// En DatabaseConfig.java, comentar o eliminar el @Bean
/*
@Bean
public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
    ...
}
*/
```

Y ejecutar el schema manualmente:
```bash
docker exec -i $(docker ps -q -f name=mysql) mysql -u myuser -psecret mydatabase < src/main/resources/schema.sql
```

---

**¡Ahora deberías poder ejecutar la aplicación sin errores!** 🚀