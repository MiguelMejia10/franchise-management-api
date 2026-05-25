# API Testing Examples

Ejemplos de uso de la API con curl.

## Variables de entorno

```bash
export API_URL=http://localhost:8080
```

## 1. Crear Franquicia

```bash
curl -X POST $API_URL/api/franchises \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Franquicia Principal"
  }'
```

Respuesta esperada:
```json
{
  "id": 1,
  "name": "Franquicia Principal",
  "branches": []
}
```

## 2. Agregar Sucursal a Franquicia

```bash
curl -X POST $API_URL/api/franchises/1/branches \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sucursal Norte"
  }'
```

## 3. Agregar otra Sucursal

```bash
curl -X POST $API_URL/api/franchises/1/branches \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sucursal Sur"
  }'
```

## 4. Agregar Productos a Sucursal Norte (ID: 1)

```bash
# Producto 1
curl -X POST $API_URL/api/franchises/branches/1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop Dell",
    "stock": 50
  }'

# Producto 2
curl -X POST $API_URL/api/franchises/branches/1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mouse Logitech",
    "stock": 120
  }'

# Producto 3
curl -X POST $API_URL/api/franchises/branches/1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Teclado Mecánico",
    "stock": 80
  }'
```

## 5. Agregar Productos a Sucursal Sur (ID: 2)

```bash
# Producto 1
curl -X POST $API_URL/api/franchises/branches/2/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Monitor Samsung",
    "stock": 95
  }'

# Producto 2
curl -X POST $API_URL/api/franchises/branches/2/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Webcam HD",
    "stock": 150
  }'

# Producto 3
curl -X POST $API_URL/api/franchises/branches/2/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Auriculares",
    "stock": 60
  }'
```

## 6. Actualizar Stock de un Producto

```bash
curl -X PATCH $API_URL/api/franchises/products/1/stock \
  -H "Content-Type: application/json" \
  -d '{
    "stock": 75
  }'
```

## 7. Eliminar un Producto

```bash
curl -X DELETE $API_URL/api/franchises/products/3
```

## 8. Obtener Productos con Mayor Stock por Sucursal

Este endpoint devuelve el producto con mayor stock de cada sucursal de la franquicia.

```bash
curl -X GET $API_URL/api/franchises/1/top-stock-products
```

Respuesta esperada:
```json
[
  {
    "id": 2,
    "name": "Mouse Logitech",
    "stock": 120,
    "branchId": 1,
    "branchName": "Sucursal Norte"
  },
  {
    "id": 5,
    "name": "Webcam HD",
    "stock": 150,
    "branchId": 2,
    "branchName": "Sucursal Sur"
  }
]
```

## 9. Actualizar Nombre de Franquicia

```bash
curl -X PATCH $API_URL/api/franchises/1/name \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Franquicia Premium"
  }'
```

## 10. Actualizar Nombre de Sucursal

```bash
curl -X PATCH $API_URL/api/franchises/branches/1/name \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sucursal Centro"
  }'
```

## 11. Actualizar Nombre de Producto

```bash
curl -X PATCH $API_URL/api/franchises/products/1/name \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop HP EliteBook"
  }'
```

## 12. Listar Todas las Franquicias

```bash
curl -X GET $API_URL/api/franchises
```

## 13. Obtener Franquicia por ID

```bash
curl -X GET $API_URL/api/franchises/1
```

## Script de Prueba Completo

```bash
#!/bin/bash

API_URL=http://localhost:8080

echo "1. Creando franquicia..."
FRANCHISE=$(curl -s -X POST $API_URL/api/franchises \
  -H "Content-Type: application/json" \
  -d '{"name": "Franquicia Principal"}')
echo $FRANCHISE | jq

echo -e "\n2. Agregando sucursales..."
BRANCH1=$(curl -s -X POST $API_URL/api/franchises/1/branches \
  -H "Content-Type: application/json" \
  -d '{"name": "Sucursal Norte"}')
echo $BRANCH1 | jq

BRANCH2=$(curl -s -X POST $API_URL/api/franchises/1/branches \
  -H "Content-Type: application/json" \
  -d '{"name": "Sucursal Sur"}')
echo $BRANCH2 | jq

echo -e "\n3. Agregando productos a Sucursal Norte..."
curl -s -X POST $API_URL/api/franchises/branches/1/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Laptop Dell", "stock": 50}' | jq

curl -s -X POST $API_URL/api/franchises/branches/1/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Mouse Logitech", "stock": 120}' | jq

echo -e "\n4. Agregando productos a Sucursal Sur..."
curl -s -X POST $API_URL/api/franchises/branches/2/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Monitor Samsung", "stock": 95}' | jq

curl -s -X POST $API_URL/api/franchises/branches/2/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Webcam HD", "stock": 150}' | jq

echo -e "\n5. Obteniendo productos con mayor stock..."
curl -s -X GET $API_URL/api/franchises/1/top-stock-products | jq

echo -e "\n6. Actualizando stock de producto..."
curl -s -X PATCH $API_URL/api/franchises/products/1/stock \
  -H "Content-Type: application/json" \
  -d '{"stock": 200}' | jq

echo -e "\n7. Verificando productos con mayor stock después de actualización..."
curl -s -X GET $API_URL/api/franchises/1/top-stock-products | jq
```

## Errores de Validación

### Nombre vacío
```bash
curl -X POST $API_URL/api/franchises \
  -H "Content-Type: application/json" \
  -d '{"name": ""}'
```

Respuesta:
```json
{
  "timestamp": "2026-05-22T10:30:00",
  "status": 400,
  "errors": {
    "name": "Name is required"
  }
}
```

### Stock negativo
```bash
curl -X POST $API_URL/api/franchises/branches/1/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Test", "stock": -10}'
```

Respuesta:
```json
{
  "timestamp": "2026-05-22T10:30:00",
  "status": 400,
  "errors": {
    "stock": "Stock must be greater than or equal to 0"
  }
}
```

### Recurso no encontrado
```bash
curl -X GET $API_URL/api/franchises/999
```

Respuesta:
```json
{
  "timestamp": "2026-05-22T10:30:00",
  "status": 404,
  "message": "Franchise not found with id: 999"
}
```