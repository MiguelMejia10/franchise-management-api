# Franchise Management API

API REST **Reactiva** para gestión de franquicias, sucursales y productos desarrollada con Spring Boot WebFlux.

## Tecnologías

- Java 21
- **Spring Boot 4.0.6**
- **Spring WebFlux** (Programación Reactiva)
- **Spring Data R2DBC** (Reactive Relational Database Connectivity)
- **R2DBC MySQL Driver** (Driver reactivo para MySQL)
- **Project Reactor** (Mono y Flux)
- MySQL 8
- Gradle
- Docker Compose
- Lombok
- SpringDoc OpenAPI (Swagger para WebFlux)

## ¿Por qué Programación Reactiva?

Este proyecto implementa **programación reactiva** utilizando:

- **Mono<T>**: Para operaciones que retornan 0 o 1 elemento de forma asíncrona y no bloqueante
- **Flux<T>**: Para streams de datos que retornan 0 a N elementos de forma asíncrona
- **R2DBC**: Driver de base de datos completamente reactivo y no bloqueante
- **WebFlux**: Framework web reactivo basado en Netty (no usa Tomcat)

**Ventajas:**
- ✅ **No bloqueante**: Mayor eficiencia en operaciones I/O
- ✅ **Escalabilidad**: Maneja más requests con menos recursos
- ✅ **Backpressure**: Control de flujo automático
- ✅ **Asincronía**: Ejecución concurrente sin bloqueo de threads

## Requisitos

- Java 21
- Docker y Docker Compose
- Gradle (incluido via wrapper)

## Configuración

La aplicación utiliza MySQL como base de datos. La configuración de Docker Compose está incluida en `compose.yaml`:

- Base de datos: `mydatabase`
- Usuario: `myuser`
- Contraseña: `secret`
- Puerto: `3306`

## Despliegue Local

### 1. Clonar el repositorio

```bash
git clone <repository-url>
cd franchise-management-api
```

### 2. Iniciar la base de datos

```bash
docker-compose up -d
```

### 3. Ejecutar la aplicación

```bash
./gradlew bootRun
```

La aplicación estará disponible en: `http://localhost:8080`

### 4. Acceder a la documentación API (Swagger)

```
http://localhost:8080/swagger-ui.html
```

## Endpoints Principales

### Franquicias

- **POST** `/api/franchises` - Crear nueva franquicia
- **GET** `/api/franchises` - Listar todas las franquicias
- **GET** `/api/franchises/{id}` - Obtener franquicia por ID
- **PATCH** `/api/franchises/{id}/name` - Actualizar nombre de franquicia

### Sucursales

- **POST** `/api/franchises/{franchiseId}/branches` - Agregar sucursal a franquicia
- **PATCH** `/api/franchises/branches/{branchId}/name` - Actualizar nombre de sucursal

### Productos

- **POST** `/api/franchises/branches/{branchId}/products` - Agregar producto a sucursal
- **DELETE** `/api/franchises/products/{productId}` - Eliminar producto
- **PATCH** `/api/franchises/products/{productId}/stock` - Actualizar stock de producto
- **PATCH** `/api/franchises/products/{productId}/name` - Actualizar nombre de producto

### Consultas Especiales

- **GET** `/api/franchises/{franchiseId}/top-stock-products` - Obtener productos con mayor stock por sucursal

## Modelo de Datos

### Franchise (Franquicia)
```json
{
  "id": 1,
  "name": "Franquicia Principal",
  "branches": []
}
```

### Branch (Sucursal)
```json
{
  "id": 1,
  "name": "Sucursal Centro",
  "products": []
}
```

### Product (Producto)
```json
{
  "id": 1,
  "name": "Producto A",
  "stock": 100
}
```

## Ejemplos de Uso

### Crear una franquicia

```bash
curl -X POST http://localhost:8080/api/franchises \
  -H "Content-Type: application/json" \
  -d '{"name": "Mi Franquicia"}'
```

### Agregar sucursal a franquicia

```bash
curl -X POST http://localhost:8080/api/franchises/1/branches \
  -H "Content-Type: application/json" \
  -d '{"name": "Sucursal Norte"}'
```

### Agregar producto a sucursal

```bash
curl -X POST http://localhost:8080/api/franchises/branches/1/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Laptop", "stock": 50}'
```

### Actualizar stock de producto

```bash
curl -X PATCH http://localhost:8080/api/franchises/products/1/stock \
  -H "Content-Type: application/json" \
  -d '{"stock": 75}'
```

### Obtener productos con mayor stock por sucursal

```bash
curl -X GET http://localhost:8080/api/franchises/1/top-stock-products
```

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/franchise/management/api/
│   │   ├── config/          # Configuración (R2DBC, Schema init)
│   │   ├── controller/      # Controladores REST Reactivos (retornan Mono/Flux)
│   │   ├── dto/             # DTOs para requests/responses
│   │   ├── entity/          # Entidades R2DBC (sin relaciones JPA)
│   │   ├── exception/       # Manejo global de excepciones reactivo
│   │   ├── repository/      # ReactiveCrudRepository (R2DBC)
│   │   └── service/         # Servicios reactivos con Mono/Flux
│   └── resources/
│       ├── application.yaml # Configuración R2DBC
│       └── schema.sql       # Schema DDL (R2DBC no auto-genera)
└── test/                    # Tests reactivos
```

## Docker

### Construir imagen Docker

```bash
./gradlew bootBuildImage
```

### Desplegar con Docker Compose

Crear archivo `docker-compose.full.yaml`:

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:latest
    environment:
      MYSQL_DATABASE: mydatabase
      MYSQL_USER: myuser
      MYSQL_PASSWORD: secret
      MYSQL_ROOT_PASSWORD: verysecret
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql

  app:
    image: franchise-management-api:0.0.1-SNAPSHOT
    depends_on:
      - mysql
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/mydatabase
      SPRING_DATASOURCE_USERNAME: myuser
      SPRING_DATASOURCE_PASSWORD: secret

volumes:
  mysql-data:
```

Ejecutar:
```bash
docker-compose -f docker-compose.full.yaml up -d
```

## Características Implementadas

✅ Proyecto desarrollado en Spring Boot con **Spring WebFlux**
✅ Endpoints para agregar franquicia (retorna `Mono<Franchise>`)
✅ Endpoints para agregar sucursal a franquicia (retorna `Mono<Branch>`)
✅ Endpoints para agregar producto a sucursal (retorna `Mono<Product>`)
✅ Endpoints para eliminar producto (retorna `Mono<Void>`)
✅ Endpoints para modificar stock de producto (retorna `Mono<Product>`)
✅ Endpoint para mostrar productos con mayor stock por sucursal (retorna `Flux<ProductResponse>`)
✅ Sistema de persistencia **reactivo** con **R2DBC MySQL**
✅ Endpoints para actualizar nombres de franquicia, sucursal y producto
✅ Documentación con Swagger/OpenAPI para WebFlux
✅ Manejo de excepciones global reactivo
✅ Validación de datos con Bean Validation
✅ Docker Compose para base de datos

## Puntos Extra Implementados

✅ Aplicación empaquetada con Docker
✅ **Programación Reactiva Real** con **Spring WebFlux + R2DBC**
  - Todos los endpoints retornan `Mono<T>` o `Flux<T>`
  - Operaciones no bloqueantes con `flatMap`, `map`, `switchIfEmpty`
  - Composición reactiva de operaciones I/O
  - Backpressure automático con Project Reactor
✅ Endpoints adicionales para actualizar nombres
✅ Infraestructura como código con Docker Compose
✅ Queries reactivas personalizadas con `@Query` en R2DBC
✅ Despliegue en la nube (compatible con AWS, GCP, Azure)
✅ Documentación completa con README.md

## Testing

### Tests Unitarios Implementados

El proyecto incluye **21 tests unitarios** que verifican el correcto funcionamiento de todos los componentes:

#### Tests de Controladores (9 tests)
```bash
./gradlew test --tests FranchiseControllerTest
```
- Pruebas de todos los endpoints con `WebTestClient`
- Validación de requests y responses
- Manejo de errores y validaciones

#### Tests de Servicios (12 tests)
```bash
./gradlew test --tests FranchiseServiceTest
```
- Pruebas con `StepVerifier` de reactor-test
- Verificación de flujos reactivos con `Mono` y `Flux`
- Casos de éxito y error

#### Ejecutar Todos los Tests
```bash
./gradlew test
```

**Resultado:** ✅ 21/21 tests pasando

### Script de Prueba de la API
```bash
chmod +x test-api.sh
./test-api.sh
```
Este script ejecuta un flujo completo de pruebas contra la API en ejecución.

## Build

```bash
./gradlew build
```

## Licencia

Este proyecto es una prueba técnica para Accenture.