# Programación Reactiva - Implementación Detallada

Este documento explica cómo se implementó la programación reactiva en el proyecto Franchise Management API.

## 🔄 ¿Qué es Programación Reactiva?

La programación reactiva es un paradigma de programación declarativo que se centra en flujos de datos asíncronos y la propagación de cambios. En lugar de bloquear threads esperando respuestas de operaciones I/O (como consultas a base de datos), las operaciones reactivas liberan el thread inmediatamente y continúan cuando los datos están disponibles.

## 🎯 Stack Tecnológico Reactivo

### 1. Spring WebFlux vs Spring MVC

| Spring MVC (Bloqueante) | Spring WebFlux (Reactivo) |
|------------------------|---------------------------|
| Basado en Servlet API | Basado en Reactive Streams |
| Servidor: Tomcat | Servidor: Netty |
| Modelo bloqueante | Modelo no bloqueante |
| Un thread por request | Event loop con pocos threads |
| Escalabilidad vertical | Escalabilidad horizontal |

### 2. R2DBC vs JDBC

| JDBC/JPA (Bloqueante) | R2DBC (Reactivo) |
|-----------------------|------------------|
| Conexiones bloqueantes | Conexiones no bloqueantes |
| `EntityManager` | `R2dbcEntityTemplate` |
| `JpaRepository` | `ReactiveCrudRepository` |
| Retorna objetos directos | Retorna `Mono<T>` o `Flux<T>` |
| Hibernate ORM | Sin ORM (manejo manual de relaciones) |

### 3. Project Reactor

**Mono<T>**: Publisher que emite 0 o 1 elemento
```java
Mono<Franchise> franchise = franchiseRepository.findById(1L);
```

**Flux<T>**: Publisher que emite 0 a N elementos
```java
Flux<Product> products = productRepository.findAll();
```

## 📦 Dependencias Clave

```gradle
// WebFlux en lugar de Web MVC
implementation 'org.springframework.boot:spring-boot-starter-webflux'

// R2DBC en lugar de JPA
implementation 'org.springframework.boot:spring-boot-starter-data-r2dbc'

// Driver R2DBC para MySQL
implementation 'io.asyncer:r2dbc-mysql:1.0.5'

// Swagger para WebFlux
implementation 'org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0'

// Testing reactivo
testImplementation 'io.projectreactor:reactor-test'
```

## 🏗️ Arquitectura Reactiva

### Entidades (Sin Relaciones JPA)

```java
@Table("products")
public class Product {
    @Id
    private Long id;
    private String name;
    private Integer stock;

    @Column("branch_id")
    private Long branchId;  // ⚠️ Sin @ManyToOne - relación manual
}
```

**Diferencia clave**: R2DBC no soporta relaciones como JPA (`@OneToMany`, `@ManyToOne`). Las relaciones se manejan manualmente mediante consultas.

### Repositorios Reactivos

```java
public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {

    // Retorna Flux en lugar de List
    Flux<Product> findByBranchId(Long branchId);

    // Query reactiva personalizada
    @Query("""
        SELECT p.* FROM products p
        WHERE p.branch_id = :branchId
        ORDER BY p.stock DESC
    """)
    Flux<Product> findTopProductsByBranch(Long branchId);
}
```

### Servicios Reactivos

**❌ Antes (Bloqueante):**
```java
public Product createProduct(ProductRequest request) {
    Product product = new Product();
    product.setName(request.getName());
    return productRepository.save(product);  // Bloquea el thread
}
```

**✅ Ahora (Reactivo):**
```java
public Mono<Product> createProduct(ProductRequest request) {
    Product product = new Product();
    product.setName(request.getName());
    return productRepository.save(product);  // No bloquea, retorna Mono
}
```

### Composición Reactiva

**Ejemplo 1: Validar existencia antes de crear**
```java
public Mono<Product> addProductToBranch(Long branchId, ProductRequest request) {
    return branchRepository.findById(branchId)
        .switchIfEmpty(Mono.error(new RuntimeException("Branch not found")))
        .flatMap(branch -> {
            Product product = new Product();
            product.setName(request.getName());
            product.setBranchId(branchId);
            return productRepository.save(product);
        });
}
```

**Operadores clave:**
- `flatMap`: Transforma elemento y retorna nuevo Mono/Flux (para operaciones asíncronas)
- `map`: Transforma elemento sincrónicamente
- `switchIfEmpty`: Alternativa si el stream está vacío
- `collectList()`: Convierte Flux a Mono<List>

**Ejemplo 2: Consulta con joins manuales**
```java
public Mono<FranchiseResponse> getFranchiseById(Long franchiseId) {
    return franchiseRepository.findById(franchiseId)
        .flatMap(franchise ->
            branchRepository.findByFranchiseId(franchiseId)
                .flatMap(branch ->
                    productRepository.findByBranchId(branch.getId())
                        .map(product -> new ProductSimpleResponse(...))
                        .collectList()
                        .map(products -> new BranchResponse(..., products))
                )
                .collectList()
                .map(branches -> new FranchiseResponse(..., branches))
        );
}
```

### Controladores Reactivos

**❌ Antes (MVC Bloqueante):**
```java
@PostMapping
public ResponseEntity<Product> create(@RequestBody ProductRequest request) {
    Product product = service.createProduct(request);
    return ResponseEntity.ok(product);
}
```

**✅ Ahora (WebFlux Reactivo):**
```java
@PostMapping
public Mono<Product> create(@RequestBody ProductRequest request) {
    return service.createProduct(request);
}
```

El framework automáticamente:
1. Suscribe al `Mono`
2. Espera el resultado de forma no bloqueante
3. Serializa la respuesta JSON
4. Retorna al cliente

### Manejo de Excepciones Reactivo

```java
@ExceptionHandler(RuntimeException.class)
public Mono<ResponseEntity<ErrorResponse>> handleException(RuntimeException ex) {
    ErrorResponse error = new ErrorResponse(...);
    return Mono.just(ResponseEntity.status(404).body(error));
}
```

## 🔍 Query Compleja Reactiva

Query para obtener productos con mayor stock por sucursal:

```java
@Query("""
    SELECT p.* FROM products p
    INNER JOIN branches b ON p.branch_id = b.id
    WHERE b.franchise_id = :franchiseId
    AND p.stock = (
        SELECT MAX(p2.stock)
        FROM products p2
        WHERE p2.branch_id = p.branch_id
    )
    ORDER BY b.name, p.name
""")
Flux<Product> findTopStockProductsByFranchise(Long franchiseId);
```

Servicio que combina los resultados:
```java
public Flux<ProductResponse> getTopStockProductsByFranchise(Long franchiseId) {
    return franchiseRepository.findById(franchiseId)
        .switchIfEmpty(Mono.error(new RuntimeException("Franchise not found")))
        .flatMapMany(franchise ->
            productRepository.findTopStockProductsByFranchise(franchiseId)
                .flatMap(product ->
                    branchRepository.findById(product.getBranchId())
                        .map(branch -> new ProductResponse(
                            product.getId(),
                            product.getName(),
                            product.getStock(),
                            branch.getId(),
                            branch.getName()
                        ))
                )
        );
}
```

## 📊 Configuración R2DBC

**application.yaml:**
```yaml
spring:
  r2dbc:
    url: r2dbc:mysql://localhost:3306/mydatabase
    username: myuser
    password: secret

logging:
  level:
    io.r2dbc: DEBUG
    org.springframework.data.r2dbc: DEBUG
```

**Inicialización de Schema:**
```java
@Configuration
public class DatabaseConfig {
    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));

        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
```

## ✅ Ventajas de esta Implementación

1. **No Bloqueante**: Ninguna operación I/O bloquea threads
2. **Escalable**: Maneja miles de conexiones con pocos threads
3. **Eficiente**: Menor uso de memoria y CPU
4. **Backpressure**: Control automático de flujo de datos
5. **Composable**: Fácil combinación de operaciones asíncronas

## ⚠️ Consideraciones

1. **Sin ORM**: R2DBC no tiene ORM como Hibernate, las relaciones se manejan manualmente
2. **Curva de aprendizaje**: Requiere entender operadores reactivos
3. **Debugging**: Más complejo que código imperativo
4. **Testing**: Requiere `reactor-test` para testing reactivo

## 🧪 Testing Reactivo

```java
@Test
void testCreateProduct() {
    ProductRequest request = new ProductRequest("Laptop", 50);

    Mono<Product> result = service.createProduct(1L, request);

    StepVerifier.create(result)
        .assertNext(product -> {
            assertThat(product.getName()).isEqualTo("Laptop");
            assertThat(product.getStock()).isEqualTo(50);
        })
        .verifyComplete();
}
```

## 📈 Comparación de Performance

| Métrica | Spring MVC + JPA | Spring WebFlux + R2DBC |
|---------|------------------|------------------------|
| Threads | 1 por request | Event loop (pocos) |
| Memoria | Alta (thread stack) | Baja |
| Latencia | Media | Baja |
| Throughput | Limitado por threads | Alto |
| Conexiones DB | Bloqueantes | No bloqueantes |

## 🎓 Recursos de Aprendizaje

- [Project Reactor Reference](https://projectreactor.io/docs/core/release/reference/)
- [Spring WebFlux Docs](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [R2DBC Specification](https://r2dbc.io/spec/1.0.0.RELEASE/spec/html/)

---

**Conclusión**: Este proyecto implementa programación reactiva end-to-end, desde el controlador hasta la base de datos, utilizando las mejores prácticas de Spring WebFlux y R2DBC.