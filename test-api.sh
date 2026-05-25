#!/bin/bash

# Script de prueba para la API de Franchise Management

API_URL=${API_URL:-http://localhost:8080}

echo "========================================="
echo "Franchise Management API - Test Script"
echo "API URL: $API_URL"
echo "========================================="

# Verificar que la API está activa
echo -e "\n🔍 Verificando que la API está activa..."
if ! curl -s -o /dev/null -w "%{http_code}" $API_URL/actuator/health | grep -q "200"; then
    echo "❌ Error: La API no está respondiendo en $API_URL"
    echo "Por favor, asegúrate de que la aplicación esté corriendo con: ./gradlew bootRun"
    exit 1
fi
echo "✅ API está activa"

# 1. Crear franquicia
echo -e "\n📝 1. Creando franquicia 'Franquicia Principal'..."
FRANCHISE=$(curl -s -X POST $API_URL/api/franchises \
  -H "Content-Type: application/json" \
  -d '{"name": "Franquicia Principal"}')
echo $FRANCHISE | jq
FRANCHISE_ID=$(echo $FRANCHISE | jq -r '.id')
echo "✅ Franquicia creada con ID: $FRANCHISE_ID"

# 2. Agregar sucursales
echo -e "\n🏢 2. Agregando sucursales..."
BRANCH1=$(curl -s -X POST $API_URL/api/franchises/$FRANCHISE_ID/branches \
  -H "Content-Type: application/json" \
  -d '{"name": "Sucursal Norte"}')
BRANCH1_ID=$(echo $BRANCH1 | jq -r '.id')
echo "✅ Sucursal Norte creada con ID: $BRANCH1_ID"

BRANCH2=$(curl -s -X POST $API_URL/api/franchises/$FRANCHISE_ID/branches \
  -H "Content-Type: application/json" \
  -d '{"name": "Sucursal Sur"}')
BRANCH2_ID=$(echo $BRANCH2 | jq -r '.id')
echo "✅ Sucursal Sur creada con ID: $BRANCH2_ID"

# 3. Agregar productos a Sucursal Norte
echo -e "\n📦 3. Agregando productos a Sucursal Norte..."
PROD1=$(curl -s -X POST $API_URL/api/franchises/branches/$BRANCH1_ID/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Laptop Dell", "stock": 50}')
echo "✅ Producto: Laptop Dell (Stock: 50)"

PROD2=$(curl -s -X POST $API_URL/api/franchises/branches/$BRANCH1_ID/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Mouse Logitech", "stock": 120}')
echo "✅ Producto: Mouse Logitech (Stock: 120)"

PROD3=$(curl -s -X POST $API_URL/api/franchises/branches/$BRANCH1_ID/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Teclado Mecánico", "stock": 80}')
echo "✅ Producto: Teclado Mecánico (Stock: 80)"

# 4. Agregar productos a Sucursal Sur
echo -e "\n📦 4. Agregando productos a Sucursal Sur..."
PROD4=$(curl -s -X POST $API_URL/api/franchises/branches/$BRANCH2_ID/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Monitor Samsung", "stock": 95}')
echo "✅ Producto: Monitor Samsung (Stock: 95)"

PROD5=$(curl -s -X POST $API_URL/api/franchises/branches/$BRANCH2_ID/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Webcam HD", "stock": 150}')
echo "✅ Producto: Webcam HD (Stock: 150)"

PROD6=$(curl -s -X POST $API_URL/api/franchises/branches/$BRANCH2_ID/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Auriculares", "stock": 60}')
echo "✅ Producto: Auriculares (Stock: 60)"

# 5. Obtener productos con mayor stock
echo -e "\n🏆 5. Obteniendo productos con MAYOR STOCK por sucursal..."
echo "Esperado: Mouse Logitech (120) de Sucursal Norte y Webcam HD (150) de Sucursal Sur"
TOP_PRODUCTS=$(curl -s -X GET $API_URL/api/franchises/$FRANCHISE_ID/top-stock-products)
echo $TOP_PRODUCTS | jq
echo "✅ Consulta ejecutada correctamente"

# 6. Actualizar stock
echo -e "\n🔄 6. Actualizando stock de 'Laptop Dell' a 200..."
PROD1_ID=$(echo $PROD1 | jq -r '.id')
curl -s -X PATCH $API_URL/api/franchises/products/$PROD1_ID/stock \
  -H "Content-Type: application/json" \
  -d '{"stock": 200}' | jq
echo "✅ Stock actualizado"

# 7. Verificar productos con mayor stock después de actualización
echo -e "\n🏆 7. Verificando productos con MAYOR STOCK después de actualización..."
echo "Esperado: Laptop Dell (200) de Sucursal Norte y Webcam HD (150) de Sucursal Sur"
curl -s -X GET $API_URL/api/franchises/$FRANCHISE_ID/top-stock-products | jq
echo "✅ Consulta ejecutada correctamente"

# 8. Actualizar nombres
echo -e "\n✏️  8. Actualizando nombres..."
curl -s -X PATCH $API_URL/api/franchises/$FRANCHISE_ID/name \
  -H "Content-Type: application/json" \
  -d '{"name": "Franquicia Premium"}' > /dev/null
echo "✅ Nombre de franquicia actualizado a 'Franquicia Premium'"

curl -s -X PATCH $API_URL/api/franchises/branches/$BRANCH1_ID/name \
  -H "Content-Type: application/json" \
  -d '{"name": "Sucursal Centro"}' > /dev/null
echo "✅ Nombre de sucursal actualizado a 'Sucursal Centro'"

# 9. Eliminar producto
echo -e "\n🗑️  9. Eliminando producto 'Auriculares'..."
PROD6_ID=$(echo $PROD6 | jq -r '.id')
curl -s -X DELETE $API_URL/api/franchises/products/$PROD6_ID
echo "✅ Producto eliminado"

# 10. Mostrar estado final
echo -e "\n📊 10. Estado final de la franquicia..."
curl -s -X GET $API_URL/api/franchises/$FRANCHISE_ID | jq
echo "✅ Test completado exitosamente"

echo -e "\n========================================="
echo "✅ Todas las pruebas completadas"
echo "========================================="
echo -e "\nPuedes ver la documentación Swagger en:"
echo "  $API_URL/swagger-ui.html"
echo -e "\nPara más ejemplos, consulta el archivo API_EXAMPLES.md"