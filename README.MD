# ms-bff

Backend For Frontend del sistema Cloud Native.

## Descripción

`ms-bff` funciona como punto de entrada backend para el frontend.

Su objetivo es evitar que el cliente necesite conocer directamente las direcciones y detalles internos de cada microservicio.

## Tecnologías

- Java
- Spring Boot
- Spring Web
- Spring Security
- JWT
- Microsoft Entra ID
- Docker
- GitHub Actions

## Puerto

```text
8083
```

## Responsabilidades

- Recibir solicitudes del frontend.
- Centralizar comunicación con microservicios.
- Validar JWT.
- Aplicar autorización.
- Exponer endpoints públicos.
- Exponer endpoints privados.
- Gestionar pedidos de invitados.
- Permitir seguimiento de pedidos.
- Consultar productos.

## Seguridad

El sistema utiliza Microsoft Entra ID como proveedor de identidad.

El frontend obtiene un JWT y lo utiliza en las llamadas protegidas al backend.

## Endpoints públicos conocidos

### Productos

```http
GET /api/bff/productos/**
```

### Crear pedido como invitado

```http
POST /api/bff/pedidos/invitado
```

### Seguimiento de pedido

```http
GET /api/bff/pedidos/seguimiento/{codigo}
```

## Integración con microservicios

El BFF se comunica principalmente con:

```text
ms-pedidos
ms-catalogo
```

Arquitectura:

```text
Frontend React
      |
      v
AWS API Gateway
      |
      v
ms-bff
   /       \
  v         v
Pedidos   Catálogo
```

## Gestión de stock

El BFF no modifica directamente el stock.

El flujo correcto es:

```text
BFF
 |
 v
ms-pedidos
 |
 | pedido cambia a ACEPTADO
 v
RabbitMQ
 |
 v
ms-catalogo
 |
 v
Actualización de stock
```

## Pedido de invitado

El sistema permite crear pedidos sin autenticación.

El seguimiento se realiza mediante:

```http
GET /api/bff/pedidos/seguimiento/{codigo}
```

## API Gateway

El BFF se encuentra detrás de AWS API Gateway.

Las integraciones deben apuntar a las rutas correctas del BFF.

## Variables de entorno

Dependiendo de la configuración:

```text
SERVER_PORT
PEDIDOS_SERVICE_URL
CATALOGO_SERVICE_URL

AZURE_TENANT_ID
AZURE_CLIENT_ID

JWT_*
```

No deben almacenarse secretos reales en Git.

## Compilación

Windows:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Linux:

```bash
./mvnw clean package -DskipTests
```

## Docker

```bash
docker build -t ms-bff .
```

Ejemplo:

```bash
docker run -d \
  --name ms-bff \
  --restart unless-stopped \
  --network cloudnative-network \
  -p 8083:8083 \
  --env-file bff.env \
  ms-bff
```

## Flujo general

```text
Usuario
  |
  v
Frontend React
  |
  v
Microsoft Entra ID
  |
  | JWT
  v
AWS API Gateway
  |
  v
ms-bff
  |
  +------------+
  |            |
  v            v
ms-pedidos  ms-catalogo
```

## Buenas prácticas

- BFF como punto de entrada al backend.
- JWT para rutas protegidas.
- Endpoints públicos definidos explícitamente.
- Separación de responsabilidades.
- Variables sensibles fuera del repositorio.
- Docker.
- Despliegue automatizado.